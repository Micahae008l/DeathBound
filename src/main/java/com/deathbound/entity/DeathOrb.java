package com.deathbound.entity;

import com.deathbound.registry.ModEntities;
import com.deathbound.registry.ModItems;
import com.deathbound.registry.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DeathOrb extends Fireball {
   private static final double SPEED = 0.55;
   private int hold = 20;

   public DeathOrb(EntityType<? extends DeathOrb> type, Level level) {
      super(type, level);
      this.setItem(new ItemStack(ModItems.DEATH_ORB));
   }

   public DeathOrb(Level level, LivingEntity owner, Vec3 at, int hold) {
      super(ModEntities.DEATH_ORB, at.x, at.y, at.z, new Vec3(0.0, 0.01, 0.0), level);
      this.setOwner(owner);
      this.setItem(new ItemStack(ModItems.DEATH_ORB));
      this.accelerationPower = 0.0;
      this.hold = hold;
   }

   @Override
   protected boolean shouldBurn() {
      return false;
   }

   @Override
   protected float getInertia() {
      return 1.0F;
   }

   @Override
   protected ParticleOptions getTrailParticle() {
      return ModParticles.SOUL_FLAME;
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

      if (!this.level().isClientSide()) {
         if (this.tickCount > 160) {
            this.detonate();
         } else if (this.tickCount < this.hold) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.6));
         } else {
            LivingEntity target = this.findTarget();
            if (target != null) {
               Vec3 want = target.getEyePosition().subtract(0.0, 0.4, 0.0).subtract(this.position()).normalize().scale(0.55);
               this.setDeltaMovement(this.getDeltaMovement().lerp(want, this.tickCount < this.hold + 10 ? 0.3 : 0.09));
            }
         }
      }
   }

   private LivingEntity findTarget() {
      return this.getOwner() instanceof Mob mob && mob.getTarget() != null && mob.getTarget().isAlive()
         ? mob.getTarget()
         : this.level().getNearestPlayer(this, 40.0);
   }

   @Override
   protected boolean canHitEntity(Entity target) {
      return super.canHitEntity(target)
         && !(target instanceof DeathOrb)
         && !(target instanceof SoulAnchor)
         && !(target instanceof LostSoul)
         && (target instanceof Player || this.tickCount > this.hold);
   }

   @Override
   protected void onHitEntity(EntityHitResult hit) {
      super.onHitEntity(hit);
      if (this.level() instanceof ServerLevel level) {
         LivingEntity owner = this.getOwner() instanceof LivingEntity l ? l : null;
         if (owner == null || !hit.getEntity().isAlliedTo(owner)) {
            hit.getEntity().hurtServer(level, level.damageSources().source(Hazards.SOUL_REND, this, owner), 7.0F);
         }
      }
   }

   @Override
   protected void onHit(HitResult hit) {
      super.onHit(hit);
      if (!this.level().isClientSide()) {
         this.detonate();
      }
   }

   private void detonate() {
      if (this.level() instanceof ServerLevel level) {
         level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY(), this.getZ(), 30, 0.4, 0.4, 0.4, 0.12);
         level.playSound(null, this.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.0F, 0.7F);
         LivingEntity owner = this.getOwner() instanceof LivingEntity l ? l : null;

         for (LivingEntity v : level.getEntitiesOfClass(LivingEntity.class, new AABB(this.position(), this.position()).inflate(2.0), e -> e != owner)) {
            if (owner == null || !v.isAlliedTo(owner)) {
               v.hurtServer(level, level.damageSources().source(Hazards.SOUL_REND, this, owner), 3.0F);
            }
         }

         this.discard();
      }
   }
}
