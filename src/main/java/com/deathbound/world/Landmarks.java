package com.deathbound.world;

import com.deathbound.DeathBound;
import com.deathbound.block.FerryBlock;
import com.deathbound.mixin.SignTextAccess;
import com.deathbound.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

final class Landmarks {
   static final ResourceKey<LootTable> QUEST_OAR = loot("chests/quest_oar");
   static final ResourceKey<LootTable> QUEST_TAG = loot("chests/quest_tag");
   static final ResourceKey<LootTable> FERRY_LOOT = loot("chests/ferry");
   static final ResourceKey<LootTable> APOTHECARY_LOOT = loot("chests/apothecary");
   static final ResourceKey<LootTable> CURIO_LOOT = loot("chests/curios");
   static final ResourceKey<LootTable> HUNTER_LOOT = loot("chests/hunter");
   static final ResourceKey<LootTable> LAST_CUP_LOOT = loot("chests/last_cup");
   static final BlockState DRY_BED = ModBlocks.ASHEN_SOIL.defaultBlockState();
   static final BlockState CRACK = Blocks.SOUL_SOIL.defaultBlockState();
   static final BlockState HULL = Structures.PLANKS;
   static final BlockState TRUNK = ModBlocks.OSSIFIED_LOG.defaultBlockState();
   static final int MERE_LX = Layout.MERE.x() - 4;
   static final int MERE_LZ = Layout.MERE.z() + 1;
   static final double MERE_RX = 10.5;
   static final double MERE_RZ = 8.5;

   static boolean inLandingRiver(int x, int z) {
      Layout.Island is = Layout.ARRIVAL;
      return x >= is.x() + 4 && z >= is.z() - 13 && z <= is.z() - 9;
   }

   static boolean inCrossingRiver(int x, int z) {
      Layout.Island is = Layout.HUB;
      return z >= is.z() + 13 && z <= is.z() + 16 && Math.abs(x - is.x()) <= is.radius() + 2;
   }

   private static void channel(Build b, Layout.Island is, int x, int z, boolean bank) {
      int top = Layout.surface(is, x, z);
      if (top != -2147483648 && b.in(x, z)) {
         int bed = is.top() - (bank ? 2 : 3);
         b.fill(x, bed + 1, z, x, Math.max(top, is.top()) + 3, z, Structures.AIR);
         double r = Build.hash(x, 0, z, 401);
         boolean crack = (x + (int)(Build.hash(z, 0, 0, 402) * 3.0)) % 5 == 0 || (z + (int)(Build.hash(x, 0, 0, 403) * 3.0)) % 6 == 0;
         b.set(x, bed, z, crack ? CRACK : DRY_BED);
         b.set(x, bed - 1, z, Structures.STONE);
         if (!bank && r < 0.05) {
            b.set(x, bed + 1, z, Decor.bones((int)(r * 1000.0)));
         } else if (!bank && r < 0.065) {
            b.set(x, bed + 1, z, Structures.skull((int)(r * 1000.0) % 16));
         } else if (r > 0.94) {
            b.set(x, bed + 1, z, Blocks.DEAD_BUSH.defaultBlockState());
         }
      }
   }

   static void landing(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      int y = is.top();
      if (b.overlaps(cx + 2, cz - 14, cx + is.radius() + 3, cz - 6)) {
         for (int x = cx + 4; x <= cx + is.radius() + 2; x++) {
            for (int z = cz - 13; z <= cz - 9; z++) {
               channel(b, is, x, z, z == cz - 13);
            }

            if (Layout.surface(is, x, cz - 8) != -2147483648) {
               b.fill(x, y - 3, cz - 8, x, y, cz - 8, (xx, yy, zz) -> yy == y ? Structures.CHISELED : Structures.BRICKS);
               b.set(x, y - 2, cz - 9, Build.hash(x, 0, 0, 431) < 0.5 ? CRACK : DRY_BED);
            }
         }

         for (int x = cx + 2; x <= cx + 7; x++) {
            for (int z = cz - 10; z <= cz - 8; z++) {
               b.set(x, y, z, Structures.PLANKS);
            }
         }

         for (int[] p : new int[][]{{cx + 4, cz - 11}, {cx + 7, cz - 11}, {cx + 7, cz - 7}}) {
            b.fill(p[0], y - 2, p[1], p[0], y + 1, p[1], Structures.log(Axis.Y));
         }

         b.set(cx + 7, y + 2, cz - 11, Structures.log(Axis.Y));
         // the Ferryman's notice, nailed to the post at the end of the dock
         b.set(cx + 7, y + 1, cz - 6, Structures.note(0, Direction.SOUTH, false));
         b.set(cx + 7, y + 3, cz - 11, Structures.lantern(false));
         b.setLinked(cx + 4, y + 2, cz - 11, Structures.chain());
         b.set(cx + 3, y + 1, cz - 10, Structures.facing(ModBlocks.GHOSTWOOD_TABLE, Direction.SOUTH));
         b.set(cx + 3, y + 2, cz - 10, Structures.facing(ModBlocks.FARE_BOWL, Direction.SOUTH));
         b.set(cx + 4, y + 1, cz - 10, Structures.facing(ModBlocks.GHOSTWOOD_TABLE, Direction.SOUTH));
         b.set(cx + 4, y + 2, cz - 10, Structures.candles(2));
         b.set(cx + 3, y + 1, cz - 8, Structures.facing(ModBlocks.GHOSTWOOD_CHAIR, Direction.NORTH));
         int bx0 = cx + 10;
         int bed = y - 2;

         // the river's gone: the ferry stands on its end by the dock, leaning on a log
         int ground = Layout.surface(is, cx + 9, cz - 7);
         if (ground != -2147483648) {
            for (int k = 0; k < 4; k++) {
               b.set(cx + 9, ground + 1 + k, cz - 7,
                  Structures.facing(ModBlocks.FERRY, Direction.SOUTH).setValue(FerryBlock.PART, k).setValue(FerryBlock.UPRIGHT, true));
            }

            b.fill(cx + 9, ground + 1, cz - 8, cx + 9, ground + 3, cz - 8, Structures.log(Axis.Y));
         }

         b.chest(bx0 + 1, bed + 1, cz - 9, Direction.NORTH, FERRY_LOOT);
      }
   }

