package com.deathbound.client.mixin;

import com.deathbound.client.Cine;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dev filming only: while the film camera (Cine) is running, the screen overlays that belong to the player's own head
 * (inside a block, under water, on fire) are wrong, since the picture is the film camera's, not the player's. Without
 * this, a player the server holds inside a wall flashes the wall's texture over every third frame of a shot.
 */
@Mixin(ScreenEffectRenderer.class)
abstract class ScreenEffectMixin {
   @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
   private void deathbound$noOverlayWhileFilming(
      float partialTicks, SubmitNodeCollector collector, PlayerRenderState player, CameraRenderState camera, boolean hideGui, CallbackInfo ci
   ) {
      if (Cine.active()) {
         ci.cancel();
      }
   }
}
