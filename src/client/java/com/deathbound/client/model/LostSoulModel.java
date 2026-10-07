package com.deathbound.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

public class LostSoulModel extends EntityModel<LostSoulModel.State> {
   private final ModelPart body;
   private final ModelPart head;
   private final ModelPart rightArm;
   private final ModelPart leftArm;
   private final ModelPart tail;
   private final ModelPart tail2;
   private final ModelPart tail3;

   public LostSoulModel(ModelPart model) {
      super(model, RenderTypes::entityTranslucent);
      ModelPart root = model.getChild("root");
      this.body = root.getChild("body");
      this.head = this.body.getChild("head");
      this.rightArm = this.body.getChild("right_arm");
      this.leftArm = this.body.getChild("left_arm");
      this.tail = this.body.getChild("tail");
      this.tail2 = this.tail.getChild("tail2");
      this.tail3 = this.tail2.getChild("tail3");
   }

   public void setupAnim(LostSoulModel.State state) {
      super.setupAnim(state);
      float t = state.ageInTicks + state.seed * 13;
      this.head.xRot = this.head.xRot + ((state.waiting ? 0.55F : 0.3F) + Mth.sin(t * 0.04F) * 0.06F);
      this.head.zRot = this.head.zRot + Mth.sin(t * 0.03F) * 0.06F;
      this.body.xRot = this.body.xRot + (0.12F + Mth.sin(t * 0.05F) * 0.03F);
      this.rightArm.xRot = this.rightArm.xRot + (0.35F + Mth.sin(t * 0.06F) * 0.08F);
      this.leftArm.xRot = this.leftArm.xRot + (0.35F + Mth.sin(t * 0.06F + 1.3F) * 0.08F);
      this.rightArm.zRot = this.rightArm.zRot + (0.08F + Mth.sin(t * 0.05F) * 0.04F);
      this.leftArm.zRot = this.leftArm.zRot - (0.08F + Mth.sin(t * 0.05F + 0.7F) * 0.04F);
      this.tail.xRot = this.tail.xRot + (0.25F + Mth.sin(t * 0.09F) * 0.12F);
      this.tail2.xRot = this.tail2.xRot + (0.2F + Mth.sin(t * 0.09F - 0.8F) * 0.18F);
      this.tail3.xRot = this.tail3.xRot + (0.2F + Mth.sin(t * 0.09F - 1.6F) * 0.25F);
      this.tail.zRot = this.tail.zRot + Mth.sin(t * 0.07F) * 0.08F;
      this.tail2.zRot = this.tail2.zRot + Mth.sin(t * 0.07F - 0.9F) * 0.12F;
      this.tail3.zRot = this.tail3.zRot + Mth.sin(t * 0.07F - 1.8F) * 0.18F;
      if (state.waiting) {
         this.rightArm.xRot -= 0.6F;
         this.leftArm.xRot -= 0.6F;
      }
   }

   public static class State extends LivingEntityRenderState {
      public float alpha = 1.0F;
      public boolean waiting;
      public int seed;
      public int tint;
   }
}
