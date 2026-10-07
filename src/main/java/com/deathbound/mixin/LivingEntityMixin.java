package com.deathbound.mixin;

import com.deathbound.item.ReaperScytheItem;
import com.deathbound.item.ScytheCombo;
import com.deathbound.registry.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin implements ScytheCombo {
   @Unique
   private int deathbound$combo;
   @Unique
   private int deathbound$lastSwing = -100;

   @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)Z", at = @At("RETURN"))
   private void deathbound$countCombo(InteractionHand hand, SwingAnimation animation, boolean sendToSwingingEntity, CallbackInfoReturnable<Boolean> cir) {
      LivingEntity self = (LivingEntity)(Object)this;
      if (cir.getReturnValueZ() && hand == InteractionHand.MAIN_HAND && self.getMainHandItem().is(ModItems.REAPER_SCYTHE)) {
         this.deathbound$combo = self.tickCount - this.deathbound$lastSwing > 32 ? 0 : (this.deathbound$combo + 1) % 3;
         this.deathbound$lastSwing = self.tickCount;
         ReaperScytheItem.onSwing(self, this.deathbound$combo);
      }
   }

   @Override
   public int deathbound$comboStep() {
      return this.deathbound$combo;
   }
}
