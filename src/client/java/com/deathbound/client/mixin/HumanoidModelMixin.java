package com.deathbound.client.mixin;

import com.deathbound.client.ScytheSwing;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
abstract class HumanoidModelMixin {
   @Inject(method = "setupAttackAnimation", at = @At("TAIL"))
   private void deathbound$scytheArms(HumanoidRenderState state, CallbackInfo ci) {
      ScytheSwing.thirdPerson((HumanoidModel<?>)(Object)this, state);
   }
}
