package com.deathbound.client;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public final class RewardCards {
   private static final int IN = 8;
   private static final int HOLD = 110;
   private static final int OUT = 10;
   private static final Deque<RewardCards.Card> QUEUE = new ArrayDeque<>();
   private static RewardCards.Card showing;
   private static int age;

   public static void add(Component title, List<ItemStack> items) {
      QUEUE.add(new RewardCards.Card(title, items));
   }

   public static void tick() {
      if (showing == null) {
         showing = QUEUE.poll();
         age = 0;
      } else if (++age > 128) {
         showing = null;
      }
   }

   public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
      RewardCards.Card c = showing;
      if (c != null) {
         Font font = Minecraft.getInstance().font;
         float t = age + delta.getGameTimeDeltaPartialTick(false);
         float slide = t < 8.0F ? 1.0F - t / 8.0F : (t > 118.0F ? (t - 8.0F - 110.0F) / 10.0F : 0.0F);
         slide = Mth.clamp(slide, 0.0F, 1.0F);
         slide *= slide;
         int rows = Math.min(c.items.size(), 6);
         int w = 172;
         int h = 20 + rows * 18 + 4;

         for (ItemStack s : c.items) {
            w = Math.max(w, 36 + font.width(name(s)));
         }

         w = Math.min(w, 240);
         int x = g.guiWidth() - w - 8 + (int)(slide * (w + 12));
         int y = 40;
         g.fill(x, y, x + w, y + h, -536081902);
         g.fill(x, y, x + 2, y + h, -5410572);
         g.fill(x, y, x + w, y + 1, -12898990);
         g.fill(x, y + h - 1, x + w, y + h, -12898990);
         g.text(font, c.title, x + 10, y + 7, -2503440);
         int ry = y + 21;

         for (int i = 0; i < rows; i++) {
            ItemStack s = c.items.get(i);
            g.item(s, x + 8, ry);
            g.itemDecorations(font, s, x + 8, ry);
            g.text(font, name(s), x + 30, ry + 4, -1646352);
            ry += 18;
         }
      }
   }

   private static Component name(ItemStack s) {
      return s.getHoverName();
   }

   private RewardCards() {
   }

   private record Card(Component title, List<ItemStack> items) {
   }
}
