package com.deathbound.client;

import com.deathbound.item.ScytheCombo;
import com.deathbound.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
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

   /** The swinging arm's pitch (third person) for a combo step: forward is negative. */
   private static float mainPitch(int combo, float pre, float cut) {
      return switch (combo) {
         case 0 -> -1.0F * pre - 1.35F * cut;
         case 1 -> -1.25F * pre - 1.3F * cut;
         default -> -2.95F * pre - 0.75F * cut;
      };
   }

   /** Where the hand closes on the snath, in the item's frame (blocks): matches the model's third-person grip. */
   private static final float GRIP_Y = -1.9F / 16.0F;
   private static final float GRIP_Z = 1.5F / 16.0F;
   /** The arm's pitch while just holding an item (vanilla's ITEM arm pose). */
   private static final float HOLD_PITCH = (float)(-Math.PI / 10.0);

   /**
    * Third person, the wrist. At rest the scythe is held out like a sword, the snath angled forward and the blade hooking
    * down in front. The arms do the swinging (thirdPerson); left alone, raising them would lay the snath back over the
    * shoulder and wave the blade over the head. So the wrist cancels the arm's pitch and tips the scythe its own way:
    * cocked up on the wind-up, then over and down so the blade hangs in front at body height and is dragged through
    * whatever you face (the slam brings it down to the ground).
    */
   public static void thirdPersonItem(ArmedEntityRenderState state, HumanoidArm arm, ItemStack stack, PoseStack pose) {
      SwingDescription swing = state.currentSwing;
      if (swing != null && !(state.swingAnimation <= 0.0F) && stack.is(ModItems.REAPER_SCYTHE) && state instanceof AvatarRenderState avatar
         && swing.hand().asArm(state.mainArm) == arm && Minecraft.getInstance().level != null) {
         int combo = combo(Minecraft.getInstance().level.getEntity(avatar.id));
         float[] c = curve(state.swingAnimation);
         float pre = c[0];
         float cut = c[1];
         float tip = switch (combo) {   // degrees from the sword hold, forward is negative
            case 0 -> 50.0F * pre - 32.0F * cut;
            case 1 -> 45.0F * pre - 28.0F * cut;
            default -> 70.0F * pre - 42.0F * cut;
         };
         float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
         boolean slam = combo >= 2;
         pose.translate(0.0F, GRIP_Y, GRIP_Z);
         pose.rotateDegrees(Axis.ZP, side * (slam ? 35.0F * cut : 0.0F));   // the slam comes down in front of you, not by your foot
         pose.rotateDegrees(Axis.XP, (float)Math.toDegrees(mainPitch(combo, pre, cut) - HOLD_PITCH) + tip);
         // turned out at rest so it can be seen; it swings facing ahead
         pose.rotateDegrees(Axis.ZP, side * 25.0F * Math.min(1.0F, pre + cut));
         pose.translate(0.0F, -GRIP_Y, -GRIP_Z);
      }
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
            // the frame here: x right, y up, z toward the camera; the scythe stands up from the hand with its blade
            // pointing into the screen. Wind up to one side, then tip the top away (pitch) so the blade drops and hangs
            // in front, and sweep it across the crosshair (yaw); the slam lifts it high and brings it straight down
            switch (combo(Minecraft.getInstance().player)) {
               case 0:
                  roll = inv * -22.0F * pre;
                  pitch = 12.0F * pre - 46.0F * cut;
                  yaw = inv * (-28.0F * pre + 52.0F * cut);
                  tx = inv * (0.12F * pre - 0.3F * cut);
                  ty = 0.08F * pre + 0.1F * cut;
                  tz = -0.1F * cut;
                  break;
               case 1:
                  roll = inv * 26.0F * pre;
                  pitch = 10.0F * pre - 46.0F * cut;
                  yaw = inv * (30.0F * pre + 20.0F * cut);
                  tx = inv * (-0.26F * pre - 0.12F * cut);
                  ty = 0.1F * pre + 0.1F * cut;
                  tz = -0.1F * cut;
                  break;
               default:
                  pitch = 30.0F * pre - 62.0F * cut;
                  yaw = inv * 14.0F * cut;
                  tx = inv * -0.22F * cut;
                  ty = 0.22F * pre + 0.07F * cut;
                  tz = -0.1F * cut;
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
            float mainX = mainPitch(combo, pre, cut);
            float offZ = 0.0F;
            float yaw;
            float pitch;
            float mainY;
            float mainZ;
            float offX;
            float offY;
            float step;
            switch (combo) {
               case 0:
                  yaw = inv * (0.65F * pre - 0.85F * cut);
                  pitch = 0.16F * cut;
                  mainY = inv * (0.55F * pre - 0.65F * cut);
                  mainZ = inv * 0.25F * pre;
                  offX = -1.15F * pre - 1.25F * cut;
                  offY = inv * (0.9F * pre - 0.35F * cut);
                  step = 0.35F * cut;
                  break;
               case 1:
                  yaw = inv * (-0.7F * pre + 0.75F * cut);
                  pitch = 0.14F * cut;
                  mainY = inv * (-0.8F * pre + 0.5F * cut);
                  mainZ = -inv * 0.2F * pre;
                  offX = -1.0F * pre - 1.2F * cut;
                  offY = inv * (-0.4F * pre + 0.85F * cut);
                  step = 0.3F * cut;
                  break;
               default:
                  yaw = inv * 0.15F * cut;
                  pitch = -0.22F * pre + 0.28F * cut;
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
