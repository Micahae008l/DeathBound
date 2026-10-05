package com.underworld.client.render;

import com.underworld.Underworld;
import com.underworld.client.render.state.HollowArrowRenderState;
import com.underworld.entity.hunter.HollowArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class HollowArrowRenderer extends ArrowRenderer<HollowArrow, HollowArrowRenderState> {
	private static final Identifier HOLLOW = Underworld.id("textures/entity/projectiles/hollow_arrow.png");
	private static final Identifier SHOT = Underworld.id("textures/entity/projectiles/hollow_shot.png");

	public HollowArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected Identifier getTextureLocation(HollowArrowRenderState state) {
		return state.plainShot ? SHOT : HOLLOW;
	}

	@Override
	protected int getBlockLightLevel(HollowArrow entity, BlockPos blockPos) {
		return entity.getMode().glows() ? 15 : super.getBlockLightLevel(entity, blockPos);
	}

	@Override
	public HollowArrowRenderState createRenderState() {
		return new HollowArrowRenderState();
	}

	@Override
	public void extractRenderState(HollowArrow entity, HollowArrowRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.plainShot = entity.getMode() == HollowArrow.Mode.PLAYER_SHOT;
	}
}
