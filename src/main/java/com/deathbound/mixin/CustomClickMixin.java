package com.deathbound.mixin;

import com.deathbound.charm.Charms;
import com.deathbound.npc.Collector;
import com.deathbound.npc.Quests;
import com.deathbound.npc.Soulforge;
import com.deathbound.npc.Talk;
import com.deathbound.npc.Temper;
import com.deathbound.npc.UnderworldNpc;
import com.deathbound.world.Director;
import com.deathbound.world.Layout;
import java.util.Comparator;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
abstract class CustomClickMixin {
   @Inject(method = "handleCustomClickAction", at = @At("TAIL"))
   private void deathbound$onCustomClick(ServerboundCustomClickActionPacket packet, CallbackInfo ci) {
      if ((Object)this instanceof ServerGamePacketListenerImpl game && packet.id().getNamespace().equals("deathbound")) {
         ServerPlayer player = game.player;
         if (packet.id().getPath().equals("reclaim")) {
            boolean had = Charms.reclaim(player);
            player.sendSystemMessage(
               Component.translatable(had ? "message.deathbound.reclaim.given" : "message.deathbound.reclaim.none")
                  .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            );
            if (had) {
               player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 1.0F, 0.6F);
            }
         } else if (packet.id().getPath().equals("soulforge")) {
            Soulforge.forge(player, packet.payload().flatMap(Tag::asString).orElse(""));
         } else if (packet.id().getPath().equals("talk")
            || packet.id().getPath().equals("ask")
            || packet.id().getPath().equals("back")
            || packet.id().getPath().equals("topic")) {
            String arg = packet.payload().flatMap(Tag::asString).orElse("");
            switch (packet.id().getPath()) {
               case "talk":
                  Talk.open(player, arg);
                  break;
               case "ask":
                  Talk.ask(player, arg);
                  break;
               case "back":
                  Talk.back(player, arg);
                  break;
               default:
                  Talk.topic(player, arg);
            }
         } else if (packet.id().getPath().equals("quest")) {
            Quests.talk(player, packet.payload().flatMap(Tag::asString).orElse(""));
         } else if (packet.id().getPath().equals("quest_accept")) {
            Quests.accept(player, packet.payload().flatMap(Tag::asString).orElse(""));
         } else if (packet.id().getPath().equals("temper")) {
            Temper.temper(player, packet.payload().flatMap(Tag::asString).orElse(""));
         } else if (packet.id().getPath().equals("collect")) {
            Collector.collect(player);
         } else if (packet.id().getPath().equals("ending")) {
            if (Layout.inArena(player.getX(), player.getY(), player.getZ())) {
               Director.chooseEnding(player, packet.payload().flatMap(Tag::asString).orElse(""));
            }
         } else if (packet.id().getPath().equals("trade")) {
            player.level()
               .getEntitiesOfClass(UnderworldNpc.class, player.getBoundingBox().inflate(8.0))
               .stream()
               .min(Comparator.comparingDouble(player::distanceToSqr))
               .ifPresent(npc -> npc.openTrade(player));
         }
      }
   }
}
