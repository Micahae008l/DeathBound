package com.deathbound.story;

import com.deathbound.registry.ModAttachments;
import com.deathbound.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * The stories of the dead. BOOKS turn up in chests as Lost Journals, each one only once per world; NOTES are pinned
 * to trees and posts or left on tables (StoryNoteBlock, its "story" property is the index here). Text lives in the
 * lang file as story.deathbound.<id>.title / .author / .p1..pN, written by tools/stories.py: keep the ids in step.
 */
public final class Stories {
   public static final List<String> BOOKS = List.of(
      "sand_keeps_time", "three_skulls", "bait", "a_soft_hiss", "go_to_bed", "under_me_stars", "wear_the_gold", "a_bed_in_the_nether",
      "the_cat_is_patient", "last_torch", "light_as_snow", "wings", "shh", "bad_omen", "under_the_sea", "kid", "channeling",
      "small_blue_spiders", "straight_down", "the_nice_lady", "return_to_sender", "the_stone_moved", "bad_trade", "cactus_fence"
   );
   public static final List<String> NOTES = List.of(
      "note_ferry", "note_lights", "note_marks", "note_shed", "note_list", "note_yours", "note_tib", "note_lamps"
   );
   /** Set by the client: opens the reading screen for a story. */
   public static Consumer<String> opener = id -> {};

   public static boolean isNote(String id) {
      return NOTES.contains(id);
   }

   public static void init() {
      // a chest's Lost Journal gets a story nobody in this world has found yet; once all are out, a Soul instead
      LootTableEvents.MODIFY_DROPS.register((table, context, drops) -> {
         for (int i = 0; i < drops.size(); i++) {
            ItemStack s = drops.get(i);
            if (s.is(ModItems.STORY_BOOK) && !s.has(ModItems.STORY)) {
               String id = claim(context.getLevel(), context.getRandom());
               drops.set(i, id == null ? new ItemStack(ModItems.SOUL) : book(id));
            }
         }
      });
   }

   private static @Nullable String claim(ServerLevel level, RandomSource random) {
      ServerLevel keeper = level.getServer().overworld();
      List<String> claimed = new ArrayList<>(keeper.getAttachedOrElse(ModAttachments.CLAIMED_STORIES, List.of()));
      List<String> left = BOOKS.stream().filter(b -> !claimed.contains(b)).toList();
      if (left.isEmpty()) {
         return null;
      }

      String id = left.get(random.nextInt(left.size()));
      claimed.add(id);
      keeper.setAttached(ModAttachments.CLAIMED_STORIES, List.copyOf(claimed));
      return id;
   }

   public static ItemStack book(String id) {
      ItemStack s = new ItemStack(ModItems.STORY_BOOK);
      s.set(ModItems.STORY, id);
      return s;
   }

   /** Reading a story for the first time keeps it in the player's Journal. */
   public static void found(ServerPlayer player, String id) {
      List<String> found = new ArrayList<>(player.getAttachedOrElse(ModAttachments.STORIES_FOUND, List.of()));
      if (!found.contains(id)) {
         found.add(id);
         player.setAttached(ModAttachments.STORIES_FOUND, List.copyOf(found));
         player.sendOverlayMessage(
            Component.translatable("lore.deathbound.filed", Component.translatable("story.deathbound." + id + ".title")).withStyle(ChatFormatting.LIGHT_PURPLE)
         );
      }

      player.level().playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, isNote(id) ? 1.3F : 0.9F);
   }

   private Stories() {
   }
}
