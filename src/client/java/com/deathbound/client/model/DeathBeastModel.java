package com.deathbound.client.model;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class DeathBeastModel extends EntityModel<DeathModel.State> {
   static final AnimationDefinition CLAW = Anim.of(1.7F)
      .rot("right_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.4F, -30.0F, 20.0F, 70.0F, 0.6F, -95.0F, -40.0F, -10.0F, 0.85F, -70.0F, -30.0F, 0.0F, 1.7F, 0.0F, 0.0F, 0.0F)
      .rot("right_forearm", 0.0F, 0.0F, 0.0F, 0.0F, 0.4F, -50.0F, 0.0F, 0.0F, 0.6F, -5.0F, 0.0F, 0.0F, 1.7F, 0.0F, 0.0F, 0.0F)
      .rot(
         "left_arm",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.6F,
         0.0F,
         0.0F,
         0.0F,
         0.85F,
         -30.0F,
         -20.0F,
         -70.0F,
         1.2F,
         -95.0F,
         40.0F,
         10.0F,
         1.4F,
         -70.0F,
         30.0F,
         0.0F,
         1.7F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot("left_forearm", 0.0F, 0.0F, 0.0F, 0.0F, 0.85F, -50.0F, 0.0F, 0.0F, 1.2F, -5.0F, 0.0F, 0.0F, 1.7F, 0.0F, 0.0F, 0.0F)
      .rot(
         "torso",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.4F,
         -5.0F,
         25.0F,
         0.0F,
         0.6F,
         5.0F,
         -30.0F,
         0.0F,
         0.85F,
         -5.0F,
         -25.0F,
         0.0F,
         1.2F,
         5.0F,
         30.0F,
         0.0F,
         1.7F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "jaw",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.5F,
         35.0F,
         0.0F,
         0.0F,
         0.7F,
         10.0F,
         0.0F,
         0.0F,
         1.1F,
         35.0F,
         0.0F,
         0.0F,
         1.3F,
         10.0F,
         0.0F,
         0.0F,
         1.7F,
         0.0F,
         0.0F,
         0.0F
      )
      .build();
   static final AnimationDefinition LEAP = Anim.of(2.8F)
      .pos(
         "pelvis",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.55F,
         0.0F,
         -7.0F,
         2.0F,
         0.65F,
         0.0F,
         4.0F,
         0.0F,
         1.0F,
         0.0F,
         3.0F,
         0.0F,
         1.2F,
         0.0F,
         -5.0F,
         0.0F,
         2.0F,
         0.0F,
         -4.0F,
         0.0F,
         2.8F,
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
         0.55F,
         -30.0F,
         0.0F,
         0.0F,
         0.65F,
         25.0F,
         0.0F,
         0.0F,
         1.0F,
         -20.0F,
         0.0F,
         0.0F,
         1.2F,
         -35.0F,
         0.0F,
         0.0F,
         2.0F,
         -30.0F,
         0.0F,
         0.0F,
         2.8F,
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
         0.55F,
         -30.0F,
         0.0F,
         0.0F,
         0.65F,
         25.0F,
         0.0F,
         0.0F,
         1.0F,
         -20.0F,
         0.0F,
         0.0F,
         1.2F,
         -35.0F,
         0.0F,
         0.0F,
         2.0F,
         -30.0F,
         0.0F,
         0.0F,
         2.8F,
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
         0.55F,
         45.0F,
         0.0F,
         0.0F,
         0.65F,
         -30.0F,
         0.0F,
         0.0F,
         1.0F,
         20.0F,
         0.0F,
         0.0F,
         1.2F,
         45.0F,
         0.0F,
         0.0F,
         2.8F,
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
         0.55F,
         45.0F,
         0.0F,
         0.0F,
         0.65F,
         -30.0F,
         0.0F,
         0.0F,
         1.0F,
         20.0F,
         0.0F,
         0.0F,
         1.2F,
         45.0F,
         0.0F,
         0.0F,
         2.8F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "torso",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.55F,
         15.0F,
         0.0F,
         0.0F,
         0.65F,
         -25.0F,
         0.0F,
         0.0F,
         1.0F,
         -20.0F,
         0.0F,
         0.0F,
         1.2F,
         25.0F,
         0.0F,
         0.0F,
         2.0F,
         20.0F,
         0.0F,
         0.0F,
         2.8F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "right_arm",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.55F,
         40.0F,
         0.0F,
         15.0F,
         0.65F,
         -140.0F,
         0.0F,
         20.0F,
         1.0F,
         -170.0F,
         0.0F,
         15.0F,
         1.2F,
         -40.0F,
         0.0F,
         20.0F,
         2.0F,
         -35.0F,
         0.0F,
         20.0F,
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
         0.55F,
         40.0F,
         0.0F,
         -15.0F,
         0.65F,
         -140.0F,
         0.0F,
         -20.0F,
         1.0F,
         -170.0F,
         0.0F,
         -15.0F,
         1.2F,
         -40.0F,
         0.0F,
         -20.0F,
         2.0F,
         -35.0F,
         0.0F,
         -20.0F,
         2.8F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot("jaw", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, 45.0F, 0.0F, 0.0F, 1.2F, 20.0F, 0.0F, 0.0F, 2.8F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition CHARGE = Anim.of(2.4F)
      .rot("neck", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, -35.0F, 0.0F, 0.0F, 0.7F, 20.0F, 0.0F, 0.0F, 2.0F, 18.0F, 0.0F, 0.0F, 2.4F, 0.0F, 0.0F, 0.0F)
      .rot("jaw", 0.0F, 0.0F, 0.0F, 0.0F, 0.4F, 45.0F, 0.0F, 0.0F, 0.7F, 15.0F, 0.0F, 0.0F, 2.4F, 0.0F, 0.0F, 0.0F)
      .rot("right_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, -30.0F, 0.0F, 60.0F, 0.7F, 45.0F, 0.0F, 10.0F, 2.0F, 40.0F, 0.0F, 10.0F, 2.4F, 0.0F, 0.0F, 0.0F)
      .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, -30.0F, 0.0F, -60.0F, 0.7F, 45.0F, 0.0F, -10.0F, 2.0F, 40.0F, 0.0F, -10.0F, 2.4F, 0.0F, 0.0F, 0.0F)
      .rot("torso", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, -15.0F, 0.0F, 0.0F, 0.7F, 25.0F, 0.0F, 0.0F, 2.0F, 22.0F, 0.0F, 0.0F, 2.4F, 0.0F, 0.0F, 0.0F)
      .rot("cloak", 0.0F, 0.0F, 0.0F, 0.0F, 0.7F, -45.0F, 0.0F, 0.0F, 2.0F, -50.0F, 0.0F, 0.0F, 2.4F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition RIP = Anim.of(2.2F)
      .rot("right_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.7F, -170.0F, 0.0F, 10.0F, 0.9F, -30.0F, 0.0F, 10.0F, 1.5F, -15.0F, 0.0F, 12.0F, 2.2F, 0.0F, 0.0F, 0.0F)
      .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.7F, -170.0F, 0.0F, -10.0F, 0.9F, -30.0F, 0.0F, -10.0F, 1.5F, -15.0F, 0.0F, -12.0F, 2.2F, 0.0F, 0.0F, 0.0F)
      .rot("torso", 0.0F, 0.0F, 0.0F, 0.0F, 0.7F, -25.0F, 0.0F, 0.0F, 0.9F, 30.0F, 0.0F, 0.0F, 1.5F, 26.0F, 0.0F, 0.0F, 2.2F, 0.0F, 0.0F, 0.0F)
      .pos("pelvis", 0.0F, 0.0F, 0.0F, 0.0F, 0.7F, 0.0F, 2.0F, 0.0F, 0.9F, 0.0F, -4.0F, 0.0F, 1.5F, 0.0F, -4.0F, 0.0F, 2.2F, 0.0F, 0.0F, 0.0F)
      .rot("jaw", 0.0F, 0.0F, 0.0F, 0.0F, 0.7F, 30.0F, 0.0F, 0.0F, 0.95F, 50.0F, 0.0F, 0.0F, 1.5F, 10.0F, 0.0F, 0.0F, 2.2F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition ROAR = Anim.of(2.0F)
      .rot("torso", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -25.0F, 0.0F, 0.0F, 1.4F, -22.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F)
      .rot("neck", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -20.0F, 0.0F, 0.0F, 1.4F, -18.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F)
      .rot(
         "head",
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.6F,
         -30.0F,
         0.0F,
         0.0F,
         0.8F,
         -25.0F,
         6.0F,
         0.0F,
         1.0F,
         -30.0F,
         -6.0F,
         0.0F,
         1.2F,
         -25.0F,
         4.0F,
         0.0F,
         1.4F,
         -28.0F,
         0.0F,
         0.0F,
         2.0F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot("jaw", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, 52.0F, 0.0F, 0.0F, 1.4F, 50.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F)
      .rot("right_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -60.0F, 0.0F, 80.0F, 1.4F, -58.0F, 0.0F, 78.0F, 2.0F, 0.0F, 0.0F, 0.0F)
      .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -60.0F, 0.0F, -80.0F, 1.4F, -58.0F, 0.0F, -78.0F, 2.0F, 0.0F, 0.0F, 0.0F)
      .rot("right_hand", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -40.0F, 0.0F, 0.0F, 1.4F, -40.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F)
      .rot("left_hand", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -40.0F, 0.0F, 0.0F, 1.4F, -40.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F)
      .rot("cloak", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -30.0F, 0.0F, 0.0F, 1.4F, -35.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition TRANSFORM = Anim.of(5.5F)
      .pos("root", 0.0F, 0.0F, 6.0F, 0.0F, 2.5F, 0.0F, 6.0F, 0.0F, 3.2F, 0.0F, 0.0F, 0.0F, 5.5F, 0.0F, 0.0F, 0.0F)
      .rot(
         "torso",
         0.0F,
         50.0F,
         0.0F,
         0.0F,
         2.5F,
         50.0F,
         0.0F,
         0.0F,
         3.3F,
         30.0F,
         0.0F,
         0.0F,
         4.0F,
         -28.0F,
         0.0F,
         0.0F,
         4.8F,
         -24.0F,
         0.0F,
         0.0F,
         5.5F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "neck",
         0.0F,
         30.0F,
         0.0F,
         0.0F,
         2.5F,
         30.0F,
         0.0F,
         0.0F,
         3.3F,
         10.0F,
         0.0F,
         0.0F,
         4.0F,
         -25.0F,
         0.0F,
         0.0F,
         4.8F,
         -20.0F,
         0.0F,
         0.0F,
         5.5F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot("head", 0.0F, 30.0F, 0.0F, 0.0F, 2.5F, 30.0F, 0.0F, 0.0F, 4.0F, -35.0F, 0.0F, 0.0F, 4.8F, -30.0F, 0.0F, 0.0F, 5.5F, 0.0F, 0.0F, 0.0F)
      .rot("jaw", 0.0F, 0.0F, 0.0F, 0.0F, 3.6F, 0.0F, 0.0F, 0.0F, 4.0F, 55.0F, 0.0F, 0.0F, 4.8F, 50.0F, 0.0F, 0.0F, 5.5F, 0.0F, 0.0F, 0.0F)
      .rot(
         "right_arm",
         0.0F,
         -100.0F,
         0.0F,
         -40.0F,
         2.5F,
         -100.0F,
         0.0F,
         -40.0F,
         3.3F,
         -60.0F,
         0.0F,
         10.0F,
         4.0F,
         -70.0F,
         0.0F,
         85.0F,
         4.8F,
         -65.0F,
         0.0F,
         80.0F,
         5.5F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot(
         "left_arm",
         0.0F,
         -100.0F,
         0.0F,
         40.0F,
         2.5F,
         -100.0F,
         0.0F,
         40.0F,
         3.3F,
         -60.0F,
         0.0F,
         -10.0F,
         4.0F,
         -70.0F,
         0.0F,
         -85.0F,
         4.8F,
         -65.0F,
         0.0F,
         -80.0F,
         5.5F,
         0.0F,
         0.0F,
         0.0F
      )
      .rot("right_forearm", 0.0F, -80.0F, 0.0F, 0.0F, 2.5F, -80.0F, 0.0F, 0.0F, 3.3F, -40.0F, 0.0F, 0.0F, 4.0F, 0.0F, 0.0F, 0.0F, 5.5F, 0.0F, 0.0F, 0.0F)
      .rot("left_forearm", 0.0F, -80.0F, 0.0F, 0.0F, 2.5F, -80.0F, 0.0F, 0.0F, 3.3F, -40.0F, 0.0F, 0.0F, 4.0F, 0.0F, 0.0F, 0.0F, 5.5F, 0.0F, 0.0F, 0.0F)
      .rot("right_thigh", 0.0F, -40.0F, 0.0F, 0.0F, 2.5F, -40.0F, 0.0F, 0.0F, 3.3F, 0.0F, 0.0F, 0.0F, 5.5F, 0.0F, 0.0F, 0.0F)
      .rot("left_thigh", 0.0F, -40.0F, 0.0F, 0.0F, 2.5F, -40.0F, 0.0F, 0.0F, 3.3F, 0.0F, 0.0F, 0.0F, 5.5F, 0.0F, 0.0F, 0.0F)
      .rot("right_shin", 0.0F, 40.0F, 0.0F, 0.0F, 2.5F, 40.0F, 0.0F, 0.0F, 3.3F, 0.0F, 0.0F, 0.0F, 5.5F, 0.0F, 0.0F, 0.0F)
      .rot("left_shin", 0.0F, 40.0F, 0.0F, 0.0F, 2.5F, 40.0F, 0.0F, 0.0F, 3.3F, 0.0F, 0.0F, 0.0F, 5.5F, 0.0F, 0.0F, 0.0F)
      .rot("cloak", 0.0F, 40.0F, 0.0F, 0.0F, 2.5F, 40.0F, 0.0F, 0.0F, 4.0F, -40.0F, 0.0F, 0.0F, 5.5F, 0.0F, 0.0F, 0.0F)
      .build();
   static final AnimationDefinition COLLAPSE = Anim.of(4.5F)
      .pos("pelvis", 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, -12.0F, 0.0F, 2.0F, 0.0F, -16.0F, 2.0F, 4.5F, 0.0F, -22.0F, 4.0F)
      .rot("right_thigh", 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, -50.0F, 0.0F, 10.0F, 4.5F, -55.0F, 0.0F, 15.0F)
      .rot("left_thigh", 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, -50.0F, 0.0F, -10.0F, 4.5F, -55.0F, 0.0F, -15.0F)
      .rot("right_shin", 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 80.0F, 0.0F, 0.0F, 4.5F, 85.0F, 0.0F, 0.0F)
      .rot("left_shin", 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 80.0F, 0.0F, 0.0F, 4.5F, 85.0F, 0.0F, 0.0F)
      .rot("torso", 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, -20.0F, 0.0F, 0.0F, 2.0F, 15.0F, 0.0F, 5.0F, 4.5F, 30.0F, 0.0F, 10.0F)
      .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, -40.0F, 0.0F, 0.0F, 2.0F, 30.0F, 20.0F, 0.0F, 4.5F, 40.0F, 30.0F, 10.0F)
      .rot("jaw", 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 55.0F, 0.0F, 0.0F, 4.5F, 40.0F, 0.0F, 0.0F)
      .rot("right_arm", 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, -80.0F, 0.0F, 70.0F, 2.0F, 20.0F, 0.0F, 20.0F, 4.5F, 30.0F, 0.0F, 25.0F)
      .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, -80.0F, 0.0F, -70.0F, 2.0F, 20.0F, 0.0F, -20.0F, 4.5F, 30.0F, 0.0F, -25.0F)
      .scale("root", 0.0F, 1.0F, 1.0F, 1.0F, 3.0F, 1.0F, 1.0F, 1.0F, 4.5F, 0.3F, 0.15F, 0.3F)
      .build();
   private final ModelPart root;
   private final ModelPart pelvis;
   private final ModelPart torso;
   private final ModelPart cloak;
   private final ModelPart neck;
   private final ModelPart head;
   private final ModelPart jaw;
   private final ModelPart rightArm;
   private final ModelPart leftArm;
   private final ModelPart rightForearm;
   private final ModelPart leftForearm;
   private final ModelPart rightHand;
   private final ModelPart leftHand;
   private final ModelPart rightThigh;
   private final ModelPart leftThigh;
   private final ModelPart rightShin;
   private final ModelPart leftShin;
   private final ModelPart loin;
   private final KeyframeAnimation claw;
   private final KeyframeAnimation leap;
   private final KeyframeAnimation charge;
   private final KeyframeAnimation rip;
   private final KeyframeAnimation roar;
   private final KeyframeAnimation transform;
   private final KeyframeAnimation collapse;

   public DeathBeastModel(ModelPart model) {
      super(model);
      this.root = model.getChild("root");
      this.pelvis = this.root.getChild("pelvis");
      this.loin = this.pelvis.getChild("loin_front");
      this.torso = this.pelvis.getChild("torso");
      this.cloak = this.torso.getChild("cloak");
      this.neck = this.torso.getChild("neck");
      this.head = this.neck.getChild("head");
      this.jaw = this.head.getChild("jaw");
      this.rightArm = this.torso.getChild("right_arm");
      this.leftArm = this.torso.getChild("left_arm");
      this.rightForearm = this.rightArm.getChild("right_forearm");
      this.leftForearm = this.leftArm.getChild("left_forearm");
      this.rightHand = this.rightForearm.getChild("right_hand");
      this.leftHand = this.leftForearm.getChild("left_hand");
      this.rightThigh = this.pelvis.getChild("right_thigh");
      this.leftThigh = this.pelvis.getChild("left_thigh");
      this.rightShin = this.rightThigh.getChild("right_shin");
      this.leftShin = this.leftThigh.getChild("left_shin");
      this.claw = CLAW.bake(model);
      this.leap = LEAP.bake(model);
      this.charge = CHARGE.bake(model);
      this.rip = RIP.bake(model);
      this.roar = ROAR.bake(model);
      this.transform = TRANSFORM.bake(model);
      this.collapse = COLLAPSE.bake(model);
   }

   public void setupAnim(DeathModel.State state) {
      super.setupAnim(state);
      float age = state.ageInTicks;
      float speed = Math.min(state.walkAnimationSpeed, 1.0F);
      float walk = state.walkAnimationPos * 0.45F;
      float breath = Mth.sin(age * 0.09F);
      this.torso.xRot += breath * 0.035F;
      this.pelvis.y += breath * 0.4F;
      this.jaw.xRot = this.jaw.xRot + (0.08F + Math.max(0.0F, breath) * 0.08F);
      this.neck.yRot = this.neck.yRot + state.yRot * 0.017453292F * 0.5F;
      this.head.yRot = this.head.yRot + state.yRot * 0.017453292F * 0.5F;
      this.head.xRot = this.head.xRot + state.xRot * 0.017453292F * 0.6F;
      this.rightArm.zRot = this.rightArm.zRot + (0.05F + Mth.sin(age * 0.06F) * 0.04F);
      this.leftArm.zRot = this.leftArm.zRot - (0.05F + Mth.sin(age * 0.06F + 1.0F) * 0.04F);
      this.cloak.xRot = this.cloak.xRot + (Mth.sin(age * 0.07F) * 0.05F - speed * 0.4F);
      this.loin.xRot = this.loin.xRot + (-speed * 0.6F + Mth.sin(age * 0.1F) * 0.05F);
      this.rightThigh.xRot = this.rightThigh.xRot + Mth.cos(walk) * 0.6F * speed;
      this.leftThigh.xRot = this.leftThigh.xRot + Mth.cos(walk + 3.1415927F) * 0.6F * speed;
      this.rightShin.xRot = this.rightShin.xRot + Math.max(0.0F, Mth.sin(walk)) * 0.6F * speed;
      this.leftShin.xRot = this.leftShin.xRot + Math.max(0.0F, -Mth.sin(walk)) * 0.6F * speed;
      this.rightArm.xRot = this.rightArm.xRot + Mth.cos(walk + 3.1415927F) * 0.5F * speed;
      this.leftArm.xRot = this.leftArm.xRot + Mth.cos(walk) * 0.5F * speed;
      this.torso.zRot = this.torso.zRot + Mth.sin(walk) * 0.1F * speed;
      this.torso.yRot = this.torso.yRot + Mth.sin(walk) * 0.12F * speed;
      this.pelvis.y = this.pelvis.y - Math.abs(Mth.sin(walk)) * 1.6F * speed;
      if (state.collapse > 0.0F) {
         this.collapse.apply((long)(state.collapse * 50.0F), 1.0F);
      } else {
         this.claw.apply(state.claw, age);
         this.leap.apply(state.leap, age);
         this.charge.apply(state.charge, age);
         this.rip.apply(state.rip, age);
         this.roar.apply(state.roar, age);
         this.transform.apply(state.transform, age);
      }
   }
}
