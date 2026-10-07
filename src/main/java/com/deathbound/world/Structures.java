package com.deathbound.world;

import com.deathbound.DeathBound;
import com.deathbound.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootTable;

final class Structures {
   static final ResourceKey<LootTable> ARRIVAL_LOOT = loot("chests/arrival");
   static final ResourceKey<LootTable> RUINS_LOOT = loot("chests/ruins");
   static final ResourceKey<LootTable> HOLLOW_LOOT = loot("chests/hollow_root");
   static final ResourceKey<LootTable> VILLAGE_LOOT = loot("chests/village");
   static final ResourceKey<LootTable> CRYPT_LOOT = loot("chests/crypt");
   static final ResourceKey<LootTable> VAULT_LOOT = loot("chests/crypt_vault");
   static final ResourceKey<LootTable> SPIRE_LOOT = loot("chests/spire");
   static final ResourceKey<LootTable> WATCH_LOOT = loot("chests/watch");
   static final ResourceKey<LootTable> CITADEL_LOOT = loot("chests/citadel");
   static final ResourceKey<LootTable> CAMP_LOOT = loot("chests/death_camp");
   static final ResourceKey<LootTable> BARREL_LOOT = loot("chests/house_barrel");
   static final ResourceKey<LootTable> TOWER_LOOT = loot("chests/tower");
   static final BlockState AIR = Blocks.AIR.defaultBlockState();
   static final BlockState STONE = ModBlocks.SOULSTONE.defaultBlockState();
   static final BlockState BRICKS = ModBlocks.SOULSTONE_BRICKS.defaultBlockState();
   static final BlockState CRACKED = ModBlocks.CRACKED_SOULSTONE_BRICKS.defaultBlockState();
   static final BlockState CHISELED = ModBlocks.CHISELED_SOULSTONE.defaultBlockState();
   static final BlockState TILES = ModBlocks.SOULSTONE_TILES.defaultBlockState();
   static final BlockState PILLAR = ModBlocks.SOULSTONE_PILLAR.defaultBlockState();
   static final BlockState WALL = ModBlocks.SOULSTONE_BRICK_WALL.defaultBlockState();
   static final BlockState VEIL = ModBlocks.VEILED_SOULSTONE.defaultBlockState();
   static final BlockState PLANKS = ModBlocks.GHOSTWOOD_PLANKS.defaultBlockState();
   static final BlockState SEAL = ModBlocks.SOUL_SEAL.defaultBlockState();
   static final BlockState ROOF = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
   static final BlockState ROOF_DARK = Blocks.BLACKSTONE.defaultBlockState();
   static final BlockState GLASS = Blocks.STAINED_GLASS_PANE.pick(DyeColor.PURPLE).defaultBlockState();
   static final BlockState TEAR = Blocks.CRYING_OBSIDIAN.defaultBlockState();
   static final BlockState BONE = Blocks.BONE_BLOCK.defaultBlockState();
   static final BlockState WEB = Blocks.COBWEB.defaultBlockState();
   static final BlockState DARK = ModBlocks.DARK_SOULSTONE_BRICKS.defaultBlockState();
   static final BlockState POLISHED = ModBlocks.POLISHED_SOULSTONE.defaultBlockState();
   static final BlockState VEINED = ModBlocks.SOUL_VEINED_BRICKS.defaultBlockState();
   static final Build.Pick WORN = Structures::worn;

   private static ResourceKey<LootTable> loot(String path) {
      return ResourceKey.create(Registries.LOOT_TABLE, DeathBound.id(path));
   }

   static BlockState worn(int x, int y, int z) {
      double r = Build.hash(x, y, z, 101);
      return r < 0.16 ? CRACKED : (r < 0.21 ? STONE : (r < 0.27 ? DARK : (r < 0.29 ? VEINED : BRICKS)));
   }

   static Build.Pick wall(int base) {
      return (x, y, z) -> {
         int rel = y - base;
         if (rel <= 1) {
            return Build.hash(x, y, z, 104) < 0.25 ? CRACKED : DARK;
         } else {
            return rel % 7 == 0 ? POLISHED : worn(x, y, z);
         }
      };
   }

