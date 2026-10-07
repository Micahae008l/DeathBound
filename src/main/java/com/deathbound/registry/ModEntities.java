package com.deathbound.registry;

import com.deathbound.DeathBound;
import com.deathbound.entity.DeathEntity;
import com.deathbound.entity.DeathOrb;
import com.deathbound.entity.DeathsGuard;
import com.deathbound.entity.Gravebound;
import com.deathbound.entity.HollowHunter;
import com.deathbound.entity.HunterArrow;
import com.deathbound.entity.LanternWisp;
import com.deathbound.entity.LostSoul;
import com.deathbound.entity.Shade;
import com.deathbound.entity.SkeletonKid;
import com.deathbound.entity.SoulAnchor;
import com.deathbound.entity.SoulBolt;
import com.deathbound.entity.SoulWisp;
import com.deathbound.npc.UnderworldNpc;
import com.deathbound.world.Layout;
import java.util.List;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class ModEntities {
   public static final EntityType<Gravebound> GRAVEBOUND = register(
      "gravebound", Builder.of(Gravebound::new, MobCategory.MONSTER).sized(0.6F, 1.95F).eyeHeight(1.7F).clientTrackingRange(8).notInPeaceful()
   );
   public static final EntityType<SoulWisp> SOUL_WISP = register(
      "soul_wisp", Builder.of(SoulWisp::new, MobCategory.MONSTER).sized(0.6F, 0.75F).eyeHeight(0.45F).fireImmune().clientTrackingRange(8).notInPeaceful()
   );
   public static final EntityType<DeathsGuard> DEATHS_GUARD = register(
      "deaths_guard", Builder.of(DeathsGuard::new, MobCategory.MONSTER).sized(1.4F, 3.0F).eyeHeight(2.6F).fireImmune().clientTrackingRange(10).notInPeaceful()
   );
   public static final EntityType<HollowHunter> HOLLOW_HUNTER = register(
      "hollow_hunter", Builder.of(HollowHunter::new, MobCategory.MONSTER).sized(0.8F, 2.8F).eyeHeight(2.45F).clientTrackingRange(12)
   );
   public static final EntityType<HunterArrow> HUNTER_ARROW = register(
      "hunter_arrow",
      Builder.<HunterArrow>of(HunterArrow::new, MobCategory.MISC).noLootTable().sized(0.5F, 0.5F).eyeHeight(0.13F).clientTrackingRange(4).updateInterval(20)
   );
   public static final EntityType<DeathEntity> DEATH = register(
      "death", Builder.of(DeathEntity::new, MobCategory.MONSTER).sized(1.2F, 2.7F).eyeHeight(2.35F).fireImmune().clientTrackingRange(12).notInPeaceful()
   );
   public static final EntityType<LostSoul> LOST_SOUL = register(
      "lost_soul", Builder.of(LostSoul::new, MobCategory.MISC).sized(0.6F, 2.2F).eyeHeight(1.9F).fireImmune().clientTrackingRange(10)
   );
   public static final EntityType<Shade> SHADE = register(
      "shade", Builder.of(Shade::new, MobCategory.MISC).sized(0.6F, 2.3F).eyeHeight(2.1F).fireImmune().noSummon().clientTrackingRange(6)
   );
   public static final EntityType<SoulAnchor> SOUL_ANCHOR = register(
      "soul_anchor", Builder.of(SoulAnchor::new, MobCategory.MISC).sized(1.0F, 1.8F).eyeHeight(0.9F).fireImmune().clientTrackingRange(10)
   );
   public static final EntityType<UnderworldNpc> FERRYMAN = register(
      "ferryman", Builder.of(UnderworldNpc::new, MobCategory.MISC).sized(0.8F, 2.5F).eyeHeight(2.2F).fireImmune().clientTrackingRange(10)
   );
   public static final EntityType<UnderworldNpc> GRAVEDIGGER = register(
      "gravedigger", Builder.of(UnderworldNpc::new, MobCategory.MISC).sized(0.6F, 1.95F).eyeHeight(1.7F).fireImmune().clientTrackingRange(10)
   );
   public static final EntityType<UnderworldNpc> COLLECTOR = register(
      "collector", Builder.of(UnderworldNpc::new, MobCategory.MISC).sized(0.8F, 1.9F).eyeHeight(1.4F).fireImmune().clientTrackingRange(10)
   );
   public static final EntityType<UnderworldNpc> BONESMITH = register(
      "bonesmith", Builder.of(UnderworldNpc::new, MobCategory.MISC).sized(0.8F, 1.45F).eyeHeight(1.15F).fireImmune().clientTrackingRange(10)
   );
   public static final EntityType<UnderworldNpc> MIRA = register(
      "mira", Builder.of(UnderworldNpc::new, MobCategory.MISC).sized(0.5F, 1.25F).eyeHeight(1.0F).fireImmune().clientTrackingRange(10)
   );
   public static final EntityType<UnderworldNpc> LAMPLIGHTER = register(
      "lamplighter", Builder.of(UnderworldNpc::new, MobCategory.MISC).sized(0.7F, 2.3F).eyeHeight(2.0F).fireImmune().clientTrackingRange(10)
   );
   public static final EntityType<UnderworldNpc> SENTRY = register(
      "sentry", Builder.of(UnderworldNpc::new, MobCategory.MISC).sized(0.7F, 2.0F).eyeHeight(1.8F).fireImmune().clientTrackingRange(10)
   );
   public static final EntityType<SkeletonKid> SKELETON_KID = register(
      "skeleton_kid", Builder.of(SkeletonKid::new, MobCategory.MISC).sized(0.5F, 1.0F).eyeHeight(0.8F).fireImmune().clientTrackingRange(10)
   );
   public static final EntityType<LanternWisp> LANTERN_WISP = register(
      "lantern_wisp",
      Builder.of(LanternWisp::new, MobCategory.MISC).sized(0.4F, 0.5F).eyeHeight(0.3F).fireImmune().noSummon().clientTrackingRange(10).updateInterval(1)
   );
   public static final EntityType<UnderworldNpc> PROPHET = register(
      "prophet", Builder.of(UnderworldNpc::new, MobCategory.MISC).sized(0.9F, 2.1F).eyeHeight(1.7F).fireImmune().clientTrackingRange(10)
   );
   public static final EntityType<SoulBolt> SOUL_BOLT = register(
      "soul_bolt", Builder.<SoulBolt>of(SoulBolt::new, MobCategory.MISC).noLootTable().sized(0.4F, 0.4F).clientTrackingRange(6).updateInterval(5)
   );
   public static final EntityType<DeathOrb> DEATH_ORB = register(
      "death_orb", Builder.<DeathOrb>of(DeathOrb::new, MobCategory.MISC).noLootTable().sized(0.7F, 0.7F).clientTrackingRange(8).updateInterval(2)
   );

   private static <T extends Entity> EntityType<T> register(String name, Builder<T> builder) {
      ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, DeathBound.id(name));
      return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
   }

   public static void init() {
      FabricDefaultAttributeRegistry.register(GRAVEBOUND, Gravebound.createAttributes());
      FabricDefaultAttributeRegistry.register(SOUL_WISP, SoulWisp.createAttributes());
      FabricDefaultAttributeRegistry.register(DEATHS_GUARD, DeathsGuard.createAttributes());
      FabricDefaultAttributeRegistry.register(DEATH, DeathEntity.createAttributes());
      FabricDefaultAttributeRegistry.register(HOLLOW_HUNTER, HollowHunter.createAttributes());
      FabricDefaultAttributeRegistry.register(LOST_SOUL, LostSoul.createAttributes());
      FabricDefaultAttributeRegistry.register(SHADE, Shade.createAttributes());
      FabricDefaultAttributeRegistry.register(SKELETON_KID, SkeletonKid.createAttributes());
      FabricDefaultAttributeRegistry.register(LANTERN_WISP, LanternWisp.createAttributes());
      FabricDefaultAttributeRegistry.register(SOUL_ANCHOR, SoulAnchor.createAttributes());

      for (EntityType<UnderworldNpc> npc : List.of(FERRYMAN, GRAVEDIGGER, PROPHET, COLLECTOR, BONESMITH, MIRA, LAMPLIGHTER, SENTRY)) {
         FabricDefaultAttributeRegistry.register(npc, UnderworldNpc.createAttributes());
      }

      SpawnPlacements.register(
         GRAVEBOUND,
         SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         (type, level, reason, pos, random) -> !Layout.sanctuary(pos) && Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
      );
      SpawnPlacements.register(
         SOUL_WISP,
         SpawnPlacementTypes.NO_RESTRICTIONS,
         Types.MOTION_BLOCKING_NO_LEAVES,
         (type, level, reason, pos, random) -> !Layout.sanctuary(pos) && Monster.checkAnyLightMonsterSpawnRules(type, level, reason, pos, random)
      );
   }

   private ModEntities() {
   }
}
