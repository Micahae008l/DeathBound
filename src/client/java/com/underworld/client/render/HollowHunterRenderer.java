package com.underworld.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.underworld.Underworld;
import com.underworld.client.model.HollowHunterModel;
import com.underworld.client.model.ModModelLayers;
import com.underworld.client.render.state.HollowHunterRenderState;
import com.underworld.entity.hunter.HollowHunter;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class HollowHunterRenderer extends MobRenderer<HollowHunter, HollowHunterRenderState, HollowHunterModel> {
	private static final Identifier TEXTURE = Underworld.id("textures/entity/hollow_hunter/hollow_hunter.png");
	private static final RenderType[] GLOW = {
		RenderTypes.eyes(Underworld.id("textures/entity/hollow_hunter/hollow_hunter_glow_1.png")),
		RenderTypes.eyes(Underworld.id("textures/entity/hollow_hunter/hollow_hunter_glow_2.png")),
		RenderTypes.eyes(Underworld.id("textures/entity/hollow_hunter/hollow_hunter_glow_3.png")),
	};

	public HollowHunterRenderer(EntityRendererProvider.Context context) {
		super(context, new HollowHunterModel(context.bakeLayer(ModModelLayers.HOLLOW_HUNTER)), 0.8F);
		this.addLayer(new GlowLayer(this));
	}

	@Override
	public HollowHunterRenderState createRenderState() {
		return new HollowHunterRenderState();
	}

	@Override
	public void extractRenderState(HollowHunter entity, HollowHunterRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.phase = entity.getPhase();
		state.anim = entity.getAnim();
		state.animTime = entity.getAnimTime(partialTicks);
		state.eyeLit = entity.isEyeLit();
		state.hidden = entity.isHidden();
		state.hasRedOverlay = entity.hurtTime > 0 && entity.deathTime == 0;
	}

	@Override
	public void submit(HollowHunterRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!state.hidden) {
			super.submit(state, poseStack, collector, camera);
		}
	}

	@Override
	public Identifier getTextureLocation(HollowHunterRenderState state) {
		return TEXTURE;
	}

	@Override
	protected float getFlipDegrees() {
		return 0.0F; // he crumbles instead of tipping over
	}

	@Override
	protected float getShadowRadius(HollowHunterRenderState state) {
		return state.hidden ? 0.0F : super.getShadowRadius(state);
	}

	/** The eye, spectral arrows and runes. Brighter textures per phase; flickers out on death; dim while vulnerable. */
	private static class GlowLayer extends RenderLayer<HollowHunterRenderState, HollowHunterModel> {
		GlowLayer(RenderLayerParent<HollowHunterRenderState, HollowHunterModel> parent) {
			super(parent);
		}

		@Override
		public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, HollowHunterRenderState state, float yRot, float xRot) {
			if (!state.eyeLit || state.hidden) {
				return;
			}
			float bright = 1.0F;
			if (state.deathTime > 0) {
				if (state.deathTime < 34 && ((int) (state.deathTime * 1.3F)) % 4 == 1) {
					return; // flicker
				}
				bright = 1.0F - Mth.clamp((state.deathTime - 18) / 30.0F, 0, 1);
			} else if (state.anim == HollowHunter.ANIM_VULNERABLE) {
				bright = 0.4F + Mth.sin(state.ageInTicks * 0.3F) * 0.1F;
			} else if (state.anim == HollowHunter.ANIM_LAST_HUNT) {
				bright = 0.85F + Mth.sin(state.ageInTicks * 0.8F) * 0.15F;
			}
			if (bright <= 0.02F) {
				return;
			}
			int c = (int) (Mth.clamp(bright, 0, 1) * 255);
			RenderType type = GLOW[Mth.clamp(state.phase, 1, 3) - 1];
			collector.order(1).submitModel(this.getParentModel(), state, poseStack, type, light, OverlayTexture.NO_OVERLAY, ARGB.color(255, c, c, c), null, state.outlineColor);
		}
	}
}
