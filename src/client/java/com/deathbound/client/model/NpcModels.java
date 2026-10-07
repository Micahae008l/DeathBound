package com.deathbound.client.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;

public final class NpcModels {
   private static float window(float p, float a, float b) {
      return !(p < a) && !(p > b) ? Mth.sin((p - a) / (b - a) * 3.1415927F) : 0.0F;
   }

   private static void look(ModelPart head, NpcModels.State s, float amount) {
      head.yRot = head.yRot + s.yRot * 0.017453292F * amount;
      head.xRot = head.xRot + s.xRot * 0.017453292F * amount;
   }

   private NpcModels() {
   }

   public static class Bonesmith extends EntityModel<NpcModels.State> {
      static final AnimationDefinition TALK = Anim.of(1.8F)
         .rot(
            "jaw",
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.15F,
            22.0F,
            0.0F,
            0.0F,
            0.3F,
            0.0F,
            0.0F,
            0.0F,
            0.45F,
            20.0F,
            0.0F,
            0.0F,
            0.6F,
            0.0F,
            0.0F,
            0.0F,
            0.9F,
            18.0F,
            0.0F,
            0.0F,
            1.05F,
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
            0.2F,
            12.0F,
            0.0F,
            0.0F,
            0.45F,
            -6.0F,
            0.0F,
            0.0F,
            0.7F,
            10.0F,
            0.0F,
            0.0F,
            1.0F,
            -4.0F,
            10.0F,
            0.0F,
            1.8F,
            0.0F,
            0.0F,
            0.0F
         )
         .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.4F, -70.0F, 0.0F, -15.0F, 1.2F, -65.0F, 0.0F, -12.0F, 1.8F, 0.0F, 0.0F, 0.0F)
         .rot("left_forearm", 0.0F, 0.0F, 0.0F, 0.0F, 0.4F, -20.0F, 0.0F, 0.0F, 1.2F, -25.0F, 0.0F, 0.0F, 1.8F, 0.0F, 0.0F, 0.0F)
         .build();
      static final AnimationDefinition IDLE = Anim.of(3.0F)
         .rot("body", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, 0.0F, 18.0F, 0.0F, 1.8F, 0.0F, 20.0F, 0.0F, 2.4F, 0.0F, -6.0F, 0.0F, 3.0F, 0.0F, 0.0F, 0.0F)
         .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, 4.0F, 10.0F, 6.0F, 1.8F, 6.0F, 12.0F, 4.0F, 3.0F, 0.0F, 0.0F, 0.0F)
         .build();
      static final AnimationDefinition TEMPER = Anim.of(3.2F)
         .rot(
            "right_arm",
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.5F,
            -110.0F,
            0.0F,
            10.0F,
            0.75F,
            40.0F,
            0.0F,
            6.0F,
            1.9F,
            42.0F,
            0.0F,
            6.0F,
            2.4F,
            -60.0F,
            0.0F,
            4.0F,
            3.2F,
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
            0.5F,
            -40.0F,
            0.0F,
            0.0F,
            0.75F,
            10.0F,
            0.0F,
            0.0F,
            1.9F,
            10.0F,
            0.0F,
            0.0F,
            2.4F,
            -50.0F,
            0.0F,
            0.0F,
            3.2F,
            0.0F,
            0.0F,
            0.0F
         )
         .rot(
            "body",
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.5F,
            -8.0F,
            0.0F,
            0.0F,
            0.75F,
            22.0F,
            0.0F,
            0.0F,
            1.9F,
            20.0F,
            0.0F,
            0.0F,
            2.4F,
            0.0F,
            0.0F,
            0.0F,
            3.2F,
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
            0.75F,
            20.0F,
            0.0F,
            0.0F,
            1.9F,
            22.0F,
            0.0F,
            0.0F,
            2.4F,
            -10.0F,
            -15.0F,
            10.0F,
            2.9F,
            -8.0F,
            12.0F,
            -8.0F,
            3.2F,
            0.0F,
            0.0F,
            0.0F
         )
         .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.75F, 20.0F, 0.0F, -20.0F, 1.9F, 20.0F, 0.0F, -20.0F, 3.2F, 0.0F, 0.0F, 0.0F)
         .build();
      private final ModelPart body;
      private final ModelPart head;
      private final ModelPart jaw;
      private final ModelPart rightArm;
      private final ModelPart rightForearm;
      private final KeyframeAnimation talk;
      private final KeyframeAnimation idle;
      private final KeyframeAnimation temper;

      public Bonesmith(ModelPart model) {
         super(model);
         ModelPart root = model.getChild("root");
         this.body = root.getChild("hips").getChild("body");
         this.head = this.body.getChild("head");
         this.jaw = this.head.getChild("jaw");
         this.rightArm = this.body.getChild("right_arm");
         this.rightForearm = this.rightArm.getChild("right_forearm");
         this.talk = TALK.bake(model);
         this.idle = IDLE.bake(model);
         this.temper = TEMPER.bake(model);
      }

      public void setupAnim(NpcModels.State s) {
         super.setupAnim(s);
         float t = s.ageInTicks + s.seed;
         NpcModels.look(this.head, s, 0.5F);
         this.body.zRot = this.body.zRot + Mth.sin(t * 0.05F) * 0.025F;
         this.jaw.xRot = this.jaw.xRot + (0.05F + Mth.sin(t * 0.07F) * 0.04F);
         float c = s.ageInTicks % 160.0F / 160.0F;
         float dip = NpcModels.window(c, 0.24F, 0.62F) * s.work;
         float look = NpcModels.window(c, 0.64F, 0.9F) * s.work;
         this.rightArm.xRot += 0.75F * dip - 0.9F * look;
         this.rightForearm.xRot += 0.3F * dip - 0.5F * look;
         this.body.xRot += 0.3F * dip;
         this.head.xRot += 0.35F * dip - 0.1F * look;
         this.head.zRot += 0.2F * look;
         this.idle.apply(s.idle, s.ageInTicks);
         this.talk.apply(s.talk, s.ageInTicks);
         this.temper.apply(s.temper, s.ageInTicks);
      }
   }

   public static class Collector extends EntityModel<NpcModels.State> {
      static final AnimationDefinition IDLE = Anim.of(3.0F)
         .rot("right_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, -55.0F, 25.0F, 0.0F, 2.2F, -58.0F, 22.0F, 0.0F, 3.0F, 0.0F, 0.0F, 0.0F)
         .rot("right_forearm", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, -60.0F, 0.0F, 0.0F, 2.2F, -62.0F, 0.0F, 0.0F, 3.0F, 0.0F, 0.0F, 0.0F)
         .rot(
            "head",
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.5F,
            12.0F,
            -18.0F,
            0.0F,
            1.4F,
            16.0F,
            -14.0F,
            6.0F,
            2.0F,
            10.0F,
            -18.0F,
            0.0F,
            2.4F,
            4.0F,
            0.0F,
            0.0F,
            3.0F,
            0.0F,
            0.0F,
            0.0F
         )
         .rot("body", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, 4.0F, 0.0F, 0.0F, 2.4F, 4.0F, 0.0F, 0.0F, 3.0F, 0.0F, 0.0F, 0.0F)
         .build();
      static final AnimationDefinition TALK = Anim.of(2.0F)
         .rot("body", 0.0F, 0.0F, 0.0F, 0.0F, 0.25F, 6.0F, 0.0F, 0.0F, 1.6F, 5.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F)
         .rot(
            "head",
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.2F,
            -10.0F,
            0.0F,
            0.0F,
            0.45F,
            4.0F,
            8.0F,
            0.0F,
            0.7F,
            -8.0F,
            -6.0F,
            0.0F,
            1.0F,
            6.0F,
            0.0F,
            0.0F,
            2.0F,
            0.0F,
            0.0F,
            0.0F
         )
         .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, -40.0F, 0.0F, -20.0F, 0.8F, -30.0F, 0.0F, -10.0F, 1.3F, -45.0F, 0.0F, -25.0F, 2.0F, 0.0F, 0.0F, 0.0F)
         .rot("left_forearm", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, -30.0F, 0.0F, 0.0F, 0.8F, -50.0F, 0.0F, 0.0F, 1.3F, -25.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F)
         .rot("right_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, -20.0F, 0.0F, 15.0F, 1.5F, -15.0F, 0.0F, 10.0F, 2.0F, 0.0F, 0.0F, 0.0F)
         .build();
      private final ModelPart root;
      private final ModelPart body;
      private final ModelPart head;
      private final ModelPart pack;
      private final ModelPart lantern;
      private final ModelPart coatTail;
      private final KeyframeAnimation talk;
      private final KeyframeAnimation idle;

      public Collector(ModelPart model) {
         super(model);
         this.root = model.getChild("root");
         this.body = this.root.getChild("body");
         this.head = this.body.getChild("head");
         this.pack = this.body.getChild("pack");
         this.lantern = this.pack.getChild("pack_lantern");
         this.coatTail = this.body.getChild("coat_tail");
         this.talk = TALK.bake(model);
         this.idle = IDLE.bake(model);
      }

      public void setupAnim(NpcModels.State s) {
         super.setupAnim(s);
         float t = s.ageInTicks + s.seed;
         NpcModels.look(this.head, s, 0.7F);
         this.body.xRot = this.body.xRot + Mth.sin(t * 0.11F) * 0.025F;
         this.head.xRot = this.head.xRot - Mth.sin(t * 0.11F) * 0.02F;
         this.pack.zRot = this.pack.zRot + Mth.sin(t * 0.05F) * 0.04F;
         this.pack.xRot = this.pack.xRot + Mth.sin(t * 0.11F + 0.4F) * 0.02F;
         this.lantern.zRot = this.lantern.zRot + Mth.sin(t * 0.09F) * 0.22F;
         this.lantern.xRot = this.lantern.xRot + Mth.sin(t * 0.067F + 1.1F) * 0.14F;
         this.coatTail.xRot = this.coatTail.xRot + Mth.sin(t * 0.05F) * 0.04F;
         this.root.zRot = this.root.zRot + Mth.sin(t * 0.023F) * 0.02F;
         float e = NpcModels.window(s.ageInTicks % 140.0F / 140.0F, 0.12F, 0.78F) * s.work;
         ModelPart arm = this.body.getChild("right_arm");
         arm.xRot -= 1.0F * e;
         arm.getChild("right_forearm").xRot -= 0.6F * e;
         ModelPart var5 = arm.getChild("right_forearm");
         var5.zRot = var5.zRot + Mth.sin(s.ageInTicks * 0.15F) * 0.2F * e;
         this.head.xRot += 0.3F * e;
         this.head.zRot += 0.15F * e;
         this.idle.apply(s.idle, s.ageInTicks);
         this.talk.apply(s.talk, s.ageInTicks);
      }
   }

   public static class Ferryman extends EntityModel<NpcModels.State> {
      static final AnimationDefinition TALK = Anim.of(1.8F)
         .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.35F, -50.0F, 0.0F, -18.0F, 1.2F, -46.0F, 0.0F, -14.0F, 1.8F, 0.0F, 0.0F, 0.0F)
         .rot("left_forearm", 0.0F, 0.0F, 0.0F, 0.0F, 0.35F, -40.0F, 0.0F, 0.0F, 0.8F, -30.0F, 10.0F, 0.0F, 1.2F, -38.0F, 0.0F, 0.0F, 1.8F, 0.0F, 0.0F, 0.0F)
         .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, 8.0F, 0.0F, 0.0F, 0.6F, -5.0F, 0.0F, 0.0F, 0.95F, 7.0F, 0.0F, 0.0F, 1.8F, 0.0F, 0.0F, 0.0F)
         .rot("chest", 0.0F, 0.0F, 0.0F, 0.0F, 0.4F, -4.0F, 0.0F, 0.0F, 1.8F, 0.0F, 0.0F, 0.0F)
         .build();
      static final AnimationDefinition IDLE = Anim.of(3.4F)
         .rot("right_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -14.0F, 0.0F, 0.0F, 2.6F, -14.0F, 0.0F, 0.0F, 3.4F, 0.0F, 0.0F, 0.0F)
         .rot(
            "head",
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.7F,
            -6.0F,
            32.0F,
            0.0F,
            1.6F,
            -6.0F,
            30.0F,
            0.0F,
            2.2F,
            -4.0F,
            -28.0F,
            0.0F,
            2.9F,
            -4.0F,
            -26.0F,
            0.0F,
            3.4F,
            0.0F,
            0.0F,
            0.0F
         )
         .rot("chest", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -4.0F, 0.0F, 0.0F, 2.6F, -4.0F, 0.0F, 0.0F, 3.4F, 0.0F, 0.0F, 0.0F)
         .build();
      private final ModelPart chest;
      private final ModelPart head;
      private final ModelPart hoodTip;
      private final ModelPart beard;
      private final ModelPart cloak;
      private final ModelPart skirt;
      private final ModelPart lantern;
      private final ModelPart pole;
      private final KeyframeAnimation talk;
      private final KeyframeAnimation idle;

      public Ferryman(ModelPart model) {
         super(model);
         ModelPart root = model.getChild("root");
         ModelPart hips = root.getChild("hips");
         this.chest = hips.getChild("chest");
         this.head = this.chest.getChild("head");
         this.hoodTip = this.head.getChild("hood_tip");
         this.beard = this.head.getChild("beard");
         this.cloak = hips.getChild("cloak");
         this.skirt = hips.getChild("skirt");
         this.pole = this.chest.getChild("right_arm").getChild("right_forearm").getChild("right_hand").getChild("pole");
         this.lantern = this.pole.getChild("lantern");
         this.talk = TALK.bake(model);
         this.idle = IDLE.bake(model);
      }

      public void setupAnim(NpcModels.State s) {
         super.setupAnim(s);
         float t = s.ageInTicks + s.seed;
         NpcModels.look(this.head, s, 0.8F);
         this.chest.xRot = this.chest.xRot + Mth.sin(t * 0.05F) * 0.025F;
         this.head.xRot = this.head.xRot - Mth.sin(t * 0.05F) * 0.02F;
         this.hoodTip.xRot = this.hoodTip.xRot + Mth.sin(t * 0.05F + 0.6F) * 0.08F;
         this.beard.xRot = this.beard.xRot + Mth.sin(t * 0.06F) * 0.05F;
         this.beard.zRot = this.beard.zRot + Mth.sin(t * 0.045F) * 0.04F;
         this.cloak.xRot = this.cloak.xRot + (0.04F + Mth.sin(t * 0.04F) * 0.06F);
         this.cloak.zRot = this.cloak.zRot + Mth.sin(t * 0.033F) * 0.03F;
         this.skirt.xRot = this.skirt.xRot + Mth.sin(t * 0.045F + 1.0F) * 0.02F;
         this.lantern.zRot = this.lantern.zRot + Mth.sin(t * 0.07F) * 0.16F;
         this.lantern.xRot = this.lantern.xRot + Mth.sin(t * 0.053F + 1.3F) * 0.1F;
         this.pole.zRot = this.pole.zRot + Mth.sin(t * 0.03F) * 0.012F;
         float push = Mth.sin(t * 6.2831855F / 90.0F) * s.work;
         this.chest.getChild("right_arm").xRot += push * 0.35F;
         this.chest.xRot = this.chest.xRot + Math.max(0.0F, push) * 0.16F;
         this.head.yRot = this.head.yRot + 0.35F * s.work;
         this.idle.apply(s.idle, s.ageInTicks);
         this.talk.apply(s.talk, s.ageInTicks);
      }
   }

   public static class Gravedigger extends EntityModel<NpcModels.State> {
      static final AnimationDefinition TALK = Anim.of(2.0F)
         .rot(
            "head",
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.25F,
            0.0F,
            -12.0F,
            0.0F,
            0.5F,
            0.0F,
            10.0F,
            0.0F,
            0.75F,
            0.0F,
            -8.0F,
            0.0F,
            1.0F,
            6.0F,
            0.0F,
            0.0F,
            2.0F,
            0.0F,
            0.0F,
            0.0F
         )
         .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, -30.0F, 0.0F, -10.0F, 1.3F, -28.0F, 0.0F, -6.0F, 2.0F, 0.0F, 0.0F, 0.0F)
         .rot("left_forearm", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, -30.0F, -20.0F, 0.0F, 0.7F, -15.0F, 15.0F, 0.0F, 1.3F, -25.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F)
         .rot("body", 0.0F, 0.0F, 0.0F, 0.0F, 0.3F, -5.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.0F, 0.0F)
         .build();
      static final AnimationDefinition IDLE = Anim.of(2.8F)
         .rot(
            "body",
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.4F,
            18.0F,
            0.0F,
            0.0F,
            0.7F,
            24.0F,
            0.0F,
            0.0F,
            1.1F,
            18.0F,
            0.0F,
            0.0F,
            1.4F,
            24.0F,
            0.0F,
            0.0F,
            2.0F,
            14.0F,
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
            0.4F,
            -20.0F,
            0.0F,
            0.0F,
            0.7F,
            10.0F,
            0.0F,
            0.0F,
            1.1F,
            -20.0F,
            0.0F,
            0.0F,
            1.4F,
            10.0F,
            0.0F,
            0.0F,
            2.0F,
            -10.0F,
            0.0F,
            0.0F,
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
            0.4F,
            -15.0F,
            0.0F,
            0.0F,
            0.7F,
            12.0F,
            0.0F,
            0.0F,
            1.1F,
            -15.0F,
            0.0F,
            0.0F,
            1.4F,
            12.0F,
            0.0F,
            0.0F,
            2.0F,
            -8.0F,
            0.0F,
            0.0F,
            2.8F,
            0.0F,
            0.0F,
            0.0F
         )
         .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.4F, 10.0F, 0.0F, 0.0F, 2.0F, 10.0F, 0.0F, 0.0F, 2.4F, -6.0F, 0.0F, 0.0F, 2.8F, 0.0F, 0.0F, 0.0F)
         .build();
      private final ModelPart root;
      private final ModelPart body;
      private final ModelPart head;
      private final ModelPart hat;
      private final ModelPart coatTail;
      private final ModelPart lantern;
      private final KeyframeAnimation talk;
      private final KeyframeAnimation idle;

      public Gravedigger(ModelPart model) {
         super(model);
         this.root = model.getChild("root");
         this.body = this.root.getChild("body");
         this.head = this.body.getChild("head");
         this.hat = this.head.getChild("hat");
         this.coatTail = this.body.getChild("coat_tail");
         this.lantern = this.body.getChild("lantern");
         this.talk = TALK.bake(model);
         this.idle = IDLE.bake(model);
      }

      public void setupAnim(NpcModels.State s) {
         super.setupAnim(s);
         float t = s.ageInTicks + s.seed;
         NpcModels.look(this.head, s, 1.0F);
         this.body.xRot = this.body.xRot + Mth.sin(t * 0.09F) * 0.02F;
         this.root.zRot = this.root.zRot + Mth.sin(t * 0.021F) * 0.015F;
         this.head.zRot = this.head.zRot + Mth.sin(t * 1.3F) * 0.004F;
         this.hat.xRot = this.hat.xRot + Mth.sin(t * 0.09F + 0.5F) * 0.01F;
         this.coatTail.xRot = this.coatTail.xRot + (0.05F + Mth.sin(t * 0.04F) * 0.03F);
         this.lantern.zRot = this.lantern.zRot + Mth.sin(t * 0.08F) * 0.12F;
         this.lantern.xRot = this.lantern.xRot + Mth.sin(t * 0.06F + 0.8F) * 0.08F;
         float p = s.ageInTicks % 26.0F / 26.0F;
         float lift = p < 0.62F ? Mth.sin(p / 0.62F * 1.5707964F) : (p < 0.72F ? Mth.cos((p - 0.62F) / 0.1F * 1.5707964F) : 0.0F);
         ModelPart arm = this.body.getChild("right_arm");
         arm.xRot = arm.xRot - 1.9F * lift * s.work;
         ModelPart var10000 = arm.getChild("right_forearm");
         var10000.xRot = var10000.xRot + 0.5F * lift * s.work;
         this.body.xRot = this.body.xRot + (0.18F - 0.08F * lift) * s.work;
         this.head.xRot = this.head.xRot + 0.35F * s.work;
         this.idle.apply(s.idle, s.ageInTicks);
         this.talk.apply(s.talk, s.ageInTicks);
      }
   }

   public static class Lamplighter extends EntityModel<NpcModels.State> {
      static final AnimationDefinition IDLE = Anim.of(3.0F)
         .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -60.0F, 0.0F, -10.0F, 2.2F, -62.0F, 0.0F, -12.0F, 3.0F, 0.0F, 0.0F, 0.0F)
         .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.6F, -15.0F, 20.0F, 0.0F, 2.2F, -15.0F, -15.0F, 0.0F, 3.0F, 0.0F, 0.0F, 0.0F)
         .build();
      private final ModelPart body;
      private final ModelPart head;
      private final ModelPart lamp;
      private final ModelPart coat;
      private final KeyframeAnimation idle;

      public Lamplighter(ModelPart model) {
         super(model, RenderTypes::entityTranslucent);
         this.body = model.getChild("root").getChild("body");
         this.head = this.body.getChild("head");
         this.coat = this.body.getChild("coat");
         this.lamp = this.body.getChild("left_arm").getChild("pole").getChild("lamp");
         this.idle = IDLE.bake(model);
      }

      public void setupAnim(NpcModels.State s) {
         super.setupAnim(s);
         float t = s.ageInTicks + s.seed * 7;
         NpcModels.look(this.head, s, 0.6F);
         this.lamp.zRot = this.lamp.zRot + Mth.sin(t * 0.07F) * 0.3F;
         this.lamp.xRot = this.lamp.xRot + Mth.sin(t * 0.05F + 0.8F) * 0.2F;
         this.coat.xRot = this.coat.xRot + Mth.sin(t * 0.04F) * 0.04F;
         this.body.zRot = this.body.zRot + Mth.sin(t * 0.03F) * 0.03F;
         this.idle.apply(s.idle, s.ageInTicks);
      }
   }

   public static class Mira extends EntityModel<NpcModels.State> {
      private final ModelPart root;
      private final ModelPart body;
      private final ModelPart head;
      private final ModelPart braid;
      private final ModelPart rightArm;
      private final ModelPart leftArm;

      public Mira(ModelPart model) {
         super(model, RenderTypes::entityTranslucent);
         this.root = model.getChild("root");
         this.body = this.root.getChild("skirt").getChild("body");
         this.head = this.body.getChild("head");
         this.braid = this.head.getChild("braid");
         this.rightArm = this.body.getChild("right_arm");
         this.leftArm = this.body.getChild("left_arm");
      }

      public void setupAnim(NpcModels.State s) {
         super.setupAnim(s);
         float t = s.ageInTicks + s.seed * 13;
         NpcModels.look(this.head, s, 0.8F);
         this.root.y = this.root.y + Mth.sin(t * 0.08F) * 0.8F;
         this.body.zRot = this.body.zRot + Mth.sin(t * 0.05F) * 0.06F;
         this.head.zRot = this.head.zRot + Mth.sin(t * 0.04F) * 0.1F;
         this.braid.xRot = this.braid.xRot + (0.15F + Mth.sin(t * 0.08F) * 0.08F);
         float count = NpcModels.window(t % 120.0F / 120.0F, 0.1F, 0.6F) * s.work;
         this.rightArm.xRot -= 1.1F * count;
         this.rightArm.zRot = this.rightArm.zRot + Mth.sin(t * 0.5F) * 0.15F * count;
         this.leftArm.xRot -= 0.6F * count;
         this.head.xRot += 0.3F * count;
         this.rightArm.xRot = this.rightArm.xRot + (1.0F - Math.min(1.0F, s.work)) * -0.4F * Mth.sin(t * 0.2F);
      }
   }

   public static class Prophet extends EntityModel<NpcModels.State> {
      static final AnimationDefinition TALK = Anim.of(2.2F)
         .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.12F, -38.0F, 0.0F, 0.0F, 0.5F, -30.0F, 0.0F, 6.0F, 1.4F, -25.0F, 0.0F, -4.0F, 2.2F, 0.0F, 0.0F, 0.0F)
         .rot("body", 0.0F, 0.0F, 0.0F, 0.0F, 0.12F, -12.0F, 0.0F, 0.0F, 1.4F, -8.0F, 0.0F, 0.0F, 2.2F, 0.0F, 0.0F, 0.0F)
         .rot("right_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.15F, 0.0F, 0.0F, -10.0F, 0.3F, 0.0F, 0.0F, 4.0F, 0.45F, 0.0F, 0.0F, -6.0F, 2.2F, 0.0F, 0.0F, 0.0F)
         .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.15F, 0.0F, 0.0F, 10.0F, 0.3F, 0.0F, 0.0F, -4.0F, 0.45F, 0.0F, 0.0F, 6.0F, 2.2F, 0.0F, 0.0F, 0.0F)
         .build();
      static final AnimationDefinition IDLE = Anim.of(3.0F)
         .rot("head", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, -28.0F, 18.0F, 14.0F, 1.8F, -26.0F, 20.0F, 12.0F, 2.4F, -10.0F, -8.0F, 0.0F, 3.0F, 0.0F, 0.0F, 0.0F)
         .rot("right_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, 0.0F, 0.0F, -6.0F, 1.8F, 0.0F, 0.0F, -5.0F, 3.0F, 0.0F, 0.0F, 0.0F)
         .rot("left_arm", 0.0F, 0.0F, 0.0F, 0.0F, 0.5F, 0.0F, 0.0F, 6.0F, 1.8F, 0.0F, 0.0F, 5.0F, 3.0F, 0.0F, 0.0F, 0.0F)
         .build();
      private final ModelPart root;
      private final ModelPart body;
      private final ModelPart head;
      private final ModelPart hair;
      private final ModelPart rightArm;
      private final ModelPart leftArm;
      private final ModelPart rightChain;
      private final ModelPart leftChain;
      private final ModelPart[] regalia;
      private final KeyframeAnimation talk;
      private final KeyframeAnimation idle;

      public Prophet(ModelPart model) {
         super(model);
         this.root = model.getChild("root");
         ModelPart hips = this.root.getChild("hips");
         this.body = hips.getChild("body");
         this.head = this.body.getChild("head");
         this.hair = this.head.getChild("hair");
         this.rightArm = this.body.getChild("right_arm");
         this.leftArm = this.body.getChild("left_arm");
         this.rightChain = this.rightArm.getChild("right_forearm").getChild("right_chain");
         this.leftChain = this.leftArm.getChild("left_forearm").getChild("left_chain");
         List<ModelPart> r = new ArrayList<>(
            List.of(this.head.getChild("crown"), this.body.getChild("mantle"), this.body.getChild("robe"), hips.getChild("robe_hips"))
         );

         for (String side : new String[]{"right", "left"}) {
            ModelPart arm = this.body.getChild(side + "_arm");
            ModelPart thigh = hips.getChild(side + "_thigh");
            r.add(arm.getChild(side + "_sleeve"));
            r.add(arm.getChild(side + "_forearm").getChild(side + "_cuff"));
            r.add(thigh.getChild(side + "_thigh_robe"));
            r.add(thigh.getChild(side + "_shin").getChild(side + "_shin_robe"));
         }

         this.regalia = r.toArray(ModelPart[]::new);
         this.talk = TALK.bake(model);
         this.idle = IDLE.bake(model);
      }

      public void setupAnim(NpcModels.State s) {
         super.setupAnim(s);
         float t = s.ageInTicks + s.seed * 31;
         NpcModels.look(this.head, s, 0.5F);
         this.rightChain.visible = !s.freed;
         this.leftChain.visible = !s.freed;

         for (ModelPart p : this.regalia) {
            p.visible = s.freed;
         }

         if (s.freed) {
            this.rightArm.zRot -= 2.25F;
            this.leftArm.zRot += 2.25F;
            this.rightArm.xRot -= 0.55F;
            this.leftArm.xRot -= 0.55F;
            this.body.xRot -= 0.2F;
            this.head.xRot = this.head.xRot - (0.35F + Mth.sin(t * 0.02F) * 0.04F);
            float c = s.corruption;
            this.head.yRot = this.head.yRot + Mth.sin(t * 0.021F) * 0.35F * c;
            this.head.zRot = this.head.zRot + Mth.sin(t * 0.013F + 0.6F) * 0.08F * c;
            this.rightArm.xRot -= c * 0.15F;
            this.leftArm.xRot -= c * 0.15F;
            this.idle.apply(s.idle, s.ageInTicks);
            this.talk.apply(s.talk, s.ageInTicks);
         } else {
            this.root.y = this.root.y + Mth.sin(t * 0.045F) * 0.9F;
            this.body.zRot = this.body.zRot + Mth.sin(t * 0.03F) * 0.05F;
            this.head.zRot = this.head.zRot + Mth.sin(t * 0.037F) * 0.12F;
            this.hair.xRot = this.hair.xRot + Mth.sin(t * 0.05F) * 0.05F;
            this.rightArm.zRot = this.rightArm.zRot + Mth.sin(t * 0.03F + 0.4F) * 0.04F;
            this.leftArm.zRot = this.leftArm.zRot - Mth.sin(t * 0.03F + 1.1F) * 0.04F;
            this.rightChain.zRot = this.rightChain.zRot + Mth.sin(t * 0.03F) * 0.03F;
            this.leftChain.zRot = this.leftChain.zRot - Mth.sin(t * 0.03F + 0.7F) * 0.03F;
            float phase = t % 190.0F / 14.0F;
            if (phase < 1.0F) {
               float c = Mth.sin(phase * 3.1415927F) * (phase < 0.5F ? 1.0F : Mth.sin(phase * 40.0F) * 0.5F + 0.5F);
               this.body.xRot -= c * 0.25F;
               this.head.xRot -= c * 0.55F;
               this.rightArm.zRot -= c * 0.12F;
               this.leftArm.zRot += c * 0.12F;
            }

            this.body.zRot = this.body.zRot + Mth.sin(t * 0.025F) * 0.07F * s.work;
            this.head.yRot = this.head.yRot + Mth.sin(t * 0.013F + 1.0F) * 0.5F * s.work;
            this.idle.apply(s.idle, s.ageInTicks);
            this.talk.apply(s.talk, s.ageInTicks);
         }
      }
   }

   public static class Sentry extends EntityModel<NpcModels.State> {
      private final ModelPart root;
      private final ModelPart body;
      private final ModelPart head;
      private final ModelPart tail;

      public Sentry(ModelPart model) {
         super(model, RenderTypes::entityTranslucent);
         this.root = model.getChild("root");
         this.body = this.root.getChild("body");
         this.head = this.body.getChild("head");
         this.tail = this.body.getChild("tail");
      }

      public void setupAnim(NpcModels.State s) {
         super.setupAnim(s);
         float t = s.ageInTicks + s.seed * 5;
         NpcModels.look(this.head, s, 0.4F);
         this.root.y = this.root.y + Mth.sin(t * 0.06F) * 0.6F;
         this.tail.xRot = this.tail.xRot + Mth.sin(t * 0.09F) * 0.12F;
         this.tail.zRot = this.tail.zRot + Mth.sin(t * 0.07F) * 0.08F;
         float stare = NpcModels.window(t % 220.0F / 220.0F, 0.5F, 0.85F) * s.work;
         this.head.yRot += 0.6F * stare;
         this.head.xRot -= 0.1F * stare;
      }
   }

   public static class State extends LivingEntityRenderState {
      public final AnimationState talk = new AnimationState();
      public final AnimationState idle = new AnimationState();
      public final AnimationState temper = new AnimationState();
      public int seed;
      public float work;
      public boolean freed;
      public float corruption;
   }
}
