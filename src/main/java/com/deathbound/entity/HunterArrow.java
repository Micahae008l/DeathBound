package com.deathbound.entity;

import com.deathbound.registry.ModEntities;
import com.deathbound.registry.ModParticles;
import com.deathbound.world.Layout;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class HunterArrow extends AbstractArrow {
   private @Nullable MobEffectInstance onHit;
   private static final EntityDataAccessor<Boolean> RIVEN = SynchedEntityData.defineId(HunterArrow.class, EntityDataSerializers.BOOLEAN);
   private float rivenDamage;
   private int rivenAge;
   private int broken;
   private final Set<Integer> struck = new HashSet<>();
   private boolean seeking;
   private int quarry = -1;

   public HunterArrow(EntityType<? extends HunterArrow> type, Level level) {
      super(type, level);
   }

   public HunterArrow(Level level, LivingEntity owner, ItemStack pickup, @Nullable ItemStack weapon) {
      super(ModEntities.HUNTER_ARROW, owner, level, pickup, weapon);
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(RIVEN, false);
   }

   public void riven(float damage) {
      this.entityData.set(RIVEN, true);
      this.rivenDamage = damage;
      this.setNoGravity(true);
   }

   public void seek() {
      this.seeking = true;
   }

   public boolean isSeeking() {
      return this.seeking;
   }

   public boolean isRiven() {
      return this.entityData.get(RIVEN);
   }

   @Override
   public void tick() {
      if (this.isRiven()) {
         this.rivenTick();
      } else {
         super.tick();
         if (this.seeking && !this.isInGround() && this.tickCount > 2 && this.level() instanceof ServerLevel level) {
            this.steer(level);
         }

         if (this.level().isClientSide() && this.getDeltaMovement().lengthSqr() > 0.01) {
            this.level().addParticle(ParticleTypes.WITCH, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
         }
      }
   }

   private void steer(ServerLevel level) {
      Vec3 v = this.getDeltaMovement();
      LivingEntity q = this.quarry >= 0 && level.getEntity(this.quarry) instanceof LivingEntity l && l.isAlive() ? l : null;
      if (q == null) {
         q = level.getEntitiesOfClass(
               LivingEntity.class,
               this.getBoundingBox().inflate(18.0),
               e -> e instanceof Enemy && e.isAlive() && e != this.getOwner() && e.position().subtract(this.position()).dot(v) > 0.0
            )
            .stream()
            .min(Comparator.comparingDouble(this::distanceToSqr))
            .orElse(null);
         this.quarry = q == null ? -1 : q.getId();
      }

      if (q != null) {
         Vec3 want = q.getBoundingBox().getCenter().subtract(this.position()).normalize();
         this.setDeltaMovement(v.normalize().lerp(want, 0.22).normalize().scale(Math.max(v.length(), 1.2)));
         this.needsSync = true;
      }

      level.sendParticles(ModParticles.SOUL_FLAME, this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
      if (this.tickCount > 100) {
         this.seeking = false;
      }
   }

   private void rivenTick() {
      Vec3 from = this.position();
      Vec3 v = this.getDeltaMovement();
      Vec3 to = from.add(v);
      this.setOldPosAndRot();
      this.setPos(to);
      if (this.level() instanceof ServerLevel level) {
         int var12 = Math.max(1, (int)Math.ceil(v.length() / 0.5));

         for (int i = 1; i <= var12; i++) {
            Vec3 p = from.lerp(to, (double)i / var12);
            level.sendParticles(ModParticles.SOUL_FLAME, p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.01);
            if (i % 2 == 0) {
               level.sendParticles(ModParticles.SOUL_MOTE, p.x, p.y, p.z, 1, 0.1, 0.1, 0.1, 0.0);
            }

            BlockPos c = BlockPos.containing(p);

            for (BlockPos b : BlockPos.betweenClosed(c.offset(-1, -1, -1), c.offset(1, 1, 1))) {
               if (this.broken < 60 && Vec3.atCenterOf(b).distanceToSqr(p) < 1.1 && breakable(level, b)) {
                  level.destroyBlock(b, false, this);
                  if (++this.broken % 4 == 0) {
                     level.sendParticles(ParticleTypes.EXPLOSION, b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0);
                  }
               }
            }

            for (LivingEntity e : level.getEntitiesOfClass(
               LivingEntity.class, new AABB(p, p).inflate(0.9), ex -> ex != this.getOwner() && ex.isAlive() && !this.struck.contains(ex.getId())
            )) {
               this.struck.add(e.getId());
               e.hurtServer(level, this.damageSources().arrow(this, this.getOwner()), this.rivenDamage);
               Vec3 push = v.normalize();
               e.push(push.x * 1.6, 0.45, push.z * 1.6);
               e.needsSync = true;
               level.playSound(null, e.blockPosition(), SoundEvents.TRIDENT_HIT, SoundSource.HOSTILE, 1.5F, 0.6F);
            }
         }

         if (++this.rivenAge > 24) {
            this.discard();
         }
      }
   }

   private static boolean breakable(ServerLevel level, BlockPos b) {
      BlockState s = level.getBlockState(b);
      Layout.Island h = Layout.HOLLOW;
      double dx = b.getX() - h.x();
      double dz = b.getZ() - h.z();
      if (!s.isAir()
         && s.getFluidState().isEmpty()
         && !s.hasBlockEntity()
         && b.getY() >= h.top()
         && !(dx * dx + dz * dz > (h.radius() + 2.0) * (h.radius() + 2.0))) {
         float hard = s.getDestroySpeed(level, b);
         return hard >= 0.0F && hard < 50.0F;
      } else {
         return false;
      }
   }

   @Override
   protected void doPostHurtEffects(LivingEntity target) {
      super.doPostHurtEffects(target);
      if (this.onHit != null) {
         target.addEffect(new MobEffectInstance(this.onHit), this.getEffectSource());
      }

      if (this.getOwner() instanceof Player) {
         target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120), this.getEffectSource());
      }
   }

   public void onHit(MobEffectInstance effect) {
      this.onHit = effect;
   }

   @Override
   protected ItemStack getDefaultPickupItem() {
      return new ItemStack(Items.ARROW);
   }

   public static HunterArrow shotBy(Level level, LivingEntity owner) {
      HunterArrow arrow = new HunterArrow(ModEntities.HUNTER_ARROW, level);
      arrow.setOwner(owner);
      arrow.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
      arrow.pickup = Pickup.DISALLOWED;
      return arrow;
   }
}
