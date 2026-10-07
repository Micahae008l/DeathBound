package com.deathbound.mixin;

import com.deathbound.world.Difficulty;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
abstract class PlayerMixin {
   @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
   private float deathbound$scale(float damage, ServerLevel level, DamageSource source) {
      return Difficulty.scale((Player)(Object)this, source, damage);
   }
}
