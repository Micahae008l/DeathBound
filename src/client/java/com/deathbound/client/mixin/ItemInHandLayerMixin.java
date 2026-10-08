package com.deathbound.client.mixin;

import com.deathbound.client.ScytheSwing;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Third person: the scythe turns in the hand during its combo (where vanilla turns the spear). */
@Mixin(ItemInHandLayer.class)
abstract class ItemInHandLayerMixin {
   @Inject(
      method = "submitArmWithItem",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
      )
   )
   private void deathbound$scytheWrist(
      ArmedEntityRenderState state,
      ItemStackRenderState item,
      ItemStack stack,
      HumanoidArm arm,
      PoseStack poseStack,
      SubmitNodeCollector collector,
      int lightCoords,
      CallbackInfo ci
   ) {
      ScytheSwing.thirdPersonItem(state, arm, stack, poseStack);
   }
}
