package com.deathbound.world;

import com.deathbound.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

final class Decor {
   static BlockState remains(Direction d) {
      return Structures.facing(ModBlocks.REMAINS, d);
   }

   static BlockState slumped(Direction d) {
      return Structures.facing(ModBlocks.SLUMPED_REMAINS, d);
   }

   static BlockState hanging() {
      return Structures.facing(ModBlocks.HANGING_REMAINS, Direction.NORTH);
   }

   static BlockState gibbet(int salt) {
      return Structures.facing(ModBlocks.GIBBET_CAGE, Structures.dir(salt));
   }

   static BlockState bones(int salt) {
      return Structures.facing(ModBlocks.BONE_PILE, Structures.dir(salt));
   }

   static BlockState spike(int salt) {
      return Structures.facing(ModBlocks.SKULL_SPIKE, Structures.dir(salt));
   }

   static Direction toward(double dx, double dz) {
      return Math.abs(dx) > Math.abs(dz) ? (dx > 0.0 ? Direction.EAST : Direction.WEST) : (dz > 0.0 ? Direction.SOUTH : Direction.NORTH);
   }

   static void camp(Build b, Layout.Island is, int salt) {
      double a = Build.hash(salt, is.seed(), 1, 91) * 3.141592653589793 * 2.0;
      double r = (0.42 + Build.hash(salt, is.seed(), 2, 91) * 0.2) * is.radius();
      int px = is.x() + (int)Math.round(Math.cos(a) * r);
      int pz = is.z() + (int)Math.round(Math.sin(a) * r);
      if (px - 2 >> 4 != px + 2 >> 4) {
         px = (px >> 4 << 4) + 8;
      }

      if (pz - 1 >> 4 != pz + 1 >> 4) {
         pz = (pz >> 4 << 4) + 8;
      }

      if (b.in(px, pz)) {
         int y = Layout.surface(is, px, pz);
         if (y != -2147483648) {
            for (int dx = -2; dx <= 2; dx++) {
               for (int dz = -1; dz <= 1; dz++) {
                  if (Layout.surface(is, px + dx, pz + dz) != y || !b.get(px + dx, y + 1, pz + dz).canBeReplaced() || !b.get(px + dx, y, pz + dz).canOcclude()) {
                     return;
                  }
               }
            }

            b.set(px, y + 1, pz, Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
            b.set(px + 2, y + 1, pz, Structures.facing(ModBlocks.BEDROLL, Direction.SOUTH));
            b.set(px + 2, y + 1, pz + 1, remains(Direction.SOUTH));
            if (Build.hash(px, salt, pz, 77) < 0.5) {
               b.barrel(px - 2, y + 1, pz, Direction.UP, Structures.CAMP_LOOT);
            }

            b.set(px - 2, y + 1, pz + 1, bones(salt));
            b.set(px - 1, y + 1, pz - 1, Structures.candles(1));
         }
      }
   }

   static void decorate(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      int y = is.top();
      if (is.theme() != Layout.Theme.CITADEL && is.theme() != Layout.Theme.DEBRIS) {
         camp(b, is, 1);
         if (is.radius() >= 26) {
            camp(b, is, 2);
         }
      }

      switch (is.theme()) {
         case ARRIVAL:
            b.set(cx - 4, y + 1, cz - 4, Structures.brazier());
            b.set(cx + 4, y + 1, cz - 4, Structures.brazier());

            for (int k = 0; k < 9; k++) {
               double a = k * 3.141592653589793 * 2.0 / 9.0 + 0.35;
               if (k % 3 == 1) {
                  int px = cx + (int)Math.round(Math.cos(a) * 10.0);
                  int pz = cz + (int)Math.round(Math.sin(a) * 10.0);
                  Structures.onGround(b, is, px, pz, slumped(toward(-Math.cos(a), -Math.sin(a))));
               }
            }

            Structures.onGround(b, is, cx + 13, cz + 6, remains(Direction.EAST));
            Structures.onGround(b, is, cx - 12, cz + 9, bones(1));
            Structures.onGround(b, is, cx + 7, cz + 13, bones(2));
            break;
         case HUB:
            for (int k = 0; k < 4; k++) {
               Structures.onGround(b, is, cx + 12 + k * 2, cz + 6 + k % 2, Structures.facing(ModBlocks.TOMBSTONE, Structures.dir(k)));
            }

            for (int s = -1; s <= 1; s += 2) {
               b.set(cx + s * 2, y + 1, cz - 7, Structures.brazier());
               b.set(cx + s * 2, y + 1, cz + 7, Structures.brazier());

               for (int z = cz - 13; z >= cz - 25; z -= 4) {
                  Structures.onGround(b, is, cx + s * 3, z, spike(z));
               }
            }

            for (int x : new int[]{cx - 5, cx + 3, cx + 6}) {
               if (!b.get(x, y + 10, cz + 8).isAir()) {
                  b.set(x, y + 9, cz + 8, hanging());
               }
            }

            b.set(cx - 5, y + 1, cz - 4, remains(Direction.SOUTH));
            b.set(cx + 6, y + 1, cz + 2, slumped(Direction.WEST));

            for (int i = 0; i < 9; i++) {
               int x = cx + (int)((Build.hash(i, 3, 0, 57) - 0.5) * 2.0 * (is.radius() - 5));
               int z = cz + (int)((Build.hash(i, 4, 0, 57) - 0.5) * 2.0 * (is.radius() - 5));
               if (Math.abs(x - cx) > 10 || Math.abs(z - cz) > 10) {
                  Structures.onGround(b, is, x, z, i % 3 == 0 ? remains(Structures.dir(i)) : (i % 3 == 1 ? bones(i) : slumped(Structures.dir(i))));
               }
            }
            break;
         case FOREST:
            for (int i = 0; i < 10; i++) {
               int x = cx + (int)((Build.hash(i, 5, 0, 58) - 0.5) * 2.0 * (is.radius() - 6));
               int z = cz + (int)((Build.hash(i, 6, 0, 58) - 0.5) * 2.0 * (is.radius() - 6));
               Structures.onGround(b, is, x, z, i % 2 == 0 ? remains(Structures.dir(i)) : bones(i));
            }
            break;
         case VILLAGE:
            for (int sx = -1; sx <= 1; sx += 2) {
               for (int sz = -1; sz <= 1; sz += 2) {
                  Structures.onGround(b, is, cx + sx * 3, cz + sz * 3, Structures.brazier());
               }
            }

            int[][] houses = new int[][]{{-15, -12, 7, 6}, {2, -18, 8, 6}, {14, -6, 6, 7}, {-18, 5, 7, 6}, {5, 11, 8, 6}, {-5, 17, 6, 5}};

            for (int i = 0; i < houses.length; i++) {
               int x0 = cx + houses[i][0];
               int z0 = cz + houses[i][1];
               int x1 = x0 + houses[i][2] - 1;
               int z1 = z0 + houses[i][3] - 1;
               b.set(x1 - 1, y + 1, z0 + 1, slumped(Direction.SOUTH));
               if (i % 2 == 1) {
                  b.set(x0 + 2, y + 1, z1 - 1, remains(Direction.EAST));
               }
            }

            Structures.onGround(b, is, cx + 8, cz + 1, remains(Direction.WEST));
            Structures.onGround(b, is, cx - 7, cz - 1, bones(3));

            for (int k = 0; k < 6; k++) {
               Structures.onGround(b, is, cx - 10 + k % 3 * 2, cz + 13 + k / 3 * 2, Structures.facing(ModBlocks.TOMBSTONE, Direction.NORTH));
            }

            Structures.onGround(b, is, cx + 1, cz - 10, slumped(Direction.EAST));
            int bx = cx + 18;
            int bz = cz + 14;
            int by = Layout.surface(is, bx, bz);
            if (by != -2147483648) {
               b.set(bx + 2, by + 13, bz, Structures.DARK);
               b.set(bx + 3, by + 13, bz, Structures.DARK);
               b.set(bx + 3, by + 12, bz, hanging());
            }
            break;
         case CRYPT:
            for (int k = 0; k < 9; k++) {
               Structures.onGround(b, is, cx - 12 + k % 3 * 3 + k / 3 * 9, cz + 18 + k / 3 % 2 * 3, Structures.facing(ModBlocks.TOMBSTONE, Direction.SOUTH));
            }

            int mz1 = cz + 14;
            int yc = y - 9;
            int cz0 = mz1 - 13;
            Structures.onGround(b, is, cx - 3, mz1 + 2, spike(1));
            Structures.onGround(b, is, cx + 3, mz1 + 2, spike(2));
            Structures.onGround(b, is, cx - 4, mz1 + 5, remains(Direction.SOUTH));

            for (int side = -1; side <= 1; side += 2) {
               b.set(cx + side * 8 + 3, yc + 3, cz0 - 3, hanging());
            }

            b.set(cx - 10, yc + 1, cz0 - 6, remains(Direction.NORTH));
            b.set(cx - 6, yc + 1, cz0 - 6, bones(1));
            b.set(cx + 4, yc, cz0 - 1, bones(5));
            b.set(cx - 10, yc, cz0 + 1, remains(Direction.EAST));
            b.set(cx - 2, yc, cz0 + 1, slumped(Direction.NORTH));
            b.set(cx + 16, yc, cz0 - 3, Structures.brazier());
            b.set(cx + 16, yc, cz0 + 3, Structures.brazier());
            break;
         case SPIRE:
            for (int s = -1; s <= 1; s += 2) {
               b.set(cx + s * 4, y + 45, cz, Structures.DARK);
               b.set(cx + s * 5, y + 45, cz, Structures.DARK);
               Structures.hang(b, cx + s * 5, y + 45, cz, 1, gibbet(s + 1));
            }

            b.set(cx + 2, y + 19, cz + 2, hanging());
            b.set(cx - 2, y + 31, cz + 1, hanging());
            Structures.onGround(b, is, cx + 2, cz + 7, slumped(Direction.SOUTH));
            Structures.onGround(b, is, cx - 4, cz + 8, bones(4));
            break;
         case WATCH:
            b.set(cx - 7, y + 19, cz, gibbet(1));
            Structures.onGround(b, is, cx + 5, cz + 1, remains(Direction.EAST));
            break;
         case GATE:
            int dz = Layout.DOOR_Z;

            for (int z = cz + 18; z >= dz + 8; z -= 8) {
               Structures.onGround(b, is, cx - 9, z, spike(z));
               Structures.onGround(b, is, cx + 9, z, spike(z + 1));
            }

            b.set(cx - 3, y + 1, dz + 5, Structures.brazier());
            b.set(cx + 3, y + 1, dz + 5, Structures.brazier());
            b.set(cx - 4, y + 1, dz + 3, bones(1));
            b.set(cx + 4, y + 1, dz + 4, bones(2));
            b.set(cx - 4, y + 1, dz + 8, remains(Direction.EAST));
            b.set(cx + 4, y + 1, dz + 10, remains(Direction.WEST));
            b.set(cx - 5, y + 1, dz + 12, slumped(Direction.EAST));

            for (int s = -1; s <= 1; s += 2) {
               b.set(cx + s * 13, y + 33, dz + 5, Structures.DARK);
               b.set(cx + s * 13, y + 33, dz + 6, Structures.DARK);
               Structures.hang(b, cx + s * 13, y + 33, dz + 6, 2, hanging());
            }
            break;
         case CITADEL:
            citadel(b, is);
            break;
         case DEBRIS:
            if (is.seed() % 3 == 0) {
               Structures.onGround(b, is, cx + 1, cz - 1, remains(Structures.dir(is.seed())));
            }
      }
   }

   private static void citadel(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      int y = is.top();

      for (int k = 0; k < 8; k++) {
         if (k != 6) {
            double a = k * 3.141592653589793 / 4.0;
            b.set(cx + (int)Math.round(Math.cos(a) * 19.0), y + 1, cz + (int)Math.round(Math.sin(a) * 19.0), Structures.brazier());
         }
      }

      for (int k = 0; k < 28; k++) {
         double a = k * 3.141592653589793 * 2.0 / 28.0;
         int x = cx + (int)Math.round(Math.cos(a) * 32.3);
         int z = cz + (int)Math.round(Math.sin(a) * 32.3);
         if ((z <= cz + 28 || Math.abs(x - cx) >= 6) && b.air(x, y + 15, z) && !b.get(x, y + 14, z).isAir()) {
            b.set(x, y + 15, z, spike(k));
         }
      }

      for (int k = 0; k < 8; k++) {
         double a = k * 3.141592653589793 / 4.0 + 0.39269908169872414;
         int x = cx + (int)Math.round(Math.cos(a) * 30.0);
         int z = cz + (int)Math.round(Math.sin(a) * 30.0);
         b.set(x, y + 14, z, Structures.DARK);
         b.set(x, y + 13, z, hanging());
      }

      for (double deg : new double[]{45.0, 245.0, 295.0}) {
         double a = Math.toRadians(deg);
         int tx = cx + (int)Math.round(Math.cos(a) * 33.0);
         int tz = cz + (int)Math.round(Math.sin(a) * 33.0);
         double ux = (cx - tx) / 33.0;
         double uz = (cz - tz) / 33.0;
         int bx = tx + (int)Math.round(ux * 6.0);
         int bz = tz + (int)Math.round(uz * 6.0);
         int ex = tx + (int)Math.round(ux * 7.5);
         int ez = tz + (int)Math.round(uz * 7.5);
         b.set(bx, y + 25, bz, Structures.DARK);
         b.set(ex, y + 25, ez, Structures.DARK);
         Structures.hang(b, ex, y + 25, ez, 2, gibbet((int)deg));
      }

      int tz = cz - 24;

      for (int s = -1; s <= 1; s += 2) {
         b.set(cx + s * 7, y + 2, tz + 5, bones(s + 3));
         b.set(cx + s * 3, y + 4, tz + 3, slumped(Direction.SOUTH));
         b.set(cx + s * 8, y + 2, tz - 6, spike(s + 5));
         b.set(cx + s * 5, y + 3, tz + 4, remains(s < 0 ? Direction.EAST : Direction.WEST));
      }
   }

   private static boolean built(BlockState s) {
      return s.is(ModBlocks.SOULSTONE_BRICKS)
         || s.is(ModBlocks.CRACKED_SOULSTONE_BRICKS)
         || s.is(ModBlocks.DARK_SOULSTONE_BRICKS)
         || s.is(ModBlocks.SOULSTONE_TILES)
         || s.is(ModBlocks.POLISHED_SOULSTONE)
         || s.is(ModBlocks.SOULSTONE_BRICK_SLAB)
         || s.is(ModBlocks.SOULSTONE_BRICK_STAIRS)
         || s.is(ModBlocks.GHOSTWOOD_PLANKS)
         || s.is(ModBlocks.CHISELED_SOULSTONE);
   }

   static void neglect(Build b) {
      for (int x = b.x0; x <= b.x1; x++) {
         for (int z = b.z0; z <= b.z1; z++) {
            for (int y = 170; y >= 60; y--) {
               if (b.get(x, y, z).isAir()) {
                  BlockState above = b.get(x, y + 1, z);
                  if (!above.isAir() && above.canOcclude()) {
                     int walls = 0;

                     for (Direction d : Plane.HORIZONTAL) {
                        if (b.get(x + d.getStepX(), y, z + d.getStepZ()).canOcclude()) {
                           walls++;
                        }
                     }

                     double r = Build.hash(x, y, z, 401);
                     if (walls >= 2 && r < 0.3) {
                        b.set(x, y, z, Structures.WEB);
                        if (walls >= 3 && b.get(x, y - 1, z).isAir() && r < 0.12) {
                           b.set(x, y - 1, z, Structures.WEB);
                        }
                     } else if (walls == 0 && built(above) && r < 0.035 && clear(b, x, y, z, 7)) {
                        int len = 2 + (int)(Build.hash(x, y, z, 402) * 4.0);

                        for (int k = 0; k < len; k++) {
                           b.setLinked(x, y - k, z, Blocks.IRON_CHAIN.defaultBlockState());
                        }

                        double end = Build.hash(x, y, z, 403);
                        if (end < 0.35) {
                           b.set(x, y - len, z, Structures.lantern(true));
                        } else if (end < 0.6) {
                           b.set(x, y - len, z, hanging());
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean clear(Build b, int x, int y, int z, int depth) {
      for (int k = 1; k <= depth; k++) {
         if (!b.get(x, y - k, z).isAir()) {
            return false;
         }
      }

      return true;
   }

   private Decor() {
   }
}