   static void crossing(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      int y = is.top();
      if (b.overlaps(cx - is.radius() - 2, cz + 12, cx + is.radius() + 2, cz + 17)) {
         for (int x = cx - is.radius() - 2; x <= cx + is.radius() + 2; x++) {
            for (int z = cz + 13; z <= cz + 16; z++) {
               if (Math.abs(x - cx) > 2) {
                  channel(b, is, x, z, z == cz + 13 || z == cz + 16);
               }
            }
         }

         for (int z = cz + 12; z <= cz + 17; z++) {
            int top = Layout.surface(is, cx, z);
            b.fill(cx - 2, y - 2, z, cx + 2, y - 1, z, Structures.STONE);
            b.fill(cx - 1, y, z, cx + 1, y, z, Structures.TILES);
            b.set(cx - 2, y + 1, z, Structures.WALL);
            b.set(cx + 2, y + 1, z, Structures.WALL);
            if (top != -2147483648) {
               b.fill(cx - 1, y + 1, z, cx + 1, top + 2, z, Structures.AIR);
            }
         }

         // the dry river runs on under the footbridge: only the two ends rest on stone
         for (int z = cz + 13; z <= cz + 16; z++) {
            b.fill(cx - 2, y - 3, z, cx + 2, y - 3, z, DRY_BED);
            b.fill(cx - 2, y - 2, z, cx + 2, y - 1, z, Structures.AIR);
         }

         // Pip's ball, where it rolled: on the dry bed under the footbridge (placed after the bed is cut, or it gets erased)
         b.set(cx + 1, y - 2, cz + 14, ModBlocks.PIPS_BALL.defaultBlockState());

         b.set(cx - 2, y + 2, cz + 12, Structures.lantern(false));
         b.set(cx + 2, y + 2, cz + 17, Structures.lantern(false));
      }
   }

   static void lake(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      int y = is.top();
      int R = is.radius();
      if (b.overlaps(cx - R, cz - R, cx + R, cz + R)) {
         int bed = y - 4;

         for (int x = Math.max(cx - R, b.x0); x <= Math.min(cx + R, b.x1); x++) {
            for (int z = Math.max(cz - R, b.z0); z <= Math.min(cz + R, b.z1); z++) {
               double d = Math.hypot(x - cx, z - cz);
               int top = Layout.surface(is, x, z);
               if (!(d < 35.5) && top != -2147483648 && (z <= cz || Math.abs(x - cx) > 4)) {
                  double t = (d - 35.5) / (R - 2 - 35.5);
                  int floor = t > 0.78 ? bed + (int)Math.round((t - 0.78) / 0.22 * 4.0) : bed;
                  if (floor < top) {
                     b.fill(x, floor + 1, z, x, top + 2, z, Structures.AIR);
                     boolean crack = (x + (int)(Build.hash(z, 1, 0, 411) * 4.0)) % 7 == 0 || (z + (int)(Build.hash(x, 1, 0, 412) * 4.0)) % 6 == 0;
                     b.set(x, floor, z, crack ? CRACK : DRY_BED);
                     double r = Build.hash(x, floor, z, 413);
                     if (r < 0.012) {
                        b.set(x, floor + 1, z, Structures.skull((int)(r * 9000.0) % 16));
                     } else if (r < 0.03) {
                        b.set(x, floor + 1, z, Decor.bones((int)(r * 9000.0)));
                     } else if (r < 0.034) {
                        b.set(x, floor + 1, z, Structures.crystal(Direction.UP));
                     } else if (r < 0.04) {
                        b.set(x, floor + 1, z, Decor.remains(Direction.from2DDataValue((int)(r * 4000.0) % 4)));
                     }
                  }
               }
            }
         }

         for (int[] boat : new int[][]{{cx - 42, cz + 6, 0}, {cx + 39, cz - 18, 1}, {cx - 20, cz - 44, 0}, {cx + 24, cz + 38, 1}}) {
            skiff(b, boat[0], bed + 1, boat[1], boat[2] == 1);
         }

         int px = cx + 43;
         int pz = cz + 4;
         if (b.overlaps(px - 2, pz - 2, px + 2, pz + 2)) {
            b.fill(px - 1, bed - 1, pz - 1, px + 1, bed - 1, pz + 1, Structures.TEAR);
            b.fill(px - 1, bed, pz - 1, px + 1, bed, pz + 1, Structures.AIR);
            b.chest(px, bed, pz, Direction.WEST, LAST_CUP_LOOT);
            b.set(px + 1, bed, pz + 1, Structures.candles(2));
         }
      }
   }

