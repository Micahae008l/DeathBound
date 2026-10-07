package com.deathbound.client.render;

import com.deathbound.DeathBound;
import com.deathbound.client.DeathBoundClient;
import com.deathbound.registry.ModAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class CrownedLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
   private static final Identifier SKIN = DeathBound.id("textures/entity/corruption.png");
   private static final Identifier GLOW = DeathBound.id("textures/entity/corruption_glow.png");
   private static final Identifier CROWN = DeathBound.id("textures/entity/crown.png");
   private final HumanoidModel<AvatarRenderState> crown;

   public CrownedLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, Context ctx) {
      super(parent);
      this.crown = new HumanoidModel<>(ctx.bakeLayer(DeathBoundClient.Layers.CROWN));
   }

   public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
      Minecraft mc = Minecraft.getInstance();
      Entity e = mc.level == null ? null : mc.level.getEntity(state.id);
      if (e != null && e.hasAttached(ModAttachments.CROWNED) && !state.isInvisible) {
         float k = Mth.clamp((float)(mc.level.getGameTime() - e.getAttached(ModAttachments.CROWNED)) / 100.0F, 0.0F, 1.0F);
         int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
         collector.order(1)
            .submitModel(this.getParentModel(), state, pose, RenderTypes.entityTranslucent(SKIN), light, overlay, ARGB.white(k), null, state.outlineColor);
         collector.order(2).submitModel(this.getParentModel(), state, pose, RenderTypes.eyes(GLOW), light, overlay, ARGB.white(k), null, state.outlineColor);
         if (k > 0.6F) {
            collector.order(3).submitModel(this.crown, state, pose, RenderTypes.entityCutout(CROWN), light, overlay, -1, null, state.outlineColor);
         }
      }
   }

   public static LayerDefinition crownMesh() {
      MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
      PartDefinition root = mesh.getRoot();
      PartDefinition head = root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-4.5F, -9.5F, -4.5F, 9.0F, 2.0F, 9.0F)
            .texOffs(0, 12)
            .addBox(-0.5F, -14.5F, -4.6F, 1.0F, 5.0F, 1.0F)
            .texOffs(4, 12)
            .addBox(-3.0F, -12.5F, -4.6F, 1.0F, 3.0F, 1.0F)
            .texOffs(4, 12)
            .addBox(2.0F, -12.5F, -4.6F, 1.0F, 3.0F, 1.0F)
            .texOffs(8, 12)
            .addBox(-4.6F, -13.0F, -1.0F, 1.0F, 4.0F, 1.0F)
            .texOffs(8, 12)
            .addBox(3.6F, -13.0F, -1.0F, 1.0F, 4.0F, 1.0F)
            .texOffs(12, 12)
            .addBox(-4.6F, -11.5F, 3.5F, 1.0F, 2.0F, 1.0F)
            .texOffs(12, 12)
            .addBox(3.6F, -11.5F, 3.5F, 1.0F, 2.0F, 1.0F)
            .texOffs(16, 12)
            .addBox(-0.5F, -12.0F, 3.6F, 1.0F, 3.0F, 1.0F),
         PartPose.ZERO
      );
      head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
      root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
      root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
      root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
      root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
      root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
      return LayerDefinition.create(mesh, 64, 32);
   }
}
