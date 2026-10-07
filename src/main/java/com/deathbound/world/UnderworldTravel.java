package com.deathbound.world;

import com.deathbound.DeathBound;
import com.deathbound.registry.ModAttachments;
import com.deathbound.registry.ModItems;
import com.deathbound.registry.ModNet;
import com.deathbound.registry.ModParticles;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public final class UnderworldTravel {
   public static final ResourceKey<Level> UNDERWORLD = ResourceKey.create(Registries.DIMENSION, DeathBound.id("underworld"));
   private static final int DESCENT_TICKS = 110;
   private static final Map<UUID, Integer> PENDING = new HashMap<>();

   public static boolean inUnderworld(Entity entity) {
      return entity.level().dimension() == UNDERWORLD;
   }

   public static void beginDescent(ServerPlayer player) {
      if (!PENDING.containsKey(player.getUUID())) {
         player.setAttached(ModAttachments.RETURN_POINT, GlobalPos.of(player.level().dimension(), player.blockPosition()));
         PENDING.put(player.getUUID(), 110);
         ModNet.cinematic(player, 0);
         ServerLevel level = player.level();
         level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_DEATH, SoundSource.PLAYERS, 1.0F, 0.6F);
         level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 2.0F, 0.5F);
         level.playSound(null, player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 1.0F, 0.5F);
         level.sendParticles(ModParticles.SOUL_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 80, 0.4, 0.8, 0.4, 0.06);
         player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 130, 9, false, false, false));
         player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 170, 4, false, false, false));
      }
   }

   private static void arrive(ServerPlayer player) {
      MinecraftServer server = player.level().getServer();
      ServerLevel underworld = server.getLevel(UNDERWORLD);
      if (underworld == null) {
         DeathBound.LOG.error("Underworld dimension is missing; is the datapack loaded?");
      } else {
         BlockPos spot = Layout.ARRIVAL_SPAWN;
         underworld.getChunk(spot);
         player.teleport(new TeleportTransition(underworld, Vec3.atBottomCenterOf(spot), Vec3.ZERO, 180.0F, 0.0F, TeleportTransition.DO_NOTHING));
         underworld.playSound(null, spot, SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.value(), SoundSource.AMBIENT, 2.0F, 0.6F);
         player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false, false));
      }
   }

   public static void returnToLife(ServerPlayer player, boolean viaFerry) {
      Milestones.award(player, "return_fare");
      GlobalPos back = player.removeAttached(ModAttachments.RETURN_POINT);
      ServerLevel target = back == null ? null : player.level().getServer().getLevel(back.dimension());
      ModNet.cinematic(player, 1);
      if (target == null) {
         player.teleport(TeleportTransition.createDefault(player, TeleportTransition.DO_NOTHING));
      } else {
         target.getChunk(back.pos());
         player.teleport(new TeleportTransition(target, Vec3.atBottomCenterOf(back.pos()), Vec3.ZERO, player.getYRot(), 0.0F, TeleportTransition.DO_NOTHING));
      }

      player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.7F, 0.8F);
      player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
   }

   public static void init() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         Iterator<Entry<UUID, Integer>> it = PENDING.entrySet().iterator();

         while (it.hasNext()) {
            Entry<UUID, Integer> e = it.next();
            ServerPlayer p = server.getPlayerList().getPlayer(e.getKey());
            if (p == null) {
               it.remove();
            } else {
               int left = e.getValue() - 1;
               if (left <= 0) {
                  it.remove();
                  arrive(p);
               } else {
                  e.setValue(left);
                  p.setDeltaMovement(Vec3.ZERO);
               }
            }
         }
      });
      ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
         if (entity instanceof ServerPlayer p && !inUnderworld(p) && p.getOffhandItem().is(ModItems.DEATHBOUND_RELIC) && !PENDING.containsKey(p.getUUID())) {
            p.setHealth(p.getMaxHealth());
            p.resetFallDistance();
            p.clearFire();
            beginDescent(p);
            return false;
         } else {
            return true;
         }
      });
      ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
         if (!alive && oldPlayer.level().dimension() == UNDERWORLD) {
            newPlayer.removeAttached(ModAttachments.RETURN_POINT);
            ModNet.cinematic(newPlayer, 2);
            if (newPlayer.hasAttached(ModAttachments.FERRY_KEEP)) {
               newPlayer.sendSystemMessage(Component.translatable("message.deathbound.ferry_keep").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
         }
      });
   }

   private UnderworldTravel() {
   }
}
