package com.underworld.effect;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class HollowMarkEffect extends MobEffect {
	public static final int COLOR = 0x9B4DFF;

	public HollowMarkEffect() {
		super(MobEffectCategory.HARMFUL, COLOR, new DustParticleOptions(COLOR, 0.8F));
	}
}
