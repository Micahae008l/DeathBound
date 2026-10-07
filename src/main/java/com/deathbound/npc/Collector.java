package com.deathbound.npc;

import com.deathbound.registry.ModAttachments;
import com.deathbound.registry.ModBlocks;
import com.deathbound.registry.ModEffects;
import com.deathbound.registry.ModItems;
import com.deathbound.world.Milestones;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

public final class Collector {
   private static final int COLOR = 9414775;

   public static void collect(ServerPlayer player) {
      List<String> have = new ArrayList<>(player.getAttachedOrElse(ModAttachments.COLLECTED, List.of()));
      int before = have.size();
      int given = 0;

      for (Item artifact : ModItems.ARTIFACTS) {
         String id = BuiltInRegistries.ITEM.getKey(artifact).getPath();
         if (!have.contains(id)) {
            for (ItemStack stack : player.getInventory()) {
               if (stack.is(artifact)) {
                  stack.shrink(1);
                  have.add(id);
                  reward(player, id);
                  say(player, "collector.deathbound." + id);
                  given++;
                  break;
               }
            }
         }
      }

      player.setAttached(ModAttachments.COLLECTED, have);
      if (given > 0) {
         player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_INSERT, SoundSource.NEUTRAL, 1.0F, 0.7F);
         if (before < ModItems.ARTIFACTS.size() && have.size() == ModItems.ARTIFACTS.size()) {
            Rewards.give(
               player,
               Quests.from("collector"),
               new ItemStack(Items.TOTEM_OF_UNDYING),
               new ItemStack(ModItems.GRAVE_RUNE, 3),
               new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)
            );
            say(player, "collector.deathbound.complete");
            Milestones.award(player, "forgotten_things");
         }
      } else {
         say(player, have.size() == ModItems.ARTIFACTS.size() ? "collector.deathbound.done" : "collector.deathbound.nothing");
      }
   }

   private static void reward(ServerPlayer player, String id) {
      ItemStack[] pay = switch (id) {
         case "ferry_coin" -> new ItemStack[]{new ItemStack(ModItems.SOUL, 8)};
         case "wooden_horse" -> new ItemStack[]{PotionContents.createItemStack(Items.POTION, ModEffects.LAST_BREATH_POTION)};
         case "kings_quill" -> new ItemStack[]{new ItemStack(ModItems.GRAVE_RUNE)};
         case "warden_seal" -> new ItemStack[]{new ItemStack(ModItems.GRAVE_RUNE, 2)};
         case "melted_chains" -> new ItemStack[]{new ItemStack(ModItems.SOUL, 16)};
         case "kings_ring" -> new ItemStack[]{new ItemStack(Items.TOTEM_OF_UNDYING)};
         case "hunters_arrowhead" -> new ItemStack[]{
            new ItemStack(ModBlocks.EYE_JAR, 2), PotionContents.createItemStack(Items.POTION, ModEffects.LONG_GRAVE_SIGHT_POTION)
         };
         case "last_drop" -> new ItemStack[]{new ItemStack(ModItems.GRAVE_RUNE, 2), new ItemStack(ModBlocks.SOUL_JAR, 3)};
         default -> new ItemStack[0];
      };
      Rewards.give(player, Quests.from("collector"), pay);
   }

   private static void say(ServerPlayer player, String key) {
      player.sendSystemMessage(
         Component.translatable(
            "deathbound.say",
            Component.translatable("entity.deathbound.collector").withColor(9414775),
            Component.translatable(key).withStyle(ChatFormatting.GRAY)
         )
      );
   }

   private Collector() {
   }
}
