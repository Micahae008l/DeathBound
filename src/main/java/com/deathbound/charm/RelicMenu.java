package com.deathbound.charm;

import com.deathbound.item.CharmItem;
import com.deathbound.registry.ModItems;
import com.deathbound.registry.ModMenus;
import com.deathbound.world.Milestones;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class RelicMenu extends AbstractContainerMenu {
   public static final int SLOT_Y = 34;
   private final SimpleContainer charms;
   private final Player player;
   private final InteractionHand hand;
   public final int slotCount;
   private boolean loaded;

   public RelicMenu(int id, Inventory inventory, Integer slotCount) {
      this(id, inventory, slotCount, InteractionHand.MAIN_HAND);
   }

   public RelicMenu(int id, Inventory inventory, int slotCount, InteractionHand hand) {
      super(ModMenus.RELIC, id);
      this.player = inventory.player;
      this.hand = hand;
      this.slotCount = slotCount;
      this.charms = new SimpleContainer(slotCount) {
         @Override
         public void setChanged() {
            super.setChanged();
            if (RelicMenu.this.loaded) {
               RelicMenu.this.writeBack();
            }
         }
      };
      ItemStack relic = this.relic();
      if (!this.player.level().isClientSide() && relic.is(ModItems.DEATHBOUND_RELIC)) {
         NonNullList<ItemStack> stored = NonNullList.withSize(slotCount, ItemStack.EMPTY);
         relic.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(stored);

         for (int i = 0; i < slotCount; i++) {
            this.charms.setItem(i, stored.get(i));
         }
      }

      this.loaded = !this.player.level().isClientSide();
      int x0 = 88 - slotCount * 13;

      for (int i = 0; i < slotCount; i++) {
         this.addSlot(new RelicMenu.CharmSlot(i, x0 + i * 26 + 4, 34));
      }

      int heldSlot = hand == InteractionHand.MAIN_HAND ? inventory.getSelectedSlot() : -1;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         int index = col;
         this.addSlot(index == heldSlot ? new RelicMenu.LockedSlot(inventory, index, 8 + col * 18, 142) : new Slot(inventory, index, 8 + col * 18, 142));
      }
   }

   private ItemStack relic() {
      return this.player.getItemInHand(this.hand);
   }

   private void writeBack() {
      ItemStack relic = this.relic();
      if (relic.is(ModItems.DEATHBOUND_RELIC)) {
         NonNullList<ItemStack> list = NonNullList.withSize(this.slotCount, ItemStack.EMPTY);

         for (int i = 0; i < this.slotCount; i++) {
            list.set(i, this.charms.getItem(i));
         }

         relic.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(list));
         if (this.player instanceof ServerPlayer sp) {
            long bound = list.stream().filter(s -> !s.isEmpty()).count();
            if (bound > 0) {
               Milestones.award(sp, "bound");
            }

            if (bound == this.slotCount) {
               Milestones.award(sp, "full_hand");
            }
         }
      }
   }

   @Override
   public boolean stillValid(Player player) {
      return player.level().isClientSide() || this.relic().is(ModItems.DEATHBOUND_RELIC);
   }

   @Override
   public ItemStack quickMoveStack(Player player, int slotIndex) {
      Slot slot = this.slots.get(slotIndex);
      if (!slot.hasItem()) {
         return ItemStack.EMPTY;
      }

      ItemStack stack = slot.getItem();
      ItemStack copy = stack.copy();
      if (slotIndex < this.slotCount) {
         if (!this.moveItemStackTo(stack, this.slotCount, this.slots.size(), true)) {
            return ItemStack.EMPTY;
         }
      } else if (!(stack.getItem() instanceof CharmItem) || !this.moveItemStackTo(stack, 0, this.slotCount, false)) {
         return ItemStack.EMPTY;
      }

      if (stack.isEmpty()) {
         slot.setByPlayer(ItemStack.EMPTY);
      } else {
         slot.setChanged();
      }

      return copy;
   }

   private class CharmSlot extends Slot {
      CharmSlot(int index, int x, int y) {
         super(RelicMenu.this.charms, index, x, y);
      }

      @Override
      public boolean mayPlace(ItemStack stack) {
         if (stack.getItem() instanceof CharmItem charm) {
            for (int var6 = 0; var6 < RelicMenu.this.charms.getContainerSize(); var6++) {
               if (var6 != this.getContainerSlot() && RelicMenu.this.charms.getItem(var6).getItem() instanceof CharmItem other && other.charm == charm.charm) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      }

      @Override
      public int getMaxStackSize() {
         return 1;
      }
   }

   private static class LockedSlot extends Slot {
      LockedSlot(Inventory inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }

      @Override
      public boolean mayPickup(Player player) {
         return false;
      }

      @Override
      public boolean mayPlace(ItemStack stack) {
         return false;
      }
   }
}
