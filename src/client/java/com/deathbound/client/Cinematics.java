package com.deathbound.client;

import com.deathbound.registry.ModItems;
import com.deathbound.world.UnderworldTravel;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class Cinematics {
   private static final Identifier VIGNETTE = Identifier.withDefaultNamespace("textures/misc/vignette.png");
   private static final RandomSource JITTER = RandomSource.create();
   private static int kind = -1;
   private static int age;

   private static int length(int k) {
      return switch (k) {
         case 0 -> 150;
         case 1 -> 90;
         case 2 -> 120;
         default -> 80;
         case 4, 7 -> 110;
         case 9, 10, 11 -> 160;
      };
   }

   public static boolean active() {
      return kind >= 0;
   }

   public static boolean endingShowing() {
      return kind == 9 || kind == 10 || kind == 11;
   }

   public static void play(int k) {
      kind = k;
      age = 0;
   }

   public static void tick() {
      if (kind >= 0 && ++age > length(kind)) {
         kind = -1;
      }
   }

   private static float ramp(float t, float start, float end) {
      return Mth.clamp((t - start) / (end - start), 0.0F, 1.0F);
   }

   private static float window(float t, float a, float b, float c, float d) {
      return Math.min(ramp(t, a, b), 1.0F - ramp(t, c, d));
   }

   public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player != null) {
         renderRite(g, player, delta);
      }

      if (kind >= 0) {
         float t = age + delta.getGameTimeDeltaPartialTick(false);
         Font font = mc.font;
         int w = g.guiWidth();
         int h = g.guiHeight();
         g.nextStratum();
         switch (kind) {
            case 0:
               float black = Math.min(ramp(t, 0.0F, 18.0F), 1.0F - ramp(t, 118.0F, 148.0F));
               fill(g, w, h, 327946, black);
               vignette(g, w, h, 0.6F * black, 0.35F, 0.05F, 0.6F);
               float shake = 1.0F - ramp(t, 30.0F, 60.0F);
               title(
                  g,
                  font,
                  Component.translatable("cinematic.deathbound.conquer"),
                  w / 2,
                  h / 2 - 34,
                  4.0F,
                  15260927,
                  window(t, 22.0F, 34.0F, 92.0F, 104.0F),
                  shake
               );
               title(
                  g,
                  font,
                  Component.translatable("cinematic.deathbound.forgotten"),
                  w / 2,
                  h / 2 + 6,
                  2.6F,
                  10640639,
                  window(t, 40.0F, 52.0F, 92.0F, 104.0F),
                  shake * 0.6F
               );
               title(
                  g,
                  font,
                  Component.translatable("cinematic.deathbound.journey"),
                  w / 2,
                  h / 2 + 52,
                  1.2F,
                  12101848,
                  window(t, 70.0F, 82.0F, 110.0F, 122.0F),
                  0.0F
               );
               break;
            case 1:
               fill(g, w, h, 16051967, window(t, 0.0F, 6.0F, 20.0F, 70.0F) * 0.9F);
               title(
                  g, font, Component.translatable("cinematic.deathbound.return"), w / 2, h / 2 - 8, 2.2F, 3809116, window(t, 6.0F, 18.0F, 55.0F, 80.0F), 0.0F
               );
               break;
            case 2:
               fill(g, w, h, 327688, 1.0F - ramp(t, 50.0F, 110.0F));
               title(
                  g,
                  font,
                  Component.translatable("cinematic.deathbound.were_forgotten"),
                  w / 2,
                  h / 2 - 20,
                  3.0F,
                  11549951,
                  window(t, 6.0F, 22.0F, 75.0F, 105.0F),
                  0.4F
               );
               title(
                  g,
                  font,
                  Component.translatable("cinematic.deathbound.were_forgotten.sub"),
                  w / 2,
                  h / 2 + 22,
                  1.2F,
                  12628192,
                  window(t, 25.0F, 38.0F, 80.0F, 105.0F),
                  0.0F
               );
               break;
            case 3:
               title(g, font, Component.translatable("cinematic.deathbound.door"), w / 2, h / 3, 2.4F, 13674751, window(t, 0.0F, 12.0F, 55.0F, 80.0F), 0.3F);
               break;
            case 4:
               vignette(g, w, h, window(t, 0.0F, 20.0F, 80.0F, 110.0F) * 0.8F, 0.25F, 0.0F, 0.45F);
               title(
                  g,
                  font,
                  Component.translatable("cinematic.deathbound.death"),
                  w / 2,
                  h / 3 - 10,
                  5.0F,
                  15590655,
                  window(t, 8.0F, 26.0F, 78.0F, 104.0F),
                  0.25F
               );
               title(
                  g,
                  font,
                  Component.translatable("cinematic.deathbound.death.throne"),
                  w / 2,
                  h / 3 + 34,
                  1.6F,
                  10909928,
                  window(t, 26.0F, 40.0F, 80.0F, 104.0F),
                  0.0F
               );
               break;
            case 5:
               title(
                  g,
                  font,
                  Component.translatable("cinematic.deathbound.death.reaper"),
                  w / 2,
                  h / 3,
                  3.0F,
                  13213951,
                  window(t, 0.0F, 10.0F, 50.0F, 78.0F),
                  0.5F
               );
               break;
            case 6:
               fill(g, w, h, 11563263, window(t, 40.0F, 50.0F, 52.0F, 70.0F) * 0.85F);
               title(
                  g,
                  font,
                  Component.translatable("cinematic.deathbound.death.beast"),
                  w / 2,
                  h / 3,
                  3.6F,
                  16734860,
                  window(t, 48.0F, 56.0F, 66.0F, 80.0F),
                  1.0F
               );
               break;
            case 7:
               fill(g, w, h, 15391999, window(t, 0.0F, 4.0F, 10.0F, 40.0F) * 0.7F);
               title(
                  g, font, Component.translatable("cinematic.deathbound.victory"), w / 2, h / 3, 3.2F, 16051967, window(t, 10.0F, 26.0F, 80.0F, 108.0F), 0.0F
               );
               title(
                  g,
                  font,
                  Component.translatable("cinematic.deathbound.victory.sub"),
                  w / 2,
                  h / 3 + 34,
                  1.3F,
                  12098792,
                  window(t, 30.0F, 44.0F, 84.0F, 108.0F),
                  0.0F
               );
            case 8:
            default:
               break;
            case 9:
            case 10:
            case 11:
               String key = "cinematic.deathbound.ending." + (kind == 9 ? "take" : (kind == 10 ? "king" : "break"));
               fill(g, w, h, 327946, window(t, 0.0F, 20.0F, 120.0F, 160.0F) * 0.85F);
               vignette(g, w, h, window(t, 0.0F, 20.0F, 120.0F, 160.0F) * 0.7F, 0.3F, 0.0F, 0.5F);
               title(
                  g, font, Component.translatable(key), w / 2, h / 2 - 22, 3.4F, kind == 9 ? 16767392 : 15590655, window(t, 14.0F, 34.0F, 110.0F, 150.0F), 0.0F
               );
               title(g, font, Component.translatable(key + ".sub"), w / 2, h / 2 + 20, 1.3F, 12098792, window(t, 40.0F, 56.0F, 116.0F, 150.0F), 0.0F);
         }
      }
   }

   private static void renderRite(GuiGraphicsExtractor g, LocalPlayer player, DeltaTracker delta) {
      if (player.isUsingItem() && player.getUseItem().is(ModItems.DEATHBOUND_RELIC)) {
         float progress = Mth.clamp((player.getTicksUsingItem() + delta.getGameTimeDeltaPartialTick(false)) / 100.0F, 0.0F, 1.0F);
         int w = g.guiWidth();
         int h = g.guiHeight();
         g.nextStratum();
         vignette(g, w, h, 0.25F + progress * 0.75F, 0.45F, 0.1F, 0.7F);
         fill(g, w, h, 655378, progress * progress * 0.75F);
         boolean below = UnderworldTravel.inUnderworld(player);
         Font font = Minecraft.getInstance().font;
         Component ask = Component.translatable(below ? "cinematic.deathbound.rite.ferry" : "cinematic.deathbound.rite.ask");
         title(g, font, ask, w / 2, h / 2 + 28, 1.3F, 14207231, Math.min(ramp(progress, 0.05F, 0.25F), 1.0F - ramp(progress, 0.85F, 1.0F)), progress * 0.6F);
      }
   }

   private static void fill(GuiGraphicsExtractor g, int w, int h, int rgb, float alpha) {
      if (alpha > 0.003F) {
         g.fill(0, 0, w, h, ARGB.color(Mth.clamp(alpha, 0.0F, 1.0F), rgb));
      }
   }

   private static void vignette(GuiGraphicsExtractor g, int w, int h, float strength, float r, float gr, float b) {
      if (strength > 0.003F) {
         int color = ARGB.colorFromFloat(
            1.0F,
            Mth.clamp(strength * (1.0F - r * 0.4F), 0.0F, 1.0F),
            Mth.clamp(strength * (1.0F - gr), 0.0F, 1.0F),
            Mth.clamp(strength * (1.0F - b * 0.4F), 0.0F, 1.0F)
         );
         g.blit(RenderPipelines.VIGNETTE, VIGNETTE, 0, 0, 0.0F, 0.0F, w, h, w, h, color);
      }
   }

   private static void title(GuiGraphicsExtractor g, Font font, Component text, int x, int y, float scale, int rgb, float alpha, float shake) {
      if (!(alpha <= 0.02F)) {
         g.pose().pushMatrix();
         float jx = shake > 0.0F ? (JITTER.nextFloat() - 0.5F) * shake * 2.0F : 0.0F;
         float jy = shake > 0.0F ? (JITTER.nextFloat() - 0.5F) * shake * 2.0F : 0.0F;
         g.pose().translate(x + jx, y + jy);
         g.pose().scale(scale, scale);
         int width = font.width(text);
         int argb = ARGB.color(Mth.clamp(alpha, 0.0F, 1.0F), rgb);
         g.text(font, text, -width / 2, -4, argb, true);
         g.pose().popMatrix();
      }
   }

   private Cinematics() {
   }
}
