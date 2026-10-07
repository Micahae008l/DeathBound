package com.deathbound.item;

import com.deathbound.charm.Charm;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;

public class CharmItem extends Item {
   public final Charm charm;

   public CharmItem(Charm charm, Item.Properties properties) {
      super(properties);
      this.charm = charm;
   }

   public static void lines(Consumer<Component> builder, String key, ChatFormatting... style) {
      builder.accept(Component.translatable(key).withStyle(style));

      for (int i = 2; Language.getInstance().has(key + i); i++) {
         builder.accept(Component.translatable(key + i).withStyle(style));
      }
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
      String key = "item.deathbound." + this.charm.id;
      builder.accept(Component.translatable(key + ".lore").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
      lines(builder, key + ".desc", ChatFormatting.GRAY);
      builder.accept(Component.translatable("item.deathbound.charm.bind_hint").withStyle(ChatFormatting.DARK_GRAY));
   }
}
