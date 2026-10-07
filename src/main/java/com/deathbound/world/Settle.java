package com.deathbound.world;

import com.deathbound.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

final class Settle {
   private static final int REACH = 12;
   private static final Set<Block> HANGING = Set.of(
      ModBlocks.HANGING_REMAINS, ModBlocks.GIBBET_CAGE, ModBlocks.HANGING_BLADE, ModBlocks.BONE_CHANDELIER, Blocks.IRON_CHAIN
   );
   private static final Set<Block> RESTING = Set.of(
      ModBlocks.REMAINS,
      ModBlocks.SLUMPED_REMAINS,
      ModBlocks.BONE_PILE,
      ModBlocks.SKULL,
      ModBlocks.PIERCED_SKULL,
      ModBlocks.SKULL_SPIKE,
      ModBlocks.STUCK_ARROWS,
      ModBlocks.HUNTER_STAKE,
      ModBlocks.URN,
      ModBlocks.TOMBSTONE,
      ModBlocks.FARE_BOWL,
      ModBlocks.BEDROLL,
      ModBlocks.GHOSTWOOD_TABLE,
      ModBlocks.GHOSTWOOD_CHAIR,
      ModBlocks.BONE_CANDELABRA,
      ModBlocks.GLOWCAP,
      ModBlocks.SOUL_BRAZIER,
      ModBlocks.JAR,
      ModBlocks.SOUL_JAR,
      ModBlocks.BONE_JAR,
      ModBlocks.EYE_JAR,
      ModBlocks.JAR_CROWN,
      ModBlocks.JAR_KEYS,
      ModBlocks.JAR_HEART,
      ModBlocks.JAR_MOTH,
      ModBlocks.BOTTLED_SHIP
   );
   private static final Set<Block> PLANTS = Set.of(ModBlocks.GLOOM_GRASS, ModBlocks.DEAD_GRASS, ModBlocks.TALL_DEAD_GRASS, ModBlocks.GHOST_BLOOM);

   static void chunk(Build b) {
      ChunkAccess chunk = b.level.getChunk(b.x0 >> 4, b.z0 >> 4);
      LevelChunkSection[] sections = chunk.getSections();

      for (int i = sections.length - 1; i >= 0; i--) {
         if (!sections[i].hasOnlyAir()) {
            int baseY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(i));

            for (int ly = 15; ly >= 0; ly--) {
               for (int lx = 0; lx < 16; lx++) {
                  for (int lz = 0; lz < 16; lz++) {
                     BlockState s = sections[i].getBlockState(lx, ly, lz);
                     if (!s.isAir()) {
                        int x = b.x0 + lx;
                        int y = baseY + ly;
                        int z = b.z0 + lz;
                        if (hangs(s)) {
                           hangUp(b, x, y, z);
                        } else if (PLANTS.contains(s.getBlock())) {
                           if (!holds(b, x, y - 1, z)) {
                              b.set(x, y, z, Blocks.AIR.defaultBlockState());
                           }
                        } else if (rests(s)) {
                           fall(b, x, y, z, s);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean hangs(BlockState s) {
      if (s.getBlock() instanceof ChainBlock) {
         return s.getValue(ChainBlock.AXIS) == Axis.Y;
      } else {
         return s.getBlock() instanceof LanternBlock ? s.getValue(LanternBlock.HANGING) : HANGING.contains(s.getBlock());
      }
   }

   private static boolean rests(BlockState s) {
      return s.getBlock() instanceof LanternBlock ? !s.getValue(LanternBlock.HANGING) : s.getBlock() instanceof CandleBlock || RESTING.contains(s.getBlock());
   }

   private static boolean holds(Build b, int x, int y, int z) {
      BlockState s = b.get(x, y, z);
      return !s.isAir()
         && s.getFluidState().isEmpty()
         && (!s.getCollisionShape(b.level, new BlockPos(x, y, z)).isEmpty() || s.getBlock() instanceof ChainBlock);
   }

   private static void hangUp(Build b, int x, int y, int z) {
      if (!holds(b, x, y + 1, z)) {
         for (int k = 2; k <= 12; k++) {
            BlockState s = b.get(x, y + k, z);
            if (!s.isAir() && s.getFluidState().isEmpty()) {
               if (holds(b, x, y + k, z)) {
                  for (int c = 1; c < k; c++) {
                     b.setLinked(x, y + c, z, Blocks.IRON_CHAIN.defaultBlockState());
                  }

                  return;
               }
               break;
            }
         }

         BlockState s = b.get(x, y, z);
         if (s.getBlock() instanceof LanternBlock) {
            fall(b, x, y, z, s.setValue(LanternBlock.HANGING, false));
         } else {
            b.set(x, y, z, Blocks.AIR.defaultBlockState());
         }
      }
   }

   private static void fall(Build b, int x, int y, int z, BlockState s) {
      if (holds(b, x, y - 1, z)) {
         b.set(x, y, z, s);
      } else {
         b.set(x, y, z, Blocks.AIR.defaultBlockState());

         for (int k = 1; k <= 12; k++) {
            BlockState at = b.get(x, y - k, z);
            if (!at.isAir()) {
               return;
            }

            if (holds(b, x, y - k - 1, z)) {
               b.set(x, y - k, z, s);
               return;
            }
         }
      }
   }

   static List<BlockPos> problems(Level level, ChunkAccess chunk) {
      List<BlockPos> out = new ArrayList<>();
      LevelChunkSection[] sections = chunk.getSections();
      MutableBlockPos m = new MutableBlockPos();
      int x0 = chunk.getPos().getMinBlockX();
      int z0 = chunk.getPos().getMinBlockZ();

      for (int i = 0; i < sections.length; i++) {
         if (!sections[i].hasOnlyAir()) {
            int baseY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(i));

            for (int ly = 0; ly < 16; ly++) {
               for (int lx = 0; lx < 16; lx++) {
                  for (int lz = 0; lz < 16; lz++) {
                     BlockState s = sections[i].getBlockState(lx, ly, lz);
                     boolean hang = !s.isAir() && hangs(s);
                     boolean rest = !s.isAir() && (rests(s) || PLANTS.contains(s.getBlock()));
                     if (hang || rest) {
                        m.set(x0 + lx, baseY + ly + (hang ? 1 : -1), z0 + lz);
                        BlockState n = level.getBlockState(m);
                        boolean held = !n.isAir()
                           && n.getFluidState().isEmpty()
                           && (!n.getCollisionShape(level, m).isEmpty() || n.getBlock() instanceof ChainBlock);
                        if (!held) {
                           out.add(new BlockPos(x0 + lx, baseY + ly, z0 + lz));
                        }
                     }
                  }
               }
            }
         }
      }

      return out;
   }

   private Settle() {
   }
}
