package com.deathbound.client;

import com.deathbound.DeathBound;
import com.deathbound.charm.RelicMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class RelicScreen extends AbstractContainerScreen<RelicMenu> {
   private static final Identifier TEXTURE = DeathBound.id("textures/gui/relic.png");

   public RelicScreen(RelicMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title, 176, 166);
      this.inventoryLabelY = this.imageHeight - 94;
      this.titleLabelX = 8;
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
      super.extractBackground(graphics, mouseX, mouseY, a);
      int x = (this.width - this.imageWidth) / 2;
      int y = (this.height - this.imageHeight) / 2;
      graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
      int x0 = 88 - this.menu.slotCount * 13;

      for (int i = 0; i < this.menu.slotCount; i++) {
         graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + x0 + i * 26 + 4 - 5, y + 34 - 5, 176.0F, 0.0F, 26, 26, 256, 256);
      }
   }

   @Override
   protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
      graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, -2702603, false);
      graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -4675882, false);
      Component hint = Component.translatable("container.deathbound.relic.hint");
      graphics.text(this.font, hint, (this.imageWidth - this.font.width(hint)) / 2, 62, -7374664, false);
   }
}
