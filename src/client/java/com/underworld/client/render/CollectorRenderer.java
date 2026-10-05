package com.underworld.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.underworld.Underworld;
import com.underworld.client.model.CollectorModel;
import com.underworld.client.model.ModModelLayers;
import com.underworld.client.render.state.CollectorRenderState;
import com.underworld.entity.collector.Collector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public class CollectorRenderer extends MobRenderer<Collector, CollectorRenderState, CollectorModel> {
	private static final Identifier TEXTURE = Underworld.id("textures/entity/collector/collector.png");
	private static final RenderType GLOW = RenderTypes.eyes(Underworld.id("textures/entity/collector/collector_glow.png"));

	public CollectorRenderer(EntityRendererProvider.Context context) {
		super(context, new CollectorModel(context.bakeLayer(ModModelLayers.COLLECTOR)), 0.45F);
		this.addLayer(new ItemInHandLayer<>(this));
		this.addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return GLOW;
			}
		});
	}

	@Override
	public CollectorRenderState createRenderState() {
		return new CollectorRenderState();
	}

	@Override
	public void extractRenderState(Collector entity, CollectorRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, this.itemModelResolver, partialTicks);
		state.inspecting = entity.isInspecting();
		state.vanished = entity.isVanished();
	}

	@Override
	public void submit(CollectorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!state.vanished) {
			super.submit(state, poseStack, collector, camera);
		}
	}

	@Override
	public Identifier getTextureLocation(CollectorRenderState state) {
		return TEXTURE;
	}
}
