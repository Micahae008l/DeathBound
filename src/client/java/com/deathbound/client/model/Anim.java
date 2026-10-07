package com.deathbound.client.model;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.animation.AnimationChannel.Interpolation;
import net.minecraft.client.animation.AnimationChannel.Interpolations;
import net.minecraft.client.animation.AnimationChannel.Target;
import net.minecraft.client.animation.AnimationChannel.Targets;
import net.minecraft.client.animation.AnimationDefinition.Builder;

final class Anim {
   private final Builder builder;
   private Interpolation interp = Interpolations.CATMULLROM;

   private Anim(float seconds, boolean loop) {
      this.builder = Builder.withLength(seconds);
      if (loop) {
         this.builder.looping();
      }
   }

   static Anim of(float seconds) {
      return new Anim(seconds, false);
   }

   static Anim loop(float seconds) {
      return new Anim(seconds, true);
   }

   Anim linear() {
      this.interp = Interpolations.LINEAR;
      return this;
   }

   Anim smooth() {
      this.interp = Interpolations.CATMULLROM;
      return this;
   }

   Anim rot(String bone, float... k) {
      return this.channel(bone, Targets.ROTATION, k, 0);
   }

   Anim pos(String bone, float... k) {
      return this.channel(bone, Targets.POSITION, k, 1);
   }

   Anim scale(String bone, float... k) {
      return this.channel(bone, Targets.SCALE, k, 2);
   }

   private Anim channel(String bone, Target target, float[] k, int kind) {
      if (k.length % 4 != 0) {
         throw new IllegalArgumentException("keyframes for " + bone + " must be (t, x, y, z) quadruples");
      }

      Keyframe[] frames = new Keyframe[k.length / 4];

      for (int i = 0; i < frames.length; i++) {
         float t = k[i * 4];
         float x = k[i * 4 + 1];
         float y = k[i * 4 + 2];
         float z = k[i * 4 + 3];

         frames[i] = new Keyframe(t, switch (kind) {
            case 0 -> KeyframeAnimations.degreeVec(x, y, z);
            case 1 -> KeyframeAnimations.posVec(x, y, z);
            default -> KeyframeAnimations.scaleVec(x, y, z);
         }, this.interp);
      }

      this.builder.addAnimation(bone, new AnimationChannel(target, frames));
      return this;
   }

   AnimationDefinition build() {
      return this.builder.build();
   }
}
