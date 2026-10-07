package com.deathbound.client;

import com.deathbound.DeathBound;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice.MappedView;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jspecify.annotations.Nullable;

public final class Cine {
   private static Cine.@Nullable Move move;
   private static long moveStart;
   private static float moveSeconds;
   static float handheld;
   public static boolean clearAir;
   private static Vec3 aimDir = Vec3.ZERO;
   private static float pull = 1.0F;
   private static int fps = 30;
   private static final ExecutorService WRITER = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "deathbound-recorder");
      t.setDaemon(true);
      return t;
   });
   private static @Nullable Process ffmpeg;
   private static @Nullable OutputStream pipe;
   private static long recStart;
   private static long queued;
   private static long doubled;
   private static int recW;
   private static int recH;

   public static boolean active() {
      return move != null;
   }

   public static Cine.@Nullable Pose pose(float partialTicks) {
      if (move == null) {
         return null;
      }

      float t = moveSeconds <= 0.0F ? 1.0F : Mth.clamp((float)(System.nanoTime() - moveStart) / 1.0E9F / moveSeconds, 0.0F, 1.0F);
      Cine.Pose p = move.at(0.5F - 0.5F * Mth.cos(t * 3.1415927F), partialTicks);
      if (handheld > 0.0F) {
         double s = System.nanoTime() / 1.0E9;
         Vec3 drift = new Vec3(
               Math.sin(s * 1.3) + 0.5 * Math.sin(s * 2.9 + 1.0),
               Math.sin(s * 1.1 + 2.0) + 0.4 * Math.sin(s * 3.7),
               Math.sin(s * 0.9 + 4.0) + 0.5 * Math.sin(s * 2.3 + 3.0)
            )
            .scale(0.05 * handheld);
         p = new Cine.Pose(
            p.pos().add(drift),
            p.yaw() + (float)(Math.sin(s * 1.7 + 1.0) + 0.5 * Math.sin(s * 4.1)) * 0.5F * handheld,
            p.pitch() + (float)(Math.sin(s * 1.5 + 3.0) + 0.4 * Math.sin(s * 3.3)) * 0.35F * handheld
         );
      }

      return p;
   }

   static void off() {
      move = null;
   }

   public static void shot(float seconds, Vec3 from, Vec3 to, Vec3 lookFrom, Vec3 lookTo) {
      start(seconds, (t, pt) -> looking(from.lerp(to, t), lookFrom.lerp(lookTo, t)));
   }

   public static void stopCamera() {
      move = null;
   }

   private static void start(float seconds, Cine.Move m) {
      move = m;
      moveSeconds = seconds;
      moveStart = System.nanoTime();
   }

   private static Cine.Pose looking(Vec3 from, Vec3 at) {
      Vec3 d = at.subtract(from);
      float yaw = (float)(Mth.atan2(-d.x, d.z) * 57.2957763671875);
      float pitch = (float)(-Mth.atan2(d.y, d.horizontalDistance()) * 57.2957763671875);
      return new Cine.Pose(from, yaw, pitch);
   }

   private static float[] nums(String arg) {
      String[] p = arg.trim().split("\\s+");
      float[] f = new float[p.length];

      for (int i = 0; i < p.length; i++) {
         f[i] = Float.parseFloat(p[i]);
      }

      return f;
   }

   static void cam(String arg) {
      float[] f = nums(arg);
      Vec3 a = new Vec3(f[1], f[2], f[3]);
      Vec3 b = new Vec3(f[6], f[7], f[8]);
      start(f[0], (t, pt) -> new Cine.Pose(a.lerp(b, t), Mth.lerp(t, f[4], f[9]), Mth.lerp(t, f[5], f[10])));
   }

   static void pan(String arg) {
      float[] f = nums(arg);
      Vec3 a = new Vec3(f[1], f[2], f[3]);
      Vec3 b = new Vec3(f[4], f[5], f[6]);
      Vec3 at = new Vec3(f[7], f[8], f[9]);
      start(f[0], (t, pt) -> looking(a.lerp(b, t), at));
   }

   static void orbit(String arg) {
      float[] f = nums(arg);
      Vec3 c = new Vec3(f[1], f[2], f[3]);
      start(f[0], (t, pt) -> around(c, f[4], f[5], Mth.lerp(t, f[6], f[7])));
   }

   static void follow(String arg) {
      String[] p = arg.trim().split("\\s+", 3);
      float seconds = Float.parseFloat(p[0]);
      float[] f = nums(p[2]);
      String id = p[1].contains(":") ? p[1] : "minecraft:" + p[1];
      Entity e = nearest(id);
      start(seconds, (t, pt) -> {
         Vec3 c = e != null && !e.isRemoved() ? e.getPosition(pt) : Minecraft.getInstance().player.position();
         return around(c.add(0.0, f[4], 0.0), f[0], f[1] - f[4], Mth.lerp(t, f[2], f[3]));
      });
   }

   static void shoulder(String arg) {
      String[] p = arg.trim().split("\\s+", 4);
      Entity a = nearest(id(p[1]));
      Entity b = nearest(id(p[2]));
      float[] f = nums(p[3]);
      float[] g = f.length >= 6 ? new float[]{f[3], f[4], f[5]} : f;
      aimDir = Vec3.ZERO;
      start(Float.parseFloat(p[0]), (t, pt) -> {
         Vec3 pa = center(a, pt, 0.85);
         Vec3 pb = center(b, pt, 0.5);
         Vec3 want = pb.subtract(pa).multiply(1.0, 0.0, 1.0).normalize();
         aimDir = aimDir == Vec3.ZERO ? want : aimDir.lerp(want, 0.12).normalize();
         Vec3 right = new Vec3(-aimDir.z, 0.0, aimDir.x);
         Vec3 cam = pa.subtract(aimDir.scale(Mth.lerp(t, f[1], g[1]))).add(right.scale(Mth.lerp(t, f[0], g[0]))).add(0.0, Mth.lerp(t, f[2], g[2]), 0.0);
         return looking(unblocked(pa, cam), pa.lerp(pb, 0.7));
      });
   }

   private static String id(String s) {
      return s.contains(":") ? s : "minecraft:" + s;
   }

   private static Vec3 center(@Nullable Entity e, float pt, double up) {
      return e != null && !e.isRemoved()
         ? e.getPosition(pt).add(0.0, e.getBbHeight() * up, 0.0)
         : Minecraft.getInstance().player.getPosition(pt).add(0.0, 1.4, 0.0);
   }

   private static Cine.Pose around(Vec3 c, float radius, float height, float deg) {
      float r = deg * 0.017453292F;
      return looking(unblocked(c, c.add(-Mth.sin(r) * radius, height, Mth.cos(r) * radius)), c);
   }

   private static Vec3 unblocked(Vec3 center, Vec3 cam) {
      Minecraft mc = Minecraft.getInstance();
      BlockHitResult hit = mc.level.clip(new ClipContext(center, cam, Block.VISUAL, Fluid.NONE, mc.player));
      double full = cam.distanceTo(center);
      float want = hit.getType() != Type.MISS && !(full < 0.01) ? (float)Math.max(0.1, (hit.getLocation().distanceTo(center) - 0.4) / full) : 1.0F;
      pull = want < pull ? want : Math.min(want, pull + 0.02F);
      return center.lerp(cam, pull);
   }

   static @Nullable Entity nearest(String id) {
      Minecraft mc = Minecraft.getInstance();
      Entity best = null;
      double bestD = 1.7976931348623157E308;

      for (Entity e : mc.level.entitiesForRendering()) {
         if (BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals(id)) {
            double d = e.distanceToSqr(mc.player);
            if (d < bestD) {
               best = e;
               bestD = d;
            }
         }
      }

      return best;
   }

   static void record(Minecraft mc, String name, int rate) throws IOException {
      stop();
      fps = rate;
      RenderTarget target = mc.gameRenderer.mainRenderTarget();
      recW = target.width;
      recH = target.height;
      Path dir = mc.gameDirectory.toPath().resolve("trailer");
      Files.createDirectories(dir);
      ffmpeg = new ProcessBuilder(
            System.getProperty("deathbound.ffmpeg", "ffmpeg"),
            "-y",
            "-loglevel",
            "error",
            "-f",
            "rawvideo",
            "-pix_fmt",
            "rgba",
            "-video_size",
            recW + "x" + recH,
            "-framerate",
            String.valueOf(fps),
            "-i",
            "-",
            "-vf",
            "vflip",
            "-c:v",
            "h264_nvenc",
            "-preset",
            "p6",
            "-rc",
            "vbr",
            "-cq",
            "16",
            "-b:v",
            "0",
            "-pix_fmt",
            "yuv420p",
            dir.resolve(name + ".mp4").toString()
         )
         .redirectErrorStream(true)
         .redirectOutput(dir.resolve(name + ".log").toFile())
         .start();
      pipe = ffmpeg.getOutputStream();
      recStart = System.nanoTime();
      queued = 0L;
      doubled = 0L;
      DeathBound.LOG.info("[cine] recording {} at {}x{}", new Object[]{name, recW, recH});
   }

   static void stop() {
      if (ffmpeg != null) {
         Process p = ffmpeg;
         OutputStream out = pipe;
         DeathBound.LOG.info("[cine] stopped after {} frames ({} repeated)", queued, doubled);
         ffmpeg = null;
         pipe = null;

         try {
            WRITER.<Integer>submit(() -> {
               out.close();
               return p.waitFor();
            }).get();
         } catch (Exception e) {
            DeathBound.LOG.error("[cine] stop", e);
         }
      }
   }

   public static void frame() {
      if (ffmpeg != null) {
         long due = (System.nanoTime() - recStart) * fps / 1000000000L + 1L;
         int copies = (int)(due - queued);
         RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
         if (copies > 0 && target.width == recW && target.height == recH && target.getColorTexture() != null) {
            queued = due;
            doubled += copies - 1;
            int size = recW * recH * 4;
            OutputStream out = pipe;
            GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "Cine frame", 9, size);
            RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(target.getColorTexture(), buffer, 0L, () -> {
               byte[] data = new byte[size];

               try (MappedView read = buffer.map(true, false)) {
                  read.data().get(data);
               }

               buffer.close();
               WRITER.submit(() -> {
                  try {
                     for (int i = 0; i < copies; i++) {
                        out.write(data);
                     }
                  } catch (IOException e) {
                     DeathBound.LOG.error("[cine] write", e);
                  }
               });
            }, 0);
         }
      }
   }

   private Cine() {
   }

   private interface Move {
      Cine.Pose at(float var1, float var2);
   }

   public record Pose(Vec3 pos, float yaw, float pitch) {
   }
}
