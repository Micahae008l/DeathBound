package com.deathbound.registry;

import com.deathbound.DeathBound;
import com.deathbound.world.UnderworldBuilderFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModWorldgen {
   public static void init() {
      Registry.register(BuiltInRegistries.FEATURE_TYPE, DeathBound.id("underworld_builder"), UnderworldBuilderFeature.CODEC);
   }

   private ModWorldgen() {
   }
}
