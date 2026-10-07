package com.deathbound.mixin;

import com.deathbound.charm.Charm;
import com.deathbound.charm.Charms;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodData.class)
abstract class FoodDataMixin {
   @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;heal(F)V"))
   private void deathbound$reaperCap(ServerPlayer player, float amount, Operation<Void> original) {
      // Reaper's Charm: natural healing can't lift you out of the 30% danger zone (that's the trade-off),
      // but above 30% you regenerate normally. Previously it blocked all regen at any health.
      float cap = player.getMaxHealth() * Charms.REAPER_THRESHOLD;
      if (player.getHealth() <= cap && Charms.has(player, Charm.REAPER)) {
         amount = Math.min(amount, cap - player.getHealth());
         if (amount <= 0.0F) {
            return;
         }
      }

      original.call(new Object[]{player, amount});
   }
}
