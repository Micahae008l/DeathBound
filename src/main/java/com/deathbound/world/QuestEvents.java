package com.deathbound.world;

import com.deathbound.DeathBound;
import com.deathbound.entity.HunterArrow;
import com.deathbound.entity.LostSoul;
import com.deathbound.item.AldousLanternItem;
import com.deathbound.npc.Quests;
import com.deathbound.npc.Soulforge;
import com.deathbound.registry.ModEffects;
import com.deathbound.registry.ModEntities;
import com.deathbound.registry.ModItems;
import com.deathbound.registry.ModNet;
import com.deathbound.registry.ModParticles;
import com.deathbound.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The live parts of three quests:
 * Mira - Aldous's lantern gutters every time you are hurt and goes out after three hits; a soul flame relights it.
 * The Sentry's tag - taking it off the broken bridge brings arrows out of the Hollow until you are back at the Watch.
 * The Ferryman's oar - carrying it out of the shed wakes the Ghostwood until you are out of the trees.
 */
public final class QuestEvents {
   static final TagKey<Block> RELIGHTS_LANTERN = TagKey.create(Registries.BLOCK, DeathBound.id("relights_lantern"));
   static final int LANTERN_GRACE = 20;
   static final int AMBUSH_DELAY = 40;
   static final int AMBUSH_EVERY = 26;
   static final int AMBUSH_LIMIT = 900;
   static final int WAKE_EVERY = 50;
   static final int WAKE_LIMIT = 2400;
   static final int WAKE_CAP = 6;
   private static final Map<UUID, Integer> LANTERN_SAFE_UNTIL = new HashMap<>();
   private static final Map<UUID, Integer> AMBUSH = new HashMap<>();
   private static final Set<UUID> AMBUSHED = new HashSet<>();
   private static final Map<UUID, Wake> WAKE = new HashMap<>();
   private static final Set<UUID> WOKEN = new HashSet<>();

