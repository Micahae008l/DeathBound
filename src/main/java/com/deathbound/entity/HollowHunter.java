package com.deathbound.entity;

import com.deathbound.registry.ModNet;
import com.deathbound.registry.ModParticles;
import com.deathbound.world.Director;
import com.deathbound.world.Layout;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
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
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

public class HollowHunter extends Monster {
   public static final int NONE = 0;
   public static final int AIM = 1;
   public static final int VOLLEY = 2;
   public static final int SNARE = 3;
   public static final int LEAP = 4;
   public static final int VANISH = 5;
   public static final int RIVEN = 6;
   private static final int[] LENGTH = new int[]{0, 32, 50, 22, 26, 26, 56};
   private static final int[] RELEASE = new int[]{0, 22, 34, 14, 7, 9, 40};
   private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(HollowHunter.class, EntityDataSerializers.INT);
   public final AnimationState aimAnim = new AnimationState();
   public final AnimationState volleyAnim = new AnimationState();
   public final AnimationState snareAnim = new AnimationState();
   public final AnimationState leapAnim = new AnimationState();
   public final AnimationState vanishAnim = new AnimationState();
   public final AnimationState rivenAnim = new AnimationState();
   private final ServerBossEvent bossEvent = new ServerBossEvent(
      UUID.randomUUID(), Component.translatable("entity.deathbound.hollow_hunter"), BossBarColor.WHITE, BossBarOverlay.NOTCHED_10
   );
   private int actionTick;
   private int cooldown;
   private int lastVanish;
   private int lastRiven;
   private Vec3 aimAt;
   private Vec3 mark;
   private boolean spoke;

