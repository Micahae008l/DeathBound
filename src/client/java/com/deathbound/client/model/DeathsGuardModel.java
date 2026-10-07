package com.deathbound.client.model;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;

public class DeathsGuardModel extends EntityModel<DeathsGuardModel.State> {
   private static final float[][] ATTENTION = new float[][]{
      {-8.0F, 0.0F, 6.0F}, {-45.0F, 0.0F, 0.0F}, {53.0F, 0.0F, -6.0F}, {4.0F, 0.0F, -4.0F}, {-12.0F, 0.0F, 0.0F}
   };
   private static final float[][] GUARD = new float[][]{
      {-30.0F, 0.0F, 10.0F}, {-60.0F, 0.0F, 0.0F}, {90.0F, -40.0F, 0.0F}, {-60.0F, -30.0F, -5.0F}, {-50.0F, 0.0F, 0.0F}
   };
   static final AnimationDefinition SWEEP = Anim.of(1.5F)
      .rot(
         "right_arm", 0.0F, -30.0F, 0.0F, 10.0F, 0.45F, -70.0F, 0.0F, 0.0F, 0.72F, -75.0F, -20.0F, 0.0F, 0.95F, -70.0F, -30.0F, 0.0F, 1.5F, -30.0F, 0.0F, 10.0F
      )
      .rot("right_forearm", 0.0F, -60.0F, 0.0F, 0.0F, 0.45F, -20.0F, 0.0F, 0.0F, 0.72F, -15.0F, 0.0F, 0.0F, 1.5F, -60.0F, 0.0F, 0.0F)
      .rot(
         "weapon",
         0.0F,
         90.0F,
         -40.0F,
         0.0F,
         0.3F,
         150.0F,
         -20.0F,
         0.0F,
         0.45F,
         180.0F,
         0.0F,
         0.0F,
         0.72F,
         180.0F,
         0.0F,
         0.0F,
         1.0F,
         170.0F,
         -10.0F,
         0.0F,
         1.5F,
         90.0F,
         -40.0F,
         0.0F
      )
      .rot(
         "left_arm",
         0.0F,
         -60.0F,
         -30.0F,
         -5.0F,
         0.45F,
         -70.0F,
         40.0F,
         0.0F,
         0.72F,
         -70.0F,
         -50.0F,
         0.0F,
         0.95F,
         -65.0F,
         -60.0F,
         0.0F,
         1.5F,
         -60.0F,
         -30.0F,
         -5.0F
      )
      .rot("left_forearm", 0.0F, -50.0F, 0.0F, 0.0F, 0.45F, -20.0F, 0.0F, 0.0F, 0.72F, -15.0F, 0.0F, 0.0F, 1.5F, -50.0F, 0.0F, 0.0F)
      .rot(
         "chest",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.45F,
         5.0F,
         50.0F,
         0.0F,
         0.6F,
         6.0F,
         20.0F,
         0.0F,
         0.72F,
         10.0F,
         -55.0F,
         0.0F,
         0.95F,
         8.0F,
         -62.0F,
         0.0F,
         1.5F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot("hips", 0.0F, 0.0F, 0.0F, 0.0F, 0.45F, 0.0F, 15.0F, 0.0F, 0.72F, 0.0F, -15.0F, 0.0F, 1.5F, 0.0F, 0.0F, 0.0F)
      .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.45F, 0.0F, -30.0F, 0.0F, 0.72F, 0.0F, 25.0F, 0.0F, 1.5F, 0.0F, 0.0F, 0.0F)
      .rot("right_leg", 0.0F, 0.0F, 0.0F, 0.0F, 0.45F, 15.0F, 0.0F, 0.0F, 0.72F, -20.0F, 0.0F, 0.0F, 1.5F, 0.0F, 0.0F, 0.0F)
      .rot("left_leg", 0.0F, 0.0F, 0.0F, 0.0F, 0.45F, -15.0F, 0.0F, 0.0F, 0.72F, 15.0F, 0.0F, 0.0F, 1.5F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition SLAM = Anim.of(2.1F)
      .rot(
         "right_arm",
         0.0F,
         -30.0F,
         0.0F,
         10.0F,
         0.6F,
         -170.0F,
         0.0F,
         10.0F,
         0.9F,
         -175.0F,
         0.0F,
         10.0F,
         1.1F,
         -50.0F,
         0.0F,
         10.0F,
         1.5F,
         -52.0F,
         0.0F,
         10.0F,
         2.1F,
         -30.0F,
         0.0F,
         10.0F
      )
      .rot("right_forearm", 0.0F, -60.0F, 0.0F, 0.0F, 0.6F, -20.0F, 0.0F, 0.0F, 1.1F, -10.0F, 0.0F, 0.0F, 1.5F, -10.0F, 0.0F, 0.0F, 2.1F, -60.0F, 0.0F, 0.0F)
      .rot("weapon", 0.0F, 90.0F, -40.0F, 0.0F, 0.6F, 165.0F, 0.0F, 0.0F, 1.1F, 180.0F, 0.0F, 0.0F, 1.5F, 180.0F, 0.0F, 0.0F, 2.1F, 90.0F, -40.0F, 0.0F)
      .rot(
         "left_arm",
         0.0F,
         -60.0F,
         -30.0F,
         -5.0F,
         0.6F,
         -165.0F,
         0.0F,
         -10.0F,
         0.9F,
         -170.0F,
         0.0F,
         -10.0F,
         1.1F,
         -55.0F,
         0.0F,
         -10.0F,
         1.5F,
         -57.0F,
         0.0F,
         -10.0F,
         2.1F,
         -60.0F,
         -30.0F,
         -5.0F
      )
      .rot("left_forearm", 0.0F, -50.0F, 0.0F, 0.0F, 0.6F, -25.0F, 0.0F, 0.0F, 1.1F, -10.0F, 0.0F, 0.0F, 1.5F, -10.0F, 0.0F, 0.0F, 2.1F, -50.0F, 0.0F, 0.0F)
      .rot(
         "chest",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.6F,
         -18.0F,
         0.0F,
         0.0F,
         0.9F,
         -22.0F,
         0.0F,
         0.0F,
         1.1F,
         35.0F,
         0.0F,
         0.0F,
         1.5F,
         33.0F,
         0.0F,
         0.0F,
         2.1F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -15.0F, 0.0F, 0.0F, 1.1F, 25.0F, 0.0F, 0.0F, 1.5F, 20.0F, 0.0F, 0.0F, 2.1F, 0.0F, 0.0F, 0.0F)
      .pos("hips", 0.0F, 0.0F, 0.0F, 0.0F, 0.9F, 0.0F, 1.0F, 0.0F, 1.1F, 0.0F, -3.0F, 0.0F, 1.5F, 0.0F, -3.0F, 0.0F, 2.1F, 0.0F, 0.0F, 0.0F)
      .rot("right_leg", 0.0F, 0.0F, 0.0F, 0.0F, 0.9F, 0.0F, 0.0F, 0.0F, 1.1F, -35.0F, 0.0F, 0.0F, 1.5F, -33.0F, 0.0F, 0.0F, 2.1F, 0.0F, 0.0F, 0.0F)
      .rot("right_shin", 0.0F, 0.0F, 0.0F, 0.0F, 0.9F, 0.0F, 0.0F, 0.0F, 1.1F, 55.0F, 0.0F, 0.0F, 1.5F, 52.0F, 0.0F, 0.0F, 2.1F, 0.0F, 0.0F, 0.0F)
      .rot("left_leg", 0.0F, 0.0F, 0.0F, 0.0F, 0.9F, 0.0F, 0.0F, 0.0F, 1.1F, -10.0F, 0.0F, 0.0F, 1.5F, -10.0F, 0.0F, 0.0F, 2.1F, 0.0F, 0.0F, 0.0F)
      .rot("left_shin", 0.0F, 0.0F, 0.0F, 0.0F, 0.9F, 0.0F, 0.0F, 0.0F, 1.1F, 30.0F, 0.0F, 0.0F, 1.5F, 28.0F, 0.0F, 0.0F, 2.1F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition JUDGE = Anim.of(2.8F)
      .rot(
         "right_arm",
         0.0F,
         -30.0F,
         0.0F,
         10.0F,
         0.7F,
         -170.0F,
         0.0F,
         10.0F,
         1.0F,
         -175.0F,
         0.0F,
         10.0F,
         1.15F,
         -40.0F,
         0.0F,
         8.0F,
         2.3F,
         -42.0F,
         0.0F,
         8.0F,
         2.8F,
         -30.0F,
         0.0F,
         10.0F
      )
      .rot("right_forearm", 0.0F, -60.0F, 0.0F, 0.0F, 0.7F, -20.0F, 0.0F, 0.0F, 1.15F, -30.0F, 0.0F, 0.0F, 2.3F, -30.0F, 0.0F, 0.0F, 2.8F, -60.0F, 0.0F, 0.0F)
      .rot("weapon", 0.0F, 90.0F, -40.0F, 0.0F, 0.7F, 165.0F, 0.0F, 0.0F, 1.15F, 200.0F, 0.0F, 0.0F, 2.3F, 200.0F, 0.0F, 0.0F, 2.8F, 90.0F, -40.0F, 0.0F)
      .rot(
         "left_arm",
         0.0F,
         -60.0F,
         -30.0F,
         -5.0F,
         0.7F,
         -165.0F,
         0.0F,
         -10.0F,
         1.0F,
         -170.0F,
         0.0F,
         -10.0F,
         1.15F,
         -45.0F,
         0.0F,
         -8.0F,
         2.3F,
         -47.0F,
         0.0F,
         -8.0F,
         2.8F,
         -60.0F,
         -30.0F,
         -5.0F
      )
      .rot("left_forearm", 0.0F, -50.0F, 0.0F, 0.0F, 0.7F, -25.0F, 0.0F, 0.0F, 1.15F, -30.0F, 0.0F, 0.0F, 2.3F, -30.0F, 0.0F, 0.0F, 2.8F, -50.0F, 0.0F, 0.0F)
      .rot(
         "chest",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.7F,
         -20.0F,
         0.0F,
         0.0F,
         1.0F,
         -24.0F,
         0.0F,
         0.0F,
         1.15F,
         30.0F,
         0.0F,
         0.0F,
         2.3F,
         28.0F,
         0.0F,
         0.0F,
         2.8F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.7F, -25.0F, 0.0F, 0.0F, 1.15F, -8.0F, 0.0F, 0.0F, 2.3F, -10.0F, 0.0F, 0.0F, 2.8F, 0.0F, 0.0F, 0.0F)
      .pos("hips", 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.15F, 0.0F, -6.0F, 0.0F, 2.3F, 0.0F, -6.0F, 0.0F, 2.8F, 0.0F, 0.0F, 0.0F)
      .rot("right_leg", 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.15F, -80.0F, 0.0F, 0.0F, 2.3F, -80.0F, 0.0F, 0.0F, 2.8F, 0.0F, 0.0F, 0.0F)
      .rot("right_shin", 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.15F, 80.0F, 0.0F, 0.0F, 2.3F, 80.0F, 0.0F, 0.0F, 2.8F, 0.0F, 0.0F, 0.0F)
      .rot("left_leg", 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.15F, -10.0F, 0.0F, 0.0F, 2.3F, -10.0F, 0.0F, 0.0F, 2.8F, 0.0F, 0.0F, 0.0F)
      .rot("left_shin", 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.15F, 85.0F, 0.0F, 0.0F, 2.3F, 85.0F, 0.0F, 0.0F, 2.8F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition LUNGE = Anim.of(1.5F)
      .rot("right_arm", 0.0F, -30.0F, 0.0F, 10.0F, 0.4F, -20.0F, 0.0F, 20.0F, 0.55F, -90.0F, 0.0F, 0.0F, 1.0F, -88.0F, 0.0F, 0.0F, 1.5F, -30.0F, 0.0F, 10.0F)
      .rot("right_forearm", 0.0F, -60.0F, 0.0F, 0.0F, 0.4F, -90.0F, 0.0F, 0.0F, 0.55F, 0.0F, 0.0F, 0.0F, 1.0F, -5.0F, 0.0F, 0.0F, 1.5F, -60.0F, 0.0F, 0.0F)
      .rot("weapon", 0.0F, 90.0F, -40.0F, 0.0F, 0.4F, 200.0F, 0.0F, 0.0F, 0.55F, 180.0F, 0.0F, 0.0F, 1.0F, 180.0F, 0.0F, 0.0F, 1.5F, 90.0F, -40.0F, 0.0F)
      .rot(
         "left_arm", 0.0F, -60.0F, -30.0F, -5.0F, 0.4F, -40.0F, 0.0F, -10.0F, 0.55F, -80.0F, 0.0F, 0.0F, 1.0F, -78.0F, 0.0F, 0.0F, 1.5F, -60.0F, -30.0F, -5.0F
      )
      .rot("left_forearm", 0.0F, -50.0F, 0.0F, 0.0F, 0.4F, -50.0F, 0.0F, 0.0F, 0.55F, -10.0F, 0.0F, 0.0F, 1.5F, -50.0F, 0.0F, 0.0F)
      .rot("chest", 0.0F, 0.0F, 0.0F, 0.0F, 0.4F, -10.0F, 20.0F, 0.0F, 0.55F, 20.0F, -10.0F, 0.0F, 1.0F, 18.0F, -8.0F, 0.0F, 1.5F, 0.0F, 0.0F, 0.0F)
      .rot("right_leg", 0.0F, 0.0F, 0.0F, 0.0F, 0.4F, 20.0F, 0.0F, 0.0F, 0.55F, 25.0F, 0.0F, 0.0F, 1.0F, 20.0F, 0.0F, 0.0F, 1.5F, 0.0F, 0.0F, 0.0F)
      .rot("left_leg", 0.0F, 0.0F, 0.0F, 0.0F, 0.4F, -30.0F, 0.0F, 0.0F, 0.55F, -40.0F, 0.0F, 0.0F, 1.0F, -35.0F, 0.0F, 0.0F, 1.5F, 0.0F, 0.0F, 0.0F)
      .rot("left_shin", 0.0F, 0.0F, 0.0F, 0.0F, 0.4F, 15.0F, 0.0F, 0.0F, 0.55F, 20.0F, 0.0F, 0.0F, 1.5F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition INSPECT = Anim.of(2.5F)
      .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, 25.0F, 0.0F, 12.0F, 1.2F, 30.0F, 0.0F, -8.0F, 1.9F, 26.0F, 0.0F, 10.0F, 2.5F, 0.0F, 0.0F, 0.0F)
      .rot("left_arm", 0.0F, 4.0F, 0.0F, -4.0F, 0.5F, -75.0F, 0.0F, -15.0F, 1.9F, -72.0F, 0.0F, -15.0F, 2.5F, 4.0F, 0.0F, -4.0F)
      .rot("left_forearm", 0.0F, -12.0F, 0.0F, 0.0F, 0.5F, -35.0F, 0.0F, 0.0F, 1.9F, -35.0F, 0.0F, 0.0F, 2.5F, -12.0F, 0.0F, 0.0F)
      .rot("right_arm", 0.0F, -8.0F, 0.0F, 6.0F, 2.5F, -8.0F, 0.0F, 6.0F)
      .rot("right_forearm", 0.0F, -45.0F, 0.0F, 0.0F, 2.5F, -45.0F, 0.0F, 0.0F)
      .rot("weapon", 0.0F, 53.0F, 0.0F, -6.0F, 2.5F, 53.0F, 0.0F, -6.0F)
      .rot("chest", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, 8.0F, 0.0F, 0.0F, 1.9F, 8.0F, 0.0F, 0.0F, 2.5F, 0.0F, 0.0F, 0.0F)
      .rot("jaw", 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.15F, 15.0F, 0.0F, 0.0F, 1.4F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition CONDEMN = Anim.of(1.2F)
      .rot("right_arm", 0.0F, -8.0F, 0.0F, 6.0F, 0.35F, -170.0F, 0.0F, 10.0F, 0.55F, -60.0F, 0.0F, 10.0F, 0.8F, -62.0F, 0.0F, 10.0F, 1.2F, -8.0F, 0.0F, 6.0F)
      .rot("right_forearm", 0.0F, -45.0F, 0.0F, 0.0F, 0.35F, -20.0F, 0.0F, 0.0F, 0.55F, -10.0F, 0.0F, 0.0F, 1.2F, -45.0F, 0.0F, 0.0F)
      .rot("weapon", 0.0F, 53.0F, 0.0F, -6.0F, 0.35F, 165.0F, 0.0F, 0.0F, 0.55F, 180.0F, 0.0F, 0.0F, 0.8F, 180.0F, 0.0F, 0.0F, 1.2F, 53.0F, 0.0F, -6.0F)
      .rot("left_arm", 0.0F, 4.0F, 0.0F, -4.0F, 0.35F, -150.0F, 0.0F, -10.0F, 0.55F, -60.0F, 0.0F, -10.0F, 1.2F, 4.0F, 0.0F, -4.0F)
      .rot("left_forearm", 0.0F, -12.0F, 0.0F, 0.0F, 0.35F, -20.0F, 0.0F, 0.0F, 0.55F, -10.0F, 0.0F, 0.0F, 1.2F, -12.0F, 0.0F, 0.0F)
      .rot("chest", 0.0F, 0.0F, 0.0F, 0.0F, 0.35F, -12.0F, 0.0F, 0.0F, 0.55F, 25.0F, 0.0F, 0.0F, 0.8F, 22.0F, 0.0F, 0.0F, 1.2F, 0.0F, 0.0F, 0.0F)
      .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.35F, -10.0F, 0.0F, 0.0F, 0.55F, 20.0F, 0.0F, 0.0F, 1.2F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition COLLAPSE = Anim.of(2.5F)
      .rot("right_arm", 0.0F, -30.0F, 0.0F, 10.0F, 0.6F, -10.0F, 0.0F, 12.0F, 1.6F, -40.0F, 0.0F, 20.0F, 2.5F, -45.0F, 0.0F, 25.0F)
      .rot("right_forearm", 0.0F, -60.0F, 0.0F, 0.0F, 0.6F, -20.0F, 0.0F, 0.0F, 2.5F, -10.0F, 0.0F, 0.0F)
      .rot("weapon", 0.0F, 90.0F, -40.0F, 0.0F, 0.6F, 120.0F, -30.0F, 40.0F, 1.4F, 150.0F, -30.0F, 80.0F, 2.5F, 150.0F, -30.0F, 85.0F)
      .rot("left_arm", 0.0F, -60.0F, -30.0F, -5.0F, 0.6F, 0.0F, 0.0F, -10.0F, 1.6F, -45.0F, 0.0F, -20.0F, 2.5F, -50.0F, 0.0F, -25.0F)
      .rot("left_forearm", 0.0F, -50.0F, 0.0F, 0.0F, 0.6F, -10.0F, 0.0F, 0.0F, 2.5F, -5.0F, 0.0F, 0.0F)
      .pos("hips", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, 0.0F, -9.0F, 0.0F, 1.0F, 0.0F, -10.0F, 0.0F, 1.6F, 0.0F, -15.0F, -4.0F, 2.5F, 0.0F, -17.0F, -6.0F)
      .rot("right_leg", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -80.0F, 0.0F, 0.0F, 2.5F, -85.0F, 0.0F, 5.0F)
      .rot("right_shin", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, 90.0F, 0.0F, 0.0F, 2.5F, 95.0F, 0.0F, 0.0F)
      .rot("left_leg", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -20.0F, 0.0F, -5.0F, 1.0F, -60.0F, 0.0F, -5.0F, 2.5F, -80.0F, 0.0F, -8.0F)
      .rot("left_shin", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, 60.0F, 0.0F, 0.0F, 1.0F, 90.0F, 0.0F, 0.0F, 2.5F, 95.0F, 0.0F, 0.0F)
      .rot("chest", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, 30.0F, 0.0F, 0.0F, 1.0F, 25.0F, 0.0F, 0.0F, 1.6F, 65.0F, 0.0F, 5.0F, 2.5F, 80.0F, 0.0F, 8.0F)
      .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, 40.0F, 0.0F, 0.0F, 1.6F, 30.0F, 15.0F, 10.0F, 2.5F, 25.0F, 20.0F, 15.0F)
      .rot("jaw", 0.0F, 0.0F, 0.0F, 0.0F, 0.4F, 30.0F, 0.0F, 0.0F, 2.5F, 25.0F, 0.0F, 0.0F)
      .build();
   private final ModelPart root;
   private final ModelPart hips;
   private final ModelPart chest;
   private final ModelPart head;
   private final ModelPart jaw;
   private final ModelPart rightArm;
   private final ModelPart rightForearm;
   private final ModelPart leftArm;
   private final ModelPart leftForearm;
   private final ModelPart weapon;
   private final ModelPart tabardFront;
   private final ModelPart tabardBack;
   private final ModelPart rightLeg;
   private final ModelPart rightShin;
   private final ModelPart leftLeg;
   private final ModelPart leftShin;
   private final KeyframeAnimation sweep;
   private final KeyframeAnimation slam;
   private final KeyframeAnimation lunge;
   private final KeyframeAnimation inspect;
   private final KeyframeAnimation condemn;
   private final KeyframeAnimation collapse;
   private final KeyframeAnimation judge;

   public DeathsGuardModel(ModelPart model) {
      super(model);
      this.root = model.getChild("root");
      this.hips = this.root.getChild("hips");
      this.chest = this.hips.getChild("chest");
      this.head = this.chest.getChild("head");
      this.jaw = this.head.getChild("jaw");
      this.rightArm = this.chest.getChild("right_arm");
      this.rightForearm = this.rightArm.getChild("right_forearm");
      this.weapon = this.rightForearm.getChild("weapon");
      this.leftArm = this.chest.getChild("left_arm");
      this.leftForearm = this.leftArm.getChild("left_forearm");
      this.tabardFront = this.hips.getChild("tabard_front");
      this.tabardBack = this.hips.getChild("tabard_back");
      this.rightLeg = this.root.getChild("right_leg");
      this.rightShin = this.rightLeg.getChild("right_shin");
      this.leftLeg = this.root.getChild("left_leg");
      this.leftShin = this.leftLeg.getChild("left_shin");
      this.sweep = SWEEP.bake(model);
      this.slam = SLAM.bake(model);
      this.lunge = LUNGE.bake(model);
      this.inspect = INSPECT.bake(model);
      this.condemn = CONDEMN.bake(model);
      this.judge = JUDGE.bake(model);
      this.collapse = COLLAPSE.bake(model);
   }

   private void kneel(float age) {
      this.root.y += 10.0F;
      this.rightLeg.xRot += -0.06981317F;
      this.rightShin.xRot += 1.6057029F;
      this.leftLeg.xRot += -1.4660766F;
      this.leftLeg.zRot += -0.10471976F;
      this.leftShin.xRot += 1.5009831F;
      this.tabardFront.xRot += -1.25F;
      this.tabardBack.xRot += 0.35F;
      this.chest.xRot = this.chest.xRot + (16.0F + Mth.sin(age * 0.05F) * 1.2F) * 0.017453292F;
      this.head.xRot += 0.6632251F;
      this.rightArm.xRot += -0.6981317F;
      this.rightArm.zRot += 0.13962634F;
      this.rightForearm.xRot += -0.5235988F;
      this.weapon.xRot += 4.0142574F;
      this.leftArm.xRot += -0.7853982F;
      this.leftArm.zRot += -0.13962634F;
      this.leftForearm.xRot += -0.5235988F;
   }

   private static void add(ModelPart part, float[] a, float[] b, float t) {
      part.xRot = part.xRot + Mth.lerp(t, a[0], b[0]) * 0.017453292F;
      part.yRot = part.yRot + Mth.lerp(t, a[1], b[1]) * 0.017453292F;
      part.zRot = part.zRot + Mth.lerp(t, a[2], b[2]) * 0.017453292F;
   }

   public void setupAnim(DeathsGuardModel.State state) {
      super.setupAnim(state);
      float age = state.ageInTicks;
      float speed = Math.min(state.walkAnimationSpeed, 1.0F);
      float walk = state.walkAnimationPos * 0.5F;
      boolean dying = state.collapse > 0.0F;
      if (state.kneel) {
         this.kneel(age);
      } else {
         if (state.action == 0 && !dying) {
            float r = state.readiness;
            ModelPart[] arms = new ModelPart[]{this.rightArm, this.rightForearm, this.weapon, this.leftArm, this.leftForearm};

            for (int i = 0; i < arms.length; i++) {
               add(arms[i], ATTENTION[i], GUARD[i], r);
            }

            this.chest.xRot += r * 8.0F * 0.017453292F;
            this.chest.yRot += r * -10.0F * 0.017453292F;
            this.head.yRot = this.head.yRot + state.yRot * 0.017453292F;
            this.head.xRot = this.head.xRot + state.xRot * 0.017453292F;
            this.rightLeg.xRot = this.rightLeg.xRot + Mth.cos(walk) * 0.55F * speed;
            this.leftLeg.xRot = this.leftLeg.xRot + Mth.cos(walk + 3.1415927F) * 0.55F * speed;
            this.rightShin.xRot = this.rightShin.xRot + Math.max(0.0F, -Mth.sin(walk)) * 0.7F * speed;
            this.leftShin.xRot = this.leftShin.xRot + Math.max(0.0F, Mth.sin(walk)) * 0.7F * speed;
            this.hips.y = this.hips.y - Math.abs(Mth.sin(walk)) * 1.2F * speed;
            this.chest.yRot = this.chest.yRot + Mth.sin(walk) * 0.06F * speed;
            this.leftArm.xRot = this.leftArm.xRot + Mth.cos(walk) * 0.35F * speed * (1.0F - r);
            this.chest.xRot = this.chest.xRot + Mth.sin(age * 0.05F) * 0.02F;
            this.head.zRot = this.head.zRot + Mth.sin(age * 0.03F) * 0.03F;
         }

         this.jaw.xRot = this.jaw.xRot + (0.04F + Mth.sin(age * 0.08F) * 0.03F);
         this.tabardFront.xRot = this.tabardFront.xRot + (-0.5F * speed + Mth.sin(age * 0.11F) * 0.05F);
         this.tabardBack.xRot = this.tabardBack.xRot + (0.45F * speed + Mth.sin(age * 0.11F + 1.2F) * 0.05F);
         if (dying) {
            this.collapse.apply((long)(state.collapse * 50.0F), 1.0F);
         } else {
            float heavy = 0.72F;
            this.sweep.apply(state.sweep, age, heavy);
            this.slam.apply(state.slam, age, heavy);
            this.lunge.apply(state.lunge, age, heavy);
            this.inspect.apply(state.inspect, age);
            this.condemn.apply(state.condemn, age);
            this.judge.apply(state.judge, age);
         }
      }
   }

   public static class State extends LivingEntityRenderState {
      public final AnimationState sweep = new AnimationState();
      public final AnimationState slam = new AnimationState();
      public final AnimationState lunge = new AnimationState();
      public final AnimationState inspect = new AnimationState();
      public final AnimationState condemn = new AnimationState();
      public final AnimationState judge = new AnimationState();
      public int action;
      public float readiness;
      public float collapse;
      public boolean kneel;
   }
}
