package com.deathbound.client.mixin;

import com.deathbound.client.NpcDialogScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.DialogConnectionAccess;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.core.Holder;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.MultiActionDialog;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
abstract class ShowDialogMixin {
   @Inject(
      method = "showDialog(Lnet/minecraft/core/Holder;Lnet/minecraft/client/gui/screens/dialog/DialogConnectionAccess;Lnet/minecraft/client/gui/screens/Screen;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void deathbound$npcDialog(Holder<Dialog> dialog, DialogConnectionAccess access, @Nullable Screen active, CallbackInfo ci) {
      if (dialog.value() instanceof MultiActionDialog multi) {
         String path = dialog.unwrapKey()
            .filter(k -> k.identifier().getNamespace().equals("deathbound"))
            .map(k -> k.identifier().getPath())
            .orElseGet(
               () -> multi.common().externalTitle().map(t -> t.getString()).filter(t -> t.startsWith("deathbound:")).map(t -> t.substring(11)).orElse(null)
            );
         if (path != null) {
            Screen previous = active instanceof NpcDialogScreen npc ? npc.previous() : active;
            Minecraft.getInstance().gui.setScreen(new NpcDialogScreen(multi, path, access, previous));
            ci.cancel();
         }
      }
   }
}