   static BlockState facing(Block block, Direction d) {
      return block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, d);
   }

   static Direction dir(int i) {
      return Direction.from2DDataValue(i & 3);
   }

   static boolean isProp(BlockState s) {
      return s.is(ModBlocks.GLOOM_GRASS)
         || s.is(ModBlocks.SMALL_SOUL_SHARD)
         || s.is(ModBlocks.MEDIUM_SOUL_SHARD)
         || s.is(ModBlocks.LARGE_SOUL_SHARD)
         || s.is(ModBlocks.REMAINS)
         || s.is(ModBlocks.BONE_PILE)
         || s.is(ModBlocks.SKULL_SPIKE)
         || s.is(ModBlocks.SLUMPED_REMAINS);
   }

   static void onGround(Build b, Layout.Island is, int x, int z, BlockState s) {
      if (b.in(x, z)) {
         int y = Layout.surface(is, x, z);
         if (y != -2147483648) {
            while (!b.get(x, y + 1, z).isAir() && !isProp(b.get(x, y + 1, z)) && y < is.top() + 4) {
               y++;
            }

            b.set(x, y + 1, z, s);
         }
      }
   }

   static void hang(Build b, int x, int yTop, int z, int links, BlockState thing) {
      for (int i = 1; i <= links; i++) {
         b.setLinked(x, yTop - i, z, chain());
      }

      b.set(x, yTop - links - 1, z, thing);
   }

   static BlockState brazier() {
      return ModBlocks.SOUL_BRAZIER.defaultBlockState();
   }

   static Build.Pick ruined(int base, double perBlock) {
      return (x, y, z) -> Build.hash(x, y, z, 202) < (y - base) * perBlock ? null : worn(x, y, z);
   }

   static BlockState log(Axis axis) {
      return ModBlocks.GHOSTWOOD_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
   }

   static BlockState stairs(Direction facing, boolean top) {
      return ModBlocks.SOULSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM);
   }

   static BlockState slab(boolean top) {
      return ModBlocks.SOULSTONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
   }

   static BlockState lantern(boolean hanging) {
      return ModBlocks.WRAITH_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, hanging);
   }

   static BlockState crystal(Direction facing) {
      return ModBlocks.SOUL_CRYSTAL.defaultBlockState().setValue(AmethystClusterBlock.FACING, facing);
   }

   static BlockState candles(int count) {
      return Blocks.DYED_CANDLE.pick(DyeColor.PURPLE).defaultBlockState().setValue(CandleBlock.CANDLES, count).setValue(CandleBlock.LIT, true);
   }

   static BlockState skull(int rotation) {
      return facing(ModBlocks.SKULL, Direction.from2DDataValue(((rotation & 15) + 2) / 4 % 4));
   }

   static BlockState ladder(Direction facing) {
      return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, facing);
   }

   static BlockState chain() {
      return Blocks.IRON_CHAIN.defaultBlockState();
   }

   static void build(Build b, Layout.Island is) {
      switch (is.theme()) {
         case ARRIVAL:
            arrival(b, is);
            Landmarks.landing(b, is);
            break;
         case HUB:
            hub(b, is);
            Landmarks.crossing(b, is);
            break;
         case FOREST:
            forest(b, is);
            break;
         case VILLAGE:
            village(b, is);
            Landmarks.forge(b, is);
            break;
         case CRYPT:
            crypt(b, is);
            break;
         case SPIRE:
            spire(b, is);
            Landmarks.vault(b, is);
            break;
         case WATCH:
            watch(b, is);
            Landmarks.watchTrail(b, is);
            break;
         case GATE:
            gate(b, is);
            break;
         case CITADEL:
            Landmarks.lake(b, is);
            citadel(b, is);
            break;
         case HOLLOW:
            Landmarks.hollow(b, is);
            break;
         case MERE:
            Landmarks.mere(b, is);
            break;
         case DEBRIS:
            debris(b, is);
      }

      Decor.decorate(b, is);
   }

   static void path(Build b, Layout.Island is, int ax, int az, int bx, int bz, Build.Pick pick) {
      for (int x = Math.max(Math.min(ax, bx), b.x0); x <= Math.min(Math.max(ax, bx), b.x1); x++) {
         for (int z = Math.max(Math.min(az, bz), b.z0); z <= Math.min(Math.max(az, bz), b.z1); z++) {
            int y = Layout.surface(is, x, z);
            if (y != -2147483648) {
               BlockState s = pick.at(x, y, z);
               if (s != null) {
                  b.set(x, y, z, s);
                  if (isProp(b.get(x, y + 1, z))) {
                     b.set(x, y + 1, z, AIR);
                  }
               }
            }
         }
      }
   }

   static void footing(Build b, int ax, int az, int bx, int bz, int y) {
      b.fill(ax, y - 4, az, bx, y - 1, bz, STONE);
   }

   static void arrival(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      int y = is.top();
      if (b.overlaps(cx - 20, cz - 20, cx + 20, cz + 20)) {
         footing(b, cx - 6, cz - 6, cx + 6, cz + 6, y);
         b.cylinder(cx, cz, 6.5, 0.0, y, y, (x, yy, z) -> {
            double d = Math.hypot(x - cx, z - cz);
            if (d > 5.6) {
               return BRICKS;
            }

            double r = Build.hash(x, yy, z, 61);
            return r < 0.08 ? CHISELED : (r < 0.3 ? CRACKED : TILES);
         });
         b.cylinder(cx, cz, 6.5, 0.0, y + 1, y + 2, (x, yy, z) -> AIR);
         b.fill(cx - 1, y + 1, cz - 1, cx + 1, y + 1, cz + 1, BRICKS);
         b.set(cx, y + 1, cz, TEAR);
         b.set(cx, y + 2, cz, crystal(Direction.UP));

         for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
               b.set(cx + dx, y + 2, cz + dz, candles(2 + (dx + dz + 2) / 2 % 3));
            }
         }

         for (int k = 0; k < 9; k++) {
            double a = k * 3.141592653589793 * 2.0 / 9.0 + 0.35;
            int px = cx + (int)Math.round(Math.cos(a) * 11.0);
            int pz = cz + (int)Math.round(Math.sin(a) * 11.0);
            if ((pz >= cz - 7 || Math.abs(px - cx) >= 4) && !Landmarks.inLandingRiver(px, pz)) {
               int base = Layout.surface(is, px, pz);
               if (base != -2147483648) {
                  double roll = Build.hash(px, 0, pz, 31);
                  int h = 3 + (int)(roll * 5.0);
                  b.fill(px, base + 1, pz, px, base + h, pz, PILLAR);
                  if (roll > 0.45) {
                     b.set(px, base + h + 1, pz, CHISELED);
                     b.set(px, base + h + 2, pz, lantern(false));
                  } else {
                     b.set(px + 1, base + 1, pz, CRACKED);
                     b.set(px, base + 1, pz + 1, slab(false));
                  }
               }
            }
         }

         b.chest(cx - 3, y + 1, cz + 2, Direction.EAST, ARRIVAL_LOOT);
         b.set(cx - 3, y + 1, cz + 3, candles(3));
         path(b, is, cx - 1, cz - 7, cx + 1, cz - is.radius() - 2, (x, yy, z) -> Build.hash(x, yy, z, 5) < 0.18 ? null : TILES);
         int oy = Layout.surface(is, cx - 4, cz - 13);
         if (oy != -2147483648) {
            b.fill(cx - 4, oy + 1, cz - 13, cx - 4, oy + 6, cz - 13, PILLAR);
            b.set(cx - 4, oy + 7, cz - 13, CHISELED);
            b.set(cx - 4, oy + 8, cz - 13, skull(8));
         }
      }
   }

   static void hub(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      int y = is.top();
      if (b.overlaps(cx - 30, cz - 30, cx + 30, cz + 30)) {
         footing(b, cx - 8, cz - 8, cx + 8, cz + 8, y);
         b.fill(cx - 8, y, cz - 8, cx + 8, y, cz + 8, (xx, yy, zx) -> {
            boolean edge = Math.abs(xx - cx) == 8 || Math.abs(zx - cz) == 8;
            double r = Build.hash(xx, yy, zx, 9);
            return edge ? WORN.at(xx, yy, zx) : (r < 0.12 ? ModBlocks.ASHEN_SOIL.defaultBlockState() : (r < 0.3 ? CRACKED : TILES));
         });
         b.fill(cx - 8, y + 1, cz - 8, cx + 8, y + 2, cz + 8, AIR);
         int[][] cols = new int[][]{
            {-8, -8}, {-4, -8}, {4, -8}, {8, -8}, {-8, 8}, {-4, 8}, {4, 8}, {8, 8}, {-8, -4}, {-8, 0}, {-8, 4}, {8, -4}, {8, 0}, {8, 4}
         };

         for (int[] c : cols) {
            int x = cx + c[0];
            int z = cz + c[1];
            double roll = Build.hash(x, 1, z, 13);
            boolean intact = roll > 0.35;
            int h = intact ? 7 : 2 + (int)(roll * 8.0);
            b.set(x, y + 1, z, BRICKS);
            b.fill(x, y + 2, z, x, y + 1 + h, z, PILLAR);
            if (intact) {
               b.set(x, y + 2 + h, z, CHISELED);
               if (roll > 0.8) {
                  b.set(x, y + 3 + h, z, lantern(false));
               }
            } else {
               b.set(x + (roll > 0.2 ? 1 : -1), y + 1, z, CRACKED);
               b.set(x, y + 1, z + (roll > 0.25 ? 1 : -1), slab(false));
            }
         }

         b.fill(cx - 8, y + 10, cz + 8, cx + 8, y + 10, cz + 8, (xx, yy, zx) -> Build.hash(xx, yy, zx, 17) < 0.4 ? null : WORN.at(xx, yy, zx));
         b.fill(cx - 1, y + 1, cz - 1, cx + 1, y + 1, cz + 1, BRICKS);
         b.set(cx, y + 2, cz, CHISELED);
         b.set(cx, y + 3, cz, skull(0));
         b.set(cx - 1, y + 2, cz - 1, candles(3));
         b.set(cx + 1, y + 2, cz + 1, candles(2));
         b.chest(cx, y + 1, cz + 3, Direction.SOUTH, RUINS_LOOT);

         for (int i = 0; i < 22; i++) {
            int x = cx + (int)((Build.hash(i, 0, 0, 41) - 0.5) * 2.0 * (is.radius() - 4));
            int z = cz + (int)((Build.hash(i, 1, 0, 41) - 0.5) * 2.0 * (is.radius() - 4));
            if ((Math.abs(x - cx) > 9 || Math.abs(z - cz) > 9) && Math.abs(x - cx) > 3 && !Landmarks.inCrossingRiver(x, z)) {
               int sy = Layout.surface(is, x, z);
               if (sy != -2147483648) {
                  if (i % 3 == 0) {
                     b.set(x, sy + 1, z, Decor.bones(i));
                     b.set(x + 1, sy + 1, z, Decor.remains(dir(i)));
                     if (i % 2 == 0) {
                        b.set(x - 1, sy + 1, z, skull(i % 16));
                     }
                  } else {
                     b.set(x, sy + 1, z, i % 2 == 0 ? CRACKED : BRICKS);
                     b.set(x, sy + 2, z, i % 4 == 1 ? CHISELED : slab(false));
                     b.set(x, sy, z - 1, ModBlocks.ASHEN_SOIL.defaultBlockState());
                     if (i % 5 == 0) {
                        b.set(x, sy + 1, z - 1, candles(1));
                     }
                  }
               }
            }
         }

         b.chest(cx + 16, Layout.surface(is, cx + 16, cz + 5) + 1, cz + 5, Direction.WEST, RUINS_LOOT);
         path(b, is, cx - 1, cz - 9, cx + 1, cz - is.radius() - 2, (xx, yy, zx) -> Build.hash(xx, yy, zx, 6) < 0.15 ? null : TILES);
         path(b, is, cx - 1, cz + 9, cx + 1, cz + is.radius() + 2, (xx, yy, zx) -> Build.hash(xx, yy, zx, 7) < 0.25 ? null : TILES);
      }
   }

   static void boatShed(Build b, Layout.Island is) {
      int x0 = is.x() + 6;
      int z0 = is.z() + 15;
      int y = Layout.surface(is, x0 + 1, z0 + 1);
      if (y != -2147483648 && b.overlaps(x0 - 1, z0 - 1, x0 + 4, z0 + 4)) {
         b.fill(x0, y, z0, x0 + 3, y, z0 + 3, PLANKS);
         b.fill(x0, y + 1, z0, x0 + 3, y + 3, z0 + 3, AIR);

         for (int[] c : new int[][]{{x0, z0}, {x0 + 3, z0}, {x0, z0 + 3}}) {
            b.fill(c[0], y + 1, c[1], c[0], y + 2, c[1], log(Axis.Y));
         }

         b.fill(x0, y + 3, z0, x0 + 3, y + 3, z0 + 1, (x, yy, z) -> Build.hash(x, yy, z, 731) < 0.3 ? null : PLANKS);
         b.set(x0 + 3, y + 1, z0 + 3, slab(false));
         b.chest(x0 + 1, y + 1, z0 + 2, Direction.SOUTH, Landmarks.QUEST_OAR);
         b.set(x0 + 2, y + 1, z0 + 1, ModBlocks.COFFIN.defaultBlockState());
         b.set(x0 + 1, y + 1, z0, lantern(false));
      }
   }

   static void forest(Build b, Layout.Island is) {
      boatShed(b, is);
      if (b.overlaps(is.x() - 36, is.z() - 36, is.x() + 36, is.z() + 36)) {
         for (int gx = -3; gx <= 3; gx++) {
            for (int gz = -3; gz <= 3; gz++) {
               int tx = is.x() + gx * 8 + (int)((Build.hash(gx, 0, gz, 51) - 0.5) * 6.0);
               int tz = is.z() + gz * 8 + (int)((Build.hash(gx, 1, gz, 51) - 0.5) * 6.0);
               if (gx == 0 && gz == 0) {
                  ancientTree(b, is, is.x(), is.z());
               } else if (Math.hypot(tx - is.x(), tz - is.z()) < is.radius() * 0.78
                  && Build.hash(gx, 2, gz, 51) < 0.8
                  && Math.hypot(tx - is.x(), tz - is.z()) > 6.0) {
                  deadTree(b, is, tx, tz, gx * 31 + gz);
               }
            }
         }
      }
   }

   static void deadTree(Build b, Layout.Island is, int tx, int tz, int salt) {
      if (b.overlaps(tx - 6, tz - 6, tx + 6, tz + 6)) {
         int sy = Layout.surface(is, tx, tz);
         if (sy != -2147483648) {
            int h = 6 + (int)(Build.hash(tx, 0, tz, salt) * 7.0);
            b.fill(tx, sy + 1, tz, tx, sy + h, tz, log(Axis.Y));
            Direction[] dirs = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

            for (Direction d : dirs) {
               if (Build.hash(tx, d.ordinal(), tz, salt + 1) < 0.5) {
                  b.set(tx + d.getStepX(), sy + 1, tz + d.getStepZ(), log(d.getAxis()));
               }
            }

            int branches = 2 + (int)(Build.hash(tx, 5, tz, salt) * 3.0);

            for (int i = 0; i < branches; i++) {
               Direction d = dirs[(int)(Build.hash(tx, 10 + i, tz, salt) * 4.0)];
               int by = sy + 3 + i * 2 + (int)(Build.hash(tx, 20 + i, tz, salt) * 2.0);
               if (by > sy + h) {
                  break;
               }

               int len = 2 + (int)(Build.hash(tx, 30 + i, tz, salt) * 3.0);
               int x = tx;
               int z = tz;

               for (int k = 1; k <= len; k++) {
                  x += d.getStepX();
                  z += d.getStepZ();
                  b.set(x, by, z, log(d.getAxis()));
               }

               b.set(x, by + 1, z, log(Axis.Y));
               double tip = Build.hash(tx, 40 + i, tz, salt);
               if (tip < 0.25) {
                  b.set(x, by + 2, z, WEB);
               } else if (tip < 0.45) {
                  b.setLinked(x, by - 1, z, chain());
                  b.set(x, by - 2, z, lantern(true));
               }
            }

            b.set(tx + 1, sy + h + 1, tz, log(Axis.Y));
         }
      }
   }

   static void ancientTree(Build b, Layout.Island is, int tx, int tz) {
      if (b.overlaps(tx - 9, tz - 9, tx + 9, tz + 12)) {
         int sy = is.top();
         footing(b, tx - 2, tz - 2, tx + 2, tz + 2, sy);
         b.fill(tx - 1, sy + 1, tz - 1, tx + 1, sy + 16, tz + 1, log(Axis.Y));

         for (Direction d : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
            int ox = tx + d.getStepX() * 2;
            int oz = tz + d.getStepZ() * 2;
            b.set(ox, sy + 1, oz, log(d.getAxis()));
            b.set(ox + d.getStepX(), sy + 1, oz + d.getStepZ(), log(d.getAxis()));
            int ly = sy + 9 + d.ordinal() * 2;

            for (int k = 2; k <= 6; k++) {
               b.set(tx + d.getStepX() * k, ly + k / 3, tz + d.getStepZ() * k, log(d.getAxis()));
            }

            b.setLinked(tx + d.getStepX() * 6, ly + 1, tz + d.getStepZ() * 6, chain());
            b.set(tx + d.getStepX() * 6, ly, tz + d.getStepZ() * 6, lantern(true));
         }

         int vz = tz + 3;
         b.set(tx, sy, vz, VEIL);
         b.fill(tx, sy - 1, vz, tx, sy - 6, vz, AIR);
         b.fill(tx, sy - 6, vz, tx, sy - 1, vz, (x, y, z) -> ladder(Direction.SOUTH));
         b.fill(tx - 3, sy - 7, vz + 1, tx + 3, sy - 7, vz + 7, TILES);
         b.fill(tx - 3, sy - 6, vz + 1, tx + 3, sy - 4, vz + 7, AIR);
         b.fill(tx - 3, sy - 3, vz + 1, tx + 3, sy - 3, vz + 7, log(Axis.X));
         b.chest(tx, sy - 6, vz + 6, Direction.NORTH, HOLLOW_LOOT);
         b.set(tx - 2, sy - 6, vz + 6, candles(4));
         b.set(tx + 2, sy - 6, vz + 2, POLISHED);
         b.set(tx + 2, sy - 5, vz + 2, skull(4));
         b.set(tx - 1, sy + 1, vz, skull(8));
         b.set(tx + 1, sy + 1, vz, candles(1));
      }
   }

   static void village(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      if (b.overlaps(cx - 44, cz - 44, cx + 44, cz + 44)) {
         int[][] houses = new int[][]{
            {-15, -12, 7, 6},
            {2, -18, 8, 6},
            {14, -6, 6, 7},
            {-18, 5, 7, 6},
            {5, 11, 8, 6},
            {-5, 17, 6, 5},
            {-29, -9, 6, 6},
            {18, -24, 7, 6},
            {25, 2, 6, 6},
            {-27, 15, 7, 6},
            {-14, -27, 7, 5},
            {26, -12, 6, 5}
         };

         for (int i = 0; i < houses.length; i++) {
            int[] h = houses[i];
            house(b, is, cx + h[0], cz + h[1], h[2], h[3], i);
         }

         int y = is.top();
         footing(b, cx - 2, cz - 2, cx + 2, cz + 2, y);
         b.shell(cx - 2, y + 1, cz - 2, cx + 2, y + 1, cz + 2, WORN);
         b.fill(cx - 1, y - 4, cz - 1, cx + 1, y, cz + 1, AIR);
         b.fill(cx - 1, y - 5, cz - 1, cx + 1, y - 5, cz + 1, TEAR);
         b.set(cx, y - 4, cz, crystal(Direction.UP));

         for (int dx = -2; dx <= 2; dx += 4) {
            for (int dz = -2; dz <= 2; dz += 4) {
               b.fill(cx + dx, y + 2, cz + dz, cx + dx, y + 3, cz + dz, PILLAR);
            }
         }

         b.fill(cx - 2, y + 4, cz - 2, cx + 2, y + 4, cz + 2, (x, yy, z) -> slab(false));
         b.setLinked(cx, y + 3, cz, chain());
         b.set(cx, y + 2, cz, lantern(true));
         int bx = cx + 18;
         int bz = cz + 14;
         int by = Layout.surface(is, bx, bz);
         if (by != -2147483648 && b.overlaps(bx - 2, bz - 2, bx + 2, bz + 2)) {
            footing(b, bx - 1, bz - 1, bx + 1, bz + 1, by + 1);
            b.fill(bx - 1, by + 1, bz - 1, bx + 1, by + 9, bz + 1, WORN);
            b.fill(bx - 1, by + 10, bz - 1, bx + 1, by + 12, bz + 1, (x, yy, z) -> Math.abs(x - bx) == 1 && Math.abs(z - bz) == 1 ? PILLAR : AIR);
            b.fill(bx - 1, by + 13, bz - 1, bx + 1, by + 13, bz + 1, WORN);
            b.set(bx, by + 12, bz, facing(ModBlocks.GRAVE_BELL, Direction.SOUTH));

            for (int yy = by + 1; yy <= by + 9; yy++) {
               b.set(bx, yy, bz + 2, ladder(Direction.SOUTH));
            }

            b.set(bx, by + 14, bz, skull(0));
         }

         path(b, is, cx - 1, cz - 30, cx + 1, cz + 30, (x, yy, z) -> Build.hash(x, yy, z, 61) < 0.3 ? null : TILES);
         path(b, is, cx - 30, cz - 1, cx + 30, cz + 1, (x, yy, z) -> Build.hash(x, yy, z, 62) < 0.3 ? null : TILES);

         for (int k = 5; k <= 29; k += 6) {
            for (int[] d : new int[][]{{2, k}, {-2, -k}, {k, -2}, {-k, 2}}) {
               lampPost(b, is, cx + d[0], cz + d[1]);
            }
         }

         market(b, is, cx + 4, cz - 12);
         chapel(b, is, cx - 24, cz - 21);
         fountain(b, is, cx + 1, cz + 27);
      }
   }

   static void lampPost(Build b, Layout.Island is, int x, int z) {
      int y = Layout.surface(is, x, z);
      if (y != -2147483648 && b.in(x, z) && b.get(x, y, z) != PLANKS && (b.get(x, y + 1, z).isAir() || isProp(b.get(x, y + 1, z)))) {
         b.fill(x, y + 1, z, x, y + 2, z, log(Axis.Y));
         b.set(x, y + 3, z, lantern(false));
      }
   }

   static void market(Build b, Layout.Island is, int x0, int z0) {
      int y = is.top();
      if (b.overlaps(x0 - 1, z0 - 1, x0 + 10, z0 + 5)) {
         b.fill(x0, y, z0, x0 + 9, y, z0 + 4, (x, yy, z) -> Build.hash(x, yy, z, 91) < 0.2 ? CRACKED : TILES);
         b.fill(x0, y + 1, z0, x0 + 9, y + 4, z0 + 4, AIR);

         for (int s = 0; s < 3; s++) {
            int sx = x0 + s * 3 + 1;
            b.fill(sx - 1, y + 1, z0, sx - 1, y + 2, z0, log(Axis.Y));
            b.fill(sx + 1, y + 1, z0, sx + 1, y + 2, z0, log(Axis.Y));
            b.fill(sx - 1, y + 3, z0, sx + 1, y + 3, z0 + 1, (x, yy, z) -> slab(false));
            b.set(sx, y + 1, z0 + 1, facing(ModBlocks.GHOSTWOOD_TABLE, Direction.NORTH));

            BlockState goods = switch (s) {
               case 0 -> ModBlocks.SOUL_JAR.defaultBlockState();
               case 1 -> ModBlocks.URN.defaultBlockState();
               default -> ModBlocks.BONE_JAR.defaultBlockState();
            };
            b.set(sx, y + 2, z0 + 1, goods);
            if (s == 1) {
               b.barrel(sx - 1, y + 1, z0 + 2, Direction.UP, BARREL_LOOT);
            } else {
               b.set(sx - 1, y + 1, z0 + 2, ModBlocks.CURIO_SHELF.defaultBlockState());
            }
         }

         b.set(x0 + 9, y + 1, z0 + 3, Decor.remains(Direction.WEST));
      }
   }

   static void chapel(Build b, Layout.Island is, int x0, int z0) {
      int x1 = x0 + 6;
      int z1 = z0 + 8;
      int y = is.top();
      if (b.overlaps(x0 - 1, z0 - 1, x1 + 1, z1 + 1) && Layout.surface(is, x0 + 3, z0 + 4) != -2147483648) {
         footing(b, x0, z0, x1, z1, y);
         b.fill(x0, y, z0, x1, y, z1, TILES);
         b.fill(x0, y + 1, z0, x1, y + 7, z1, (x, yy, zx) -> {
            boolean wall = x == x0 || x == x1 || zx == z0 || zx == z1;
            int peak = yy - (y + 5);
            if (peak >= 0) {
               int inset = peak + 1;
               return x != x0 + inset && x != x1 - inset ? (x > x0 + inset && x < x1 - inset ? AIR : null) : DARK;
            } else {
               return wall ? (x != x0 && x != x1 || zx != z0 && zx != z1 ? WORN.at(x, yy, zx) : PILLAR) : AIR;
            }
         });
         b.fill(x0 + 3, y + 1, z1, x0 + 3, y + 3, z1, AIR);
         b.set(x0 + 3, y + 4, z1, GLASS);

         for (int z = z0 + 2; z <= z1 - 2; z += 2) {
            b.set(x0 + 1, y + 1, z, facing(ModBlocks.COFFIN, Direction.EAST));
            b.set(x1 - 1, y + 1, z, facing(ModBlocks.COFFIN, Direction.WEST));
         }

         b.set(x0 + 3, y + 1, z0 + 1, facing(ModBlocks.GHOSTWOOD_TABLE, Direction.SOUTH));
         b.set(x0 + 3, y + 2, z0 + 1, ModBlocks.SKULL.defaultBlockState());
         b.set(x0 + 2, y + 1, z0 + 1, ModBlocks.URN.defaultBlockState());
         b.set(x0 + 4, y + 1, z0 + 1, ModBlocks.URN.defaultBlockState());
         b.set(x0 + 2, y + 2, z0 + 1, candles(3));
         b.set(x0 + 4, y + 2, z0 + 1, candles(2));
         b.set(x0 + 3, y + 5, z0 + 4, ModBlocks.BONE_CHANDELIER.defaultBlockState());
         b.set(x0 + 3, y + 3, z0, facing(ModBlocks.TATTERED_BANNER, Direction.SOUTH));
      }
   }

   static void fountain(Build b, Layout.Island is, int cx, int cz) {
      int y = is.top();
      if (b.overlaps(cx - 7, cz - 4, cx + 7, cz + 4) && Layout.surface(is, cx, cz) != -2147483648) {
         for (int x = cx - 3; x <= cx + 3; x++) {
            for (int z = cz - 3; z <= cz + 3; z++) {
               double d = Math.hypot(x - cx, z - cz);
               if (!(d > 3.4)) {
                  b.set(x, y - 1, z, STONE);
                  b.fill(x, y + 1, z, x, y + 3, z, AIR);
                  if (d > 2.4) {
                     b.set(x, y, z, POLISHED);
                     b.set(x, y + 1, z, slab(false));
                  } else {
                     b.set(x, y, z, ModBlocks.SOULWATER.defaultBlockState());
                  }
               }
            }
         }

         b.set(cx, y, cz, PILLAR);
         b.set(cx, y + 1, cz, PILLAR);
         b.set(cx, y + 2, cz, skull(8));
         int sx = cx + 6;
         b.fill(sx, y + 1, cz - 2, sx, y + 4, cz - 2, Landmarks.TRUNK);
         b.fill(sx, y + 1, cz + 2, sx, y + 4, cz + 2, Landmarks.TRUNK);
         b.fill(sx, y + 5, cz - 2, sx, y + 5, cz + 2, Landmarks.TRUNK.setValue(RotatedPillarBlock.AXIS, Axis.Z));
         b.setLinked(sx, y + 4, cz, chain());
         b.setLinked(sx, y + 3, cz, chain());
         b.set(sx, y + 2, cz, slab(false));

         for (int k = 0; k < 6; k++) {
            b.set(cx - 6, y, cz - 3 + k, k % 2 == 0 ? VEINED : TILES);
         }
      }
   }

   static void house(Build b, Layout.Island is, int x0, int z0, int w, int d, int index) {
      int x1 = x0 + w - 1;
      int z1 = z0 + d - 1;
      if (b.overlaps(x0 - 1, z0 - 1, x1 + 1, z1 + 1)) {
         int fy = is.top();
         footing(b, x0, z0, x1, z1, fy);
         b.fill(x0, fy, z0, x1, fy, z1, PLANKS);
         boolean roofed = index % 3 != 1;
         b.fill(x0, fy + 1, z0, x1, fy + 4, z1, (x, y, z) -> {
            boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
            if (!edge) {
               return AIR;
            } else {
               boolean corner = (x == x0 || x == x1) && (z == z0 || z == z1);
               if (Build.hash(x, y, z, 70 + index) < (y - fy - 1) * (roofed ? 0.08 : 0.2)) {
                  return AIR;
               } else if (corner) {
                  return log(Axis.Y);
               } else {
                  return y == fy + 1 ? WORN.at(x, y, z) : PLANKS;
               }
            }
         });
         int mx = (x0 + x1) / 2;
         int mz = (z0 + z1) / 2;
         b.set(mx, fy + 2, z0, GLASS);
         b.set(mx, fy + 3, z0, GLASS);
         b.set(x0, fy + 2, mz, AIR);
         b.set(x1, fy + 3, mz, AIR);
         int cx = is.x();
         int cz = is.z();
         if (Math.abs(mx - cx) > Math.abs(mz - cz)) {
            int dx = mx > cx ? x0 : x1;
            b.fill(dx, fy + 1, mz, dx, fy + 2, mz, AIR);
         } else {
            int dz = mz > cz ? z0 : z1;
            b.fill(mx, fy + 1, dz, mx, fy + 2, dz, AIR);
         }

         if (roofed) {
            b.fill(x0, fy + 5, z0, x1, fy + 5, z1, (x, y, z) -> Build.hash(x, y, z, 80 + index) < 0.35 ? null : slab(false));
            b.fill(x1 - 1, fy + 5, z1 - 1, x1 - 1, fy + 7, z1 - 1, WORN);
         }

         b.set(x0 + 1, fy + 1, z0 + 1, PLANKS);
         b.set(x0 + 1, fy + 2, z0 + 1, candles(1 + index % 3));
         b.set(x1 - 1, fy + 4, z0 + 1, WEB);
         if (index % 2 == 0) {
            b.chest(x1 - 1, fy + 1, z1 - 1, Direction.WEST, VILLAGE_LOOT);
         }

         furnish(b, x0, z0, x1, z1, fy + 1, index);
      }
   }

   private static void furnish(Build b, int x0, int z0, int x1, int z1, int y, int index) {
      if (index == 3) {
         Landmarks.apothecary(b, x0, z0, x1, z1, y);
      } else {
         switch (index % 3) {
            case 0:
               b.set(x0 + 1, y, z1 - 1, ModBlocks.CURIO_SHELF.defaultBlockState());
               b.set(x0 + 1, y + 1, z1 - 1, ModBlocks.BONE_JAR.defaultBlockState());
               b.set(x0 + 2, y, z1 - 1, ModBlocks.CURIO_SHELF.defaultBlockState());
               b.set(x0 + 2, y + 1, z1 - 1, ModBlocks.JAR.defaultBlockState());
               int tx = (x0 + x1) / 2;
               int tz = (z0 + z1) / 2;
               b.set(tx, y, tz, facing(ModBlocks.GHOSTWOOD_TABLE, Direction.NORTH));
               b.set(tx, y + 1, tz, facing(ModBlocks.BONE_CANDELABRA, Direction.NORTH));
               b.set(tx - 1, y, tz, facing(ModBlocks.GHOSTWOOD_CHAIR, Direction.EAST));
               b.set(tx + 1, y, tz, facing(ModBlocks.GHOSTWOOD_CHAIR, Direction.WEST));
               b.set(x1 - 1, y, z0 + 2, ModBlocks.SOUL_JAR.defaultBlockState());
               b.barrel(x0 + 1, y, z0 + 2, Direction.UP, BARREL_LOOT);
               b.set(x0 + 1, y + 1, z0 + 2, ModBlocks.JAR_MOTH.defaultBlockState());
               break;
            case 1:
               b.set(x1 - 1, y, z1 - 2, facing(ModBlocks.BEDROLL, Direction.SOUTH));
               b.set(x0 + 1, y, z1 - 1, ModBlocks.OSSUARY_SHELF.defaultBlockState());
               b.set(x0 + 1, y + 1, z1 - 1, ModBlocks.CURIO_SHELF.defaultBlockState());
               b.set(x0 + 1, y + 2, z1 - 1, ModBlocks.JAR_KEYS.defaultBlockState());
               b.set(x0 + 2, y, z1 - 1, ModBlocks.OSSUARY_SHELF.defaultBlockState());
               b.barrel(x1 - 2, y, z0 + 1, Direction.UP, BARREL_LOOT);
               b.set(x0 + 1, y, z0 + 2, facing(ModBlocks.BONE_CANDELABRA, Direction.EAST));
               b.set(x1 - 1, y, z1 - 3, facing(ModBlocks.GLOWCAP, dir(index)));
               break;
            default:
               b.set(x0 + 1, y, z1 - 1, facing(ModBlocks.GRAVE_ANVIL, Direction.EAST));
               b.set(x0 + 3, y, z1 - 1, facing(ModBlocks.ARMOR_RACK, Direction.NORTH));
               b.set(x0 + 2, y, z1 - 1, ModBlocks.OSSUARY_SHELF.defaultBlockState());
               b.set(x1 - 1, y, z0 + 2, facing(ModBlocks.GHOSTWOOD_TABLE, Direction.WEST));
               b.set(x1 - 1, y + 1, z0 + 2, ModBlocks.JAR_KEYS.defaultBlockState());
               b.barrel(x1 - 1, y, z0 + 3, Direction.WEST, BARREL_LOOT);
               b.set(x0 + 2, y, z0 + 2, facing(ModBlocks.GHOSTWOOD_TABLE, Direction.NORTH));
               b.set(x0 + 3, y, z0 + 2, facing(ModBlocks.GHOSTWOOD_CHAIR, Direction.WEST));
         }
      }
   }

   static void crypt(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      int y = is.top();
      if (b.overlaps(cx - 22, cz - 22, cx + 22, cz + 22)) {
         int mz0 = cz + 2;
         int mz1 = cz + 14;
         footing(b, cx - 5, mz0, cx + 5, mz1, y);
         b.fill(cx - 5, y, mz0, cx + 5, y, mz1, TILES);
         b.fill(cx - 5, y + 1, mz0, cx + 5, y + 7, mz1, (x, yy, zx) -> {
            boolean edge = x == cx - 5 || x == cx + 5 || zx == mz0 || zx == mz1;
            return edge ? WORN.at(x, yy, zx) : AIR;
         });

         for (int[] c : new int[][]{{-6, mz0 - 1}, {6, mz0 - 1}, {-6, mz1 + 1}, {6, mz1 + 1}}) {
            b.fill(cx + c[0], y + 1, c[1], cx + c[0], y + 9, c[1], PILLAR);
            b.set(cx + c[0], y + 10, c[1], CHISELED);
         }

         for (int k = 0; k <= 5; k++) {
            b.fill(cx - 5 + k, y + 8 + k, mz0, cx + 5 - k, y + 8 + k, mz1, k == 5 ? CHISELED : ROOF);
         }

         b.fill(cx - 1, y + 1, mz1, cx + 1, y + 4, mz1, AIR);
         b.set(cx, y + 5, mz1, CHISELED);
         b.set(cx, y + 6, mz1 + 1, skull(0));
         b.set(cx - 2, y + 1, mz1 + 1, candles(3));
         b.set(cx + 2, y + 1, mz1 + 1, candles(2));
         b.set(cx, y + 6, mz1 - 1, lantern(true));
         int depth = 10;
         int yc = y - depth + 1;
         int cz0 = mz1 - 3 - depth;
         b.fill(cx - 13, yc - 1, cz0 - 2, cx + 13, yc + 4, cz0 + 2, WORN);
         b.fill(cx - 12, yc, cz0 - 1, cx + 12, yc + 3, cz0 + 1, AIR);
         b.fill(cx - 12, yc - 1, cz0 - 1, cx + 12, yc - 1, cz0 + 1, TILES);

         for (int x = cx - 11; x <= cx + 11; x += 3) {
            b.set(x, yc + 1, cz0 - 2, AIR);
            b.set(x, yc + 1, cz0 - 2, skull(0));
            b.set(x, yc + 1, cz0 + 2, AIR);
            b.set(x, yc + 1, cz0 + 2, skull(8));
         }

         b.set(cx, yc + 3, cz0, lantern(true));

         for (int side = -1; side <= 1; side += 2) {
            int rx = cx + side * 8;
            b.fill(rx - 4, yc - 1, cz0 - 9, rx + 4, yc + 4, cz0 - 2, WORN);
            b.fill(rx - 3, yc, cz0 - 8, rx + 3, yc + 3, cz0 - 2, AIR);
            b.fill(rx - 3, yc - 1, cz0 - 8, rx + 3, yc - 1, cz0 - 3, TILES);
            b.fill(rx - 1, yc, cz0 - 2, rx + 1, yc + 2, cz0 - 2, AIR);

            for (int sx = -2; sx <= 2; sx += 4) {
               b.fill(rx + sx, yc, cz0 - 7, rx + sx, yc, cz0 - 5, BRICKS);
               b.fill(rx + sx, yc + 1, cz0 - 7, rx + sx, yc + 1, cz0 - 5, slab(false));
            }

            b.set(rx - 2, yc + 1, cz0 - 5, candles(2));
            if (side > 0) {
               b.setLinked(rx - 1, yc + 3, cz0 - 5, chain());
               b.setLinked(rx + 1, yc + 3, cz0 - 5, chain());
               b.set(rx - 1, yc, cz0 - 4, candles(3));
               b.set(rx + 1, yc, cz0 - 4, candles(2));
               b.set(rx, yc, cz0 - 3, candles(4));
            }

            b.chest(rx, yc, cz0 - 8, Direction.SOUTH, CRYPT_LOOT);

            for (int sx = -1; sx <= 1; sx += 2) {
               b.set(rx + sx * 3, yc, cz0 - 8, ModBlocks.OSSUARY_SHELF.defaultBlockState());
               b.set(rx + sx * 3, yc + 1, cz0 - 8, ModBlocks.OSSUARY_SHELF.defaultBlockState());
            }

            b.set(rx + 3, yc + 3, cz0 - 8, WEB);
         }

         Landmarks.tombs(b, cx, yc, cz0);
         b.fill(cx + 13, yc, cz0 - 1, cx + 13, yc + 2, cz0 + 1, VEIL);
         b.fill(cx + 14, yc - 1, cz0 - 4, cx + 20, yc + 4, cz0 + 4, WORN);
         b.fill(cx + 14, yc, cz0 - 3, cx + 19, yc + 3, cz0 + 3, AIR);
         b.fill(cx + 14, yc - 1, cz0 - 3, cx + 19, yc - 1, cz0 + 3, (x, yy, zx) -> (x + zx) % 3 == 0 ? TEAR : TILES);
         b.chest(cx + 19, yc, cz0 - 2, Direction.WEST, VAULT_LOOT);
         b.chest(cx + 19, yc, cz0 + 2, Direction.WEST, VAULT_LOOT);
         b.set(cx + 19, yc, cz0, CHISELED);
         b.set(cx + 19, yc + 1, cz0, skull(4));
         b.set(cx + 15, yc, cz0 - 3, candles(4));
         b.set(cx + 15, yc, cz0 + 3, candles(3));

         for (int k = 0; k < depth; k++) {
            int z = mz1 - 3 - k;
            b.fill(cx - 2, y - k, z, cx - 2, y - k + 4, z, WORN);
            b.fill(cx + 2, y - k, z, cx + 2, y - k + 4, z, WORN);
            b.fill(cx - 1, y - k + 1, z, cx + 1, y - k + 4, z, AIR);
            b.fill(cx - 1, y - k, z, cx + 1, y - k, z, stairs(Direction.SOUTH, false));
         }
      }
   }

   static void spire(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      int y = is.top();
      Build.Pick shell = wall(y);
      if (b.overlaps(cx - 9, cz - 9, cx + 9, cz + 9)) {
         footing(b, cx - 6, cz - 6, cx + 6, cz + 6, y);
         b.cylinder(cx, cz, 6.5, 0.0, y, y, (x, yy, z) -> TILES);

         for (int yy = y + 1; yy <= y + 44; yy++) {
            double r = yy <= y + 20 ? 5.5 : (yy <= y + 32 ? 4.5 : 3.5);
            int level = yy;
            b.cylinder(cx, cz, r, r - 1.15, yy, yy, shell);
            b.cylinder(cx, cz, r - 1.15, 0.0, yy, yy, (x, q, z) -> AIR);
            if (level == y + 10 || level == y + 20 || level == y + 32 || level == y + 40) {
               b.cylinder(cx, cz, r - 1.0, 0.0, yy, yy, (x, q, z) -> BRICKS);
            }
         }

         for (int k = 0; k < 4; k++) {
            double a = 0.7853981633974483 + k * 3.141592653589793 / 2.0;
            int rx = cx + (int)Math.round(Math.cos(a) * 5.6);
            int rz = cz + (int)Math.round(Math.sin(a) * 5.6);
            b.fill(rx, y + 1, rz, rx, y + 16, rz, PILLAR);
            b.set(rx, y + 17, rz, CHISELED);
         }

         for (int wy : new int[]{y + 6, y + 16, y + 26, y + 37}) {
            int r = wy <= y + 20 ? 5 : (wy <= y + 32 ? 4 : 3);

            for (Direction d : new Direction[]{Direction.EAST, Direction.WEST, Direction.SOUTH}) {
               int wx = cx + d.getStepX() * r;
               int wz = cz + d.getStepZ() * r;
               b.setLinked(wx, wy, wz, GLASS);
               b.setLinked(wx, wy + 1, wz, GLASS);
            }
         }

         b.fill(cx - 1, y + 1, cz + 5, cx + 1, y + 3, cz + 5, AIR);
         b.fill(cx - 1, y + 1, cz + 4, cx + 1, y + 3, cz + 4, AIR);
         spiralStair(b, cx, cz, y);
         b.chest(cx + 2, y + 41, cz, Direction.WEST, SPIRE_LOOT);

         for (Direction d : new Direction[]{Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH}) {
            b.fill(cx + d.getStepX() * 3, y + 42, cz + d.getStepZ() * 3, cx + d.getStepX() * 3, y + 43, cz + d.getStepZ() * 3, AIR);
         }

         b.cylinder(cx, cz, 3.5, 0.0, y + 45, y + 45, (x, yy, z) -> BRICKS);

         for (int k = 0; k < 6; k++) {
            double a = k * 3.141592653589793 / 3.0;
            int sx = cx + (int)Math.round(Math.cos(a) * 3.0);
            int sz = cz + (int)Math.round(Math.sin(a) * 3.0);
            int h = 3 + (int)(Build.hash(sx, 0, sz, 91) * 4.0);
            b.fill(sx, y + 46, sz, sx, y + 45 + h, sz, PILLAR);
            b.set(sx, y + 46 + h, sz, CHISELED);
         }

         b.fill(cx, y + 46, cz, cx, y + 55, cz, PILLAR);
         b.set(cx, y + 56, cz, TEAR);
         b.set(cx, y + 57, cz, crystal(Direction.UP));
         Landmarks.watchers(b, is);
      }
   }

   static void spiralStair(Build b, int cx, int cz, int y) {
      int[][] ring = new int[][]{{1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};
      int top = 39;
      b.fill(cx, y + 1, cz, cx, y + top, cz, PILLAR);

      for (int k = 0; k <= top; k++) {
         int[] p = ring[k % 8];
         int[] prev = ring[(k + 7) % 8];
         int sx = cx + p[0];
         int sz = cz + p[1];
         int h = y + 1 + k;
         int dx = p[0] - prev[0];
         int dz = p[1] - prev[1];
         Direction up = dx > 0 ? Direction.EAST : (dx < 0 ? Direction.WEST : (dz > 0 ? Direction.SOUTH : Direction.NORTH));
         b.set(sx, h, sz, stairs(up, false));
         b.fill(sx, h + 1, sz, sx, h + 3, sz, AIR);
      }

      for (int f : new int[]{y + 10, y + 20, y + 32}) {
         b.set(cx - 2, f - 1, cz + 2, lantern(true));
      }
   }

   static void wallStair(Build b, int cx, int cz, int y, int top) {
      int[][] ring = new int[][]{
         {2, -1}, {2, -2}, {1, -2}, {0, -2}, {-1, -2}, {-2, -2}, {-2, -1}, {-2, 0}, {-2, 1}, {-2, 2}, {-1, 2}, {0, 2}, {1, 2}, {2, 2}, {2, 1}, {2, 0}
      };

      for (int k = 0; k <= top; k++) {
         int[] p = ring[k % 16];
         int[] prev = ring[(k + 15) % 16];
         int sx = cx + p[0];
         int sz = cz + p[1];
         int h = y + 1 + k;
         int dx = p[0] - prev[0];
         int dz = p[1] - prev[1];
         Direction up = dx > 0 ? Direction.EAST : (dx < 0 ? Direction.WEST : (dz > 0 ? Direction.SOUTH : Direction.NORTH));
         b.set(sx, h, sz, stairs(up, false));
         if (k >= 3) {
            b.set(sx, h - 1, sz, stairs(up.getOpposite(), true));
         }

         b.fill(sx, h + 1, sz, sx, h + 3, sz, AIR);
      }
   }

   static void watch(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      int y = is.top();
      if (b.overlaps(cx - 8, cz - 8, cx + 8, cz + 8)) {
         footing(b, cx - 3, cz - 3, cx + 3, cz + 3, y);
         b.fill(cx - 3, y, cz - 3, cx + 3, y, cz + 3, TILES);
         Build.Pick wall = ruined(y + 20, 0.09);
         b.fill(cx - 3, y + 1, cz - 3, cx + 3, y + 27, cz + 3, (x, yy, z) -> {
            boolean edge = Math.abs(x - cx) == 3 || Math.abs(z - cz) == 3;
            boolean corner = Math.abs(x - cx) == 3 && Math.abs(z - cz) == 3;
            if (edge) {
               BlockState s = wall.at(x, yy, z);
               return s == null ? AIR : (corner ? PILLAR : s);
            } else {
               return yy != y + 8 && yy != y + 16 && yy != y + 24 ? AIR : PLANKS;
            }
         });
         b.fill(cx + 3, y + 1, cz, cx + 3, y + 2, cz, AIR);

         for (int wy : new int[]{y + 5, y + 13, y + 21}) {
            b.set(cx - 3, wy, cz, AIR);
            b.set(cx, wy, cz + 3, AIR);
         }

         wallStair(b, cx, cz, y, 23);
         b.set(cx, y + 1, cz, facing(ModBlocks.GHOSTWOOD_TABLE, Direction.NORTH));
         b.set(cx, y + 2, cz, candles(2));
         b.set(cx - 1, y + 1, cz, facing(ModBlocks.GHOSTWOOD_CHAIR, Direction.EAST));
         b.set(cx - 1, y + 1, cz + 2, facing(ModBlocks.WEAPON_RACK, Direction.NORTH));
         b.set(cx + 1, y + 1, cz + 2, facing(ModBlocks.ARMOR_RACK, Direction.NORTH));
         b.barrel(cx - 2, y + 1, cz + 2, Direction.UP, BARREL_LOOT);
         b.set(cx + 1, y + 9, cz - 1, facing(ModBlocks.BEDROLL, Direction.SOUTH));
         b.set(cx + 1, y + 9, cz + 1, facing(ModBlocks.BEDROLL, Direction.NORTH));
         b.set(cx, y + 9, cz + 1, ModBlocks.FARE_BOWL.defaultBlockState());
         b.set(cx, y + 9, cz - 2, ModBlocks.OSSUARY_SHELF.defaultBlockState());
         b.set(cx, y + 10, cz - 2, ModBlocks.JAR_MOTH.defaultBlockState());
         b.set(cx + 2, y + 9, cz - 2, candles(1));
         b.set(cx + 1, y + 17, cz - 2, facing(ModBlocks.WEAPON_RACK, Direction.SOUTH));
         b.set(cx + 2, y + 17, cz - 2, facing(ModBlocks.ARMOR_RACK, Direction.WEST));
         b.barrel(cx + 2, y + 17, cz - 1, Direction.UP, BARREL_LOOT);
         b.set(cx, y + 17, cz, facing(ModBlocks.GHOSTWOOD_TABLE, Direction.NORTH));
         b.set(cx, y + 18, cz, ModBlocks.JAR_KEYS.defaultBlockState());
         b.set(cx + 1, y + 17, cz + 1, facing(ModBlocks.SLUMPED_REMAINS, Direction.WEST));
         b.set(cx, y + 25, cz, brazier());
         b.set(cx - 1, y + 25, cz + 1, facing(ModBlocks.GHOSTWOOD_CHAIR, Direction.WEST));
         b.chest(cx + 1, y + 25, cz + 1, Direction.WEST, WATCH_LOOT);
         b.set(cx + 1, y + 25, cz - 1, Landmarks.arrows(3));
         b.set(cx + 2, y + 25, cz + 2, Landmarks.arrows(5));
         b.set(cx - 1, y + 25, cz + 2, Landmarks.arrows(8));
         b.set(cx + 2, y + 25, cz + 1, lantern(false));

         for (int f : new int[]{y + 8, y + 16, y + 24}) {
            b.set(cx, f - 1, cz, lantern(true));
         }

         b.fill(cx - 4, y + 22, cz, cx - 7, y + 22, cz, WORN);
         b.setLinked(cx - 7, y + 21, cz, chain());
         b.setLinked(cx - 7, y + 20, cz, chain());
         b.set(cx - 7, y + 19, cz, lantern(true));

         for (int i = 0; i < 8; i++) {
            int x = cx + 4 + (int)(Build.hash(i, 0, 1, 93) * 4.0);
            int z = cz - 3 + (int)(Build.hash(i, 1, 1, 93) * 7.0);
            int sy = Layout.surface(is, x, z);
            if (sy != -2147483648) {
               b.set(x, sy + 1, z, i % 3 == 0 ? slab(false) : CRACKED);
            }
         }
      }
   }

   static void gate(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      int y = is.top();
      int dz = Layout.DOOR_Z;
      if (b.overlaps(cx - 36, dz - 8, cx + 36, cz + 36)) {
         path(b, is, cx - 5, cz + is.radius() + 2, cx + 5, dz + 2, (x, yy, z) -> Math.abs(x - cx) == 5 ? WORN.at(x, yy, z) : (z % 4 == 0 ? CHISELED : TILES));
         footing(b, cx - 5, dz + 2, cx + 5, cz + 20, y);

         for (int z = cz + 22; z >= dz + 8; z -= 8) {
            statue(b, cx - 9, y, z, 12);
            statue(b, cx + 9, y, z, 4);
         }

         footing(b, cx - 18, dz - 5, cx + 18, dz + 5, y + 1);
         b.fill(cx - 16, y, dz - 1, cx + 16, y + 30, dz + 1, (x, yy, z) -> {
            if (yy == y) {
               return BRICKS;
            } else if (Layout.inDoorway(x - cx, yy - y - 1)) {
               return z == dz ? SEAL : AIR;
            } else {
               return wall(y).at(x, yy, z);
            }
         });
         b.fill(cx - 7, y + 1, dz + 2, cx + 7, y + 24, dz + 2, (x, yy, z) -> {
            int rel = yy - y - 1;
            boolean open = Layout.inDoorway(x - cx, rel);
            boolean rim = !open && (Layout.inDoorway(x - cx + 1, rel) || Layout.inDoorway(x - cx - 1, rel) || Layout.inDoorway(x - cx, rel - 1));
            return rim ? CHISELED : null;
         });

         for (int x = cx - 16; x <= cx + 16; x += 2) {
            b.set(x, y + 31, dz - 1, BRICKS);
            b.set(x, y + 31, dz + 1, BRICKS);
         }

         String[] skull = new String[]{"...B...", "BBBBBBB", "B..B..B", "B..B..B", "T..B..T", "TT.B.TT", "..BBB.."};

         for (int r = 0; r < skull.length; r++) {
            for (int c = 0; c < 7; c++) {
               char ch = skull[r].charAt(c);
               int x = cx - 3 + c;
               int yy = y + 29 - r;

               BlockState s = switch (ch) {
                  case 'B' -> POLISHED;
                  case 'K' -> ROOF_DARK;
                  case 'T' -> TEAR;
                  default -> null;
               };
               if (s != null) {
                  b.set(x, yy, dz + 2, s);
               }
            }
         }

         for (int side = -1; side <= 1; side += 2) {
            int tx0 = cx + side * 9;
            int tx1 = cx + side * 17;
            b.fill(Math.min(tx0, tx1), y, dz - 4, Math.max(tx0, tx1), y + 44, dz + 4, (x, yy, z) -> {
               boolean edge = x == tx0 || x == tx1 || z == dz - 4 || z == dz + 4;
               boolean corner = (x == tx0 || x == tx1) && (z == dz - 4 || z == dz + 4);
               if (yy == y || yy == y + 15 || yy == y + 30) {
                  return BRICKS;
               } else if (!edge) {
                  return AIR;
               } else {
                  return corner ? PILLAR : wall(y).at(x, yy, z);
               }
            });
            int mid = cx + side * 13;

            for (int wy : new int[]{y + 10, y + 24, y + 38}) {
               b.setLinked(mid, wy, dz + 4, GLASS);
               b.setLinked(mid, wy + 1, dz + 4, GLASS);
               b.setLinked(mid, wy + 2, dz + 4, GLASS);
               b.set(mid, wy + 2, dz, lantern(false));
            }

            for (int x = Math.min(tx0, tx1); x <= Math.max(tx0, tx1); x += 2) {
               b.set(x, y + 45, dz - 4, BRICKS);
               b.set(x, y + 45, dz + 4, BRICKS);
            }

            for (int[] c : new int[][]{{tx0, dz - 4}, {tx1, dz - 4}, {tx0, dz + 4}, {tx1, dz + 4}}) {
               b.fill(c[0], y + 45, c[1], c[0], y + 50, c[1], PILLAR);
               b.set(c[0], y + 51, c[1], CHISELED);
            }

            b.fill(mid, y + 45, dz, mid, y + 54, dz, PILLAR);
            b.set(mid, y + 55, dz, TEAR);
            int bx = cx + side * 6;
            b.fill(bx, y + 1, dz + 3, bx, y + 2, dz + 3, PILLAR);
            b.set(bx, y + 3, dz + 3, CHISELED);
            b.set(bx, y + 4, dz + 3, crystal(Direction.UP));
            b.set(bx, y + 3, dz + 4, crystal(Direction.SOUTH));
            int lx = cx + side * 5;
            b.fill(lx, y + 1, dz - 2, lx, y + 17, dz - 6, (x, yy, z) -> (yy - y) % 5 == 0 ? ROOF_DARK : PLANKS);
         }

         b.fill(cx - 5, y, dz - 2, cx + 5, y, dz - 8, TILES);
      }
   }

   static void statue(Build b, int x, int y, int z, int facing) {
      if (b.overlaps(x - 2, z - 2, x + 2, z + 2)) {
         b.fill(x - 1, y - 3, z - 1, x + 1, y, z + 1, STONE);
         b.fill(x - 1, y + 1, z - 1, x + 1, y + 1, z + 1, BRICKS);
         b.set(x, y + 2, z, CHISELED);
         b.fill(x, y + 3, z, x, y + 5, z, PILLAR);
         b.set(x, y + 6, z, CHISELED);
         b.set(x, y + 7, z, skull(facing));
         b.set(x - 1, y + 2, z - 1, candles(2));
         b.set(x + 1, y + 2, z + 1, candles(1));
         b.set(x, y + 5, z - 1, stairs(Direction.SOUTH, true));
         b.set(x, y + 5, z + 1, stairs(Direction.NORTH, true));
      }
   }

   static void citadel(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      int y = is.top();
      if (b.overlaps(cx - 42, cz - 42, cx + 42, cz + 44)) {
         b.cylinder(cx, cz, 34.0, 0.0, y - 4, y - 1, (x, yy, z) -> STONE);
         b.cylinder(cx, cz, 34.0, 0.0, y, y, (x, yy, z) -> {
            double dx = Math.hypot(x - cx, z - cz);
            double a = Math.atan2(z - cz, x - cx);
            double spoke = Math.abs(Math.sin(a * 4.0));
            if (dx < 3.0 || Math.abs(dx - 11.0) < 0.6 || Math.abs(dx - 21.0) < 0.6) {
               return CHISELED;
            } else if (spoke < 0.06 && dx > 3.0 && dx < 21.0) {
               return TEAR;
            } else {
               return dx > 28.0 ? WORN.at(x, yy, z) : TILES;
            }
         });
         b.cylinder(cx, cz, 34.0, 0.0, y + 1, y + 3, (x, yy, z) -> AIR);
         b.cylinder(cx, cz, 33.6, 31.0, y + 1, y + 15, (x, yy, z) -> {
            if (z > cz && Math.abs(x - cx) <= 3 && yy <= y + 9) {
               return AIR;
            } else if (yy == y + 15) {
               double a = Math.atan2(z - cz, x - cx);
               return (int)Math.floor(a * 20.0) % 2 == 0 ? WORN.at(x, yy, z) : AIR;
            } else {
               return wall(y).at(x, yy, z);
            }
         });

         for (int k = 0; k < 12; k++) {
            double a = k * 3.141592653589793 / 6.0 + 0.2617993877991494;
            int bx = cx + (int)Math.round(Math.cos(a) * 30.0);
            int bz = cz + (int)Math.round(Math.sin(a) * 30.0);
            Direction face = Math.abs(Math.cos(a)) > Math.abs(Math.sin(a))
               ? (Math.cos(a) > 0.0 ? Direction.WEST : Direction.EAST)
               : (Math.sin(a) > 0.0 ? Direction.NORTH : Direction.SOUTH);
            b.set(bx, y + 11, bz, Blocks.WALL_BANNER.pick(DyeColor.PURPLE).defaultBlockState().setValue(WallBannerBlock.FACING, face));
         }

         for (int side = -1; side <= 1; side += 2) {
            b.fill(cx + side * 4, y + 1, cz + 31, cx + side * 4, y + 12, cz + 34, PILLAR);
            b.set(cx + side * 4, y + 13, cz + 34, CHISELED);
            b.set(cx + side * 4, y + 14, cz + 34, skull(0));
         }

         b.fill(cx - 3, y + 10, cz + 31, cx + 3, y + 10, cz + 34, CHISELED);
         path(b, is, cx - 2, cz + 34, cx + 2, cz + 46, (x, yy, z) -> TILES);

         for (double deg : new double[]{45.0, 135.0, 195.0, 345.0, 245.0, 295.0}) {
            double a = Math.toRadians(deg);
            int tx = cx + (int)Math.round(Math.cos(a) * 33.0);
            int tz = cz + (int)Math.round(Math.sin(a) * 33.0);
            tower(b, tx, tz, y);
            towerRooms(b, tx, tz, y, -Math.cos(a), -Math.sin(a));
         }

         for (int k = 0; k < 8; k++) {
            double a = k * 3.141592653589793 / 4.0 + 0.39269908169872414;
            int px = cx + (int)Math.round(Math.cos(a) * 24.0);
            int pz = cz + (int)Math.round(Math.sin(a) * 24.0);
            b.set(px, y + 1, pz, BRICKS);
            b.fill(px, y + 2, pz, px, y + 9, pz, PILLAR);
            b.set(px, y + 10, pz, CHISELED);
            b.set(px, y + 11, pz, crystal(Direction.UP));
         }

         for (BlockPos p : Layout.ANCHORS) {
            b.fill(p.getX() - 1, y + 1, p.getZ() - 1, p.getX() + 1, y + 1, p.getZ() + 1, BRICKS);
            b.fill(p.getX(), y + 2, p.getZ(), p.getX(), y + 3, p.getZ(), PILLAR);
            b.set(p.getX(), y + 4, p.getZ(), TEAR);

            for (Direction d : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
               b.set(p.getX() + d.getStepX(), y + 2, p.getZ() + d.getStepZ(), crystal(d));
            }
         }

         throne(b, cx, cz - 24, y);
         Landmarks.citadelGate(b, is);
         b.chest(cx - 5, y + 2, cz - 28, Direction.EAST, CITADEL_LOOT);
         b.chest(cx + 5, y + 2, cz - 28, Direction.WEST, CITADEL_LOOT);
         b.fill(cx - 4, y + 4, cz - 32, cx + 4, y + 13, cz - 32, (x, yy, z) -> {
            int rel = yy - y - 4;
            int half = rel < 6 ? 4 : 4 - (rel - 5);
            return Math.abs(x - cx) <= half ? GLASS : null;
         });
      }
   }

   static void tower(Build b, int tx, int tz, int y) {
      if (b.overlaps(tx - 7, tz - 7, tx + 7, tz + 7)) {
         footing(b, tx - 5, tz - 5, tx + 5, tz + 5, y + 1);
         b.cylinder(tx, tz, 5.5, 0.0, y, y, (x, yy, z) -> BRICKS);
         b.cylinder(tx, tz, 5.5, 4.4, y + 1, y + 26, wall(y));
         b.cylinder(tx, tz, 4.4, 0.0, y + 1, y + 26, (x, yy, z) -> yy == y + 13 ? BRICKS : AIR);

         for (int k = 0; k < 7; k++) {
            double r = 6.2 - k * 0.9;
            b.cylinder(tx, tz, r, Math.max(0.0, r - 1.2), y + 27 + k, y + 27 + k, (x, yy, z) -> Build.hash(x, yy, z, 3) < 0.25 ? ROOF_DARK : ROOF);
         }

         b.fill(tx, y + 27, tz, tx, y + 36, tz, PILLAR);
         b.set(tx, y + 37, tz, TEAR);

         for (int wy : new int[]{y + 7, y + 19}) {
            for (Direction d : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
               b.setLinked(tx + d.getStepX() * 5, wy, tz + d.getStepZ() * 5, GLASS);
               b.setLinked(tx + d.getStepX() * 5, wy + 1, tz + d.getStepZ() * 5, GLASS);
            }

            b.set(tx, wy + 3, tz, lantern(true));
            b.setLinked(tx, wy + 4, tz, chain());
         }
      }
   }

   static void towerRooms(Build b, int tx, int tz, int y, double inX, double inZ) {
      if (b.overlaps(tx - 6, tz - 6, tx + 6, tz + 6)) {
         Direction in = Math.abs(inX) > Math.abs(inZ) ? (inX > 0.0 ? Direction.EAST : Direction.WEST) : (inZ > 0.0 ? Direction.SOUTH : Direction.NORTH);
         Direction out = in.getOpposite();
         Direction side = in.getClockWise();
         int ix = in.getStepX();
         int iz = in.getStepZ();
         int sx = side.getStepX();
         int sz = side.getStepZ();
         b.fill(tx + ix * 5, y + 1, tz + iz * 5, tx + ix * 4, y + 2, tz + iz * 4, AIR);
         b.set(tx + ix * 5, y + 3, tz + iz * 5, CHISELED);

         for (int yy = y + 1; yy <= y + 13; yy++) {
            b.set(tx - ix * 3, yy, tz - iz * 3, ladder(in));
            b.set(tx - ix * 4, yy, tz - iz * 4, BRICKS);
         }

         b.set(tx + sx * 3, y + 1, tz + sz * 3, Decor.slumped(side.getOpposite()));
         b.set(tx - sx * 3, y + 1, tz - sz * 3, Decor.remains(out));
         b.set(tx - sx * 2 + ix * 2, y + 1, tz - sz * 2 + iz * 2, Decor.bones(tx + tz));
         b.set(tx, y + 1, tz, brazier());
         b.chest(tx + sx * 3 - ix, y + 1, tz + sz * 3 - iz, side.getOpposite(), TOWER_LOOT);
         b.barrel(tx - sx * 3 - ix * 2, y + 1, tz - sz * 3 - iz * 2, Direction.UP, BARREL_LOOT);
         b.set(tx + sx * 2 - ix * 2, y + 1, tz + sz * 2 - iz * 2, facing(ModBlocks.SKULL_SPIKE, in));
         b.set(tx + sx * 3, y + 4, tz + sz * 3, WEB);
         b.set(tx + sx * 2, y + 14, tz + sz * 2, Decor.slumped(side.getOpposite()));
         b.setLinked(tx + sx * 3, y + 15, tz + sz * 3, chain());
         hang(b, tx - sx * 2, y + 26, tz - sz * 2, 4, Decor.gibbet(tx));
         b.set(tx + ix * 2, y + 14, tz + iz * 2, facing(ModBlocks.GHOSTWOOD_TABLE, side));
         b.set(tx + ix * 2, y + 15, tz + iz * 2, candles(3));
         b.set(tx + ix * 2 + sx, y + 14, tz + iz * 2 + sz, ModBlocks.JAR_HEART.defaultBlockState());
         b.chest(tx - ix * 2 + sx * 2, y + 14, tz - iz * 2 + sz * 2, in, TOWER_LOOT);
         b.set(tx, y + 17, tz, WEB);
      }
   }

   static void throne(Build b, int x, int z, int y) {
      b.fill(x - 8, y + 1, z - 6, x + 8, y + 1, z + 6, WORN);
      b.fill(x - 6, y + 2, z - 5, x + 6, y + 2, z + 4, WORN);
      b.fill(x - 4, y + 3, z - 4, x + 4, y + 3, z + 2, BRICKS);
      b.fill(x - 8, y + 1, z + 7, x + 8, y + 1, z + 7, stairs(Direction.NORTH, false));
      b.fill(x - 6, y + 2, z + 5, x + 6, y + 2, z + 5, stairs(Direction.NORTH, false));
      b.fill(x - 4, y + 3, z + 3, x + 4, y + 3, z + 3, stairs(Direction.NORTH, false));
      b.fill(x - 1, y + 4, z - 1, x + 1, y + 4, z + 1, slab(false));

      for (int side = -1; side <= 1; side += 2) {
         b.fill(x + side * 2, y + 4, z - 1, x + side * 2, y + 5, z + 1, BRICKS);
         b.set(x + side * 2, y + 6, z + 1, skull(0));
         b.set(x + side * 2, y + 6, z - 1, CHISELED);
      }

      b.fill(x - 2, y + 4, z - 2, x + 2, y + 12, z - 2, WORN);
      b.fill(x - 1, y + 4, z - 3, x + 1, y + 13, z - 3, BRICKS);
      b.fill(x, y + 7, z - 2, x, y + 11, z - 2, CHISELED);
      b.set(x - 1, y + 10, z - 2, TEAR);
      b.set(x + 1, y + 10, z - 2, TEAR);

      for (int k = -2; k <= 2; k++) {
         int h = 13 + (2 - Math.abs(k)) * 2;
         b.fill(x + k, y + 13, z - 2, x + k, y + h, z - 2, Math.abs(k) == 2 ? (px, py, pz) -> PILLAR : WORN);
         b.set(x + k, y + h + 1, z - 2, CHISELED);
      }

      b.set(x, y + 18, z - 2, skull(0));

      for (int side = -1; side <= 1; side += 2) {
         b.set(x + side * 4, y + 4, z + 2, candles(4));
         b.set(x + side * 5, y + 3, z + 4, candles(3));
      }
   }

   static void debris(Build b, Layout.Island is) {
      int cx = is.x();
      int cz = is.z();
      if (b.overlaps(cx - 3, cz - 3, cx + 3, cz + 3)) {
         int sy = Layout.surface(is, cx, cz);
         if (sy != -2147483648) {
            switch (is.seed() % 4) {
               case 0:
                  b.set(cx, sy + 1, cz, crystal(Direction.UP));
                  b.set(cx + 1, sy + 1, cz, crystal(Direction.UP));
                  b.set(cx, sy + 1, cz + 1, TEAR);
                  break;
               case 1:
                  b.fill(cx, sy + 1, cz, cx, sy + 4, cz, PILLAR);
                  b.fill(cx + 1, sy + 4, cz, cx + 2, sy + 4, cz, WORN);
                  b.setLinked(cx + 2, sy + 3, cz, chain());
                  b.set(cx + 2, sy + 2, cz, lantern(true));
                  break;
               case 2:
                  b.set(cx, sy + 1, cz, CRACKED);
                  b.set(cx, sy + 2, cz, skull(is.seed()));
                  b.set(cx + 1, sy + 1, cz, CRACKED);
                  break;
               default:
                  b.set(cx, sy + 1, cz, CHISELED);
                  b.set(cx, sy + 2, cz, candles(3));
            }
         }
      }
   }

   private Structures() {
   }
}
