package com.deathbound.item;

import com.deathbound.registry.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

/**
 * Lore books don't stack (every one has different text), so they used to fill the inventory.
 * Sneak + use a written book to file its pages into the Underworld Journal; the book is used up.
 * Plain use still reads it as normal.
 */
public final class LoreFiling {
   public static void init() {
      UseItemCallback.EVENT.register((player, level, hand) -> {
         ItemStack stack = player.getItemInHand(hand);
         if (!player.isShiftKeyDown() || !stack.is(Items.WRITTEN_BOOK) || !stack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
            return InteractionResult.PASS;
         }
         if (player instanceof ServerPlayer sp) {
            file(sp, stack);
         }
         return InteractionResult.SUCCESS;
      });
   }

   private static void file(ServerPlayer player, ItemStack book) {
      WrittenBookContent content = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
      String title = content.title().raw();
      List<ItemStack> filed = new ArrayList<>(player.getAttachedOrElse(ModAttachments.LORE_PAGES, List.of()));
      boolean known = filed.stream().anyMatch(s -> {
         WrittenBookContent c = s.get(DataComponents.WRITTEN_BOOK_CONTENT);
         return c != null && c.title().raw().equals(title) && c.pages().equals(content.pages());
      });
      if (!known) {
         filed.add(book.copyWithCount(1));
         player.setAttached(ModAttachments.LORE_PAGES, List.copyOf(filed));
      }
      book.shrink(1);
      player.level().playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 0.8F);
      player.sendOverlayMessage(
         Component.translatable(known ? "lore.deathbound.already_filed" : "lore.deathbound.filed", title).withStyle(ChatFormatting.LIGHT_PURPLE)
      );
   }

   private LoreFiling() {
   }
}
