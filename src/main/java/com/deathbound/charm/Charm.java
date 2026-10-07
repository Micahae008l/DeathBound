package com.deathbound.charm;

import com.deathbound.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public enum Charm {
   SOULBOUND("soulbound_charm", Rarity.EPIC),
   SEER("seers_charm", Rarity.RARE),
   WRAITH("wraiths_charm", Rarity.RARE),
   FERRYMAN("ferrymans_charm", Rarity.RARE),
   REAPER("reapers_charm", Rarity.EPIC),
   OPEN_DOOR("open_door_charm", Rarity.EPIC),
   HUNTER("hunters_charm", Rarity.EPIC),
   COLLECTOR("collectors_charm", Rarity.RARE),
   PHANTOM("phantom_charm", Rarity.EPIC);

   public final String id;
   public final Rarity rarity;

   Charm(String id, Rarity rarity) {
      this.id = id;
      this.rarity = rarity;
   }

   public Item item() {
      return switch (this) {
         case SOULBOUND -> ModItems.SOULBOUND_CHARM;
         case SEER -> ModItems.SEERS_CHARM;
         case WRAITH -> ModItems.WRAITHS_CHARM;
         case FERRYMAN -> ModItems.FERRYMANS_CHARM;
         case REAPER -> ModItems.REAPERS_CHARM;
         case OPEN_DOOR -> ModItems.OPEN_DOOR_CHARM;
         case HUNTER -> ModItems.HUNTERS_CHARM;
         case COLLECTOR -> ModItems.COLLECTORS_CHARM;
         case PHANTOM -> ModItems.PHANTOM_CHARM;
      };
   }
}
