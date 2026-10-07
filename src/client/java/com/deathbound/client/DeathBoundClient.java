package com.deathbound.client;

import net.minecraft.client.Minecraft;
import com.deathbound.DeathBound;
import com.deathbound.client.model.ModelMeshes;
import com.deathbound.client.model.NpcModels;
import com.deathbound.client.particle.SoulParticle;
import com.deathbound.client.render.CrownedLayer;
import com.deathbound.client.render.Renderers;
import com.deathbound.item.JournalItem;
import com.deathbound.registry.ModEntities;
import com.deathbound.registry.ModFluids;
import com.deathbound.registry.ModMenus;
import com.deathbound.registry.ModNet;
import com.deathbound.registry.ModParticles;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.block.FluidModel.Unbaked;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityTypes;

public class DeathBoundClient implements ClientModInitializer {
   private static float shakeStrength;
   private static int shakeTicks;
   private static int shakeLength;

   public static float shakeAmount(float partialTicks) {
      if (shakeTicks <= 0) {
         return 0.0F;
      }

      float t = Mth.clamp((shakeTicks - partialTicks) / shakeLength, 0.0F, 1.0F);
      return shakeStrength * t * t;
   }

   public void onInitializeClient() {
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.GRAVEBOUND, ModelMeshes::gravebound);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.SOUL_WISP, ModelMeshes::soulWisp);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.DEATHS_GUARD, ModelMeshes::deathsGuard);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.DEATH_REAPER, ModelMeshes::deathReaper);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.DEATH_BEAST, ModelMeshes::deathBeast);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.LOST_SOUL, ModelMeshes::lostSoul);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.SOUL_ANCHOR, ModelMeshes::soulAnchor);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.FERRYMAN, ModelMeshes::ferryman);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.GRAVEDIGGER, ModelMeshes::gravedigger);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.COLLECTOR, ModelMeshes::collector);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.BONESMITH, ModelMeshes::bonesmith);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.MIRA, ModelMeshes::mira);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.LAMPLIGHTER, ModelMeshes::lamplighter);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.SENTRY, ModelMeshes::sentry);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.SKELETON_KID, ModelMeshes::skeletonKid);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.LANTERN_WISP, ModelMeshes::lanternWisp);
      EntityRendererRegistry.register(ModEntities.MIRA, ctx -> new Renderers.NpcRenderer(ctx, NpcModels.Mira::new, DeathBoundClient.Layers.MIRA, "mira", 0.0F));
      EntityRendererRegistry.register(
         ModEntities.LAMPLIGHTER, ctx -> new Renderers.NpcRenderer(ctx, NpcModels.Lamplighter::new, DeathBoundClient.Layers.LAMPLIGHTER, "lamplighter", 0.0F)
      );
      EntityRendererRegistry.register(
         ModEntities.SENTRY, ctx -> new Renderers.NpcRenderer(ctx, NpcModels.Sentry::new, DeathBoundClient.Layers.SENTRY, "sentry", 0.0F)
      );
      EntityRendererRegistry.register(ModEntities.SKELETON_KID, Renderers.KidRenderer::new);
      EntityRendererRegistry.register(ModEntities.LANTERN_WISP, Renderers.WispRenderer::new);
      JournalItem.opener = p -> Journal.open(p);
      com.deathbound.story.Stories.opener = id -> Minecraft.getInstance().gui.setScreen(new StoryScreen(id));
      FluidRenderingRegistry.register(
         ModFluids.SOULWATER,
         ModFluids.FLOWING_SOULWATER,
         new Unbaked(
            new Material(DeathBound.id("block/soulwater_still"), true),
            new Material(DeathBound.id("block/soulwater_flow"), true),
            new Material(DeathBound.id("block/soulwater_still"), true),
            BlockTintSources.constant(-1)
         )
      );
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.HOLLOW_HUNTER, ModelMeshes::hollowHunter);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.CROWN, CrownedLayer::crownMesh);
      EntityRendererRegistry.register(ModEntities.HOLLOW_HUNTER, Renderers.HunterRenderer::new);
      EntityRendererRegistry.register(ModEntities.HUNTER_ARROW, Renderers.HunterArrowRenderer::new);
      ModelLayerRegistry.registerModelLayer(DeathBoundClient.Layers.PROPHET, ModelMeshes::prophet);
      EntityRendererRegistry.register(ModEntities.GRAVEBOUND, Renderers.GraveboundRenderer::new);
      EntityRendererRegistry.register(ModEntities.SOUL_WISP, Renderers.SoulWispRenderer::new);
      EntityRendererRegistry.register(ModEntities.DEATHS_GUARD, Renderers.GuardRenderer::new);
      EntityRendererRegistry.register(ModEntities.DEATH, Renderers.DeathRenderer::new);
      EntityRendererRegistry.register(ModEntities.LOST_SOUL, Renderers.LostSoulRenderer::new);
      EntityRendererRegistry.register(ModEntities.SHADE, Renderers.ShadeRenderer::new);
      EntityRendererRegistry.register(ModEntities.SOUL_ANCHOR, Renderers.AnchorRenderer::new);
      EntityRendererRegistry.register(
         ModEntities.FERRYMAN, ctx -> new Renderers.NpcRenderer(ctx, NpcModels.Ferryman::new, DeathBoundClient.Layers.FERRYMAN, "ferryman", 0.5F)
      );
      EntityRendererRegistry.register(
         ModEntities.COLLECTOR, ctx -> new Renderers.NpcRenderer(ctx, NpcModels.Collector::new, DeathBoundClient.Layers.COLLECTOR, "collector", 0.5F)
      );
      EntityRendererRegistry.register(
         ModEntities.GRAVEDIGGER, ctx -> new Renderers.NpcRenderer(ctx, NpcModels.Gravedigger::new, DeathBoundClient.Layers.GRAVEDIGGER, "gravedigger", 0.45F)
      );
      EntityRendererRegistry.register(
         ModEntities.BONESMITH, ctx -> new Renderers.NpcRenderer(ctx, NpcModels.Bonesmith::new, DeathBoundClient.Layers.BONESMITH, "bonesmith", 0.6F)
      );
      EntityRendererRegistry.register(
         ModEntities.PROPHET, ctx -> new Renderers.NpcRenderer(ctx, NpcModels.Prophet::new, DeathBoundClient.Layers.PROPHET, "prophet", 0.0F)
      );
      EntityRendererRegistry.register(ModEntities.SOUL_BOLT, ctx -> new Renderers.SoulFireRenderer<>(ctx, 0.7F));
      EntityRendererRegistry.register(ModEntities.DEATH_ORB, ctx -> new Renderers.SoulFireRenderer<>(ctx, 1.6F));
      ParticleProviderRegistry.getInstance().register(ModParticles.SOUL_MOTE, SoulParticle.MoteProvider::new);
      ParticleProviderRegistry.getInstance().register(ModParticles.SOUL_FLAME, SoulParticle.FlameProvider::new);
      ParticleProviderRegistry.getInstance().register(ModParticles.SOUL_SWEEP, SoulParticle.SweepProvider::new);
      MenuScreens.register(ModMenus.RELIC, RelicScreen::new);
      ClientPlayNetworking.registerGlobalReceiver(ModNet.Cinematic.TYPE, (payload, context) -> Cinematics.play(payload.kind()));
      ClientPlayNetworking.registerGlobalReceiver(ModNet.Shake.TYPE, (payload, context) -> {
         if (shakeTicks <= 0 || payload.strength() >= shakeAmount(0.0F)) {
            shakeStrength = payload.strength();
            shakeTicks = shakeLength = Math.max(1, payload.ticks());
         }
      });
      HudElementRegistry.addLast(DeathBound.id("cinematics"), Cinematics::render);
      HudElementRegistry.addLast(DeathBound.id("cutscene"), Cutscene::render);
      HudElementRegistry.addLast(DeathBound.id("rewards"), RewardCards::render);
      ClientPlayNetworking.registerGlobalReceiver(ModNet.Received.TYPE, (payload, context) -> RewardCards.add(payload.title(), payload.items()));
      ClientPlayNetworking.registerGlobalReceiver(ModNet.Music.TYPE, (payload, context) -> StoryMusic.set(payload.track()));
      HudElementRegistry.replaceElement(VanillaHudElements.TITLE_AND_SUBTITLE, old -> (g, tick) -> {
         if (!Cinematics.active()) {
            old.extractRenderState(g, tick);
         }
      });

      for (Identifier id : List.of(
         VanillaHudElements.HOTBAR,
         VanillaHudElements.HEALTH_BAR,
         VanillaHudElements.FOOD_BAR,
         VanillaHudElements.ARMOR_BAR,
         VanillaHudElements.AIR_BAR,
         VanillaHudElements.EXPERIENCE_LEVEL,
         VanillaHudElements.INFO_BAR,
         VanillaHudElements.CROSSHAIR,
         VanillaHudElements.CHAT,
         VanillaHudElements.TITLE_AND_SUBTITLE,
         VanillaHudElements.OVERLAY_MESSAGE,
         VanillaHudElements.MOB_EFFECTS,
         VanillaHudElements.BOSS_BAR,
         VanillaHudElements.HELD_ITEM_TOOLTIP
      )) {
         HudElementRegistry.replaceElement(id, old -> (g, tick) -> {
            if (!Cutscene.playing() && !Cinematics.endingShowing()) {
               old.extractRenderState(g, tick);
            }
         });
      }

      ClientPlayNetworking.registerGlobalReceiver(ModNet.Cutscene.TYPE, (payload, context) -> Cutscene.play(payload.which()));
      LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
         if (type == EntityTypes.PLAYER && renderer instanceof AvatarRenderer<?> avatar) {
            helper.register(new CrownedLayer(avatar, ctx));
         }
      });
      DevHarness.init();
      ClientTickEvents.END_CLIENT_TICK.register(mc -> {
         Cinematics.tick();
         Cutscene.tick();
         RewardCards.tick();
         if (shakeTicks > 0) {
            shakeTicks--;
         }
      });
   }

   public static final class Layers {
      public static final ModelLayerLocation GRAVEBOUND = layer("gravebound");
      public static final ModelLayerLocation SOUL_WISP = layer("soul_wisp");
      public static final ModelLayerLocation DEATHS_GUARD = layer("deaths_guard");
      public static final ModelLayerLocation DEATH_REAPER = layer("death_reaper");
      public static final ModelLayerLocation DEATH_BEAST = layer("death_beast");
      public static final ModelLayerLocation LOST_SOUL = layer("lost_soul");
      public static final ModelLayerLocation SOUL_ANCHOR = layer("soul_anchor");
      public static final ModelLayerLocation FERRYMAN = layer("ferryman");
      public static final ModelLayerLocation GRAVEDIGGER = layer("gravedigger");
      public static final ModelLayerLocation PROPHET = layer("prophet");
      public static final ModelLayerLocation COLLECTOR = layer("collector");
      public static final ModelLayerLocation HOLLOW_HUNTER = layer("hollow_hunter");
      public static final ModelLayerLocation CROWN = layer("crown");
      public static final ModelLayerLocation BONESMITH = layer("bonesmith");
      public static final ModelLayerLocation MIRA = layer("mira");
      public static final ModelLayerLocation LAMPLIGHTER = layer("lamplighter");
      public static final ModelLayerLocation SENTRY = layer("sentry");
      public static final ModelLayerLocation SKELETON_KID = layer("skeleton_kid");
      public static final ModelLayerLocation LANTERN_WISP = layer("lantern_wisp");

      private static ModelLayerLocation layer(String name) {
         return new ModelLayerLocation(DeathBound.id(name), "main");
      }
   }
}
