package com.deathbound.entity;

import com.deathbound.registry.ModNet;
import com.deathbound.registry.ModParticles;
import com.deathbound.world.Director;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class DeathsGuard extends Monster {
   public static final int NONE = 0;
   public static final int SWEEP = 1;
   public static final int SLAM = 2;
   public static final int LUNGE = 3;
   public static final int INSPECT = 4;
   public static final int CONDEMN = 5;
   public static final int JUDGE = 6;
   private static final int[] LENGTH = new int[]{0, 42, 58, 42, 50, 24, 56};
   private static final int[] IMPACT = new int[]{0, 19, 31, 14, 0, 11, 23};
   public static final float HEAVY_SPEED = 0.72F;
   private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(DeathsGuard.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> KNEEL = SynchedEntityData.defineId(DeathsGuard.class, EntityDataSerializers.BOOLEAN);
   public final AnimationState sweepAnim = new AnimationState();
   public final AnimationState slamAnim = new AnimationState();
   public final AnimationState lungeAnim = new AnimationState();
   public final AnimationState inspectAnim = new AnimationState();
   public final AnimationState condemnAnim = new AnimationState();
   public final AnimationState judgeAnim = new AnimationState();
   private final ServerBossEvent bossEvent = new ServerBossEvent(
      UUID.randomUUID(), Component.translatable("entity.deathbound.deaths_guard"), BossBarColor.PURPLE, BossBarOverlay.NOTCHED_6
   );
   public float readiness;
   public float readinessO;
   private int actionTick;
   private int cooldown;
   private @Nullable LostSoul judging;
   private int lastDread;

   public DeathsGuard(EntityType<? extends DeathsGuard> type, Level level) {
      super(type, level);
      this.bossEvent.setPlayBossMusic(true);
      this.lastDread = -1000;
      this.xpReward = 120;
      this.bossEvent.setVisible(false);
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 280.0)
         .add(Attributes.ARMOR, 12.0)
         .add(Attributes.ARMOR_TOUGHNESS, 4.0)
         .add(Attributes.ATTACK_DAMAGE, 11.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
         .add(Attributes.MOVEMENT_SPEED, 0.27)
         .add(Attributes.FOLLOW_RANGE, 40.0)
         .add(Attributes.STEP_HEIGHT, 1.1);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new DeathsGuard.FightGoal());
      this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.8));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F, 0.6F));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector
         .addGoal(
            2,
            new NearestAttackableTargetGoal<>(
               this, Player.class, 10, true, false, (target, level) -> target.distanceToSqr(Vec3.atCenterOf(this.getHomePosition())) < 256.0
            )
         );
   }

   @Override
   public @Nullable SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data
   ) {
      this.setHomeTo(this.blockPosition(), 8);
      return super.finalizeSpawn(level, difficulty, reason, data);
   }

   @Override
   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(ACTION, 0);
      builder.define(KNEEL, false);
   }

   public boolean kneeling() {
      return this.entityData.get(KNEEL);
   }

   public void kneel() {
      this.entityData.set(KNEEL, true);
      this.setNoAi(true);
      this.setPersistenceRequired();
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putBoolean("kneel", this.kneeling());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.entityData.set(KNEEL, input.getBooleanOr("kneel", false));
   }

   public int action() {
      return this.entityData.get(ACTION);
   }

   private void begin(int action) {
      this.actionTick = 0;
      this.entityData.set(ACTION, action, true);
      this.getNavigation().stop();
      switch (action) {
         case 1:
            this.playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 1.0F, 0.5F);
            break;
         case 2:
            this.playSound(SoundEvents.RAVAGER_ROAR, 1.2F, 0.6F);
            break;
         case 3:
            this.playSound(SoundEvents.WITHER_SKELETON_AMBIENT, 1.5F, 0.45F);
         case 4:
         case 5:
         default:
            break;
         case 6:
            this.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 2.0F, 0.6F);
            if (this.level() instanceof ServerLevel sl && this.random.nextBoolean()) {
               Speech.say(sl, this, "warden", "judge", 10466520);
            }
      }
   }

   public void debugAction(int action) {
      if (action == 0) {
         this.entityData.set(ACTION, 0, true);
      } else {
         this.begin(action);
      }
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
      if (ACTION.equals(accessor) && this.level().isClientSide()) {
         AnimationState[] all = new AnimationState[]{this.sweepAnim, this.slamAnim, this.lungeAnim, this.inspectAnim, this.condemnAnim, this.judgeAnim};

         for (AnimationState s : all) {
            s.stop();
         }

         int a = this.action();
         if (a >= 1 && a <= 6) {
            all[a - 1].start(this.tickCount);
         }
      }

      super.onSyncedDataUpdated(accessor);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level().isClientSide()) {
         this.readinessO = this.readiness;
         this.readiness = this.readiness + ((this.isAggressive() ? 1.0F : 0.0F) - this.readiness) * 0.12F;
      }
   }

   @Override
   protected void customServerAiStep(ServerLevel level) {
      super.customServerAiStep(level);
      this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
      this.bossEvent.setVisible(this.getTarget() != null || this.getHealth() < this.getMaxHealth());
      if (this.cooldown > 0) {
         this.cooldown--;
      }

      int action = this.action();
      if (action != 0) {
         this.actionTick++;
         if (this.actionTick == IMPACT[action]) {
            this.impact(level, action);
         }

         if (this.actionTick < IMPACT[action]) {
            this.telegraph(level, action);
         }

         if (action == 3 && this.actionTick > IMPACT[3] && this.actionTick < IMPACT[3] + 8) {
            Hazards.arc(level, this, 2.2F, 50.0F, 6.0F, 0.8F);
         }

         if (this.actionTick >= LENGTH[action]) {
            if (action == 4 && this.judging != null) {
               boolean pass = this.random.nextFloat() > 0.18F;
               if (!pass) {
                  this.begin(5);
                  return;
               }

               this.judging.judge(true);
               this.judging = null;
            }

            this.entityData.set(ACTION, 0, true);
            this.cooldown = action != 4 && action != 5 ? 24 + this.random.nextInt(16) : 30;
         }
      } else {
         if (this.getTarget() == null && this.cooldown == 0) {
            this.judgeTheDead(level);
         }
      }
   }

   private void telegraph(ServerLevel level, int action) {
      if (this.actionTick % 3 == 0) {
         float yaw = this.getYRot();
         switch (action) {
            case 1:
               for (int i = -4; i <= 4; i++) {
                  Vec3 p = this.position().add(Vec3.directionFromRotation(0.0F, yaw + i * 18).scale(3.4));
                  level.sendParticles(ModParticles.SOUL_FLAME, p.x, this.getY() + 0.15, p.z, 1, 0.05, 0.0, 0.05, 0.0);
               }
               break;
            case 2:
               Hazards.ring(level, this.position().add(Vec3.directionFromRotation(0.0F, yaw).scale(2.4)), 5.5F, 26);
               break;
            case 3:
               Vec3 dir = Vec3.directionFromRotation(0.0F, yaw);

               for (int i = 1; i <= 8; i++) {
                  Vec3 p = this.position().add(dir.scale(i * 1.1));
                  level.sendParticles(ModParticles.SOUL_FLAME, p.x, this.getY() + 0.15, p.z, 1, 0.08, 0.0, 0.08, 0.0);
               }
         }
      }
   }

   private void judgeTheDead(ServerLevel level) {
      List<LostSoul> waiting = level.getEntitiesOfClass(LostSoul.class, this.getBoundingBox().inflate(5.0, 2.0, 5.0), LostSoul::awaitingJudgement);
      if (!waiting.isEmpty()) {
         this.judging = waiting.getFirst();
         this.getLookControl().setLookAt(this.judging, 30.0F, 30.0F);
         this.begin(4);
      }
   }

   private void impact(ServerLevel level, int action) {
      Vec3 front = this.position().add(Vec3.directionFromRotation(0.0F, this.getYRot()).scale(2.4));
      switch (action) {
         case 1:
            Hazards.arc(level, this, 4.8F, 80.0F, (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE), 1.3F);

            for (int i = -4; i <= 4; i++) {
               Vec3 p = this.position().add(Vec3.directionFromRotation(0.0F, this.getYRot() + i * 18).scale(3.2));
               level.sendParticles(ParticleTypes.SWEEP_ATTACK, p.x, this.getY() + 1.4, p.z, 1, 0.0, 0.0, 0.0, 0.0);
               level.sendParticles(ModParticles.SOUL_FLAME, p.x, this.getY() + 1.4, p.z, 2, 0.1, 0.1, 0.1, 0.01);
            }

            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.0F, 0.5F);
            break;
         case 2:
            Hazards.shockwave(level, this, front, 5.5F, 16.0F, 0.7F);
            break;
         case 3: {
            Vec3 dir = Vec3.directionFromRotation(0.0F, this.getYRot());
            this.setDeltaMovement(dir.x * 1.35, 0.15, dir.z * 1.35);
            this.needsSync = true;
            this.playSound(SoundEvents.TRIDENT_RIPTIDE_1.value(), 1.5F, 0.6F);
         }
         case 4:
         default:
            break;
         case 5:
            if (this.judging != null && this.judging.isAlive()) {
               this.judging.judge(false);
            }

            this.judging = null;
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.4F, 0.6F);
            break;
         case 6: {
            LivingEntity t = this.getTarget();
            Vec3 dir = t != null
               ? t.position().subtract(this.position()).multiply(1.0, 0.0, 1.0).normalize()
               : Vec3.directionFromRotation(0.0F, this.getYRot());
            int lines = this.getHealth() < this.getMaxHealth() * 0.5F ? 3 : 1;

            for (int l = 0; l < lines; l++) {
               Vec3 d = dir.yRot((l - (lines - 1) / 2.0F) * 0.42F);

               for (int i = 1; i <= 9; i++) {
                  Vec3 p = this.position().add(d.scale(1.2 + i * 1.7));
                  Hazards.eruption(level, new Vec3(p.x, this.getY() + 1.0, p.z), 8 + i * 3, 12.0F, 1.4F, this);
               }
            }

            ModNet.shake(level, this.position(), 28.0, 1.4F, 14);
            this.playSound(SoundEvents.ANVIL_LAND, 1.6F, 0.5F);
            level.sendParticles(ModParticles.SOUL_FLAME, front.x, this.getY() + 0.3, front.z, 30, 0.6, 0.2, 0.6, 0.12);
         }
      }
   }

   @Override
   public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
      if (this.isNoAi() && !source.isCreativePlayer()) {
         return false;
      }

      if (this.action() == 4 || this.action() == 5) {
         this.judging = null;
         this.entityData.set(ACTION, 0, true);
      }

      boolean above = this.getHealth() > this.getMaxHealth() * 0.5F;
      boolean hurt = super.hurtServer(level, source, damage);
      if (hurt && above && this.getHealth() <= this.getMaxHealth() * 0.5F && this.isAlive()) {
         Speech.say(level, this, "warden", "half", 10466520);
      }

      return hurt;
   }

   @Override
   public void die(DamageSource source) {
      super.die(source);
      if (this.level() instanceof ServerLevel level) {
         Speech.say(level, this, "warden", "defeat", 10466520);
         Director.onGuardDefeated(level);
      }
   }

   @Override
   protected void tickDeath() {
      this.deathTime++;
      if (this.level() instanceof ServerLevel level) {
         if (this.deathTime % 4 == 0) {
            level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY() + 1.5, this.getZ(), 6, 0.5, 1.0, 0.5, 0.04);
         }

         if (this.deathTime >= 50 && !this.isRemoved()) {
            level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY() + 1.5, this.getZ(), 120, 0.6, 1.4, 0.6, 0.1);
            this.playSound(SoundEvents.WITHER_DEATH, 1.0F, 0.6F);
            ModNet.shake(level, this.position(), 40.0, 1.5F, 20);
            this.remove(RemovalReason.KILLED);
         }
      }
   }

   @Override
   public void startSeenByPlayer(ServerPlayer player) {
      super.startSeenByPlayer(player);
      this.bossEvent.addPlayer(player);
   }

   @Override
   public void stopSeenByPlayer(ServerPlayer player) {
      super.stopSeenByPlayer(player);
      this.bossEvent.removePlayer(player);
   }

   @Override
   public void checkDespawn() {
   }

   @Override
   public boolean removeWhenFarAway(double distSqr) {
      return false;
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return this.getTarget() == null ? null : SoundEvents.WITHER_SKELETON_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.WITHER_SKELETON_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.WITHER_SKELETON_DEATH;
   }

   @Override
   public float getVoicePitch() {
      return 0.55F;
   }

   @Override
   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.IRON_GOLEM_STEP, 0.8F, 0.7F);
   }

   private class FightGoal extends Goal {
      FightGoal() {
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         LivingEntity t = DeathsGuard.this.getTarget();
         return t != null && t.isAlive();
      }

      @Override
      public boolean requiresUpdateEveryTick() {
         return true;
      }

      @Override
      public void start() {
         DeathsGuard.this.setAggressive(true);
         if (DeathsGuard.this.level() instanceof ServerLevel sl && DeathsGuard.this.tickCount - DeathsGuard.this.lastDread > 600) {
            DeathsGuard.this.lastDread = DeathsGuard.this.tickCount;
            Hazards.dread(sl, DeathsGuard.this, 32.0, 120);
            Speech.say(sl, DeathsGuard.this, "warden", "start", 10466520);
         }
      }

      @Override
      public void stop() {
         DeathsGuard.this.setAggressive(false);
         DeathsGuard.this.getNavigation().stop();
      }

      @Override
      public void tick() {
         DeathsGuard g = DeathsGuard.this;
         LivingEntity target = g.getTarget();
         if (target != null) {
            int action = g.action();
            if (action == 0 || g.actionTick < DeathsGuard.IMPACT[action] - 3) {
               g.getLookControl().setLookAt(target, 20.0F, 20.0F);
               double dx = target.getX() - g.getX();
               double dz = target.getZ() - g.getZ();
               g.setYRot((float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
               g.yBodyRot = g.getYRot();
            }

            if (action == 0) {
               double dist = g.distanceTo(target);
               if (g.cooldown == 0) {
                  if (dist < 4.2) {
                     g.begin(g.random.nextFloat() < 0.6F ? 1 : 2);
                     return;
                  }

                  if (dist > 5.0 && dist < 18.0 && g.random.nextInt(22) == 0) {
                     g.begin(6);
                     g.cooldown = 50;
                     return;
                  }

                  if (dist > 7.0 && dist < 14.0 && g.random.nextInt(30) == 0) {
                     g.begin(3);
                     g.cooldown = 60;
                     return;
                  }
               }

               g.getNavigation().moveTo(target, 1.0);
            }
         }
      }
   }
}