   public HollowHunter(EntityType<? extends HollowHunter> type, Level level) {
      super(type, level);
      this.bossEvent.setPlayBossMusic(true);
      this.aimAt = Vec3.ZERO;
      this.mark = Vec3.ZERO;
      this.xpReward = 100;
      this.bossEvent.setVisible(false);
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 290.0)
         .add(Attributes.ARMOR, 6.0)
         .add(Attributes.ATTACK_DAMAGE, 7.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
         .add(Attributes.MOVEMENT_SPEED, 0.31)
         .add(Attributes.FOLLOW_RANGE, 48.0)
         .add(Attributes.STEP_HEIGHT, 1.2);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new HollowHunter.HuntGoal());
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 24.0F, 0.8F));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector
         .addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, (target, level) -> inHollow(target.position(), 6.0)));
   }

   private static boolean inHollow(Vec3 p, double margin) {
      Layout.Island h = Layout.HOLLOW;
      double dx = p.x - h.x();
      double dz = p.z - h.z();
      return dx * dx + dz * dz < (h.radius() + margin) * (h.radius() + margin) && p.y > h.top() - 20;
   }

   @Override
   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(ACTION, 0);
   }

   public int action() {
      return this.entityData.get(ACTION);
   }

   private boolean wounded() {
      return this.getHealth() < this.getMaxHealth() * 0.5F;
   }

   private void begin(int action) {
      this.actionTick = 0;
      this.entityData.set(ACTION, action, true);
      this.getNavigation().stop();
      switch (action) {
         case 1:
         case 3:
            this.playSound(SoundEvents.CROSSBOW_LOADING_START.value(), 1.4F, 0.5F);
            break;
         case 2:
            this.playSound(SoundEvents.CROSSBOW_QUICK_CHARGE_3.value(), 1.6F, 0.6F);
            break;
         case 4:
            this.playSound(SoundEvents.SKELETON_AMBIENT, 1.6F, 0.5F);
            break;
         case 5:
            this.playSound(SoundEvents.PHANTOM_SWOOP, 1.4F, 0.5F);
            break;
         case 6:
            this.lastRiven = this.tickCount;
            this.playSound(SoundEvents.CROSSBOW_LOADING_START.value(), 1.8F, 0.3F);
            this.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 2.0F, 0.6F);
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
         AnimationState[] all = new AnimationState[]{this.aimAnim, this.volleyAnim, this.snareAnim, this.leapAnim, this.vanishAnim, this.rivenAnim};

         for (AnimationState s : all) {
            s.stop();
         }

         int a = this.action();
         if (a >= 1 && a <= all.length) {
            all[a - 1].start(this.tickCount);
         }
      }

      super.onSyncedDataUpdated(accessor);
   }

   @Override
   protected void customServerAiStep(ServerLevel level) {
      super.customServerAiStep(level);
      if (this.isInWall() || this.getY() < Layout.HOLLOW.top() - 12) {
         level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.4, this.getZ(), 20, 0.3, 0.8, 0.3, 0.02);
         this.setDeltaMovement(Vec3.ZERO);
         this.fallDistance = 0.0;
         this.reappear(level, this.getTarget());
      }

      this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
      this.bossEvent.setVisible(this.getTarget() != null || this.getHealth() < this.getMaxHealth());
      if (this.cooldown > 0) {
         this.cooldown--;
      }

      int action = this.action();
      if (action != 0) {
         this.actionTick++;
         LivingEntity target = this.getTarget();
         int t = this.actionTick;
         int at = RELEASE[action];
         switch (action) {
            case 1:
            case 3:
               if (target != null && t <= at - 5) {
                  this.aimAt = target.getEyePosition().add(target.getDeltaMovement().scale(2.0));
               }

               if (t < at && t % 2 == 0) {
                  sightLine(level, this.getEyePosition(), this.aimAt);
               }

               if (t == at) {
                  this.loose(level, this.aimAt, action == 3);
                  if (action == 1 && this.wounded()) {
                     this.loose(level, this.aimAt.add(this.getLookAngle().cross(new Vec3(0.0, 1.0, 0.0)).normalize().scale(1.4)), false);
                  }
               }
               break;
            case 2:
               if (t == 6 && target != null) {
                  this.mark = target.position();
               }

               if (t >= 6 && t < at && t % 2 == 0) {
                  ring(level, this.mark, 3.2);
               }

               if (t == 10 || t == 16 || t == 22) {
                  this.playSound(SoundEvents.ARROW_SHOOT, 1.2F, 0.6F + t * 0.01F);
               }

               if (t == at) {
                  for (int i = 0; i < 14; i++) {
                     double a = this.random.nextDouble() * 3.141592653589793 * 2.0;
                     double d = Math.sqrt(this.random.nextDouble()) * 3.2;
                     HunterArrow arrow = this.arrow(level, false);
                     arrow.setPos(this.mark.x + Math.cos(a) * d, this.mark.y + 16.0 + this.random.nextDouble() * 4.0, this.mark.z + Math.sin(a) * d);
                     arrow.setDeltaMovement(0.0, -2.4, 0.0);
                     arrow.setBaseDamage(2.5);
                     level.addFreshEntity(arrow);
                  }
               }
               break;
            case 4:
               if (target != null && t == at && this.distanceToSqr(target) < 12.25) {
                  target.hurtServer(level, this.damageSources().mobAttack(this), 6.0F);
                  Vec3 push = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0).normalize();
                  target.push(push.x * 1.3, 0.45, push.z * 1.3);
                  target.needsSync = true;
                  this.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.2F, 0.6F);
               }

               if (t == at + 2 && target != null) {
                  Vec3 away = this.position().subtract(target.position()).multiply(1.0, 0.0, 1.0).normalize();
                  this.setDeltaMovement(away.x * 1.15, 0.62, away.z * 1.15);
                  this.needsSync = true;
               }
               break;
            case 5:
               if (t == at) {
                  this.setInvisible(true);
                  level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.4, this.getZ(), 40, 0.4, 1.0, 0.4, 0.02);
               }

               if (t == at + 8) {
                  this.reappear(level, target);
               }

               if (t == at + 10) {
                  this.setInvisible(false);
                  this.playSound(SoundEvents.SKELETON_HORSE_AMBIENT, 1.8F, 0.5F);
                  level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY() + 1.4, this.getZ(), 30, 0.4, 1.0, 0.4, 0.03);
               }
               break;
            case 6:
               if (target != null && t <= at - 8) {
                  this.aimAt = target.getEyePosition().add(0.0, -0.3, 0.0);
               }

               Vec3 eye = this.getEyePosition();
               if (t < at) {
                  if (t % 2 == 0 || t > at - 8) {
                     rivenLine(level, eye, this.aimAt, t > at - 8);
                  }

                  if (t > 12) {
                     Vec3 bow = eye.add(this.getLookAngle().scale(0.8));

                     for (int i = 0; i < 2; i++) {
                        Vec3 from = bow.add(this.random.nextGaussian() * 1.2, this.random.nextGaussian() * 1.2, this.random.nextGaussian() * 1.2);
                        Vec3 v = bow.subtract(from).scale(0.12);
                        level.sendParticles(ModParticles.SOUL_FLAME, from.x, from.y, from.z, 0, v.x, v.y, v.z, 1.0);
                     }
                  }

                  if (t == at - 8) {
                     this.playSound(SoundEvents.CROSSBOW_LOADING_END.value(), 2.0F, 0.4F);
                     this.playSound(SoundEvents.BEACON_POWER_SELECT, 1.6F, 1.4F);
                  }
               }

               if (t == at) {
                  this.rivenShot(level, eye, this.aimAt);
               }
         }

         if (this.actionTick >= LENGTH[action]) {
            this.setInvisible(false);
            this.entityData.set(ACTION, 0, true);
            this.cooldown = (this.wounded() ? 10 : 18) + this.random.nextInt(this.wounded() ? 14 : 22);
         }
      }
   }

   private HunterArrow arrow(ServerLevel level, boolean snare) {
      return HunterArrow.shotBy(level, this);
   }

   private void loose(ServerLevel level, Vec3 to, boolean snare) {
      HunterArrow arrow = this.arrow(level, snare);
      Vec3 from = this.getEyePosition().add(this.getLookAngle().scale(0.6));
      arrow.setPos(from);
      Vec3 d = to.subtract(from);
      arrow.shoot(d.x, d.y + d.horizontalDistance() * 0.05, d.z, 2.9F, 0.6F);
      arrow.setBaseDamage(snare ? 1.5 : 3.0);
      if (snare) {
         arrow.onHit(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
      }

      level.addFreshEntity(arrow);
      this.playSound(SoundEvents.ARROW_SHOOT, 1.6F, 0.5F);
   }

   private static void rivenLine(ServerLevel level, Vec3 from, Vec3 to, boolean locked) {
      Vec3 d = to.subtract(from).normalize();
      double k = 1.0;

      while (k < 40.0) {
         Vec3 p = from.add(d.scale(k));
         level.sendParticles(locked ? ModParticles.SOUL_FLAME : ModParticles.SOUL_MOTE, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
         k += locked ? 0.4 : 0.8;
      }
   }

   private void rivenShot(ServerLevel level, Vec3 eye, Vec3 to) {
      HunterArrow arrow = HunterArrow.shotBy(level, this);
      Vec3 from = eye.add(this.getLookAngle().scale(0.8));
      arrow.setPos(from);
      Vec3 d = to.subtract(from);
      arrow.shoot(d.x, d.y, d.z, 2.0F, 0.0F);
      arrow.riven(13.0F);
      level.addFreshEntity(arrow);
      this.playSound(SoundEvents.CROSSBOW_SHOOT, 2.5F, 0.4F);
      this.playSound(SoundEvents.WITHER_SHOOT, 1.6F, 0.6F);
      level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, -4616961), from.x, from.y, from.z, 1, 0.0, 0.0, 0.0, 0.0);
      ModNet.shake(level, this.position(), 28.0, 1.2F, 10);
      Vec3 back = this.position().subtract(to).multiply(1.0, 0.0, 1.0).normalize();
      this.setDeltaMovement(back.x * 0.5, 0.15, back.z * 0.5);
      this.needsSync = true;
   }

   private static void sightLine(ServerLevel level, Vec3 from, Vec3 to) {
      Vec3 d = to.subtract(from);
      int n = (int)Math.min(40.0, d.length() * 1.5);

      for (int i = 1; i < n; i++) {
         Vec3 p = from.add(d.scale((double)i / n));
         level.sendParticles(ModParticles.SOUL_MOTE, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
      }
   }

   private static void ring(ServerLevel level, Vec3 c, double r) {
      for (int i = 0; i < 24; i++) {
         double a = i * 3.141592653589793 * 2.0 / 24.0;
         level.sendParticles(ModParticles.SOUL_FLAME, c.x + Math.cos(a) * r, c.y + 0.1, c.z + Math.sin(a) * r, 1, 0.0, 0.02, 0.0, 0.0);
      }
   }

   private void reappear(ServerLevel level, LivingEntity target) {
      Layout.Island h = Layout.HOLLOW;
      Vec3 best = null;

      for (int i = 0; i < 24 && best == null; i++) {
         double a = this.random.nextDouble() * 3.141592653589793 * 2.0;
         double d = 6.0 + this.random.nextDouble() * (h.radius() - 9);
         int x = (int)(h.x() + Math.cos(a) * d);
         int z = (int)(h.z() + Math.sin(a) * d);
         BlockPos top = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
         Vec3 spot = Vec3.atBottomCenterOf(top);
         boolean fits = level.noCollision(this, this.getDimensions(this.getPose()).makeBoundingBox(spot).inflate(0.2, 0.0, 0.2))
            && level.getBlockState(top.below()).isFaceSturdy(level, top.below(), Direction.UP);
         if (fits && top.getY() > h.top() - 4 && top.getY() <= h.top() + 3 && (target == null || spot.distanceTo(target.position()) > 10.0)) {
            best = spot;
         }
      }

      if (best == null) {
         best = new Vec3(h.x() + 9.5, h.top() + 8, h.z() - 6.5);
      }

      this.teleportTo(best.x, best.y, best.z);
   }

   @Override
   public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
      if (this.action() == 5 && this.isInvisible()) {
         return false;
      }

      boolean above = !this.wounded();
      boolean hurt = super.hurtServer(level, source, damage);
      if (hurt && above && this.wounded() && this.isAlive()) {
         Speech.say(level, this, "hunter", "half", 14209732);
      }

      return hurt;
   }

   @Override
   public void die(DamageSource source) {
      super.die(source);
      if (this.level() instanceof ServerLevel level) {
         Speech.say(level, this, "hunter", "defeat", 14209732);
         Director.onHunterDefeated(level);
      }
   }

   @Override
   protected void tickDeath() {
      this.deathTime++;
      if (this.level() instanceof ServerLevel level) {
         if (this.deathTime % 5 == 0) {
            level.sendParticles(ParticleTypes.WHITE_ASH, this.getX(), this.getY() + 1.5, this.getZ(), 10, 0.4, 1.0, 0.4, 0.02);
         }

         if (this.deathTime >= 40 && !this.isRemoved()) {
            level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY() + 1.5, this.getZ(), 80, 0.5, 1.2, 0.5, 0.06);
            this.playSound(SoundEvents.SKELETON_DEATH, 1.2F, 0.4F);
            ModNet.shake(level, this.position(), 32.0, 1.0F, 14);
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
   public float getVoicePitch() {
      return 0.5F;
   }

   @Override
   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.SKELETON_STEP, 0.6F, 0.6F);
   }

   private class HuntGoal extends Goal {
      private int strafe = 1;
      private int strafeTicks;

      HuntGoal() {
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         LivingEntity t = HollowHunter.this.getTarget();
         return t != null && t.isAlive();
      }

      @Override
      public boolean requiresUpdateEveryTick() {
         return true;
      }

      @Override
      public void start() {
         HollowHunter h = HollowHunter.this;
         h.setAggressive(true);
         if (!h.spoke && h.level() instanceof ServerLevel sl) {
            h.spoke = true;
            Speech.say(sl, h, "hunter", "start", 14209732);
         }

         if (h.getY() > Layout.HOLLOW.top() + 4 && h.action() == 0) {
            h.lastVanish = h.tickCount;
            h.begin(5);
         }
      }

      @Override
      public void stop() {
         HollowHunter.this.setAggressive(false);
         HollowHunter.this.getNavigation().stop();
      }

      @Override
      public void tick() {
         HollowHunter h = HollowHunter.this;
         LivingEntity target = h.getTarget();
         if (target != null) {
            h.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (h.action() != 0) {
               double dx = target.getX() - h.getX();
               double dz = target.getZ() - h.getZ();
               h.setYRot((float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
               h.yBodyRot = h.getYRot();
            } else {
               double dist = h.distanceTo(target);
               if (h.cooldown == 0) {
                  if (dist < 4.5) {
                     h.begin(4);
                  } else if (h.tickCount - h.lastVanish > (h.wounded() ? 200 : 320) && h.random.nextInt(4) == 0) {
                     h.lastVanish = h.tickCount;
                     h.begin(5);
                  } else if (dist > 7.0 && h.tickCount - h.lastRiven > (h.wounded() ? 260 : 420) && h.random.nextInt(3) == 0) {
                     h.begin(6);
                  } else {
                     int roll = h.random.nextInt(10);
                     h.begin(roll < 5 ? 1 : (roll < 7 ? 3 : (dist > 6.0 ? 2 : 1)));
                  }
               } else {
                  if (dist < 8.0) {
                     Vec3 away = h.position().add(h.position().subtract(target.position()).multiply(1.0, 0.0, 1.0).normalize().scale(6.0));
                     h.getNavigation().moveTo(away.x, away.y, away.z, 1.1);
                  } else if (dist > 17.0) {
                     h.getNavigation().moveTo(target, 1.0);
                  } else {
                     if (--this.strafeTicks <= 0) {
                        this.strafe = -this.strafe;
                        this.strafeTicks = 30 + h.random.nextInt(40);
                     }

                     Vec3 side = target.position()
                        .subtract(h.position())
                        .multiply(1.0, 0.0, 1.0)
                        .normalize()
                        .cross(new Vec3(0.0, 1.0, 0.0))
                        .scale(4 * this.strafe);
                     Vec3 to = h.position().add(side);
                     h.getNavigation().moveTo(to.x, to.y, to.z, 0.9);
                  }
               }
            }
         }
      }
   }
}
