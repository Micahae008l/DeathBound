package com.deathbound.world;

import com.deathbound.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The River of the Dead's old course, from the spring at the Weir down to the Mere. Everyone thinks it died when Silas
 * drank the Lake. It didn't: it was stopped upstream, and the Mere is the one place it still seeps to. The river held the
 * land along it together, so between the islands a ribbon of that land still floats, the dry channel worn into it, cracked
 * apart here and there. On the islands it's the same channel worn into the ground.
 */
final class River {
   static final double BED = 2.5;     // the bed runs out to here from the middle of the course
   static final double LEDGE = 3.4;   // a step half-way up the bank, out to here
   static final double SHORE = 6.5;   // between the islands, the land along it reaches out to about here
   private static final double REACH = SHORE + 1.7;
   /** The course, upstream first: x, z, bed y. On an island the bed sits 2 under its top; it only ever steps down. */
   private static final int[][] COURSE = {
      {0, 300, 108}, {4, 290, 108}, {8, 282, 108},                                   // the Weir: out of the reservoir, off the north shore
      {18, 272, 106}, {32, 261, 104},                                                // floating
      {42, 252, 102}, {52, 238, 102}, {46, 222, 102}, {38, 208, 102}, {30, 199, 102}, // through the Hamlet
      {20, 190, 101}, {8, 182, 99},                                                  // floating
      {0, 171, 98}, {-12, 160, 98}, {-8, 146, 98}, {-2, 134, 98}, {4, 128, 98},      // across Wreck Reach
      {14, 118, 98}, {28, 108, 97}, {36, 100, 97},                                   // floating
      {38, 94, 97}, {39, 80, 97}                                                     // into the Mere, level with its water
   };
   /** Where the floating bed broke: a 2-block gap across the course at these points of it. */
   private static final int[] BREAKS = {3, 10, 18};
   /** x, z, bed y, distance along the course; every half block. */
   static final List<double[]> PATH = new ArrayList<>();
   private static final double[] BREAK_AT = new double[BREAKS.length];
   private static final int MIN_X;
   private static final int MAX_X;
   private static final int MIN_Z;
   private static final int MAX_Z;
   private static final Layout.Island[] BANKS = {Layout.WEIR, Layout.HAMLET, Layout.WRECKS, Layout.MERE};
   /** Where the Mired lie (Mire.java): a hand out of the bed, a block to one side of the middle, so you can pass it if you look. */
   static final List<int[]> MIRED = new ArrayList<>();

   static {
      double s = 0.0;
      double[] last = null;
      for (int i = 0; i + 1 < COURSE.length; i++) {
         int[] p0 = COURSE[Math.max(0, i - 1)], p1 = COURSE[i], p2 = COURSE[i + 1], p3 = COURSE[Math.min(COURSE.length - 1, i + 2)];
         for (int k = 0; k < BREAKS.length; k++) {
            if (BREAKS[k] == i) {
               BREAK_AT[k] = s;
            }
         }

         int n = (int)Math.ceil(Math.hypot(p2[0] - p1[0], p2[1] - p1[1]) / 0.5);
         for (int j = 0; j < n; j++) {
            double t = (double)j / n;
            double x = catmull(p0[0], p1[0], p2[0], p3[0], t);
            double z = catmull(p0[1], p1[1], p2[1], p3[1], t);
            if (last != null) {
               s += Math.hypot(x - last[0], z - last[1]);
            }

            last = new double[]{x, z, p1[2] + (p2[2] - p1[2]) * t, s};
            PATH.add(last);
         }
      }

      int[] end = COURSE[COURSE.length - 1];
      PATH.add(new double[]{end[0], end[1], end[2], s + Math.hypot(end[0] - last[0], end[1] - last[1])});
      MIN_X = (int)Math.floor(PATH.stream().mapToDouble(p -> p[0]).min().orElse(0)) - 5;
      MAX_X = (int)Math.ceil(PATH.stream().mapToDouble(p -> p[0]).max().orElse(0)) + 5;
      MIN_Z = (int)Math.floor(PATH.stream().mapToDouble(p -> p[1]).min().orElse(0)) - 5;
      MAX_Z = (int)Math.ceil(PATH.stream().mapToDouble(p -> p[1]).max().orElse(0)) + 5;

      // walking up from the Mere: the first one 20 blocks in, then one every 26 to 34; none in the Hamlet (people live
      // there), none at the Weir, none right at a gap
      double total = s;
      int side = 1;
      for (double up = 20.0; up < total - 14.0; up += 26.0 + Build.hash((int)up, 0, 0, 471) * 8.0) {
         double at = total - up;
         int i = 0;
         while (i < PATH.size() - 1 && PATH.get(i)[3] < at) {
            i++;
         }

         double[] p = PATH.get(i);
         int px = (int)Math.floor(p[0]);
         int pz = (int)Math.floor(p[1]);
         boolean home = Layout.surface(Layout.HAMLET, px, pz) != Integer.MIN_VALUE || Layout.surface(Layout.WEIR, px, pz) != Integer.MIN_VALUE;
         boolean gap = false;
         for (double b : BREAK_AT) {
            gap |= Math.abs(b - at) < 7.0;
         }

         if (!home && !gap && i > 0) {
            double[] a = PATH.get(i - 1);
            double[] c = PATH.get(i + 1);
            double dx = c[0] - a[0];
            double dz = c[1] - a[1];
            double len = Math.max(1.0E-6, Math.hypot(dx, dz));
            MIRED.add(new int[]{(int)Math.floor(p[0] - dz / len * side), (int)Math.floor(p[1] + dx / len * side), (int)Math.floor(p[2])});
            side = -side;
         }
      }
   }

