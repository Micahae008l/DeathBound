package com.deathbound.client.model;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;

public class HunterModel extends EntityModel<HunterModel.State> {
   static final AnimationDefinition AIM = Anim.of(1.6F)
      .rot(
         "right_arm",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.25F,
         -165.0F,
         0.0F,
         15.0F,
         0.38F,
         -170.0F,
         0.0F,
         12.0F,
         0.45F,
         -176.0F,
         0.0F,
         10.0F,
         0.62F,
         -80.0F,
         -20.0F,
         0.0F,
         0.75F,
         -95.0F,
         -35.0F,
         0.0F,
         1.08F,
         -95.0F,
         -36.0F,
         0.0F,
         1.13F,
         -85.0F,
         12.0F,
         0.0F,
         1.3F,
         -85.0F,
         10.0F,
         0.0F,
         1.6F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "right_forearm",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.25F,
         -110.0F,
         0.0F,
         0.0F,
         0.38F,
         -116.0F,
         0.0F,
         0.0F,
         0.45F,
         -90.0F,
         0.0F,
         0.0F,
         0.62F,
         0.0F,
         0.0F,
         -40.0F,
         0.75F,
         0.0F,
         0.0F,
         -110.0F,
         1.08F,
         0.0F,
         0.0F,
         -112.0F,
         1.13F,
         0.0F,
         0.0F,
         -30.0F,
         1.6F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.4F, -25.0F, 0.0F, 0.0F, 0.6F, -90.0F, 15.0F, 0.0F, 1.3F, -90.0F, 15.0F, 0.0F, 1.6F, 0.0F, 0.0F, 0.0F)
      .rot("bow", 0.0F, 0.0F, 0.0F, 0.0F, 0.4F, 30.0F, 0.0F, 0.0F, 0.6F, 90.0F, 0.0F, 0.0F, 1.3F, 90.0F, 0.0F, 0.0F, 1.6F, 0.0F, 0.0F, 0.0F)
      .rot(
         "head",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.3F,
         10.0F,
         28.0F,
         0.0F,
         0.45F,
         10.0F,
         24.0F,
         0.0F,
         0.6F,
         6.0F,
         8.0F,
         0.0F,
         1.1F,
         8.0F,
         10.0F,
         0.0F,
         1.6F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "chest",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.3F,
         -6.0F,
         12.0F,
         0.0F,
         0.6F,
         0.0F,
         8.0F,
         0.0F,
         1.1F,
         0.0F,
         12.0F,
         0.0F,
         1.13F,
         -4.0F,
         8.0F,
         0.0F,
         1.6F,
         0.0F,
         0.0F,
         0.0F
      )
      .build();
   static final AnimationDefinition RIVEN = Anim.of(2.8F)
      .rot(
         "right_arm",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.3F,
         -165.0F,
         0.0F,
         15.0F,
         0.45F,
         -176.0F,
         0.0F,
         10.0F,
         0.7F,
         -80.0F,
         -20.0F,
         0.0F,
         0.95F,
         -95.0F,
         -35.0F,
         0.0F,
         1.9F,
         -98.0F,
         -42.0F,
         0.0F,
         2.0F,
         -70.0F,
         30.0F,
         0.0F,
         2.3F,
         -75.0F,
         25.0F,
         0.0F,
         2.8F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "right_forearm",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.3F,
         -110.0F,
         0.0F,
         0.0F,
         0.45F,
         -90.0F,
         0.0F,
         0.0F,
         0.7F,
         0.0F,
         0.0F,
         -40.0F,
         0.95F,
         0.0F,
         0.0F,
         -115.0F,
         1.9F,
         0.0F,
         0.0F,
         -122.0F,
         2.0F,
         0.0F,
         0.0F,
         -20.0F,
         2.8F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "left_arm",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.5F,
         -30.0F,
         0.0F,
         0.0F,
         0.8F,
         -95.0F,
         15.0F,
         0.0F,
         1.9F,
         -95.0F,
         15.0F,
         0.0F,
         2.0F,
         -108.0F,
         15.0F,
         0.0F,
         2.3F,
         -95.0F,
         15.0F,
         0.0F,
         2.8F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot("bow", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, 30.0F, 0.0F, 0.0F, 0.8F, 90.0F, 0.0F, 0.0F, 2.3F, 90.0F, 0.0F, 0.0F, 2.8F, 0.0F, 0.0F, 0.0F)
      .rot(
         "head",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.35F,
         10.0F,
         28.0F,
         0.0F,
         0.8F,
         6.0F,
         8.0F,
         0.0F,
         1.9F,
         4.0F,
         6.0F,
         0.0F,
         2.0F,
         -10.0F,
         4.0F,
         0.0F,
         2.8F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "chest",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.35F,
         -6.0F,
         12.0F,
         0.0F,
         0.8F,
         -2.0F,
         10.0F,
         0.0F,
         1.9F,
         -4.0F,
         16.0F,
         0.0F,
         2.0F,
         -16.0F,
         4.0F,
         0.0F,
         2.3F,
         -10.0F,
         4.0F,
         0.0F,
         2.8F,
         0.0F,
         0.0F,
         0.0F
      )
      .build();
   static final AnimationDefinition SNARE = Anim.of(1.1F)
      .rot(
         "right_arm",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.15F,
         -165.0F,
         0.0F,
         15.0F,
         0.24F,
         -174.0F,
         0.0F,
         10.0F,
         0.36F,
         -80.0F,
         -20.0F,
         0.0F,
         0.5F,
         -95.0F,
         -35.0F,
         0.0F,
         0.68F,
         -95.0F,
         -36.0F,
         0.0F,
         0.73F,
         -85.0F,
         12.0F,
         0.0F,
         1.1F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "right_forearm",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.15F,
         -110.0F,
         0.0F,
         0.0F,
         0.24F,
         -95.0F,
         0.0F,
         0.0F,
         0.36F,
         0.0F,
         0.0F,
         -40.0F,
         0.5F,
         0.0F,
         0.0F,
         -110.0F,
         0.68F,
         0.0F,
         0.0F,
         -112.0F,
         0.73F,
         0.0F,
         0.0F,
         -30.0F,
         1.1F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, -90.0F, 15.0F, 0.0F, 0.85F, -90.0F, 15.0F, 0.0F, 1.1F, 0.0F, 0.0F, 0.0F)
      .rot("bow", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, 90.0F, 0.0F, 0.0F, 0.85F, 90.0F, 0.0F, 0.0F, 1.1F, 0.0F, 0.0F, 0.0F)
      .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.15F, 8.0F, 22.0F, 0.0F, 0.36F, 4.0F, 8.0F, 0.0F, 1.1F, 0.0F, 0.0F, 0.0F)
      .rot("chest", 0.0F, 0.0F, 0.0F, 0.0F, 0.15F, -4.0F, 10.0F, 0.0F, 0.3F, 6.0F, 8.0F, 0.0F, 0.73F, 2.0F, 8.0F, 0.0F, 1.1F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition VOLLEY = Anim.of(2.5F)
      .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.35F, -150.0F, 10.0F, 0.0F, 1.4F, -150.0F, 10.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F)
      .rot("bow", 0.0F, 0.0F, 0.0F, 0.0F, 0.35F, 90.0F, 0.0F, 0.0F, 1.4F, 90.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F)
      .rot(
         "right_arm",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.15F,
         -168.0F,
         0.0F,
         14.0F,
         0.25F,
         -174.0F,
         0.0F,
         10.0F,
         0.38F,
         -140.0F,
         0.0F,
         0.0F,
         0.45F,
         -150.0F,
         -25.0F,
         0.0F,
         0.52F,
         -140.0F,
         5.0F,
         0.0F,
         0.75F,
         -150.0F,
         -25.0F,
         0.0F,
         0.82F,
         -140.0F,
         5.0F,
         0.0F,
         1.05F,
         -150.0F,
         -25.0F,
         0.0F,
         1.12F,
         -140.0F,
         8.0F,
         0.0F,
         1.4F,
         -140.0F,
         5.0F,
         0.0F,
         2.0F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "right_forearm",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.15F,
         -110.0F,
         0.0F,
         0.0F,
         0.25F,
         -95.0F,
         0.0F,
         0.0F,
         0.38F,
         0.0F,
         0.0F,
         -30.0F,
         0.45F,
         0.0F,
         0.0F,
         -100.0F,
         0.52F,
         0.0F,
         0.0F,
         -30.0F,
         0.75F,
         0.0F,
         0.0F,
         -100.0F,
         0.82F,
         0.0F,
         0.0F,
         -30.0F,
         1.05F,
         0.0F,
         0.0F,
         -100.0F,
         1.12F,
         0.0F,
         0.0F,
         -30.0F,
         2.0F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "head",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.15F,
         6.0F,
         22.0F,
         0.0F,
         0.35F,
         -30.0F,
         0.0F,
         0.0F,
         1.4F,
         -30.0F,
         0.0F,
         0.0F,
         1.7F,
         10.0F,
         0.0F,
         0.0F,
         2.0F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot("chest", 0.0F, 0.0F, 0.0F, 0.0F, 0.15F, -4.0F, 10.0F, 0.0F, 0.35F, -15.0F, 0.0F, 0.0F, 1.4F, -15.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition LEAP = Anim.of(1.3F)
      .pos(
         "hips",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.2F,
         0.0F,
         3.0F,
         0.0F,
         0.35F,
         0.0F,
         1.0F,
         0.0F,
         0.6F,
         0.0F,
         0.0F,
         0.0F,
         1.0F,
         0.0F,
         3.0F,
         0.0F,
         1.3F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "right_thigh",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.2F,
         -40.0F,
         0.0F,
         0.0F,
         0.35F,
         -95.0F,
         0.0F,
         0.0F,
         0.6F,
         -60.0F,
         0.0F,
         0.0F,
         1.0F,
         -45.0F,
         0.0F,
         0.0F,
         1.3F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "left_thigh",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.2F,
         -40.0F,
         0.0F,
         0.0F,
         0.35F,
         -20.0F,
         0.0F,
         0.0F,
         0.6F,
         -60.0F,
         0.0F,
         0.0F,
         1.0F,
         -45.0F,
         0.0F,
         0.0F,
         1.3F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "right_shin",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.2F,
         60.0F,
         0.0F,
         0.0F,
         0.35F,
         0.0F,
         0.0F,
         0.0F,
         0.6F,
         80.0F,
         0.0F,
         0.0F,
         1.0F,
         70.0F,
         0.0F,
         0.0F,
         1.3F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "left_shin",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.2F,
         60.0F,
         0.0F,
         0.0F,
         0.35F,
         40.0F,
         0.0F,
         0.0F,
         0.6F,
         80.0F,
         0.0F,
         0.0F,
         1.0F,
         70.0F,
         0.0F,
         0.0F,
         1.3F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "chest",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.2F,
         20.0F,
         0.0F,
         0.0F,
         0.35F,
         -10.0F,
         0.0F,
         0.0F,
         0.6F,
         10.0F,
         0.0F,
         0.0F,
         1.0F,
         15.0F,
         0.0F,
         0.0F,
         1.3F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot("right_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.35F, -20.0F, 0.0F, 20.0F, 0.6F, -30.0F, 0.0F, 40.0F, 1.3F, 0.0F, 0.0F, 0.0F)
      .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.35F, -20.0F, 0.0F, -20.0F, 0.6F, -30.0F, 0.0F, -40.0F, 1.3F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition VANISH = Anim.of(1.3F)
      .pos("hips", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, 0.0F, 4.0F, 0.0F, 1.0F, 0.0F, 4.0F, 0.0F, 1.3F, 0.0F, 0.0F, 0.0F)
      .rot("right_thigh", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, -60.0F, 0.0F, 0.0F, 1.0F, -60.0F, 0.0F, 0.0F, 1.3F, 0.0F, 0.0F, 0.0F)
      .rot("left_thigh", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, -60.0F, 0.0F, 0.0F, 1.0F, -60.0F, 0.0F, 0.0F, 1.3F, 0.0F, 0.0F, 0.0F)
      .rot("right_shin", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, 90.0F, 0.0F, 0.0F, 1.0F, 90.0F, 0.0F, 0.0F, 1.3F, 0.0F, 0.0F, 0.0F)
      .rot("left_shin", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, 90.0F, 0.0F, 0.0F, 1.0F, 90.0F, 0.0F, 0.0F, 1.3F, 0.0F, 0.0F, 0.0F)
      .rot("chest", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, 45.0F, 0.0F, 0.0F, 1.0F, 45.0F, 0.0F, 0.0F, 1.3F, 0.0F, 0.0F, 0.0F)
      .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, 20.0F, 0.0F, 0.0F, 1.0F, 20.0F, 0.0F, 0.0F, 1.3F, 0.0F, 0.0F, 0.0F)
      .rot("cape", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, -30.0F, 0.0F, 0.0F, 1.0F, -30.0F, 0.0F, 0.0F, 1.3F, 0.0F, 0.0F, 0.0F)
      .rot("right_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, -30.0F, 0.0F, 25.0F, 1.0F, -30.0F, 0.0F, 25.0F, 1.3F, 0.0F, 0.0F, 0.0F)
      .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, -30.0F, 0.0F, -25.0F, 1.0F, -30.0F, 0.0F, -25.0F, 1.3F, 0.0F, 0.0F, 0.0F)
      .build();
   private final ModelPart root;
   private final ModelPart hips;
   private final ModelPart chest;
   private final ModelPart head;
   private final ModelPart jaw;
   private final ModelPart cape;
   private final ModelPart rightThigh;
   private final ModelPart leftThigh;
   private final ModelPart rightShin;
   private final ModelPart leftShin;
   private final ModelPart rightArm;
   private final ModelPart leftArm;
   private final ModelPart bow;
   private final ModelPart pullArrow;
   private final ModelPart handArrow;
   private final ModelPart nockedArrow;
   private final KeyframeAnimation aim;
   private final KeyframeAnimation snare;
   private final KeyframeAnimation volley;
   private final KeyframeAnimation leap;
   private final KeyframeAnimation vanish;
   private final KeyframeAnimation riven;

   public HunterModel(ModelPart model) {
      super(model);
      this.root = model.getChild("root");
      this.hips = this.root.getChild("hips");
      ModelPart spine = this.hips.getChild("spine");
      this.chest = spine.getChild("chest");
      this.head = this.chest.getChild("head");
      this.jaw = this.head.getChild("jaw");
      this.cape = this.chest.getChild("cape");
      this.rightThigh = this.hips.getChild("right_thigh");
      this.leftThigh = this.hips.getChild("left_thigh");
      this.rightShin = this.rightThigh.getChild("right_shin");
      this.leftShin = this.leftThigh.getChild("left_shin");
      this.rightArm = this.chest.getChild("right_arm");
      this.leftArm = this.chest.getChild("left_arm");
      this.bow = this.leftArm.getChild("left_forearm").getChild("bow");
      this.pullArrow = this.chest.getChild("pull_arrow");
      this.handArrow = this.rightArm.getChild("right_forearm").getChild("hand_arrow");
      this.nockedArrow = this.bow.getChild("nocked_arrow");
      this.aim = AIM.bake(model);
      this.snare = SNARE.bake(model);
      this.volley = VOLLEY.bake(model);
      this.leap = LEAP.bake(model);
      this.vanish = VANISH.bake(model);
      this.riven = RIVEN.bake(model);
   }

   public void setupAnim(HunterModel.State s) {
      super.setupAnim(s);
      float t = s.ageInTicks;
      this.arrows(s, t);
      if (s.collapse > 0.0F) {
         float k = Mth.clamp(s.collapse / 30.0F, 0.0F, 1.0F);
         this.hips.y += 7.0F * k;
         this.rightThigh.xRot -= 1.4F * k;
         this.rightShin.xRot += 1.6F * k;
         this.leftThigh.xRot -= 0.3F * k;
         this.leftShin.xRot += 1.4F * k;
         this.chest.xRot += 0.6F * k;
         this.head.xRot += 0.7F * k;
         this.rightArm.xRot -= 0.3F * k;
         this.leftArm.zRot -= 0.4F * k;
      } else {
         float walk = s.walkAnimationPos;
         float speed = Math.min(1.0F, s.walkAnimationSpeed * 1.4F);
         this.chest.xRot = this.chest.xRot + (s.hunting ? 0.22F : 0.08F);
         this.head.xRot = this.head.xRot + ((s.hunting ? -0.18F : -0.05F) + s.xRot * 0.017453292F * 0.6F);
         this.head.yRot = this.head.yRot + s.yRot * 0.017453292F * 0.8F;
         this.rightThigh.xRot = this.rightThigh.xRot + Mth.cos(walk * 0.55F) * 0.9F * speed;
         this.leftThigh.xRot = this.leftThigh.xRot + Mth.cos(walk * 0.55F + 3.1415927F) * 0.9F * speed;
         this.rightShin.xRot = this.rightShin.xRot + Math.max(0.0F, Mth.sin(walk * 0.55F)) * 1.1F * speed;
         this.leftShin.xRot = this.leftShin.xRot + Math.max(0.0F, Mth.sin(walk * 0.55F + 3.1415927F)) * 1.1F * speed;
         this.rightArm.xRot = this.rightArm.xRot + Mth.cos(walk * 0.55F + 3.1415927F) * 0.35F * speed;
         this.leftArm.xRot = this.leftArm.xRot + Mth.cos(walk * 0.55F) * 0.25F * speed;
         this.hips.y = this.hips.y + Math.abs(Mth.sin(walk * 0.55F)) * -0.8F * speed;
         int beat = (int)(t / 17.0F);
         float jerk = (beat * 7919 % 13 - 6) / 6.0F;
         this.head.yRot += jerk * 0.35F;
         this.head.zRot += (beat * 104729 % 7 - 3) / 3.0F * 0.12F;
         this.jaw.xRot = this.jaw.xRot + (0.08F + Mth.sin(t * 0.07F) * 0.06F);
         this.cape.xRot = this.cape.xRot + (0.06F + Mth.sin(t * 0.05F) * 0.05F + speed * 0.5F);
         this.cape.zRot = this.cape.zRot + Mth.sin(t * 0.037F) * 0.03F;
         this.bow.zRot = this.bow.zRot + Mth.sin(t * 0.04F) * 0.03F;
         this.aim.apply(s.aim, t);
         this.snare.apply(s.snare, t);
         this.volley.apply(s.volley, t);
         this.leap.apply(s.leap, t);
         this.vanish.apply(s.vanish, t);
         this.riven.apply(s.riven, t);
         if (seconds(s.riven, t) >= 0.95F && seconds(s.riven, t) < 2.0F) {
            this.chest.zRot = this.chest.zRot + Mth.sin(t * 2.1F) * 0.025F;
            this.rightArm.zRot = this.rightArm.zRot + Mth.sin(t * 2.7F) * 0.03F;
         }
      }
   }

   private void arrows(HunterModel.State s, float t) {
      float aim = seconds(s.aim, t);
      float snare = seconds(s.snare, t);
      float volley = seconds(s.volley, t);
      float riven = seconds(s.riven, t);
      boolean hand = in(aim, 0.36F, 0.62F) || in(snare, 0.2F, 0.36F) || in(volley, 0.2F, 0.38F) || in(riven, 0.4F, 0.7F);
      boolean nocked = in(aim, 0.62F, 1.1F)
         || in(snare, 0.36F, 0.7F)
         || in(volley, 0.38F, 0.5F)
         || in(volley, 0.58F, 0.8F)
         || in(volley, 0.88F, 1.1F)
         || in(riven, 0.7F, 2.0F);
      this.handArrow.visible = hand;
      this.nockedArrow.visible = nocked;
      this.pullArrow.visible = !in(aim, 0.36F, 1.5F) && !in(snare, 0.2F, 1.0F) && !in(volley, 0.2F, 1.6F) && !in(riven, 0.4F, 2.6F);
   }

   private static float seconds(AnimationState a, float t) {
      return a.isStarted() ? (float)a.getTimeInMillis(t) / 1000.0F : -1.0F;
   }

   private static boolean in(float v, float from, float to) {
      return v >= from && v < to;
   }

   public static class State extends LivingEntityRenderState {
      public final AnimationState aim = new AnimationState();
      public final AnimationState volley = new AnimationState();
      public final AnimationState snare = new AnimationState();
      public final AnimationState leap = new AnimationState();
      public final AnimationState vanish = new AnimationState();
      public final AnimationState riven = new AnimationState();
      public float collapse;
      public boolean hunting;
   }
}
