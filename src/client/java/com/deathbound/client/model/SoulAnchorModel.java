package com.deathbound.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class SoulAnchorModel extends EntityModel<LivingEntityRenderState> {
   private final ModelPart cage;
   private final ModelPart chains;

   public SoulAnchorModel(ModelPart model) {
      super(model);
      this.cage = model.getChild("root").getChild("cage");
      this.chains = this.cage.getChild("chains");
   }

   public void setupAnim(LivingEntityRenderState state) {
      super.setupAnim(state);
      float t = state.ageInTicks;
      this.cage.yRot = t * 0.025F;
      this.cage.y = this.cage.y + Mth.sin(t * 0.08F) * 1.5F;
      this.cage.zRot = Mth.sin(t * 0.05F) * 0.05F;
      this.chains.xRot = Mth.sin(t * 0.07F) * 0.12F;
      this.chains.zRot = Mth.cos(t * 0.06F) * 0.1F;
   }
}
