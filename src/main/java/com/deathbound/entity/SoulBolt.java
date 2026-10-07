package com.deathbound.entity;

import com.deathbound.registry.ModEffects;
import com.deathbound.registry.ModEntities;
import com.deathbound.registry.ModItems;
import com.deathbound.registry.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SoulBolt extends Fireball {
   private float damage = 3.0F;

   public SoulBolt(EntityType<? extends SoulBolt> type, Level level) {
      super(type, level);
      this.setItem(new ItemStack(ModItems.SOUL_BOLT));
   }

   public SoulBolt(Level level, LivingEntity owner, Vec3 direction) {
      super(ModEntities.SOUL_BOLT, owner, direction, level);
      this.setItem(new ItemStack(ModItems.SOUL_BOLT));
      this.accelerationPower = 0.055;
   }

   public SoulBolt(Level level, double x, double y, double z, Vec3 direction, float damage) {
      super(ModEntities.SOUL_BOLT, x, y, z, direction, level);
      this.setItem(new ItemStack(ModItems.SOUL_BOLT));
      this.accelerationPower = 0.1;
      this.damage = damage;
   }

   @Override
   protected boolean shouldBurn() {
      return false;
   }

   @Override
   protected ParticleOptions getTrailParticle() {
      return ModParticles.SOUL_MOTE;
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level().isClientSide() && this.random.nextInt(2) == 0) {
         this.level()
            .addParticle(
               ModParticles.SOUL_FLAME,
               this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
               this.getY() + this.getBbHeight() * 0.5,
               this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
               0.0,
               0.01,
               0.0
            );
      }

      if (!this.level().isClientSide() && this.tickCount > 120) {
         this.discard();
      }
   }

   @Override
   protected void onHitEntity(EntityHitResult hit) {
      super.onHitEntity(hit);
      if (this.level() instanceof ServerLevel level) {
         Entity target = hit.getEntity();
         LivingEntity owner = this.getOwner() instanceof LivingEntity l ? l : null;
         if (owner == null || !target.isAlliedTo(owner) && !Hazards.sameSide(target, owner)) {
            target.hurtServer(level, level.damageSources().source(Hazards.SOUL_BOLT, this, owner), this.damage);
            if (owner instanceof SoulWisp && target instanceof LivingEntity living && living.isAlive()) {
               living.addEffect(new MobEffectInstance(ModEffects.MARKED, 60, 0, false, true, true));
               level.playSound(null, target.blockPosition(), SoundEvents.SCULK_CLICKING, SoundSource.HOSTILE, 1.0F, 0.6F);
            }
         }
      }
   }

   @Override
   protected boolean canHitEntity(Entity target) {
      return super.canHitEntity(target) && !(target instanceof SoulBolt) && !(target instanceof LostSoul);
   }

   @Override
   protected void onHit(HitResult hit) {
      super.onHit(hit);
      if (this.level() instanceof ServerLevel level) {
         level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY(), this.getZ(), 10, 0.15, 0.15, 0.15, 0.05);
         level.playSound(null, this.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 1.0F, 1.4F);
         this.discard();
      }
   }
}
