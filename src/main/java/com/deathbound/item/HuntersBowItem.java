package com.deathbound.item;

import com.deathbound.entity.HunterArrow;
import com.deathbound.registry.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class HuntersBowItem extends BowItem {
   public static final int PACK = 60;
   private static final ThreadLocal<Boolean> LOOSING_PACK = ThreadLocal.withInitial(() -> false);

   public HuntersBowItem(Item.Properties properties) {
      super(properties);
   }

   @Override
   public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
      super.onUseTick(level, user, stack, remaining);
      int drawn = this.getUseDuration(stack, user) - remaining;
      if (level instanceof ServerLevel server) {
         Vec3 hands = user.getEyePosition().add(user.getLookAngle().scale(0.7)).add(0.0, -0.3, 0.0);
         if (drawn == 60) {
            server.playSound(null, user.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 0.6F);
            server.playSound(null, user.blockPosition(), SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.PLAYERS, 1.0F, 0.5F);
            server.sendParticles(ModParticles.SOUL_FLAME, hands.x, hands.y, hands.z, 20, 0.25, 0.25, 0.25, 0.04);
         } else if (drawn > 60 && drawn % 3 == 0) {
            double a = drawn * 0.5;
            server.sendParticles(ModParticles.SOUL_FLAME, hands.x + Math.cos(a) * 0.35, hands.y + Math.sin(a) * 0.35, hands.z, 1, 0.0, 0.0, 0.0, 0.0);
         } else if (drawn > 20 && drawn < 60 && drawn % 8 == 0) {
            server.sendParticles(ModParticles.SOUL_MOTE, hands.x, hands.y, hands.z, 2, 0.3, 0.3, 0.3, 0.0);
         }
      }
   }

   @Override
   public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int remaining) {
      boolean pack = this.getUseDuration(stack, user) - remaining >= 60;
      LOOSING_PACK.set(pack);

      try {
         return super.releaseUsing(stack, level, user, remaining);
      } finally {
         LOOSING_PACK.set(false);
      }
   }

   @Override
   protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack projectile, boolean isCrit) {
      HunterArrow arrow = new HunterArrow(level, shooter, projectile.copyWithCount(1), weapon);
      arrow.setCritArrow(isCrit);
      if (LOOSING_PACK.get()) {
         arrow.seek();
      }

      return arrow;
   }

   @Override
   protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float uncertainty, float angle, LivingEntity target) {
      super.shootProjectile(shooter, projectile, index, velocity, uncertainty, angle, target);
      if (projectile instanceof HunterArrow lead && lead.isSeeking() && shooter.level() instanceof ServerLevel level) {
         for (int side : new int[]{-1, 1}) {
            HunterArrow a = new HunterArrow(level, shooter, new ItemStack(Items.ARROW), shooter.getMainHandItem());
            a.pickup = Pickup.CREATIVE_ONLY;
            a.seek();
            a.setCritArrow(true);
            a.shootFromRotation(shooter, shooter.getXRot() - 4.0F, shooter.getYRot() + side * 14.0F, 0.0F, velocity * 0.8F, 1.0F);
            level.addFreshEntity(a);
         }

         level.playSound(null, shooter.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.6F, 0.8F);
         level.playSound(null, shooter.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 0.5F);
      }
   }
}
