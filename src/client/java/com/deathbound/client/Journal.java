package com.deathbound.client;

import net.minecraft.core.component.DataComponents;

import net.minecraft.world.item.component.WrittenBookContent;

import net.minecraft.world.item.ItemStack;

import com.deathbound.DeathBound;
import com.deathbound.item.AldousLanternItem;
import com.deathbound.npc.Quests;
import com.deathbound.npc.Soulforge;
import com.deathbound.registry.ModAttachments;
import com.deathbound.registry.ModBlocks;
import com.deathbound.registry.ModItems;
import com.deathbound.story.Stories;
import com.deathbound.world.QuestEvents;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen.BookAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public final class Journal {
   // click event on a story title in the Journal; never leaves the client
   private static final Identifier READ = DeathBound.id("read_story");

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
      pages.add(Component.translatable("journal.deathbound.tasks").withStyle(ChatFormatting.BOLD).append("\n\n").append(checklist(q)));

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
            MutableComponent more = s < x.last() ? progress(p, x.id(), s) : Component.empty();
            if (fits(page.copy().append(more))) {
               pages.add(page.append(more));
            } else {
               // too long for one page: the progress goes on the next one, under the quest's name
               pages.add(page);
               pages.add(Component.translatable(k + ".title").withStyle(ChatFormatting.BOLD).append(more));
            }
         }
      }

      // stories of the dead the player has read: a list of titles; clicking one opens it on parchment (StoryScreen)
      List<String> stories = p.getAttachedOrElse(ModAttachments.STORIES_FOUND, List.of());
      MutableComponent contents = Component.empty()
         .append(Component.translatable("journal.deathbound.stories_title").withStyle(ChatFormatting.BOLD))
         .append("\n\n")
         .append(Component.translatable("journal.deathbound.stories_count", stories.size(), Stories.BOOKS.size() + Stories.NOTES.size()).withStyle(ChatFormatting.DARK_PURPLE))
         .append("\n\n")
         .append(
            Component.translatable(stories.isEmpty() ? "journal.deathbound.stories_none" : "journal.deathbound.stories_hint")
               .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
         )
         .append("\n\n");
      for (String id : stories) {
         String k = "story.deathbound." + id;
         Component hover = Language.getInstance().has(k + ".author")
            ? Component.translatable("item.deathbound.story_book.by", Component.translatable(k + ".author"))
            : Component.translatable(k + ".title");
         MutableComponent line = Component.literal(Stories.isNote(id) ? "\u2022 " : "\u25aa ")
            .append(Component.translatable(k + ".title"))
            .withStyle(
               Style.EMPTY
                  .withColor(Stories.isNote(id) ? ChatFormatting.DARK_GRAY : ChatFormatting.BLACK)
                  .withUnderlined(true)
                  .withClickEvent(new ClickEvent.Custom(READ, Optional.of(StringTag.valueOf(id))))
                  .withHoverEvent(new HoverEvent.ShowText(hover))
            );
         if (!fits(contents.copy().append(line))) {
            pages.add(contents);
            contents = Component.empty();
         }

         contents.append(line).append("\n");
      }

      pages.add(contents);
      List<ItemStack> lore = p.getAttachedOrElse(ModAttachments.LORE_PAGES, List.of());
      if (!lore.isEmpty()) {
         pages.add(
            Component.translatable("journal.deathbound.lore_title")
               .withStyle(ChatFormatting.BOLD)
               .append("\n\n")
               .append(Component.translatable("journal.deathbound.lore_count", lore.size()).withStyle(ChatFormatting.DARK_PURPLE))
         );
         for (ItemStack book : lore) {
            WrittenBookContent c = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (c == null) {
               continue;
            }
            List<Component> text = c.getPages(false);
            for (int i = 0; i < text.size(); i++) {
               MutableComponent page = Component.empty();
               if (i == 0) {
                  page.append(Component.literal(c.title().raw()).withStyle(ChatFormatting.BOLD))
                     .append("\n")
                     .append(Component.literal(c.author()).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
                     .append("\n\n");
               }
               pages.add(page.append(text.get(i)));
            }
         }
      }

      if (pages.size() == 3 && stories.isEmpty() && lore.isEmpty() && q.isEmpty()) {
         pages.add(Component.translatable("journal.deathbound.empty").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
      }

      Minecraft.getInstance().gui.setScreen(new BookViewScreen(new BookAccess(pages)) {
         @Override
         protected boolean handleClickEvent(ClickEvent event) {
            if (event instanceof ClickEvent.Custom c && c.id().equals(READ)) {
               c.payload().flatMap(Tag::asString).ifPresent(id -> Minecraft.getInstance().gui.setScreen(new StoryScreen(id, this)));
               return true;
            }

            return super.handleClickEvent(event);
         }
      });
   }

   // every task at a glance: done, in hand, or not met yet
   private static MutableComponent checklist(Map<String, Integer> q) {
      MutableComponent list = Component.empty();
      for (Quests.Quest x : Quests.ALL) {
         int s = q.getOrDefault(x.id(), 0);
         Component name = s > 0 ? Component.translatable("quest.deathbound." + x.id() + ".title") : Component.translatable("journal.deathbound.unknown");
         if (s >= x.last()) {
            list.append(Component.literal("\u2714 ").append(name).withStyle(ChatFormatting.DARK_GREEN));
         } else if (s > 0) {
            list.append(Component.literal("\u2022 ").append(name).withStyle(ChatFormatting.BLACK));
         } else {
            list.append(Component.literal("\u2022 ").append(name).withStyle(ChatFormatting.GRAY));
         }

         list.append("\n");
      }

      return list;
   }

   // a book page shows 14 lines of 114px; anything past that is cut off
   private static boolean fits(Component page) {
      return Minecraft.getInstance().font.split(page, 114).size() <= 14;
   }

   // live progress under a quest that is still open: what the player has on them right now
   private static MutableComponent progress(Player p, String id, int stage) {
      List<Component> lines = new ArrayList<>();
      switch (id) {
         case "mira" -> {
            if (stage == 1) {
               ItemStack lantern = Soulforge.find(p, ModItems.ALDOUS_LANTERN);
               if (lantern.isEmpty()) {
                  lines.add(Component.translatable("journal.deathbound.progress.no_lantern"));
               } else if (AldousLanternItem.isOut(lantern)) {
                  lines.add(Component.translatable("journal.deathbound.progress.lantern_out"));
               } else {
                  lines.add(Component.translatable("journal.deathbound.progress.lantern", AldousLanternItem.MAX_HITS - AldousLanternItem.hits(lantern), AldousLanternItem.MAX_HITS));
               }

               String clue = p.getAttachedOrElse(ModAttachments.QUEST_NOTES, Map.of()).get("mira");
               lines.add(clue == null ? Component.translatable("journal.deathbound.progress.no_clue") : Component.translatable("journal.deathbound.progress.clue", QuestEvents.clue(clue)));
            } else if (Soulforge.count(p, ModItems.MIRAS_RIBBON) > 0) {
               lines.add(Component.translatable("journal.deathbound.progress.ribbon"));
            }
         }
         case "lamps" -> lines.add(Component.translatable("journal.deathbound.progress.jars", Math.min(Soulforge.count(p, ModBlocks.SOUL_JAR.asItem()), 3)));
         case "oar" -> lines.add(found(p, ModItems.FERRYMANS_OAR));
         case "ball" -> lines.add(found(p, ModItems.PIPS_BALL));
         case "name" -> lines.add(found(p, ModItems.SENTRYS_TAG));
         default -> {
         }
      }

      MutableComponent out = Component.empty();
      for (Component line : lines) {
         out.append("\n\n").append(line.copy().withStyle(ChatFormatting.DARK_PURPLE));
      }

      return out;
   }

   private static Component found(Player p, Item item) {
      return Component.translatable(Soulforge.count(p, item) > 0 ? "journal.deathbound.progress.found" : "journal.deathbound.progress.not_found");
   }

   private Journal() {
   }
}
