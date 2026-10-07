package com.deathbound.world;

import com.deathbound.DeathBound;
import com.deathbound.block.GraveLampBlock;
import com.deathbound.block.WatcherSkullBlock;
import com.deathbound.registry.ModBlocks;
import com.deathbound.registry.ModItems;
import com.deathbound.registry.ModParticles;
import com.deathbound.registry.ModSounds;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class Puzzles {
   public static final int KINGS = 0;
   public static final int WATCHERS = 1;
   public static final int BELL = 2;
   static final int BELL_TOLLS = 4;
   static final int TOMB_Y = Layout.CRYPT.top() - 9;
   static final int TOMB_Z = Layout.CRYPT.z() - 6;
   static final BlockPos[] LAMPS = new BlockPos[]{
      new BlockPos(Layout.CRYPT.x() - 10, TOMB_Y + 1, TOMB_Z),
      new BlockPos(Layout.CRYPT.x() - 6, TOMB_Y + 1, TOMB_Z),
      new BlockPos(Layout.CRYPT.x() + 6, TOMB_Y + 1, TOMB_Z),
      new BlockPos(Layout.CRYPT.x() + 10, TOMB_Y + 1, TOMB_Z)
   };
   static final Vec3 KING_TOMB = new Vec3(Layout.CRYPT.x() + 6.5, TOMB_Y + 0.6, Layout.CRYPT.z() - 4.5);
   static final int WATCH_Y = Layout.SPIRE.top() + 42;
   static final Direction[] WINDOWS = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
   static final Vec3 SPIRE_TOP = new Vec3(Layout.SPIRE.x() + 0.5, Layout.SPIRE.top() + 41.4, Layout.SPIRE.z() + 0.5);
   static final BlockPos BELL_TOWER = new BlockPos(Layout.VILLAGE.x() + 18, Layout.VILLAGE.top(), Layout.VILLAGE.z() + 14);
   public static final int GATE_Z = Layout.CITADEL.z() + 34;
   static final BlockPos GATE = new BlockPos(Layout.CITADEL.x(), Layout.CITADEL.top() + 1, GATE_Z + 4);
   private static int tolls;
   private static long lastToll;
   private static BlockPos tolled = BlockPos.ZERO;
   private static final Map<UUID, Map<String, Long>> TOLD = new HashMap<>();
   /** Bit per puzzle kind: its sigil has already been given out in this world (saved with the level). */
   private static final AttachmentType<Integer> REVEALED = AttachmentRegistry.create(
      DeathBound.id("sigils_revealed"), b -> b.persistent(Codec.INT)
   );

   public static void init() {
      ServerTickEvents.END_LEVEL_TICK.register(Puzzles::tick);
      PlayerBlockBreakEvents.BEFORE
         .register(
            (level, player, pos, state, be) -> {
               if (state.getBlock() instanceof WallSignBlock
                  && level.dimension() == UnderworldTravel.UNDERWORLD
                  && Math.abs(pos.getY() - TOMB_Y) <= 2
                  && Math.abs(pos.getZ() - (TOMB_Z + 2)) <= 1
                  && Math.abs(pos.getX() - Layout.CRYPT.x()) <= 12) {
                  player.sendOverlayMessage(Component.translatable("puzzle.deathbound.names_deep").withStyle(ChatFormatting.GRAY));
                  return false;
               } else {
                  return true;
               }
            }
         );
   }

   public static Item sigil(int kind) {
      return switch (kind) {
         case 0 -> ModItems.SIGIL_KINGS;
         case 1 -> ModItems.SIGIL_WATCHERS;
         default -> ModItems.SIGIL_BELL;
      };
   }

   static BlockPos window(Direction d) {
      return new BlockPos(Layout.SPIRE.x() + d.getStepX() * 3, WATCH_Y, Layout.SPIRE.z() + d.getStepZ() * 3);
   }

   public static void onLampLit(ServerLevel level, BlockPos pos, Player player) {
      int idx = Arrays.asList(LAMPS).indexOf(pos);
      if (idx >= 0) {
         boolean right = idx < 3;

         for (int j = 0; j < LAMPS.length && right; j++) {
            if (j != idx && lit(level, LAMPS[j]) != j < idx) {
               right = false;
            }
         }

         if (!right) {
            for (BlockPos p : LAMPS) {
               BlockState s = level.getBlockState(p);
               if (s.is(ModBlocks.GRAVE_LAMP)) {
                  level.setBlock(p, s.setValue(GraveLampBlock.LIT, false), 3);
               }
            }

            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.2F, 0.5F);
            level.playSound(null, pos, ModSounds.WHISPER, SoundSource.HOSTILE, 1.0F, 0.6F);
            tell(player, idx == 3 ? "lamps_nameless" : "lamps_wrong");
         } else {
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.4F, 0.7F + idx * 0.15F);
            player.sendOverlayMessage(Component.translatable("puzzle.deathbound.lamp_ok" + idx).withStyle(ChatFormatting.LIGHT_PURPLE));
            if (idx == 2) {
               reveal(level, KING_TOMB, 0, player);
            }
         }
      }
   }

   private static boolean lit(ServerLevel level, BlockPos p) {
      BlockState s = level.getBlockState(p);
      return s.is(ModBlocks.GRAVE_LAMP) && s.getValue(GraveLampBlock.LIT);
   }

   public static void onSkullTurned(ServerLevel level, BlockPos pos, Player player) {
      int out = 0;

      for (Direction d : WINDOWS) {
         BlockState s = level.getBlockState(window(d));
         if (s.getBlock() instanceof WatcherSkullBlock && s.getValue(HorizontalDirectionalBlock.FACING) == d) {
            out++;
         }
      }

      BlockState turned = level.getBlockState(pos);
      boolean thisOne = false;

      for (Direction d : WINDOWS) {
         thisOne |= window(d).equals(pos) && turned.getValue(HorizontalDirectionalBlock.FACING) == d;
      }

      if (thisOne) {
         level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2F, 0.8F + out * 0.1F);
      }

      player.sendOverlayMessage(
         Component.translatable(thisOne ? "puzzle.deathbound.skull_out" : "puzzle.deathbound.skull_in", out).withStyle(ChatFormatting.LIGHT_PURPLE)
      );
      if (out == WINDOWS.length) {
         reveal(level, SPIRE_TOP, 1, player);
      }
   }

   public static void onBell(ServerLevel level, BlockPos pos) {
      long now = level.getGameTime();
      if (now - lastToll > 80L) {
         tolls = 0;
      }

      if (now - lastToll >= 6L) {
         tolls++;
         lastToll = now;
         tolled = pos;

         for (ServerPlayer p : level.getPlayers(px -> px.blockPosition().distSqr(pos) < 576.0)) {
            p.sendOverlayMessage(Component.translatable("puzzle.deathbound.toll", tolls).withStyle(ChatFormatting.LIGHT_PURPLE));
         }
      }
   }

   private static void tick(ServerLevel level) {
      if (level.dimension() == UnderworldTravel.UNDERWORLD && !level.players().isEmpty()) {
         long now = level.getGameTime();
         if (tolls > 0 && now - lastToll > 50L) {
            Player near = level.getNearestPlayer(tolled.getX(), tolled.getY(), tolled.getZ(), 24.0, false);
            if (tolls == 4) {
               reveal(level, Vec3.atCenterOf(tolled.below(2)), 2, near);
            } else if (near != null) {
               tell(near, "bell_wrong");
            }

            tolls = 0;
         }

         if (now % 20L == 0L) {
            for (ServerPlayer p : level.players()) {
               clues(level, p, now);
            }
         }
      }
   }

   public static void onLockFilled(ServerLevel level, BlockPos pos, int kind, Player player) {
      level.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.6F, 0.6F);
      level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.4F, 0.5F);
      level.sendParticles(ModParticles.SOUL_FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 1.0, 40, 0.3, 0.3, 0.1, 0.02);
      Director.onSealFilled(level, kind);
      int n = Integer.bitCount(Director.seals(level));
      if (n < 3) {
         player.sendSystemMessage(Component.translatable("puzzle.deathbound.lock_filled", n).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
      }
   }

   private static void reveal(ServerLevel level, Vec3 at, int kind, Player player) {
      // Each puzzle gives its sigil once. Re-solving it (turning a skull back, re-lighting the lamps, tolling again)
      // must not drop another - unless the seal is still empty and the sigil is truly gone (e.g. fell into the void).
      int bit = 1 << kind;
      int revealed = level.getAttachedOrElse(REVEALED, 0);
      boolean sealed = (Director.seals(level) & bit) != 0;
      int regranted = bit << 4;   // a lost sigil is given again at most once
      if (sealed || (revealed & bit) != 0 && ((revealed & regranted) != 0 || sigilStillExists(level, sigil(kind)))) {
         level.playSound(null, at.x, at.y, at.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, 0.5F);
         if (player != null) {
            tell(player, "already_solved");
         }
         return;
      }

      level.setAttached(REVEALED, revealed | bit | ((revealed & bit) != 0 ? regranted : 0));
      ItemEntity item = new ItemEntity(level, at.x, at.y, at.z, new ItemStack(sigil(kind)));
      item.setDeltaMovement(0.0, 0.03, 0.0);
      item.setNoGravity(true);
      item.setPickUpDelay(30);
      item.setUnlimitedLifetime();
      level.addFreshEntity(item);
      level.sendParticles(ModParticles.SOUL_FLAME, at.x, at.y, at.z, 80, 0.4, 0.6, 0.4, 0.04);
      level.playSound(null, at.x, at.y, at.z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.6F, 0.6F);
      level.playSound(null, at.x, at.y, at.z, ModSounds.WHISPER, SoundSource.HOSTILE, 1.2F, 0.7F);
      if (player != null) {
         tell(player, "solved" + kind);
      }
   }

   /** Is this sigil still in someone's inventory/ender chest or lying around as an item? */
   private static boolean sigilStillExists(ServerLevel level, Item sigil) {
      for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
         if (p.getInventory().contains(st -> st.is(sigil)) || p.getEnderChestInventory().hasAnyMatching(st -> st.is(sigil))
            || p.containerMenu.getCarried().is(sigil)) {
            return true;
         }
      }
      for (ServerLevel l : level.getServer().getAllLevels()) {
         if (!l.getEntities(EntityTypes.ITEM, e -> e.getItem().is(sigil)).isEmpty()) {
            return true;
         }
      }
      return false;
   }

   private static void tell(Player player, String key) {
      player.sendSystemMessage(Component.translatable("puzzle.deathbound." + key).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
   }

   private static void clues(ServerLevel level, ServerPlayer p, long now) {
      clue(p, now, "clue_lamps", new Vec3(Layout.CRYPT.x() + 0.5, TOMB_Y, Layout.CRYPT.z() - 3.5), 9.5, 4.0);
      clue(p, now, "clue_watchers", SPIRE_TOP, 4.5, 3.0);
      clue(p, now, "clue_bell", new Vec3(BELL_TOWER.getX() + 0.5, p.getY(), BELL_TOWER.getZ() + 0.5), 6.0, 99.0);
      if (Director.seals(level) != 7) {
         clue(p, now, "clue_gate", Vec3.atCenterOf(GATE), 8.0, 8.0);
      }

      for (BlockPos lamp : LAMPS) {
         hint(p, now, "hint_lamp", lamp, level.getBlockState(lamp).is(ModBlocks.GRAVE_LAMP) && !lit(level, lamp));
      }

      for (Direction d : WINDOWS) {
         BlockState s = level.getBlockState(window(d));
         hint(p, now, "hint_skull", window(d), s.getBlock() instanceof WatcherSkullBlock && s.getValue(HorizontalDirectionalBlock.FACING) != d);
      }
   }

   private static void hint(ServerPlayer p, long now, String key, BlockPos at, boolean needed) {
      if (needed && p.blockPosition().distSqr(at) < 9.0) {
         Map<String, Long> told = TOLD.computeIfAbsent(p.getUUID(), u -> new HashMap<>());
         if (now - told.getOrDefault(key, -100000L) > 400L) {
            told.put(key, now);
            p.sendOverlayMessage(Component.translatable("puzzle.deathbound." + key).withStyle(ChatFormatting.GRAY));
         }
      }
   }

   private static void clue(ServerPlayer p, long now, String key, Vec3 at, double r, double dy) {
      if (!(Math.abs(p.getY() - at.y) > dy) && !(p.position().multiply(1.0, 0.0, 1.0).distanceTo(at.multiply(1.0, 0.0, 1.0)) > r)) {
         Map<String, Long> told = TOLD.computeIfAbsent(p.getUUID(), u -> new HashMap<>());
         if (now - told.getOrDefault(key, -100000L) > 3600L) {
            told.put(key, now);
            p.sendSystemMessage(Component.translatable("puzzle.deathbound." + key).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            p.level().playSound(null, p.blockPosition(), ModSounds.WHISPER, SoundSource.AMBIENT, 0.5F, 0.8F);
         }
      }
   }

   private Puzzles() {
   }
}
