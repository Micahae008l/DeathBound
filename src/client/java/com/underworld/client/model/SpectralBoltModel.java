package com.underworld.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class SpectralBoltModel extends EntityModel<EntityRenderState> {
	public SpectralBoltModel(ModelPart root) {
		super(root, RenderTypes::entityCutout);
	}
}
