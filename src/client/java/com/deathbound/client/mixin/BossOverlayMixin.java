package com.deathbound.client.mixin;

import com.deathbound.client.Cine;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BossHealthOverlay.class)
abstract class BossOverlayMixin {
   @Inject(method = {"shouldDarkenScreen", "shouldCreateWorldFog"}, at = @At("HEAD"), cancellable = true)
   private void deathbound$clearAir(CallbackInfoReturnable<Boolean> cir) {
      if (Cine.clearAir) {
         cir.setReturnValue(false);
      }
   }
}
