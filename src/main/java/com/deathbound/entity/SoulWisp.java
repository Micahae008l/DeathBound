package com.deathbound.entity;

import com.deathbound.registry.ModAttachments;
import com.deathbound.registry.ModParticles;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SoulWisp extends Monster {
   public final AnimationState spitAnim = new AnimationState();
   private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(SoulWisp.class, EntityDataSerializers.INT);
   public static final int VIOLET = 0;
   public static final int PALE = 1;
   public static final int UMBRAL = 2;

   public int variant() {
      return this.entityData.get(VARIANT);
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(VARIANT, 0);
   }

   @Override
   public @Nullable SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data
   ) {
      return super.finalizeSpawn(level, difficulty, reason, data);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putInt("variant", this.variant());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      input.getInt("variant").ifPresent(v -> this.entityData.set(VARIANT, v));
   }

   public SoulWisp(EntityType<? extends SoulWisp> type, Level level) {
      super(type, level);
      if (!level.isClientSide()) {
         float roll = this.random.nextFloat();
         this.entityData.set(VARIANT, roll < 0.55F ? 0 : (roll < 0.8F ? 1 : 2));
      }

      this.moveControl = new FlyingMoveControl<>(this, 20, true);
      this.setNoGravity(true);
      this.xpReward = 5;
   }

   public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 14.0)
         .add(Attributes.FLYING_SPEED, 0.55)
         .add(Attributes.MOVEMENT_SPEED, 0.3)
         .add(Attributes.ATTACK_DAMAGE, 3.0)
         .add(Attributes.FOLLOW_RANGE, 32.0);
   }

   @Override
   protected PathNavigation createNavigation(Level level) {
      FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
      nav.setCanOpenDoors(false);
      nav.setCanFloat(true);
      return nav;
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(2, new SoulWisp.HauntGoal());
      this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector
         .addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (target, level) -> !ModAttachments.crowned(target)));
   }

   void spit(LivingEntity target) {
      Vec3 from = this.getEyePosition();
      Vec3 dir = target.position()
         .add(0.0, target.getBbHeight() * 0.5, 0.0)
         .subtract(from)
         .normalize()
         .add(this.random.nextGaussian() * 0.11, this.random.nextGaussian() * 0.07, this.random.nextGaussian() * 0.11);
      SoulBolt bolt = new SoulBolt(this.level(), this, dir.normalize());
      bolt.setPos(from.x, from.y - 0.1, from.z);
      this.level().addFreshEntity(bolt);
      this.level().broadcastEntityEvent(this, (byte)4);
      this.playSound(SoundEvents.SOUL_ESCAPE.value(), 1.5F, 1.6F);
      this.playSound(SoundEvents.BLAZE_SHOOT, 0.5F, 1.8F);
   }

   @Override
   public void handleEntityEvent(byte id) {
      if (id == 4) {
         this.spitAnim.start(this.tickCount);
      } else {
         super.handleEntityEvent(id);
      }
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.level().isClientSide() && this.random.nextInt(3) == 0) {
         this.level()
            .addParticle(ModParticles.SOUL_MOTE, this.getRandomX(0.5), this.getY() + 0.3 + this.random.nextDouble() * 0.5, this.getRandomZ(0.5), 0.0, 0.02, 0.0);
      }
   }

   @Override
   protected void tickDeath() {
      this.deathTime++;
      if (this.deathTime >= 8 && !this.level().isClientSide() && !this.isRemoved()) {
         ((ServerLevel)this.level()).sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY() + 0.4, this.getZ(), 24, 0.3, 0.3, 0.3, 0.08);
         this.playSound(SoundEvents.SOUL_ESCAPE.value(), 2.0F, 0.8F);
         this.remove(RemovalReason.KILLED);
      }
   }

   @Override
   public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
      return false;
   }

   @Override
   protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.VEX_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.VEX_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.VEX_DEATH;
   }

   @Override
   public float getVoicePitch() {
      return super.getVoicePitch() * 0.6F;
   }

   private class HauntGoal extends Goal {
      private int cooldown = 70;
      private int repick;

      HauntGoal() {
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         LivingEntity t = SoulWisp.this.getTarget();
         return t != null && t.isAlive();
      }

      @Override
      public boolean requiresUpdateEveryTick() {
         return true;
      }

      @Override
      public void tick() {
         LivingEntity target = SoulWisp.this.getTarget();
         if (target != null) {
            SoulWisp.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (--this.repick <= 0 || SoulWisp.this.distanceToSqr(target) > 196.0) {
               this.repick = 25 + SoulWisp.this.random.nextInt(20);
               double a = SoulWisp.this.random.nextDouble() * 3.141592653589793 * 2.0;
               double r = 5.0 + SoulWisp.this.random.nextDouble() * 3.0;
               SoulWisp.this.moveControl
                  .setWantedPosition(
                     target.getX() + Math.cos(a) * r, target.getEyeY() + 1.5 + SoulWisp.this.random.nextDouble() * 2.0, target.getZ() + Math.sin(a) * r, 1.1
                  );
            }

            if (--this.cooldown <= 0 && SoulWisp.this.getSensing().hasLineOfSight(target)) {
               this.cooldown = 110 + SoulWisp.this.random.nextInt(70);
               SoulWisp.this.spit(target);
            }
         }
      }
   }
}
