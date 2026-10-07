package com.deathbound.entity;

import com.deathbound.registry.ModParticles;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SoulAnchor extends Mob {
   private @Nullable UUID boundTo;

   public SoulAnchor(EntityType<? extends SoulAnchor> type, Level level) {
      super(type, level);
      this.setNoGravity(true);
      this.xpReward = 10;
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 45.0).add(Attributes.ARMOR, 4.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
   }

   public void bindTo(DeathEntity death) {
      this.boundTo = death.getUUID();
   }

   public boolean isBoundTo(DeathEntity death) {
      return death.getUUID().equals(this.boundTo);
   }

   private @Nullable DeathEntity death() {
      return this.boundTo != null && this.level() instanceof ServerLevel level && level.getEntity(this.boundTo) instanceof DeathEntity d ? d : null;
   }

   @Override
   public void aiStep() {
      this.setDeltaMovement(Vec3.ZERO);
      super.aiStep();
      if (!(this.level() instanceof ServerLevel level)) {
         if (this.random.nextInt(2) == 0) {
            this.level()
               .addParticle(ModParticles.SOUL_MOTE, this.getRandomX(0.8), this.getY() + this.random.nextDouble() * 1.6, this.getRandomZ(0.8), 0.0, 0.03, 0.0);
         }
      } else {
         DeathEntity death = this.death();
         if (death != null && this.tickCount % 6 == 0) {
            Vec3 from = this.position().add(0.0, 0.9, 0.0);
            Vec3 to = death.position().add(0.0, 1.6, 0.0);
            int steps = (int)(from.distanceTo(to) * 1.5);
            int offset = this.tickCount / 6 % 3;

            for (int i = offset; i < steps; i += 3) {
               Vec3 p = from.lerp(to, (double)i / steps);
               level.sendParticles(ModParticles.SOUL_MOTE, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
            }
         }

         if (this.tickCount % 140 == 70) {
            Player target = level.getNearestPlayer(this, 26.0);
            if (target != null && !target.isCreative() && this.hasLineOfSight(target)) {
               Vec3 from = this.position().add(0.0, 0.9, 0.0);
               SoulBolt bolt = new SoulBolt(level, this, target.getEyePosition().subtract(from).normalize());
               bolt.setPos(from.x, from.y, from.z);
               level.addFreshEntity(bolt);
               this.playSound(SoundEvents.SOUL_ESCAPE.value(), 2.0F, 0.9F);
            }
         }
      }
   }

   @Override
   public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
      if (!(source.getEntity() instanceof Player) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         return false;
      }

      boolean hurt = super.hurtServer(level, source, damage);
      if (hurt) {
         level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY() + 0.9, this.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
      }

      return hurt;
   }

   @Override
   public void die(DamageSource source) {
      super.die(source);
      if (this.level() instanceof ServerLevel level) {
         level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY() + 0.9, this.getZ(), 80, 0.5, 0.6, 0.5, 0.15);
         this.playSound(SoundEvents.GLASS_BREAK, 2.0F, 0.5F);
         this.playSound(SoundEvents.WITHER_HURT, 1.5F, 0.6F);
         DeathEntity death = this.death();
         if (death != null) {
            death.onAnchorBroken();
         }
      }
   }

   @Override
   protected void tickDeath() {
      if (!this.level().isClientSide() && !this.isRemoved()) {
         this.remove(RemovalReason.KILLED);
      }
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   protected void doPush(Entity entity) {
   }

   @Override
   public boolean removeWhenFarAway(double distSqr) {
      return false;
   }

   @Override
   protected @Nullable SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.AMETHYST_BLOCK_HIT;
   }

   @Override
   protected @Nullable SoundEvent getDeathSound() {
      return SoundEvents.AMETHYST_BLOCK_BREAK;
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.storeNullable("bound_to", UUIDUtil.CODEC, this.boundTo);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.boundTo = input.<UUID>read("bound_to", UUIDUtil.CODEC).orElse(null);
   }
}
