package com.deathbound.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class WispModel extends EntityModel<LivingEntityRenderState> {
   private final ModelPart lantern;
   private final ModelPart flame;

   public WispModel(ModelPart model) {
      super(model);
      this.lantern = model.getChild("root").getChild("lantern");
      this.flame = this.lantern.getChild("flame");
   }

   public void setupAnim(LivingEntityRenderState s) {
      super.setupAnim(s);
      float t = s.ageInTicks;
      this.lantern.zRot = Mth.sin(t * 0.09F) * 0.12F;
      this.lantern.xRot = Mth.sin(t * 0.07F + 1.0F) * 0.1F;
      this.flame.yRot = t * 0.08F;
      this.flame.y = this.flame.y + Mth.sin(t * 0.3F) * 0.3F;
   }
}
