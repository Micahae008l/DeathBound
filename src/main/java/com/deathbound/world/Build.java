package com.deathbound.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

final class Build {
   final WorldGenLevel level;
   final RandomSource random;
   final int x0;
   final int z0;
   final int x1;
   final int z1;
   private final MutableBlockPos cursor = new MutableBlockPos();

   Build(WorldGenLevel level, RandomSource random, ChunkPos chunk) {
      this.level = level;
      this.random = random;
      this.x0 = chunk.getMinBlockX();
      this.z0 = chunk.getMinBlockZ();
      this.x1 = chunk.getMaxBlockX();
      this.z1 = chunk.getMaxBlockZ();
   }

   boolean in(int x, int z) {
      return x >= this.x0 && x <= this.x1 && z >= this.z0 && z <= this.z1;
   }

   boolean overlaps(int ax, int az, int bx, int bz) {
      return Math.max(ax, bx) >= this.x0 && Math.min(ax, bx) <= this.x1 && Math.max(az, bz) >= this.z0 && Math.min(az, bz) <= this.z1;
   }

   void set(int x, int y, int z, BlockState state) {
      if (this.in(x, z)) {
         this.level.setBlock(this.cursor.set(x, y, z), state, 2);
      }
   }

   void setLinked(int x, int y, int z, BlockState state) {
      if (this.in(x, z)) {
         this.level.setBlock(this.cursor.set(x, y, z), state, 2);
         this.level.getChunk(this.cursor).markPosForPostProcessing(this.cursor.immutable());
      }
   }

   BlockState get(int x, int y, int z) {
      return this.in(x, z) ? this.level.getBlockState(this.cursor.set(x, y, z)) : Blocks.AIR.defaultBlockState();
   }

   boolean air(int x, int y, int z) {
      return this.in(x, z) && this.get(x, y, z).isAir();
   }

   void fill(int ax, int ay, int az, int bx, int by, int bz, BlockState state) {
      this.fill(ax, ay, az, bx, by, bz, (x, y, z) -> state);
   }

   void fill(int ax, int ay, int az, int bx, int by, int bz, Build.Pick pick) {
      int minX = Math.max(Math.min(ax, bx), this.x0);
      int maxX = Math.min(Math.max(ax, bx), this.x1);
      int minZ = Math.max(Math.min(az, bz), this.z0);
      int maxZ = Math.min(Math.max(az, bz), this.z1);
      int minY = Math.min(ay, by);
      int maxY = Math.max(ay, by);

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
               BlockState s = pick.at(x, y, z);
               if (s != null) {
                  this.level.setBlock(this.cursor.set(x, y, z), s, 2);
               }
            }
         }
      }
   }

   void shell(int ax, int ay, int az, int bx, int by, int bz, Build.Pick pick) {
      int minX = Math.min(ax, bx);
      int maxX = Math.max(ax, bx);
      int minZ = Math.min(az, bz);
      int maxZ = Math.max(az, bz);
      this.fill(minX, ay, minZ, maxX, by, maxZ, (x, y, z) -> x != minX && x != maxX && z != minZ && z != maxZ ? null : pick.at(x, y, z));
   }

   void cylinder(int cx, int cz, double outer, double inner, int y0, int y1, Build.Pick pick) {
      int r = (int)Math.ceil(outer);
      double o2 = outer * outer;
      double i2 = inner * inner;
      this.fill(cx - r, y0, cz - r, cx + r, y1, cz + r, (x, y, z) -> {
         double d = (x - cx) * (x - cx) + (z - cz) * (z - cz);
         return d <= o2 && d >= i2 ? pick.at(x, y, z) : null;
      });
   }

   void chest(int x, int y, int z, Direction facing, ResourceKey<LootTable> loot) {
      if (this.in(x, z)) {
         BlockPos pos = new BlockPos(x, y, z);
         this.level.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
         RandomizableContainer.setBlockEntityLootTable(this.level, this.random, pos, loot);
      }
   }

   void barrel(int x, int y, int z, Direction facing, ResourceKey<LootTable> loot) {
      if (this.in(x, z)) {
         BlockPos pos = new BlockPos(x, y, z);
         this.level.setBlock(pos, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, facing), 2);
         RandomizableContainer.setBlockEntityLootTable(this.level, this.random, pos, loot);
      }
   }

   static double hash(int x, int y, int z, int salt) {
      long h = x * 3129871L ^ z * 116129781L ^ y * 9119L ^ salt * -7046029254386353131L;
      h = h * h * 42317861L + h * 11L;
      h ^= h >>> 29;
      h *= -4658895280553007687L;
      h ^= h >>> 32;
      return (h & 16777215L) / 1.6777216E7;
   }

   interface Pick {
      BlockState at(int var1, int var2, int var3);
   }
}
