package com.deathbound.world;

import com.deathbound.DeathBound;
import com.deathbound.block.SealLockBlock;
import com.deathbound.entity.DeathEntity;
import com.deathbound.entity.DeathsGuard;
import com.deathbound.entity.HollowHunter;
import com.deathbound.entity.LostSoul;
import com.deathbound.entity.SkeletonKid;
import com.deathbound.entity.SoulAnchor;
import com.deathbound.entity.Speech;
import com.deathbound.item.HeartOfDeathItem;
import com.deathbound.npc.UnderworldNpc;
import com.deathbound.registry.ModAttachments;
import com.deathbound.registry.ModBlocks;
import com.deathbound.registry.ModEntities;
import com.deathbound.registry.ModNet;
import com.deathbound.registry.ModParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class Director {
   public static final AttachmentType<Director.State> STATE = AttachmentRegistry.create(DeathBound.id("director"), b -> b.persistent(Director.State.CODEC));
   private static long emptyArenaSince = -1L;
   private static final Director.Haunt[] HAUNTS = new Director.Haunt[]{
      haunt(Layout.ARRIVAL, -11, 9, 0),
      haunt(Layout.ARRIVAL, 12, -6, 0),
      haunt(Layout.HUB, 9, -6, 1),
      haunt(Layout.HUB, -10, 9, 1),
      haunt(Layout.HUB, -6, -16, 1),
      haunt(Layout.FOREST, 7, 4, 2),
      haunt(Layout.FOREST, -9, -7, 2),
      haunt(Layout.FOREST, 2, 15, 2),
      haunt(Layout.VILLAGE, -12, -9, 3),
      haunt(Layout.VILLAGE, 6, 14, 3),
      haunt(Layout.VILLAGE, 16, -3, 3),
      haunt(Layout.VILLAGE, -14, 8, 3),
      new Director.Haunt(new BlockPos(Layout.CRYPT.x() - 4, Layout.CRYPT.top() - 9, Layout.CRYPT.z() + 1), 4),
      haunt(Layout.CRYPT, 9, 20, 4),
      haunt(Layout.SPIRE, 5, 7, 5),
      haunt(Layout.SPIRE, -7, -5, 5),
      haunt(Layout.WATCH, 4, 4, 6),
      haunt(Layout.WATCH, -5, -3, 6),
      haunt(Layout.GATE, -15, 20, 7),
      haunt(Layout.GATE, 16, 14, 7),
      haunt(Layout.CITADEL, -9, 44, 8),
      haunt(Layout.CITADEL, 11, 42, 8)
   };
   private static final Director.Home[] HOMES = new Director.Home[]{
      new Director.Home(ModEntities.FERRYMAN, new BlockPos(Layout.ARRIVAL.x() + 6, Layout.ARRIVAL.top() + 1, Layout.ARRIVAL.z() - 9), 270.0F, false),
      new Director.Home(ModEntities.GRAVEDIGGER, new BlockPos(Layout.VILLAGE.x() - 7, Layout.VILLAGE.top() + 1, Layout.VILLAGE.z() + 8), 270.0F, false),
      new Director.Home(ModEntities.PROPHET, new BlockPos(Layout.CRYPT.x() + 8, Layout.CRYPT.top() - 9, Layout.CRYPT.z() - 4), 0.0F, true),
      new Director.Home(ModEntities.BONESMITH, new BlockPos(Layout.MERE.x() + 2, Layout.MERE.top() + 1, Layout.MERE.z() + 1), 90.0F, false),
      new Director.Home(ModEntities.MIRA, BlockPos.ZERO, 180.0F, false),
      new Director.Home(ModEntities.LAMPLIGHTER, new BlockPos(Layout.VILLAGE.x() + 5, Layout.VILLAGE.top() + 1, Layout.VILLAGE.z() - 6), 0.0F, false),
      new Director.Home(ModEntities.SENTRY, new BlockPos(Layout.WATCH.x() - 5, Layout.WATCH.top() + 1, Layout.WATCH.z() + 1), 90.0F, false),
      new Director.Home(ModEntities.COLLECTOR, new BlockPos(Layout.SPIRE.x(), Layout.SPIRE.top() - 10, Layout.SPIRE.z() + 1), 180.0F, false)
   };
   private static final Director.Home KING_HOME = new Director.Home(ModEntities.PROPHET, Layout.THRONE_SEAT, 0.0F, true, 0.0);
   private static boolean arenaSealed = true;
   private static long hollowEmptySince = -1L;
   public static final int TAKE = 1;
   public static final int KING = 2;
   public static final int BREAK = 3;

   private static Director.State state(ServerLevel level) {
      return level.getAttachedOrElse(STATE, Director.State.FRESH);
   }

   public static void init() {
      ServerTickEvents.END_LEVEL_TICK.register(level -> {
         if (level.dimension() == UnderworldTravel.UNDERWORLD && level.getGameTime() % 20L == 0L) {
            tickGate(level);
            tickProcession(level);
            tickArena(level);
            tickNpcs(level);
            tickHollow(level);
            tickHaunts(level);
            tickThroneMusic(level);
         }
      });
   }

   private static Director.Haunt haunt(Layout.Island is, int dx, int dz, int place) {
      return new Director.Haunt(new BlockPos(is.x() + dx, is.top() + 1, is.z() + dz), place);
   }

   private static void tickHaunts(ServerLevel level) {
      for (Director.Haunt h : HAUNTS) {
         if (level.getNearestPlayer(h.at.getX(), h.at.getY(), h.at.getZ(), 48.0, e -> true) != null
            && level.isPositionEntityTicking(h.at)
            && level.getEntitiesOfClass(LostSoul.class, new AABB(h.at).inflate(7.0, 6.0, 7.0), LostSoul::lingering).isEmpty()) {
            LostSoul soul = ModEntities.LOST_SOUL.create(level, EntitySpawnReason.EVENT);
            if (soul != null) {
               soul.snapTo(h.at.getX() + 0.5, h.at.getY(), h.at.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
               soul.linger(h.at, h.place);
               level.addFreshEntity(soul);
            }
         }
      }
   }

   private static void tickKids(ServerLevel level) {
      BlockPos square = new BlockPos(Layout.VILLAGE.x(), Layout.VILLAGE.top() + 1, Layout.VILLAGE.z());
      if (level.getNearestPlayer(square.getX(), square.getY(), square.getZ(), 64.0, false) != null && level.isPositionEntityTicking(square)) {
         List<SkeletonKid> kids = level.getEntitiesOfClass(SkeletonKid.class, new AABB(square).inflate(40.0, 20.0, 40.0));

         for (SkeletonKid k : kids) {
            if (!k.hasHome()) {
               k.setHomeTo(square, 26);
            }
         }

         if (kids.size() < 4) {
            RandomSource r = level.getRandom();
            BlockPos at = standingSpot(level, square.offset(r.nextInt(13) - 6, 0, r.nextInt(13) - 6));
            SkeletonKid kid = ModEntities.SKELETON_KID.create(level, EntitySpawnReason.STRUCTURE);
            if (kid != null) {
               kid.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, r.nextFloat() * 360.0F, 0.0F);
               kid.setHomeTo(square, 26);
               level.addFreshEntity(kid);
            }
         }
      }
   }

   private static void tickNpcs(ServerLevel level) {
      if (level.getGameTime() % 40L == 0L) {
         tickKids(level);
      }

      boolean kingFreed = state(level).ending() == 2;

      for (Director.Home home : HOMES) {
         Director.Home h = kingFreed && home.type == ModEntities.PROPHET ? KING_HOME : home;
         if (h.type == ModEntities.MIRA) {
            // her place in the line differs from world to world: the souls in the line point the way
            h = new Director.Home(ModEntities.MIRA, QuestEvents.miraSpot(level), h.yaw, false);
         }
         if (level.getNearestPlayer(h.at.getX(), h.at.getY(), h.at.getZ(), 64.0, e -> true) != null && level.isPositionEntityTicking(h.at)) {
            List<UnderworldNpc> there = level.getEntities(h.type, new AABB(h.at).inflate(24.0), e -> true);
            if (there.isEmpty()) {
               BlockPos stand = h.floats ? h.at : standingSpot(level, h.at);
               UnderworldNpc npc = h.type.create(level, EntitySpawnReason.STRUCTURE);
               if (npc != null) {
                  npc.snapTo(stand.getX() + 0.5, stand.getY() + h.lift, stand.getZ() + 0.5, h.yaw, 0.0F);
                  npc.setYHeadRot(h.yaw);
                  npc.setYBodyRot(h.yaw);
                  level.addFreshEntity(npc);
               }
            } else {
               for (UnderworldNpc npc : there) {
                  boolean offHeight = h.floats && Math.abs(npc.getY() - (h.at.getY() + h.lift)) > 0.2;
                  if ((npc.blockPosition().distManhattan(h.at) > 3 || offHeight) && level.getNearestPlayer(npc, 6.0) == null) {
                     BlockPos stand = h.floats ? h.at : standingSpot(level, h.at);
                     npc.snapTo(stand.getX() + 0.5, stand.getY() + h.lift, stand.getZ() + 0.5, h.yaw, 0.0F);
                     npc.setYHeadRot(h.yaw);
                     npc.setYBodyRot(h.yaw);
                  }
               }
            }
         }
      }
   }

   static BlockPos standingSpot(ServerLevel level, BlockPos near) {
      for (int i = 0; i <= 10; i++) {
         BlockPos p = near.above(i <= 6 ? 1 - i : i - 5);
         if (level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP)
            && level.getBlockState(p).getCollisionShape(level, p).isEmpty()
            && level.getBlockState(p.above()).getCollisionShape(level, p.above()).isEmpty()) {
            return p;
         }
      }

      return near;
   }

   private static boolean playersNear(ServerLevel level, BlockPos pos, double radius) {
      return level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), radius, false) != null;
   }

   private static AABB gateBox() {
      return new AABB(Layout.GUARD_POST).inflate(48.0, 20.0, 48.0);
   }

   private static void tickGate(ServerLevel level) {
      BlockPos post = Layout.GUARD_POST;
      if (playersNear(level, post, 72.0) && level.isPositionEntityTicking(post)) {
         Director.State st = state(level);
         if (st.progress() >= 1) {
            if (!st.doorOpen()) {
               setDoor(level, true);
               level.setAttached(STATE, st.door(true));
            }
         } else {
            if (level.getEntitiesOfClass(DeathsGuard.class, gateBox()).isEmpty() && level.getGameTime() >= st.guardReadyAt()) {
               DeathsGuard guard = ModEntities.DEATHS_GUARD.create(level, EntitySpawnReason.EVENT);
               if (guard != null) {
                  guard.snapTo(post.getX() + 0.5, post.getY(), post.getZ() + 0.5, 0.0F, 0.0F);
                  guard.finalizeSpawn(level, level.getCurrentDifficultyAt(post), EntitySpawnReason.EVENT, null);
                  guard.setPersistenceRequired();
                  level.addFreshEntity(guard);
                  setDoor(level, false);
                  level.setAttached(STATE, st.door(false));
               }
            }
         }
      }
   }

   private static void setDoor(ServerLevel level, boolean open) {
      int y0 = Layout.GATE.top() + 1;

      for (int x = -4; x <= 4; x++) {
         for (int rel = 0; rel < 23; rel++) {
            if (Layout.inDoorway(x, rel)) {
               BlockPos p = new BlockPos(x, y0 + rel, Layout.DOOR_Z);
               level.setBlock(p, open ? Blocks.AIR.defaultBlockState() : ModBlocks.SOUL_SEAL.defaultBlockState(), 3);
            }
         }
      }
   }

   public static void onGuardDefeated(ServerLevel level) {
      level.setAttached(STATE, state(level).door(true).reached(1));
      setDoor(level, true);
      Vec3 door = Vec3.atCenterOf(Layout.SOUL_DESTINATION);
      level.playSound(null, Layout.SOUL_DESTINATION, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 3.0F, 0.5F);
      level.playSound(null, Layout.SOUL_DESTINATION, SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 3.0F, 0.6F);
      level.sendParticles(ModParticles.SOUL_FLAME, door.x, door.y + 8.0, door.z, 300, 4.0, 8.0, 0.6, 0.05);
      ModNet.shake(level, door, 64.0, 2.5F, 30);

      for (ServerPlayer p : level.getPlayers(px -> px.position().distanceTo(door) < 80.0)) {
         ModNet.cinematic(p, 3);
         p.sendSystemMessage(Component.translatable("puzzle.deathbound.after_warden").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
      }
   }

   private static void tickProcession(ServerLevel level) {
      BlockPos src = Layout.SOUL_SOURCE;
      BlockPos mid = new BlockPos(0, src.getY(), (src.getZ() + Layout.SOUL_DESTINATION.getZ()) / 2);
      if (playersNear(level, mid, 110.0) && level.isPositionEntityTicking(src) && level.getRandom().nextInt(4) == 0) {
         AABB path = new AABB(src.getX() - 8, src.getY() - 10, Layout.SOUL_DESTINATION.getZ() - 4, src.getX() + 8, src.getY() + 14, src.getZ() + 4);
         List<LostSoul> souls = level.getEntitiesOfClass(LostSoul.class, path);
         if (souls.size() < 14) {
            for (LostSoul s : souls) {
               if (s.position().distanceTo(Vec3.atBottomCenterOf(src)) < 2.5) {
                  return;
               }
            }

            LostSoul soul = ModEntities.LOST_SOUL.create(level, EntitySpawnReason.EVENT);
            if (soul != null) {
               soul.snapTo(src.getX() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 1.2, src.getY(), src.getZ() + 0.5, 180.0F, 0.0F);
               level.addFreshEntity(soul);
            }
         }
      }
   }

   private static AABB arena() {
      return Layout.arenaBox();
   }

   public static boolean isInBossFight(Player player) {
      return player.level() instanceof ServerLevel level
         && level.dimension() == UnderworldTravel.UNDERWORLD
         && Layout.inArena(player.getX(), player.getY(), player.getZ())
         && !level.getEntitiesOfClass(DeathEntity.class, arena()).isEmpty();
   }

   private static void tickArena(ServerLevel level) {
      BlockPos c = Layout.ARENA_CENTER;
      if (level.isPositionEntityTicking(c)) {
         List<ServerPlayer> inside = level.getPlayers(px -> !px.isSpectator() && Layout.inArena(px.getX(), px.getY(), px.getZ()));
         // every Death King in the dimension, not just inside the arena box: if he strays above it (towers, flight)
         // the old box check thought he was gone and spawned a second one
         List<DeathEntity> deaths = new java.util.ArrayList<>(level.getEntities(ModEntities.DEATH, d -> !d.isRemoved()));
         for (DeathEntity d : deaths) {
            if (d.isAlive() && !d.isNoAi() && !arena().contains(d.position()) && level.getGameTime() % 20L == 0L) {
               BlockPos seat = Layout.THRONE_SEAT;
               d.teleportTo(seat.getX() + 0.5, seat.getY() + 0.5, seat.getZ() + 0.5);
            }
         }
         Director.State st = state(level);
         long now = level.getGameTime();
         if (st.progress() >= 2) {
            sealArena(level, false);
            // the way home - except after breaking the throne, where the way out is the run back to the Landing
            boolean wantRift = st.ending() != BREAK;
            if (st.riftOpen() != wantRift) {
               setRift(level, wantRift);
               level.setAttached(STATE, st.rift(wantRift));
            }
         } else if (st.citadelOpen()) {
            sealArena(level, !inside.isEmpty() && deaths.stream().anyMatch(LivingEntity::isAlive));
            if (inside.isEmpty()) {
               if (!deaths.isEmpty()) {
                  if (emptyArenaSince < 0L) {
                     emptyArenaSince = now;
                  } else if (now - emptyArenaSince > 600L) {
                     deaths.stream().filter(d -> !d.isNoAi()).forEach(d -> d.discard());
                     level.getEntitiesOfClass(SoulAnchor.class, arena()).forEach(a -> a.discard());
                     emptyArenaSince = -1L;
                  }
               }
            } else {
               emptyArenaSince = -1L;
               if (deaths.isEmpty() && now >= st.deathReadyAt()) {
                  if (st.riftOpen()) {
                     setRift(level, false);
                  }

                  DeathEntity death = ModEntities.DEATH.create(level, EntitySpawnReason.EVENT);
                  if (death == null) {
                     return;
                  }

                  BlockPos seat = Layout.THRONE_SEAT;
                  death.snapTo(seat.getX() + 0.5, seat.getY() + 0.5, seat.getZ() + 0.5, 0.0F, 0.0F);
                  death.setPersistenceRequired();
                  level.addFreshEntity(death);
                  Speech.say(level, death, "death", "wake", 13215487);

                  for (BlockPos a : Layout.ANCHORS) {
                     SoulAnchor anchor = ModEntities.SOUL_ANCHOR.create(level, EntitySpawnReason.EVENT);
                     if (anchor != null) {
                        anchor.snapTo(a.getX() + 0.5, a.getY(), a.getZ() + 0.5, 0.0F, 0.0F);
                        anchor.bindTo(death);
                        level.addFreshEntity(anchor);
                     }
                  }

                  level.setAttached(STATE, st.rift(false));
                  level.playSound(null, seat, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.5F, 0.5F);

                  for (ServerPlayer p : inside) {
                     ModNet.cinematic(p, 4);
                  }
               }
            }
         }
      }
   }

   public static int seals(ServerLevel level) {
      return state(level).seals();
   }

   public static void onSealFilled(ServerLevel level, int kind) {
      Director.State st = state(level).sealed(1 << kind);
      level.setAttached(STATE, st);
      if (st.citadelOpen()) {
         openCitadel(level, true);
      }
   }

   public static void debugSeals(ServerLevel level, int mask) {
      Director.State st = state(level);
      level.setAttached(
         STATE, new Director.State(st.guardReadyAt(), st.deathReadyAt(), st.doorOpen(), st.riftOpen(), st.progress(), st.ending(), st.hunterSlain(), mask & 7)
      );
      if ((mask & 7) == 7) {
         openCitadel(level, true);
      }
   }

   private static void openCitadel(ServerLevel level, boolean loud) {
      int cx = Layout.CITADEL.x();
      int z = Puzzles.GATE_Z;
      int y = Layout.CITADEL.top();

      for (int x = cx - 3; x <= cx + 3; x++) {
         for (int yy = y + 1; yy <= y + 9; yy++) {
            BlockPos p = new BlockPos(x, yy, z);
            BlockState s = level.getBlockState(p);
            if (s.is(ModBlocks.SOUL_SEAL) || s.getBlock() instanceof SealLockBlock) {
               level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            }
         }
      }

      if (loud) {
         Vec3 gate = new Vec3(cx + 0.5, y + 5, z + 0.5);
         level.playSound(null, gate.x, gate.y, gate.z, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 3.0F, 0.5F);
         level.playSound(null, gate.x, gate.y, gate.z, SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 3.0F, 0.5F);
         level.sendParticles(ModParticles.SOUL_FLAME, gate.x, gate.y, gate.z, 300, 3.0, 4.0, 0.6, 0.05);
         ModNet.shake(level, gate, 64.0, 2.0F, 30);

         for (ServerPlayer p : level.getPlayers(px -> px.position().distanceTo(gate) < 96.0)) {
            ModNet.bigTitle(p);
            p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 30));
            p.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("puzzle.deathbound.gate_open.sub").withStyle(ChatFormatting.GRAY)));
            p.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("puzzle.deathbound.gate_open").withColor(12167129)));
         }
      }
   }

   private static void sealArena(ServerLevel level, boolean closed) {
      if (closed != arenaSealed) {
         arenaSealed = closed;
         int cx = Layout.CITADEL.x();
         int z = Layout.CITADEL.z() + 32;
         int y = Layout.CITADEL.top();
         int changed = 0;

         for (int x = cx - 3; x <= cx + 3; x++) {
            for (int yy = y + 1; yy <= y + 9; yy++) {
               BlockPos p = new BlockPos(x, yy, z);
               BlockState s = level.getBlockState(p);
               if (closed && s.isAir()) {
                  level.setBlock(p, ModBlocks.SOUL_SEAL.defaultBlockState(), 3);
                  changed++;
               } else if (!closed && s.is(ModBlocks.SOUL_SEAL)) {
                  level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                  changed++;
               }
            }
         }

         if (changed != 0) {
            BlockPos gate = new BlockPos(cx, y + 3, z);
            level.playSound(null, gate, closed ? SoundEvents.IRON_DOOR_CLOSE : SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 4.0F, 0.4F);
            level.playSound(null, gate, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 3.0F, 0.5F);
            ModNet.shake(level, Vec3.atCenterOf(gate), 48.0, 0.8F, 16);
         }
      }
   }

   private static void setRift(ServerLevel level, boolean open) {
      BlockPos c = Layout.ARENA_CENTER;

      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = 0; dy < 4; dy++) {
            level.setBlock(c.offset(dx, dy, 0), open ? ModBlocks.SOUL_RIFT.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
         }
      }
   }

   private static void tickThroneMusic(ServerLevel level) {
      Director.State st = state(level);
      if (level.getGameTime() % 200L == 0L && st.progress() >= 2 && st.ending() == 0) {
         for (ServerPlayer p : level.getPlayers(px -> px.blockPosition().distSqr(Layout.ARENA_CENTER) < 10000.0 && !Endings.running(px))) {
            ModNet.music(p, 1);
         }
      }
   }

   public static void onDeathDefeated(ServerLevel level) {
      level.setAttached(STATE, state(level).rift(true).reached(2));
      setRift(level, true);
      level.getEntitiesOfClass(SoulAnchor.class, arena()).forEach(a -> a.discard());

      for (ServerPlayer p : level.getPlayers(px -> Layout.inArena(px.getX(), px.getY(), px.getZ()))) {
         ModNet.cinematic(p, 7);
         ModNet.music(p, 1);
      }
   }

   private static void tickHollow(ServerLevel level) {
      Layout.Island h = Layout.HOLLOW;
      BlockPos c = h.center();
      if (!state(level).hunterSlain() && level.isPositionEntityTicking(c)) {
         AABB ground = new AABB(c).inflate(h.radius() + 4, 30.0, h.radius() + 4);
         boolean someone = !level.getPlayers(p -> !p.isSpectator() && !p.isCreative() && ground.contains(p.position())).isEmpty();
         List<HollowHunter> hunters = level.getEntitiesOfClass(HollowHunter.class, ground.inflate(20.0));
         if (!someone) {
            if (!hunters.isEmpty()) {
               long now = level.getGameTime();
               if (hollowEmptySince < 0L) {
                  hollowEmptySince = now;
               } else if (now - hollowEmptySince > 600L) {
                  hunters.forEach(e -> e.discard());
                  hollowEmptySince = -1L;
               }
            }
         } else {
            hollowEmptySince = -1L;
            if (hunters.isEmpty()) {
               HollowHunter hunter = ModEntities.HOLLOW_HUNTER.create(level, EntitySpawnReason.EVENT);
               if (hunter != null) {
                  hunter.snapTo(h.x() + 9.5, h.top() + 8, h.z() - 6.5, 90.0F, 0.0F);
                  hunter.setPersistenceRequired();
                  level.addFreshEntity(hunter);
                  level.playSound(null, hunter.blockPosition(), SoundEvents.SKELETON_HORSE_AMBIENT, SoundSource.HOSTILE, 3.0F, 0.4F);
               }
            }
         }
      }
   }

   public static void onHunterDefeated(ServerLevel level) {
      level.setAttached(STATE, state(level).hunted());
   }

   public static void debugStory(ServerLevel level, int progress, int ending) {
      Director.State st = state(level);
      level.setAttached(
         STATE,
         new Director.State(
            st.guardReadyAt(), st.deathReadyAt(), progress >= 1, progress >= 2, progress, ending, st.hunterSlain(), progress >= 2 ? 7 : st.seals()
         )
      );
      if (progress >= 2) {
         openCitadel(level, false);
      }

      setDoor(level, progress >= 1);
      if (ending == 0) {
         level.players().forEach(p -> p.removeAttached(ModAttachments.CROWNED));
      }
   }

   public static Director.State storyState(ServerLevel level) {
      return state(level);
   }

   public static String dialogFor(ServerLevel level, String base) {
      Director.State st = state(level);
      List<String> tries = new ArrayList<>();
      if (st.ending() > 0) {
         tries.add(base + ".end" + st.ending());
      }

      if (st.progress() >= 2) {
         tries.add(base + ".slain");
      }

      if (st.progress() >= 1) {
         tries.add(base + ".warden");
      }

      Registry<Dialog> dialogs = level.registryAccess().lookupOrThrow(Registries.DIALOG);

      for (String t : tries) {
         if (dialogs.get(ResourceKey.create(Registries.DIALOG, DeathBound.id(t))).isPresent()) {
            return t;
         }
      }

      return base;
   }

   public static void chooseEnding(ServerPlayer player, String which) {
      ServerLevel level = player.level();
      Director.State st = state(level);

      int ending = switch (which) {
         case "take" -> 1;
         case "king" -> 2;
         case "break" -> 3;
         default -> 0;
      };
      if (ending != 0 && st.progress() >= 2 && level.dimension() == UnderworldTravel.UNDERWORLD) {
         if (st.ending() != 0) {
            player.sendSystemMessage(Component.translatable("message.deathbound.ending.done"));
         } else {
            level.setAttached(STATE, st.ended(ending));
            setDoor(level, true);
            HeartOfDeathItem.consume(player);
            BlockPos seat = Layout.THRONE_SEAT;
            Vec3 at = Vec3.atCenterOf(seat);
            switch (ending) {
               case 2:
                  level.getEntities(ModEntities.PROPHET, e -> true).forEach(e -> e.discard());
                  level.playSound(null, seat, SoundEvents.CHAIN_BREAK, SoundSource.NEUTRAL, 3.0F, 0.5F);
                  break;
               case 3:
                  RandomSource r = level.getRandom();

                  for (BlockPos p : BlockPos.betweenClosed(seat.offset(-3, -3, -3), seat.offset(3, 6, 3))) {
                     if (!level.getBlockState(p).isAir() && p.distSqr(seat) < 18.0 && r.nextInt(3) != 0) {
                        level.setBlock(p, r.nextInt(3) == 0 ? Blocks.GRAVEL.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
                     }
                  }

                  level.playSound(null, seat, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 3.0F, 0.6F);
                  level.playSound(null, seat, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.BLOCKS, 3.0F, 0.5F);
                  break;
               default:
                  level.playSound(null, seat, SoundEvents.BEACON_POWER_SELECT, SoundSource.NEUTRAL, 3.0F, 0.5F);
            }

            level.sendParticles(ModParticles.SOUL_FLAME, at.x, at.y + 1.0, at.z, 260, 2.0, 3.0, 2.0, 0.08);
            ModNet.shake(level, at, 64.0, 2.0F, 30);
            player.sendSystemMessage(Component.translatable("message.deathbound.ending." + which).withStyle(ChatFormatting.LIGHT_PURPLE));
            int cinematic = ending == 1 ? 9 : (ending == 2 ? 10 : 11);

            for (ServerPlayer p : level.players()) {
               if (p != player) {
                  ModNet.cinematic(p, cinematic);
               }
            }

            Endings.begin(player, ending);
         }
      }
   }

   private Director() {
   }

   private record Haunt(BlockPos at, int place) {
   }

   private record Home(EntityType<UnderworldNpc> type, BlockPos at, float yaw, boolean floats, double lift) {
      Home(EntityType<UnderworldNpc> type, BlockPos at, float yaw, boolean floats) {
         this(type, at, yaw, floats, floats ? 0.35 : 0.0);
      }
   }

   public record State(long guardReadyAt, long deathReadyAt, boolean doorOpen, boolean riftOpen, int progress, int ending, boolean hunterSlain, int seals) {
      static final Director.State FRESH = new Director.State(0L, 0L, false, false, 0, 0, false, 0);
      static final Codec<Director.State> CODEC = RecordCodecBuilder.create(
         i -> i.group(
               Codec.LONG.fieldOf("guard_ready_at").forGetter(Director.State::guardReadyAt),
               Codec.LONG.fieldOf("death_ready_at").forGetter(Director.State::deathReadyAt),
               Codec.BOOL.fieldOf("door_open").forGetter(Director.State::doorOpen),
               Codec.BOOL.fieldOf("rift_open").forGetter(Director.State::riftOpen),
               Codec.INT.optionalFieldOf("progress", 0).forGetter(Director.State::progress),
               Codec.INT.optionalFieldOf("ending", 0).forGetter(Director.State::ending),
               Codec.BOOL.optionalFieldOf("hunter_slain", false).forGetter(Director.State::hunterSlain),
               Codec.INT.optionalFieldOf("seals", 0).forGetter(Director.State::seals)
            )
            .apply(i, Director.State::new)
      );

      Director.State door(boolean open) {
         return new Director.State(this.guardReadyAt, this.deathReadyAt, open, this.riftOpen, this.progress, this.ending, this.hunterSlain, this.seals);
      }

      Director.State rift(boolean open) {
         return new Director.State(this.guardReadyAt, this.deathReadyAt, this.doorOpen, open, this.progress, this.ending, this.hunterSlain, this.seals);
      }

      Director.State reached(int stage) {
         return new Director.State(
            this.guardReadyAt, this.deathReadyAt, this.doorOpen, this.riftOpen, Math.max(this.progress, stage), this.ending, this.hunterSlain, this.seals
         );
      }

      Director.State ended(int which) {
         return new Director.State(this.guardReadyAt, this.deathReadyAt, true, this.riftOpen, this.progress, which, this.hunterSlain, this.seals);
      }

      Director.State sealed(int bit) {
         return new Director.State(
            this.guardReadyAt, this.deathReadyAt, this.doorOpen, this.riftOpen, this.progress, this.ending, this.hunterSlain, this.seals | bit
         );
      }

      boolean citadelOpen() {
         return this.seals == 7;
      }

      Director.State hunted() {
         return new Director.State(this.guardReadyAt, this.deathReadyAt, this.doorOpen, this.riftOpen, this.progress, this.ending, true, this.seals);
      }
   }
}
