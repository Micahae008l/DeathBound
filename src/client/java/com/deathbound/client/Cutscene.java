package com.deathbound.client;

import com.deathbound.world.Layout;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class Cutscene {
   private static List<Cutscene.Shot> shots;
   private static int index;
   private static int age;
   private static int elapsed;
   private static int which;
   private static CameraType cameraBefore;

   private static Cutscene.Shot shot(
      int ticks, double x0, double y0, double z0, double x1, double y1, double z1, double lx0, double ly0, double lz0, double lx1, double ly1, double lz1
   ) {
      return new Cutscene.Shot(ticks, new Vec3(x0, y0, z0), new Vec3(x1, y1, z1), new Vec3(lx0, ly0, lz0), new Vec3(lx1, ly1, lz1));
   }

   private static List<Cutscene.Shot> build(int which) {
      double sx = Layout.THRONE_SEAT.getX() + 0.5;
      double sy = Layout.THRONE_SEAT.getY() + 0.5;
      double sz = Layout.THRONE_SEAT.getZ() + 0.5;
      double gy = Layout.GATE.top() + 1;
      double dz = Layout.DOOR_Z;
      double post = Layout.GUARD_POST.getZ();

      return switch (which) {
         case 1 -> List.of(
            shot(110, sx, sy + 2.0, sz + 13.5, sx, sy + 1.3, sz + 7.0, sx, sy + 1.1, sz, sx, sy + 1.1, sz),
            shot(100, sx + 2.6, sy + 1.4, sz + 4.6, sx - 2.6, sy + 1.4, sz + 4.6, sx, sy + 0.8, sz, sx, sy + 0.8, sz),
            shot(90, sx + 8.0, sy - 0.6, sz + 7.4, sx + 7.0, sy - 0.9, sz + 6.6, sx, sy - 0.8, sz + 5.4, sx, sy - 0.6, sz + 5.0)
         );
         case 2 -> List.of(
            shot(80, sx + 5.5, sy + 2.5, sz + 11.0, sx - 4.5, sy + 2.5, sz + 10.0, sx, sy + 1.3, sz, sx, sy + 1.3, sz),
            shot(120, sx + 1.7, sy + 0.9, sz + 8.4, sx + 1.2, sy + 0.8, sz + 7.0, sx, sy + 0.7, sz, sx, sy + 0.8, sz),
            shot(180, 9.0, gy + 4.0, post + 9.0, 7.0, gy + 3.0, post + 5.0, 0.5, gy + 1.5, post + 1.0, 0.5, gy + 1.5, post - 2.0),
            shot(
               100,
               Layout.CITADEL.x() + 2.5,
               Layout.CITADEL.top() + 5,
               Layout.CITADEL.z() + 46,
               Layout.CITADEL.x() + 6.5,
               Layout.CITADEL.top() + 4,
               Layout.CITADEL.z() + 40,
               Layout.CITADEL.x() + 30,
               Layout.CITADEL.top() - 3,
               Layout.CITADEL.z() + 34,
               Layout.CITADEL.x() + 36,
               Layout.CITADEL.top() - 3,
               Layout.CITADEL.z() + 20
            ),
            shot(110, sx + 1.4, sy + 1.9, sz + 3.4, sx + 0.6, sy + 1.9, sz + 2.3, sx, sy + 1.8, sz, sx, sy + 1.85, sz)
         );
         case 3 -> List.of(
            shot(60, sx, sy, sz + 9.0, sx, sy + 0.3, sz + 7.0, sx, sy + 0.5, sz, sx, sy + 0.5, sz),
            shot(170, 16.0, gy + 11.0, dz + 21.0, 12.0, gy + 9.0, dz + 17.0, 0.5, gy + 9.0, dz, 0.5, gy + 5.0, dz)
         );
         case 4 -> {
            double ay = Layout.ARRIVAL.top() + 1;
            double az = Layout.ARRIVAL.z();
            yield List.of(shot(130, 3.5, ay + 2.0, az - 8.0, 1.5, ay + 3.5, az - 11.0, 0.5, ay + 4.0, az - 50.0, 0.5, ay + 12.0, az - 95.0));
         }
         default -> List.of();
      };
   }

   public static void play(int which) {
      Minecraft mc = Minecraft.getInstance();
      cameraBefore = mc.options.getCameraType();
      mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
      shots = new ArrayList<>(build(which));
      if (!shots.isEmpty() && which != 3) {
         Cutscene.Shot last = shots.getLast();
         shots.add(new Cutscene.Shot(150, last.to, last.to, last.lookTo, last.lookTo));
      }

      Cutscene.which = which;
      index = 0;
      age = 0;
      elapsed = 0;
      if (shots.isEmpty()) {
         shots = null;
      } else {
         start(shots.getFirst());
      }
   }

   private static void start(Cutscene.Shot s) {
      Cine.shot(s.ticks / 20.0F, s.from, s.to, s.lookFrom, s.lookTo);
   }

   public static boolean playing() {
      return shots != null;
   }

   public static void tick() {
      if (shots != null) {
         elapsed++;
         if (++age >= shots.get(index).ticks) {
            age = 0;
            if (++index >= shots.size()) {
               shots = null;
               Cine.stopCamera();
               if (cameraBefore != null) {
                  Minecraft.getInstance().options.setCameraType(cameraBefore);
               }

               return;
            }

            start(shots.get(index));
         }
      }
   }

   public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
      if (shots != null) {
         int w = g.guiWidth();
         int h = g.guiHeight();
         int bar = h / 9;
         g.fill(0, 0, w, bar, -16777216);
         g.fill(0, h - bar, w, h, -16777216);
         float t = elapsed + delta.getGameTimeDeltaPartialTick(false);
         float black = 0.0F;
         int cut = 0;

         for (int i = 0; i < shots.size() - 1; i++) {
            cut += shots.get(i).ticks;
            black = Math.max(black, 1.0F - Mth.clamp((Math.abs(t - cut) - 3.0F) / 12.0F, 0.0F, 1.0F));
         }

         black = Math.max(black, 1.0F - Mth.clamp(t / 15.0F, 0.0F, 1.0F));
         if (black > 0.01F) {
            g.fill(0, 0, w, h, (int)(black * 255.0F) << 24);
         }

         Font font = Minecraft.getInstance().font;
         String key = "cutscene.deathbound." + which + "." + index;
         if (!I18n.get(key).equals(key)) {
            MutableComponent line = Component.translatable(key);
            float in = Mth.clamp((age + delta.getGameTimeDeltaPartialTick(false) - 12.0F) / 10.0F, 0.0F, 1.0F);
            int alpha = (int)(in * (1.0F - black) * 255.0F);
            if (alpha > 8) {
               int y = h - bar + (bar - 9) / 2;

               for (FormattedCharSequence part : font.split(line, w - 40)) {
                  g.text(font, part, (w - font.width(part)) / 2, y, ARGB.color(alpha, 14273776), false);
                  y += 9 + 2;
               }
            }
         }
      }
   }

   private Cutscene() {
   }

   private record Shot(int ticks, Vec3 from, Vec3 to, Vec3 lookFrom, Vec3 lookTo) {
   }
}
