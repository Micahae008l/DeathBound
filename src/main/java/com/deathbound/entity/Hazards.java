package com.deathbound.entity;

import com.deathbound.DeathBound;
import com.deathbound.registry.ModNet;
import com.deathbound.registry.ModParticles;
import com.deathbound.registry.ModSounds;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class Hazards {
   public static final ResourceKey<DamageType> SOUL_REND = ResourceKey.create(Registries.DAMAGE_TYPE, DeathBound.id("soul_rend"));
   private static final List<Hazards.Eruption> ERUPTIONS = new ArrayList<>();
   public static final ResourceKey<DamageType> CRUSH = ResourceKey.create(Registries.DAMAGE_TYPE, DeathBound.id("crush"));
   /** Soul projectiles (Soul Wisp bolts, the Death King's orbs). Unlike Soul Rend this one can be blocked by shields. */
   public static final ResourceKey<DamageType> SOUL_BOLT = ResourceKey.create(Registries.DAMAGE_TYPE, DeathBound.id("soul_bolt"));

   public static void dread(ServerLevel level, Entity source, double radius, int ticks) {
      MobEffectUtil.addEffectToPlayersAround(
         level, source, source.position(), radius, new MobEffectInstance(MobEffects.DARKNESS, ticks, 0, false, false), ticks / 2
      );
      level.playSound(null, source.blockPosition(), ModSounds.HEARTBEAT, SoundSource.HOSTILE, 3.0F, 0.8F);
   }

   /**
    * The dead serve the Death King: Underworld monsters are on his side. His area attacks pass through them and
    * they never damage him, so they don't end up fighting him (or each other) mid-boss-fight.
    */
   public static boolean sameSide(Entity a, Entity b) {
      return a != null && b != null && a != b && underworldMonster(a) && underworldMonster(b);
   }

   private static boolean underworldMonster(Entity e) {
      return e instanceof net.minecraft.world.entity.Mob && e instanceof net.minecraft.world.entity.monster.Enemy
         && net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace().equals("deathbound");
   }

   public static DamageSource crush(ServerLevel level, LivingEntity owner) {
      return level.damageSources().source(CRUSH, owner);
   }

   public static DamageSource soulRend(ServerLevel level, @Nullable LivingEntity owner) {
      return level.damageSources().source(SOUL_REND, owner);
   }

   public static void eruption(ServerLevel level, Vec3 pos, int fuse, float damage, float radius, @Nullable LivingEntity owner) {
      ERUPTIONS.add(new Hazards.Eruption(level, groundAt(level, pos), fuse, damage, radius, owner, new int[1]));
      level.playSound(null, BlockPos.containing(pos), SoundEvents.SOUL_SAND_BREAK, SoundSource.HOSTILE, 1.0F, 0.5F);
   }

   static Vec3 groundAt(ServerLevel level, Vec3 pos) {
      MutableBlockPos p = BlockPos.containing(pos).mutable();

      for (int i = 0; i < 8 && level.getBlockState(p.below()).isAir(); i++) {
         p.move(0, -1, 0);
      }

      for (int i = 0; i < 4 && !level.getBlockState(p).isAir(); i++) {
         p.move(0, 1, 0);
      }

      return new Vec3(pos.x, p.getY(), pos.z);
   }

   public static void init() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         Iterator<Hazards.Eruption> it = ERUPTIONS.iterator();

         while (it.hasNext()) {
            Hazards.Eruption e = it.next();
            int age = ++e.age[0];
            if (age < e.fuse) {
               if (age % 3 == 0) {
                  ring(e.level, e.pos.add(0.0, 0.1, 0.0), e.radius, (int)(e.radius * 10.0F));
               }
            } else {
               it.remove();
               burst(e);
            }
         }
      });
   }

   private static void burst(Hazards.Eruption e) {
      ServerLevel level = e.level;
      Vec3 p = e.pos;

      for (int y = 0; y < 7; y++) {
         level.sendParticles(ModParticles.SOUL_FLAME, p.x, p.y + y * 0.8, p.z, 10, e.radius * 0.35, 0.3, e.radius * 0.35, 0.03);
      }

      level.sendParticles(ParticleTypes.SCULK_SOUL, p.x, p.y + 0.5, p.z, 12, e.radius * 0.4, 0.4, e.radius * 0.4, 0.08);
      level.playSound(null, BlockPos.containing(p), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.4F, 0.5F);
      level.playSound(null, BlockPos.containing(p), SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 2.0F, 0.6F);
      AABB box = new AABB(p.x - e.radius, p.y - 0.5, p.z - e.radius, p.x + e.radius, p.y + 5.0, p.z + e.radius);

      for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box, v -> v.isAlive() && v != e.owner && !(v instanceof SoulAnchor))) {
         if ((e.owner == null || !victim.isAlliedTo(e.owner) && !sameSide(victim, e.owner)) && victim.hurtServer(level, soulRend(level, e.owner), e.damage)) {
            victim.push(0.0, 0.75, 0.0);
            victim.needsSync = true;
         }
      }
   }

   public static void shockwave(ServerLevel level, LivingEntity owner, Vec3 center, float radius, float damage, float lift) {
      BlockState ground = level.getBlockState(BlockPos.containing(center).below());
      ring(level, center.add(0.0, 0.2, 0.0), radius * 0.5F, 24);
      ring(level, center.add(0.0, 0.2, 0.0), radius, 40);
      if (!ground.isAir()) {
         level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), center.x, center.y + 0.2, center.z, 60, radius * 0.4, 0.2, radius * 0.4, 0.2);
      }

      level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5, center.z, 2, 0.5, 0.2, 0.5, 0.0);
      level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.6F, 0.55F);
      level.playSound(null, BlockPos.containing(center), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.8F, 0.4F);
      ModNet.shake(level, center, radius * 4.0F, 2.2F, 14);

      for (LivingEntity v : level.getEntitiesOfClass(
         LivingEntity.class, new AABB(center, center).inflate(radius, 3.0, radius), vx -> vx != owner && vx.isAlive()
      )) {
         if (!v.isAlliedTo(owner) && !sameSide(v, owner) && !(v instanceof SoulAnchor) && !(v instanceof LostSoul)) {
            double d = v.position().distanceTo(center);
            if (!(d > radius)) {
               float scale = (float)(1.0 - d / radius * 0.6);
               if (v.hurtServer(level, crush(level, owner), damage * scale)) {
                  Vec3 away = v.position().subtract(center).multiply(1.0, 0.0, 1.0).normalize().scale(0.9 * scale);
                  v.push(away.x, lift * scale, away.z);
                  v.needsSync = true;
               }
            }
         }
      }
   }

   public static int arc(ServerLevel level, LivingEntity owner, float radius, float halfAngleDeg, float damage, float knockback) {
      Vec3 look = Vec3.directionFromRotation(0.0F, owner.getYRot());
      int hits = 0;

      for (LivingEntity v : level.getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(radius, 1.5, radius), vx -> vx != owner && vx.isAlive())) {
         if (!v.isAlliedTo(owner) && !sameSide(v, owner) && !(v instanceof SoulAnchor) && !(v instanceof LostSoul)) {
            Vec3 to = v.position().subtract(owner.position()).multiply(1.0, 0.0, 1.0);
            double dist = to.length();
            if (!(dist > radius + v.getBbWidth() * 0.5)) {
               double angle = Math.toDegrees(Math.acos(Mth.clamp(to.normalize().dot(look), -1.0, 1.0)));
               if ((!(dist > 1.2) || !(angle > halfAngleDeg)) && !sameSide(v, owner) && v.hurtServer(level, level.damageSources().mobAttack(owner), damage)) {
                  Vec3 kb = to.normalize().scale(knockback);
                  v.push(kb.x, 0.35, kb.z);
                  v.needsSync = true;
                  hits++;
               }
            }
         }
      }

      return hits;
   }

   public static void ring(ServerLevel level, Vec3 c, float radius, int points) {
      for (int i = 0; i < points; i++) {
         double a = i * 3.141592653589793 * 2.0 / points;
         level.sendParticles(ModParticles.SOUL_FLAME, c.x + Math.cos(a) * radius, c.y, c.z + Math.sin(a) * radius, 1, 0.0, 0.02, 0.0, 0.0);
      }
   }

   private Hazards() {
   }

   private record Eruption(ServerLevel level, Vec3 pos, int fuse, float damage, float radius, @Nullable LivingEntity owner, int[] age) {
   }
}
