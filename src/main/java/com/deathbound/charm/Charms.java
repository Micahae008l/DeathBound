package com.deathbound.charm;

import com.deathbound.item.CharmItem;
import com.deathbound.npc.Rewards;
import com.deathbound.registry.ModAttachments;
import com.deathbound.registry.ModBlocks;
import com.deathbound.registry.ModEffects;
import com.deathbound.registry.ModItems;
import com.deathbound.registry.ModParticles;
import com.deathbound.world.Dread;
import com.deathbound.world.UnderworldTravel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Prediction;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;

public final class Charms {
   public static final float REAPER_THRESHOLD = 0.3F;
   public static final float COLLECTOR_CHANCE = 0.35F;
   public static final int PHANTOM_COOLDOWN = 600;
   // when each player's Phantom Charm can phase again (server tick)
   private static final Map<UUID, Integer> PHANTOM_READY = new HashMap<>();

   public static ItemStack findRelic(Player player) {
      Inventory inv = player.getInventory();

      for (int i = 0; i < inv.getContainerSize(); i++) {
         ItemStack s = inv.getItem(i);
         if (s.is(ModItems.DEATHBOUND_RELIC)) {
            return s;
         }
      }

      return ItemStack.EMPTY;
   }

   public static int slots(ItemStack relic) {
      return relic.getOrDefault(ModItems.AWAKENED, false) ? 4 : 3;
   }

   private static NonNullList<ItemStack> contents(ItemStack relic) {
      NonNullList<ItemStack> list = NonNullList.withSize(slots(relic), ItemStack.EMPTY);
      relic.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(list);
      return list;
   }

   public static boolean has(Player player, Charm charm) {
      ItemStack relic = findRelic(player);
      if (relic.isEmpty()) {
         return false;
      }

      for (ItemStack s : contents(relic)) {
         if (s.getItem() instanceof CharmItem c && c.charm == charm) {
            return true;
         }
      }

      return false;
   }

