package com.deathbound.entity;

import com.deathbound.registry.ModSounds;
import com.deathbound.world.Dread;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Shade extends PathfinderMob {
   private ServerPlayer watching;
   private int life;

   public Shade(EntityType<? extends Shade> type, Level level) {
      super(type, level);
      this.setNoAi(true);
      this.setSilent(true);
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0);
   }

   public void watch(ServerPlayer player) {
      this.watching = player;
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level() instanceof ServerLevel level) {
         ServerPlayer p = this.watching;
         if (p != null && !p.isRemoved() && p.level() == level && ++this.life <= 600) {
            float yaw = (float)(Mth.atan2(p.getZ() - this.getZ(), p.getX() - this.getX()) * 57.2957763671875) - 90.0F;
            this.setYRot(yaw);
            this.setYHeadRot(yaw);
            this.yBodyRot = yaw;
            Vec3 to = this.getEyePosition().subtract(p.getEyePosition());
            boolean seen = p.getLookAngle().dot(to.normalize()) > 0.985 && p.hasLineOfSight(this);
            if (this.life > 20 && (seen || to.length() < 12.0)) {
               level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.2, this.getZ(), 18, 0.2, 0.7, 0.2, 0.01);
               Dread.whisperTo(p, ModSounds.WHISPER, this.position(), 0.7F, 0.55F, this.random);
               this.discard();
            }
         } else {
            this.discard();
         }
      }
   }

   @Override
   public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
      return false;
   }

   @Override
   public boolean shouldBeSaved() {
      return false;
   }

   @Override
   public boolean isPickable() {
      return false;
   }

   @Override
   public boolean isPushable() {
      return false;
   }
}
