package com.deathbound.item;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;

// Aldous's lantern for Mira. Each hit its carrier takes makes the flame gutter; after MAX_HITS it is out until relit
// at a soul flame. The count lives in custom_model_data so the item model can show the flame shrinking.
public class AldousLanternItem extends Item {
   public static final int MAX_HITS = 3;

   public AldousLanternItem(Item.Properties properties) {
      super(properties);
   }

   public static int hits(ItemStack stack) {
      CustomModelData data = stack.get(DataComponents.CUSTOM_MODEL_DATA);
      Float f = data == null ? null : data.getFloat(0);
      return f == null ? 0 : Math.round(f);
   }

   public static boolean isOut(ItemStack stack) {
      return hits(stack) >= MAX_HITS;
   }

   public static void setHits(ItemStack stack, int hits) {
      if (hits <= 0) {
         stack.remove(DataComponents.CUSTOM_MODEL_DATA);
      } else {
         stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of((float)Math.min(hits, MAX_HITS)), List.of(), List.of(), List.of()));
      }
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
      int hits = hits(stack);
      if (hits >= MAX_HITS) {
         builder.accept(Component.translatable("item.deathbound.aldous_lantern.out").withStyle(ChatFormatting.DARK_GRAY));
      } else if (hits > 0) {
         builder.accept(Component.translatable("item.deathbound.aldous_lantern.guttering", MAX_HITS - hits).withStyle(ChatFormatting.GOLD));
      } else {
         builder.accept(Component.translatable("item.deathbound.aldous_lantern.lit").withStyle(ChatFormatting.GOLD));
      }
   }
}
