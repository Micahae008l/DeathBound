package com.deathbound.item;

import com.deathbound.charm.Charm;
import com.deathbound.charm.Charms;
import com.deathbound.charm.RelicMenu;
import com.deathbound.registry.ModItems;
import com.deathbound.registry.ModParticles;
import com.deathbound.world.Director;
import com.deathbound.world.UnderworldTravel;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class DeathboundRelicItem extends Item {
   public static final int RITE_TICKS = 100;

   public DeathboundRelicItem(Item.Properties properties) {
      super(properties);
   }

   @Override
   public InteractionResult use(Level level, Player player, final InteractionHand hand) {
      ItemStack relic = player.getItemInHand(hand);
      if (player.isSecondaryUseActive()) {
         if (player instanceof ServerPlayer sp) {
            final int slots = Charms.slots(relic);
            sp.openMenu(new ExtendedMenuProvider<Integer>() {
               public Integer getScreenOpeningData(ServerPlayer p) {
                  return slots;
               }

               @Override
               public Component getDisplayName() {
                  return Component.translatable("container.deathbound.relic");
               }

               @Override
               public AbstractContainerMenu createMenu(int id, Inventory inventory, Player p) {
                  return new RelicMenu(id, inventory, slots, hand);
               }
            });
         }

         return InteractionResult.SUCCESS;
      } else {
         boolean openDoor = relic.getOrDefault(ModItems.AWAKENED, false) || Charms.has(player, Charm.OPEN_DOOR);
         if (!UnderworldTravel.inUnderworld(player) && !openDoor) {
            player.sendOverlayMessage(Component.translatable("message.deathbound.relic.offhand").withStyle(ChatFormatting.DARK_PURPLE));
            return InteractionResult.FAIL;
         }

         if (UnderworldTravel.inUnderworld(player)) {
            boolean canFerry = openDoor || Charms.has(player, Charm.FERRYMAN);
            if (!canFerry) {
               player.sendOverlayMessage(Component.translatable("message.deathbound.relic.silent").withStyle(ChatFormatting.DARK_PURPLE));
               return InteractionResult.FAIL;
            }

            if (Director.isInBossFight(player)) {
               player.sendOverlayMessage(Component.translatable("message.deathbound.relic.bound").withStyle(ChatFormatting.DARK_RED));
               return InteractionResult.FAIL;
            }
         }

         player.startUsingItem(hand);
         return InteractionResult.CONSUME;
      }
   }

   @Override
   public int getUseDuration(ItemStack stack, LivingEntity user) {
      return 100;
   }

   @Override
   public ItemUseAnimation getUseAnimation(ItemStack stack) {
      return ItemUseAnimation.TOOT_HORN;
   }

   @Override
   public void onUseTick(Level level, LivingEntity user, ItemStack stack, int ticksRemaining) {
      if (level instanceof ServerLevel server) {
         int elapsed = 100 - ticksRemaining;
         float t = elapsed / 100.0F;
         double angle = elapsed * (0.25 + t * 0.6);

         for (int beat = 0; beat < 2; beat++) {
            double a = angle + beat * 3.141592653589793;
            double r = 1.6 - t * 1.2;
            server.sendParticles(
               ModParticles.SOUL_FLAME, user.getX() + Math.cos(a) * r, user.getY() + 0.2 + t * 1.6, user.getZ() + Math.sin(a) * r, 1, 0.0, 0.0, 0.0, 0.0
            );
         }

         int beat = Mth.lerpInt(t, 24, 6);
         if (elapsed % beat == 0) {
            server.playSound(null, user.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0F + t, 0.8F + t * 0.3F);
         }
      }
   }

   @Override
   public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
      if (user instanceof ServerPlayer player) {
         if (UnderworldTravel.inUnderworld(player)) {
            if (!stack.getOrDefault(ModItems.AWAKENED, false) && !Charms.has(player, Charm.OPEN_DOOR)) {
               Charms.spendFerryman(player);
            }

            UnderworldTravel.returnToLife(player, true);
         } else {
            UnderworldTravel.beginDescent(player);
         }
      }

      return stack;
   }

   @Override
   public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingTime) {
      if (user instanceof ServerPlayer player && remainingTime > 0) {
         player.sendOverlayMessage(Component.translatable("message.deathbound.relic.broken").withStyle(ChatFormatting.GRAY));
      }

      return false;
   }

   @Override
   public boolean isFoil(ItemStack stack) {
      return stack.getOrDefault(ModItems.AWAKENED, false);
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
      builder.accept(Component.translatable("item.deathbound.deathbound_relic.lore").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
      if (stack.getOrDefault(ModItems.AWAKENED, false)) {
         builder.accept(Component.translatable("item.deathbound.deathbound_relic.awakened").withStyle(ChatFormatting.LIGHT_PURPLE));
      }

      builder.accept(Component.translatable("item.deathbound.deathbound_relic.use").withStyle(ChatFormatting.GRAY));
      builder.accept(Component.translatable("item.deathbound.deathbound_relic.sneak").withStyle(ChatFormatting.GRAY));
   }
}
