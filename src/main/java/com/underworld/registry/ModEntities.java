package com.underworld.registry;

import com.underworld.Underworld;
import com.underworld.entity.collector.Collector;
import com.underworld.entity.hunter.HollowArrow;
import com.underworld.entity.hunter.HollowHunter;
import com.underworld.entity.hunter.SpectralBolt;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
	public static final EntityType<HollowHunter> HOLLOW_HUNTER = register(
		"hollow_hunter",
		EntityType.Builder.of(HollowHunter::new, MobCategory.MONSTER).sized(0.9F, 3.4F).eyeHeight(3.0F).fireImmune().clientTrackingRange(16)
	);
	public static final EntityType<Collector> COLLECTOR = register(
		"collector",
		EntityType.Builder.of(Collector::new, MobCategory.MISC).sized(0.6F, 1.8F).eyeHeight(1.45F).clientTrackingRange(10)
	);
	public static final EntityType<HollowArrow> HOLLOW_ARROW = register(
		"hollow_arrow",
		EntityType.Builder.<HollowArrow>of(HollowArrow::new, MobCategory.MISC).noLootTable().sized(0.5F, 0.5F).eyeHeight(0.13F).clientTrackingRange(8).updateInterval(20)
	);
	public static final EntityType<SpectralBolt> SPECTRAL_BOLT = register(
		"spectral_bolt",
		EntityType.Builder.<SpectralBolt>of(SpectralBolt::new, MobCategory.MISC).noLootTable().sized(0.4F, 0.4F).clientTrackingRange(8).updateInterval(1)
	);

	private ModEntities() {
	}

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Underworld.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(HOLLOW_HUNTER, HollowHunter.createAttributes());
		FabricDefaultAttributeRegistry.register(COLLECTOR, Collector.createAttributes());
	}
}
