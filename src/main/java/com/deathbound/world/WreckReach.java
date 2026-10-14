package com.deathbound.world;

import com.deathbound.block.FerryBlock;
import com.deathbound.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Wreck Reach: the dead came down the river from its spring in boats nobody rowed, and the Ferryman met them at the
 * Landing. When the water stopped, the boats up here ran aground with everyone aboard. The big barge lies in the channel
 * at the bend, its bow driven into the bank; the way upriver goes in at its broken stern, past its passengers, and out
 * where the bow split. Smaller ferries lie along the bed above and below it.
 */
final class WreckReach {
   static final int XC = -11;
   static final int ZS = 159;   // the stern; the bow points north, upriver
   static final int YB = 98;    // the channel's bed here
   /** The barge's half-width at each step from the stern to the bow. */
   private static final int[] HB = {1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0};
   /**
    * Who is still sitting in it: steps from the stern, and which side of the middle. They leave a way through: up the
    * middle, then round the mast on the east side to where the bow split.
    */
   private static final int[][] PASSENGERS = {{2, 1}, {3, -1}, {4, 1}, {4, -1}, {5, -1}, {6, -1}, {7, 0}, {8, -1}, {8, 0}};
   // the same spruce and dark oak as the Ferryman's own boats
   private static final BlockState SIDE = Blocks.SPRUCE_PLANKS.defaultBlockState();
   private static final BlockState FLOOR = Blocks.DARK_OAK_PLANKS.defaultBlockState();

   static void build(Build b, Layout.Island is) {
      barge(b);
      ferry(b, -7, YB + 1, 142, Direction.NORTH);    // in the bed above the barge, run up against the west bank
      ferry(b, -2, YB + 1, 168, Direction.WEST);     // across the bed below it, where the river bent
      int y = Layout.surface(is, -1, 163);
      if (y != Integer.MIN_VALUE) {
         ferry(b, -1, y + 1, 166, Direction.NORTH);   // dragged up onto the bank by someone who meant to walk the rest
         b.set(0, y + 1, 162, Decor.remains(Direction.NORTH));
      }

      for (int[] post : new int[][]{{-17, 155}, {-5, 152}, {-11, 140}, {-4, 161}}) {
         mooring(b, is, post[0], post[1]);
      }
   }

   private static void barge(Build b) {
      if (!b.overlaps(XC - 4, ZS - 12, XC + 4, ZS + 4)) {
         return;
      }

      for (int u = 0; u < HB.length; u++) {
         int z = ZS - u;
         int hb = HB[u];
         b.fill(XC - hb, YB + 1, z, XC + hb, YB + 6, z, Structures.AIR);   // where the hull pushed into the bank
         b.fill(XC - hb, YB - 1, z, XC + hb, YB - 1, z, Structures.STONE);
         for (int v = -hb; v <= hb; v++) {
            int x = XC + v;
            b.set(x, YB, z, v == 0 ? log(Blocks.DARK_OAK_LOG, Axis.Z) : FLOOR);   // the bottom, and the keel down the middle
            if (Math.abs(v) == hb || u == 0) {
               b.set(x, YB + 1, z, SIDE);
               b.set(x, YB + 2, z, log(Blocks.SPRUCE_LOG, u == 0 ? Axis.X : Axis.Z));   // the rail
            }
         }
      }

      // the bow: its stem post, and the one light left on the river
      b.fill(XC, YB + 1, ZS - 10, XC, YB + 3, ZS - 10, log(Blocks.DARK_OAK_LOG, Axis.Y));
      b.set(XC, YB + 4, ZS - 10, Structures.lantern(false));
      // the stern was stove in: the way in
      b.fill(XC, YB + 1, ZS, XC, YB + 2, ZS, Structures.AIR);
      b.set(XC + 1, YB + 2, ZS, Structures.AIR);
      // and the bow split on the east side: the way out
      b.fill(XC + 2, YB + 1, ZS - 8, XC + 2, YB + 2, ZS - 8, Structures.AIR);
      b.set(XC + 2, YB + 2, ZS - 7, Structures.AIR);
      b.set(XC + 3, YB + 1, ZS - 9, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));   // a sprung plank

      // the mast, snapped off, with what's left of a flag on it
      b.fill(XC, YB + 1, ZS - 6, XC, YB + 6, ZS - 6, log(Blocks.SPRUCE_LOG, Axis.Y));
      b.set(XC + 1, YB + 5, ZS - 6, Structures.facing(ModBlocks.TATTERED_BANNER, Direction.EAST));

      // candles somebody left burning on the rail, for the ones who stayed
      for (int[] c : new int[][]{{2, -2, 3}, {4, 2, 2}, {6, -2, 2}, {8, 2, 3}}) {
         b.set(XC + c[1], YB + 3, ZS - c[0], Structures.candles(c[2]));
      }

      for (int[] p : PASSENGERS) {
         b.set(XC + p[1], YB + 1, ZS - p[0], Structures.facing(ModBlocks.SLUMPED_REMAINS, Direction.NORTH));
      }

      b.chest(XC - 1, YB + 1, ZS - 1, Direction.NORTH, Structures.WRECK_LOOT);
      b.set(XC - 1, YB + 2, ZS - 2, Structures.note(8, Direction.EAST, false));   // scratched into the hull, at eye height
   }

   /** One of the Ferryman's four-part boats, stern first, lying flat with its bow toward `bow`. */
   private static void ferry(Build b, int x, int y, int z, Direction bow) {
      for (int k = 0; k < 4; k++) {
         b.set(x + bow.getStepX() * k, y, z + bow.getStepZ() * k,
            Structures.facing(ModBlocks.FERRY, bow).setValue(FerryBlock.PART, k).setValue(FerryBlock.UPRIGHT, false));
      }
   }

   private static void mooring(Build b, Layout.Island is, int x, int z) {
      int y = Layout.surface(is, x, z);
      if (y != Integer.MIN_VALUE && b.in(x, z)) {
         b.fill(x, y + 1, z, x, y + 1 + (Build.hash(x, y, z, 482) < 0.5 ? 0 : 1), z, Blocks.SPRUCE_FENCE.defaultBlockState());
      }
   }

   private static BlockState log(net.minecraft.world.level.block.Block block, Axis axis) {
      return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
   }

   private WreckReach() {
   }
}
