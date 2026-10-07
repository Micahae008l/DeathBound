package com.deathbound.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class KidModel extends EntityModel<KidModel.State> {
   private final ModelPart body;
   private final ModelPart head;
   private final ModelPart cap;
   private final ModelPart bow;
   private final ModelPart scarf;
   private final ModelPart rightArm;
   private final ModelPart leftArm;
   private final ModelPart rightLeg;
   private final ModelPart leftLeg;

   public KidModel(ModelPart model) {
      super(model);
      ModelPart root = model.getChild("root");
      this.body = root.getChild("body");
      this.head = this.body.getChild("head");
      this.cap = this.head.getChild("cap");
      this.bow = this.head.getChild("bow");
      this.scarf = this.body.getChild("scarf_tail");
      this.rightArm = this.body.getChild("right_arm");
      this.leftArm = this.body.getChild("left_arm");
      this.rightLeg = root.getChild("right_leg");
      this.leftLeg = root.getChild("left_leg");
   }

   public void setupAnim(KidModel.State s) {
      super.setupAnim(s);
      this.cap.visible = s.variant == 1;
      this.bow.visible = s.variant == 2;
      float w = s.walkAnimationPos * 0.9F;
      float sp = Math.min(1.0F, s.walkAnimationSpeed * 1.4F);
      this.rightLeg.xRot = Mth.cos(w) * 1.1F * sp;
      this.leftLeg.xRot = Mth.cos(w + 3.1415927F) * 1.1F * sp;
      this.rightArm.xRot = Mth.cos(w + 3.1415927F) * 1.2F * sp - 0.1F;
      this.leftArm.xRot = Mth.cos(w) * 1.2F * sp - 0.1F;
      this.rightArm.zRot = 0.15F + sp * 0.3F;
      this.leftArm.zRot = -0.15F - sp * 0.3F;
      this.body.y = this.body.y - Math.abs(Mth.sin(w)) * 1.2F * sp;
      this.body.xRot = 0.15F * sp;
      this.head.yRot = s.yRot * 0.017453292F;
      this.head.xRot = s.xRot * 0.017453292F - 0.1F * sp + Mth.sin(s.ageInTicks * 0.1F) * 0.03F;
      this.head.zRot = Mth.sin(w * 0.5F) * 0.12F * sp;
      this.scarf.xRot = 0.3F + sp * 0.9F + Mth.sin(s.ageInTicks * 0.3F) * 0.15F * sp;
   }

   public static class State extends LivingEntityRenderState {
      public int variant;
   }
}
