package com.underworld.registry;

import com.underworld.Underworld;
import com.underworld.item.HollowArrowItem;
import com.underworld.item.HollowBowItem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ItemLore;

public final class ModItems {
	private static final List<Item> TAB_ORDER = new ArrayList<>();

	// ---- Hollow Hunter loot
	public static final Item HOLLOW_BOW = register("hollow_bow", HollowBowItem::new,
		new Item.Properties().durability(640).enchantable(1).rarity(Rarity.EPIC).fireResistant()
			.component(DataComponents.LORE, lore("The Hunter's own bow.", "Hollow Shot: marks a target; hit it again to release its soul.", "Sneak + use: step to your last embedded Hollow Arrow.")));
	public static final Item HOLLOW_ARROW = register("hollow_arrow", HollowArrowItem::new,
		new Item.Properties().rarity(Rarity.UNCOMMON).component(DataComponents.LORE, lore("Glows. Stays where it lands.")));
	public static final Item HUNTERS_EYE = register("hunters_eye", Item::new,
		new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant().component(DataComponents.LORE, lore("It is still looking at something.")));

	// ---- Ancient Artifacts (brought to the Collector)
	public static final Item BROKEN_SOUL_LANTERN = artifact("broken_soul_lantern", Rarity.UNCOMMON, "A tiny soul is still trapped inside.");
	public static final Item ANCIENT_DEATH_COIN = artifact("ancient_death_coin", Rarity.UNCOMMON, "Minted before the Underworld fell.");
	public static final Item WARDENS_SEAL = artifact("wardens_seal", Rarity.RARE, "A cracked symbol of the ancient Warden.");
	public static final Item HOLLOW_CRYSTAL = artifact("hollow_crystal", Rarity.RARE, "Almost no energy left in it.");
	public static final Item FORGOTTEN_CROWN = artifact("forgotten_crown", Rarity.EPIC, "Belonged to a ruler nobody remembers.");

	// ---- Collector room props
	public static final Item SEALED_RELIC = register("sealed_relic", Item::new,
		new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).component(DataComponents.LORE, lore("Not for sale.")));
	public static final Item COLLECTOR_LEDGER = register("collector_ledger", Item::new,
		new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).component(DataComponents.LORE, lore(
			"Lantern - east ruins. Soul still inside.", "Coin - older than the river.", "Seal - ??? (Warden)", "Crystal - empty. Why?", "Crown - whose?", "Arrow - his. Do not follow.")));

	// ---- Spawn eggs
	public static final Item HOLLOW_HUNTER_SPAWN_EGG = register("hollow_hunter_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.HOLLOW_HUNTER));
	public static final Item COLLECTOR_SPAWN_EGG = register("collector_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.COLLECTOR));

	public static final List<Item> ARTIFACTS = List.of(BROKEN_SOUL_LANTERN, ANCIENT_DEATH_COIN, WARDENS_SEAL, HOLLOW_CRYSTAL, FORGOTTEN_CROWN);

	public static final CreativeModeTab TAB = Registry.register(
		BuiltInRegistries.CREATIVE_MODE_TAB,
		Underworld.id("underworld_characters"),
		FabricCreativeModeTab.builder()
			.title(Component.literal("Underworld Characters"))
			.icon(() -> new ItemStack(HOLLOW_BOW))
			.displayItems((params, output) -> TAB_ORDER.forEach(output::accept))
			.build()
	);

	private ModItems() {
	}

	private static Item artifact(String name, Rarity rarity, String line) {
		return register(name, Item::new, new Item.Properties().stacksTo(16).rarity(rarity)
			.component(DataComponents.LORE, lore("Ancient Artifact", line)));
	}

	private static ItemLore lore(String... lines) {
		List<Component> list = new ArrayList<>();
		for (String l : lines) {
			list.add(Component.literal(l).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
		}
		return new ItemLore(list);
	}

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Underworld.id(name));
		Item item = Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
		TAB_ORDER.add(item);
		return item;
	}

	static Item registerBlockItem(String name, Function<Item.Properties, Item> factory) {
		return register(name, factory, new Item.Properties());
	}

	public static void init() {
	}
}