   private static void skiff(Build b, int x, int y, int z, boolean alongX) {
      if (b.overlaps(x - 4, z - 4, x + 4, z + 4)) {
         for (int k = -2; k <= 2; k++) {
            int bx = alongX ? x + k : x;
            int bz = alongX ? z : z + k;
            b.set(bx, y, bz, HULL);
            if (Math.abs(k) == 2) {
               b.set(bx, y + 1, bz, Structures.log(alongX ? Axis.X : Axis.Z));
            }
         }
      }
   }

   static void forge(Build b, Layout.Island is) {
      int x0 = is.x() - 10;
      int z0 = is.z() + 6;
      int x1 = is.x() - 3;
      int z1 = is.z() + 11;
      int y = is.top();
      if (b.overlaps(x0 - 1, z0 - 1, x1 + 1, z1 + 1)) {
         Structures.footing(b, x0, z0, x1, z1, y);
         b.fill(x0, y, z0, x1, y, z1, (x, yy, z) -> Build.hash(x, yy, z, 421) < 0.3 ? Structures.CRACKED : Structures.BRICKS);
         b.fill(x0, y + 1, z0, x1, y + 6, z1, Structures.AIR);

         for (int[] c : new int[][]{{x0, z0}, {x1, z0}, {x0, z1}, {x1, z1}}) {
            b.fill(c[0], y + 1, c[1], c[0], y + 4, c[1], Structures.log(Axis.Y));
         }

         b.fill(x0, y + 5, z0, x1, y + 5, z1, (x, yy, z) -> Build.hash(x, yy, z, 422) < 0.15 ? null : Structures.slab(false));
         b.set(x0 + 1, y + 1, z1 - 1, Structures.BRICKS);
         b.set(x0 + 1, y + 2, z1 - 1, Structures.BRICKS);
         b.set(x0 + 2, y + 1, z1 - 1, Blocks.SOUL_CAMPFIRE.defaultBlockState());
         b.set(x0 + 3, y + 1, z1 - 1, Structures.BRICKS);
         b.set(x0 + 3, y + 2, z1 - 1, ModBlocks.BONE_JAR.defaultBlockState());
         b.set(x0 + 2, y + 1, z1, Structures.BRICKS);
         b.fill(x0 + 2, y + 2, z1, x0 + 2, y + 8, z1, Structures.BRICKS);
         b.set(x0 + 4, y + 1, z0 + 2, Structures.facing(ModBlocks.GRAVE_ANVIL, Direction.NORTH));
         b.set(x0 + 3, y + 4, z0 + 2, Structures.lantern(true));
         b.set(x1 - 2, y + 1, z0 + 1, Structures.facing(ModBlocks.ARMOR_RACK, Direction.SOUTH));
         b.set(x1 - 1, y + 1, z0 + 1, Structures.facing(ModBlocks.ARMOR_RACK, Direction.SOUTH));
         b.set(x1 - 1, y + 1, z0 + 2, Structures.facing(ModBlocks.GHOSTWOOD_TABLE, Direction.WEST));
         b.set(x1 - 1, y + 2, z0 + 2, ModBlocks.JAR_KEYS.defaultBlockState());
         b.set(x1 - 1, y + 1, z1 - 1, Structures.facing(ModBlocks.WEAPON_RACK, Direction.NORTH));
         b.set(x1 - 2, y + 1, z1 - 1, Structures.facing(ModBlocks.WEAPON_RACK, Direction.NORTH));
         b.set(x0 + 4, y + 1, z1 - 1, Structures.facing(ModBlocks.BEDROLL, Direction.EAST));
         b.barrel(x0 + 1, y + 1, z0 + 1, Direction.UP, Structures.BARREL_LOOT);
         b.set(x0 + 1, y + 2, z0 + 1, ModBlocks.SOUL_JAR.defaultBlockState());
         b.setLinked(x0 + 4, y + 4, z1 - 1, Structures.chain());
         b.set(x0 + 4, y + 3, z1 - 1, Structures.lantern(true));
      }
   }

