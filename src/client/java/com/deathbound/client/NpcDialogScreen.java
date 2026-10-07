package com.deathbound.client;

import com.deathbound.npc.UnderworldNpc;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.DialogConnectionAccess;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Custom;
import net.minecraft.network.chat.ClickEvent.ShowDialog;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.MultiActionDialog;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public class NpcDialogScreen extends Screen {
   private static final float CHARS_PER_TICK = 3.2F;
   private final MultiActionDialog dialog;
   private final DialogConnectionAccess access;
   private final @Nullable Screen previous;
   private final NpcDialogScreen.Voice voice;
   private final String id;
   private final List<List<NpcDialogScreen.Line>> pages = new ArrayList<>();
   private final List<Button> answers = new ArrayList<>();
   private int page;
   private int total;
   private int hold;
   private int sinceDone;
   private float shown;
   private int panelX;
   private int panelY;
   private int panelW;
   private int panelH;
   private int textW;

   public NpcDialogScreen(MultiActionDialog dialog, String id, DialogConnectionAccess access, @Nullable Screen previous) {
      super(dialog.common().title());
      this.dialog = dialog;
      this.access = access;
      this.previous = previous;
      this.id = id;
      this.voice = id.startsWith("ferryman")
         ? new NpcDialogScreen.Voice(SoundEvents.NOTE_BLOCK_BASS.value(), 0.55F)
         : (
            id.startsWith("prophet") || id.startsWith("king")
               ? new NpcDialogScreen.Voice(SoundEvents.NOTE_BLOCK_CHIME.value(), 0.62F)
               : (
                  id.startsWith("throne")
                     ? new NpcDialogScreen.Voice(SoundEvents.NOTE_BLOCK_BASS.value(), 0.45F)
                     : (
                        id.startsWith("collector")
                           ? new NpcDialogScreen.Voice(SoundEvents.NOTE_BLOCK_BIT.value(), 0.7F)
                           : (
                              id.startsWith("bonesmith")
                                 ? new NpcDialogScreen.Voice(SoundEvents.NOTE_BLOCK_XYLOPHONE.value(), 0.5F)
                                 : (
                                    !id.startsWith("mira") && !id.startsWith("kid")
                                       ? new NpcDialogScreen.Voice(SoundEvents.NOTE_BLOCK_BASS.value(), 0.85F)
                                       : new NpcDialogScreen.Voice(SoundEvents.NOTE_BLOCK_BELL.value(), 1.1F)
                                 )
                           )
                     )
               )
         );
   }

   public @Nullable Screen previous() {
      return this.previous;
   }

   public String id() {
      return this.id;
   }

   @Override
   protected void init() {
      this.panelW = Math.min(this.width - 24, 480);
      this.panelX = (this.width - this.panelW) / 2;
      int buttonsW = Math.min(170, this.panelW * 2 / 5);
      this.textW = this.panelW - buttonsW - 30;
      this.pages.clear();

      for (DialogBody body : this.dialog.common().body()) {
         if (body instanceof PlainMessage msg) {
            Style style = msg.contents().getStyle();
            List<NpcDialogScreen.Line> lines = new ArrayList<>();

            for (FormattedText part : this.font.getSplitter().splitLines(msg.contents().getString(), this.textW, style)) {
               lines.add(new NpcDialogScreen.Line(part.getString(), style));
            }

            this.pages.add(lines);
         }
      }

      if (this.pages.isEmpty()) {
         this.pages.add(List.of());
      }

      this.page = Mth.clamp(this.page, 0, this.pages.size() - 1);
      this.total = this.pageLength();
      List<ActionButton> buttons = new ArrayList<>(this.dialog.actions());
      this.dialog.exitAction().ifPresent(buttons::add);
      int buttonsH = buttons.size() * 18;
      int tallest = this.pages.stream().mapToInt(List::size).max().orElse(1);
      this.panelH = Math.max(18 + tallest * 10 + 12, buttonsH + 4) + 16;
      this.panelY = this.height - this.panelH - 12;
      this.answers.clear();
      int by = this.panelY + 10 + Math.max(0, (this.panelH - 20 - buttonsH) / 2);

      for (ActionButton ab : buttons) {
         Button b = Button.builder(ab.button().label(), btn -> this.choose(ab))
            .bounds(this.panelX + this.panelW - buttonsW - 10, by, buttonsW, 16)
            .tooltip(ab.button().tooltip().map(Tooltip::create).orElse(null))
            .build();
         b.visible = this.lastPage() && this.done();
         this.answers.add(this.addRenderableWidget(b));
         by += 18;
      }
   }

   private int pageLength() {
      return this.pages.get(this.page).stream().mapToInt(l -> l.text.length()).sum();
   }

   private boolean lastPage() {
      return this.page >= this.pages.size() - 1;
   }

   private boolean done() {
      return this.shown >= this.total;
   }

   private void finish() {
      this.shown = this.total;
   }

   private void turn(int to) {
      boolean backward = to < this.page;
      this.page = Mth.clamp(to, 0, this.pages.size() - 1);
      this.total = this.pageLength();
      this.shown = backward ? this.total : 0.0F;
      this.hold = 2;
      this.sinceDone = 0;
      this.answers.forEach(b -> b.visible = false);
   }

   private void choose(ActionButton ab) {
      Optional<ClickEvent> event = ab.action().flatMap(a -> a.createAction(Map.of()));
      if (event.isEmpty()) {
         this.onClose();
      } else {
         switch ((ClickEvent)event.get()) {
            case ShowDialog show:
               this.access.openDialog(show.dialog(), this);
               break;
            case Custom custom:
               this.access.sendCustomAction(custom.id(), custom.payload());
               this.minecraft.gui.setScreen(this.previous);
               break;
            default:
               this.onClose();
         }
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (this.done()) {
         this.sinceDone++;
         if (this.lastPage()) {
            this.answers.forEach(b -> b.visible = true);
         }
      } else {
         this.keepTalking();
         if (this.hold > 0) {
            this.hold--;
         } else {
            int before = (int)this.shown;
            this.shown = Math.min(this.total, this.shown + 3.2F);
            int after = (int)this.shown;

            for (int i = before; i < after; i++) {
               char c = this.charAt(i);
               if (i % 3 == 0 && Character.isLetterOrDigit(c)) {
                  float pitch = this.voice.pitch * (0.92F + (float)Math.random() * 0.16F);
                  this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(this.voice.sound, pitch, 0.2F));
               }

               if ((c == '.' || c == '?' || c == '!') && i + 1 < this.total) {
                  this.hold = 4;
                  this.shown = i + 1;
                  break;
               }

               if (c == ',' || c == ';') {
                  this.hold = 1;
                  this.shown = i + 1;
                  break;
               }
            }
         }
      }
   }

   private char charAt(int index) {
      for (NpcDialogScreen.Line l : this.pages.get(this.page)) {
         if (index < l.text.length()) {
            return l.text.charAt(index);
         }

         index -= l.text.length();
      }

      return ' ';
   }

   private void keepTalking() {
      if (this.minecraft.player != null && this.minecraft.level != null && this.minecraft.player.tickCount % 38 == 0) {
         this.minecraft
            .level
            .getEntitiesOfClass(UnderworldNpc.class, this.minecraft.player.getBoundingBox().inflate(10.0))
            .stream()
            .min(Comparator.comparingDouble(this.minecraft.player::distanceToSqr))
            .ifPresent(npc -> npc.talkAnim.start(npc.tickCount));
      }
   }

   private boolean onBackArrow(double mx, double my) {
      int x = this.panelX + 10;
      int y = this.panelY + this.panelH - 14;
      return this.page > 0 && mx >= x && mx <= x + 12 && my >= y - 2 && my <= y + 10;
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (this.onBackArrow(event.x(), event.y())) {
         this.turn(this.page - 1);
         return true;
      } else if (!this.done()) {
         this.finish();
         return true;
      } else if (!this.lastPage() && event.x() < this.panelX + this.panelW - 180) {
         this.turn(this.page + 1);
         return true;
      } else {
         return super.mouseClicked(event, doubleClick);
      }
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      int k = event.key();
      if (k == 80 && this.page > 0) {
         this.turn(this.page - 1);
         return true;
      }

      if (k == 44 || k == 40 || k == 79) {
         if (!this.done()) {
            this.finish();
            return true;
         }

         if (!this.lastPage()) {
            this.turn(this.page + 1);
            return true;
         }
      }

      return super.keyPressed(event);
   }

   public void harnessNext() {
      if (!this.done()) {
         this.finish();
      } else if (!this.lastPage()) {
         this.turn(this.page + 1);
      }
   }

   public void harnessPick(int n) {
      List<ActionButton> buttons = new ArrayList<>(this.dialog.actions());
      this.dialog.exitAction().ifPresent(buttons::add);
      n = n < 0 ? buttons.size() + n : n;
      if (n >= 0 && n < buttons.size()) {
         this.choose(buttons.get(n));
      }
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.previous);
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
      graphics.fillGradient(0, this.height / 2, this.width, this.height, 0, -1342177280);
      graphics.fill(this.panelX, this.panelY, this.panelX + this.panelW, this.panelY + this.panelH, -435418606);
      int edge = -12898990;
      graphics.fill(this.panelX, this.panelY, this.panelX + this.panelW, this.panelY + 1, edge);
      graphics.fill(this.panelX, this.panelY + this.panelH - 1, this.panelX + this.panelW, this.panelY + this.panelH, edge);
      graphics.fill(this.panelX, this.panelY, this.panelX + 1, this.panelY + this.panelH, edge);
      graphics.fill(this.panelX + this.panelW - 1, this.panelY, this.panelX + this.panelW, this.panelY + this.panelH, edge);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
      int x = this.panelX + 12;
      int y = this.panelY + 9;
      graphics.text(this.font, this.title, x, y, -1);
      if (this.pages.size() > 1) {
         String count = this.page + 1 + "/" + this.pages.size();
         graphics.text(this.font, count, this.panelX + 12 + this.textW - this.font.width(count), y, -10595216);
      }

      y += 16;
      int left = (int)this.shown;

      for (NpcDialogScreen.Line l : this.pages.get(this.page)) {
         if (left <= 0) {
            break;
         }

         String part = l.text.length() <= left ? l.text : l.text.substring(0, left);
         left -= l.text.length();
         int color = l.style.getColor() == null ? -1646352 : ARGB.opaque(l.style.getColor().getValue());
         graphics.text(this.font, Component.literal(part).withStyle(l.style), x, y, color);
         y += 10;
      }

      boolean blink = this.minecraft.player == null || this.minecraft.player.tickCount / 8 % 2 == 0;
      int by = this.panelY + this.panelH - 14;
      if (!this.done()) {
         if (blink) {
            graphics.text(this.font, "...", this.panelX + 12 + this.textW - 12, by, -7701848);
         }
      } else if (!this.lastPage()) {
         graphics.text(this.font, blink ? "▶" : "▷", this.panelX + 12 + this.textW - 8, by, -4616961);
      }

      if (this.page > 0) {
         boolean over = this.onBackArrow(mouseX, mouseY);
         graphics.text(this.font, "◀", this.panelX + 10, by, over ? -1646352 : -9542784);
      }

      float fade = Mth.clamp(this.sinceDone / 6.0F, 0.0F, 1.0F);

      for (Button b : this.answers) {
         b.setAlpha(fade);
      }

      super.extractRenderState(graphics, mouseX, mouseY, a);
   }

   private record Line(String text, Style style) {
   }

   private record Voice(SoundEvent sound, float pitch) {
   }
}
