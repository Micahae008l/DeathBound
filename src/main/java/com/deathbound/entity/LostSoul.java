package com.deathbound.entity;

import com.deathbound.npc.Rewards;
import com.deathbound.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import com.deathbound.registry.ModParticles;
import com.deathbound.registry.ModSounds;
import com.deathbound.world.Layout;
import com.deathbound.world.QuestEvents;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LostSoul extends PathfinderMob {
   public static final int WALKING = 0;
   public static final int WAITING = 1;
   public static final int PASSED = 2;
   public static final int CONDEMNED = 3;
   public static final int LINGERING = 4;
   public static final String[] PLACES = new String[]{"arrival", "crossing", "ghostwood", "village", "crypt", "spire", "watch", "gate", "citadel"};
   public static final int PLACE_LINES = 3;
   private BlockPos home;
   private int place = -1;
   private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(LostSoul.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> FADE = SynchedEntityData.defineId(LostSoul.class, EntityDataSerializers.INT);
   public static final int FADE_TICKS = 24;
   private static final EntityDataAccessor<Integer> TINT = SynchedEntityData.defineId(LostSoul.class, EntityDataSerializers.INT);
   public static final int LINES = 24;
   /** Chance that a Lost Soul gives a Soul the first time it is spoken to (rolled once per ghost). */
   public static final float SOUL_GIFT_CHANCE = 0.25F;
   private boolean soulRolled;

   public void linger(BlockPos home, int place) {
      this.home = home;
      this.place = place;
      this.entityData.set(STATE, 4);
   }

   public boolean lingering() {
      return this.soulState() == 4;
   }

   public int tint() {
      return this.entityData.get(TINT);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putInt("tint", this.tint());
      output.putBoolean("soul_rolled", this.soulRolled);
      if (this.home != null) {
         output.store("home", BlockPos.CODEC, this.home);
         output.putInt("place", this.place);
         output.putInt("state", this.soulState());
      }
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      input.getInt("tint").ifPresent(t -> this.entityData.set(TINT, t));
      this.soulRolled = input.getBooleanOr("soul_rolled", false);
      input.<BlockPos>read("home", BlockPos.CODEC).ifPresent(h -> this.linger(h, input.getIntOr("place", 0)));
   }

   public LostSoul(EntityType<? extends LostSoul> type, Level level) {
      super(type, level);
      if (!level.isClientSide()) {
         this.entityData.set(TINT, this.random.nextInt(6));
         if (this.random.nextFloat() < 0.22F) {
            this.getAttribute(Attributes.SCALE).setBaseValue(0.62);
         }
      }

      this.noPhysics = true;
      this.setNoGravity(true);
   }

   public static Builder createAttributes() {
      return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.1);
   }

   @Override
   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TINT, 0);
      builder.define(STATE, 0);
      builder.define(FADE, 0);
   }

   public int soulState() {
      return this.entityData.get(STATE);
   }

   public int fade() {
      return this.entityData.get(FADE);
   }

   public boolean awaitingJudgement() {
      return this.soulState() == 1;
   }

   public void judge(boolean pass) {
      this.entityData.set(STATE, pass ? 2 : 3);
      if (!pass && this.level() instanceof ServerLevel level) {
         level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY() + 1.2, this.getZ(), 30, 0.3, 0.6, 0.3, 0.06);
         this.playSound(SoundEvents.GHAST_HURT, 0.9F, 0.5F);
      } else {
         this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 0.6F);
         if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.0, this.getZ(), 40, 0.25, 1.5, 0.25, 0.03);
         }
      }
   }

   @Override
   public void tick() {
      this.noPhysics = true;
      super.tick();
      this.setNoGravity(true);
      if (this.level().isClientSide()) {
         if (this.random.nextInt(4) == 0) {
            this.level()
               .addParticle(
                  ModParticles.SOUL_MOTE, this.getRandomX(0.5), this.getY() + 0.2 + this.random.nextDouble() * 1.6, this.getRandomZ(0.5), 0.0, 0.01, 0.0
               );
         }
      } else {
         int state = this.soulState();
         if (state == 4) {
            this.drift();
         } else {
            int fade = this.fade();
            boolean atDoor = this.getZ() <= Layout.DOOR_Z + 1.5;
            if (state == 3 || atDoor || fade > 0) {
               this.entityData.set(FADE, fade + (state == 3 ? 2 : 1));
               this.setDeltaMovement(Vec3.ZERO);
               if (atDoor && state != 3) {
                  this.move(-0.03);
               }

               if (fade >= 24) {
                  this.discard();
               }
            } else if (state == 1) {
               this.setDeltaMovement(Vec3.ZERO);
               if (!this.guardWatching()) {
                  this.entityData.set(STATE, 2);
               }

               this.hover();
            } else {
               List<LostSoul> ahead = this.level()
                  .getEntitiesOfClass(
                     LostSoul.class,
                     new AABB(this.getX() - 1.2, this.getY() - 2.0, this.getZ() - 2.0, this.getX() + 1.2, this.getY() + 2.0, this.getZ() - 0.1),
                     s -> s != this && s.fade() == 0
                  );
               boolean nearGuard = this.position().distanceTo(Vec3.atBottomCenterOf(Layout.GUARD_POST)) < 3.2;
               if (state == 0 && nearGuard && this.guardWatching()) {
                  this.entityData.set(STATE, 1);
               } else {
                  if (ahead.isEmpty()) {
                     this.move(-0.055);
                  }

                  this.hover();
               }
            }
         }
      }
   }

   private boolean guardWatching() {
      return !this.level()
         .getEntitiesOfClass(DeathsGuard.class, new AABB(Layout.GUARD_POST).inflate(10.0, 6.0, 10.0), g -> g.isAlive() && g.getTarget() == null)
         .isEmpty();
   }

   private void drift() {
      if (this.home == null) {
         this.discard();
      } else {
         double t = this.tickCount * 0.006 + this.getId() * 1.7;
         double tx = this.home.getX() + 0.5 + Math.sin(t) * 3.5;
         double tz = this.home.getZ() + 0.5 + Math.cos(t * 1.3) * 3.5;
         Vec3 step = new Vec3(tx - this.getX(), 0.0, tz - this.getZ());
         if (step.lengthSqr() > 4.0E-4) {
            step = step.normalize().scale(Math.min(0.03, step.length()));
         }

         this.setPos(this.getX() + step.x, this.getY(), this.getZ() + step.z);
         Player watcher = this.level().getNearestPlayer(this, 8.0);
         float yaw = watcher != null
            ? (float)Math.toDegrees(Math.atan2(watcher.getZ() - this.getZ(), watcher.getX() - this.getX())) - 90.0F
            : (float)Math.toDegrees(Math.atan2(step.z, step.x)) - 90.0F;
         this.setYRot(yaw);
         this.yBodyRot = yaw;
         this.yHeadRot = yaw;
         this.hover();
      }
   }

   private void move(double dz) {
      double dx = (0.5 - this.getX()) * 0.01;
      this.setPos(this.getX() + dx, this.getY(), this.getZ() + dz);
      this.setYRot(180.0F);
      this.yBodyRot = 180.0F;
      this.yHeadRot = 180.0F;
   }

   private void hover() {
      MutableBlockPos p = BlockPos.containing(this.getX(), this.getY() + 1.5, this.getZ()).mutable();

      for (int i = 0; i < 6 && this.level().getBlockState(p).getCollisionShape(this.level(), p).isEmpty(); i++) {
         p.move(0, -1, 0);
      }

      double ground = p.getY() + this.level().getBlockState(p).getCollisionShape(this.level(), p).max(Axis.Y);
      double target = ground + 0.15 + Math.sin(this.tickCount * 0.08 + this.getId()) * 0.08;
      this.setPos(this.getX(), this.getY() + (target - this.getY()) * 0.25, this.getZ());
   }

   @Override
   public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
      return !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   public boolean canBeCollidedWith(Entity other) {
      return false;
   }

   @Override
   public boolean isPickable() {
      return false;
   }

   @Override
   protected void doPush(Entity entity) {
   }

   @Override
   public boolean removeWhenFarAway(double distSqr) {
      return true;
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return ModSounds.WHISPER;
   }

   @Override
   protected InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (hand == InteractionHand.MAIN_HAND && !this.level().isClientSide()) {
         String key = this.lingering() && this.place >= 0
            ? "deathbound.lingering." + PLACES[Math.floorMod(this.place, PLACES.length)] + "." + Math.floorMod(this.getUUID().hashCode(), 3)
            : "deathbound.lost_soul.line." + Math.floorMod(this.getUUID().hashCode(), 24);
         Component clue = player instanceof ServerPlayer sp ? QuestEvents.miraClue(sp, this) : null;
         player.sendSystemMessage(
            Component.translatable("deathbound.lost_soul.says", clue != null ? clue : Component.translatable(key)).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
         );
         this.playSound(ModSounds.WHISPER, 0.9F, 0.8F + this.random.nextFloat() * 0.3F);
         this.getLookControl().setLookAt(player);
         if (!this.soulRolled && player instanceof ServerPlayer sp && this.soulState() != 3) {
            this.soulRolled = true;
            if (this.random.nextFloat() < SOUL_GIFT_CHANCE) {
               this.giveSoul(sp);
            }
         }
      }

      return InteractionResult.SUCCESS;
   }

   /** A small mercy: the ghost presses a Soul into the player's hand. */
   private void giveSoul(ServerPlayer player) {
      ServerLevel level = (ServerLevel)this.level();
      Vec3 from = this.position().add(0.0, this.getBbHeight() * 0.6, 0.0);
      Vec3 to = player.position().add(0.0, 1.0, 0.0);
      for (int i = 0; i <= 8; i++) {
         Vec3 p = from.lerp(to, i / 8.0);
         level.sendParticles(ModParticles.SOUL_FLAME, p.x, p.y, p.z, 1, 0.04, 0.04, 0.04, 0.0);
      }
      level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.NEUTRAL, 0.9F, 1.2F);
      player.sendSystemMessage(Component.translatable("deathbound.lost_soul.gift").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
      Rewards.give(player, Component.translatable("rewards.deathbound.from", this.getName()), new ItemStack(ModItems.SOUL));
   }

   @Override
   public int getAmbientSoundInterval() {
      return 240;
   }

   @Override
   protected float getSoundVolume() {
      return 0.35F;
   }
}
