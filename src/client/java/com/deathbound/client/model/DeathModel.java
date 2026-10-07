package com.deathbound.client.model;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;

public class DeathModel extends EntityModel<DeathModel.State> {
   static final AnimationDefinition GESTURE = Anim.of(1.7F)
      .rot("right_arm", 0.0F, -40.0F, 0.0F, 0.0F, 0.6F, -75.0F, 0.0F, 10.0F, 1.1F, -72.0F, 0.0F, 10.0F, 1.7F, -40.0F, 0.0F, 0.0F)
      .rot("right_forearm", 0.0F, -35.0F, 0.0F, 0.0F, 0.6F, -95.0F, 0.0F, 0.0F, 1.1F, -90.0F, 0.0F, 0.0F, 1.7F, -35.0F, 0.0F, 0.0F)
      .rot("right_hand", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -35.0F, 0.0F, 0.0F, 1.1F, -30.0F, 0.0F, 0.0F, 1.7F, 0.0F, 0.0F, 0.0F)
      .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, 12.0F, -10.0F, 0.0F, 1.1F, 10.0F, -8.0F, 0.0F, 1.7F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition RISE = Anim.of(3.5F)
      .pos("hips", 0.0F, 0.0F, -11.0F, 0.0F, 0.8F, 0.0F, -12.0F, -1.0F, 1.6F, 0.0F, -4.0F, 0.0F, 2.2F, 0.0F, 0.0F, 0.0F, 3.5F, 0.0F, 0.0F, 0.0F)
      .rot("right_leg", 0.0F, -90.0F, 8.0F, 0.0F, 0.8F, -95.0F, 8.0F, 0.0F, 1.6F, -40.0F, 4.0F, 0.0F, 2.2F, 0.0F, 0.0F, 0.0F, 3.5F, 0.0F, 0.0F, 0.0F)
      .rot("left_leg", 0.0F, -90.0F, -8.0F, 0.0F, 0.8F, -95.0F, -8.0F, 0.0F, 1.6F, -40.0F, -4.0F, 0.0F, 2.2F, 0.0F, 0.0F, 0.0F, 3.5F, 0.0F, 0.0F, 0.0F)
      .rot("right_shin", 0.0F, 90.0F, 0.0F, 0.0F, 0.8F, 95.0F, 0.0F, 0.0F, 1.6F, 45.0F, 0.0F, 0.0F, 2.2F, 0.0F, 0.0F, 0.0F, 3.5F, 0.0F, 0.0F, 0.0F)
      .rot("left_shin", 0.0F, 90.0F, 0.0F, 0.0F, 0.8F, 95.0F, 0.0F, 0.0F, 1.6F, 45.0F, 0.0F, 0.0F, 2.2F, 0.0F, 0.0F, 0.0F, 3.5F, 0.0F, 0.0F, 0.0F)
      .rot(
         "chest",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.8F,
         25.0F,
         0.0F,
         0.0F,
         1.6F,
         10.0F,
         0.0F,
         0.0F,
         2.3F,
         -25.0F,
         0.0F,
         0.0F,
         3.0F,
         -15.0F,
         0.0F,
         0.0F,
         3.5F,
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
         0.8F,
         15.0F,
         0.0F,
         0.0F,
         2.0F,
         0.0F,
         0.0F,
         0.0F,
         2.3F,
         -30.0F,
         0.0F,
         0.0F,
         3.0F,
         -20.0F,
         0.0F,
         0.0F,
         3.5F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "right_arm",
         0.0F,
         -40.0F,
         0.0F,
         0.0F,
         0.8F,
         -20.0F,
         0.0F,
         10.0F,
         1.6F,
         -10.0F,
         0.0F,
         25.0F,
         2.3F,
         -60.0F,
         0.0F,
         70.0F,
         3.0F,
         -50.0F,
         0.0F,
         60.0F,
         3.5F,
         -15.0F,
         0.0F,
         15.0F
      )
      .rot(
         "left_arm",
         0.0F,
         -40.0F,
         0.0F,
         0.0F,
         0.8F,
         -20.0F,
         0.0F,
         -10.0F,
         1.6F,
         -10.0F,
         0.0F,
         -25.0F,
         2.3F,
         -60.0F,
         0.0F,
         -70.0F,
         3.0F,
         -50.0F,
         0.0F,
         -60.0F,
         3.5F,
         -15.0F,
         0.0F,
         -15.0F
      )
      .rot("right_forearm", 0.0F, -35.0F, 0.0F, 0.0F, 0.8F, -10.0F, 0.0F, 0.0F, 2.3F, -20.0F, 0.0F, 0.0F, 3.5F, -30.0F, 0.0F, 0.0F)
      .rot("left_forearm", 0.0F, -35.0F, 0.0F, 0.0F, 0.8F, -10.0F, 0.0F, 0.0F, 2.3F, -20.0F, 0.0F, 0.0F, 3.5F, -30.0F, 0.0F, 0.0F)
      .rot("cape", 0.0F, 20.0F, 0.0F, 0.0F, 1.6F, 10.0F, 0.0F, 0.0F, 2.3F, 40.0F, 0.0F, 0.0F, 3.5F, 8.0F, 0.0F, 0.0F)
      .pos("root", 0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F, 2.3F, 0.0F, 3.0F, 0.0F, 3.5F, 0.0F, 2.0F, 0.0F)
      .build();
   static final AnimationDefinition BARRAGE = Anim.of(2.3F)
      .rot(
         "right_arm",
         0.0F,
         -15.0F,
         0.0F,
         15.0F,
         0.4F,
         -100.0F,
         0.0F,
         20.0F,
         0.5F,
         -115.0F,
         0.0F,
         10.0F,
         0.65F,
         -95.0F,
         0.0F,
         20.0F,
         1.1F,
         -115.0F,
         0.0F,
         10.0F,
         1.25F,
         -95.0F,
         0.0F,
         20.0F,
         1.7F,
         -115.0F,
         0.0F,
         10.0F,
         1.9F,
         -95.0F,
         0.0F,
         20.0F,
         2.3F,
         -15.0F,
         0.0F,
         15.0F
      )
      .rot(
         "left_arm",
         0.0F,
         -15.0F,
         0.0F,
         -15.0F,
         0.4F,
         -100.0F,
         0.0F,
         -20.0F,
         0.8F,
         -115.0F,
         0.0F,
         -10.0F,
         0.95F,
         -95.0F,
         0.0F,
         -20.0F,
         1.4F,
         -115.0F,
         0.0F,
         -10.0F,
         1.55F,
         -95.0F,
         0.0F,
         -20.0F,
         2.3F,
         -15.0F,
         0.0F,
         -15.0F
      )
      .rot("right_forearm", 0.0F, -30.0F, 0.0F, 0.0F, 0.4F, -20.0F, 0.0F, 0.0F, 2.0F, -20.0F, 0.0F, 0.0F, 2.3F, -30.0F, 0.0F, 0.0F)
      .rot("left_forearm", 0.0F, -30.0F, 0.0F, 0.0F, 0.4F, -20.0F, 0.0F, 0.0F, 2.0F, -20.0F, 0.0F, 0.0F, 2.3F, -30.0F, 0.0F, 0.0F)
      .rot(
         "chest",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.4F,
         -8.0F,
         0.0F,
         0.0F,
         0.5F,
         -4.0F,
         8.0F,
         0.0F,
         0.8F,
         -4.0F,
         -8.0F,
         0.0F,
         1.1F,
         -4.0F,
         8.0F,
         0.0F,
         1.4F,
         -4.0F,
         -8.0F,
         0.0F,
         1.7F,
         -4.0F,
         8.0F,
         0.0F,
         2.3F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.4F, -10.0F, 0.0F, 0.0F, 2.0F, -10.0F, 0.0F, 0.0F, 2.3F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition BLINK = Anim.of(1.6F)
      .scale(
         "root",
         0.0F,
         1.0F,
         1.0F,
         1.0F,
         0.2F,
         1.1F,
         0.85F,
         1.1F,
         0.38F,
         0.05F,
         1.6F,
         0.05F,
         0.42F,
         0.05F,
         1.6F,
         0.05F,
         0.55F,
         1.0F,
         1.0F,
         1.0F,
         1.6F,
         1.0F,
         1.0F,
         1.0F
      )
      .rot("chest", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, 25.0F, 0.0F, 0.0F, 0.55F, 15.0F, 25.0F, 0.0F, 0.85F, 20.0F, -35.0F, 0.0F, 1.6F, 0.0F, 0.0F, 0.0F)
      .rot(
         "right_arm",
         0.0F,
         -15.0F,
         0.0F,
         15.0F,
         0.55F,
         -120.0F,
         0.0F,
         60.0F,
         0.7F,
         -150.0F,
         20.0F,
         50.0F,
         0.85F,
         -40.0F,
         -30.0F,
         -30.0F,
         1.1F,
         -35.0F,
         -20.0F,
         -20.0F,
         1.6F,
         -15.0F,
         0.0F,
         15.0F
      )
      .rot("right_forearm", 0.0F, -30.0F, 0.0F, 0.0F, 0.7F, -40.0F, 0.0F, 0.0F, 0.85F, -5.0F, 0.0F, 0.0F, 1.6F, -30.0F, 0.0F, 0.0F)
      .rot("right_hand", 0.0F, 0.0F, 0.0F, 0.0F, 0.7F, -40.0F, 0.0F, 0.0F, 0.85F, 20.0F, 0.0F, 0.0F, 1.6F, 0.0F, 0.0F, 0.0F)
      .rot("left_arm", 0.0F, -15.0F, 0.0F, -15.0F, 0.85F, 10.0F, 0.0F, -30.0F, 1.6F, -15.0F, 0.0F, -15.0F)
      .build();
   static final AnimationDefinition NOVA = Anim.of(2.8F)
      .rot(
         "right_arm",
         0.0F,
         -15.0F,
         0.0F,
         15.0F,
         0.8F,
         -170.0F,
         0.0F,
         20.0F,
         1.0F,
         -175.0F,
         0.0F,
         15.0F,
         1.1F,
         -60.0F,
         0.0F,
         25.0F,
         1.8F,
         -55.0F,
         0.0F,
         25.0F,
         2.8F,
         -15.0F,
         0.0F,
         15.0F
      )
      .rot(
         "left_arm",
         0.0F,
         -15.0F,
         0.0F,
         -15.0F,
         0.8F,
         -170.0F,
         0.0F,
         -20.0F,
         1.0F,
         -175.0F,
         0.0F,
         -15.0F,
         1.1F,
         -60.0F,
         0.0F,
         -25.0F,
         1.8F,
         -55.0F,
         0.0F,
         -25.0F,
         2.8F,
         -15.0F,
         0.0F,
         -15.0F
      )
      .rot("right_forearm", 0.0F, -30.0F, 0.0F, 0.0F, 0.8F, -10.0F, 0.0F, 0.0F, 1.1F, 0.0F, 0.0F, 0.0F, 2.8F, -30.0F, 0.0F, 0.0F)
      .rot("left_forearm", 0.0F, -30.0F, 0.0F, 0.0F, 0.8F, -10.0F, 0.0F, 0.0F, 1.1F, 0.0F, 0.0F, 0.0F, 2.8F, -30.0F, 0.0F, 0.0F)
      .rot("chest", 0.0F, 0.0F, 0.0F, 0.0F, 0.8F, -15.0F, 0.0F, 0.0F, 1.1F, 32.0F, 0.0F, 0.0F, 1.8F, 28.0F, 0.0F, 0.0F, 2.8F, 0.0F, 0.0F, 0.0F)
      .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.8F, -25.0F, 0.0F, 0.0F, 1.1F, 20.0F, 0.0F, 0.0F, 2.8F, 0.0F, 0.0F, 0.0F)
      .pos("hips", 0.0F, 0.0F, 0.0F, 0.0F, 0.8F, 0.0F, 1.0F, 0.0F, 1.1F, 0.0F, -4.0F, 0.0F, 1.8F, 0.0F, -4.0F, 0.0F, 2.8F, 0.0F, 0.0F, 0.0F)
      .rot("right_leg", 0.0F, 0.0F, 0.0F, 0.0F, 1.1F, -30.0F, 0.0F, 0.0F, 1.8F, -30.0F, 0.0F, 0.0F, 2.8F, 0.0F, 0.0F, 0.0F)
      .rot("left_leg", 0.0F, 0.0F, 0.0F, 0.0F, 1.1F, -30.0F, 0.0F, 0.0F, 1.8F, -30.0F, 0.0F, 0.0F, 2.8F, 0.0F, 0.0F, 0.0F)
      .rot("right_shin", 0.0F, 0.0F, 0.0F, 0.0F, 1.1F, 50.0F, 0.0F, 0.0F, 1.8F, 50.0F, 0.0F, 0.0F, 2.8F, 0.0F, 0.0F, 0.0F)
      .rot("left_shin", 0.0F, 0.0F, 0.0F, 0.0F, 1.1F, 50.0F, 0.0F, 0.0F, 1.8F, 50.0F, 0.0F, 0.0F, 2.8F, 0.0F, 0.0F, 0.0F)
      .rot("cape", 0.0F, 8.0F, 0.0F, 0.0F, 1.1F, 45.0F, 0.0F, 0.0F, 2.8F, 8.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition SUMMON = Anim.of(2.0F)
      .rot("right_arm", 0.0F, -15.0F, 0.0F, 15.0F, 0.6F, -35.0F, 0.0F, 85.0F, 1.4F, -40.0F, 0.0F, 88.0F, 2.0F, -15.0F, 0.0F, 15.0F)
      .rot("left_arm", 0.0F, -15.0F, 0.0F, -15.0F, 0.6F, -35.0F, 0.0F, -85.0F, 1.4F, -40.0F, 0.0F, -88.0F, 2.0F, -15.0F, 0.0F, -15.0F)
      .rot("right_forearm", 0.0F, -30.0F, 0.0F, 0.0F, 0.6F, -10.0F, 0.0F, 0.0F, 2.0F, -30.0F, 0.0F, 0.0F)
      .rot("left_forearm", 0.0F, -30.0F, 0.0F, 0.0F, 0.6F, -10.0F, 0.0F, 0.0F, 2.0F, -30.0F, 0.0F, 0.0F)
      .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -35.0F, 0.0F, 0.0F, 1.4F, -30.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F)
      .rot("chest", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -15.0F, 0.0F, 0.0F, 1.4F, -12.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F)
      .pos("root", 0.0F, 0.0F, 2.0F, 0.0F, 1.0F, 0.0F, 5.0F, 0.0F, 1.4F, 0.0F, 5.0F, 0.0F, 2.0F, 0.0F, 2.0F, 0.0F)
      .build();
   static final AnimationDefinition TRANSFORM = Anim.of(2.5F)
      .rot("right_arm", 0.0F, -15.0F, 0.0F, 15.0F, 0.4F, -150.0F, 0.0F, -25.0F, 2.5F, -155.0F, 0.0F, -30.0F)
      .rot("left_arm", 0.0F, -15.0F, 0.0F, -15.0F, 0.4F, -150.0F, 0.0F, 25.0F, 2.5F, -155.0F, 0.0F, 30.0F)
      .rot("right_forearm", 0.0F, -30.0F, 0.0F, 0.0F, 0.4F, -95.0F, 0.0F, 0.0F, 2.5F, -100.0F, 0.0F, 0.0F)
      .rot("left_forearm", 0.0F, -30.0F, 0.0F, 0.0F, 0.4F, -95.0F, 0.0F, 0.0F, 2.5F, -100.0F, 0.0F, 0.0F)
      .rot(
         "chest",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.3F,
         12.0F,
         6.0F,
         0.0F,
         0.45F,
         -6.0F,
         -6.0F,
         4.0F,
         0.6F,
         14.0F,
         4.0F,
         -3.0F,
         0.75F,
         -4.0F,
         -8.0F,
         2.0F,
         0.9F,
         16.0F,
         6.0F,
         -4.0F,
         1.1F,
         0.0F,
         -4.0F,
         5.0F,
         1.3F,
         20.0F,
         8.0F,
         -6.0F,
         1.5F,
         -2.0F,
         -6.0F,
         4.0F,
         1.7F,
         22.0F,
         4.0F,
         -2.0F,
         1.9F,
         5.0F,
         -8.0F,
         6.0F,
         2.1F,
         28.0F,
         6.0F,
         -5.0F,
         2.5F,
         35.0F,
         0.0F,
         0.0F
      )
      .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, 30.0F, 0.0F, 0.0F, 1.5F, 20.0F, 15.0F, 0.0F, 2.5F, 40.0F, -10.0F, 0.0F)
      .pos("root", 0.0F, 0.0F, 2.0F, 0.0F, 2.4F, 0.0F, 9.0F, 0.0F, 2.5F, 0.0F, 9.0F, 0.0F)
      .rot("cape", 0.0F, 8.0F, 0.0F, 0.0F, 1.0F, 50.0F, 0.0F, 0.0F, 2.5F, 70.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition SEATED = Anim.of(1.0F)
      .pos("hips", 0.0F, 0.0F, -11.0F, 0.0F)
      .rot("right_leg", 0.0F, -90.0F, 8.0F, 0.0F)
      .rot("left_leg", 0.0F, -90.0F, -8.0F, 0.0F)
      .rot("right_shin", 0.0F, 90.0F, 0.0F, 0.0F)
      .rot("left_shin", 0.0F, 90.0F, 0.0F, 0.0F)
      .rot("right_arm", 0.0F, -40.0F, 0.0F, 0.0F)
      .rot("left_arm", 0.0F, -40.0F, 0.0F, 0.0F)
      .rot("right_forearm", 0.0F, -35.0F, 0.0F, 0.0F)
      .rot("left_forearm", 0.0F, -35.0F, 0.0F, 0.0F)
      .rot("cape", 0.0F, 20.0F, 0.0F, 0.0F)
      .build();
   private final ModelPart root;
   private final ModelPart hips;
   private final ModelPart chest;
   private final ModelPart cape;
   private final ModelPart head;
   private final ModelPart rightArm;
   private final ModelPart leftArm;
   private final ModelPart rightForearm;
   private final ModelPart leftForearm;
   private final ModelPart rightHand;
   private final ModelPart leftHand;
   private final ModelPart rightOrb;
   private final ModelPart leftOrb;
   private final ModelPart rightLeg;
   private final ModelPart leftLeg;
   private final ModelPart rightShin;
   private final ModelPart leftShin;
   private final KeyframeAnimation gesture;
   private final KeyframeAnimation rise;
   private final KeyframeAnimation barrage;
   private final KeyframeAnimation blink;
   private final KeyframeAnimation nova;
   private final KeyframeAnimation summon;
   private final KeyframeAnimation transform;
   private final KeyframeAnimation seated;

   public DeathModel(ModelPart model) {
      super(model);
      this.root = model.getChild("root");
      this.hips = this.root.getChild("hips");
      this.chest = this.hips.getChild("chest");
      this.cape = this.chest.getChild("cape");
      this.head = this.chest.getChild("head");
      this.rightArm = this.chest.getChild("right_arm");
      this.leftArm = this.chest.getChild("left_arm");
      this.rightForearm = this.rightArm.getChild("right_forearm");
      this.leftForearm = this.leftArm.getChild("left_forearm");
      this.rightHand = this.rightForearm.getChild("right_hand");
      this.leftHand = this.leftForearm.getChild("left_hand");
      this.rightOrb = this.rightHand.getChild("right_orb");
      this.leftOrb = this.leftHand.getChild("left_orb");
      this.rightLeg = this.hips.getChild("right_leg");
      this.leftLeg = this.hips.getChild("left_leg");
      this.rightShin = this.rightLeg.getChild("right_shin");
      this.leftShin = this.leftLeg.getChild("left_shin");
      this.gesture = GESTURE.bake(model);
      this.rise = RISE.bake(model);
      this.barrage = BARRAGE.bake(model);
      this.blink = BLINK.bake(model);
      this.nova = NOVA.bake(model);
      this.summon = SUMMON.bake(model);
      this.transform = TRANSFORM.bake(model);
      this.seated = SEATED.bake(model);
   }

   public void setupAnim(DeathModel.State state) {
      super.setupAnim(state);
      float age = state.ageInTicks;
      int action = state.action;
      boolean seated = state.phase == 1 && action != 11;
      this.head.yRot = this.head.yRot + state.yRot * 0.017453292F;
      this.head.xRot = this.head.xRot + state.xRot * 0.017453292F;
      this.cape.xRot = this.cape.xRot + (Mth.sin(age * 0.06F) * 0.04F + Math.min(state.walkAnimationSpeed, 1.0F) * 0.5F);
      this.cape.zRot = this.cape.zRot + Mth.sin(age * 0.045F) * 0.03F;
      this.rightOrb.visible = state.phase >= 2;
      this.leftOrb.visible = state.phase >= 2;
      if (seated) {
         this.seated.applyStatic();
         this.chest.xRot = this.chest.xRot + Mth.sin(age * 0.04F) * 0.02F;
         this.rightHand.xRot = this.rightHand.xRot + Math.max(0.0F, Mth.sin(age * 0.25F)) * Math.max(0.0F, Mth.sin(age * 0.021F)) * 0.25F;
         this.gesture.apply(state.gesture, age);
      } else {
         if (action == 0 && state.collapse <= 0.0F) {
            float bob = Mth.sin(age * 0.07F);
            this.root.y -= 2.0F + bob * 1.2F;
            this.rightArm.xRot = this.rightArm.xRot + (-0.2617994F + Mth.sin(age * 0.05F) * 0.05F);
            this.rightArm.zRot += 0.2617994F;
            this.leftArm.xRot = this.leftArm.xRot + (-0.2617994F + Mth.sin(age * 0.05F + 1.5F) * 0.05F);
            this.leftArm.zRot -= 0.2617994F;
            this.rightForearm.xRot -= 0.5235988F;
            this.leftForearm.xRot -= 0.5235988F;
            this.rightLeg.xRot = this.rightLeg.xRot + Mth.sin(age * 0.05F) * 0.06F;
            this.leftLeg.xRot = this.leftLeg.xRot - Mth.sin(age * 0.05F) * 0.06F;
            this.cape.xRot += 0.13962634F;
            float speed = Math.min(state.walkAnimationSpeed * 1.6F, 1.0F);
            float stride = state.walkAnimationPos * 0.55F;
            this.rightLeg.xRot = this.rightLeg.xRot + Mth.cos(stride) * 0.6F * speed;
            this.leftLeg.xRot = this.leftLeg.xRot - Mth.cos(stride) * 0.6F * speed;
            this.rightShin.xRot = this.rightShin.xRot + Math.max(0.0F, Mth.sin(stride)) * 0.55F * speed;
            this.leftShin.xRot = this.leftShin.xRot + Math.max(0.0F, -Mth.sin(stride)) * 0.55F * speed;
            this.chest.xRot += 0.16F * speed;
            this.head.xRot -= 0.12F * speed;
            this.rightArm.xRot = this.rightArm.xRot - Mth.cos(stride) * 0.25F * speed;
            this.leftArm.xRot = this.leftArm.xRot + Mth.cos(stride) * 0.25F * speed;
            this.cape.xRot += 0.35F * speed;
         }

         float orbPulse = 1.0F + Mth.sin(age * 0.3F) * 0.12F;
         this.rightOrb.xScale = this.rightOrb.yScale = this.rightOrb.zScale = orbPulse;
         this.leftOrb.xScale = this.leftOrb.yScale = this.leftOrb.zScale = orbPulse;
         this.rightOrb.y = this.rightOrb.y + Mth.sin(age * 0.15F) * 0.6F;
         this.leftOrb.y = this.leftOrb.y + Mth.cos(age * 0.15F) * 0.6F;
         this.rise.apply(state.rise, age);
         this.barrage.apply(state.barrage, age);
         this.blink.apply(state.blink, age);
         this.nova.apply(state.nova, age);
         this.summon.apply(state.summon, age);
         this.transform.apply(state.transform, age);
      }
   }

   public static class State extends LivingEntityRenderState {
      public final AnimationState gesture = new AnimationState();
      public final AnimationState rise = new AnimationState();
      public final AnimationState barrage = new AnimationState();
      public final AnimationState blink = new AnimationState();
      public final AnimationState nova = new AnimationState();
      public final AnimationState summon = new AnimationState();
      public final AnimationState transform = new AnimationState();
      public final AnimationState claw = new AnimationState();
      public final AnimationState leap = new AnimationState();
      public final AnimationState charge = new AnimationState();
      public final AnimationState rip = new AnimationState();
      public final AnimationState roar = new AnimationState();
      public int phase = 1;
      public int action;
      public float collapse;
   }
}
