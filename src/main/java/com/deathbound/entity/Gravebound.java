package com.deathbound.entity;

import com.deathbound.registry.ModAttachments;
import com.deathbound.registry.ModEffects;
import com.deathbound.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class Gravebound extends Monster {
   private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(Gravebound.class, EntityDataSerializers.INT);
   public static final int SHROUDED = 0;
   public static final int BARE = 1;
   public static final int KNIGHT = 2;
   public static final int CHARRED = 3;
   public static final int RISE_TICKS = 34;
   public final AnimationState riseAnim = new AnimationState();
   public final AnimationState attackAnim = new AnimationState();
   private int riseTicks;

   public Gravebound(EntityType<? extends Gravebound> type, Level level) {
      super(type, level);
      this.xpReward = 7;
      if (!level.isClientSide()) {
         float roll = this.random.nextFloat();
         this.setVariant(roll < 0.38F ? 0 : (roll < 0.68F ? 1 : (roll < 0.92F ? 2 : 3)));
      }
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 26.0)
         .add(Attributes.MOVEMENT_SPEED, 0.24)
         .add(Attributes.ATTACK_DAMAGE, 4.0)
         .add(Attributes.ARMOR, 3.0)
         .add(Attributes.FOLLOW_RANGE, 32.0);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, false));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector
         .addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (target, level) -> !ModAttachments.crowned(target)));
   }

   @Override
   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(VARIANT, 0);
   }

   public int variant() {
      return this.entityData.get(VARIANT);
   }

   private void setVariant(int variant) {
      this.entityData.set(VARIANT, variant);
      boolean armed = variant == 1 || variant == 2;
      this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(armed ? 6.0 : 4.0);
      this.getAttribute(Attributes.ARMOR).setBaseValue(variant == 2 ? 7.0 : 3.0);
      this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(variant == 2 ? 0.21 : 0.24);
   }

   public boolean isArmed() {
      return this.variant() == 1 || this.variant() == 2;
   }

   @Override
   public @Nullable SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data
   ) {
      if (reason != EntitySpawnReason.SPAWN_ITEM_USE && reason != EntitySpawnReason.COMMAND && reason != EntitySpawnReason.CHUNK_GENERATION) {
         this.rise();
      }

      return super.finalizeSpawn(level, difficulty, reason, data);
   }

   public void rise() {
      this.riseTicks = 34;
      this.setPose(Pose.EMERGING);
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
      if (DATA_POSE.equals(accessor) && this.level().isClientSide() && this.getPose() == Pose.EMERGING) {
         this.riseAnim.start(this.tickCount);
      }

      super.onSyncedDataUpdated(accessor);
   }

   @Override
   protected boolean isImmobile() {
      return super.isImmobile() || this.getPose() == Pose.EMERGING;
   }

   @Override
   public void tick() {
      super.tick();
      if (this.getPose() == Pose.EMERGING) {
         BlockState below = this.level().getBlockState(this.blockPosition().below());
         if (this.level().isClientSide()) {
            if (!below.isAir() && this.random.nextInt(2) == 0) {
               this.level()
                  .addParticle(
                     new BlockParticleOption(ParticleTypes.BLOCK, below), this.getRandomX(0.8), this.getY() + 0.1, this.getRandomZ(0.8), 0.0, 0.2, 0.0
                  );
            }

            if (this.random.nextInt(3) == 0) {
               this.level().addParticle(ModParticles.SOUL_MOTE, this.getRandomX(0.6), this.getY() + 0.2, this.getRandomZ(0.6), 0.0, 0.05, 0.0);
            }
         } else if (--this.riseTicks <= 0) {
            this.setPose(Pose.STANDING);
         }
      }
   }

   @Override
   public boolean doHurtTarget(ServerLevel level, Entity target) {
      level.broadcastEntityEvent(this, (byte)4);
      boolean hit = super.doHurtTarget(level, target);
      if (hit && this.variant() == 3 && target instanceof LivingEntity living) {
         living.addEffect(new MobEffectInstance(ModEffects.MARKED, 60, 0, false, true, true));
         level.playSound(null, target.blockPosition(), SoundEvents.SCULK_CLICKING, SoundSource.HOSTILE, 1.0F, 0.6F);
      }

      return hit;
   }

   @Override
   public void handleEntityEvent(byte id) {
      if (id == 4) {
         this.attackAnim.start(this.tickCount);
      } else {
         super.handleEntityEvent(id);
      }
   }

   @Override
   protected void tickDeath() {
      this.deathTime++;
      if (this.deathTime >= 26 && !this.level().isClientSide() && !this.isRemoved()) {
         ((ServerLevel)this.level()).sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY() + 0.6, this.getZ(), 18, 0.25, 0.4, 0.25, 0.05);
         this.level().broadcastEntityEvent(this, (byte)60);
         this.remove(RemovalReason.KILLED);
      }
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putInt("variant", this.variant());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      input.getInt("variant").ifPresentOrElse(this::setVariant, () -> {
         if (input.getBooleanOr("bare", false)) {
            this.setVariant(1);
         }
      });
   }

   @Override
   public float getVoicePitch() {
      return super.getVoicePitch() * 0.72F;
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.SKELETON_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.SKELETON_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.SKELETON_DEATH;
   }

   @Override
   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.SKELETON_STEP, 0.15F, 0.8F);
   }
}
