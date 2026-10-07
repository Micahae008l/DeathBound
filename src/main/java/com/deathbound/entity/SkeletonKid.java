package com.deathbound.entity;

import com.deathbound.npc.Talk;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class SkeletonKid extends PathfinderMob {
   private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(SkeletonKid.class, EntityDataSerializers.INT);

   public SkeletonKid(EntityType<? extends SkeletonKid> type, Level level) {
      super(type, level);
      if (!level.isClientSide()) {
         this.entityData.set(VARIANT, this.random.nextInt(3));
      }
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.3);
   }

   public int variant() {
      return this.entityData.get(VARIANT);
   }

   @Override
   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(VARIANT, 0);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putInt("variant", this.variant());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.entityData.set(VARIANT, input.getIntOr("variant", 0));
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new SkeletonKid.Tag(this));
      this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.15));
      this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (hand != InteractionHand.MAIN_HAND) {
         return InteractionResult.PASS;
      }

      if (player instanceof ServerPlayer sp && this.level() instanceof ServerLevel level) {
         this.getNavigation().stop();
         this.getLookControl().setLookAt(player);
         this.playSound(SoundEvents.SKELETON_AMBIENT, 0.6F, 1.8F);
         Talk.open(sp, "kid");
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
      if (source.getEntity() instanceof Player) {
         this.playSound(SoundEvents.SKELETON_AMBIENT, 0.6F, 2.0F);
      }

      return false;
   }

   @Override
   protected @Nullable SoundEvent getAmbientSound() {
      return this.random.nextInt(3) == 0 ? SoundEvents.SKELETON_AMBIENT : null;
   }

   @Override
   public float getVoicePitch() {
      return 1.9F + this.random.nextFloat() * 0.2F;
   }

   @Override
   protected @Nullable SoundEvent getHurtSound(DamageSource source) {
      return null;
   }

   @Override
   protected @Nullable SoundEvent getDeathSound() {
      return null;
   }

   @Override
   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.SKELETON_STEP, 0.15F, 1.8F);
   }

   @Override
   public boolean removeWhenFarAway(double distSqr) {
      return false;
   }

   @Override
   public boolean requiresCustomPersistence() {
      return true;
   }

   static class Tag extends Goal {
      final SkeletonKid kid;
      @Nullable SkeletonKid it;
      int left;

      Tag(SkeletonKid kid) {
         this.kid = kid;
         this.setFlags(EnumSet.of(Flag.MOVE));
      }

      @Override
      public boolean canUse() {
         if (this.kid.random.nextInt(80) != 0) {
            return false;
         }

         List<SkeletonKid> others = this.kid.level().getEntitiesOfClass(SkeletonKid.class, this.kid.getBoundingBox().inflate(14.0), k -> k != this.kid);
         this.it = others.isEmpty() ? null : others.get(this.kid.random.nextInt(others.size()));
         return this.it != null;
      }

      @Override
      public void start() {
         this.left = 60 + this.kid.random.nextInt(80);
      }

      @Override
      public boolean canContinueToUse() {
         return this.it != null
            && this.it.isAlive()
            && --this.left > 0
            && this.kid.distanceToSqr(this.it) > 1.5
            && (!this.kid.hasHome() || this.kid.isWithinHome(this.it.blockPosition()));
      }

      @Override
      public void tick() {
         if (this.it != null && this.kid.tickCount % 5 == 0) {
            this.kid.getNavigation().moveTo(this.it, 1.55);
         }
      }

      @Override
      public void stop() {
         if (this.it != null && this.kid.distanceToSqr(this.it) <= 2.5) {
            this.kid.playSound(SoundEvents.SKELETON_AMBIENT, 0.5F, 1.9F);
         }

         this.it = null;
         this.kid.getNavigation().stop();
      }
   }
}