   static boolean near(double x, double z) {
      return x >= MIN_X && x <= MAX_X && z >= MIN_Z && z <= MAX_Z;
   }

   private static double catmull(double a, double b, double c, double d, double t) {
      return 0.5 * (2.0 * b + (c - a) * t + (2.0 * a - 5.0 * b + 4.0 * c - d) * t * t + (3.0 * b - a - 3.0 * c + d) * t * t * t);
   }

   private River() {
   }

   static void build(Build b) {
      if (!b.overlaps(MIN_X, MIN_Z, MAX_X, MAX_Z)) {
         return;
      }

      // only the stretch of the course near this chunk
      List<Integer> near = new ArrayList<>();
      for (int i = 0; i < PATH.size(); i++) {
         double[] p = PATH.get(i);
         if (p[0] > b.x0 - 6 && p[0] < b.x1 + 6 && p[1] > b.z0 - 6 && p[1] < b.z1 + 6) {
            near.add(i);
         }
      }

      if (near.isEmpty()) {
         return;
      }

      for (int x = b.x0; x <= b.x1; x++) {
         for (int z = b.z0; z <= b.z1; z++) {
            int at = -1;
            double w2 = REACH * REACH;
            for (int i : near) {
               double[] p = PATH.get(i);
               double d2 = (x + 0.5 - p[0]) * (x + 0.5 - p[0]) + (z + 0.5 - p[1]) * (z + 0.5 - p[1]);
               if (d2 < w2) {
                  w2 = d2;
                  at = i;
               }
            }

            // the course ends open at both ends: no rounded cap of bank across the Mere's inlet or the reservoir
            if (at > 0 && at < PATH.size() - 1) {
               column(b, x, z, PATH.get(at), Math.sqrt(w2));
            }
         }
      }

      for (int[] m : MIRED) {
         BlockState bed = b.get(m[0], m[2], m[1]);
         if (b.in(m[0], m[1]) && (bed.is(ModBlocks.ASHEN_SOIL) || bed.is(Blocks.SOUL_SOIL))) {
            b.set(m[0], m[2], m[1], Landmarks.DRY_BED);   // the hand only takes root in ashen soil
            b.set(m[0], m[2] + 1, m[1], ModBlocks.MIRED_HAND.defaultBlockState());
         }
      }
   }

   private static void column(Build b, int x, int z, double[] p, double w) {
      int y = (int)Math.floor(p[2]);
      int top = Integer.MIN_VALUE;
      for (Layout.Island is : BANKS) {
         top = Math.max(top, Layout.surface(is, x, z));
      }

      if (top >= y) {
         channel(b, x, z, y, top, w);
      } else if (w <= shore(x, z) && !broken(p[3], w, x, z)) {
         ribbon(b, x, z, y, w);
      }
   }

   /** How far the land along the river reaches out from the middle of the course. */
   private static double shore(int x, int z) {
      return SHORE + 1.6 * Layout.noise(x * 0.12, z * 0.12, 459);
   }

   /** A step half-way up the bank: most of the way along, never all of it, so the banks are worn rather than dug. */
   private static boolean ledge(int x, int z, double w) {
      return w <= LEDGE && Layout.noise(x * 0.2, z * 0.2, 461) > -0.25;
   }

   /** On an island: the dry channel worn into the ground. */
   private static void channel(Build b, int x, int z, int y, int top, double w) {
      double r = Build.hash(x, 0, z, 451);
      if (w <= BED) {
         b.fill(x, y + 1, z, x, top + 3, z, Structures.AIR);
         bed(b, x, y, z, r, true);
         b.set(x, y - 1, z, Structures.STONE);
      } else if (top >= y + 2 && ledge(x, z, w)) {
         b.fill(x, y + 2, z, x, top + 3, z, Structures.AIR);
         b.set(x, y + 1, z, ModBlocks.ASHEN_SOIL.defaultBlockState());
         b.set(x, y, z, Structures.STONE);
      }
   }

