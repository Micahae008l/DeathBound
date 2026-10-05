package com.underworld.registry;

import com.underworld.Underworld;
import com.underworld.effect.HollowMarkEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public final class ModEffects {
	/** Applied by Hollow Arrows. The Hunter prioritises marked players; the Hollow Bow detonates it. */
	public static final Holder<MobEffect> HOLLOW_MARK = Registry.registerForHolder(
		BuiltInRegistries.MOB_EFFECT, Underworld.id("hollow_mark"), new HollowMarkEffect()
	);

	private ModEffects() {
	}

	public static void init() {
	}
}
