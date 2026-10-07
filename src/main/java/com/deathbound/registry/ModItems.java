package com.deathbound.registry;

import com.deathbound.DeathBound;
import com.deathbound.charm.Charm;
import com.deathbound.item.CharmItem;
import com.deathbound.item.DeathboundRelicItem;
import com.deathbound.item.HeartOfDeathItem;
import com.deathbound.item.HuntersBowItem;
import com.deathbound.item.JournalItem;
import com.deathbound.item.LanternWispItem;
import com.deathbound.item.ReaperScytheItem;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.Block;

public final class ModItems {
   private static final Map<String, Integer> LORE = new HashMap<>(Map.of("soul", 1, "grave_rune", 1));
   public static final List<Item> ARTIFACTS = new ArrayList<>();
   public static final DataComponentType<Boolean> AWAKENED = Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      DeathBound.id("awakened"),
      DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build()
   );
   public static final DataComponentType<Integer> REAPED = Registry.register(
      BuiltInRegistries.DATA_COMPONENT_TYPE,
      DeathBound.id("reaped"),
      DataComponentType.<Integer>builder().persistent(Codec.intRange(0, 99)).networkSynchronized(ByteBufCodecs.VAR_INT).ignoreSwapAnimation().build()
   );
   public static final Item DEATHBOUND_RELIC = register(
      "deathbound_relic",
      DeathboundRelicItem::new,
      new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant().component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
   );
   public static final Item SOULBOUND_CHARM = charm(Charm.SOULBOUND, new Properties());
   public static final Item SEERS_CHARM = charm(Charm.SEER, new Properties());
   public static final Item WRAITHS_CHARM = charm(Charm.WRAITH, new Properties());
   public static final Item FERRYMANS_CHARM = charm(Charm.FERRYMAN, new Properties().durability(3));
   public static final Item REAPERS_CHARM = charm(Charm.REAPER, new Properties());
   public static final Item OPEN_DOOR_CHARM = charm(Charm.OPEN_DOOR, new Properties().fireResistant());
   public static final Item HUNTERS_CHARM = charm(Charm.HUNTER, new Properties().fireResistant());
   public static final Item SOUL = register("soul", Item::new, new Properties().rarity(Rarity.UNCOMMON));
   public static final Item GRAVE_RUNE = register("grave_rune", Item::new, new Properties().rarity(Rarity.RARE));
   public static final Item HEART_OF_DEATH = register("heart_of_death", HeartOfDeathItem::new, new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
   public static final Item FERRY_COIN = artifact("ferry_coin");
   public static final Item WOODEN_HORSE = artifact("wooden_horse");
   public static final Item KINGS_QUILL = artifact("kings_quill");
   public static final Item WARDEN_SEAL = artifact("warden_seal");
   public static final Item MELTED_CHAINS = artifact("melted_chains");
   public static final Item KINGS_RING = artifact("kings_ring");
   public static final Item HUNTERS_ARROWHEAD = artifact("hunters_arrowhead");
   public static final Item LAST_DROP = artifact("last_drop");
   public static final Item HUNTERS_BOW = lore(
      "hunters_bow", 3, HuntersBowItem::new, new Properties().durability(640).enchantable(1).rarity(Rarity.EPIC).fireResistant()
   );
   public static final Item UNDERWORLD_JOURNAL = lore("underworld_journal", 1, JournalItem::new, new Properties().stacksTo(1));
   public static final Item ALDOUS_LANTERN = lore("aldous_lantern", 1, Item::new, new Properties().stacksTo(1));
   public static final Item MIRAS_RIBBON = lore("miras_ribbon", 1, Item::new, new Properties().stacksTo(1));
   public static final Item FERRYMANS_OAR = lore("ferrymans_oar", 1, Item::new, new Properties().stacksTo(1));
   public static final Item PIPS_BALL = lore("pips_ball", 1, Item::new, new Properties().stacksTo(1));
   public static final Item SENTRYS_TAG = lore("sentrys_tag", 1, Item::new, new Properties().stacksTo(1));
   public static final Item LANTERN_WISP = lore("lantern_wisp", 2, LanternWispItem::new, new Properties().stacksTo(1).rarity(Rarity.RARE));
   public static final Item ASHEN_LILY = register(
      "ashen_lily", p -> new PlaceOnWaterBlockItem(ModBlocks.ASHEN_LILY, p), new Properties().useBlockDescriptionPrefix()
   );
   public static final Item SOULWATER_BUCKET = register(
      "soulwater_bucket", p -> new BucketItem(ModFluids.SOULWATER, p), new Properties().craftRemainder(Items.BUCKET).stacksTo(1)
   );
   public static final Item SPENT_CHAIN = lore("spent_chain", 2, Item::new, new Properties().stacksTo(1).fireResistant());
   public static final Item SIGIL_KINGS = sigil("sigil_kings");
   public static final Item SIGIL_WATCHERS = sigil("sigil_watchers");
   public static final Item SIGIL_BELL = sigil("sigil_bell");
   public static final Item REAPER_SCYTHE = register(
      "reaper_scythe",
      ReaperScytheItem::new,
      new Properties()
         .rarity(Rarity.EPIC)
         .fireResistant()
         .durability(1800)
         .enchantable(15)
         .repairable(TagKey.create(Registries.ITEM, DeathBound.id("reaper_scythe_repair")))
         .attributes(
            ItemAttributeModifiers.builder()
               .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 10.0, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
               .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.05, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
               .add(
                  Attributes.ENTITY_INTERACTION_RANGE,
                  new AttributeModifier(DeathBound.id("scythe_reach"), 1.0, Operation.ADD_VALUE),
                  EquipmentSlotGroup.MAINHAND
               )
               .build()
         )
         .component(DataComponents.WEAPON, new Weapon(1))
         .component(DataComponents.ATTACK_RANGE, new AttackRange(0.0F, 4.0F, 0.0F, 6.0F, 0.3F, 1.0F))
         .component(DataComponents.ATTACK_ANIMATION, new SwingAnimation(SwingAnimationType.NONE, 13))
         .component(REAPED, 0)
   );
   public static final Item SOUL_BOLT = register("soul_bolt", Item::new, new Properties());
   public static final Item DEATH_ORB = register("death_orb", Item::new, new Properties());
   public static final Item GRAVEBOUND_SPAWN_EGG = egg("gravebound", ModEntities.GRAVEBOUND);
   public static final Item SOUL_WISP_SPAWN_EGG = egg("soul_wisp", ModEntities.SOUL_WISP);
   public static final Item DEATHS_GUARD_SPAWN_EGG = egg("deaths_guard", ModEntities.DEATHS_GUARD);
   public static final Item DEATH_SPAWN_EGG = egg("death", ModEntities.DEATH);
   public static final Item HOLLOW_HUNTER_SPAWN_EGG = egg("hollow_hunter", ModEntities.HOLLOW_HUNTER);
   public static final Item LOST_SOUL_SPAWN_EGG = egg("lost_soul", ModEntities.LOST_SOUL);
   public static final ResourceKey<CreativeModeTab> TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, DeathBound.id("main"));

   private static Item lore(String name, int lines, Function<Properties, Item> factory, Properties props) {
      LORE.put(name, lines);
      return register(name, factory, props);
   }

   private static Item sigil(String name) {
      LORE.put(name, 2);
      return register(
         name, Item::new, new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
   }

   private static Item artifact(String name) {
      LORE.put(name, 2);
      Item item = register(name, Item::new, new Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant());
      ARTIFACTS.add(item);
      return item;
   }

   private static Item charm(Charm charm, Properties props) {
      return register(charm.id, p -> new CharmItem(charm, p), props.stacksTo(1).rarity(charm.rarity));
   }

   private static Item egg(String mob, EntityType<?> type) {
      return register(mob + "_spawn_egg", SpawnEggItem::new, new Properties().spawnEgg(type));
   }

   private static Item register(String name, Function<Properties, Item> factory, Properties props) {
      Integer lines = LORE.get(name);
      if (lines != null) {
         List<Component> l = new ArrayList<>();

         for (int i = 0; i < lines; i++) {
            l.add(Component.translatable("item.deathbound." + name + ".lore" + i).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
         }

         props.component(DataComponents.LORE, new ItemLore(l));
      }

      ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, DeathBound.id(name));
      return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props.setId(key)));
   }

   public static void init() {
      Registry.register(
         BuiltInRegistries.CREATIVE_MODE_TAB,
         TAB,
         FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.deathbound"))
            .icon(() -> new ItemStack(DEATHBOUND_RELIC))
            .displayItems((params, out) -> {
               out.accept(DEATHBOUND_RELIC);

               for (Charm c : Charm.values()) {
                  out.accept(c.item());
               }

               out.accept(REAPER_SCYTHE);
               out.accept(HUNTERS_BOW);
               out.accept(SOUL);
               out.accept(GRAVE_RUNE);
               out.accept(HEART_OF_DEATH);
               ARTIFACTS.forEach(out::accept);
               out.accept(SIGIL_KINGS);
               out.accept(SIGIL_WATCHERS);
               out.accept(SIGIL_BELL);

               for (Block b : ModBlocks.WITH_ITEMS) {
                  out.accept(b);
               }

               out.accept(GRAVEBOUND_SPAWN_EGG);
               out.accept(SOUL_WISP_SPAWN_EGG);
               out.accept(DEATHS_GUARD_SPAWN_EGG);
               out.accept(DEATH_SPAWN_EGG);
               out.accept(HOLLOW_HUNTER_SPAWN_EGG);
               out.accept(LOST_SOUL_SPAWN_EGG);
            })
            .build()
      );
   }

   private ModItems() {
   }
}
