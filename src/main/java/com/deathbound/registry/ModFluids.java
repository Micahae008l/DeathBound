package com.deathbound.registry;

import com.deathbound.DeathBound;
import com.deathbound.block.SoulwaterFluid;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;

public final class ModFluids {
   public static final FlowingFluid SOULWATER = Registry.register(BuiltInRegistries.FLUID, DeathBound.id("soulwater"), new SoulwaterFluid.Source());
   public static final FlowingFluid FLOWING_SOULWATER = Registry.register(
      BuiltInRegistries.FLUID, DeathBound.id("flowing_soulwater"), new SoulwaterFluid.Flowing()
   );

   public static void init() {
   }

   private ModFluids() {
   }
}
