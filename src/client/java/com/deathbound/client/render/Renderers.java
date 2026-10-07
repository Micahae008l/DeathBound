package com.deathbound.client.render;

import com.deathbound.DeathBound;
import com.deathbound.client.DeathBoundClient;
import com.deathbound.client.model.DeathBeastModel;
import com.deathbound.client.model.DeathModel;
import com.deathbound.client.model.DeathsGuardModel;
import com.deathbound.client.model.GraveboundModel;
import com.deathbound.client.model.HunterModel;
import com.deathbound.client.model.KidModel;
import com.deathbound.client.model.LostSoulModel;
import com.deathbound.client.model.NpcModels;
import com.deathbound.client.model.SoulAnchorModel;
import com.deathbound.client.model.SoulWispModel;
import com.deathbound.client.model.WispModel;
import com.deathbound.entity.DeathEntity;
import com.deathbound.entity.DeathsGuard;
import com.deathbound.entity.Gravebound;
import com.deathbound.entity.HollowHunter;
import com.deathbound.entity.HunterArrow;
import com.deathbound.entity.LanternWisp;
import com.deathbound.entity.LostSoul;
import com.deathbound.entity.Shade;
import com.deathbound.entity.SkeletonKid;
import com.deathbound.entity.SoulAnchor;
import com.deathbound.entity.SoulWisp;
import com.deathbound.npc.UnderworldNpc;
import com.deathbound.world.Layout;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public final class Renderers {
   private static Identifier tex(String name) {
      return DeathBound.id("textures/entity/" + name + ".png");
   }

   private static void extractDeath(DeathEntity entity, DeathModel.State state, float partialTicks) {
      state.gesture.copyFrom(entity.gestureAnim);
      state.rise.copyFrom(entity.riseAnim);
      state.barrage.copyFrom(entity.barrageAnim);
      state.blink.copyFrom(entity.blinkAnim);
      state.nova.copyFrom(entity.novaAnim);
      state.summon.copyFrom(entity.summonAnim);
      state.transform.copyFrom(entity.transformAnim);
      state.claw.copyFrom(entity.clawAnim);
      state.leap.copyFrom(entity.leapAnim);
      state.charge.copyFrom(entity.chargeAnim);
      state.rip.copyFrom(entity.ripAnim);
      state.roar.copyFrom(entity.roarAnim);
      state.phase = entity.phase();
      state.action = entity.action();
      state.collapse = entity.deathTime > 0 ? (entity.deathTime + partialTicks) * 90.0F / 170.0F : 0.0F;
      state.deathTime = 0.0F;
      state.hasRedOverlay = entity.hurtTime > 0;
   }

   private Renderers() {
   }

   public static class AnchorRenderer extends MobRenderer<SoulAnchor, LivingEntityRenderState, SoulAnchorModel> {
      private static final Identifier TEXTURE = Renderers.tex("soul_anchor");
      private static final Identifier GLOW = Renderers.tex("soul_anchor_glow");

      public AnchorRenderer(Context ctx) {
         super(ctx, new SoulAnchorModel(ctx.bakeLayer(DeathBoundClient.Layers.SOUL_ANCHOR)), 0.5F);
         this.addLayer(
            new LivingEntityEmissiveLayer<>(
               this,
               s -> GLOW,
               (s, age) -> 0.75F + 0.25F * Mth.sin(age * 0.2F),
               new SoulAnchorModel(ctx.bakeLayer(DeathBoundClient.Layers.SOUL_ANCHOR)),
               RenderTypes::eyes,
               true
            )
         );
      }

      protected int getBlockLightLevel(SoulAnchor entity, BlockPos pos) {
         return 15;
      }

      @Override
      public Identifier getTextureLocation(LivingEntityRenderState state) {
         return TEXTURE;
      }

      public LivingEntityRenderState createRenderState() {
         return new LivingEntityRenderState();
      }
   }

   static class BeastRenderer extends MobRenderer<DeathEntity, DeathModel.State, DeathBeastModel> {
      private static final Identifier TEXTURE = Renderers.tex("death_beast");
      private static final Identifier GLOW = Renderers.tex("death_beast_glow");

      BeastRenderer(Context ctx) {
         super(ctx, new DeathBeastModel(ctx.bakeLayer(DeathBoundClient.Layers.DEATH_BEAST)), 1.6F);
         this.addLayer(
            new LivingEntityEmissiveLayer<>(
               this,
               s -> GLOW,
               (s, age) -> 1.0F - Math.min(1.0F, s.collapse / 90.0F),
               new DeathBeastModel(ctx.bakeLayer(DeathBoundClient.Layers.DEATH_BEAST)),
               RenderTypes::eyes,
               true
            )
         );
      }

      public Identifier getTextureLocation(DeathModel.State state) {
         return TEXTURE;
      }

      public DeathModel.State createRenderState() {
         return new DeathModel.State();
      }

      public void extractRenderState(DeathEntity entity, DeathModel.State state, float partialTicks) {
         super.extractRenderState(entity, state, partialTicks);
         Renderers.extractDeath(entity, state, partialTicks);
      }
   }

   public static class DeathRenderer extends EntityRenderer<DeathEntity, DeathModel.State> {
      private final Renderers.ReaperRenderer reaper;
      private final Renderers.BeastRenderer beast;

      public DeathRenderer(Context ctx) {
         super(ctx);
         this.reaper = new Renderers.ReaperRenderer(ctx);
         this.beast = new Renderers.BeastRenderer(ctx);
      }

      public DeathModel.State createRenderState() {
         return new DeathModel.State();
      }

      public void extractRenderState(DeathEntity entity, DeathModel.State state, float partialTicks) {
         (entity.phase() == 3 ? this.beast : this.reaper).extractRenderState(entity, state, partialTicks);
      }

      protected AABB getBoundingBoxForCulling(DeathEntity entity, float partialTicks) {
         return super.getBoundingBoxForCulling(entity, partialTicks).inflate(4.0, 3.0, 4.0);
      }

      public void submit(DeathModel.State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
         if (state.phase == 3) {
            this.beast.submit(state, poseStack, collector, camera);
         } else {
            this.reaper.submit(state, poseStack, collector, camera);
         }
      }
   }

   public static class GraveboundRenderer extends MobRenderer<Gravebound, GraveboundModel.State, GraveboundModel> {
      private static final Identifier[] SKINS = new Identifier[]{
         Renderers.tex("gravebound_shrouded"), Renderers.tex("gravebound_bare"), Renderers.tex("gravebound_knight"), Renderers.tex("gravebound_charred")
      };
      private static final Identifier[] GLOWS = new Identifier[]{
         Renderers.tex("gravebound_shrouded_glow"),
         Renderers.tex("gravebound_bare_glow"),
         Renderers.tex("gravebound_knight_glow"),
         Renderers.tex("gravebound_charred_glow")
      };

      public GraveboundRenderer(Context ctx) {
         super(ctx, new GraveboundModel(ctx.bakeLayer(DeathBoundClient.Layers.GRAVEBOUND)), 0.45F);
         this.addLayer(
            new LivingEntityEmissiveLayer<>(
               this,
               s -> GLOWS[Math.floorMod(s.variant, GLOWS.length)],
               (s, age) -> 1.0F - Math.min(1.0F, s.collapse / 26.0F),
               new GraveboundModel(ctx.bakeLayer(DeathBoundClient.Layers.GRAVEBOUND)),
               RenderTypes::eyes,
               true
            )
         );
      }

      public Identifier getTextureLocation(GraveboundModel.State state) {
         return SKINS[Math.floorMod(state.variant, SKINS.length)];
      }

      public GraveboundModel.State createRenderState() {
         return new GraveboundModel.State();
      }

      public void extractRenderState(Gravebound entity, GraveboundModel.State state, float partialTicks) {
         super.extractRenderState(entity, state, partialTicks);
         state.rise.copyFrom(entity.riseAnim);
         state.attack.copyFrom(entity.attackAnim);
         state.variant = entity.variant();
         state.bare = entity.isArmed();
         state.collapse = entity.deathTime > 0 ? entity.deathTime + partialTicks : 0.0F;
         state.deathTime = 0.0F;
         state.hasRedOverlay = entity.hurtTime > 0;
      }
   }

   public static class GuardRenderer extends MobRenderer<DeathsGuard, DeathsGuardModel.State, DeathsGuardModel> {
      private static final Identifier TEXTURE = Renderers.tex("deaths_guard");
      private static final Identifier GLOW = Renderers.tex("deaths_guard_glow");

      public GuardRenderer(Context ctx) {
         super(ctx, new DeathsGuardModel(ctx.bakeLayer(DeathBoundClient.Layers.DEATHS_GUARD)), 1.0F);
         this.addLayer(
            new LivingEntityEmissiveLayer<>(
               this,
               s -> GLOW,
               (s, age) -> s.kneel ? 1.0F : 1.0F - Math.min(1.0F, s.collapse / 50.0F),
               new DeathsGuardModel(ctx.bakeLayer(DeathBoundClient.Layers.DEATHS_GUARD)),
               RenderTypes::eyes,
               true
            )
         );
      }

      protected AABB getBoundingBoxForCulling(DeathsGuard entity, float partialTicks) {
         return super.getBoundingBoxForCulling(entity, partialTicks).inflate(2.5, 1.5, 2.5);
      }

      public Identifier getTextureLocation(DeathsGuardModel.State state) {
         return TEXTURE;
      }

      public DeathsGuardModel.State createRenderState() {
         return new DeathsGuardModel.State();
      }

      public void extractRenderState(DeathsGuard entity, DeathsGuardModel.State state, float partialTicks) {
         super.extractRenderState(entity, state, partialTicks);
         state.sweep.copyFrom(entity.sweepAnim);
         state.slam.copyFrom(entity.slamAnim);
         state.lunge.copyFrom(entity.lungeAnim);
         state.inspect.copyFrom(entity.inspectAnim);
         state.judge.copyFrom(entity.judgeAnim);
         state.condemn.copyFrom(entity.condemnAnim);
         state.action = entity.action();
         state.readiness = Mth.lerp(partialTicks, entity.readinessO, entity.readiness);
         state.kneel = entity.kneeling();
         state.collapse = !state.kneel && entity.deathTime > 0 ? entity.deathTime + partialTicks : 0.0F;
         state.deathTime = 0.0F;
         state.hasRedOverlay = entity.hurtTime > 0;
      }
   }

   public static class HunterArrowRenderer extends ArrowRenderer<HunterArrow, ArrowRenderState> {
      private static final Identifier TEXTURE = DeathBound.id("textures/entity/projectiles/hunter_arrow.png");

      public HunterArrowRenderer(Context ctx) {
         super(ctx);
      }

      @Override
      protected Identifier getTextureLocation(ArrowRenderState state) {
         return TEXTURE;
      }

      public ArrowRenderState createRenderState() {
         return new ArrowRenderState();
      }

      protected int getBlockLightLevel(HunterArrow arrow, BlockPos pos) {
         return 15;
      }
   }

   public static class HunterRenderer extends MobRenderer<HollowHunter, HunterModel.State, HunterModel> {
      private static final Identifier TEXTURE = Renderers.tex("hollow_hunter");
      private static final Identifier GLOW = Renderers.tex("hollow_hunter_glow");

      public HunterRenderer(Context ctx) {
         super(ctx, new HunterModel(ctx.bakeLayer(DeathBoundClient.Layers.HOLLOW_HUNTER)), 0.6F);
         this.addLayer(
            new LivingEntityEmissiveLayer<>(
               this,
               s -> GLOW,
               (s, age) -> 1.0F - Math.min(1.0F, s.collapse / 35.0F),
               new HunterModel(ctx.bakeLayer(DeathBoundClient.Layers.HOLLOW_HUNTER)),
               RenderTypes::eyes,
               true
            )
         );
      }

      protected AABB getBoundingBoxForCulling(HollowHunter entity, float partialTicks) {
         return super.getBoundingBoxForCulling(entity, partialTicks).inflate(1.5, 1.0, 1.5);
      }

      public Identifier getTextureLocation(HunterModel.State state) {
         return TEXTURE;
      }

      public HunterModel.State createRenderState() {
         return new HunterModel.State();
      }

      public void extractRenderState(HollowHunter entity, HunterModel.State state, float partialTicks) {
         super.extractRenderState(entity, state, partialTicks);
         state.aim.copyFrom(entity.aimAnim);
         state.snare.copyFrom(entity.snareAnim);
         state.volley.copyFrom(entity.volleyAnim);
         state.leap.copyFrom(entity.leapAnim);
         state.vanish.copyFrom(entity.vanishAnim);
         state.riven.copyFrom(entity.rivenAnim);
         state.hunting = entity.isAggressive();
         state.collapse = entity.deathTime > 0 ? entity.deathTime + partialTicks : 0.0F;
         state.deathTime = 0.0F;
         state.hasRedOverlay = entity.hurtTime > 0;
      }
   }

   public static class KidRenderer extends MobRenderer<SkeletonKid, KidModel.State, KidModel> {
      private static final Identifier[] SKINS = new Identifier[]{
         Renderers.tex("skeleton_kid_0"), Renderers.tex("skeleton_kid_1"), Renderers.tex("skeleton_kid_2")
      };
      private static final Identifier GLOW = Renderers.tex("skeleton_kid_0_glow");

      public KidRenderer(Context ctx) {
         super(ctx, new KidModel(ctx.bakeLayer(DeathBoundClient.Layers.SKELETON_KID)), 0.3F);
         this.addLayer(
            new LivingEntityEmissiveLayer<>(
               this, s -> GLOW, (s, age) -> 1.0F, new KidModel(ctx.bakeLayer(DeathBoundClient.Layers.SKELETON_KID)), RenderTypes::eyes, true
            )
         );
      }

      public Identifier getTextureLocation(KidModel.State state) {
         return SKINS[Math.floorMod(state.variant, SKINS.length)];
      }

      public KidModel.State createRenderState() {
         return new KidModel.State();
      }

      public void extractRenderState(SkeletonKid entity, KidModel.State state, float partialTicks) {
         super.extractRenderState(entity, state, partialTicks);
         state.variant = entity.variant();
      }
   }

   public static class LostSoulRenderer extends MobRenderer<LostSoul, LostSoulModel.State, LostSoulModel> {
      private static final Identifier[] SKINS = new Identifier[]{
         Renderers.tex("lost_soul"),
         Renderers.tex("lost_soul_pale"),
         Renderers.tex("lost_soul_dusk"),
         Renderers.tex("lost_soul_drowned"),
         Renderers.tex("lost_soul_ashen"),
         Renderers.tex("lost_soul_veiled")
      };
      private static final Identifier[] GLOWS = new Identifier[]{
         Renderers.tex("lost_soul_glow"),
         Renderers.tex("lost_soul_pale_glow"),
         Renderers.tex("lost_soul_dusk_glow"),
         Renderers.tex("lost_soul_drowned_glow"),
         Renderers.tex("lost_soul_ashen_glow"),
         Renderers.tex("lost_soul_veiled_glow")
      };

      public LostSoulRenderer(Context ctx) {
         super(ctx, new LostSoulModel(ctx.bakeLayer(DeathBoundClient.Layers.LOST_SOUL)), 0.0F);
         this.addLayer(
            new LivingEntityEmissiveLayer<>(
               this,
               s -> GLOWS[Math.floorMod(s.tint, GLOWS.length)],
               (s, age) -> s.alpha,
               new LostSoulModel(ctx.bakeLayer(DeathBoundClient.Layers.LOST_SOUL)),
               RenderTypes::eyes,
               true
            )
         );
      }

      protected int getBlockLightLevel(LostSoul entity, BlockPos pos) {
         return 13;
      }

      protected int getModelTint(LostSoulModel.State state) {
         return ARGB.white(state.alpha);
      }

      public Identifier getTextureLocation(LostSoulModel.State state) {
         return SKINS[Math.floorMod(state.tint, SKINS.length)];
      }

      public LostSoulModel.State createRenderState() {
         return new LostSoulModel.State();
      }

      public void extractRenderState(LostSoul entity, LostSoulModel.State state, float partialTicks) {
         super.extractRenderState(entity, state, partialTicks);
         float fade = 1.0F - Mth.clamp((entity.fade() + (entity.fade() > 0 ? partialTicks : 0.0F)) / 24.0F, 0.0F, 1.0F);
         float flicker = 0.85F + 0.15F * Mth.sin(state.ageInTicks * 0.31F + entity.getId());
         state.alpha = Mth.clamp(fade * flicker * Math.min(1.0F, state.ageInTicks / 30.0F), 0.0F, 1.0F);
         state.waiting = entity.awaitingJudgement();
         state.seed = entity.getId();
         state.tint = entity.tint();
      }
   }

   public static class NpcRenderer extends MobRenderer<UnderworldNpc, NpcModels.State, EntityModel<NpcModels.State>> {
      private final Identifier texture;
      private final Identifier glow;

      public NpcRenderer(Context ctx, Function<ModelPart, EntityModel<NpcModels.State>> model, ModelLayerLocation layer, String name, float shadow) {
         super(ctx, model.apply(ctx.bakeLayer(layer)), shadow);
         this.texture = Renderers.tex(name);
         this.glow = Renderers.tex(name + "_glow");
         this.addLayer(new LivingEntityEmissiveLayer<>(this, s -> this.glow, (s, age) -> 1.0F, model.apply(ctx.bakeLayer(layer)), RenderTypes::eyes, true));
         if (name.equals("prophet")) {
            Identifier veins = Renderers.tex("prophet_corrupt");
            this.addLayer(
               new LivingEntityEmissiveLayer<>(
                  this, s -> veins, (s, age) -> s.corruption * (0.8F + 0.2F * Mth.sin(age * 0.15F)), model.apply(ctx.bakeLayer(layer)), RenderTypes::eyes, true
               )
            );
         }
      }

      protected int getBlockLightLevel(UnderworldNpc entity, BlockPos pos) {
         return Math.max(super.getBlockLightLevel(entity, pos), 10);
      }

      public Identifier getTextureLocation(NpcModels.State state) {
         return this.texture;
      }

      public NpcModels.State createRenderState() {
         return new NpcModels.State();
      }

      public void extractRenderState(UnderworldNpc entity, NpcModels.State state, float partialTicks) {
         super.extractRenderState(entity, state, partialTicks);
         state.talk.copyFrom(entity.talkAnim);
         state.idle.copyFrom(entity.idleAnim);
         state.temper.copyFrom(entity.temperAnim);
         state.seed = entity.getId();
         state.work = entity.work(partialTicks);
         state.freed = entity.blockPosition().distSqr(Layout.THRONE_SEAT) < 16.0;
         state.corruption = entity.corruption();
      }
   }

   static class ReaperRenderer extends MobRenderer<DeathEntity, DeathModel.State, DeathModel> {
      private static final Identifier TEXTURE = Renderers.tex("death_reaper");
      private static final Identifier GLOW = Renderers.tex("death_reaper_glow");

      ReaperRenderer(Context ctx) {
         super(ctx, new DeathModel(ctx.bakeLayer(DeathBoundClient.Layers.DEATH_REAPER)), 0.8F);
         this.addLayer(
            new LivingEntityEmissiveLayer<>(
               this, s -> GLOW, (s, age) -> 1.0F, new DeathModel(ctx.bakeLayer(DeathBoundClient.Layers.DEATH_REAPER)), RenderTypes::eyes, true
            )
         );
      }

      public Identifier getTextureLocation(DeathModel.State state) {
         return TEXTURE;
      }

      public DeathModel.State createRenderState() {
         return new DeathModel.State();
      }

      public void extractRenderState(DeathEntity entity, DeathModel.State state, float partialTicks) {
         super.extractRenderState(entity, state, partialTicks);
         Renderers.extractDeath(entity, state, partialTicks);
      }
   }

   public static class ShadeRenderer extends HumanoidMobRenderer<Shade, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
      private static final Identifier TEXTURE = Renderers.tex("shade");
      private static final Identifier EYES = Renderers.tex("shade_eyes");

      public ShadeRenderer(Context ctx) {
         super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), 0.0F);
         this.addLayer(
            new LivingEntityEmissiveLayer<>(this, s -> EYES, (s, age) -> 1.0F, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), RenderTypes::eyes, true)
         );
      }

      public Identifier getTextureLocation(HumanoidRenderState state) {
         return TEXTURE;
      }

      public HumanoidRenderState createRenderState() {
         return new HumanoidRenderState();
      }

      protected void scale(HumanoidRenderState state, PoseStack pose) {
         pose.scale(0.82F, 1.22F, 0.82F);
      }
   }

   public static class SoulFireRenderer<T extends Entity> extends EntityRenderer<T, EntityRenderState> {
      private static final RenderType FIRE = RenderTypes.entityCutout(Renderers.tex("purple_fire"));
      private final float size;

      public SoulFireRenderer(Context ctx, float size) {
         super(ctx);
         this.size = size;
      }

      @Override
      protected int getBlockLightLevel(T entity, BlockPos pos) {
         return 15;
      }

      @Override
      public EntityRenderState createRenderState() {
         return new EntityRenderState();
      }

      @Override
      public void submit(EntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
         int frame = (int)state.ageInTicks & 31;
         float v0 = frame / 32.0F;
         float v1 = (frame + 1) / 32.0F;
         pose.pushPose();
         pose.translate(0.0F, state.boundingBoxHeight * 0.5F, 0.0F);
         pose.rotate(camera.orientation);
         pose.scale(this.size, this.size, 1.0F);
         collector.submitCustomGeometry(
            pose,
            FIRE,
            (p, buffer) -> {
               float[][] v = new float[][]{{-0.5F, -0.5F, 0.0F, v1}, {0.5F, -0.5F, 1.0F, v1}, {0.5F, 0.5F, 1.0F, v0}, {-0.5F, 0.5F, 0.0F, v0}};

               for (float[] c : v) {
                  buffer.addVertex(p, c[0], c[1], 0.0F)
                     .setColor(-1)
                     .setUv(c[2], c[3])
                     .setOverlay(OverlayTexture.NO_OVERLAY)
                     .setLight(15728880)
                     .setNormal(p, 0.0F, 1.0F, 0.0F);
               }
            }
         );
         pose.popPose();
         super.submit(state, pose, collector, camera);
      }
   }

   public static class SoulWispRenderer extends MobRenderer<SoulWisp, SoulWispModel.State, SoulWispModel> {
      private static final Identifier[] SKINS = new Identifier[]{
         Renderers.tex("soul_wisp"), Renderers.tex("soul_wisp_pale"), Renderers.tex("soul_wisp_umbral")
      };
      private static final Identifier[] GLOWS = new Identifier[]{
         Renderers.tex("soul_wisp_glow"), Renderers.tex("soul_wisp_pale_glow"), Renderers.tex("soul_wisp_umbral_glow")
      };

      public SoulWispRenderer(Context ctx) {
         super(ctx, new SoulWispModel(ctx.bakeLayer(DeathBoundClient.Layers.SOUL_WISP)), 0.25F);
         this.addLayer(
            new LivingEntityEmissiveLayer<>(
               this,
               s -> GLOWS[Math.floorMod(s.variant, GLOWS.length)],
               (s, age) -> 1.0F,
               new SoulWispModel(ctx.bakeLayer(DeathBoundClient.Layers.SOUL_WISP)),
               RenderTypes::eyes,
               true
            )
         );
      }

      protected int getBlockLightLevel(SoulWisp entity, BlockPos pos) {
         return 12;
      }

      public Identifier getTextureLocation(SoulWispModel.State state) {
         return SKINS[Math.floorMod(state.variant, SKINS.length)];
      }

      public SoulWispModel.State createRenderState() {
         return new SoulWispModel.State();
      }

      public void extractRenderState(SoulWisp entity, SoulWispModel.State state, float partialTicks) {
         super.extractRenderState(entity, state, partialTicks);
         state.variant = entity.variant();
         state.spit.copyFrom(entity.spitAnim);
         state.deathTime = 0.0F;
         state.hasRedOverlay = entity.hurtTime > 0;
      }
   }

   public static class WispRenderer extends MobRenderer<LanternWisp, LivingEntityRenderState, WispModel> {
      private static final Identifier SKIN = Renderers.tex("lantern_wisp");
      private static final Identifier GLOW = Renderers.tex("lantern_wisp_glow");

      public WispRenderer(Context ctx) {
         super(ctx, new WispModel(ctx.bakeLayer(DeathBoundClient.Layers.LANTERN_WISP)), 0.0F);
         this.addLayer(
            new LivingEntityEmissiveLayer<>(
               this, s -> GLOW, (s, age) -> 1.0F, new WispModel(ctx.bakeLayer(DeathBoundClient.Layers.LANTERN_WISP)), RenderTypes::eyes, true
            )
         );
      }

      protected int getBlockLightLevel(LanternWisp entity, BlockPos pos) {
         return 15;
      }

      @Override
      public Identifier getTextureLocation(LivingEntityRenderState state) {
         return SKIN;
      }

      public LivingEntityRenderState createRenderState() {
         return new LivingEntityRenderState();
      }
   }
}
