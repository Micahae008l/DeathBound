package com.deathbound.entity;

import com.deathbound.registry.ModParticles;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class LanternWisp extends PathfinderMob {
   private @Nullable UUID owner;
   private @Nullable BlockPos lit;
   private int alone;
   private int warned;

   public LanternWisp(EntityType<? extends LanternWisp> type, Level level) {
      super(type, level);
      this.setNoGravity(true);
      this.noPhysics = true;
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.0);
   }

   public void setOwner(Player p) {
      this.owner = p.getUUID();
   }

   public boolean ownedBy(Player p) {
      return p.getUUID().equals(this.owner);
   }

   @Override
   public void tick() {
      this.noPhysics = true;
      super.tick();
      if (this.level() instanceof ServerLevel level) {
         Player p = this.owner == null ? null : level.getPlayerByUUID(this.owner);
         if (p != null && p.level() == level && p.isAlive()) {
            this.alone = 0;
            float yaw = p.getYRot() * 0.017453292F;
            Vec3 right = new Vec3(-Mth.cos(yaw), 0.0, -Mth.sin(yaw));
            Vec3 back = new Vec3(Mth.sin(yaw), 0.0, -Mth.cos(yaw));
            Vec3 want = p.position().add(right.scale(-0.75)).add(back.scale(0.55)).add(0.0, 1.75 + Mth.sin(this.tickCount * 0.12F) * 0.08, 0.0);
            Vec3 at = this.position();
            if (at.distanceToSqr(want) > 400.0) {
               this.setPos(want);
            } else {
               this.setPos(at.lerp(want, 0.22));
            }

            this.setYRot(p.getYRot());
            this.yBodyRot = p.getYRot();
            this.setYHeadRot(p.getYRot());
            BlockPos here = this.blockPosition();
            if (!here.equals(this.lit)) {
               this.unlight(level);
               if (level.getBlockState(here).isAir()) {
                  level.setBlock(here, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 11), 3);
                  this.lit = here;
               }
            }

            if (this.tickCount % 10 == 0) {
               boolean near = !level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0), e -> e instanceof Enemy && e.isAlive())
                  .isEmpty();
               if (near) {
                  level.sendParticles(new DustParticleOptions(13643850, 0.9F), this.getX(), this.getY() + 0.25, this.getZ(), 4, 0.12, 0.12, 0.12, 0.0);
                  if (this.warned <= 0) {
                     level.playSound(null, this.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.NEUTRAL, 0.8F, 0.5F);
                     this.warned = 8;
                  }
               }

               this.warned--;
            }
         } else {
            if (++this.alone > 100) {
               this.discard();
            }
         }
      } else {
         if (this.random.nextInt(6) == 0) {
            this.level().addParticle(ModParticles.SOUL_MOTE, this.getRandomX(0.3), this.getY() + 0.3, this.getRandomZ(0.3), 0.0, 0.01, 0.0);
         }
      }
   }

   private void unlight(ServerLevel level) {
      if (this.lit != null && level.getBlockState(this.lit).is(Blocks.LIGHT)) {
         level.setBlock(this.lit, Blocks.AIR.defaultBlockState(), 3);
      }

      this.lit = null;
   }

   @Override
   public void remove(RemovalReason reason) {
      if (this.level() instanceof ServerLevel level) {
         this.unlight(level);
      }

      super.remove(reason);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      if (this.owner != null) {
         output.putString("owner", this.owner.toString());
      }
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      input.getString("owner").ifPresent(s -> this.owner = UUID.fromString(s));
   }

   @Override
   public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
      return false;
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   public boolean isPickable() {
      return false;
   }

   @Override
   public boolean removeWhenFarAway(double distSqr) {
      return false;
   }
}