   static void apothecary(Build b, int x0, int z0, int x1, int z1, int y) {
      for (int x = x0 + 1; x <= x1 - 1; x++) {
         b.set(x, y, z1 - 1, ModBlocks.CURIO_SHELF.defaultBlockState());
         b.set(x, y + 1, z1 - 1, jarFor(x, z1));
      }

      b.set(x0 + 1, y, z0 + 1, Structures.facing(ModBlocks.GHOSTWOOD_TABLE, Direction.NORTH));
      b.set(x0 + 1, y + 1, z0 + 1, ModBlocks.JAR_HEART.defaultBlockState());
      b.set(x0 + 2, y, z0 + 1, Structures.facing(ModBlocks.GHOSTWOOD_TABLE, Direction.NORTH));
      b.set(x0 + 2, y + 1, z0 + 1, ModBlocks.EYE_JAR.defaultBlockState());
      b.set(x0 + 2, y, z0 + 2, Structures.facing(ModBlocks.GHOSTWOOD_CHAIR, Direction.NORTH));
      b.set(x1 - 1, y, z0 + 1, ModBlocks.CURIO_SHELF.defaultBlockState());
      b.set(x1 - 1, y + 1, z0 + 1, ModBlocks.JAR_MOTH.defaultBlockState());
      b.barrel(x0 + 1, y, z1 - 2, Direction.UP, APOTHECARY_LOOT);
      b.set(x0 + 1, y + 1, z1 - 2, ModBlocks.SOUL_JAR.defaultBlockState());
      b.set(x1 - 1, y + 3, (z0 + z1) / 2, Structures.lantern(true));
   }

   static void tombs(Build b, int cx, int yc, int cz0) {
      String[][] names = new String[][]{
         {"HALDEN", "the first King", "came down", "digging"},
         {"YSOLDE", "the second", "came down", "with a bow"},
         {"THE KING", "", "(empty)", ""},
         {"", "", "", ""}
      };
      int k = 0;

      for (int side = -1; side <= 1; side += 2) {
         int rx = cx + side * 8;

         for (int sx = -2; sx <= 2; sx += 4) {
            String[] n = names[k++];
            sign(b, rx + sx, yc, cz0 - 4, Direction.SOUTH, n);
            if (k == 3) {
               b.fill(rx + sx, yc + 1, cz0 - 7, rx + sx, yc + 1, cz0 - 5, Structures.AIR);
               b.fill(rx + sx, yc, cz0 - 6, rx + sx, yc, cz0 - 6, Structures.AIR);
            }

            if (k == 4) {
               b.fill(rx + sx, yc + 1, cz0 - 7, rx + sx, yc + 1, cz0 - 5, Structures.AIR);
               b.set(rx + sx, yc + 1, cz0 - 5, Structures.candles(1));
            }

            b.set(rx + sx, yc + 1, cz0 - 7, ModBlocks.GRAVE_LAMP.defaultBlockState());
         }
      }
   }

   static void citadelGate(Build b, Layout.Island is) {
      int cx = is.x();
      int y = is.top();
      int z = Puzzles.GATE_Z;
      if (b.overlaps(cx - 4, z - 1, cx + 4, z + 1)) {
         b.fill(cx - 3, y + 1, z, cx + 3, y + 9, z, Structures.SEAL);
         b.set(cx - 2, y + 2, z, Structures.facing(ModBlocks.LOCK_KINGS, Direction.SOUTH));
         b.set(cx, y + 2, z, Structures.facing(ModBlocks.LOCK_WATCHERS, Direction.SOUTH));
         b.set(cx + 2, y + 2, z, Structures.facing(ModBlocks.LOCK_BELL, Direction.SOUTH));
      }
   }

   static void watchers(Build b, Layout.Island is) {
      for (Direction d : Puzzles.WINDOWS) {
         BlockPos w = Puzzles.window(d);
         if (b.in(w.getX(), w.getZ())) {
            b.set(w.getX(), w.getY() - 1, w.getZ(), Structures.BRICKS);
            b.set(w.getX(), w.getY(), w.getZ(), Structures.facing(ModBlocks.WATCHER_SKULL, d.getOpposite()));
         }
      }
   }

