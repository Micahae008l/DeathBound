package com.deathbound.item;

import java.util.function.Consumer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class JournalItem extends Item {
   public static Consumer<Player> opener = p -> {};

   public JournalItem(Item.Properties properties) {
      super(properties);
   }

   @Override
   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      if (level.isClientSide()) {
         opener.accept(player);
      }

      return InteractionResult.SUCCESS;
   }
}