   private static void wear(Player player, Charm charm, int amount) {
      ItemStack relic = findRelic(player);
      NonNullList<ItemStack> list = contents(relic);

      for (int i = 0; i < list.size(); i++) {
         ItemStack s = list.get(i);
         if (s.getItem() instanceof CharmItem c && c.charm == charm) {
            if (s.isDamageableItem() && s.getDamageValue() + amount < s.getMaxDamage()) {
               s.setDamageValue(s.getDamageValue() + amount);
            } else {
               list.set(i, ItemStack.EMPTY);
               player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS, 1.0F, 0.6F);
               player.sendSystemMessage(Component.translatable("message.deathbound.charm.shattered", s.getHoverName()).withStyle(ChatFormatting.DARK_PURPLE));
            }

            relic.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(list));
            return;
         }
      }
   }

   public static void spendFerryman(Player player) {
      wear(player, Charm.FERRYMAN, 1);
   }

   public static void init() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            tick(p);
         }
      });
      ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !(entity instanceof ServerPlayer player && phantomPhase(player, source, amount)));
      ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
         if (entity instanceof ServerPlayer player && !player.level().getGameRules().get(GameRules.KEEP_INVENTORY)) {
            stashSoulboundItems(player);
            if (UnderworldTravel.inUnderworld(player) && !heldTotem(player, source)) {
               ferryKeep(player);
            }
         }

         return true;
      });
      ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
         if (entity instanceof AbstractArrow arrow && arrow.tickCount == 0 && arrow.getOwner() instanceof ServerPlayer p && has(p, Charm.HUNTER)) {
            arrow.setDeltaMovement(arrow.getDeltaMovement().scale(1.35));
            arrow.setBaseDamage(3.0);
         }
      });
      ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
         if (source.getEntity() instanceof ServerPlayer player && entity instanceof Enemy) {
            if (has(player, Charm.WRAITH)) {
               feedWraith(player, entity);
            }

            if (has(player, Charm.COLLECTOR) && UnderworldTravel.inUnderworld(player) && player.getRandom().nextFloat() < COLLECTOR_CHANCE) {
               collectSoul(player, entity);
            }
         }
      });

   }

   private static void stashSoulboundItems(ServerPlayer player) {
      boolean keepAll = has(player, Charm.SOULBOUND);
      if (keepAll) {
         wear(player, Charm.SOULBOUND, 1);
      }

      Inventory inv = player.getInventory();
      List<ModAttachments.Kept> kept = new ArrayList<>(player.getAttachedOrElse(ModAttachments.SOUL_STASH, List.of()));

      for (int i = 0; i < inv.getContainerSize(); i++) {
         ItemStack s = inv.getItem(i);
         if (!s.isEmpty() && (keepAll || s.is(ModItems.DEATHBOUND_RELIC))) {
            kept.add(new ModAttachments.Kept(i, s.copy()));
            inv.setItem(i, ItemStack.EMPTY);
         }
      }

      if (!kept.isEmpty()) {
         player.setAttached(ModAttachments.SOUL_STASH, kept);
      }

      if (keepAll) {
         player.sendSystemMessage(Component.translatable("message.deathbound.soulbound.saved").withStyle(ChatFormatting.LIGHT_PURPLE));
      }
   }

   private static boolean heldTotem(ServerPlayer player, DamageSource source) {
      if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         return false;
      }

      for (InteractionHand hand : InteractionHand.values()) {
         if (player.getItemInHand(hand).has(DataComponents.DEATH_PROTECTION)) {
            return true;
         }
      }

      return false;
   }

   private static void ferryKeep(ServerPlayer player) {
      Inventory inv = player.getInventory();
      List<ItemStack> kept = new ArrayList<>(player.getAttachedOrElse(ModAttachments.FERRY_KEEP, List.of()));

      for (int i = 0; i < inv.getContainerSize(); i++) {
         ItemStack s = inv.getItem(i);
         if (!s.isEmpty()) {
            kept.add(s.copy());
            inv.setItem(i, ItemStack.EMPTY);
         }
      }

      if (!kept.isEmpty()) {
         player.setAttached(ModAttachments.FERRY_KEEP, kept);
      }
   }

   public static boolean reclaim(ServerPlayer player) {
      List<ItemStack> kept = player.removeAttached(ModAttachments.FERRY_KEEP);
      if (kept != null && !kept.isEmpty()) {
         Rewards.give(
            player, Component.translatable("rewards.deathbound.from", Component.translatable("entity.deathbound.ferryman")), kept.toArray(ItemStack[]::new)
         );
         return true;
      } else {
         return false;
      }
   }

   private static void restoreStash(ServerPlayer player) {
      List<ModAttachments.Kept> kept = player.removeAttached(ModAttachments.SOUL_STASH);
      if (kept != null) {
         Inventory inv = player.getInventory();

         for (ModAttachments.Kept k : kept) {
            ItemStack s = k.stack().copy();
            if (k.slot() >= 0 && k.slot() < inv.getContainerSize() && inv.getItem(k.slot()).isEmpty()) {
               inv.setItem(k.slot(), s);
            } else if (!inv.add(s)) {
               player.drop(s, false, Prediction.SERVER_ONLY);
            }
         }
      }
   }

   private static void tick(ServerPlayer player) {
      if (!player.isDeadOrDying()) {
         if (player.hasAttached(ModAttachments.SOUL_STASH)) {
            restoreStash(player);
         }

         int t = player.tickCount;
         if (t % 10 == 0 && has(player, Charm.REAPER) && player.getHealth() <= player.getMaxHealth() * 0.3F) {
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 30, 1, true, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 30, 1, true, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 30, 0, true, true, true));
            if (t % 40 == 0) {
               player.level().sendParticles(ModParticles.SOUL_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 6, 0.3, 0.5, 0.3, 0.02);
            }
         }

         if (t % 60 == 0 && has(player, Charm.SEER)) {
            seerPulse(player);
         }

         Integer ready = PHANTOM_READY.get(player.getUUID());
         if (ready != null && player.level().getServer().getTickCount() >= ready) {
            PHANTOM_READY.remove(player.getUUID());
            if (has(player, Charm.PHANTOM)) {
               // only the wearer hears it: the charm is ready again
               Dread.whisperTo(player, SoundEvents.SOUL_ESCAPE.value(), player.position(), 0.6F, 1.5F, player.getRandom());
            }
         }
      }
   }

   private static void seerPulse(ServerPlayer player) {
      ServerLevel level = player.level();

      for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(32.0), m -> m instanceof Enemy)) {
         mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 70, 0, true, false));
      }

      BlockPos c = player.blockPosition();

      for (int cx = -1; cx <= 1; cx++) {
         for (int cz = -1; cz <= 1; cz++) {
            for (BlockEntity be : level.getChunkAt(c.offset(cx * 16, 0, cz * 16)).getBlockEntities().values()) {
               BlockPos p = be.getBlockPos();
               // only chests nobody has opened yet: Minecraft clears the loot table on first open
               if (be instanceof RandomizableContainer loot && loot.getLootTable() != null && p.distSqr(c) < 576.0) {
                  level.sendParticles(player, ModParticles.SOUL_FLAME, true, false, p.getX() + 0.5, p.getY() + 1.2, p.getZ() + 0.5, 8, 0.15, 0.4, 0.15, 0.01);
               }
            }
         }
      }

      for (BlockPos p : BlockPos.betweenClosed(c.offset(-10, -6, -10), c.offset(10, 6, 10))) {
         if (level.getBlockState(p).is(ModBlocks.VEILED_SOULSTONE)) {
            level.sendParticles(player, ModParticles.SOUL_MOTE, true, false, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 3, 0.35, 0.35, 0.35, 0.0);
         }
      }

      level.playSound(null, c, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.35F, 0.5F);
   }

   private static void collectSoul(ServerPlayer player, LivingEntity victim) {
      ServerLevel level = player.level();
      victim.spawnAtLocation(level, new ItemStack(ModItems.SOUL));
      level.sendParticles(ModParticles.SOUL_MOTE, victim.getX(), victim.getY() + victim.getBbHeight() * 0.5, victim.getZ(), 10, 0.3, 0.4, 0.3, 0.02);
      level.playSound(null, victim.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9F, 0.6F);
   }

   // Phantom Charm: once every PHANTOM_COOLDOWN ticks, a hit from a mob or a projectile passes through you.
   // Hits that would be ignored anyway (creative, hurt cooldown, a raised shield) don't use it up.
   private static boolean phantomPhase(ServerPlayer player, DamageSource source, float amount) {
      if (amount <= 0.0F || source.getEntity() == null || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
         || player.isCreative() || player.isSpectator() || player.getInvulnerableTime() > 10 || player.isBlocking() || player.isInvulnerableTo(player.level(), source)
         || PHANTOM_READY.containsKey(player.getUUID()) || !has(player, Charm.PHANTOM)) {
         return false;
      }

      ServerLevel level = player.level();
      PHANTOM_READY.put(player.getUUID(), level.getServer().getTickCount() + PHANTOM_COOLDOWN);
      player.setInvulnerableTime(20);
      player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 12, 0, true, false, false));
      level.sendParticles(ModParticles.SOUL_MOTE, player.getX(), player.getY() + 1.0, player.getZ(), 24, 0.35, 0.6, 0.35, 0.03);
      level.playSound(null, player.blockPosition(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 1.0F, 0.6F);
      level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
      return true;
   }

   private static void feedWraith(ServerPlayer player, LivingEntity victim) {
      MobEffectInstance current = player.getEffect(ModEffects.WRAITH_FURY);
      int amp = current == null ? 0 : Math.min(current.getAmplifier() + 1, 4);
      player.addEffect(new MobEffectInstance(ModEffects.WRAITH_FURY, 200, amp, false, true, true));
      player.heal(2.0F);
      ServerLevel level = player.level();
      Vec3 from = victim.position().add(0.0, victim.getBbHeight() * 0.6, 0.0);
      Vec3 to = player.position().add(0.0, 1.0, 0.0);

      for (int i = 0; i <= 10; i++) {
         Vec3 p = from.lerp(to, i / 10.0);
         level.sendParticles(ModParticles.SOUL_FLAME, p.x, p.y + Math.sin(i * 0.6) * 0.3, p.z, 1, 0.05, 0.05, 0.05, 0.0);
      }

      level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.7F + amp * 0.12F);
   }

   private Charms() {
   }
}
