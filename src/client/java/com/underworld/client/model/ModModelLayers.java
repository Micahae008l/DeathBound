package com.underworld.client.model;

import com.underworld.Underworld;
import net.minecraft.client.model.geom.ModelLayerLocation;

public final class ModModelLayers {
	public static final ModelLayerLocation HOLLOW_HUNTER = new ModelLayerLocation(Underworld.id("hollow_hunter"), "main");
	public static final ModelLayerLocation COLLECTOR = new ModelLayerLocation(Underworld.id("collector"), "main");
	public static final ModelLayerLocation SPECTRAL_BOLT = new ModelLayerLocation(Underworld.id("spectral_bolt"), "main");

	private ModModelLayers() {
	}
}
