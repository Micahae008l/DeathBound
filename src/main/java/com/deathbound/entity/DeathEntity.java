package com.deathbound.entity;

import com.deathbound.registry.ModEntities;
import com.deathbound.registry.ModNet;
import com.deathbound.registry.ModParticles;
import com.deathbound.world.Director;
import com.deathbound.world.Layout;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class DeathEntity extends Monster {
   public static final int THRONE = 1;
   public static final int REAPER = 2;
   public static final int BEAST = 3;
   public static final int IDLE = 0;
   public static final int GESTURE = 10;
   public static final int RISE = 11;
   public static final int BARRAGE = 20;
   public static final int BLINK = 21;
   public static final int NOVA = 22;
   public static final int SUMMON = 23;
   public static final int TRANSFORM = 29;
   public static final int CLAW = 30;
   public static final int LEAP = 31;
   public static final int CHARGE = 32;
   public static final int RIP = 33;
   public static final int ROAR = 34;
   private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(DeathEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(DeathEntity.class, EntityDataSerializers.INT);
   private static final float BEAST_AT = 0.45F;
   public final AnimationState gestureAnim = new AnimationState();
   public final AnimationState riseAnim = new AnimationState();
   public final AnimationState barrageAnim = new AnimationState();
   public final AnimationState blinkAnim = new AnimationState();
   public final AnimationState novaAnim = new AnimationState();
   public final AnimationState summonAnim = new AnimationState();
   public final AnimationState transformAnim = new AnimationState();
   public final AnimationState clawAnim = new AnimationState();
   public final AnimationState leapAnim = new AnimationState();
   public final AnimationState chargeAnim = new AnimationState();
   public final AnimationState ripAnim = new AnimationState();
   public final AnimationState roarAnim = new AnimationState();
   private final ServerBossEvent bossEvent = new ServerBossEvent(
      UUID.randomUUID(), Component.translatable("boss.deathbound.death.throne"), BossBarColor.PURPLE, BossBarOverlay.NOTCHED_10
   );
   private int actionTick;
   private int cooldown;
   private int hazardClock;
   private int pendingHazard;
   private float novaRadius;
   private boolean leaping;
   private Vec3 chargeDir;
   private final Set<UUID> struck;
   public static final int DYING = 170;
   private DamageSource killedBy;

   public DeathEntity(EntityType<? extends DeathEntity> type, Level level) {
      super(type, level);
      this.bossEvent.setPlayBossMusic(true);
      this.cooldown = 40;
      this.hazardClock = 60;
      this.novaRadius = -1.0F;
      this.chargeDir = Vec3.ZERO;
      this.struck = new HashSet<>();
      this.xpReward = 500;
      this.bossEvent.setDarkenScreen(true);
      this.bossEvent.setCreateWorldFog(true);
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 700.0)
         .add(Attributes.ARMOR, 10.0)
         .add(Attributes.ARMOR_TOUGHNESS, 6.0)
         .add(Attributes.ATTACK_DAMAGE, 10.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
         .add(Attributes.MOVEMENT_SPEED, 0.26)
         .add(Attributes.FOLLOW_RANGE, 48.0)
         .add(Attributes.STEP_HEIGHT, 1.6);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new DeathEntity.HuntGoal());
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector
         .addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 5, false, false, (t, level) -> Layout.inArena(t.getX(), t.getY(), t.getZ())));
   }

   @Override
   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(PHASE, 1);
      builder.define(ACTION, 0);
   }

   public int phase() {
      return this.entityData.get(PHASE);
   }

   public int action() {
      return this.entityData.get(ACTION);
   }

   private void setPhase(int phase) {
      if (phase != this.phase() && phase > 1 && this.level() instanceof ServerLevel level) {
         level.playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 4.0F, 0.5F);
         level.playSound(null, this.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.5F, 0.6F);
      }

      this.entityData.set(PHASE, phase);
   }

   private void begin(int action) {
      this.actionTick = 0;
      this.struck.clear();
      this.entityData.set(ACTION, action, true);
      this.getNavigation().stop();
      if (action == 21 && this.level() instanceof ServerLevel sl && this.random.nextFloat() < 0.3F) {
         Speech.say(sl, this, "death", "blink", 13215487);
      }
   }

   public void debugPhase(int phase) {
      this.setPhase(phase);
      this.entityData.set(ACTION, 0, true);
   }

   public void debugAction(int action) {
      if (action == 0) {
         this.entityData.set(ACTION, 0, true);
      } else {
         this.begin(action);
      }
   }

   private static int length(int action) {
      return switch (action) {
         case 10 -> 34;
         case 11 -> 70;
         default -> 0;
         case 20 -> 46;
         case 21 -> 32;
         case 22 -> 56;
         case 23 -> 40;
         case 29 -> 110;
         case 30 -> 34;
         case 31 -> 56;
         case 32 -> 48;
         case 33 -> 44;
         case 34 -> 40;
      };
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
      if (PHASE.equals(accessor)) {
         this.refreshDimensions();
      }

      if (ACTION.equals(accessor) && this.level().isClientSide()) {
         AnimationState[] all = new AnimationState[]{
            this.gestureAnim,
            this.riseAnim,
            this.barrageAnim,
            this.blinkAnim,
            this.novaAnim,
            this.summonAnim,
            this.transformAnim,
            this.clawAnim,
            this.leapAnim,
            this.chargeAnim,
            this.ripAnim,
            this.roarAnim
         };

         for (AnimationState s : all) {
            s.stop();
         }
         AnimationState start = switch (this.action()) {
            case 10 -> this.gestureAnim;
            case 11 -> this.riseAnim;
            default -> null;
            case 20 -> this.barrageAnim;
            case 21 -> this.blinkAnim;
            case 22 -> this.novaAnim;
            case 23 -> this.summonAnim;
            case 29 -> this.transformAnim;
            case 30 -> this.clawAnim;
            case 31 -> this.leapAnim;
            case 32 -> this.chargeAnim;
            case 33 -> this.ripAnim;
            case 34 -> this.roarAnim;
         };
         if (start != null) {
            start.start(this.tickCount);
         }
      }

      super.onSyncedDataUpdated(accessor);
   }

   @Override
   public EntityDimensions getDefaultDimensions(Pose pose) {
      return this.phase() == 3 ? EntityDimensions.scalable(2.4F, 4.1F).withEyeHeight(3.4F) : super.getDefaultDimensions(pose);
   }

   @Override
   public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
      int a = this.action();
      return (this.phase() == 1 || a == 11 || a == 29) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ? true : super.isInvulnerableTo(level, source);
   }

   @Override
   public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
      if (this.phase() == 3 && this.getHealth() > this.getMaxHealth() * 0.2F && this.getHealth() - damage <= this.getMaxHealth() * 0.2F) {
         Speech.say(level, this, "death", "low", 13215487);
      }

      if (this.phase() == 1 && source.getEntity() instanceof Player p && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         p.sendOverlayMessage(Component.translatable("message.deathbound.death.bound").withStyle(ChatFormatting.DARK_PURPLE));
         level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY() + 1.6, this.getZ(), 10, 0.4, 0.6, 0.4, 0.02);
      }

      return super.hurtServer(level, source, damage);
   }

   public void onAnchorBroken() {
      this.playSound(SoundEvents.WITHER_HURT, 2.0F, 0.5F);
      ModNet.shake((ServerLevel)this.level(), this.position(), 48.0, 1.5F, 12);
   }

   private List<SoulAnchor> anchors(ServerLevel level) {
      return level.getEntitiesOfClass(SoulAnchor.class, Layout.arenaBox(), a -> a.isAlive() && a.isBoundTo(this));
   }

   private List<ServerPlayer> challengers(ServerLevel level) {
      return level.getPlayers(p -> !p.isSpectator() && !p.isCreative() && Layout.inArena(p.getX(), p.getY(), p.getZ()));
   }

   @Override
   protected void customServerAiStep(ServerLevel level) {
      super.customServerAiStep(level);
      int phase = this.phase();
      this.updateBossBar(level, phase);
      if (this.cooldown > 0) {
         this.cooldown--;
      }

      int action = this.action();
      if (action != 0) {
         this.actionTick++;
         this.performAction(level, action, this.actionTick);
         if (this.actionTick >= length(action)) {
            this.finishAction(level, action);
         }
      } else {
         switch (phase) {
            case 1:
               this.thronePhase(level);
               break;
            case 2:
               if (this.getHealth() <= this.getMaxHealth() * 0.45F) {
                  this.begin(29);
               } else {
                  this.chooseReaperAction(level);
               }
               break;
            default:
               this.chooseBeastAction(level);
         }
      }
   }

   private void updateBossBar(ServerLevel level, int phase) {
      if (phase == 1) {
         this.bossEvent.setName(Component.translatable("boss.deathbound.death.throne"));
         this.bossEvent.setProgress(this.anchors(level).size() / 4.0F);
      } else {
         this.bossEvent.setName(Component.translatable(phase == 2 ? "boss.deathbound.death.reaper" : "boss.deathbound.death.beast"));
         this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
         this.bossEvent.setColor(phase == 3 ? BossBarColor.RED : BossBarColor.PURPLE);
      }
   }

   private void thronePhase(ServerLevel level) {
      BlockPos seat = Layout.THRONE_SEAT;
      this.setPos(seat.getX() + 0.5, seat.getY() + 0.5, seat.getZ() + 0.5);
      this.setDeltaMovement(Vec3.ZERO);
      this.setYRot(0.0F);
      this.yBodyRot = 0.0F;
      Player nearest = level.getNearestPlayer(this, 40.0);
      if (nearest != null) {
         this.getLookControl().setLookAt(nearest, 10.0F, 30.0F);
      }

      if (this.anchors(level).isEmpty()) {
         this.begin(11);
      } else {
         if (--this.hazardClock <= 0 && !this.challengers(level).isEmpty()) {
            this.hazardClock = 90 + this.random.nextInt(50);
            long gravebound = level.getEntitiesOfClass(Gravebound.class, Layout.arenaBox()).size();
            this.pendingHazard = this.random.nextInt(gravebound < 5L ? 3 : 2);
            this.begin(10);
         }
      }
   }

   private void chooseReaperAction(ServerLevel level) {
      LivingEntity target = this.getTarget();
      if (target != null && this.cooldown <= 0) {
         double dist = this.distanceTo(target);
         int roll = this.random.nextInt(100);
         if (dist < 4.5) {
            this.begin(roll < 55 ? 21 : 22);
         } else if (roll < 38) {
            this.begin(20);
         } else if (roll < 60) {
            this.begin(22);
         } else {
            this.begin(21);
         }
      }
   }

   private void chooseBeastAction(ServerLevel level) {
      LivingEntity target = this.getTarget();
      if (target != null && this.cooldown <= 0) {
         double dist = this.distanceTo(target);
         int roll = this.random.nextInt(100);
         if (dist < 5.5) {
            this.begin(roll < 60 ? 30 : (roll < 85 ? 33 : 34));
         } else if (dist < 18.0) {
            this.begin(roll < 50 ? 31 : (roll < 82 ? 32 : 33));
         }
      }
   }

   private void performAction(ServerLevel level, int action, int t) {
      LivingEntity target = this.getTarget();
      switch (action) {
         case 10:
            if (t == 14) {
               this.unleashHazard(level);
            }
            break;
         case 11:
            if (t == 1) {
               Hazards.dread(level, this, 48.0, 100);
               Speech.say(level, this, "death", "rise", 13215487);
               this.playSound(SoundEvents.WITHER_SPAWN, 2.0F, 0.6F);
               ModNet.shake(level, this.position(), 64.0, 2.0F, 50);
            }

            if (t % 3 == 0) {
               level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 8, 0.8, 1.2, 0.8, 0.06);
            }

            if (t == 40) {
               this.challengers(level).forEach(p -> ModNet.cinematic(p, 5));
            }
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         default:
            break;
         case 20:
            if (t >= 10 && t <= 34 && (t - 10) % 6 == 0) {
               int k = (t - 10) / 6;
               double a = Math.toRadians(this.yBodyRot + 90.0F) + (k - 2) * 0.55;
               Vec3 at = this.position().add(Math.cos(a) * 1.8, 2.8 + k % 2 * 0.6, Math.sin(a) * 1.8);
               level.addFreshEntity(new DeathOrb(level, this, at, 34 - k * 5));
               this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0F, 0.6F + k * 0.08F);
            }
            break;
         case 21:
            if (t == 8 && target != null) {
               Vec3 behind = target.position().subtract(Vec3.directionFromRotation(0.0F, target.getYRot()).scale(2.2));
               level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY() + 1.2, this.getZ(), 40, 0.4, 1.0, 0.4, 0.06);
               this.teleportTo(behind.x, Hazards.groundAt(level, behind).y, behind.z);
               this.lookAtTarget(target);
               level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY() + 1.2, this.getZ(), 40, 0.4, 1.0, 0.4, 0.06);
               this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.5F, 0.5F);
            }

            if (t == 17) {
               Hazards.arc(level, this, 3.8F, 75.0F, 12.0F, 1.1F);
               this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.6F, 0.5F);
            }
            break;
         case 22:
            if (t == 22) {
               this.novaRadius = 1.0F;
               this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 1.4F, 0.6F);
               ModNet.shake(level, this.position(), 30.0, 1.2F, 10);
            }

            if (this.novaRadius > 0.0F) {
               this.novaRadius += 0.7F;
               Hazards.ring(level, this.position().add(0.0, 0.25, 0.0), this.novaRadius, (int)(this.novaRadius * 6.0F));

               for (LivingEntity v : level.getEntitiesOfClass(
                  LivingEntity.class, this.getBoundingBox().inflate(this.novaRadius + 1.0F, 1.5, this.novaRadius + 1.0F)
               )) {
                  if (v != this && !v.isAlliedTo(this) && !(v instanceof SoulAnchor) && !this.struck.contains(v.getUUID())) {
                     double d = v.position().distanceTo(this.position());
                     if (Math.abs(d - this.novaRadius) < 0.9 && v.getY() < this.getY() + 0.6) {
                        this.struck.add(v.getUUID());
                        if (v.hurtServer(level, Hazards.soulRend(level, this), 9.0F)) {
                           Vec3 out = v.position().subtract(this.position()).normalize().scale(0.8);
                           v.push(out.x, 0.4, out.z);
                           v.needsSync = true;
                        }
                     }
                  }
               }

               if (this.novaRadius > 18.0F) {
                  this.novaRadius = -1.0F;
               }
            }
            break;
         case 23:
            if (t == 20) {
               for (int i = 0; i < 3; i++) {
                  SoulWisp wisp = ModEntities.SOUL_WISP.create(level, EntitySpawnReason.MOB_SUMMONED);
                  if (wisp != null) {
                     double a = i * 3.141592653589793 * 2.0 / 3.0;
                     wisp.snapTo(this.getX() + Math.cos(a) * 2.0, this.getY() + 3.0, this.getZ() + Math.sin(a) * 2.0, 0.0F, 0.0F);
                     wisp.setTarget(target);
                     level.addFreshEntity(wisp);
                     level.sendParticles(ModParticles.SOUL_FLAME, wisp.getX(), wisp.getY(), wisp.getZ(), 20, 0.3, 0.3, 0.3, 0.05);
                  }
               }

               this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.6F, 0.6F);
            }
            break;
         case 29:
            this.setDeltaMovement(0.0, t < 50 ? 0.03 : -0.02, 0.0);
            if (t % 2 == 0) {
               level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY() + 1.5, this.getZ(), 12, 1.0, 1.6, 1.0, 0.1);
            }

            if (t == 1) {
               Hazards.dread(level, this, 48.0, 120);
               Speech.say(level, this, "death", "beast", 13215487);
               this.playSound(SoundEvents.WITHER_SPAWN, 2.5F, 0.4F);
               this.challengers(level).forEach(p -> ModNet.cinematic(p, 6));
            }

            if (t == 50) {
               this.setPhase(3);
               level.sendParticles(
                  ColorParticleOption.create(ParticleTypes.FLASH, -3632897), this.getX(), this.getY() + 2.0, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0
               );
               level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY() + 2.0, this.getZ(), 300, 1.5, 2.0, 1.5, 0.3);
               this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 2.5F, 0.5F);
               ModNet.shake(level, this.position(), 80.0, 3.5F, 40);

               for (LivingEntity v : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(7.0))) {
                  if (v != this && !(v instanceof SoulAnchor)) {
                     Vec3 out = v.position().subtract(this.position()).normalize().scale(1.4);
                     v.push(out.x, 0.5, out.z);
                     v.needsSync = true;
                  }
               }
            }
            break;
         case 30:
            if (t == 12 || t == 24) {
               Hazards.arc(level, this, 4.8F, 85.0F, 11.0F, 1.2F);
               this.playSound(SoundEvents.RAVAGER_ATTACK, 1.6F, 0.6F);
            }

            if (t < 12 && target != null) {
               this.lookAtTarget(target);
            }
            break;
         case 31:
            if (t < 12 && target != null) {
               this.lookAtTarget(target);
            }

            if (t == 12 && target != null) {
               Vec3 to = target.position().subtract(this.position());
               double d = Math.min(to.horizontalDistance(), 16.0);
               Vec3 flat = to.multiply(1.0, 0.0, 1.0).normalize().scale(d / 16.0 * 1.3);
               this.setDeltaMovement(flat.x, 0.95, flat.z);
               this.needsSync = true;
               this.leaping = true;
               this.playSound(SoundEvents.RAVAGER_ROAR, 2.0F, 0.6F);
            }

            if (this.leaping && t > 16 && this.onGround()) {
               this.leaping = false;
               Hazards.shockwave(level, this, this.position(), 6.5F, 16.0F, 0.9F);
               this.actionTick = Math.max(this.actionTick, length(31) - 14);
            }
            break;
         case 32:
            if (t < 14 && target != null) {
               this.lookAtTarget(target);
               this.chargeDir = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0).normalize();
            }

            if (t == 10) {
               this.playSound(SoundEvents.RAVAGER_ROAR, 2.2F, 0.5F);
            }

            if (t >= 14 && t < 40) {
               this.setDeltaMovement(this.chargeDir.x * 0.85, this.getDeltaMovement().y, this.chargeDir.z * 0.85);
               this.needsSync = true;

               for (LivingEntity v : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.8))) {
                  if (v != this
                     && !v.isAlliedTo(this)
                     && !(v instanceof SoulAnchor)
                     && this.struck.add(v.getUUID())
                     && v.hurtServer(level, Hazards.crush(level, this), 13.0F)) {
                     v.push(this.chargeDir.x * 1.6, 0.6, this.chargeDir.z * 1.6);
                     v.needsSync = true;
                  }
               }

               if (t % 2 == 0) {
                  level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY() + 0.3, this.getZ(), 6, 0.8, 0.1, 0.8, 0.02);
               }

               if (this.horizontalCollision) {
                  ModNet.shake(level, this.position(), 30.0, 2.0F, 10);
                  this.actionTick = 40;
               }
            }
            break;
         case 33:
            if (t < 16 && target != null) {
               this.lookAtTarget(target);
            }

            if (t == 18) {
               Vec3 dir = Vec3.directionFromRotation(0.0F, this.getYRot());

               for (int i = 0; i < 7; i++) {
                  Hazards.eruption(level, this.position().add(dir.scale(2.5 + i * 2.0)), 4 + i * 3, 11.0F, 1.8F, this);
               }

               Hazards.shockwave(level, this, this.position().add(dir.scale(2.0)), 3.0F, 8.0F, 0.4F);
            }
            break;
         case 34:
            if (t == 14) {
               this.playSound(SoundEvents.WARDEN_ROAR, 3.0F, 0.6F);
               ModNet.shake(level, this.position(), 48.0, 3.0F, 25);

               for (LivingEntity v : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0))) {
                  if (v != this && !(v instanceof SoulAnchor)) {
                     if (v instanceof Player p) {
                        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0), this);
                     }

                     if (v.distanceTo(this) < 7.0F) {
                        Vec3 out = v.position().subtract(this.position()).normalize().scale(1.5);
                        v.push(out.x, 0.5, out.z);
                        v.needsSync = true;
                     }
                  }
               }
            }
      }
   }

   private void unleashHazard(ServerLevel level) {
      List<ServerPlayer> players = this.challengers(level);
      switch (this.pendingHazard) {
         case 0:
            for (ServerPlayer p : players) {
               Hazards.eruption(level, p.position(), 30, 9.0F, 2.2F, this);

               for (int i = 0; i < 2; i++) {
                  Hazards.eruption(
                     level,
                     p.position().add((this.random.nextDouble() - 0.5) * 10.0, 0.0, (this.random.nextDouble() - 0.5) * 10.0),
                     36 + i * 6,
                     9.0F,
                     2.2F,
                     this
                  );
               }
            }

            this.playSound(SoundEvents.SOUL_ESCAPE.value(), 3.0F, 0.4F);
            break;
         case 1:
            for (ServerPlayer p : players) {
               for (int i = 0; i < 7; i++) {
                  Vec3 start = p.position()
                     .add((this.random.nextDouble() - 0.5) * 12.0, 16.0 + this.random.nextDouble() * 4.0, (this.random.nextDouble() - 0.5) * 12.0);
                  Vec3 aim = p.position().add((this.random.nextDouble() - 0.5) * 4.0, 0.0, (this.random.nextDouble() - 0.5) * 4.0);
                  level.addFreshEntity(new SoulBolt(level, start.x, start.y, start.z, aim.subtract(start).normalize(), 5.0F));
               }
            }

            this.playSound(SoundEvents.EVOKER_CAST_SPELL, 2.0F, 0.5F);
            break;
         default:
            for (int i = 0; i < 2; i++) {
               BlockPos at = Layout.ANCHORS.get(this.random.nextInt(Layout.ANCHORS.size()));
               Gravebound g = ModEntities.GRAVEBOUND.create(level, EntitySpawnReason.MOB_SUMMONED);
               if (g != null) {
                  g.snapTo(
                     at.getX() + 0.5 + this.random.nextInt(5) - 2.0, Layout.CITADEL.top() + 1, at.getZ() + 0.5 + 3.0, this.random.nextFloat() * 360.0F, 0.0F
                  );
                  g.finalizeSpawn(level, level.getCurrentDifficultyAt(g.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
                  level.addFreshEntity(g);
               }
            }

            this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 2.0F, 0.5F);
      }
   }

   private void finishAction(ServerLevel level, int action) {
      this.entityData.set(ACTION, 0, true);
      this.novaRadius = -1.0F;
      this.leaping = false;
      if (action == 11) {
         this.setPhase(2);
         this.cooldown = 30;
         this.setHomeTo(Layout.ARENA_CENTER, 28);
      } else {
         this.cooldown = switch (this.phase()) {
            case 2 -> 30 + this.random.nextInt(30);
            case 3 -> 12 + this.random.nextInt(18);
            default -> 0;
         };
      }
   }

   private void lookAtTarget(LivingEntity target) {
      double dx = target.getX() - this.getX();
      double dz = target.getZ() - this.getZ();
      this.setYRot((float)(Mth.atan2(dz, dx) * 57.2957763671875) - 90.0F);
      this.yBodyRot = this.getYRot();
      this.yHeadRot = this.getYRot();
   }

   @Override
   public void die(DamageSource source) {
      super.die(source);
      if (this.level() instanceof ServerLevel level) {
         Speech.say(level, this, "death", "defeat", 13215487);
         this.playSound(SoundEvents.WITHER_DEATH, 3.0F, 0.5F);
         ModNet.shake(level, this.position(), 80.0, 2.0F, 20);
         this.bossEvent.removeAllPlayers();
         level.getPlayers(p -> p.distanceToSqr(this) < 14400.0).forEach(p -> ModNet.music(p, 2));
      }
   }

   @Override
   protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
      this.killedBy = source;
   }

   @Override
   protected void tickDeath() {
      this.deathTime++;
      if (this.level() instanceof ServerLevel level) {
         int var14 = this.deathTime;
         double x = this.getX();
         double y = this.getY();
         double z = this.getZ();
         double mid = y + this.getBbHeight() * 0.55;
         this.lastHurtByPlayerMemoryTime = Math.max(this.lastHurtByPlayerMemoryTime, 20);
         level.sendParticles(ModParticles.SOUL_FLAME, x, mid, z, 1 + var14 / 12, 0.6, this.getBbHeight() * 0.3, 0.6, 0.01 + var14 * 6.0E-4);
         if (var14 % 20 == 0 && var14 <= 120) {
            this.playSound(SoundEvents.WARDEN_HEARTBEAT, 5.0F, 0.75F - var14 * 0.003F);
         }

         if (var14 == 30) {
            this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 4.0F, 0.45F);
            ModNet.shake(level, this.position(), 96.0, 3.0F, 40);
         }

         if (var14 >= 50 && var14 < 145) {
            for (int i = 0; i < 3; i++) {
               level.sendParticles(
                  ParticleTypes.SOUL,
                  x + this.random.nextGaussian() * 0.4,
                  mid,
                  z + this.random.nextGaussian() * 0.4,
                  0,
                  this.random.nextGaussian() * 0.05,
                  1.0,
                  this.random.nextGaussian() * 0.05,
                  0.35 + this.random.nextDouble() * 0.4
               );
            }

            if (var14 % 10 == 0) {
               level.playSound(null, x, mid, z, SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 3.0F, 0.5F + this.random.nextFloat() * 0.4F);

               for (int i = 0; i < 32; i++) {
                  double a = i * 3.141592653589793 * 2.0 / 32.0;
                  level.sendParticles(ModParticles.SOUL_FLAME, x, y + 0.2, z, 0, Math.cos(a), 0.0, Math.sin(a), 0.35);
               }
            }
         }

         if (var14 == 100) {
            this.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 3.0F, 0.5F);
         }

         if (var14 >= 170 && !this.isRemoved()) {
            level.sendParticles(ModParticles.SOUL_FLAME, x, y + 2.0, z, 400, 2.0, 3.0, 2.0, 0.25);
            level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, -3632897), x, y + 2.0, z, 1, 0.0, 0.0, 0.0, 0.0);
            level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 3.0F, 0.4F);
            level.playSound(null, x, y, z, SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 4.0F, 0.5F);
            ModNet.shake(level, this.position(), 96.0, 2.5F, 25);
            super.dropAllDeathLoot(level, this.killedBy != null ? this.killedBy : this.damageSources().generic());
            Director.onDeathDefeated(level);
            this.remove(RemovalReason.KILLED);
         }
      }
   }

   @Override
   public void startSeenByPlayer(ServerPlayer player) {
      super.startSeenByPlayer(player);
      if (!this.isDeadOrDying()) {
         this.bossEvent.addPlayer(player);
      }
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
   public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
      return false;
   }

   @Override
   protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putInt("phase", this.phase());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.setPhase(input.getIntOr("phase", 1));
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return this.phase() == 3 ? SoundEvents.RAVAGER_AMBIENT : SoundEvents.WITHER_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource source) {
      return this.phase() == 3 ? SoundEvents.RAVAGER_HURT : SoundEvents.WITHER_SKELETON_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.WITHER_DEATH;
   }

   @Override
   public float getVoicePitch() {
      return 0.5F;
   }

   @Override
   protected void playStepSound(BlockPos pos, BlockState state) {
      if (this.phase() == 3) {
         this.playSound(SoundEvents.RAVAGER_STEP, 1.0F, 0.6F);
      }
   }

   private class HuntGoal extends Goal {
      HuntGoal() {
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         return DeathEntity.this.phase() != 1 && DeathEntity.this.getTarget() != null;
      }

      @Override
      public boolean requiresUpdateEveryTick() {
         return true;
      }

      @Override
      public void tick() {
         DeathEntity d = DeathEntity.this;
         LivingEntity target = d.getTarget();
         if (target != null && d.action() == 0) {
            d.lookAtTarget(target);
            double dist = d.distanceTo(target);
            if (d.phase() == 3) {
               d.getNavigation().moveTo(target, 1.35);
            } else if (dist > 10.0) {
               d.getNavigation().moveTo(target, 1.0);
            } else if (dist < 6.0) {
               Vec3 away = d.position().add(d.position().subtract(target.position()).multiply(1.0, 0.0, 1.0).normalize().scale(4.0));
               d.getNavigation().moveTo(away.x, away.y, away.z, 0.85);
            } else {
               d.getNavigation().stop();
               d.getMoveControl().strafe(0.0F, d.tickCount / 60 % 2 == 0 ? 0.4F : -0.4F);
            }
         }
      }
   }
}
