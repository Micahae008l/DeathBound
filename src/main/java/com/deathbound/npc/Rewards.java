package com.deathbound.npc;

import com.deathbound.registry.ModNet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;

public final class Rewards {
   public static void give(ServerPlayer p, Component from, ItemStack... stacks) {
      List<ItemStack> shown = new ArrayList<>();

      for (ItemStack s : stacks) {
         if (!s.isEmpty()) {
            shown.add(s.copy());
            ItemStack left = s.copy();
            if (!p.getInventory().add(left) && !left.isEmpty()) {
               p.drop(left, false, Prediction.SERVER_ONLY);
            }
         }
      }

      show(p, from, shown);
   }

   public static void show(ServerPlayer p, Component from, List<ItemStack> shown) {
      if (!shown.isEmpty()) {
         ModNet.received(p, from, shown);
         p.level().playSound(null, p.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.6F, 0.8F);
      }
   }

   private Rewards() {
   }
}
