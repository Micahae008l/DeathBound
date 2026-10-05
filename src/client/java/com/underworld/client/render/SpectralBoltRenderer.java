package com.underworld.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.underworld.Underworld;
import com.underworld.client.model.ModModelLayers;
import com.underworld.client.model.SpectralBoltModel;
import com.underworld.client.render.state.SpectralBoltRenderState;
import com.underworld.entity.hunter.SpectralBolt;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SpectralBoltRenderer extends EntityRenderer<SpectralBolt, SpectralBoltRenderState> {
	private static final Identifier TEXTURE = Underworld.id("textures/entity/spectral_bolt/spectral_bolt.png");
	private static final RenderType BASE = RenderTypes.entityCutout(TEXTURE);
	private static final RenderType GLOW = RenderTypes.eyes(Underworld.id("textures/entity/spectral_bolt/spectral_bolt_glow.png"));
	private static final int FULL_BRIGHT = 0xF000F0;
	private final SpectralBoltModel model;

	public SpectralBoltRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new SpectralBoltModel(context.bakeLayer(ModModelLayers.SPECTRAL_BOLT));
	}

	@Override
	protected int getBlockLightLevel(SpectralBolt entity, BlockPos blockPos) {
		return 15;
	}

	@Override
	public void submit(SpectralBoltRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0.0F, 0.2F, 0.0F);
		// vanilla projectile convention: yaw-90 is the heading, -pitch the elevation; the model's head points to -Z
		float a = (state.yRot - 90.0F) * Mth.DEG_TO_RAD;
		float b = -state.xRot * Mth.DEG_TO_RAD;
		Vector3f dir = new Vector3f(Mth.cos(b) * Mth.cos(a), Mth.sin(b), Mth.cos(b) * Mth.sin(a));
		poseStack.mulPose(new Matrix4f().rotation(new Quaternionf().rotationTo(0.0F, 0.0F, -1.0F, dir.x, dir.y, dir.z)));
		float s = 1.6F * (state.launched ? 1.0F : 1.0F + Mth.sin(state.ageInTicks * 0.6F) * 0.08F);
		poseStack.scale(s, s, s);
		// opaque base (the translucent pipeline dropped these) + additive glow on top
		collector.submitModel(this.model, state, poseStack, BASE, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
		int g = state.launched ? 255 : (int) (190 + Mth.sin(state.ageInTicks * 0.6F) * 60);
		collector.order(1).submitModel(this.model, state, poseStack, GLOW, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ARGB.color(255, g, g, g), null, state.outlineColor);
		poseStack.popPose();
		super.submit(state, poseStack, collector, camera);
	}

	@Override
	public SpectralBoltRenderState createRenderState() {
		return new SpectralBoltRenderState();
	}

	@Override
	public void extractRenderState(SpectralBolt entity, SpectralBoltRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.yRot = entity.getYRot(partialTicks);
		state.xRot = entity.getXRot(partialTicks);
		state.launched = entity.isLaunched();
	}
}
