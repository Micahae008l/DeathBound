package com.deathbound.item;

import com.deathbound.entity.LanternWisp;
import com.deathbound.registry.ModEntities;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class LanternWispItem extends Item {
   public LanternWispItem(Item.Properties properties) {
      super(properties);
   }

   @Override
   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      if (level instanceof ServerLevel server) {
         List<? extends LanternWisp> mine = server.getEntities(ModEntities.LANTERN_WISP, w -> w.ownedBy(player));
         if (!mine.isEmpty()) {
            mine.forEach(w -> w.discard());
            player.sendOverlayMessage(Component.translatable("message.deathbound.wisp.sleep").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            server.playSound(null, player.blockPosition(), SoundEvents.CANDLE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.8F);
         } else {
            LanternWisp wisp = ModEntities.LANTERN_WISP.create(server, EntitySpawnReason.MOB_SUMMONED);
            if (wisp != null) {
               wisp.setOwner(player);
               wisp.snapTo(player.getX(), player.getEyeY(), player.getZ(), player.getYRot(), 0.0F);
               server.addFreshEntity(wisp);
               player.sendOverlayMessage(Component.translatable("message.deathbound.wisp.wake").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
               server.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.4F);
            }
         }

         player.getCooldowns().addCooldown(player.getItemInHand(hand), 20);
      }

      return InteractionResult.SUCCESS;
   }
}
