package com.deathbound.entity;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class Speech {
   public static final int WARDEN = 10466520;
   public static final int DEATH = 13215487;
   public static final int HUNTER = 14209732;

   public static void say(ServerLevel level, Entity source, String speaker, String line, int color) {
      Component text = Component.translatable(
         "deathbound.say",
         Component.translatable("deathbound.speaker." + speaker).withColor(color).withStyle(ChatFormatting.BOLD),
         Component.translatable("deathbound.say." + speaker + "." + line).withStyle(ChatFormatting.WHITE)
      );

      for (ServerPlayer p : level.getPlayers(px -> px.distanceToSqr(source) < 3136.0)) {
         p.sendSystemMessage(text);
      }
   }

   public static void init() {
      ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
         if (entity instanceof ServerPlayer p && p.level() instanceof ServerLevel level) {
            if (source.getEntity() instanceof DeathsGuard g) {
               say(level, g, "warden", "kill", 10466520);
            } else if (source.getEntity() instanceof DeathEntity d) {
               say(level, d, "death", "kill", 13215487);
            } else if (source.getEntity() instanceof HollowHunter h) {
               say(level, h, "hunter", "kill", 14209732);
            }
         }
      });
   }

   private Speech() {
   }
}
