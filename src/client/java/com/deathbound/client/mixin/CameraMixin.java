package com.deathbound.client.mixin;

import com.deathbound.client.Cine;
import com.deathbound.client.DeathBoundClient;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
abstract class CameraMixin {
   @Shadow
   protected abstract void setRotation(float var1, float var2);

   @Shadow
   protected abstract void setPosition(Vec3 var1);

   @Shadow
   public abstract float yRot();

   @Shadow
   public abstract float xRot();

   @Inject(method = "alignWithEntity", at = @At("TAIL"))
   private void deathbound$shake(float partialTicks, CallbackInfo ci) {
      Cine.Pose film = Cine.pose(partialTicks);
      if (film != null) {
         this.setPosition(film.pos());
         this.setRotation(film.yaw(), film.pitch());
      }

      float amp = DeathBoundClient.shakeAmount(partialTicks);
      if (amp > 0.001F) {
         double t = System.nanoTime() / 1.0E9 * 38.0;
         float dy = (float)(Math.sin(t * 1.3) + Math.sin(t * 2.9 + 1.7) * 0.5) * amp;
         float dx = (float)(Math.sin(t * 1.7 + 0.4) + Math.sin(t * 3.3 + 2.1) * 0.5) * amp * 0.7F;
         this.setRotation(this.yRot() + dy, this.xRot() + dx);
      }
   }
}
