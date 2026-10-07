package com.deathbound.world;

import com.deathbound.DeathBound;
import com.deathbound.entity.DeathEntity;
import com.deathbound.entity.DeathsGuard;
import com.deathbound.entity.Gravebound;
import com.deathbound.entity.LostSoul;
import com.deathbound.item.HeartOfDeathItem;
import com.deathbound.npc.UnderworldNpc;
import com.deathbound.registry.ModAttachments;
import com.deathbound.registry.ModBlocks;
import com.deathbound.registry.ModEntities;
import com.deathbound.registry.ModItems;
import com.deathbound.registry.ModNet;
import com.deathbound.registry.ModParticles;
import com.deathbound.registry.ModSounds;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class Endings {
   public static final int TAKE = 1;
   public static final int KING = 2;
   public static final int BREAK = 3;
   public static final int FALLEN = 4;
   static final int[][] CUTS = new int[][]{new int[0], {300}, {80, 200, 380, 480, 590}, {60, 230}, {130}};
   private static final List<Endings.Run> RUNS = new ArrayList<>();
   private static final List<Endings.Fall> FALLS = new ArrayList<>();
   private static final Vec3 SEAT = new Vec3(Layout.THRONE_SEAT.getX() + 0.5, Layout.THRONE_SEAT.getY() + 0.5, Layout.THRONE_SEAT.getZ() + 0.5);
   private static final double FALL_SPEED = 0.16;
   private static final double FALL_LEASH = 22.0;
   private static final int FALL_GRACE = 70;
   private static final double FALL_END = Layout.ARRIVAL.z() - Layout.ARRIVAL.radius() - 3;
   private static final Identifier WILD = DeathBound.id("wild");

   public static boolean running(ServerPlayer p) {
      return RUNS.stream().anyMatch(r -> r.player == p) || FALLS.stream().anyMatch(f -> f.player == p);
   }

   public static void init() {
      ServerTickEvents.END_LEVEL_TICK.register(Endings::tick);
      UseBlockCallback.EVENT
         .register(
            (player, level, hand, hit) -> {
               if (level instanceof ServerLevel server
                  && player instanceof ServerPlayer sp
                  && server.dimension() == UnderworldTravel.UNDERWORLD
                  && player.getItemInHand(hand).getItem() instanceof HeartOfDeathItem
                  && hit.getBlockPos().distSqr(Layout.THRONE_SEAT) < 25.0) {
                  Director.State st = Director.storyState(server);
                  if (st.progress() >= 2 && st.ending() == 0) {
                     server.registryAccess()
                        .lookupOrThrow(Registries.DIALOG)
                        .get(ResourceKey.create(Registries.DIALOG, DeathBound.id("throne")))
                        .ifPresent(sp::openDialog);
                     return InteractionResult.SUCCESS;
                  }
               }

               return InteractionResult.PASS;
            }
         );
      ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
         if (entity instanceof Gravebound g && level.dimension() == UnderworldTravel.UNDERWORLD && Director.storyState(level).ending() == 3) {
            wild(g);
         }
      });
   }

   static void begin(ServerPlayer p, int which) {
      p.level().getEntitiesOfClass(DeathEntity.class, new AABB(Layout.THRONE_SEAT).inflate(80.0)).forEach(e -> e.discard());
      RUNS.removeIf(r -> r.player == p);
      RUNS.add(new Endings.Run(p, which, p.level().getGameTime()));
      int length = CUTS[which][CUTS[which].length - 1] + 10;
      p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, length, 255, false, false));
      p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, length, 255, false, false));
      if (which != 1) {
         p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, length, 0, false, false));
      }

      ModNet.cutscene(p, which);
      ModNet.music(p, 0);
   }

   private static void tick(ServerLevel level) {
      if (level.dimension() == UnderworldTravel.UNDERWORLD) {
         long now = level.getGameTime();

         for (int i = RUNS.size() - 1; i >= 0; i--) {
            Endings.Run r = RUNS.get(i);
            r.t = (int)(now - r.start);
            int t = r.t;
            if (!r.player.isRemoved() && r.player.level() == level) {
               switch (r.which) {
                  case 1:
                     take(level, r.player, r, t);
                     break;
                  case 2:
                     king(level, r.player, r, t);
                     break;
                  case 3:
                     breaking(level, r.player, r, t);
                     break;
                  case 4:
                     fallen(level, r.player, r, t);
               }

               r.last = t;
               int[] cuts = CUTS[r.which];
               if (t >= cuts[cuts.length - 1]) {
                  RUNS.remove(i);
                  r.player.removeEffect(MobEffects.INVISIBILITY);
                  r.player.removeEffect(MobEffects.NIGHT_VISION);
                  if (r.which == 3) {
                     startFall(level, r.player);
                  } else {
                     ModNet.cinematic(r.player, r.which == 1 ? 9 : (r.which == 2 ? 10 : 11));
                  }
               }
            } else {
               RUNS.remove(i);
            }
         }

         for (int i = FALLS.size() - 1; i >= 0; i--) {
            if (fall(level, FALLS.get(i))) {
               FALLS.remove(i);
            }
         }

         lasting(level, Director.storyState(level).ending());
      }
   }

   private static void put(ServerPlayer p, double x, double y, double z, float yaw) {
      boolean far = new Vec3(x, y, z).distanceTo(SEAT) > 40.0;
      if (far) {
         p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
      } else {
         p.removeEffect(MobEffects.NIGHT_VISION);
      }

      p.teleportTo(p.level(), x, y, z, Set.of(), yaw, 0.0F, true);
   }

   private static double ground(ServerLevel level, double x, double z, int fromY) {
      MutableBlockPos m = new MutableBlockPos(Mth.floor(x), fromY, Mth.floor(z));

      for (int y = fromY; y > fromY - 24; y--) {
         m.setY(y);
         if (level.getBlockState(m.below()).isFaceSturdy(level, m.below(), Direction.UP)
            && level.getBlockState(m).getCollisionShape(level, m).isEmpty()
            && level.getBlockState(m.above()).getCollisionShape(level, m.above()).isEmpty()) {
            return y;
         }
      }

      return fromY;
   }

   private static void title(ServerPlayer p, String key) {
      ModNet.bigTitle(p);
      p.connection.send(new ClientboundSetTitlesAnimationPacket(6, 50, 16));
      p.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(key + ".sub").withStyle(ChatFormatting.GRAY)));
      p.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(key).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD)));
   }

   private static void take(ServerLevel level, ServerPlayer p, Endings.Run r, int t) {
      if (r.at(0)) {
         put(p, SEAT.x, SEAT.y, SEAT.z, 0.0F);
         p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 320, 0, false, false));
         level.playSound(null, Layout.THRONE_SEAT, SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 3.0F, 0.5F);
      }

      if (t >= 20 && t < 200 && t % 3 == 0) {
         double a = t * 0.35;
         double rad = 3.2 - (t - 20) / 180.0 * 2.4;

         for (int k = 0; k < 3; k++) {
            double b = a + k * 3.141592653589793 * 2.0 / 3.0;
            level.sendParticles(
               ModParticles.SOUL_FLAME, SEAT.x + Math.cos(b) * rad, SEAT.y + 0.3 + (t - 20) / 180.0 * 2.0, SEAT.z + Math.sin(b) * rad, 2, 0.05, 0.05, 0.05, 0.0
            );
         }

         if (t % 21 == 0) {
            level.playSound(null, Layout.THRONE_SEAT, SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 1.2F, 0.4F + t / 300.0F);
         }
      }

      if (r.at(120)) {
         p.setAttached(ModAttachments.CROWNED, level.getGameTime());
         level.playSound(null, Layout.THRONE_SEAT, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 2.0F, 0.5F);
         level.sendParticles(ModParticles.SOUL_FLAME, SEAT.x, SEAT.y + 1.2, SEAT.z, 200, 0.6, 1.0, 0.6, 0.12);
         ModNet.shake(level, SEAT, 40.0, 2.0F, 20);
      }

      if (r.at(170)) {
         DeathsGuard warden = ModEntities.DEATHS_GUARD.create(level, EntitySpawnReason.EVENT);
         if (warden != null) {
            warden.snapTo(SEAT.x, Layout.CITADEL.top() + 1, SEAT.z + 9.5, 180.0F, 0.0F);
            warden.setYHeadRot(180.0F);
            warden.setYBodyRot(180.0F);
            warden.kneel();
            level.addFreshEntity(warden);
            level.playSound(null, warden.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.5F, 0.4F);
            level.sendParticles(ModParticles.SOUL_FLAME, SEAT.x, Layout.CITADEL.top() + 1.2, SEAT.z + 9.5, 120, 1.2, 0.2, 1.2, 0.03);
         }
      }

      if (t > 200 && t < 300 && t % 4 == 0) {
         for (DeathsGuard w : level.getEntitiesOfClass(
            DeathsGuard.class, new AABB(SEAT.x, SEAT.y, SEAT.z + 9.5, SEAT.x, SEAT.y, SEAT.z + 9.5).inflate(3.0, 6.0, 3.0)
         )) {
            level.sendParticles(ModParticles.SOUL_FLAME, w.getX(), w.getY() + 0.1, w.getZ(), 6, 0.9, 0.05, 0.9, 0.01);
         }
      }
   }

   private static UnderworldNpc king(ServerLevel level) {
      List<UnderworldNpc> there = level.getEntitiesOfClass(
         UnderworldNpc.class, new AABB(Layout.THRONE_SEAT).inflate(4.0), n -> n.kind() == UnderworldNpc.Kind.PROPHET
      );
      return there.isEmpty() ? null : there.getFirst();
   }

   private static void king(ServerLevel level, ServerPlayer p, Endings.Run r, int t) {
      if (r.at(0)) {
         level.sendParticles(ParticleTypes.END_ROD, SEAT.x, SEAT.y + 1.0, SEAT.z, 120, 0.5, 1.2, 0.5, 0.05);
      }

      if (r.at(80)) {
         p.removeEffect(MobEffects.INVISIBILITY);
         put(p, SEAT.x, ground(level, SEAT.x, SEAT.z + 4.5, (int)SEAT.y + 1), SEAT.z + 4.5, 180.0F);
         p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 120, 0, false, false));
         level.playSound(null, Layout.THRONE_SEAT, ModSounds.WHISPER, SoundSource.NEUTRAL, 2.0F, 0.6F);
      }

      if (t > 95 && t < 165) {
         Vec3 from = p.position().add(0.0, 1.1, 0.0);
         Vec3 to = SEAT.add(0.0, 1.3, 0.3);

         for (int k = 0; k < 3; k++) {
            double f = ((t - 95) * 0.06 + k / 3.0) % 1.0;
            Vec3 q = from.lerp(to, f).add(0.0, Math.sin(f * 3.141592653589793) * 0.5, 0.0);
            level.sendParticles(ModParticles.SOUL_FLAME, q.x, q.y, q.z, 2, 0.04, 0.04, 0.04, 0.0);
         }

         if (t % 12 == 0) {
            level.playSound(null, p.blockPosition(), SoundEvents.CHAIN_STEP, SoundSource.PLAYERS, 1.2F, 0.5F);
         }
      }

      if (r.at(160)) {
         takeChain(p);
         level.playSound(null, p.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 2.0F, 0.6F);
         level.playSound(null, Layout.THRONE_SEAT, SoundEvents.CHAIN_BREAK, SoundSource.NEUTRAL, 2.0F, 0.5F);
         level.sendParticles(ModParticles.SOUL_FLAME, SEAT.x, SEAT.y + 1.3, SEAT.z + 0.3, 60, 0.3, 0.3, 0.3, 0.05);
         UnderworldNpc king = king(level);
         if (king != null) {
            king.setCorruption(0.04F);
         }
      }

      if (r.at(200)) {
         p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 400, 0, false, false));
         put(p, 0.5, Layout.GATE.top() + 1, Layout.DOOR_Z + 20.5, 180.0F);
         judge(level);

         for (int i = 0; i < 8; i++) {
            LostSoul soul = ModEntities.LOST_SOUL.create(level, EntitySpawnReason.EVENT);
            if (soul != null) {
               soul.snapTo(0.5 + (i % 2 == 0 ? -0.4 : 0.4), Layout.GATE.top() + 1, Layout.GUARD_POST.getZ() + 3.5 + i * 1.6, 180.0F, 0.0F);
               level.addFreshEntity(soul);
            }
         }
      }

      if (t > 200 && t < 380 && t % 14 == 0) {
         judgeNext(level);
      }

      if (t > 200 && t < 480) {
         refillLake(level, 90);
      }

      if (r.at(380)) {
         put(p, Layout.CITADEL.x() + 2.5, Layout.CITADEL.top() + 1, Layout.CITADEL.z() + 42.5, 135.0F);
      }

      if (t > 380 && t < 480 && t % 2 == 0) {
         RandomSource rng = level.getRandom();

         for (int k = 0; k < 8; k++) {
            double a = 0.2 + rng.nextDouble() * 1.2;
            double d = 37.0 + rng.nextDouble() * 13.0;
            level.sendParticles(
               ParticleTypes.END_ROD,
               Layout.CITADEL.x() + Math.cos(a) * d,
               Layout.CITADEL.top() - 1.5,
               Layout.CITADEL.z() + Math.sin(a) * d,
               1,
               0.2,
               0.0,
               0.2,
               0.02
            );
         }
      }

      if (r.at(480)) {
         put(p, SEAT.x, ground(level, SEAT.x, SEAT.z + 6.5, (int)SEAT.y + 1), SEAT.z + 6.5, 180.0F);
      }

      if (t >= 480) {
         UnderworldNpc king = king(level);
         if (king != null) {
            king.setCorruption(0.04F + Math.min(1.0F, (t - 480) / 150.0F) * 0.5F);
         }

         if (t == 500 || t == 545 || t == 600) {
            level.playSound(null, Layout.THRONE_SEAT, ModSounds.WHISPER, SoundSource.NEUTRAL, 2.5F, 0.5F);
         }

         if (t % 3 == 0) {
            level.sendParticles(ModParticles.SOUL_FLAME, SEAT.x, SEAT.y + 1.2, SEAT.z + 0.4, 1, 0.25, 0.15, 0.1, 0.0);
         }
      }
   }

   static void takeChain(ServerPlayer p) {
      Inventory inv = p.getInventory();

      for (int i = 0; i < inv.getContainerSize(); i++) {
         if (inv.getItem(i).is(ModItems.DEATHBOUND_RELIC)) {
            inv.setItem(i, new ItemStack(ModItems.SPENT_CHAIN));
         }
      }

      p.sendSystemMessage(Component.translatable("message.deathbound.ending.price").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
   }

   static DeathsGuard judge(ServerLevel level) {
      List<DeathsGuard> there = level.getEntitiesOfClass(DeathsGuard.class, new AABB(Layout.GUARD_POST).inflate(6.0));
      if (!there.isEmpty()) {
         return there.getFirst();
      }

      DeathsGuard warden = ModEntities.DEATHS_GUARD.create(level, EntitySpawnReason.EVENT);
      if (warden != null) {
         warden.snapTo(Layout.GUARD_POST.getX() + 0.5, Layout.GUARD_POST.getY(), Layout.GUARD_POST.getZ() + 0.5, 0.0F, 0.0F);
         warden.setNoAi(true);
         warden.setPersistenceRequired();
         level.addFreshEntity(warden);
      }

      return warden;
   }

   static void judgeNext(ServerLevel level) {
      level.getEntitiesOfClass(LostSoul.class, new AABB(Layout.GUARD_POST).inflate(4.0, 3.0, 4.0), LostSoul::awaitingJudgement)
         .stream()
         .findFirst()
         .ifPresent(s -> s.judge(level.getRandom().nextFloat() < 0.7F));
   }

   static boolean refillLake(ServerLevel level, int budget) {
      Layout.Island is = Layout.CITADEL;
      RandomSource r = level.getRandom();
      int filled = 0;
      int top = is.top();

      for (int i = 0; i < budget * 4 && filled < budget; i++) {
         double a = r.nextDouble() * 3.141592653589793 * 2.0;
         double d = 36.0 + r.nextDouble() * (is.radius() - 38);
         int x = is.x() + (int)Math.round(Math.cos(a) * d);
         int z = is.z() + (int)Math.round(Math.sin(a) * d);
         if (z <= is.z() || Math.abs(x - is.x()) > 4) {
            MutableBlockPos m = new MutableBlockPos(x, top - 1, z);
            if (level.isLoaded(m)) {
               for (int y = top - 1; y > top - 8; y--) {
                  m.setY(y);
                  BlockState s = level.getBlockState(m);
                  if (!s.isAir() && (!s.canBeReplaced() || s.liquid())) {
                     if (!s.liquid()) {
                        break;
                     }
                  } else {
                     BlockState below = level.getBlockState(m.below());
                     if (!below.isAir()) {
                        level.setBlock(m, ModBlocks.SOULWATER.defaultBlockState(), 2);
                        filled++;
                        break;
                     }
                  }
               }
            }
         }
      }

      return filled > 0;
   }

   private static void breaking(ServerLevel level, ServerPlayer p, Endings.Run r, int t) {
      if (r.at(60)) {
         put(p, 0.5, Layout.GATE.top() + 1, Layout.DOOR_Z + 22.5, 180.0F);
         DeathsGuard warden = judge(level);
         if (warden != null) {
            warden.setYRot(0.0F);
            warden.setYBodyRot(0.0F);
         }
      }

      if (t >= 80 && t < 200) {
         RandomSource rng = level.getRandom();
         int band = Layout.GATE.top() + 31 - (t - 80) / 4;

         for (int k = 0; k < 14; k++) {
            BlockPos q = new BlockPos(rng.nextInt(33) - 16, band - rng.nextInt(5), Layout.DOOR_Z - 1 + rng.nextInt(3));
            BlockState s = level.getBlockState(q);
            if (!s.isAir() && s.getDestroySpeed(level, q) >= 0.0F && !(s.getBlock() instanceof EntityBlock)) {
               FallingBlockEntity.fall(level, q, s);
               if (k % 3 == 0) {
                  level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, q.getX() + 0.5, q.getY() + 0.5, q.getZ() + 0.5, 2, 0.4, 0.4, 0.4, 0.01);
               }
            }
         }

         if (t % 10 == 0) {
            level.playSound(null, Layout.SOUL_DESTINATION, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 2.0F, 0.4F);
            ModNet.shake(level, Vec3.atCenterOf(Layout.SOUL_DESTINATION), 60.0, 1.2F, 10);
         }
      }

      if (t >= 140 && t < 228) {
         for (DeathsGuard w : level.getEntitiesOfClass(DeathsGuard.class, new AABB(Layout.GUARD_POST).inflate(30.0))) {
            w.setPos(w.getX(), w.getY(), w.getZ() + 0.07);
            w.setYRot(0.0F);
            w.setYBodyRot(0.0F);
         }
      }

      if (r.at(228)) {
         for (DeathsGuard w : level.getEntitiesOfClass(DeathsGuard.class, new AABB(Layout.GUARD_POST).inflate(30.0))) {
            level.sendParticles(ParticleTypes.END_ROD, w.getX(), w.getY() + 1.5, w.getZ(), 80, 0.6, 1.2, 0.6, 0.05);
            w.discard();
         }
      }
   }

   private static void startFall(ServerLevel level, ServerPlayer p) {
      p.removeEffect(MobEffects.SLOWNESS);
      p.removeEffect(MobEffects.RESISTANCE);
      put(p, SEAT.x, ground(level, SEAT.x, SEAT.z + 5.5, (int)SEAT.y + 2), SEAT.z + 5.5, 0.0F);
      p.removeEffect(MobEffects.NIGHT_VISION);
      int gy = Layout.GATE.top();

      for (BlockPos q : BlockPos.betweenClosed(-4, gy + 1, Layout.DOOR_Z - 4, 4, gy + 7, Layout.DOOR_Z + 4)) {
         BlockState s = level.getBlockState(q);
         if (!s.isAir() && !(s.getBlock() instanceof EntityBlock)) {
            level.setBlock(q, Blocks.AIR.defaultBlockState(), 2);
         }
      }

      Endings.Fall f = new Endings.Fall(p);
      f.bar.addPlayer(p);
      FALLS.removeIf(o -> o.player == p);
      FALLS.add(f);
      title(p, "title.deathbound.fall");
      level.playSound(null, p.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.AMBIENT, 2.0F, 0.4F);
      ModNet.shake(level, p.position(), 30.0, 2.5F, 30);
   }

   private static int width(double z) {
      for (Layout.Island is : new Layout.Island[]{Layout.CITADEL, Layout.GATE, Layout.HUB}) {
         double dz = z - is.z();
         if (Math.abs(dz) < is.radius()) {
            return (int)Math.sqrt(is.radius() * is.radius() - dz * dz);
         }
      }

      return 4;
   }

   private static boolean fall(ServerLevel level, Endings.Fall f) {
      ServerPlayer p = f.player;
      RandomSource rng = level.getRandom();
      f.age++;
      if (f.age > 70) {
         double speed = f.escaped ? 0.4 : 0.16;
         f.front = Math.min(FALL_END, Math.max(f.front + speed, !f.escaped && !p.isRemoved() ? p.getZ() - 22.0 : f.front));
      }

      if (f.age % 2 == 0) {
         int half = width(f.front);

         for (int k = 0; k < 12; k++) {
            double x = (rng.nextDouble() * 2.0 - 1.0) * Math.min(half, 14);
            int top = level.getHeight(Types.WORLD_SURFACE, Mth.floor(x), Mth.floor(f.front));
            if (top > 50) {
               level.sendParticles(ParticleTypes.LARGE_SMOKE, x, top + 0.3, f.front, 2, 0.6, 0.4, 0.4, 0.02);
               level.sendParticles(ModParticles.SOUL_FLAME, x, top + 0.2, f.front, 3, 0.5, 0.3, 0.5, 0.04);
               level.sendParticles(ParticleTypes.END_ROD, x, top + 0.5, f.front, 0, 0.0, 1.0, 0.0, 0.25);
            }
         }
      }

      for (int k = 0; k < 30; k++) {
         boolean road = k < 14;
         int z = Mth.floor(f.front - rng.nextDouble() * 5.0);
         int half = road ? 5 : width(z);
         int x = rng.nextInt(half * 2 + 1) - half;
         if (level.isLoaded(new BlockPos(x, 0, z))) {
            int top = level.getHeight(Types.WORLD_SURFACE, x, z) - 1;
            if (top >= 50) {
               for (int dy = 0; dy < (road ? 4 : 2); dy++) {
                  BlockPos q = new BlockPos(x, top - dy, z);
                  BlockState s = level.getBlockState(q);
                  if (!s.isAir() && !(s.getDestroySpeed(level, q) < 0.0F) && !(s.getBlock() instanceof EntityBlock)) {
                     if (dy == 0 && k % 4 == 0) {
                        FallingBlockEntity.fall(level, q, s);
                     } else {
                        if (k % 6 == 1) {
                           level.levelEvent(2001, q, Block.getId(s));
                        }

                        level.setBlock(q, Blocks.AIR.defaultBlockState(), 2);
                     }
                  }
               }
            }
         }
      }

      if (f.age % 30 == 0 && f.front < FALL_END) {
         BlockPos edge = BlockPos.containing(0.0, Layout.CITADEL.top(), f.front);
         level.playSound(null, edge, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0F, 0.35F);
         level.playSound(null, edge, SoundEvents.DEEPSLATE_BREAK, SoundSource.BLOCKS, 4.0F, 0.4F);
      }

      if (!p.isRemoved() && f.age % 3 == 0) {
         rising(level, p, 6, 36);
      }

      if (f.age % 12 == 0 && !p.isRemoved()) {
         Iterator var16 = level.getEntitiesOfClass(LostSoul.class, p.getBoundingBox().inflate(64.0)).iterator();
         if (var16.hasNext()) {
            LostSoul s = (LostSoul)var16.next();
            level.sendParticles(ParticleTypes.END_ROD, s.getX(), s.getY() + 1.0, s.getZ(), 0, 0.0, 1.0, 0.0, 0.35);
            level.sendParticles(ParticleTypes.END_ROD, s.getX(), s.getY() + 1.0, s.getZ(), 30, 0.2, 2.0, 0.2, 0.04);
            s.discard();
         }
      }

      if (!f.escaped && !p.isRemoved()) {
         double total = Layout.ARRIVAL.z() - SEAT.z;
         f.bar.setProgress((float)Mth.clamp((p.getZ() - SEAT.z) / total, 0.0, 1.0));
         double behind = p.getZ() - f.front;
         if (f.age % (behind < 14.0 ? 20 : 40) == 0) {
            ModNet.shake(level, p.position(), 16.0, behind < 14.0 ? 2.0F : 0.9F, 14);
         }

         boolean home = p.getZ() > Layout.ARRIVAL.z() - Layout.ARRIVAL.radius() + 4 && Math.abs(p.getX() - Layout.ARRIVAL.x()) < Layout.ARRIVAL.radius();
         if (p.getY() < Layout.ARRIVAL.top() - 50 && p.level() == level) {
            p.resetFallDistance();
            p.setDeltaMovement(Vec3.ZERO);
            p.sendSystemMessage(Component.translatable("message.deathbound.fall.caught").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            home = true;
         } else if (p.getZ() < f.front + 1.0 && p.onGround()) {
            for (BlockPos q : BlockPos.betweenClosed(p.blockPosition().offset(-1, -4, -1), p.blockPosition().offset(1, -1, 1))) {
               BlockState s = level.getBlockState(q);
               if (!s.isAir() && s.getDestroySpeed(level, q) >= 0.0F && !(s.getBlock() instanceof EntityBlock)) {
                  level.setBlock(q, Blocks.AIR.defaultBlockState(), 2);
               }
            }
         }

         if (home || p.level() != level) {
            f.escaped = true;
            f.bar.removeAllPlayers();
            if (p.level() == level) {
               begin(p, 4);
            }
         }
      }

      if (f.front >= FALL_END && f.age > 110) {
         f.bar.removeAllPlayers();
         if (!f.escaped && !p.isRemoved() && p.level() == level) {
            begin(p, 4);
         }

         return true;
      } else {
         return p.isRemoved() && f.escaped;
      }
   }

   private static void rising(ServerLevel level, ServerPlayer p, int count, int radius) {
      RandomSource rng = level.getRandom();

      for (int i = 0; i < count; i++) {
         int x = Mth.floor(p.getX()) + rng.nextInt(radius * 2 + 1) - radius;
         int z = Mth.floor(p.getZ()) + rng.nextInt(radius * 2 + 1) - radius;
         if (level.isLoaded(new BlockPos(x, 0, z))) {
            int y = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y >= 60 && !(Math.abs(y - p.getY()) > 30.0)) {
               level.sendParticles(ParticleTypes.END_ROD, x + 0.5, y + 0.2, z + 0.5, 0, 0.0, 1.0, 0.0, 0.12 + rng.nextDouble() * 0.12);
               if (rng.nextInt(3) == 0) {
                  level.sendParticles(ModParticles.SOUL_MOTE, x + 0.5, y + 0.6, z + 0.5, 3, 0.2, 0.4, 0.2, 0.01);
               }
            }
         }
      }
   }

   private static void fallen(ServerLevel level, ServerPlayer p, Endings.Run r, int t) {
      if (r.at(0)) {
         put(
            p,
            Layout.ARRIVAL.x() + 0.5,
            ground(level, Layout.ARRIVAL.x() + 0.5, Layout.ARRIVAL.z() - 11.5, Layout.ARRIVAL.top() + 4),
            Layout.ARRIVAL.z() - 11.5,
            180.0F
         );
         p.removeEffect(MobEffects.NIGHT_VISION);
         p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false));
      }

      if (t % 2 == 0) {
         RandomSource rng = level.getRandom();

         for (int k = 0; k < 10; k++) {
            double x = rng.nextGaussian() * 30.0;
            double z = Layout.HUB.z() + 40 + rng.nextGaussian() * 35.0;
            level.sendParticles(ParticleTypes.END_ROD, x, Layout.HUB.top() + rng.nextDouble() * 6.0, z, 0, 0.0, 1.0, 0.0, 0.2 + rng.nextDouble() * 0.2);
         }
      }
   }

   private static void lasting(ServerLevel level, int ending) {
      long now = level.getGameTime();
      if (ending == 0 && now % 20L == 0L && Director.storyState(level).progress() >= 2 && level.getNearestPlayer(SEAT.x, SEAT.y, SEAT.z, 48.0, false) != null) {
         level.sendParticles(ModParticles.SOUL_FLAME, SEAT.x, SEAT.y + 0.5, SEAT.z, 12, 0.3, 1.5, 0.3, 0.02);
         level.sendParticles(ParticleTypes.END_ROD, SEAT.x, SEAT.y + 2.5, SEAT.z, 4, 0.1, 2.0, 0.1, 0.01);
         if (now % 40L == 0L) {
            level.playSound(null, Layout.THRONE_SEAT, ModSounds.HEARTBEAT, SoundSource.HOSTILE, 2.0F, 0.7F);
         }
      }

      switch (ending) {
         case 2:
            if (now % 40L == 0L && level.getNearestPlayer(Layout.GUARD_POST.getX(), Layout.GUARD_POST.getY(), Layout.GUARD_POST.getZ(), 96.0, false) != null) {
               judge(level);
               judgeNext(level);
            }

            if (now % 5L == 0L && level.getNearestPlayer(Layout.CITADEL.x(), Layout.CITADEL.top(), Layout.CITADEL.z(), 120.0, false) != null) {
               refillLake(level, 12);
            }

            if (now % 20L == 0L && RUNS.isEmpty()) {
               UnderworldNpc king = king(level);
               if (king != null) {
                  king.setCorruption(Math.max(0.55F, king.corruption()) + 0.0015F);
                  if (now % 400L == 0L) {
                     level.playSound(null, Layout.THRONE_SEAT, ModSounds.WHISPER, SoundSource.NEUTRAL, 1.5F, 0.5F);
                  }
               }
            }
            break;
         case 3:
            for (ServerPlayer p : level.players()) {
               if (now % 5L == 0L) {
                  rising(level, p, 2, 32);
               }

               if (now % 100L == 0L
                  && !Layout.sanctuary(p.blockPosition())
                  && FALLS.isEmpty()
                  && RUNS.isEmpty()
                  && level.getEntitiesOfClass(Gravebound.class, p.getBoundingBox().inflate(40.0)).size() < 6) {
                  prowl(level, p);
               }
            }
      }
   }

   private static void prowl(ServerLevel level, ServerPlayer p) {
      RandomSource rng = level.getRandom();
      int tries = 0;

      for (int made = 0; tries < 10 && made < 1 + rng.nextInt(2); tries++) {
         double a = rng.nextDouble() * 3.141592653589793 * 2.0;
         double d = 18.0 + rng.nextDouble() * 10.0;
         int x = Mth.floor(p.getX() + Math.cos(a) * d);
         int z = Mth.floor(p.getZ() + Math.sin(a) * d);
         if (level.isLoaded(new BlockPos(x, 0, z))) {
            int y = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos at = new BlockPos(x, y, z);
            if (y >= 60 && !(Math.abs(y - p.getY()) > 12.0) && !Layout.sanctuary(at) && level.getBlockState(at.below()).isSolid()) {
               Gravebound g = ModEntities.GRAVEBOUND.create(level, EntitySpawnReason.EVENT);
               if (g != null) {
                  g.snapTo(x + 0.5, y, z + 0.5, rng.nextFloat() * 360.0F, 0.0F);
                  level.addFreshEntity(g);
                  made++;
               }
            }
         }
      }
   }

   private static void wild(Gravebound g) {
      AttributeInstance health = g.getAttribute(Attributes.MAX_HEALTH);
      if (health != null && !health.hasModifier(WILD)) {
         health.addPermanentModifier(new AttributeModifier(WILD, 0.5, Operation.ADD_MULTIPLIED_BASE));
         AttributeInstance speed = g.getAttribute(Attributes.MOVEMENT_SPEED);
         if (speed != null) {
            speed.addPermanentModifier(new AttributeModifier(WILD, 0.18, Operation.ADD_MULTIPLIED_BASE));
         }

         AttributeInstance damage = g.getAttribute(Attributes.ATTACK_DAMAGE);
         if (damage != null) {
            damage.addPermanentModifier(new AttributeModifier(WILD, 2.0, Operation.ADD_VALUE));
         }

         g.setHealth(g.getMaxHealth());
      }
   }

   private Endings() {
   }

   private static final class Fall {
      final ServerPlayer player;
      final ServerBossEvent bar = new ServerBossEvent(
         UUID.randomUUID(), Component.translatable("bossbar.deathbound.fall"), BossBarColor.PURPLE, BossBarOverlay.NOTCHED_20
      );
      int age;
      double front = Endings.SEAT.z - 3.0;
      boolean escaped;

      Fall(ServerPlayer player) {
         this.player = player;
      }
   }

   private static final class Run {
      final ServerPlayer player;
      final int which;
      final long start;
      int last = -1;
      int t;

      Run(ServerPlayer player, int which, long start) {
         this.player = player;
         this.which = which;
         this.start = start;
      }

      boolean at(int e) {
         return this.last < e && this.t >= e;
      }
   }
}
