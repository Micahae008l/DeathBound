package com.deathbound.world;

import com.deathbound.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;

public final class Layout {
   public static final Layout.Island ARRIVAL = new Layout.Island(Layout.Theme.ARRIVAL, 0, 0, 100, 18, 26, 1);
   public static final Layout.Island HUB = new Layout.Island(Layout.Theme.HUB, 0, -95, 98, 26, 30, 2);
   public static final Layout.Island FOREST = new Layout.Island(Layout.Theme.FOREST, -78, -38, 102, 28, 30, 3);
   public static final Layout.Island VILLAGE = new Layout.Island(Layout.Theme.VILLAGE, 82, -44, 99, 44, 34, 4);
   public static final Layout.Island CRYPT = new Layout.Island(Layout.Theme.CRYPT, -62, -168, 97, 30, 44, 5);
   public static final Layout.Island SPIRE = new Layout.Island(Layout.Theme.SPIRE, 96, -152, 100, 16, 28, 6);
   public static final Layout.Island WATCH = new Layout.Island(Layout.Theme.WATCH, -136, -100, 96, 14, 24, 7);
   public static final Layout.Island GATE = new Layout.Island(Layout.Theme.GATE, 0, -228, 100, 34, 38, 8);
   public static final Layout.Island CITADEL = new Layout.Island(Layout.Theme.CITADEL, 0, -340, 100, 52, 46, 9);
   public static final Layout.Island HOLLOW = new Layout.Island(Layout.Theme.HOLLOW, -206, -126, 92, 22, 26, 10);
   public static final Layout.Island MERE = new Layout.Island(Layout.Theme.MERE, 40, 74, 97, 20, 24, 11);
   public static final List<Layout.Island> ISLANDS = List.of(
      ARRIVAL,
      HUB,
      FOREST,
      VILLAGE,
      CRYPT,
      SPIRE,
      WATCH,
      GATE,
      CITADEL,
      HOLLOW,
      MERE,
      new Layout.Island(Layout.Theme.DEBRIS, 34, 30, 108, 5, 9, 20),
      new Layout.Island(Layout.Theme.DEBRIS, -36, 24, 94, 4, 8, 21),
      new Layout.Island(Layout.Theme.DEBRIS, 44, -100, 112, 6, 10, 22),
      new Layout.Island(Layout.Theme.DEBRIS, -38, -92, 90, 5, 9, 23),
      new Layout.Island(Layout.Theme.DEBRIS, -120, -40, 110, 5, 8, 24),
      new Layout.Island(Layout.Theme.DEBRIS, 132, -96, 92, 6, 10, 25),
      new Layout.Island(Layout.Theme.DEBRIS, 52, -200, 118, 7, 11, 26),
      new Layout.Island(Layout.Theme.DEBRIS, -50, -232, 112, 6, 10, 27),
      new Layout.Island(Layout.Theme.DEBRIS, 70, -300, 96, 8, 12, 28),
      new Layout.Island(Layout.Theme.DEBRIS, -78, -330, 124, 7, 11, 29),
      new Layout.Island(Layout.Theme.DEBRIS, 66, -390, 130, 6, 9, 30),
      new Layout.Island(Layout.Theme.DEBRIS, -60, -400, 90, 5, 8, 31),
      new Layout.Island(Layout.Theme.DEBRIS, -110, -170, 120, 4, 7, 32),
      new Layout.Island(Layout.Theme.DEBRIS, 140, -190, 108, 5, 8, 33)
   );
   public static final List<Layout.Bridge> BRIDGES = List.of(
      new Layout.Bridge(ARRIVAL, HUB, 3, false),
      new Layout.Bridge(ARRIVAL, FOREST, 3, false),
      new Layout.Bridge(ARRIVAL, VILLAGE, 3, false),
      new Layout.Bridge(ARRIVAL, MERE, 3, false),
      new Layout.Bridge(FOREST, WATCH, 3, false),
      new Layout.Bridge(VILLAGE, SPIRE, 3, false),
      new Layout.Bridge(HUB, CRYPT, 3, false),
      new Layout.Bridge(HUB, VILLAGE, 3, false),
      new Layout.Bridge(HUB, GATE, 5, true),
      new Layout.Bridge(GATE, CITADEL, 5, true)
   );
   public static final BlockPos ARRIVAL_SPAWN = new BlockPos(0, ARRIVAL.top + 1, 5);
   public static final int DOOR_Z = GATE.z - 25;
   public static final int DOOR_HALF_WIDTH = 4;
   public static final int DOOR_HEIGHT = 20;
   public static final BlockPos GUARD_POST = new BlockPos(0, GATE.top + 1, DOOR_Z + 6);
   public static final BlockPos SOUL_SOURCE = new BlockPos(0, HUB.top + 1, HUB.z - HUB.radius + 3);
   public static final BlockPos SOUL_DESTINATION = new BlockPos(0, GATE.top + 1, DOOR_Z);
   public static final BlockPos ARENA_CENTER = new BlockPos(0, CITADEL.top + 1, CITADEL.z);
   public static final int ARENA_RADIUS = 30;
   public static final BlockPos THRONE_SEAT = new BlockPos(0, CITADEL.top + 4, CITADEL.z - 24);
   public static final List<BlockPos> ANCHORS = List.of(
      new BlockPos(-17, CITADEL.top + 6, CITADEL.z - 10),
      new BlockPos(17, CITADEL.top + 6, CITADEL.z - 10),
      new BlockPos(-11, CITADEL.top + 6, CITADEL.z + 8),
      new BlockPos(11, CITADEL.top + 6, CITADEL.z + 8)
   );
   static final BlockPos COLLECTOR_ROOM = new BlockPos(SPIRE.x, SPIRE.top - 10, SPIRE.z);

