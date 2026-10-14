package com.deathbound.world;

import com.deathbound.entity.Gravebound;
import com.deathbound.registry.ModBlocks;
import com.deathbound.registry.ModEntities;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;

/**
 * The Mired: the dead who climbed out of the boats when the river stopped, and stuck fast as the mud dried. Each one
 * is a hand out of the riverbed (ModBlocks.MIRED_HAND, laid by River). Come near and the mud cracks around it; come
 * close and it takes you by the ankle and climbs out. Each hand only does it once.
 */
public final class Mire {
   static final double WARN = 7.0;
   static final double GRAB = 2.3;
   private static final Map<BlockPos, Long> WARNED = new HashMap<>();

   public static void init() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         if (server.getTickCount() % 4 != 0) {
            return;
         }

         ServerLevel level = server.getLevel(UnderworldTravel.UNDERWORLD);
         if (level != null) {
            for (ServerPlayer p : level.players()) {
               if (!p.isSpectator() && !p.isCreative() && p.isAlive() && River.near(p.getX(), p.getZ())) {
                  scan(level, p);
               }
            }
         }
      });
      ServerLifecycleEvents.SERVER_STOPPED.register(server -> WARNED.clear());
   }

   private static void scan(ServerLevel level, ServerPlayer p) {
      BlockPos at = p.blockPosition();
      int r = (int)Math.ceil(WARN);
      for (BlockPos q : BlockPos.betweenClosed(at.offset(-r, -3, -r), at.offset(r, 2, r))) {
         if (!level.getBlockState(q).is(ModBlocks.MIRED_HAND)) {
            continue;
         }

         double d = Math.hypot(q.getX() + 0.5 - p.getX(), q.getZ() + 0.5 - p.getZ());
         if (d <= GRAB) {
            grab(level, q.immutable(), p);
            return;
         }

         long now = level.getGameTime();
         Long last = WARNED.get(q);
         if (d <= WARN && (last == null || now - last > 200L)) {
            // the mud cracks: the only warning you get
            WARNED.put(q.immutable(), now);
            level.playSound(null, q, SoundEvents.MUD_BREAK, SoundSource.HOSTILE, 0.9F, 0.55F);
            level.playSound(null, q, SoundEvents.SKELETON_AMBIENT, SoundSource.HOSTILE, 0.35F, 0.5F);
            level.sendParticles(
               new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.ASHEN_SOIL.defaultBlockState()), q.getX() + 0.5, q.getY() + 0.1, q.getZ() + 0.5, 8, 0.3, 0.05, 0.3, 0.02
            );
         }
      }
   }

   /** It takes the player by the ankle and climbs out after them. Public for the debug command. */
   public static void grab(ServerLevel level, BlockPos hand, ServerPlayer p) {
      level.setBlock(hand, Blocks.AIR.defaultBlockState(), 3);
      WARNED.remove(hand);
      Gravebound mired = ModEntities.GRAVEBOUND.create(level, EntitySpawnReason.EVENT);
      if (mired == null) {
         return;
      }

      mired.setVariant(Gravebound.MIRED);
      float yaw = (float)(Mth.atan2(p.getZ() - hand.getZ() - 0.5, p.getX() - hand.getX() - 0.5) * 180.0F / (float)Math.PI) - 90.0F;
      mired.snapTo(hand.getX() + 0.5, hand.getY(), hand.getZ() + 0.5, yaw, 0.0F);
      mired.finalizeSpawn(level, level.getCurrentDifficultyAt(hand), EntitySpawnReason.EVENT, null);   // climbs out of the mud
      mired.setTarget(p);
      level.addFreshEntity(mired);
      p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 4, false, false, true));
      p.hurtServer(level, level.damageSources().mobAttack(mired), 2.0F);
      level.sendParticles(
         new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.ASHEN_SOIL.defaultBlockState()), hand.getX() + 0.5, hand.getY() + 0.2, hand.getZ() + 0.5, 36, 0.45, 0.2, 0.45, 0.12
      );
      level.playSound(null, hand, SoundEvents.MUD_BREAK, SoundSource.HOSTILE, 1.5F, 0.6F);
      level.playSound(null, hand, SoundEvents.SKELETON_HURT, SoundSource.HOSTILE, 1.0F, 0.5F);
   }

   private Mire() {
   }
}
