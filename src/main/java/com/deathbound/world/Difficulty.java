package com.deathbound.world;

import com.deathbound.entity.Hazards;
import com.deathbound.npc.Soulforge;
import com.deathbound.registry.ModEffects;
import com.deathbound.registry.ModParticles;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class Difficulty {
   static final double FULL_ARMOR = 15.0;
   static final double NAKED_SCALE = 0.5;

   public static float scale(Player player, DamageSource source, float damage) {
      if (player.hasEffect(ModEffects.MARKED) && damage > 0.0F) {
         player.removeEffect(ModEffects.MARKED);
         damage *= 2.0F;
         if (player.level() instanceof ServerLevel level) {
            level.sendParticles(ModParticles.SOUL_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 24, 0.3, 0.5, 0.3, 0.08);
            level.playSound(null, player.blockPosition(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.6F, 1.6F);
         }
      }

      if (!fromBelow(source)) {
         return damage;
      }

      double armor = player.getAttributeValue(Attributes.ARMOR);
      double gear = 0.5 + 0.5 * Mth.clamp(armor / 15.0, 0.0, 1.0);
      double forged = 1.0 - Soulforge.ward(player);
      double warded = player.hasEffect(ModEffects.WARDING) ? 0.8 : 1.0;
      return (float)(damage * gear * forged * warded);
   }

   private static boolean fromBelow(DamageSource source) {
      if (source.is(Hazards.SOUL_REND) || source.is(Hazards.SOUL_BOLT)) {
         return true;
      }

      Entity e = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
      return e != null && BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace().equals("deathbound");
   }

   private Difficulty() {
   }
}
