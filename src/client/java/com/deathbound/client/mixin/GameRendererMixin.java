package com.deathbound.client.mixin;

import com.deathbound.client.Cine;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
   @Inject(method = "render", at = @At("HEAD"))
   private void deathbound$record(CallbackInfo ci) {
      Cine.frame();
   }
}
