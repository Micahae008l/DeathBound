package com.deathbound.registry;

import com.deathbound.DeathBound;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class ModNet {
   private static final Map<UUID, Long> LAST_BIG = new HashMap<>();

   public static void received(ServerPlayer player, Component title, List<ItemStack> items) {
      ServerPlayNetworking.send(player, new ModNet.Received(title, items));
   }

   public static void music(ServerPlayer player, int track) {
      ServerPlayNetworking.send(player, new ModNet.Music(track));
   }

   public static void bigTitle(ServerPlayer player) {
      LAST_BIG.put(player.getUUID(), player.level().getGameTime());
   }

   public static boolean recentlyTitled(ServerPlayer player, int ticks) {
      Long t = LAST_BIG.get(player.getUUID());
      return t != null && player.level().getGameTime() - t < ticks;
   }

   public static void cutscene(ServerPlayer player, int which) {
      ServerPlayNetworking.send(player, new ModNet.Cutscene(which));
   }

   public static void cinematic(ServerPlayer player, int kind) {
      bigTitle(player);
      ServerPlayNetworking.send(player, new ModNet.Cinematic(kind));
   }

   public static void shake(ServerLevel level, Vec3 at, double radius, float strength, int ticks) {
      for (ServerPlayer p : level.players()) {
         double d = p.position().distanceTo(at);
         if (d < radius) {
            ServerPlayNetworking.send(p, new ModNet.Shake(strength * (float)(1.0 - d / radius), ticks));
         }
      }
   }

   public static void init() {
      PayloadTypeRegistry.clientboundPlay().register(ModNet.Cinematic.TYPE, ModNet.Cinematic.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(ModNet.Shake.TYPE, ModNet.Shake.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(ModNet.Cutscene.TYPE, ModNet.Cutscene.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(ModNet.Received.TYPE, ModNet.Received.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(ModNet.Music.TYPE, ModNet.Music.CODEC);
   }

   private ModNet() {
   }

   public record Cinematic(int kind) implements CustomPacketPayload {
      public static final int DESCENT = 0;
      public static final int RETURN = 1;
      public static final int FORGOTTEN = 2;
      public static final int DOOR_OPENS = 3;
      public static final int THRONE = 4;
      public static final int REAPER = 5;
      public static final int BEAST = 6;
      public static final int VICTORY = 7;
      public static final int AWAKENED = 8;
      public static final int ENDING_TAKE = 9;
      public static final int ENDING_KING = 10;
      public static final int ENDING_BREAK = 11;
      public static final Type<ModNet.Cinematic> TYPE = new Type<>(DeathBound.id("cinematic"));
      public static final StreamCodec<RegistryFriendlyByteBuf, ModNet.Cinematic> CODEC = StreamCodec.composite(
         ByteBufCodecs.VAR_INT, ModNet.Cinematic::kind, ModNet.Cinematic::new
      );

      @Override
      public Type<ModNet.Cinematic> type() {
         return TYPE;
      }
   }

   public record Cutscene(int which) implements CustomPacketPayload {
      public static final Type<ModNet.Cutscene> TYPE = new Type<>(DeathBound.id("cutscene"));
      public static final StreamCodec<RegistryFriendlyByteBuf, ModNet.Cutscene> CODEC = StreamCodec.composite(
         ByteBufCodecs.VAR_INT, ModNet.Cutscene::which, ModNet.Cutscene::new
      );

      @Override
      public Type<ModNet.Cutscene> type() {
         return TYPE;
      }
   }

   public record Music(int track) implements CustomPacketPayload {
      public static final int NONE = 0;
      public static final int THRONE = 1;
      public static final int SILENCE = 2;
      public static final Type<ModNet.Music> TYPE = new Type<>(DeathBound.id("music"));
      public static final StreamCodec<RegistryFriendlyByteBuf, ModNet.Music> CODEC = StreamCodec.composite(
         ByteBufCodecs.VAR_INT, ModNet.Music::track, ModNet.Music::new
      );

      @Override
      public Type<ModNet.Music> type() {
         return TYPE;
      }
   }

   public record Received(Component title, List<ItemStack> items) implements CustomPacketPayload {
      public static final Type<ModNet.Received> TYPE = new Type<>(DeathBound.id("received"));
      public static final StreamCodec<RegistryFriendlyByteBuf, ModNet.Received> CODEC = StreamCodec.composite(
         ComponentSerialization.STREAM_CODEC, ModNet.Received::title, ItemStack.OPTIONAL_LIST_STREAM_CODEC, ModNet.Received::items, ModNet.Received::new
      );

      @Override
      public Type<ModNet.Received> type() {
         return TYPE;
      }
   }

   public record Shake(float strength, int ticks) implements CustomPacketPayload {
      public static final Type<ModNet.Shake> TYPE = new Type<>(DeathBound.id("shake"));
      public static final StreamCodec<RegistryFriendlyByteBuf, ModNet.Shake> CODEC = StreamCodec.composite(
         ByteBufCodecs.FLOAT, ModNet.Shake::strength, ByteBufCodecs.VAR_INT, ModNet.Shake::ticks, ModNet.Shake::new
      );

      @Override
      public Type<ModNet.Shake> type() {
         return TYPE;
      }
   }
}
