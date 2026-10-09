package com.deathbound.item;

import com.deathbound.entity.LostSoul;
import com.deathbound.npc.UnderworldNpc;
import com.deathbound.registry.ModItems;
import com.deathbound.registry.ModNet;
import com.deathbound.registry.ModParticles;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ReaperScytheItem extends Item {
   public static final int REAP_COST = 5;
   public static final int REAP_MAX = 10;

   public ReaperScytheItem(Item.Properties properties) {
      super(properties);
   }

   /**
    * What the blade does lands when the blade gets there, not on the click: the swing's slash, flames and slam wait for
    * the animation (ScytheSwing). Checked on screen, one shot per swing: what the server sends N ticks after the swing
    * shows N + 1 ticks after the click (give or take one); the blade crosses from tick 5 to 7 and the slam lands at 6.
    */
   private record Later(int at, Runnable fx) {
   }

   private static final List<Later> LATER = new ArrayList<>();

   private static void later(ServerLevel level, int ticks, Runnable fx) {
      LATER.add(new Later(level.getServer().getTickCount() + ticks, fx));
   }

   public static void init() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         if (!LATER.isEmpty()) {
            int now = server.getTickCount();
            List<Later> due = LATER.stream().filter(l -> l.at() <= now).toList();
            LATER.removeAll(due);
            due.forEach(l -> l.fx().run());
         }
      });
      ServerLifecycleEvents.SERVER_STOPPED.register(server -> LATER.clear());
   }

   /** Still there to finish the swing: alive, in the same world, the scythe still in hand. */
   private static boolean still(LivingEntity user, ServerLevel level) {
      return user.isAlive() && user.level() == level && user.getMainHandItem().is(ModItems.REAPER_SCYTHE);
   }

   @Override
   public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      super.postHurtEnemy(stack, target, attacker);
      if (attacker.level() instanceof ServerLevel level) {
         float var10 = (float)attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6F;
         Vec3 look = Vec3.directionFromRotation(0.0F, attacker.getYRot());

         for (LivingEntity v : level.getEntitiesOfClass(LivingEntity.class, attacker.getBoundingBox().inflate(4.2, 1.5, 4.2))) {
            if (v != target && !spared(v, attacker)) {
               Vec3 to = v.position().subtract(attacker.position()).multiply(1.0, 0.0, 1.0);
               if (!(to.length() > 4.2)
                  && !(Math.toDegrees(Math.acos(Mth.clamp(to.normalize().dot(look), -1.0, 1.0))) > 75.0)
                  && v.hurtServer(level, source(level, attacker), var10)) {
                  v.knockback(0.4, -to.x, -to.z, source(level, attacker), var10);
                  if (!v.isAlive()) {
                     reap(stack, level, attacker, v);
                  }
               }
            }
         }

         later(level, 5, () -> {   // the blade through the target
            if (!still(attacker, level)) {
               return;
            }
            float yaw = attacker.getYRot();
            Vec3 ahead = Vec3.directionFromRotation(0.0F, yaw);
            for (int i = -3; i <= 3; i++) {
               Vec3 p = attacker.position().add(Vec3.directionFromRotation(0.0F, yaw + i * 22).scale(2.6));
               level.sendParticles(ModParticles.SOUL_FLAME, p.x, attacker.getY() + 1.1, p.z, 2, 0.1, 0.05, 0.1, 0.01);
            }

            level.sendParticles(
               ParticleTypes.SWEEP_ATTACK, attacker.getX() + ahead.x * 2.0, attacker.getY() + 1.1, attacker.getZ() + ahead.z * 2.0, 1, 0.0, 0.0, 0.0, 0.0
            );
            level.playSound(null, attacker.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.65F);
         });
         if (!target.isAlive()) {
            reap(stack, level, attacker, target);
         }
      }
   }

   private static DamageSource source(ServerLevel level, LivingEntity attacker) {
      return attacker instanceof Player p ? level.damageSources().playerAttack(p) : level.damageSources().mobAttack(attacker);
   }

   private static boolean spared(LivingEntity v, LivingEntity user) {
      return v == user || v.isAlliedTo(user) || v instanceof ArmorStand || v instanceof LostSoul || v instanceof Player || v instanceof UnderworldNpc;
   }

   public static void onSwing(LivingEntity user, int step) {
      if (user.level() instanceof ServerLevel level) {
         if (step < 2) {
            int dir = step == 0 ? 1 : -1;   // the first cut goes right to left, the backhand left to right
            level.playSound(null, user.blockPosition(), SoundEvents.BREEZE_WHIRL, SoundSource.PLAYERS, 0.5F, 1.4F);

            for (int k = 0; k < 3; k++) {   // the slash follows the blade across, a tick a piece
               int i = 1 - k;
               later(level, 4 + k, () -> {
                  if (!still(user, level)) {
                     return;
                  }
                  Vec3 p = user.position().add(Vec3.directionFromRotation(0.0F, user.getYRot() + dir * i * 40).scale(2.4));
                  level.sendParticles(ModParticles.SOUL_SWEEP, p.x, user.getY() + 1.1 - i * 0.08 * dir, p.z, 0, 0.95, 0.0, 0.0, 1.0);
                  if (i == 0) {
                     level.playSound(null, user.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.1F, step == 0 ? 0.55F : 0.68F);
                  }
               });
            }
         } else {
            // raised overhead now; it all lands when the blade hits the ground
            level.playSound(null, user.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.PLAYERS, 1.1F, 0.7F);
            later(level, 5, () -> {
               if (still(user, level)) {
                  slam(level, user);
               }
            });
         }
      }
   }

   private static void slam(ServerLevel level, LivingEntity user) {
      float yaw = user.getYRot();
      ItemStack stack = user.getMainHandItem();
      float damage = (float)user.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.9F;

      for (LivingEntity v : level.getEntitiesOfClass(LivingEntity.class, user.getBoundingBox().inflate(3.8, 1.5, 3.8))) {
         if (!spared(v, user) && !(v.distanceTo(user) > 3.8) && v.hurtServer(level, source(level, user), damage)) {
            Vec3 in = user.position().subtract(v.position()).multiply(1.0, 0.0, 1.0).normalize();
            v.push(in.x * 0.45, 0.3, in.z * 0.45);
            v.needsSync = true;
            if (!v.isAlive()) {
               reap(stack, level, user, v);
            }
         }
      }

      for (int k = 0; k < 6; k++) {
         Vec3 p = user.position().add(Vec3.directionFromRotation(0.0F, yaw + k * 60).scale(2.6));
         level.sendParticles(ModParticles.SOUL_SWEEP, p.x, user.getY() + 0.9, p.z, 0, 1.25, 0.0, 0.0, 1.0);
      }

      for (int i = 0; i < 24; i++) {
         double a = i * 3.141592653589793 / 12.0;
         level.sendParticles(
            ModParticles.SOUL_FLAME, user.getX() + Math.cos(a) * 3.2, user.getY() + 0.1, user.getZ() + Math.sin(a) * 3.2, 1, 0.0, 0.05, 0.0, 0.02
         );
      }

      level.playSound(null, user.blockPosition(), SoundEvents.MACE_SMASH_GROUND, SoundSource.PLAYERS, 0.9F, 0.7F);
      if (user instanceof ServerPlayer sp) {
         ServerPlayNetworking.send(sp, new ModNet.Shake(0.9F, 6));
      }
   }

   private static void dash(Level level, Player player, ItemStack stack) {
      Vec3 dir = player.getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
      player.setDeltaMovement(dir.x * 1.9, 0.2, dir.z * 1.9);
      player.getCooldowns().addCooldown(stack, 45);
      if (level instanceof ServerLevel server) {
         player.needsSync = true;
         player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 8, 2, false, false));
         Vec3 from = player.position();
         Vec3 to = from.add(dir.scale(6.5));
         float damage = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.7F;

         for (LivingEntity v : server.getEntitiesOfClass(LivingEntity.class, new AABB(from, to).inflate(1.6, 1.2, 1.6))) {
            if (!spared(v, player)) {
               Vec3 rel = v.position().subtract(from);
               double along = Mth.clamp(rel.dot(dir), 0.0, 6.5);
               if (rel.subtract(dir.scale(along)).multiply(1.0, 0.0, 1.0).length() < 1.6
                  && v.hurtServer(server, source(server, player), damage)
                  && !v.isAlive()) {
                  reap(stack, server, player, v);
               }
            }
         }

         for (int i = 0; i <= 10; i++) {
            Vec3 p = from.lerp(to, i / 10.0);
            server.sendParticles(ModParticles.SOUL_FLAME, p.x, p.y + 0.9, p.z, 2, 0.2, 0.4, 0.2, 0.01);
         }

         server.sendParticles(ModParticles.SOUL_SWEEP, to.x, to.y + 1.0, to.z, 0, 1.8, 0.0, 0.0, 1.0);
         server.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.PLAYERS, 1.0F, 0.9F);
         server.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.4F, 0.6F);
      }
   }

   private static void reap(ItemStack stack, ServerLevel level, LivingEntity reaper, LivingEntity victim) {
      int n = Math.min(stack.getOrDefault(ModItems.REAPED, 0) + 1, 10);
      stack.set(ModItems.REAPED, n);
      Vec3 from = victim.position().add(0.0, victim.getBbHeight() * 0.6, 0.0);
      Vec3 to = reaper.position().add(0.0, 1.0, 0.0);

      for (int i = 0; i <= 12; i++) {
         Vec3 p = from.lerp(to, i / 12.0);
         level.sendParticles(ModParticles.SOUL_MOTE, p.x, p.y + Math.sin(i * 0.5) * 0.4, p.z, 1, 0.03, 0.03, 0.03, 0.0);
      }

      level.playSound(null, reaper.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, n >= 5 ? 1.3F : 0.8F);
      if (n == 5 && reaper instanceof Player p) {
         p.sendOverlayMessage(Component.translatable("message.deathbound.scythe.ready").withStyle(ChatFormatting.LIGHT_PURPLE));
      }
   }

   @Override
   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      int reaped = stack.getOrDefault(ModItems.REAPED, 0);
      if (!player.isSecondaryUseActive()) {
         dash(level, player, stack);
         return InteractionResult.SUCCESS;
      }

      if (reaped < 5) {
         player.sendOverlayMessage(Component.translatable("message.deathbound.scythe.hungry", reaped, 5).withStyle(ChatFormatting.GRAY));
         return InteractionResult.PASS;
      }

      if (level instanceof ServerLevel server) {
         stack.set(ModItems.REAPED, reaped - 5);
         this.soulReap(server, player, stack);
         player.getCooldowns().addCooldown(stack, 30);
      }

      return InteractionResult.SUCCESS;
   }

   private void soulReap(ServerLevel level, Player player, ItemStack stack) {
      float dealt = 0.0F;

      for (LivingEntity v : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5.5, 2.0, 5.5))) {
         if (!spared(v, player) && !(v.distanceTo(player) > 5.8)) {
            float before = v.getHealth();
            if (v.hurtServer(level, source(level, player), 14.0F)) {
               dealt += Math.max(0.0F, before - v.getHealth());
               Vec3 out = v.position().subtract(player.position()).multiply(1.0, 0.0, 1.0).normalize();
               v.push(out.x * 0.9, 0.35, out.z * 0.9);
               v.needsSync = true;
               if (!v.isAlive()) {
                  reap(stack, level, player, v);
               }
            }
         }
      }

      player.heal(Math.min(8.0F, dealt * 0.25F));

      for (int ring = 1; ring <= 5; ring++) {
         for (int i = 0; i < ring * 10; i++) {
            double a = i * 3.141592653589793 * 2.0 / (ring * 10);
            level.sendParticles(
               ModParticles.SOUL_FLAME,
               player.getX() + Math.cos(a) * ring,
               player.getY() + 0.9 + ring * 0.05,
               player.getZ() + Math.sin(a) * ring,
               1,
               0.0,
               0.05,
               0.0,
               0.0
            );
         }
      }

      level.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0, player.getZ(), 8, 2.5, 0.2, 2.5, 0.0);
      level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 1.2F, 0.6F);
      level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.7F, 0.6F);
      if (player instanceof ServerPlayer sp) {
         ServerPlayNetworking.send(sp, new ModNet.Shake(1.6F, 10));
      }

      stack.hurtAndBreak(2, player, InteractionHand.MAIN_HAND);
   }

   @Override
   public boolean isFoil(ItemStack stack) {
      return stack.getOrDefault(ModItems.REAPED, 0) >= 5 || super.isFoil(stack);
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
      builder.accept(Component.translatable("item.deathbound.reaper_scythe.lore").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
      builder.accept(
         Component.translatable("item.deathbound.reaper_scythe.reaped", stack.getOrDefault(ModItems.REAPED, 0), 10).withStyle(ChatFormatting.LIGHT_PURPLE)
      );
      CharmItem.lines(builder, "item.deathbound.reaper_scythe.desc", ChatFormatting.GRAY);
      builder.accept(Component.translatable("item.deathbound.reaper_scythe.reap", 5).withStyle(ChatFormatting.GRAY));
   }
}
