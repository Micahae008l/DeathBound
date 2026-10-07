package com.deathbound.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record UnderworldBuilderFeature() implements Feature {
   public static final MapCodec<UnderworldBuilderFeature> CODEC = MapCodec.unit(new UnderworldBuilderFeature());

   @Override
   public MapCodec<UnderworldBuilderFeature> codec() {
      return CODEC;
   }

   @Override
   public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
      Build b = new Build(level, random, new ChunkPos(origin.getX() >> 4, origin.getZ() >> 4));

      for (Layout.Island is : Layout.ISLANDS) {
         Layout.island(b, is);
      }

      for (Layout.Bridge br : Layout.BRIDGES) {
         Layout.bridge(b, br);
      }

      for (Layout.Island is : Layout.ISLANDS) {
         Structures.build(b, is);
      }

      Decor.neglect(b);
      Settle.chunk(b);
      return true;
   }
}
