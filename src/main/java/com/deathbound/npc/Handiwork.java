package com.deathbound.npc;

import com.deathbound.registry.ModParticles;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Display.BillboardConstraints;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class Handiwork {
   private static final String TAG = "deathbound_handiwork";
   private static final int QUENCH = 98;
   private static final int FORGE = 130;
   private static final List<Handiwork.Job> JOBS = new ArrayList<>();
   private static final List<ItemDisplay> STRAYS = new ArrayList<>();

   public static boolean busy(ServerPlayer p) {
      return JOBS.stream().anyMatch(j -> j.player == p);
   }

   static void quench(ServerPlayer p, UnderworldNpc smith, Consumer<ItemStack> change) {
      start(p, smith, EquipmentSlot.MAINHAND, change, true);
   }

   static void forge(ServerPlayer p, UnderworldNpc smith, EquipmentSlot slot, Consumer<ItemStack> change) {
      start(p, smith, slot, change, false);
   }

   private static void start(ServerPlayer p, UnderworldNpc smith, EquipmentSlot slot, Consumer<ItemStack> change, boolean quench) {
      ItemStack item = p.getItemBySlot(slot);
      if (smith == null) {
         change.accept(item);
      } else {
         ServerLevel level = p.level();
         item = item.copy();
         p.setItemSlot(slot, ItemStack.EMPTY);
         Vec3 fwd = Vec3.directionFromRotation(0.0F, home(smith));
         Vec3 hearth = null;
         if (!quench) {
            BlockPos s = smith.blockPosition();

            for (BlockPos b : BlockPos.betweenClosed(s.offset(-6, -1, -6), s.offset(6, 2, 6))) {
               if (level.getBlockState(b).is(Blocks.SOUL_CAMPFIRE)) {
                  hearth = Vec3.atBottomCenterOf(b).add(0.0, 0.85, 0.0);
                  break;
               }
            }
         }

         ItemDisplay d = new ItemDisplay(EntityTypes.ITEM_DISPLAY, level);
         d.setItemStack(item.copy());
         d.setItemTransform(ItemDisplayContext.NONE);
         d.setBillboardConstraints(BillboardConstraints.VERTICAL);
         d.setPosRotInterpolationDuration(2);
         d.addTag("deathbound_handiwork");
         Vec3 from = hand(p);
         d.setPos(from.x, from.y, from.z);
         pose(d, quench ? 45.0F : 0.0F, 0.0F, 0.0F, 0.75F, 0);
         Handiwork.Job j = new Handiwork.Job(p, smith, d, item, slot, change, quench, fwd, hearth);
         j.last = from;
         JOBS.add(j);
         level.addFreshEntity(d);
         level.playSound(null, p.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.8F, 0.6F);
      }
   }

   private static float home(UnderworldNpc smith) {
      return smith.kind() == UnderworldNpc.Kind.BONESMITH ? 90.0F : 270.0F;
   }

   public static void init() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (int i = JOBS.size() - 1; i >= 0; i--) {
            if (tick(JOBS.get(i))) {
               JOBS.remove(i);
            }
         }

         for (ItemDisplay d : STRAYS) {
            if (d.level() instanceof ServerLevel level && !d.isRemoved()) {
               ItemEntity drop = new ItemEntity(level, d.getX(), d.getY(), d.getZ(), d.getItemStack().copy());
               drop.setUnlimitedLifetime();
               level.addFreshEntity(drop);
               d.discard();
            }
         }

         STRAYS.clear();
      });
      ServerEntityEvents.ENTITY_LOAD.register((e, level) -> {
         if (e instanceof ItemDisplay d && d.entityTags().contains("deathbound_handiwork") && JOBS.stream().noneMatch(j -> j.shown == d)) {
            STRAYS.add(d);
         }
      });
   }

   private static boolean tick(Handiwork.Job j) {
      int t = j.t++;
      ServerLevel level = (ServerLevel)j.shown.level();
      ServerPlayer p = j.player;
      if (!p.isRemoved() && !p.hasDisconnected() && !p.isDeadOrDying() && !j.smith.isRemoved() && !j.shown.isRemoved()) {
         j.smith.setYRot(home(j.smith));
         j.smith.setYHeadRot(home(j.smith));
         j.smith.yBodyRot = home(j.smith);
         boolean end = j.quench ? quench(j, level, t) : forge(j, level, t);
         if (end) {
            j.shown.discard();
            ItemStack back = j.item.copy();
            Component from = Quests.from(j.quench ? "bonesmith" : "gravedigger");
            if (p.getItemBySlot(j.slot).isEmpty()) {
               p.setItemSlot(j.slot, back);
               Rewards.show(p, from, List.of(back));
            } else {
               Rewards.give(p, from, back);
            }

            level.playSound(
               null,
               p.blockPosition(),
               j.quench ? SoundEvents.ARMOR_EQUIP_IRON.value() : SoundEvents.ARMOR_EQUIP_NETHERITE.value(),
               SoundSource.PLAYERS,
               1.0F,
               0.8F
            );
         }

         return end;
      } else {
         if (j.t <= (j.quench ? 40 : 100)) {
            j.change.accept(j.item);
         }

         ItemEntity drop = new ItemEntity(level, j.smith.getX(), j.smith.getY() + 0.5, j.smith.getZ(), j.item.copy());
         drop.setUnlimitedLifetime();
         level.addFreshEntity(drop);
         j.shown.discard();
         return true;
      }
   }

   private static boolean quench(Handiwork.Job j, ServerLevel level, int t) {
      Vec3 base = j.smith.position();
      Vec3 f = j.fwd;
      Vec3 hand = base.add(f.scale(0.5)).add(0.0, 1.7, 0.0);
      Vec3 over = base.add(f.scale(0.3)).add(0.0, 3.2, 0.0);
      Vec3 under = base.add(f.scale(2.0)).add(0.0, -0.75, 0.0);
      Vec3 lifted = base.add(f.scale(1.6)).add(0.0, 2.6, 0.0);
      Vec3 shown = base.add(f.scale(1.3)).add(0.0, 2.2, 0.0);
      Vec3 at;
      if (t <= 10) {
         at = ease(hand(j.player), hand, t / 10.0);
      } else if (t <= 22) {
         at = ease(hand, over, (t - 10) / 12.0);
      } else if (t <= 27) {
         at = ease(over, under, (t - 22) / 5.0);
      } else if (t <= 50) {
         at = under.add(0.0, Math.sin(t * 0.7) * 0.03, 0.0);
      } else if (t <= 60) {
         at = ease(under, lifted, (t - 50) / 10.0);
      } else if (t <= 76) {
         at = ease(lifted, shown, (t - 60) / 16.0).add(0.0, Math.sin(t * 0.3) * 0.05, 0.0);
      } else {
         at = ease(shown, hand(j.player), (t - 76) / 20.0);
      }

      move(j, at);
      ItemDisplay d = j.shown;
      switch (t) {
         case 10:
            level.playSound(null, base.x, base.y + 1.5, base.z, SoundEvents.SKELETON_AMBIENT, SoundSource.NEUTRAL, 1.0F, 0.4F);
            level.broadcastEntityEvent(j.smith, (byte)93);
            break;
         case 12:
            level.broadcastEntityEvent(j.smith, (byte)94);
            pose(d, 45.0F, 0.0F, 0.0F, 0.75F, 10);
            break;
         case 22:
            pose(d, -135.0F, 0.0F, 0.0F, 0.75F, 5);
            break;
         case 27:
            sound(level, under, SoundEvents.GENERIC_SPLASH, 1.4F, 0.7F);
            sound(level, under, SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 0.8F, 0.8F);
            sound(level, under, SoundEvents.FIRE_EXTINGUISH, 1.6F, 0.5F);
            level.sendParticles(ParticleTypes.SPLASH, under.x, base.y + 0.1, under.z, 60, 0.5, 0.1, 0.5, 0.3);
            level.sendParticles(ParticleTypes.CLOUD, under.x, base.y + 0.3, under.z, 8, 0.3, 0.1, 0.3, 0.02);
            d.setGlowingTag(true);
            d.setGlowColorOverride(9329622);
            break;
         case 34:
            sound(level, under, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 1.2F, 0.6F);
            break;
         case 40:
            j.change.accept(j.item);
            d.setItemStack(j.item.copy());
            d.setGlowColorOverride(13214463);
            sound(level, under, SoundEvents.CONDUIT_ACTIVATE, 1.4F, 0.8F);
            ring(level, ModParticles.SOUL_FLAME, under.x, base.y + 0.15, under.z, 28, 0.3);
            break;
         case 50:
            pose(d, 45.0F, 0.0F, 0.0F, 0.75F, 10);
            break;
         case 51:
            sound(level, under, SoundEvents.GENERIC_SPLASH, 0.9F, 1.1F);
            level.sendParticles(ParticleTypes.SPLASH, under.x, base.y + 0.1, under.z, 40, 0.3, 0.1, 0.3, 0.2);
            break;
         case 60:
         case 66:
         case 72:
            d.setBrightnessOverride(new Brightness(15, 15));
            pose(d, 45.0F, (t - 54) * 20, 0.0F, 0.85F, 6);
            break;
         case 76:
            flare(level, d.position().add(0.0, 0.2, 0.0), j.smith);
      }

      if (t > 27 && t < 50) {
         level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, under.x, under.y, under.z, 3, 0.15, 0.1, 0.15, 0.05);
         if (t % 2 == 0) {
            level.sendParticles(ParticleTypes.CLOUD, under.x, base.y + 0.2, under.z, 2, 0.3, 0.05, 0.3, 0.02);
         }

         for (int i = 0; i < 2; i++) {
            double a = level.getRandom().nextDouble() * 3.141592653589793 * 2.0;
            double r = 1.5 + level.getRandom().nextDouble() * 2.0;
            Vec3 from = under.add(Math.cos(a) * r, -1.6, Math.sin(a) * r);
            Vec3 v = under.subtract(from).scale(0.075);
            level.sendParticles(ModParticles.SOUL_FLAME, from.x, from.y, from.z, 0, v.x, v.y, v.z, 1.0);
         }
      }

      if (t > 51 && t < 62) {
         level.sendParticles(ParticleTypes.FALLING_WATER, at.x, at.y - 0.3, at.z, 3, 0.1, 0.2, 0.1, 0.0);
      }

      if (t >= 60 && t < 76) {
         helix(level, at, t);
      }

      if (t > 76) {
         level.sendParticles(ModParticles.SOUL_FLAME, at.x, at.y, at.z, 1, 0.05, 0.05, 0.05, 0.0);
      }

      return t >= 98;
   }

   private static boolean forge(Handiwork.Job j, ServerLevel level, int t) {
      Vec3 base = j.smith.position();
      Vec3 f = j.fwd;
      Vec3 anvil = base.add(f.scale(1.0)).add(0.0, 1.08, 0.0);
      Vec3 fire = j.hearth != null ? j.hearth : anvil.add(0.0, 0.6, 0.0);
      Vec3 up = anvil.add(0.0, 1.0, 0.0);
      Vec3 at;
      if (t <= 12) {
         at = ease(hand(j.player), fire, t / 12.0);
      } else if (t <= 40) {
         at = fire.add(0.0, Math.sin(t * 0.4) * 0.03, 0.0);
      } else if (t <= 50) {
         at = ease(fire, anvil, (t - 40) / 10.0);
      } else if (t <= 104) {
         at = anvil;
      } else if (t <= 112) {
         at = ease(anvil, up, (t - 104) / 8.0);
      } else {
         at = ease(up, hand(j.player), (t - 112) / 16.0);
      }

      move(j, at);
      ItemDisplay d = j.shown;
      switch (t) {
         case 12:
            sound(level, fire, SoundEvents.FIRECHARGE_USE, 0.8F, 0.6F);
            d.setBrightnessOverride(new Brightness(15, 15));
            d.setGlowingTag(true);
            d.setGlowColorOverride(6280166);
            break;
         case 40:
            pose(d, 0.0F, 0.0F, -90.0F, 0.7F, 10);
            break;
         case 46:
            level.broadcastEntityEvent(j.smith, (byte)95);
            d.setGlowColorOverride(14717002);
            break;
         case 96:
            j.change.accept(j.item);
            d.setItemStack(j.item.copy());
            d.setGlowColorOverride(13214463);
            sound(level, anvil, SoundEvents.SMITHING_TABLE_USE, 1.2F, 0.6F);
            sound(level, anvil, SoundEvents.SOUL_ESCAPE.value(), 1.5F, 0.7F);
            ring(level, ModParticles.SOUL_FLAME, anvil.x, anvil.y, anvil.z, 24, 0.25);
            break;
         case 104:
            pose(d, 0.0F, 120.0F, 0.0F, 0.8F, 8);
            break;
         case 112:
            flare(level, up, j.smith);
      }

      if (t > 12 && t < 40) {
         level.sendParticles(ModParticles.SOUL_FLAME, fire.x, fire.y - 0.2, fire.z, 2, 0.2, 0.15, 0.2, 0.01);
         if (t % 8 == 0) {
            sound(level, fire, SoundEvents.FURNACE_FIRE_CRACKLE, 1.0F, 0.7F);
         }
      }

      if (t > 50 && t < 104) {
         if (t % 3 == 0) {
            level.sendParticles(ParticleTypes.SMALL_FLAME, anvil.x, anvil.y + 0.05, anvil.z, 1, 0.15, 0.02, 0.15, 0.0);
         }

         if (t % 9 == 0) {
            level.sendParticles(ParticleTypes.LAVA, anvil.x, anvil.y + 0.1, anvil.z, 1, 0.1, 0.0, 0.1, 0.0);
         }
      }

      if (t >= 104 && t < 112) {
         helix(level, at, t);
      }

      if (t > 112) {
         level.sendParticles(ModParticles.SOUL_FLAME, at.x, at.y, at.z, 1, 0.05, 0.05, 0.05, 0.0);
      }

      return t >= 130;
   }

   private static Vec3 hand(ServerPlayer p) {
      return p.getEyePosition().add(p.getLookAngle().scale(0.6)).add(0.0, -0.55, 0.0);
   }

   private static Vec3 ease(Vec3 a, Vec3 b, double k) {
      k = Mth.clamp(k, 0.0, 1.0);
      return a.lerp(b, k * k * (3.0 - 2.0 * k));
   }

   private static void move(Handiwork.Job j, Vec3 at) {
      if (!at.equals(j.last)) {
         j.shown.setPos(at.x, at.y, at.z);
         j.last = at;
      }
   }

   private static void pose(ItemDisplay d, float z, float y, float x, float scale, int ticks) {
      Quaternionf q = new Quaternionf().rotateY(y * 0.017453292F).rotateX(x * 0.017453292F).rotateZ(z * 0.017453292F);
      d.setTransformation(new Transformation(new Vector3f(), q, new Vector3f(scale), new Quaternionf()));
      d.setTransformationInterpolationDelay(0);
      d.setTransformationInterpolationDuration(ticks);
   }

   private static void sound(ServerLevel level, Vec3 at, SoundEvent s, float volume, float pitch) {
      level.playSound(null, at.x, at.y, at.z, s, SoundSource.NEUTRAL, volume, pitch);
   }

   private static void ring(ServerLevel level, ParticleOptions type, double x, double y, double z, int n, double speed) {
      for (int i = 0; i < n; i++) {
         double a = i * 3.141592653589793 * 2.0 / n;
         level.sendParticles(type, x, y, z, 0, Math.cos(a), 0.0, Math.sin(a), speed);
      }
   }

   private static void helix(ServerLevel level, Vec3 at, int t) {
      for (int k = 0; k < 2; k++) {
         double a = t * 0.55 + k * 3.141592653589793;
         level.sendParticles(ModParticles.SOUL_MOTE, at.x + Math.cos(a) * 0.6, at.y - 0.5 + t % 16 * 0.07, at.z + Math.sin(a) * 0.6, 1, 0.0, 0.0, 0.0, 0.0);
      }
   }

   private static void flare(ServerLevel level, Vec3 at, UnderworldNpc smith) {
      level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, -3562753), at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);

      for (int i = 0; i < 70; i++) {
         Vec3 v = new Vec3(level.getRandom().nextGaussian(), level.getRandom().nextGaussian(), level.getRandom().nextGaussian()).normalize();
         level.sendParticles(i % 3 == 0 ? ModParticles.SOUL_MOTE : ModParticles.SOUL_FLAME, at.x, at.y, at.z, 0, v.x, v.y, v.z, 0.22);
      }

      sound(level, at, SoundEvents.AMETHYST_BLOCK_CHIME, 2.0F, 0.7F);
      sound(level, at, SoundEvents.BELL_RESONATE, 1.0F, 1.4F);
      sound(level, at, SoundEvents.ENCHANTMENT_TABLE_USE, 1.2F, 0.8F);
      level.broadcastEntityEvent(smith, (byte)93);
   }

   private Handiwork() {
   }

   private static final class Job {
      final ServerPlayer player;
      final UnderworldNpc smith;
      final ItemDisplay shown;
      final ItemStack item;
      final EquipmentSlot slot;
      final Consumer<ItemStack> change;
      final boolean quench;
      final Vec3 fwd;
      final Vec3 hearth;
      Vec3 last;
      int t;

      Job(
         ServerPlayer player,
         UnderworldNpc smith,
         ItemDisplay shown,
         ItemStack item,
         EquipmentSlot slot,
         Consumer<ItemStack> change,
         boolean quench,
         Vec3 fwd,
         Vec3 hearth
      ) {
         this.player = player;
         this.smith = smith;
         this.shown = shown;
         this.item = item;
         this.slot = slot;
         this.change = change;
         this.quench = quench;
         this.fwd = fwd;
         this.hearth = hearth;
      }
   }
}