   public static void init() {
      ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseTaken, taken, blocked) -> {
         if (entity instanceof ServerPlayer p && taken > 0.0F) {
            lanternHit(p);
         }
      });
      UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
         if (player instanceof ServerPlayer p && world instanceof ServerLevel level) {
            return relight(p, level, player.getItemInHand(hand), hit.getBlockPos());
         }

         return InteractionResult.PASS;
      });
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            tick(server, p);
         }
      });
   }

   // ---- Mira: the lantern ----

   private static void lanternHit(ServerPlayer p) {
      if (Quests.stage(p, "mira") != 1) {
         return;
      }

      ItemStack lantern = Soulforge.find(p, ModItems.ALDOUS_LANTERN);
      int now = p.level().getServer().getTickCount();
      if (lantern.isEmpty() || AldousLanternItem.isOut(lantern) || now < LANTERN_SAFE_UNTIL.getOrDefault(p.getUUID(), 0)) {
         return;
      }

      // one hit at a time: a burst of damage only costs one flame
      LANTERN_SAFE_UNTIL.put(p.getUUID(), now + LANTERN_GRACE);
      int hits = AldousLanternItem.hits(lantern) + 1;
      AldousLanternItem.setHits(lantern, hits);
      ServerLevel level = p.level();
      if (hits >= AldousLanternItem.MAX_HITS) {
         level.playSound(null, p.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.8F);
         level.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, p.getX(), p.getY() + 1.0, p.getZ(), 12, 0.2, 0.3, 0.2, 0.01);
         p.sendSystemMessage(Component.translatable("quest.deathbound.mira.lantern_out").withStyle(ChatFormatting.GOLD));
      } else {
         level.playSound(null, p.blockPosition(), SoundEvents.CANDLE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.4F);
         p.sendOverlayMessage(Component.translatable("quest.deathbound.mira.lantern_gutters", AldousLanternItem.MAX_HITS - hits).withStyle(ChatFormatting.GOLD));
      }
   }

   private static InteractionResult relight(ServerPlayer p, ServerLevel level, ItemStack held, BlockPos pos) {
      if (!held.is(ModItems.ALDOUS_LANTERN) || AldousLanternItem.hits(held) == 0) {
         return InteractionResult.PASS;
      }

      BlockState state = level.getBlockState(pos);
      if (!state.is(RELIGHTS_LANTERN) || state.hasProperty(BlockStateProperties.LIT) && !state.getValue(BlockStateProperties.LIT)) {
         return InteractionResult.PASS;
      }

      AldousLanternItem.setHits(held, 0);
      level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0F, 0.8F);
      level.playSound(null, pos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
      level.sendParticles(ModParticles.SOUL_FLAME, p.getX(), p.getY() + 1.2, p.getZ(), 10, 0.2, 0.2, 0.2, 0.02);
      p.sendOverlayMessage(Component.translatable("quest.deathbound.mira.lantern_lit").withStyle(ChatFormatting.GOLD));
      return InteractionResult.SUCCESS;
   }

   // Mira stands somewhere along the line on the Gate island: one of these spots, fixed per world.
   private static final int[][] MIRA_SPOTS = new int[][]{
      {-3, 4}, {3, 10}, {-3, 16}, {3, 22}, {-3, 28}, {3, 34}, {3, 4}, {-3, 10}, {3, 16}, {-3, 22}, {3, 28}, {-3, 34}
   };

   public static BlockPos miraSpot(ServerLevel level) {
      int[] s = MIRA_SPOTS[Math.floorMod(Long.hashCode(level.getSeed() * 31L + 7L), MIRA_SPOTS.length)];
      return new BlockPos(s[0], Layout.GATE.top() + 1, Layout.GATE.z() + Layout.GATE.radius() - s[1]);
   }

   /** What a soul in the line says about Mira while you are looking for her, or null when you aren't. */
   public static @Nullable Component miraClue(ServerPlayer p, LostSoul soul) {
      if (Quests.stage(p, "mira") != 1 || soul.lingering()) {
         return null;
      }

      BlockPos mira = miraSpot(p.level());
      double dx = mira.getX() + 0.5 - soul.getX();
      double dz = mira.getZ() + 0.5 - soul.getZ();
      double far = Math.sqrt(dx * dx + dz * dz);
      int pick = Math.floorMod(soul.getUUID().hashCode(), 3);
      String side = mira.getX() < 0 ? "left" : "right";
      String key;
      if (far < 6.0) {
         key = "near";
      } else if (dz < 0.0) {
         key = far < 24.0 ? "ahead_close" : "ahead_far";
      } else {
         key = far < 24.0 ? "behind_close" : "behind_far";
      }

      Component clue = Component.translatable("quest.deathbound.mira.clue." + key + "." + pick);
      return far < 30.0 ? Component.translatable("quest.deathbound.mira.clue.with_side", clue, Component.translatable("quest.deathbound.mira.clue." + side)) : clue;
   }

   // ---- per tick ----

   private static void tick(MinecraftServer server, ServerPlayer p) {
      int now = server.getTickCount();
      UUID id = p.getUUID();
      boolean below = UnderworldTravel.inUnderworld(p) && p.isAlive();
      if (now % 10 == 0) {
         boolean tag = Soulforge.count(p, ModItems.SENTRYS_TAG) > 0;
         if (!tag) {
            AMBUSHED.remove(id);
         } else if (below && !AMBUSHED.contains(id) && Quests.stage(p, "name") == 1 && nearBridge(p)) {
            AMBUSHED.add(id);
            AMBUSH.put(id, now);
            startAmbush(p);
         }

         boolean oar = Soulforge.count(p, ModItems.FERRYMANS_OAR) > 0;
         if (!oar) {
            WOKEN.remove(id);
         } else if (below && !WOKEN.contains(id) && Quests.stage(p, "oar") == 1 && inForest(p, 0.0)) {
            WOKEN.add(id);
            WAKE.put(id, new Wake(now, new ArrayList<>()));
            wakeTheWood(p);
         }
      }

      Integer ambushSince = AMBUSH.get(id);
      if (ambushSince != null) {
         ambushTick(p, now - ambushSince, below);
      }

      Wake wake = WAKE.get(id);
      if (wake != null) {
         wakeTick(p, wake, now - wake.since, below);
      }
   }

   // ---- the Sentry's tag ----

   private static Vec3 watchEnd() {
      return new Vec3(Layout.WATCH.x() - Layout.WATCH.radius() + 2 + 0.5, Layout.WATCH.top() + 1, Layout.WATCH.z() + 0.5);
   }

   private static Vec3 hollowEnd() {
      return new Vec3(Layout.HOLLOW.x() + Layout.HOLLOW.radius() - 3 + 0.5, Layout.HOLLOW.top() + 1, Layout.HOLLOW.z() + 6 + 0.5);
   }

   private static boolean nearBridge(ServerPlayer p) {
      Vec3 mid = watchEnd().lerp(hollowEnd(), 0.5);
      return p.position().distanceTo(mid) < 30.0;
   }

   private static void startAmbush(ServerPlayer p) {
      ServerLevel level = p.level();
      p.addEffect(new MobEffectInstance(ModEffects.MARKED, 600, 0));
      Dread.whisperTo(p, ModSounds.WAIL, hollowEnd().add(0.0, 8.0, 0.0), 1.0F, 0.55F, p.getRandom());
      level.playSound(null, BlockPos.containing(hollowEnd()), SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.HOSTILE, 4.0F, 0.5F);
      p.sendSystemMessage(Component.translatable("quest.deathbound.name.ambush").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
   }

   private static void ambushTick(ServerPlayer p, int t, boolean below) {
      UUID id = p.getUUID();
      double home = p.position().multiply(1.0, 0.0, 1.0).distanceTo(watchEnd().multiply(1.0, 0.0, 1.0));
      if (!below || t > AMBUSH_LIMIT || !nearBridge(p) && home > 6.0) {
         AMBUSH.remove(id);
         return;
      }

      if (home < 6.0 && t > AMBUSH_DELAY) {
         AMBUSH.remove(id);
         p.sendSystemMessage(Component.translatable("quest.deathbound.name.ambush_over").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
         return;
      }

      if (t < AMBUSH_DELAY) {
         return;
      }

      int beat = (t - AMBUSH_DELAY) % AMBUSH_EVERY;
      ServerLevel level = p.level();
      RandomSource r = p.getRandom();
      Vec3 from = hollowEnd().add((r.nextDouble() - 0.5) * 10.0, 9.0 + r.nextDouble() * 6.0, (r.nextDouble() - 0.5) * 12.0);
      if (beat == AMBUSH_EVERY - 10) {
         // the draw: a creak from the dark, a moment before the arrow
         level.playSound(null, BlockPos.containing(from), SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), SoundSource.HOSTILE, 3.0F, 0.5F);
      } else if (beat == 0) {
         int volley = (t - AMBUSH_DELAY) / AMBUSH_EVERY % 4 == 3 ? 3 : 1;
         for (int i = 0; i < volley; i++) {
            loose(level, p, from, i - (volley - 1) / 2.0);
         }
      }
   }

   private static void loose(ServerLevel level, ServerPlayer p, Vec3 from, double spread) {
      HunterArrow arrow = new HunterArrow(ModEntities.HUNTER_ARROW, level);
      arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
      arrow.setPos(from);
      Vec3 to = p.position().add(0.0, p.getBbHeight() * 0.55, 0.0);
      double flight = to.distanceTo(from) / 3.2;
      Vec3 lead = p.getDeltaMovement().multiply(1.0, 0.0, 1.0).scale(flight);
      Vec3 side = to.subtract(from).cross(new Vec3(0.0, 1.0, 0.0)).normalize().scale(spread * 1.6);
      Vec3 d = to.add(lead).add(side).subtract(from);
      arrow.shoot(d.x, d.y + d.horizontalDistance() * 0.04, d.z, 3.2F, 0.6F);
      arrow.setBaseDamage(3.0);
      level.addFreshEntity(arrow);
      level.playSound(null, BlockPos.containing(from), SoundEvents.CROSSBOW_SHOOT, SoundSource.HOSTILE, 3.0F, 0.45F);
      level.sendParticles(ModParticles.SOUL_FLAME, from.x, from.y, from.z, 6, 0.15, 0.15, 0.15, 0.02);
   }

   // ---- the Ferryman's oar ----

   private record Wake(int since, List<Mob> risen) {
   }

   private static boolean inForest(ServerPlayer p, double margin) {
      double dx = p.getX() - Layout.FOREST.x();
      double dz = p.getZ() - Layout.FOREST.z();
      return Math.sqrt(dx * dx + dz * dz) < Layout.FOREST.radius() + margin;
   }

   private static void wakeTheWood(ServerPlayer p) {
      ServerLevel level = p.level();
      p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0, false, false));
      Dread.whisperTo(p, ModSounds.WAIL, p.position().add(0.0, 4.0, 0.0), 1.0F, 0.6F, p.getRandom());
      level.playSound(null, p.blockPosition(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 2.0F, 0.5F);
      ModNet.shake(level, p.position(), 20.0, 1.0F, 20);
      p.sendSystemMessage(Component.translatable("quest.deathbound.oar.wake").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
   }

   private static void wakeTick(ServerPlayer p, Wake wake, int t, boolean below) {
      ServerLevel level = p.level();
      wake.risen.removeIf(m -> !m.isAlive() || m.isRemoved());
      if (!below || t > WAKE_LIMIT) {
         settle(level, wake);
         WAKE.remove(p.getUUID());
         return;
      }

      if (!inForest(p, 6.0)) {
         settle(level, wake);
         WAKE.remove(p.getUUID());
         p.sendSystemMessage(Component.translatable("quest.deathbound.oar.wake_over").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
         return;
      }

      RandomSource r = p.getRandom();
      if (t % WAKE_EVERY == 0 && wake.risen.size() < WAKE_CAP) {
         int n = 1 + (r.nextInt(3) == 0 ? 1 : 0);
         for (int i = 0; i < n; i++) {
            raise(level, p, wake, r);
         }
      }

      if (t % 160 == 80 && r.nextBoolean()) {
         Dread.shade(level, p, r);
      }
   }

   private static void raise(ServerLevel level, ServerPlayer p, Wake wake, RandomSource r) {
      double a = r.nextDouble() * Math.PI * 2.0;
      double dist = 8.0 + r.nextDouble() * 6.0;
      BlockPos at = Director.standingSpot(level, BlockPos.containing(p.getX() + Math.cos(a) * dist, p.getY(), p.getZ() + Math.sin(a) * dist));
      Mob mob = (r.nextInt(4) == 0 ? ModEntities.SOUL_WISP : ModEntities.GRAVEBOUND).create(level, EntitySpawnReason.EVENT);
      if (mob != null) {
         mob.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, r.nextFloat() * 360.0F, 0.0F);
         mob.setTarget(p);
         level.addFreshEntity(mob);
         wake.risen.add(mob);
         level.sendParticles(ModParticles.SOUL_FLAME, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 14, 0.4, 0.5, 0.4, 0.03);
         level.playSound(null, at, SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 1.4F, 0.6F);
      }
   }

   // whatever the wood sent after you crumbles once you are out
   private static void settle(ServerLevel level, Wake wake) {
      for (Mob m : wake.risen) {
         if (m.isAlive()) {
            level.sendParticles(ModParticles.SOUL_FLAME, m.getX(), m.getY() + 0.8, m.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
            m.discard();
         }
      }

      wake.risen.clear();
   }

   private QuestEvents() {
   }
}