   public static boolean inArena(double x, double y, double z) {
      double dx = x - ARENA_CENTER.getX();
      double dz = z - ARENA_CENTER.getZ();
      return dx * dx + dz * dz < 1024.0 && y > CITADEL.top - 8 && y < CITADEL.top + 40;
   }

   public static boolean sanctuary(BlockPos pos) {
      for (Layout.Island is : new Layout.Island[]{ARRIVAL, VILLAGE, GATE, CITADEL, HOLLOW, MERE}) {
         double dx = pos.getX() - is.x;
         double dz = pos.getZ() - is.z;
         double r = is.radius + 12;
         if (dx * dx + dz * dz < r * r) {
            return true;
         }
      }

      return COLLECTOR_ROOM.distSqr(pos) < 144.0;
   }

   public static AABB arenaBox() {
      return new AABB(ARENA_CENTER).inflate(32.0, 20.0, 32.0).move(0.0, 12.0, 0.0);
   }

   public static boolean inDoorway(int x, int yRel) {
      if (yRel >= 0 && yRel < 23) {
         int half = 4;
         if (yRel >= 16) {
            half -= (yRel - 16 + 1) / 2 + (yRel >= 20 ? 1 : 0);
         }

         return Math.abs(x) <= half;
      } else {
         return false;
      }
   }

   static double noise(double x, double z, int salt) {
      int xi = (int)Math.floor(x);
      int zi = (int)Math.floor(z);
      double tx = x - xi;
      double tz = z - zi;
      tx = tx * tx * (3.0 - 2.0 * tx);
      tz = tz * tz * (3.0 - 2.0 * tz);
      double a = Build.hash(xi, 0, zi, salt);
      double b = Build.hash(xi + 1, 0, zi, salt);
      double c = Build.hash(xi, 0, zi + 1, salt);
      double d = Build.hash(xi + 1, 0, zi + 1, salt);
      return ((a + (b - a) * tx) * (1.0 - tz) + (c + (d - c) * tx) * tz) * 2.0 - 1.0;
   }

   static double shore(Layout.Island is, double angle) {
      int s = is.seed;
      return is.radius * (1.0 + 0.14 * Math.sin(3.0 * angle + s) + 0.08 * Math.sin(5.0 * angle + s * 1.7) + 0.05 * Math.sin(8.0 * angle + s * 2.3));
   }

   static int surface(Layout.Island is, int x, int z) {
      double dx = x - is.x;
      double dz = z - is.z;
      double d = Math.sqrt(dx * dx + dz * dz) / shore(is, Math.atan2(dz, dx));
      if (d >= 1.0) {
         return -2147483648;
      }

      boolean flat = is.theme != Layout.Theme.DEBRIS && is.theme != Layout.Theme.FOREST;
      int bump = (int)Math.round(noise(x * 0.09, z * 0.09, is.seed) * (flat ? 0.6 : 1.8));
      int rim = d > 0.84 ? (int)((d - 0.84) * 12.0) : 0;
      return is.top + bump - rim;
   }