   /**
    * Between the islands: a ribbon of the land that ran along the river, with the dry channel worn into it, built like an
    * island (Layout.island): the same soil, rock and keel underneath, the same grass and bones on top.
    */
   private static void ribbon(Build b, int x, int z, int y, double w) {
      double rel = w / shore(x, z);
      int top;
      if (w <= BED) {
         top = y;
      } else if (ledge(x, z, w)) {
         top = y + 1;
      } else {
         top = y + 2 + (int)Math.round(Layout.noise(x * 0.09, z * 0.09, 460) * 0.9) - (rel > 0.8 ? (int)((rel - 0.8) * 10.0) : 0);
         top = Math.max(top, y + 1);
      }

      double body = Math.pow(Math.max(0.0, 1.0 - rel), 0.62) * (0.78 + 0.32 * Layout.noise(x * 0.14, z * 0.14, 462));
      int bottom = top - 2 - (int)(12.0 * body);
      double spike = Build.hash(x, 0, z, 463);
      if (spike < 0.03 && rel < 0.85) {
         bottom -= 3 + (int)(spike * 200.0);
      }

      BlockState soil = ModBlocks.ASHEN_SOIL.defaultBlockState();
      for (int yy = bottom; yy < top; yy++) {
         int depth = top - yy;
         double r = Build.hash(x, yy, z, 464);
         b.set(x, yy, z, depth <= 2 ? (r < 0.55 ? soil : Structures.STONE)
            : (r < 0.035 ? Blocks.BLACKSTONE.defaultBlockState() : (r < 0.042 && depth > 5 ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Structures.STONE)));
      }

      double r = Build.hash(x, top, z, 465);
      if (top == y) {
         bed(b, x, y, z, r, false);
      } else {
         b.set(x, top, z, soil);
         BlockState prop = null;
         if (r < 0.006) {
            prop = Decor.bones((int)(r * 9000.0));
         } else if (r < 0.009) {
            prop = Decor.remains(Direction.from2DDataValue((int)(r * 4000.0) & 3));
         } else if (r < 0.022) {
            prop = ModBlocks.SMALL_SOUL_SHARD.defaultBlockState();
         } else if (r < 0.027) {
            prop = ModBlocks.MEDIUM_SOUL_SHARD.defaultBlockState();
         } else if (r < 0.14) {
            double kind = Build.hash(x, top, z, 466);
            prop = (kind < 0.35 ? ModBlocks.GLOOM_GRASS : (kind < 0.8 ? ModBlocks.DEAD_GRASS : (kind < 0.97 ? ModBlocks.TALL_DEAD_GRASS : ModBlocks.GHOST_BLOOM)))
               .defaultBlockState();
         }

         if (prop != null) {
            b.set(x, top + 1, z, prop);
         }
      }

      double under = Build.hash(x, bottom, z, 467);
      if (under < 0.05) {
         b.set(x, bottom - 1, z, ModBlocks.SOUL_CRYSTAL.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.DOWN));
      } else if (rel < 0.5 && under > 0.997) {
         b.setLinked(x, bottom - 1, z, Structures.chain());
         b.setLinked(x, bottom - 2, z, Structures.chain());
         b.set(x, bottom - 3, z, ModBlocks.HANGING_REMAINS.defaultBlockState());
      }
   }

   private static void bed(Build b, int x, int y, int z, double r, boolean cut) {
      boolean crack = (x + (int)(Build.hash(z, 0, 0, 456) * 3.0)) % 5 == 0 || (z + (int)(Build.hash(x, 0, 0, 457) * 3.0)) % 6 == 0;
      b.set(x, y, z, crack ? Landmarks.CRACK : Landmarks.DRY_BED);
      if (r < 0.03) {
         b.set(x, y + 1, z, Decor.bones((int)(r * 1000.0)));
      } else if (r < 0.038) {
         b.set(x, y + 1, z, Structures.skull((int)(r * 10000.0) % 16));
      } else if (!cut && r > 0.985) {
         b.set(x, y + 1, z, Blocks.DEAD_BUSH.defaultBlockState());
      }
   }

   /** The gaps: two blocks across the bed where you walk, wider and ragged out on the banks, like the land cracked. */
   private static boolean broken(double s, double w, int x, int z) {
      for (double at : BREAK_AT) {
         double half = 1.0 + (w > 1.5 ? Build.hash(x, 1, z, 458) * 1.5 + (w - 1.5) * 0.3 : 0.0);
         if (Math.abs(s - at) < half) {
            return true;
         }
      }

      return false;
   }
}
