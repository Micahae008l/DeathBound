package com.underworld.client;

import com.underworld.client.model.ModModelLayers;
import com.underworld.client.model.geom.CollectorGeometry;
import com.underworld.client.model.geom.HollowHunterGeometry;
import com.underworld.client.model.geom.SpectralBoltGeometry;
import com.underworld.client.render.CollectorRenderer;
import com.underworld.client.render.HollowArrowRenderer;
import com.underworld.client.render.HollowHunterRenderer;
import com.underworld.client.render.SpectralBoltRenderer;
import com.underworld.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

public class UnderworldClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(ModModelLayers.HOLLOW_HUNTER, HollowHunterGeometry::create);
		ModelLayerRegistry.registerModelLayer(ModModelLayers.COLLECTOR, CollectorGeometry::create);
		ModelLayerRegistry.registerModelLayer(ModModelLayers.SPECTRAL_BOLT, SpectralBoltGeometry::create);

		EntityRendererRegistry.register(ModEntities.HOLLOW_HUNTER, HollowHunterRenderer::new);
		EntityRendererRegistry.register(ModEntities.COLLECTOR, CollectorRenderer::new);
		EntityRendererRegistry.register(ModEntities.SPECTRAL_BOLT, SpectralBoltRenderer::new);
		EntityRendererRegistry.register(ModEntities.HOLLOW_ARROW, HollowArrowRenderer::new);
	}
}
