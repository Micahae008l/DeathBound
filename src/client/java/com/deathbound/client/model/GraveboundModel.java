package com.deathbound.client.model;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;

public class GraveboundModel extends EntityModel<GraveboundModel.State> {
   static final AnimationDefinition RISE = Anim.of(1.7F)
      .pos(
         "root",
         0.0F,
         0.0F,
         -30.0F,
         0.0F,
         0.35F,
         0.0F,
         -22.0F,
         0.0F,
         0.8F,
         0.0F,
         -12.0F,
         0.0F,
         1.2F,
         0.0F,
         -4.0F,
         0.0F,
         1.5F,
         0.0F,
         -1.0F,
         0.0F,
         1.7F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot("root", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, 0.0F, 0.0F, -8.0F, 1.0F, 0.0F, 0.0F, 6.0F, 1.3F, 0.0F, 0.0F, -3.0F, 1.7F, 0.0F, 0.0F, 0.0F)
      .rot("body", 0.0F, 50.0F, 0.0F, 0.0F, 0.8F, 35.0F, 0.0F, 5.0F, 1.3F, 15.0F, 0.0F, -5.0F, 1.7F, 0.0F, 0.0F, 0.0F)
      .rot("head", 0.0F, 45.0F, 0.0F, 0.0F, 0.9F, 30.0F, 20.0F, 0.0F, 1.3F, -20.0F, -10.0F, 0.0F, 1.7F, 0.0F, 0.0F, 0.0F)
      .rot("jaw", 0.0F, 0.0F, 0.0F, 0.0F, 1.15F, 0.0F, 0.0F, 0.0F, 1.3F, 38.0F, 0.0F, 0.0F, 1.55F, 0.0F, 0.0F, 0.0F)
      .rot(
         "right_arm",
         0.0F,
         -175.0F,
         0.0F,
         15.0F,
         0.4F,
         -160.0F,
         0.0F,
         25.0F,
         0.7F,
         -130.0F,
         0.0F,
         10.0F,
         1.0F,
         -150.0F,
         0.0F,
         30.0F,
         1.3F,
         -60.0F,
         0.0F,
         10.0F,
         1.7F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "left_arm",
         0.0F,
         -170.0F,
         0.0F,
         -10.0F,
         0.5F,
         -140.0F,
         0.0F,
         -30.0F,
         0.8F,
         -165.0F,
         0.0F,
         -15.0F,
         1.1F,
         -100.0F,
         0.0F,
         -25.0F,
         1.4F,
         -40.0F,
         0.0F,
         -10.0F,
         1.7F,
         0.0F,
         0.0F,
         0.0F
      )
      .build();
   static final AnimationDefinition ATTACK = Anim.of(0.65F)
      .rot("right_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.15F, -165.0F, 20.0F, 25.0F, 0.3F, -40.0F, -10.0F, -5.0F, 0.45F, -55.0F, 0.0F, 0.0F, 0.65F, 0.0F, 0.0F, 0.0F)
      .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.18F, -120.0F, -10.0F, -25.0F, 0.33F, -70.0F, 10.0F, 0.0F, 0.65F, 0.0F, 0.0F, 0.0F)
      .rot("body", 0.0F, 0.0F, 0.0F, 0.0F, 0.15F, -12.0F, 15.0F, 0.0F, 0.32F, 18.0F, -18.0F, 0.0F, 0.65F, 0.0F, 0.0F, 0.0F)
      .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.15F, -15.0F, 0.0F, 0.0F, 0.32F, 10.0F, 0.0F, 0.0F, 0.65F, 0.0F, 0.0F, 0.0F)
      .rot("jaw", 0.0F, 0.0F, 0.0F, 0.0F, 0.1F, 40.0F, 0.0F, 0.0F, 0.35F, 10.0F, 0.0F, 0.0F, 0.5F, 0.0F, 0.0F, 0.0F)
      .rot("weapon", 0.0F, 0.0F, 0.0F, 0.0F, 0.15F, -40.0F, 0.0F, 0.0F, 0.3F, 20.0F, 0.0F, 0.0F, 0.65F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition COLLAPSE = Anim.of(1.3F)
      .pos("root", 0.0F, 0.0F, 0.0F, 0.0F, 0.35F, 0.0F, -5.0F, 0.0F, 0.7F, 0.0F, -9.0F, 0.0F, 1.0F, 0.0F, -14.0F, -2.0F, 1.3F, 0.0F, -16.0F, -3.0F)
      .rot("right_leg", 0.0F, 0.0F, 0.0F, 0.0F, 0.35F, -70.0F, 0.0F, 8.0F, 0.7F, -90.0F, 0.0F, 10.0F, 1.3F, -90.0F, 0.0F, 15.0F)
      .rot("left_leg", 0.0F, 0.0F, 0.0F, 0.0F, 0.35F, -60.0F, 0.0F, -8.0F, 0.7F, -88.0F, 0.0F, -10.0F, 1.3F, -90.0F, 0.0F, -14.0F)
      .rot("body", 0.0F, 0.0F, 0.0F, 0.0F, 0.35F, 20.0F, 0.0F, 4.0F, 0.7F, 40.0F, 0.0F, -6.0F, 1.0F, 75.0F, 0.0F, -10.0F, 1.3F, 85.0F, 0.0F, -12.0F)
      .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, 30.0F, 10.0F, 0.0F, 1.0F, 40.0F, 25.0F, 10.0F, 1.3F, 45.0F, 30.0F, 15.0F)
      .rot("jaw", 0.0F, 0.0F, 0.0F, 0.0F, 0.4F, 35.0F, 0.0F, 0.0F, 1.3F, 30.0F, 0.0F, 0.0F)
      .rot("right_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, -30.0F, 0.0F, 20.0F, 1.3F, -80.0F, 0.0F, 40.0F)
      .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, -20.0F, 0.0F, -25.0F, 1.3F, -85.0F, 0.0F, -35.0F)
      .build();
   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart head;
   private final ModelPart jaw;
   private final ModelPart rightArm;
   private final ModelPart leftArm;
   private final ModelPart weapon;
   private final ModelPart ragFront;
   private final ModelPart ragBack;
   private final ModelPart rightLeg;
   private final ModelPart leftLeg;
   private final KeyframeAnimation rise;
   private final KeyframeAnimation attack;
   private final KeyframeAnimation collapse;