   static void sign(Build b, int x, int y, int z, Direction facing, String... lines) {
      if (b.in(x, z)) {
         b.set(x, y, z, Blocks.DARK_OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, facing));
         if (b.level.getBlockEntity(new BlockPos(x, y, z)) instanceof SignBlockEntity sign) {
            List<Component> msgs = new ArrayList<>();

            for (int i = 0; i < 4; i++) {
               msgs.add(Component.literal(i < lines.length ? lines[i] : ""));
            }

            ((SignTextAccess)sign).deathbound$setFrontText(new SignText(msgs, msgs, DyeColor.PURPLE, true));
         }
      }
   }

   static void vault(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      int y = is.top();
      int x0 = cx - 3;
      int x1 = cx + 3;
      int z0 = cz - 2;
      int z1 = cz + 3;
      int floor = y - 11;
      if (b.overlaps(x0 - 1, z0 - 1, x1 + 1, z1 + 1)) {
         b.fill(x0 - 1, floor - 1, z0 - 1, x1 + 1, floor + 5, z1 + 1, Structures.DARK);
         b.fill(x0, floor + 1, z0, x1, floor + 4, z1, Structures.AIR);
         b.fill(x0, floor, z0, x1, floor, z1, Structures.PLANKS);
         b.fill(x0, floor + 5, z0, x1, floor + 5, z1, Structures.log(Axis.X));
         int sx = x1;
         int sz = cz + 2;
         b.set(sx, y, sz, Structures.VEIL);

         for (int yy = floor + 1; yy < y; yy++) {
            b.set(sx + 1, yy, sz, Structures.DARK);
            b.set(sx, yy, sz, Structures.ladder(Direction.WEST));
         }

         for (int x = x0; x < x1; x++) {
            b.set(x, floor + 1, z0, ModBlocks.CURIO_SHELF.defaultBlockState());
            b.set(x, floor + 2, z0, ModBlocks.CURIO_SHELF.defaultBlockState());
            b.set(x, floor + 3, z0, curioFor(x - x0));
         }

         for (int z = z0 + 1; z < z1; z++) {
            b.set(x0, floor + 1, z, ModBlocks.CURIO_SHELF.defaultBlockState());
            b.set(x0, floor + 2, z, curioFor(z - z0 + 3));
         }

         b.set(cx, floor + 1, cz, Structures.facing(ModBlocks.GHOSTWOOD_TABLE, Direction.NORTH));
         b.set(cx, floor + 2, cz, ModBlocks.BOTTLED_SHIP.defaultBlockState());
         b.set(cx + 1, floor + 1, cz, Structures.facing(ModBlocks.GHOSTWOOD_TABLE, Direction.NORTH));
         b.set(cx + 1, floor + 2, cz, ModBlocks.JAR_CROWN.defaultBlockState());
         b.chest(x1, floor + 1, z0 + 1, Direction.WEST, CURIO_LOOT);
         b.set(x1, floor + 1, z1, Structures.facing(ModBlocks.GLOWCAP, Direction.NORTH));
         b.set(x0 + 1, floor + 1, z1, Structures.facing(ModBlocks.GLOWCAP, Direction.EAST));
         b.set(x0 + 1, floor + 1, z0 + 1, Structures.facing(ModBlocks.GLOWCAP, Direction.SOUTH));

         for (int x = x0 + 1; x <= x1 - 1; x += 2) {
            b.set(x, floor + 3, z1, Structures.facing(ModBlocks.WALL_GLOWCAP, Direction.NORTH));
         }

         b.set(x1, floor + 3, cz, Structures.facing(ModBlocks.WALL_GLOWCAP, Direction.WEST));
         b.set(x1, floor + 2, cz - 1, Structures.facing(ModBlocks.WALL_GLOWCAP, Direction.WEST));

         for (int z = z0 + 1; z < z1; z += 2) {
            b.set(x0, floor + 3, z, Structures.facing(ModBlocks.WALL_GLOWCAP, Direction.EAST));
         }

         b.set(cx - 1, floor + 1, z1, Structures.facing(ModBlocks.GLOWCAP, Direction.WEST));
         b.set(x0 + 2, floor + 1, z0 + 1, Structures.facing(ModBlocks.GLOWCAP, Direction.NORTH));
      }
   }

   private static BlockState curioFor(int k) {
      return switch (Math.floorMod(k, 7)) {
         case 0 -> ModBlocks.JAR_KEYS.defaultBlockState();
         case 1 -> ModBlocks.SOUL_JAR.defaultBlockState();
         case 2 -> ModBlocks.JAR_MOTH.defaultBlockState();
         case 3 -> ModBlocks.EYE_JAR.defaultBlockState();
         case 4 -> ModBlocks.JAR_HEART.defaultBlockState();
         case 5 -> Structures.AIR;
         default -> ModBlocks.BONE_JAR.defaultBlockState();
      };
   }

   private static BlockState jarFor(int x, int z) {
      return switch (Math.floorMod(x * 5 + z * 3, 5)) {
         case 0 -> ModBlocks.SOUL_JAR.defaultBlockState();
         case 1 -> ModBlocks.EYE_JAR.defaultBlockState();
         case 2 -> ModBlocks.BONE_JAR.defaultBlockState();
         case 3 -> Structures.AIR;
         default -> ModBlocks.JAR.defaultBlockState();
      };
   }

   static BlockState arrows(int salt) {
      return Structures.facing(ModBlocks.STUCK_ARROWS, Structures.dir(salt));
   }

   static BlockState pierced(int salt) {
      return Structures.facing(ModBlocks.PIERCED_SKULL, Structures.dir(salt));
   }

   static BlockState stake(int salt) {
      return Structures.facing(ModBlocks.HUNTER_STAKE, Structures.dir(salt));
   }

   static double mereShore(int x, int z) {
      double dx = (x - MERE_LX) / 10.5;
      double dz = (z - MERE_LZ) / 8.5;
      return dx * dx + dz * dz + Layout.noise(x * 0.25, z * 0.25, 77) * 0.1;
   }

   static void mere(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      int y = is.top();
      if (b.overlaps(cx - is.radius() - 3, cz - is.radius() - 3, cx + is.radius() + 3, cz + is.radius() + 3)) {
         BlockState water = ModBlocks.SOULWATER.defaultBlockState();

         for (int x = Math.max(MERE_LX - 13, b.x0); x <= Math.min(MERE_LX + 13, b.x1); x++) {
            for (int z = Math.max(MERE_LZ - 11, b.z0); z <= Math.min(MERE_LZ + 11, b.z1); z++) {
               int top = Layout.surface(is, x, z);
               if (top != -2147483648) {
                  double e = mereShore(x, z);
                  double r = Build.hash(x, 0, z, 601);
                  if (e < 1.0) {
                     int depth = 1 + (int)((1.0 - e) * 3.6);
                     b.fill(x, y + 1, z, x, Math.max(top, y) + 2, z, Structures.AIR);
                     b.fill(x, y - depth + 1, z, x, y, z, water);
                     b.set(x, y - depth, z, r < 0.5 ? DRY_BED : Structures.STONE);
                     if (depth >= 3 && r < 0.05) {
                        b.set(x, y - depth, z, ModBlocks.SOUL_VEINED_BRICKS.defaultBlockState());
                     }
                  } else if (e < 1.45) {
                     if (top < y) {
                        b.fill(x, top + 1, z, x, y, z, DRY_BED);
                     }

                     int sy = Math.max(top, y);
                     if (b.get(x, sy + 1, z).isAir() && r < 0.45) {
                        b.set(x, sy + 1, z, r < 0.06 ? Decor.bones((int)(r * 9000.0)) : ModBlocks.GLOOM_GRASS.defaultBlockState());
                     }
                  }
               }
            }
         }

         for (int i = 0; i < 4; i++) {
            double a = 2.4 + i * 1.15;
            double d = 13.0 + Build.hash(i, 0, 0, 603) * 3.0;
            boneTree(b, is, MERE_LX + (int)Math.round(Math.cos(a) * d), MERE_LZ + (int)Math.round(Math.sin(a) * d), i + 7);
         }

         for (int i = 0; i < 26; i++) {
            int x = MERE_LX + (int)Math.round((Build.hash(i, 1, 0, 606) * 2.0 - 1.0) * 10.5);
            int z = MERE_LZ + (int)Math.round((Build.hash(i, 2, 0, 606) * 2.0 - 1.0) * 8.5);
            if (b.in(x, z) && mereShore(x, z) < 0.8 && b.get(x, y + 1, z).isAir()) {
               b.set(x, y + 1, z, ModBlocks.ASHEN_LILY.defaultBlockState());
            }
         }

         workshop(b, y);
      }
   }

   private static void workshop(Build b, int y) {
      int lx = MERE_LX;
      int lz = MERE_LZ;
      if (b.overlaps(lx + 3, lz - 5, lx + 18, lz + 5)) {
         BlockState deck = Structures.DARK;
         BlockState flag = Structures.TILES;

         for (int x = lx + 6; x <= lx + 11; x++) {
            for (int z = lz - 1; z <= lz + 1; z++) {
               b.set(x, y, z, z == lz ? flag : deck);
               b.fill(x, y - 4, z, x, y - 1, z, (xx, yy, zz) -> (xx + zz) % 2 == 0 ? Structures.PILLAR : null);
            }
         }

         for (int z = lz - 2; z <= lz + 2; z++) {
            b.set(lx + 6, y, z, deck);
            b.set(lx + 7, y, z, deck);
         }

         b.set(lx + 6, y, lz, ModBlocks.SOUL_VEINED_BRICKS.defaultBlockState());
         b.set(lx + 7, y + 1, lz + 2, Structures.brazier());
         b.set(lx + 7, y + 1, lz - 2, ModBlocks.URN.defaultBlockState());
         b.fill(lx + 8, y + 1, lz - 2, lx + 8, y + 6, lz - 2, TRUNK);
         b.fill(lx + 3, y + 6, lz - 2, lx + 7, y + 6, lz - 2, TRUNK.setValue(RotatedPillarBlock.AXIS, Axis.X));
         b.set(lx + 7, y + 5, lz - 2, TRUNK.setValue(RotatedPillarBlock.AXIS, Axis.X));
         b.setLinked(lx + 3, y + 5, lz - 2, Structures.chain());
         b.setLinked(lx + 3, y + 4, lz - 2, Structures.chain());
         b.setLinked(lx + 3, y + 3, lz - 2, Structures.chain());
         b.set(lx + 3, y + 2, lz - 2, Structures.facing(ModBlocks.HANGING_BLADE, Direction.WEST));
         b.set(lx + 8, y + 4, lz - 3, Structures.facing(ModBlocks.TATTERED_BANNER, Direction.NORTH));
         int x0 = lx + 11;
         int x1 = lx + 17;
         Structures.footing(b, x0, lz - 4, x1, lz + 4, y);
         b.fill(x0, y, lz - 4, x1, y, lz + 4, (xx, yy, z) -> Build.hash(xx, yy, z, 604) < 0.25 ? Structures.CRACKED : flag);
         b.fill(x0, y + 1, lz - 4, x1 + 2, y + 8, lz + 4, Structures.AIR);
         b.fill(x0, y + 7, lz, x1 + 1, y + 7, lz, TRUNK.setValue(RotatedPillarBlock.AXIS, Axis.X));
         int[][] rib = new int[][]{{4, 1}, {4, 2}, {4, 3}, {4, 4}, {3, 4}, {3, 5}, {2, 5}, {2, 6}, {1, 6}, {1, 7}};

         for (int x = x0 + 1; x <= x1; x += 2) {
            for (int[] r : rib) {
               b.set(x, y + r[1], lz - r[0], TRUNK);
               b.set(x, y + r[1], lz + r[0], TRUNK);
            }
         }

         b.set(x1 + 2, y + 7, lz, Structures.skull(4));
         b.set(x0 + 2, y + 1, lz - 2, Structures.facing(ModBlocks.GRAVE_ANVIL, Direction.WEST));
         b.set(x0 + 4, y + 1, lz - 2, Structures.facing(ModBlocks.WHETSTONE, Direction.NORTH));
         b.set(x0 + 2, y, lz + 2, ModBlocks.SOULWATER.defaultBlockState());
         b.set(x0 + 3, y, lz + 2, ModBlocks.SOULWATER.defaultBlockState());
         b.fill(x0 + 1, y - 1, lz + 1, x0 + 4, y - 1, lz + 3, Structures.STONE);
         b.set(x0 + 1, y, lz + 2, Structures.POLISHED);
         b.set(x0 + 4, y, lz + 2, Structures.POLISHED);
         b.set(x0 + 2, y, lz + 3, Structures.POLISHED);
         b.set(x0 + 3, y, lz + 3, Structures.POLISHED);
         b.set(x1, y + 1, lz - 2, Structures.facing(ModBlocks.WEAPON_RACK, Direction.WEST));
         b.set(x1, y + 1, lz + 2, Structures.facing(ModBlocks.WEAPON_RACK, Direction.WEST));
         b.set(x1, y + 1, lz, Structures.facing(ModBlocks.COFFIN, Direction.WEST));
         b.barrel(x0 + 5, y + 1, lz + 3, Direction.UP, Structures.BARREL_LOOT);
         b.set(x0 + 5, y + 1, lz - 3, Decor.bones(3));
         b.set(x0 + 2, y + 6, lz, ModBlocks.BONE_CHANDELIER.defaultBlockState());
         b.set(x0 + 6, y + 6, lz, ModBlocks.BONE_CHANDELIER.defaultBlockState());
         b.set(x0 + 4, y + 5, lz, Structures.facing(ModBlocks.HANGING_BLADE, Direction.NORTH));

         for (int z : new int[]{lz - 2, lz + 2}) {
            b.fill(x0, y + 1, z, x0, y + 2, z, Structures.log(Axis.Y));
            b.set(x0, y + 3, z, Structures.lantern(false));
         }
      }
   }

   static void hollow(Build b, Layout.Island is) {
      brokenBridge(b);
      int cx = is.x();
      int cz = is.z();
      int y = is.top();
      if (b.overlaps(cx - is.radius() - 4, cz - is.radius() - 4, cx + is.radius() + 4, cz + is.radius() + 4)) {
         for (int i = 0; i < 16; i++) {
            double a = Build.hash(i, 0, 0, 501) * 3.141592653589793 * 2.0;
            double d = 10.0 + Build.hash(i, 1, 0, 501) * (is.radius() - 12);
            boneTree(b, is, cx + (int)Math.round(Math.cos(a) * d), cz + (int)Math.round(Math.sin(a) * d), i);
         }

         for (int i = 0; i < 30; i++) {
            double a = Build.hash(i, 0, 1, 502) * 3.141592653589793 * 2.0;
            double d = Build.hash(i, 1, 1, 502) * (is.radius() - 3);
            int x = cx + (int)Math.round(Math.cos(a) * d);
            int z = cz + (int)Math.round(Math.sin(a) * d);
            int sy = Layout.surface(is, x, z);
            if (sy != -2147483648 && b.in(x, z)) {
               if (i % 4 == 0) {
                  b.set(x, sy + 1, z, Decor.remains(Direction.from2DDataValue(i % 4)));
                  b.set(x + 1, sy + 1, z, arrows(i));
                  b.set(x - 1, sy + 1, z + 1, pierced(i));
               } else {
                  b.set(x, sy + 1, z, arrows(i));
               }
            }
         }

         int px = cx + 9;
         int pz = cz - 7;
         int py = y + 7;
         if (b.overlaps(px - 3, pz - 3, px + 3, pz + 3)) {
            for (int[] c : new int[][]{{px - 2, pz - 2}, {px + 2, pz - 2}, {px - 2, pz + 2}, {px + 2, pz + 2}}) {
               b.fill(c[0], y + 1, c[1], c[0], py + 1, c[1], TRUNK);
            }

            b.fill(px - 2, py, pz - 2, px + 2, py, pz + 2, (xx, yy, zx) -> Math.abs(xx - px) == 2 && Math.abs(zx - pz) == 2 ? TRUNK : Structures.PLANKS);

            for (int yy = y + 1; yy <= py; yy++) {
               b.set(px - 3, yy, pz, Structures.ladder(Direction.WEST));
               b.set(px - 2, yy, pz, TRUNK);
            }

            b.chest(px + 1, py + 1, pz + 1, Direction.WEST, HUNTER_LOOT);
            b.set(px - 1, py + 1, pz - 1, pierced(3));
            b.set(px + 1, py + 1, pz - 1, ModBlocks.EYE_JAR.defaultBlockState());
            b.set(px, py + 1, pz + 1, arrows(5));
         }

         for (int k = 0; k < 7; k++) {
            double a = k * 3.141592653589793 * 2.0 / 7.0 + 0.4;
            int x = cx + (int)Math.round(Math.cos(a) * 8.0);
            int z = cz + (int)Math.round(Math.sin(a) * 8.0);
            int sy = Layout.surface(is, x, z);
            if (sy != -2147483648 && b.in(x, z)) {
               b.set(x, sy + 1, z, stake(k));
            }
         }
      }
   }

   private static void boneTree(Build b, Layout.Island is, int tx, int tz, int salt) {
      int sy = Layout.surface(is, tx, tz);
      if (sy != -2147483648 && b.overlaps(tx - 4, tz - 4, tx + 4, tz + 4)) {
         int h = 6 + (int)(Build.hash(tx, 0, tz, 511) * 6.0);
         b.fill(tx, sy + 1, tz, tx, sy + h, tz, TRUNK);

         for (int k = 0; k < 3; k++) {
            Direction d = Direction.from2DDataValue((salt + k) % 4);
            int by = sy + h - 1 - k * 2;
            int len = 2 + (int)(Build.hash(tx, k, tz, 512) * 2.0);

            for (int i = 1; i <= len; i++) {
               b.set(tx + d.getStepX() * i, by + (i > 1 ? 1 : 0), tz + d.getStepZ() * i, TRUNK.setValue(RotatedPillarBlock.AXIS, d.getAxis()));
            }
         }

         if (salt % 3 == 0) {
            b.set(tx, sy + h + 1, tz, pierced(salt));
         }

         b.set(tx + 1, sy + 1, tz, arrows(salt));
      }
   }

   static void brokenBridge(Build b) {
      Layout.Island w = Layout.WATCH;
      Layout.Island h = Layout.HOLLOW;
      int ax = w.x() - w.radius() + 2;
      int az = w.z();
      int bx = h.x() + h.radius() - 3;
      int bz = h.z() + 6;
      if (b.overlaps(Math.min(ax, bx) - 2, Math.min(az, bz) - 2, Math.max(ax, bx) + 2, Math.max(az, bz) + 2)) {
         int n = Math.abs(ax - bx);

         for (int i = 0; i <= n; i++) {
            double t = (double)i / n;
            int x = ax - i;
            int z = (int)Math.round(az + (bz - az) * t);
            int y = (int)Math.round(w.top() + (h.top() - w.top()) * t) - (int)Math.round(Math.sin(t * 3.141592653589793) * 3.0);
            double gap = Build.hash(x, 0, z, 521);
            boolean missing = t > 0.15 && t < 0.9 && gap < 0.12 + t * 0.12 && i % 4 != 0;
            if (!missing) {
               b.set(x, y, z, Structures.PLANKS);
               if (gap > 0.6) {
                  b.set(x, y, z + 1, Structures.PLANKS);
               }

               if (gap > 0.9) {
                  b.set(x, y + 1, z, arrows(i));
               }
            }

            if (i == n / 2) {
               b.set(x, y, z, Structures.PLANKS);
               b.set(x, y, z + 1, Structures.PLANKS);
               b.chest(x, y + 1, z + 1, Direction.NORTH, QUEST_TAG);
            }

            if (i % 14 == 0) {
               b.set(x, y, z - 1, Structures.PLANKS);
               b.set(x, y + 1, z - 1, stake(i));
            } else if (i % 7 == 0) {
               b.fill(x, y, z - 1, x, y + 1, z - 1, Structures.log(Axis.Y));
               b.set(x, y + 2, z - 1, Structures.lantern(false));
            }
         }
      }
   }

   static void watchTrail(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      if (b.overlaps(cx - is.radius() - 2, cz - 6, cx - 3, cz + 6)) {
         int[][] stakes = new int[][]{{cx - 6, cz - 3}, {cx - 9, cz + 2}, {cx - 12, cz - 1}};

         for (int k = 0; k < stakes.length; k++) {
            int x = stakes[k][0];
            int z = stakes[k][1];
            int sy = Layout.surface(is, x, z);
            if (sy != -2147483648 && b.in(x, z)) {
               b.set(x, sy + 1, z, stake(k + 1));
            }
         }

         for (int i = 0; i < 9; i++) {
            int x = cx - 4 - (int)(Build.hash(i, 0, 2, 541) * (is.radius() - 4));
            int z = cz - 5 + (int)(Build.hash(i, 1, 2, 541) * 10.0);
            int sy = Layout.surface(is, x, z);
            if (sy != -2147483648 && b.in(x, z) && b.get(x, sy + 1, z).isAir()) {
               b.set(x, sy + 1, z, i % 4 == 0 ? Decor.remains(Structures.dir(i)) : arrows(i));
            }
         }
      }
   }

   private static ResourceKey<LootTable> loot(String path) {
      return ResourceKey.create(Registries.LOOT_TABLE, DeathBound.id(path));
   }

   private Landmarks() {
   }
}
