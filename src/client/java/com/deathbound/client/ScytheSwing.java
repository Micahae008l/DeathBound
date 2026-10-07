package com.deathbound.client;

import com.deathbound.item.ScytheCombo;
import com.deathbound.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.util.Ease;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity.SwingDescription;
import net.minecraft.world.item.ItemStack;

public final class ScytheSwing {
   private static float span(float t, float a, float b) {
      return Mth.clamp((t - a) / (b - a), 0.0F, 1.0F);
   }

   private static float[] curve(float p) {
      float wind = Ease.outCubic(span(p, 0.0F, 0.22F));
      float strike = Ease.inOutCubic(span(p, 0.2F, 0.5F));
      float settle = Ease.inOutSine(span(p, 0.58F, 1.0F));
      return new float[]{wind * (1.0F - strike), strike * (1.0F - settle)};
   }

   private static int combo(Entity entity) {
      return entity instanceof ScytheCombo c ? c.deathbound$comboStep() : 0;
   }

   public static void firstPerson(PlayerRenderState playerState, InteractionHand hand, float attack, ItemStack stack, PoseStack pose) {
      AvatarRenderState a = playerState.avatarRenderState;
      if (a != null && stack.is(ModItems.REAPER_SCYTHE) && (!a.isUsingItem || a.useItemHand != hand)) {
         SwingDescription swing = a.currentSwing;
         if (swing != null && swing.hand() == hand && !(attack <= 0.0F)) {
            HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? a.mainArm : a.mainArm.getOpposite();
            int inv = arm == HumanoidArm.RIGHT ? 1 : -1;
            float[] c = curve(attack);
            float pre = c[0];
            float cut = c[1];
            float yaw = 0.0F;
            float pitch = 0.0F;
            float roll = 0.0F;
            float tx = 0.0F;
            float ty = 0.0F;
            float tz = 0.0F;
            switch (combo(Minecraft.getInstance().player)) {
               case 0:
                  yaw = inv * (55.0F * pre - 130.0F * cut);
                  roll = inv * (-20.0F * pre - 35.0F * cut);
                  pitch = -12.0F * pre + 18.0F * cut;
                  tx = inv * (0.25F * pre - 0.62F * cut);
                  ty = 0.08F * pre - 0.08F * cut;
                  tz = 0.05F * pre - 0.22F * cut;
                  break;
               case 1:
                  yaw = inv * (-70.0F * pre + 120.0F * cut);
                  roll = inv * (30.0F * pre + 35.0F * cut);
                  pitch = -18.0F * pre + 14.0F * cut;
                  tx = inv * (-0.38F * pre + 0.55F * cut);
                  ty = 0.12F * pre - 0.05F * cut;
                  tz = 0.05F * pre - 0.22F * cut;
                  break;
               default:
                  float spin = Ease.inOutCubic(span(attack, 0.1F, 0.5F));
                  float slam = Ease.inCubic(span(attack, 0.42F, 0.56F)) * (1.0F - Ease.inOutSine(span(attack, 0.68F, 1.0F)));
                  yaw = inv * -360 * spin;
                  pitch = -35.0F * pre * (1.0F - slam) + 75.0F * slam;
                  roll = inv * -12 * slam;
                  ty = 0.22F * pre * (1.0F - slam) - 0.28F * slam;
                  tz = -0.32F * slam;
                  tx = inv * -0.12F * slam;
            }

            pose.translate(tx, ty, tz);
            pose.rotateDegrees(Axis.YP, yaw);
            pose.rotateDegrees(Axis.XP, pitch);
            pose.rotateDegrees(Axis.ZP, roll);
         }
      }
   }

   public static void thirdPerson(HumanoidModel<?> model, HumanoidRenderState state) {
      SwingDescription swing = state.currentSwing;
      if (swing != null && !(state.swingAnimation <= 0.0F) && state instanceof AvatarRenderState avatar) {
         HumanoidArm armSide = swing.hand().asArm(state.mainArm);
         ItemStack held = armSide == HumanoidArm.RIGHT ? state.rightHandItemStack : state.leftHandItemStack;
         if (held.is(ModItems.REAPER_SCYTHE) && Minecraft.getInstance().level != null) {
            int combo = combo(Minecraft.getInstance().level.getEntity(avatar.id));
            ModelPart main = armSide == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
            ModelPart off = armSide == HumanoidArm.RIGHT ? model.leftArm : model.rightArm;
            ModelPart lead = armSide == HumanoidArm.RIGHT ? model.leftLeg : model.rightLeg;
            ModelPart trail = armSide == HumanoidArm.RIGHT ? model.rightLeg : model.leftLeg;
            int inv = armSide == HumanoidArm.RIGHT ? 1 : -1;
            float[] c = curve(state.swingAnimation);
            float pre = c[0];
            float cut = c[1];
            float offZ = 0.0F;
            float yaw;
            float pitch;
            float mainX;
            float mainY;
            float mainZ;
            float offX;
            float offY;
            float step;
            switch (combo) {
               case 0:
                  yaw = inv * (0.65F * pre - 0.85F * cut);
                  pitch = 0.16F * cut;
                  mainX = -1.0F * pre - 1.35F * cut;
                  mainY = inv * (0.55F * pre - 0.65F * cut);
                  mainZ = inv * 0.25F * pre;
                  offX = -1.15F * pre - 1.25F * cut;
                  offY = inv * (0.9F * pre - 0.35F * cut);
                  step = 0.35F * cut;
                  break;
               case 1:
                  yaw = inv * (-0.7F * pre + 0.75F * cut);
                  pitch = 0.14F * cut;
                  mainX = -1.25F * pre - 1.3F * cut;
                  mainY = inv * (-0.8F * pre + 0.5F * cut);
                  mainZ = -inv * 0.2F * pre;
                  offX = -1.0F * pre - 1.2F * cut;
                  offY = inv * (-0.4F * pre + 0.85F * cut);
                  step = 0.3F * cut;
                  break;
               default:
                  yaw = inv * 0.15F * cut;
                  pitch = -0.22F * pre + 0.28F * cut;
                  mainX = -2.95F * pre - 0.75F * cut;
                  mainY = inv * 0.1F * pre;
                  mainZ = inv * 0.15F * pre;
                  offX = -2.85F * pre - 0.7F * cut;
                  offY = -inv * 0.1F * pre;
                  offZ = -inv * 0.15F * pre;
                  step = 0.6F * cut;
            }

            model.body.yRot = yaw;
            model.body.xRot = pitch;
            float age = state.ageScale;
            float sin = Mth.sin(yaw);
            float cos = Mth.cos(yaw);
            model.rightArm.z = sin * 5.0F * age;
            model.rightArm.x = -cos * 5.0F * age;
            model.leftArm.z = -sin * 5.0F * age;
            model.leftArm.x = cos * 5.0F * age;
            model.rightLeg.z = model.rightLeg.z + Mth.sin(pitch) * 12.0F * age;
            model.leftLeg.z = model.leftLeg.z + Mth.sin(pitch) * 12.0F * age;
            main.xRot = mainX;
            main.yRot = mainY + yaw;
            main.zRot = mainZ;
            off.xRot = offX;
            off.yRot = offY + yaw;
            off.zRot = offZ;
            lead.xRot -= step * 0.8F;
            trail.xRot += step * 0.5F;
            model.head.yRot -= yaw * 0.6F;
         }
      }
   }

   private ScytheSwing() {
   }
}