   public GraveboundModel(ModelPart model) {
      super(model);
      this.root = model.getChild("root");
      this.body = this.root.getChild("body");
      this.head = this.body.getChild("head");
      this.jaw = this.head.getChild("jaw");
      this.rightArm = this.body.getChild("right_arm");
      this.leftArm = this.body.getChild("left_arm");
      this.weapon = this.rightArm.getChild("weapon");
      this.ragFront = this.body.getChild("rag_front");
      this.ragBack = this.body.getChild("rag_back");
      this.rightLeg = this.root.getChild("right_leg");
      this.leftLeg = this.root.getChild("left_leg");
      this.rise = RISE.bake(model);
      this.attack = ATTACK.bake(model);
      this.collapse = COLLAPSE.bake(model);
   }

   public void setupAnim(GraveboundModel.State state) {
      super.setupAnim(state);
      float age = state.ageInTicks;
      float speed = Math.min(state.walkAnimationSpeed, 1.0F);
      float walk = state.walkAnimationPos * 0.6662F;
      this.head.yRot = this.head.yRot + state.yRot * 0.017453292F;
      this.head.xRot = this.head.xRot + state.xRot * 0.017453292F;
      this.rightLeg.xRot = this.rightLeg.xRot + Mth.cos(walk) * 1.1F * speed;
      this.leftLeg.xRot = this.leftLeg.xRot + Mth.cos(walk + 3.1415927F) * 1.1F * speed;
      this.rightArm.xRot = this.rightArm.xRot + (Mth.cos(walk + 3.1415927F) * 0.55F * speed - 0.3F * speed);
      this.leftArm.xRot = this.leftArm.xRot + (Mth.cos(walk) * 0.55F * speed - 0.3F * speed);
      this.body.zRot = this.body.zRot + Mth.sin(walk) * 0.08F * speed;
      this.body.yRot = this.body.yRot + Mth.sin(walk) * 0.12F * speed;
      this.root.y = this.root.y - Math.abs(Mth.cos(walk)) * 0.8F * speed;
      this.body.xRot = this.body.xRot + Mth.sin(age * 0.07F) * 0.025F;
      this.head.zRot = this.head.zRot + Mth.sin(age * 0.045F) * 0.07F;
      this.rightArm.zRot = this.rightArm.zRot + (Mth.sin(age * 0.09F) * 0.05F + 0.05F);
      this.leftArm.zRot = this.leftArm.zRot - (Mth.sin(age * 0.09F) * 0.05F + 0.05F);
      this.jaw.xRot = this.jaw.xRot + (0.04F + Math.max(0.0F, Mth.sin(age * 0.33F)) * 0.07F);
      this.ragFront.xRot = this.ragFront.xRot + (-0.65F * speed + Mth.sin(age * 0.15F) * 0.06F);
      this.ragBack.xRot = this.ragBack.xRot + (0.55F * speed + Mth.sin(age * 0.15F + 1.0F) * 0.06F);
      this.weapon.visible = state.bare;
      this.rise.apply(state.rise, age);
      this.attack.apply(state.attack, age);
      if (state.collapse > 0.0F) {
         this.collapse.apply((long)(state.collapse * 50.0F), 1.0F);
      }
   }

   public static class State extends LivingEntityRenderState {
      public final AnimationState rise = new AnimationState();
      public final AnimationState attack = new AnimationState();
      public boolean bare;
      public int variant;
      public float collapse;
   }
}
