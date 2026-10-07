package com.deathbound.client;

import com.deathbound.story.Stories;
import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

/**
 * Reads one of the dead's stories on a sheet of parchment sized to its text: title, who wrote it, a divider, then the
 * paragraphs. A Lost Journal is a wider page that turns when it runs long; a note is a small sheet with a nail in it.
 */
public class StoryScreen extends Screen {
   private static final int INK = 0xFF3A2A1C;
   private static final int FADED = 0xFF7A6450;
   private static final int RULE = 0xFF9C8460;
   private static final int PAPER = 0xFFE9DCC0;
   private static final int PAPER_EDGE = 0xFFC9B48A;
   private static final int PAPER_DARK = 0xFF9E8862;
   private static final int LINE = 10;
   private static final int GAP = 6;
   private final String id;
   private final boolean note;
   private final @Nullable Screen parent;
   private final List<List<Row>> pages = new ArrayList<>();
   private int page;
   private int panelX;
   private int panelY;
   private int panelW;
   private int panelH;

   public StoryScreen(String id) {
      this(id, null);
   }

   /** Opened from the Journal: closing it goes back there. */
   public StoryScreen(String id, @Nullable Screen parent) {
      super(Component.translatable("story.deathbound." + id + ".title"));
      this.id = id;
      this.note = Stories.isNote(id);
      this.parent = parent;
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.parent);
   }

   private record Row(FormattedCharSequence text, int color, boolean centered, int gapBefore, boolean divider) {
   }

   @Override
   protected void init() {
      this.panelW = this.note ? 170 : 236;
      int pad = this.note ? 14 : 18;
      int textW = this.panelW - pad * 2;
      String key = "story.deathbound." + this.id;
      Language lang = Language.getInstance();

      List<Row> head = new ArrayList<>();
      for (FormattedCharSequence l : this.font.split(this.title.copy().withStyle(ChatFormatting.BOLD), textW)) {
         head.add(new Row(l, INK, true, 0, false));
      }

      if (lang.has(key + ".author")) {
         Component by = Component.translatable(this.note ? "story.deathbound.signed" : "item.deathbound.story_book.by", Component.translatable(key + ".author"))
            .withStyle(ChatFormatting.ITALIC);
         for (FormattedCharSequence l : this.font.split(by, textW)) {
            head.add(new Row(l, FADED, true, head.isEmpty() ? 0 : 2, false));
         }
      }

      head.add(new Row(FormattedCharSequence.EMPTY, RULE, true, GAP, true));
      List<List<Row>> paragraphs = new ArrayList<>();
      for (int i = 1; lang.has(key + ".p" + i); i++) {
         List<Row> p = new ArrayList<>();
         List<FormattedCharSequence> lines = this.font.split(Component.translatable(key + ".p" + i), textW);
         for (int j = 0; j < lines.size(); j++) {
            p.add(new Row(lines.get(j), INK, false, j == 0 ? GAP : 0, false));
         }

         paragraphs.add(p);
      }

      // fill pages up to the screen's height; a paragraph never splits across pages
      int maxBody = Math.min(this.height - 60, this.note ? 220 : 200);
      List<Row> current = new ArrayList<>(head);
      for (List<Row> p : paragraphs) {
         if (!current.isEmpty() && height(current) + height(p) > maxBody && current.size() > head.size()) {
            this.pages.add(current);
            current = new ArrayList<>();
         }

         current.addAll(p);
      }

      this.pages.add(current);
      int tallest = this.pages.stream().mapToInt(StoryScreen::height).max().orElse(0);
      this.panelH = tallest + pad * 2 + (this.pages.size() > 1 ? 14 : 0) + (this.note ? 6 : 0);
      this.panelX = (this.width - this.panelW) / 2;
      this.panelY = (this.height - this.panelH) / 2;
      this.page = Math.min(this.page, this.pages.size() - 1);
   }

   private static int height(List<Row> rows) {
      int h = 0;
      for (Row r : rows) {
         h += r.gapBefore + (r.divider ? 5 : LINE);
      }

      return h;
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
      g.fill(0, 0, this.width, this.height, 0x88000000);
      int x0 = this.panelX;
      int y0 = this.panelY;
      int x1 = x0 + this.panelW;
      int y1 = y0 + this.panelH;
      // shadow, paper, a darker rim and softly browned edges
      g.fill(x0 + 4, y0 + 4, x1 + 4, y1 + 4, 0x66000000);
      g.fill(x0, y0, x1, y1, PAPER);
      g.fillGradient(x0, y0, x1, y0 + 10, 0x40806040, 0x00806040);
      g.fillGradient(x0, y1 - 12, x1, y1, 0x00806040, 0x50806040);
      g.fill(x0, y0, x0 + 3, y1, 0x30806040);
      g.fill(x1 - 3, y0, x1, y1, 0x30806040);
      g.outline(x0, y0, this.panelW, this.panelH, PAPER_DARK);
      g.outline(x0 + 1, y0 + 1, this.panelW - 2, this.panelH - 2, PAPER_EDGE);
      if (this.note) {
         // the nail it was pinned with, and a torn corner
         int cx = x0 + this.panelW / 2;
         g.fill(cx - 2, y0 + 4, cx + 2, y0 + 8, 0xFF4A4550);
         g.fill(cx - 1, y0 + 4, cx, y0 + 5, 0xFF9A96A6);
         g.fill(x1 - 6, y1 - 3, x1, y1, 0x88000000);
         g.fill(x1 - 3, y1 - 6, x1, y1 - 3, 0x88000000);
      } else {
         // a ribbon bookmark hanging over the top edge
         g.fill(x1 - 30, y0 - 3, x1 - 25, y0 + 22, 0xFF7A3CC2);
         g.fill(x1 - 30, y0 - 3, x1 - 28, y0 + 22, 0xFFA868F0);
      }
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
      super.extractRenderState(g, mouseX, mouseY, a);
      int pad = this.note ? 14 : 18;
      int x = this.panelX + pad;
      int w = this.panelW - pad * 2;
      int y = this.panelY + pad + (this.note ? 6 : 0);
      for (Row r : this.pages.get(this.page)) {
         y += r.gapBefore;
         if (r.divider) {
            int cx = this.panelX + this.panelW / 2;
            g.fill(cx - 34, y + 2, cx - 4, y + 3, RULE);
            g.fill(cx + 4, y + 2, cx + 34, y + 3, RULE);
            g.fill(cx - 1, y, cx + 1, y + 5, RULE);
            y += 5;
         } else {
            int tx = r.centered ? x + (w - this.font.width(r.text)) / 2 : x;
            g.text(this.font, r.text, tx, y, r.color, false);
            y += LINE;
         }
      }

      if (this.pages.size() > 1) {
         int by = this.panelY + this.panelH - 14;
         String count = (this.page + 1) + " / " + this.pages.size();
         g.text(this.font, count, this.panelX + (this.panelW - this.font.width(count)) / 2, by, FADED, false);
         if (this.page > 0) {
            g.text(this.font, "◀", this.panelX + pad, by, this.overBack(mouseX, mouseY) ? INK : FADED, false);
         }

         if (this.page < this.pages.size() - 1) {
            g.text(this.font, "▶", this.panelX + this.panelW - pad - 6, by, this.overNext(mouseX, mouseY) ? INK : FADED, false);
         }
      }
   }

   private boolean overBack(double mx, double my) {
      int by = this.panelY + this.panelH - 14;
      return this.page > 0 && mx >= this.panelX + 10 && mx <= this.panelX + 40 && my >= by - 4 && my <= by + 12;
   }

   private boolean overNext(double mx, double my) {
      int by = this.panelY + this.panelH - 14;
      return this.page < this.pages.size() - 1 && mx >= this.panelX + this.panelW - 40 && mx <= this.panelX + this.panelW - 10 && my >= by - 4 && my <= by + 12;
   }

   private void turn(int to) {
      this.page = to;
      this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
   }

   public void turnPage(int to) {
      if (to >= 0 && to < this.pages.size()) {
         this.turn(to);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (this.overBack(event.x(), event.y())) {
         this.turn(this.page - 1);
         return true;
      } else if (this.overNext(event.x(), event.y())) {
         this.turn(this.page + 1);
         return true;
      } else if (event.x() < this.panelX || event.x() > this.panelX + this.panelW || event.y() < this.panelY || event.y() > this.panelY + this.panelH) {
         this.onClose();
         return true;
      } else {
         return super.mouseClicked(event, doubleClick);
      }
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      int k = event.key();
      if ((k == InputConstants.KEY_LEFT || k == InputConstants.KEY_PAGEUP) && this.page > 0) {
         this.turn(this.page - 1);
         return true;
      } else if ((k == InputConstants.KEY_RIGHT || k == InputConstants.KEY_PAGEDOWN || k == InputConstants.KEY_SPACE) && this.page < this.pages.size() - 1) {
         this.turn(this.page + 1);
         return true;
      } else {
         return super.keyPressed(event);
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }
}
