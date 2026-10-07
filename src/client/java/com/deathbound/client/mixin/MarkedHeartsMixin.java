package com.deathbound.client.mixin;

import com.deathbound.DeathBound;
import com.deathbound.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Hud$HeartType")
abstract class MarkedHeartsMixin {
   private static final Identifier FULL = DeathBound.id("hud/heart/marked_full");
   private static final Identifier FULL_BLINK = DeathBound.id("hud/heart/marked_full_blinking");
   private static final Identifier HALF = DeathBound.id("hud/heart/marked_half");
   private static final Identifier HALF_BLINK = DeathBound.id("hud/heart/marked_half_blinking");

   @Inject(method = "getSprite", at = @At("HEAD"), cancellable = true)
   private void deathbound$marked(boolean hardcore, boolean half, boolean blinking, CallbackInfoReturnable<Identifier> cir) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (((Enum<?>)(Object)this).name().equals("NORMAL") && player != null && player.hasEffect(ModEffects.MARKED)) {
         cir.setReturnValue(half ? (blinking ? HALF_BLINK : HALF) : (blinking ? FULL_BLINK : FULL));
      }
   }
}
