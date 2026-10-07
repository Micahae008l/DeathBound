package com.deathbound.world;

import com.deathbound.block.RemainsBlock;
import com.deathbound.entity.Shade;
import com.deathbound.registry.ModBlocks;
import com.deathbound.registry.ModEntities;
import com.deathbound.registry.ModNet;
import com.deathbound.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class Dread {
   static final BlockPos BELL = new BlockPos(Layout.VILLAGE.x() + 20, Layout.VILLAGE.top() + 12, Layout.VILLAGE.z() + 14);
   private static final List<Dread.Steps> STEPS = new ArrayList<>();
   private static final Map<UUID, Layout.Theme> WHERE = new HashMap<>();

   public static void init() {
      ServerTickEvents.END_LEVEL_TICK.register(Dread::tick);
   }

   private static void tick(ServerLevel level) {
      if (level.dimension() == UnderworldTravel.UNDERWORLD && !level.players().isEmpty()) {
         RandomSource r = level.getRandom();
         if (r.nextInt(4800) == 0) {
            level.playSound(null, BELL, ModSounds.TOLL, SoundSource.AMBIENT, 14.0F, 0.5F);
         }

         stepTick(r);
         if (level.getGameTime() % 10L == 0L) {
            for (ServerPlayer p : level.players()) {
               if (!p.isSpectator() && !Director.isInBossFight(p)) {
                  unrest(level, p, r);
                  announce(p);
                  if (r.nextInt(450) == 0) {
                     Vec3 back = p.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale(-3.5);
                     STEPS.add(new Dread.Steps(p, p.position().add(back), 3 + r.nextInt(3), 0));
                  }

                  if (r.nextInt(700) == 0) {
                     double a = r.nextDouble() * 3.141592653589793 * 2.0;
                     whisperTo(
                        p, ModSounds.WAIL, p.position().add(Math.cos(a) * 28.0, 6 + r.nextInt(10), Math.sin(a) * 28.0), 0.9F, 0.5F + r.nextFloat() * 0.15F, r
                     );
                  }

                  if (r.nextInt(800) == 0) {
                     shade(level, p, r);
                  }
               }
            }
         }
      }
   }

   private static void unrest(ServerLevel level, ServerPlayer p, RandomSource r) {
      MutableBlockPos m = new MutableBlockPos();
      Vec3 eye = p.getEyePosition();
      Vec3 look = p.getLookAngle();

      for (int i = 0; i < 40; i++) {
         int dx = r.nextInt(25) - 12;
         int dy = r.nextInt(9) - 4;
         int dz = r.nextInt(25) - 12;
         m.set(p.getBlockX() + dx, p.getBlockY() + dy, p.getBlockZ() + dz);
         BlockState s = level.getBlockState(m);
         if (s.getBlock() instanceof RemainsBlock && !s.getValue(RemainsBlock.TWITCH)) {
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (d > 2.5 && d < 9.0 && r.nextInt(5) == 0) {
               RemainsBlock.twitch(level, m.immutable(), s, r);
            }
         } else if (s.is(ModBlocks.SKULL_SPIKE) || s.is(ModBlocks.SKULL)) {
            Vec3 to = Vec3.atCenterOf(m).subtract(eye);
            if (look.dot(to.normalize()) < 0.25) {
               Direction face = Decor.toward(-to.x, -to.z);
               if (s.getValue(HorizontalDirectionalBlock.FACING) != face) {
                  level.setBlock(m, s.setValue(HorizontalDirectionalBlock.FACING, face), 2);
               }
            }
         }
      }
   }

   static void shade(ServerLevel level, ServerPlayer p, RandomSource r) {
      Vec3 look = p.getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
      double a = Math.atan2(look.z, look.x) + 3.141592653589793 + (r.nextDouble() - 0.5) * 1.8;
      double d = 20.0 + r.nextDouble() * 10.0;
      int x = Mth.floor(p.getX() + Math.cos(a) * d);
      int z = Mth.floor(p.getZ() + Math.sin(a) * d);
      MutableBlockPos m = new MutableBlockPos(x, p.getBlockY() + 8, z);
      int i = 0;

      while (i < 24) {
         if (level.getBlockState(m).isFaceSturdy(level, m, Direction.UP)
            && level.getBlockState(m.above()).isAir()
            && level.getBlockState(m.above(2)).isAir()
            && level.getBlockState(m.above(3)).isAir()) {
            Shade s = ModEntities.SHADE.create(level, EntitySpawnReason.EVENT);
            if (s != null) {
               s.snapTo(x + 0.5, m.getY() + 1, z + 0.5, 0.0F, 0.0F);
               s.watch(p);
               level.addFreshEntity(s);
            }

            return;
         }

         i++;
         m.move(Direction.DOWN);
      }
   }

   private static void stepTick(RandomSource r) {
      for (int i = STEPS.size() - 1; i >= 0; i--) {
         Dread.Steps s = STEPS.get(i);
         if (s.player.isRemoved() || s.left <= 0) {
            STEPS.remove(i);
         } else if (s.delay > 0) {
            STEPS.set(i, new Dread.Steps(s.player, s.at, s.left, s.delay - 1));
         } else {
            Vec3 at = s.at.add(s.player.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale(0.35));
            whisperTo(s.player, ModSounds.FOOTSTEP, at, 0.55F, 0.85F + r.nextFloat() * 0.1F, r);
            STEPS.set(i, new Dread.Steps(s.player, at, s.left - 1, 7 + r.nextInt(4)));
         }
      }
   }

   private static void announce(ServerPlayer p) {
      Layout.Theme here = null;

      for (Layout.Island is : Layout.ISLANDS) {
         double dx = p.getX() - is.x();
         double dz = p.getZ() - is.z();
         if (is.theme() != Layout.Theme.DEBRIS
            && dx * dx + dz * dz < (is.radius() + 3.0) * (is.radius() + 3.0)
            && p.getY() > is.top() - 30
            && p.getY() < is.top() + 60) {
            here = is.theme();
         }
      }

      if (!Endings.running(p) && !ModNet.recentlyTitled(p, 140)) {
         Layout.Theme was = WHERE.put(p.getUUID(), here);
         if (here != null && here != was) {
            String key = "area.deathbound." + here.name().toLowerCase(Locale.ROOT);
            p.connection.send(new ClientboundSetTitlesAnimationPacket(15, 60, 25));
            p.connection
               .send(new ClientboundSetSubtitleTextPacket(Component.translatable(key + ".line").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            p.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(key).withColor(12167129)));
         }
      }
   }

   public static void whisperTo(ServerPlayer p, SoundEvent sound, Vec3 at, float volume, float pitch, RandomSource r) {
      p.connection
         .send(
            new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), SoundSource.HOSTILE, at.x, at.y, at.z, volume, pitch, r.nextLong())
         );
   }

   private Dread() {
   }

   private record Steps(ServerPlayer player, Vec3 at, int left, int delay) {
   }
}