   static void island(Build b, Layout.Island is) {
      int reach = (int)Math.ceil(is.radius * 1.3);
      if (b.overlaps(is.x - reach, is.z - reach, is.x + reach, is.z + reach)) {
         BlockState soulstone = ModBlocks.SOULSTONE.defaultBlockState();
         BlockState soil = ModBlocks.ASHEN_SOIL.defaultBlockState();

         for (int x = Math.max(b.x0, is.x - reach); x <= Math.min(b.x1, is.x + reach); x++) {
            for (int z = Math.max(b.z0, is.z - reach); z <= Math.min(b.z1, is.z + reach); z++) {
               double dx = x - is.x;
               double dz = z - is.z;
               double d = Math.sqrt(dx * dx + dz * dz) / shore(is, Math.atan2(dz, dx));
               if (!(d >= 1.0)) {
                  int top = surface(is, x, z);
                  double body = Math.pow(1.0 - d, 0.62) * (0.78 + 0.32 * noise(x * 0.14, z * 0.14, is.seed + 7));
                  int bottom = top - 2 - (int)(is.depth * body);
                  double spikeRoll = Build.hash(x, 0, z, is.seed + 3);
                  if (spikeRoll < 0.035 && d < 0.85) {
                     bottom -= 3 + (int)(spikeRoll * 300.0);
                  }

                  for (int y = bottom; y <= top; y++) {
                     int depth = top - y;
                     double r = Build.hash(x, y, z, is.seed);
                     BlockState s;
                     if (depth == 0) {
                        s = soil;
                     } else if (depth <= 2) {
                        s = r < 0.55 ? soil : soulstone;
                     } else if (r < 0.035) {
                        s = Blocks.BLACKSTONE.defaultBlockState();
                     } else if (r < 0.042 && y < top - 6) {
                        s = Blocks.CRYING_OBSIDIAN.defaultBlockState();
                     } else {
                        s = soulstone;
                     }

                     b.set(x, y, z, s);
                  }

                  double under = Build.hash(x, bottom, z, is.seed + 11);
                  if (under < 0.05) {
                     b.set(x, bottom - 1, z, ModBlocks.SOUL_CRYSTAL.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.DOWN));
                  } else if (under < 0.07) {
                     b.set(x, bottom, z, Blocks.CRYING_OBSIDIAN.defaultBlockState());
                  }

                  if (is.theme != Layout.Theme.CITADEL && is.theme != Layout.Theme.GATE) {
                     double prop = Build.hash(x, top, z, is.seed + 21);
                     Direction face = Direction.from2DDataValue((int)(prop * 4000.0) & 3);
                     BlockState s = null;
                     if (d < 0.8) {
                        if (prop < 0.006) {
                           s = ModBlocks.BONE_PILE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, face);
                        } else if (prop < 0.009) {
                           s = ModBlocks.REMAINS.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, face);
                        } else if (prop < 0.024) {
                           s = ModBlocks.SMALL_SOUL_SHARD.defaultBlockState();
                        } else if (prop < 0.03) {
                           s = ModBlocks.MEDIUM_SOUL_SHARD.defaultBlockState();
                        } else if (prop < 0.033) {
                           s = ModBlocks.LARGE_SOUL_SHARD.defaultBlockState();
                        } else if (prop < 0.039) {
                           s = soulstone;
                        }
                     }

                     double grass = Build.hash(x, top, z, is.seed + 5);
                     if (s == null && grass < (is.theme == Layout.Theme.FOREST ? 0.24 : 0.12)) {
                        double kind = Build.hash(x, top, z, is.seed + 6);
                        s = (kind < 0.35
                              ? ModBlocks.GLOOM_GRASS
                              : (kind < 0.8 ? ModBlocks.DEAD_GRASS : (kind < 0.97 ? ModBlocks.TALL_DEAD_GRASS : ModBlocks.GHOST_BLOOM)))
                           .defaultBlockState();
                     }

                     if (s != null) {
                        b.set(x, top + 1, z, s);
                     }
                  }

                  if (d < 0.55 && Build.hash(x, bottom, z, is.seed + 31) < 0.0035) {
                     b.setLinked(x, bottom - 1, z, Blocks.IRON_CHAIN.defaultBlockState());
                     b.setLinked(x, bottom - 2, z, Blocks.IRON_CHAIN.defaultBlockState());
                     b.set(x, bottom - 3, z, ModBlocks.HANGING_REMAINS.defaultBlockState());
                  }
               }
            }
         }
      }
   }

   static void bridge(Build b, Layout.Bridge br) {
      double ax = br.from.x;
      double az = br.from.z;
      double bx = br.to.x;
      double bz = br.to.z;
      double len = Math.sqrt((bx - ax) * (bx - ax) + (bz - az) * (bz - az));
      double ux = (bx - ax) / len;
      double uz = (bz - az) / len;
      double px = -uz;
      double pz = ux;
      double start = br.from.radius * 0.8;
      double end = len - br.to.radius * 0.8;
      int pad = br.width + 3;
      if (b.overlaps(
         (int)Math.floor(Math.min(ax, bx)) - pad,
         (int)Math.floor(Math.min(az, bz)) - pad,
         (int)Math.ceil(Math.max(ax, bx)) + pad,
         (int)Math.ceil(Math.max(az, bz)) + pad
      )) {
         int ya = br.from.top;
         int yb = br.to.top;
         int half = br.width / 2;
         BlockState deck = ModBlocks.SOULSTONE_BRICKS.defaultBlockState();
         BlockState cracked = ModBlocks.CRACKED_SOULSTONE_BRICKS.defaultBlockState();
         BlockState tile = ModBlocks.SOULSTONE_TILES.defaultBlockState();
         BlockState slab = ModBlocks.SOULSTONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
         BlockState wall = ModBlocks.SOULSTONE_BRICK_WALL.defaultBlockState();

         for (double t = start; t <= end; t += 0.3) {
            double f = (t - start) / Math.max(1.0, end - start);
            double yf = ya + (yb - ya) * f - Math.sin(f * 3.141592653589793) * (br.grand ? 1.5 : 2.5);
            int base = (int)Math.floor(yf);
            boolean halfStep = yf - base >= 0.5;

            for (int k = -half - 1; k <= half + 1; k++) {
               int x = (int)Math.round(ax + ux * t + px * k);
               int z = (int)Math.round(az + uz * t + pz * k);
               if (b.in(x, z)) {
                  boolean rail = Math.abs(k) == half + 1;
                  double roll = Build.hash(x, base, z, 77);
                  if (rail) {
                     b.set(x, base, z, deck);
                     int step = (int)Math.round(t);
                     int post = step % 8;
                     if (post == 0) {
                        b.set(x, base + 1, z, ModBlocks.SOULSTONE_PILLAR.defaultBlockState());
                        b.set(x, base + 2, z, ModBlocks.CHISELED_SOULSTONE.defaultBlockState());
                        b.set(
                           x,
                           base + 3,
                           z,
                           Build.hash(step, k, 0, 79) < 0.35
                              ? ModBlocks.SKULL_SPIKE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.from2DDataValue(step & 3))
                              : ModBlocks.WRAITH_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false)
                        );
                     } else if (step % 29 == 13 && b.get(x, base + 1, z).isAir()) {
                        Direction in = Math.abs(px * k) > Math.abs(pz * k)
                           ? (px * k > 0.0 ? Direction.WEST : Direction.EAST)
                           : (pz * k > 0.0 ? Direction.NORTH : Direction.SOUTH);
                        b.set(
                           x - (int)Math.signum((float)Math.round(px * k)),
                           base + 1,
                           z - (int)Math.signum((float)Math.round(pz * k)),
                           ModBlocks.SLUMPED_REMAINS.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, in)
                        );
                        b.setLinked(x, base + 1, z, wall);
                     } else if (roll > 0.12 && b.get(x, base + 1, z).isAir()) {
                        b.setLinked(x, base + 1, z, wall);
                     }
                  } else {
                     BlockState s = br.grand && k == 0 ? tile : (roll < 0.15 ? cracked : deck);
                     b.set(x, base, z, s);
                     if (halfStep && b.get(x, base + 1, z).isAir()) {
                        b.set(x, base + 1, z, slab);
                     }

                     if (k == 0 && Build.hash(x, base - 1, z, 78) < 0.7) {
                        b.set(x, base - 1, z, ModBlocks.SOULSTONE.defaultBlockState());
                     }

                     if (k == 0 && (int)Math.round(t) % 23 == 11) {
                        b.set(x, base - 1, z, ModBlocks.DARK_SOULSTONE_BRICKS.defaultBlockState());
                        b.setLinked(x, base - 2, z, Blocks.IRON_CHAIN.defaultBlockState());
                        b.set(x, base - 3, z, ModBlocks.GIBBET_CAGE.defaultBlockState());
                     }
                  }
               }
            }
         }
      }
   }

   private Layout() {
   }

   public record Bridge(Layout.Island from, Layout.Island to, int width, boolean grand) {
   }

   public record Island(Layout.Theme theme, int x, int z, int top, int radius, int depth, int seed) {
      BlockPos center() {
         return new BlockPos(this.x, this.top, this.z);
      }
   }

   public enum Theme {
      ARRIVAL,
      HUB,
      FOREST,
      VILLAGE,
      CRYPT,
      SPIRE,
      WATCH,
      GATE,
      CITADEL,
      HOLLOW,
      MERE,
      DEBRIS;
   }
}
