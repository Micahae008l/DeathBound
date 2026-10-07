package com.deathbound.item;

import com.deathbound.registry.ModItems;
import com.deathbound.story.Stories;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

// A Lost Journal: one of the dead's stories (ModItems.STORY). Use to read; sneak + use to file it in your Journal.
public class StoryBookItem extends Item {
   public StoryBookItem(Item.Properties properties) {
      super(properties);
   }

   @Override
   public Component getName(ItemStack stack) {
      String id = stack.get(ModItems.STORY);
      return id == null ? super.getName(stack) : Component.translatable("story.deathbound." + id + ".title");
   }

   @Override
   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      String id = stack.get(ModItems.STORY);
      if (id == null) {
         return InteractionResult.PASS;
      }

      if (level.isClientSide()) {
         if (!player.isShiftKeyDown()) {
            Stories.opener.accept(id);
         }
      } else if (player instanceof ServerPlayer sp) {
         Stories.found(sp, id);
         if (sp.isShiftKeyDown()) {
            stack.shrink(1);
         }
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
      String id = stack.get(ModItems.STORY);
      if (id != null && Language.getInstance().has("story.deathbound." + id + ".author")) {
         builder.accept(Component.translatable("item.deathbound.story_book.by", Component.translatable("story.deathbound." + id + ".author"))
            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
      }

      builder.accept(Component.translatable("item.deathbound.story_book.hint").withStyle(ChatFormatting.DARK_GRAY));
   }
}
