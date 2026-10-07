package com.deathbound.world;

import com.deathbound.registry.ModItems;
import java.util.Map;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

public final class LootInjection {
   private static final Map<ResourceKey<LootTable>, Float> RELIC_CHANCE = Map.of(
      BuiltInLootTables.ANCIENT_CITY,
      0.12F,
      BuiltInLootTables.STRONGHOLD_CORRIDOR,
      0.06F,
      BuiltInLootTables.WOODLAND_MANSION,
      0.15F,
      BuiltInLootTables.BASTION_TREASURE,
      0.2F,
      BuiltInLootTables.DESERT_PYRAMID,
      0.05F,
      BuiltInLootTables.JUNGLE_TEMPLE,
      0.08F,
      BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_RARE,
      0.1F
   );
   private static final ResourceKey<LootTable> WITHER_SKELETON = ResourceKey.create(
      Registries.LOOT_TABLE, Identifier.withDefaultNamespace("entities/wither_skeleton")
   );

   public static void init() {
      LootTableEvents.MODIFY
         .register(
            (key, table, source, registries) -> {
               if (source.isBuiltin()) {
                  Float chance = RELIC_CHANCE.get(key);
                  if (chance != null) {
                     table.withPool(
                        LootPool.lootPool().add(LootItem.lootTableItem(ModItems.DEATHBOUND_RELIC)).when(LootItemRandomChanceCondition.randomChance(chance))
                     );
                  } else if (key.equals(WITHER_SKELETON)) {
                     table.withPool(
                        LootPool.lootPool()
                           .add(LootItem.lootTableItem(ModItems.DEATHBOUND_RELIC))
                           .when(LootItemKilledByPlayerCondition.killedByPlayer())
                           .when(LootItemRandomChanceCondition.randomChance(0.02F))
                     );
                  }
               }
            }
         );
   }

   private LootInjection() {
   }
}
