package com.deathbound.client;

import com.deathbound.npc.Quests;
import com.deathbound.registry.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen.BookAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public final class Journal {
   public static void open(Player p) {
      Map<String, Integer> q = p.getAttachedOrElse(ModAttachments.QUESTS, Map.of());
      List<Component> pages = new ArrayList<>();
      long open = Quests.ALL.stream().filter(xx -> q.getOrDefault(xx.id(), 0) > 0 && q.getOrDefault(xx.id(), 0) < xx.last()).count();
      long done = Quests.ALL.stream().filter(xx -> q.getOrDefault(xx.id(), 0) >= xx.last()).count();
      pages.add(
         Component.translatable("journal.deathbound.title")
            .withStyle(ChatFormatting.BOLD)
            .append("\n\n")
            .append(Component.translatable("journal.deathbound.intro").withStyle(ChatFormatting.RESET))
            .append("\n\n")
            .append(Component.translatable("journal.deathbound.count", open, done).withStyle(ChatFormatting.DARK_PURPLE))
      );

      for (Quests.Quest x : Quests.ALL) {
         int s = q.getOrDefault(x.id(), 0);
         if (s > 0) {
            String k = "quest.deathbound." + x.id();
            MutableComponent page = Component.translatable(k + ".title")
               .withStyle(ChatFormatting.BOLD)
               .append("\n")
               .append(Component.translatable(k + ".giver").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
               .append("\n\n");
            page.append(
               Component.translatable(k + ".stage" + Math.min(s, x.last())).withStyle(s >= x.last() ? ChatFormatting.DARK_GREEN : ChatFormatting.BLACK)
            );
            pages.add(page);
         }
      }

      if (pages.size() == 1) {
         pages.add(Component.translatable("journal.deathbound.empty").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
      }

      Minecraft.getInstance().gui.setScreen(new BookViewScreen(new BookAccess(pages)));
   }

   private Journal() {
   }
}
