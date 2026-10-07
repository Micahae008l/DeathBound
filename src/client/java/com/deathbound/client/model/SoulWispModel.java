package com.deathbound.client.model;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;

public class SoulWispModel extends EntityModel<SoulWispModel.State> {
   static final AnimationDefinition SPIT = Anim.of(0.55F)
      .rot("jaw", 0.0F, 0.0F, 0.0F, 0.0F, 0.06F, 55.0F, 0.0F, 0.0F, 0.25F, 50.0F, 0.0F, 0.0F, 0.55F, 0.0F, 0.0F, 0.0F)
      .rot("skull", 0.0F, 0.0F, 0.0F, 0.0F, 0.08F, -20.0F, 0.0F, 0.0F, 0.3F, 5.0F, 0.0F, 0.0F, 0.55F, 0.0F, 0.0F, 0.0F)
      .pos("skull", 0.0F, 0.0F, 0.0F, 0.0F, 0.08F, 0.0F, 0.5F, 2.5F, 0.55F, 0.0F, 0.0F, 0.0F)
      .scale("flame_a", 0.0F, 1.0F, 1.0F, 1.0F, 0.1F, 1.25F, 1.4F, 1.25F, 0.55F, 1.0F, 1.0F, 1.0F)
      .scale("flame_b", 0.0F, 1.0F, 1.0F, 1.0F, 0.12F, 1.25F, 1.4F, 1.25F, 0.55F, 1.0F, 1.0F, 1.0F)
      .scale("crown", 0.0F, 1.0F, 1.0F, 1.0F, 0.1F, 1.3F, 1.6F, 1.3F, 0.55F, 1.0F, 1.0F, 1.0F)
      .build();
   private final ModelPart root;
   private final ModelPart skull;
   private final ModelPart jaw;
   private final ModelPart flameA;
   private final ModelPart flameB;
   private final ModelPart crown;
   private final ModelPart tail;
   private final ModelPart[] motes = new ModelPart[3];
   private final KeyframeAnimation spit;

   public SoulWispModel(ModelPart model) {
      super(model);
      this.root = model.getChild("root");
      this.skull = this.root.getChild("skull");
      this.jaw = this.skull.getChild("jaw");
      this.flameA = this.skull.getChild("flame_a");
      this.flameB = this.skull.getChild("flame_b");
      this.crown = this.skull.getChild("crown");
      this.tail = this.skull.getChild("tail");

      for (int i = 0; i < 3; i++) {
         this.motes[i] = this.skull.getChild("mote" + i);
      }

      this.spit = SPIT.bake(model);
   }

   public void setupAnim(SoulWispModel.State state) {
      super.setupAnim(state);
      float age = state.ageInTicks;
      this.root.y = this.root.y - (Mth.sin(age * 0.12F) * 1.4F + 1.0F);
      this.skull.yRot = this.skull.yRot + state.yRot * 0.017453292F;
      this.skull.xRot = this.skull.xRot + (state.xRot * 0.017453292F * 0.7F + Mth.sin(age * 0.09F) * 0.06F);
      this.skull.zRot = this.skull.zRot + Mth.sin(age * 0.07F) * 0.08F;
      this.jaw.xRot = this.jaw.xRot + (0.08F + Mth.sin(age * 0.45F) * 0.06F);
      this.flameA.yScale = 1.0F + Mth.sin(age * 0.7F) * 0.1F;
      this.flameA.xScale = 1.0F + Mth.sin(age * 0.5F + 1.0F) * 0.06F;
      this.flameB.yScale = 1.0F + Mth.sin(age * 0.62F + 2.0F) * 0.1F;
      this.flameB.xScale = 1.0F + Mth.sin(age * 0.47F + 3.0F) * 0.06F;
      this.crown.yScale = 1.0F + Mth.sin(age * 0.9F) * 0.18F;
      this.flameA.zRot = this.flameA.zRot + Mth.sin(age * 0.3F) * 0.07F;
      this.flameB.zRot = this.flameB.zRot + Mth.cos(age * 0.33F) * 0.07F;
      this.tail.yRot = this.tail.yRot + Mth.sin(age * 0.2F) * 0.4F;
      this.tail.xRot = this.tail.xRot + (Mth.sin(age * 0.15F) * 0.12F + Math.min(state.walkAnimationSpeed, 1.0F) * 0.4F);

      for (int i = 0; i < 3; i++) {
         float a = age * 0.08F + i * 2.094F;
         this.motes[i].x = Mth.cos(a) * 7.0F;
         this.motes[i].z = Mth.sin(a) * 7.0F;
         this.motes[i].y = -3.0F + Mth.sin(age * 0.11F + i * 1.7F) * 3.0F;
      }

      this.spit.apply(state.spit, age);
   }

   public static class State extends LivingEntityRenderState {
      public final AnimationState spit = new AnimationState();
      public int variant;
   }
}
