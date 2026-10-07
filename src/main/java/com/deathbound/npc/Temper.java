package com.deathbound.npc;

import com.deathbound.DeathBound;
import com.deathbound.entity.Gravebound;
import com.deathbound.entity.SoulWisp;
import com.deathbound.registry.ModEntities;
import com.deathbound.registry.ModItems;
import com.deathbound.registry.ModParticles;
import com.deathbound.world.Director;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.phys.Vec3;

public final class Temper {
   public static final DataComponentType<Integer> TEMPERED = Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      DeathBound.id("tempered"),
      DataComponentType.<Integer>builder().persistent(Codec.intRange(0, 3)).networkSynchronized(ByteBufCodecs.VAR_INT).build()
   );
   static final Temper.Rank[] RANKS = new Temper.Rank[]{null, new Temper.Rank(1, 12, 1.5), new Temper.Rank(2, 20, 3.0), new Temper.Rank(3, 32, 4.5)};
   static final Identifier MODIFIER = DeathBound.id("tempered");

   public static int rank(ItemStack s) {
      return s.getOrDefault(TEMPERED, 0);
   }

   static boolean isWeapon(ItemStack s) {
      return !s.isEmpty() && (s.is(ItemTags.WEAPON_ENCHANTABLE) || s.is(Items.TRIDENT) || s.is(ModItems.REAPER_SCYTHE));
   }

   public static void temper(ServerPlayer p, String which) {
      int want;
      try {
         want = Integer.parseInt(which);
      } catch (NumberFormatException e) {
         return;
      }

      if (want >= 1 && want <= 3) {
         if (!Handiwork.busy(p)) {
            ItemStack w = p.getMainHandItem();
            Director.State st = Director.storyState(p.level());
            Temper.Rank r = RANKS[want];
            int have = rank(w);
            String key;
            if (!isWeapon(w)) {
               key = "none";
            } else if (have >= want) {
               key = "already";
            } else if (have != want - 1) {
               key = "order";
            } else if (want == 2 && st.progress() < 1) {
               key = "gate2";
            } else if (want == 3 && !st.hunterSlain()) {
               key = "gate3";
            } else {
               if (Soulforge.count(p, ModItems.SOUL) >= r.souls() && Soulforge.count(p, ModItems.GRAVE_RUNE) >= r.runes()) {
                  Soulforge.take(p, ModItems.SOUL, r.souls());
                  Soulforge.take(p, ModItems.GRAVE_RUNE, r.runes());
                  int rank = want;
                  Handiwork.quench(p, smith(p), s -> apply(s, rank));
                  return;
               }

               key = "wanting";
            }

            p.sendSystemMessage(
               Component.translatable(
                     "message.deathbound.temper." + key, w.getHoverName(), r.runes(), r.souls(), Component.translatable("temper.deathbound.rank" + want)
                  )
                  .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            );
         }
      }
   }

   static void apply(ItemStack w, int rank) {
      w.set(TEMPERED, rank);
      w.set(
         DataComponents.ATTRIBUTE_MODIFIERS,
         w.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
            .withModifierAdded(
               Attributes.ATTACK_DAMAGE, new AttributeModifier(MODIFIER, RANKS[rank].damage(), Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
            )
      );
      List<Component> lines = new ArrayList<>();

      for (Component c : w.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines()) {
         if (!(c.getContents() instanceof TranslatableContents t && t.getKey().startsWith("temper.deathbound."))) {
            lines.add(c);
         }
      }

      lines.add(Component.translatable("temper.deathbound.rank" + rank).withStyle(ChatFormatting.LIGHT_PURPLE));

      for (int i = 1; i <= rank; i++) {
         lines.add(Component.translatable("temper.deathbound.effect" + i).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
      }

      w.set(DataComponents.LORE, new ItemLore(lines));
      w.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
   }

   private static UnderworldNpc smith(ServerPlayer p) {
      return p.level()
         .getEntitiesOfClass(UnderworldNpc.class, p.getBoundingBox().inflate(10.0), n -> n.getType() == ModEntities.BONESMITH)
         .stream()
         .findFirst()
         .orElse(null);
   }

   static boolean ofTheDead(LivingEntity e) {
      return BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace().equals("deathbound");
   }

   public static void init() {
      ServerLivingEntityEvents.AFTER_DAMAGE
         .register(
            (entity, source, base, taken, blocked) -> {
               int rank = source.getDirectEntity() instanceof Player hitter && source.getEntity() == hitter ? rank(hitter.getMainHandItem()) : 0;
               if (taken > 0.0F && rank >= 1 && entity.level() instanceof ServerLevel level) {
                  level.sendParticles(
                     ModParticles.SOUL_FLAME,
                     entity.getX(),
                     entity.getY() + entity.getBbHeight() * 0.6,
                     entity.getZ(),
                     4 + rank * 4,
                     0.25,
                     0.3,
                     0.25,
                     0.04 + rank * 0.02
                  );
                  level.sendParticles(
                     ParticleTypes.ENCHANTED_HIT, entity.getX(), entity.getY() + entity.getBbHeight() * 0.6, entity.getZ(), 6 * rank, 0.3, 0.3, 0.3, 0.2
                  );
               }

               if (taken > 0.0F
                  && source.getDirectEntity() instanceof Player p
                  && source.getEntity() == p
                  && ofTheDead(entity)
                  && rank(p.getMainHandItem()) >= 2) {
                  entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0, false, false));
                  if (entity instanceof Gravebound || entity instanceof SoulWisp) {
                     entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 1, false, false));
                  }

                  if (entity.level() instanceof ServerLevel level) {
                     level.sendParticles(
                        ModParticles.SOUL_FLAME, entity.getX(), entity.getY() + entity.getBbHeight() * 0.6, entity.getZ(), 6, 0.2, 0.3, 0.2, 0.03
                     );
                  }
               }
            }
         );
      ServerLivingEntityEvents.AFTER_DEATH
         .register(
            (entity, source) -> {
               if (source.getEntity() instanceof ServerPlayer p
                  && ofTheDead(entity)
                  && rank(p.getMainHandItem()) >= 3
                  && entity.level() instanceof ServerLevel level) {
                  p.heal(3.0F);
                  Vec3 from = entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0);
                  Vec3 to = p.position().add(0.0, 1.0, 0.0);

                  for (int i = 0; i <= 8; i++) {
                     Vec3 q = from.lerp(to, i / 8.0);
                     level.sendParticles(ModParticles.SOUL_FLAME, q.x, q.y, q.z, 2, 0.05, 0.05, 0.05, 0.0);
                  }

                  level.playSound(null, p.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
               }
            }
         );
   }

   private Temper() {
   }

   record Rank(int runes, int souls, double damage) {
   }
}
