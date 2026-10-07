package com.deathbound.item;

import com.deathbound.DeathBound;
import com.deathbound.charm.Charms;
import com.deathbound.registry.ModItems;
import com.deathbound.registry.ModParticles;
import com.deathbound.world.Director;
import com.deathbound.world.Layout;
import com.deathbound.world.UnderworldTravel;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class HeartOfDeathItem extends Item {
   public HeartOfDeathItem(Item.Properties properties) {
      super(properties);
   }

   @Override
   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      if (level instanceof ServerLevel server && player instanceof ServerPlayer sp && server.dimension() == UnderworldTravel.UNDERWORLD) {
         Director.State st = Director.storyState(server);
         if (st.progress() >= 2 && st.ending() == 0) {
            if (Layout.inArena(player.getX(), player.getY(), player.getZ())) {
               server.registryAccess()
                  .lookupOrThrow(Registries.DIALOG)
                  .get(ResourceKey.create(Registries.DIALOG, DeathBound.id("throne")))
                  .ifPresent(sp::openDialog);
            } else {
               player.sendOverlayMessage(Component.translatable("message.deathbound.heart.throne").withStyle(ChatFormatting.LIGHT_PURPLE));
            }

            return InteractionResult.SUCCESS;
         }
      }

      ItemStack relic = Charms.findRelic(player);
      if (relic.isEmpty()) {
         player.sendOverlayMessage(Component.translatable("message.deathbound.heart.no_relic").withStyle(ChatFormatting.GRAY));
         return InteractionResult.FAIL;
      }

      if (relic.getOrDefault(ModItems.AWAKENED, false)) {
         player.sendOverlayMessage(Component.translatable("message.deathbound.heart.already").withStyle(ChatFormatting.GRAY));
         return InteractionResult.FAIL;
      }

      if (level instanceof ServerLevel server) {
         relic.set(ModItems.AWAKENED, true);
         player.getItemInHand(hand).shrink(1);
         server.playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 0.6F, 1.6F);
         server.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, 0.6F);
         server.sendParticles(ModParticles.SOUL_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 120, 0.6, 1.0, 0.6, 0.08);
         player.sendSystemMessage(Component.translatable("message.deathbound.heart.awakened").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
      }

      return InteractionResult.SUCCESS;
   }

   public static void consume(Player player) {
      for (ItemStack stack : player.getInventory()) {
         if (stack.getItem() instanceof HeartOfDeathItem) {
            stack.shrink(1);
            break;
         }
      }

      ItemStack relic = Charms.findRelic(player);
      if (!relic.isEmpty()) {
         relic.set(ModItems.AWAKENED, true);
      }
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
      builder.accept(Component.translatable("item.deathbound.heart_of_death.lore").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
      CharmItem.lines(builder, "item.deathbound.heart_of_death.desc", ChatFormatting.GRAY);
      builder.accept(Component.translatable("item.deathbound.heart_of_death.desc2").withStyle(ChatFormatting.DARK_GRAY));
   }
}
