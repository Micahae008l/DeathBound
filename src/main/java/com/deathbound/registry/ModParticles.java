package com.deathbound.registry;

import com.deathbound.DeathBound;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModParticles {
   public static final SimpleParticleType SOUL_MOTE = Registry.register(
      BuiltInRegistries.PARTICLE_TYPE, DeathBound.id("soul_mote"), FabricParticleTypes.simple()
   );
   public static final SimpleParticleType SOUL_FLAME = Registry.register(
      BuiltInRegistries.PARTICLE_TYPE, DeathBound.id("soul_flame"), FabricParticleTypes.simple(true)
   );
   public static final SimpleParticleType SOUL_SWEEP = Registry.register(
      BuiltInRegistries.PARTICLE_TYPE, DeathBound.id("soul_sweep"), FabricParticleTypes.simple(true)
   );

   public static void init() {
   }

   private ModParticles() {
   }
}
