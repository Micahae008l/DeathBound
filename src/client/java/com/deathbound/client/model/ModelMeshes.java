package com.deathbound.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class ModelMeshes {
   private ModelMeshes() {
   }

   public static LayerDefinition gravebound() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition bodyPart = rootPart.addOrReplaceChild(
         "body",
         CubeListBuilder.create()
            .texOffs(24, 0)
            .addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F)
            .texOffs(0, 0)
            .addBox(-4.0F, -12.0F, -2.0F, 8.0F, 14.0F, 4.0F, new CubeDeformation(0.45F)),
         PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.1047F, 0.0F, 0.0F)
      );
      PartDefinition headPart = bodyPart.addOrReplaceChild(
         "head",
         CubeListBuilder.create().texOffs(24, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 6.0F, 8.0F).texOffs(36, 51).addBox(-6.0F, -8.0F, -4.4F, 12.0F, 4.0F, 0.0F),
         PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.1047F, 0.0F, 0.0F)
      );
      PartDefinition jawPart = headPart.addOrReplaceChild(
         "jaw", CubeListBuilder.create().texOffs(32, 42).addBox(-3.5F, 0.0F, -5.5F, 7.0F, 2.0F, 7.0F), PartPose.offset(0.0F, -2.0F, 2.0F)
      );
      PartDefinition rightArmPart = bodyPart.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F).texOffs(32, 30).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F),
         PartPose.offsetAndRotation(-5.0F, -10.0F, 0.0F, -0.1047F, 0.0F, 0.0F)
      );
      PartDefinition leftArmPart = bodyPart.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create().texOffs(56, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F).texOffs(0, 32).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F),
         PartPose.offsetAndRotation(5.0F, -10.0F, 0.0F, -0.1047F, 0.0F, 0.0F)
      );
      PartDefinition weaponPart = rightArmPart.addOrReplaceChild(
         "weapon",
         CubeListBuilder.create()
            .texOffs(0, 50)
            .addBox(-0.5F, -0.5F, -3.0F, 1.0F, 1.0F, 5.0F)
            .texOffs(0, 56)
            .addBox(-1.0F, -1.5F, -4.0F, 2.0F, 3.0F, 1.0F)
            .texOffs(0, 18)
            .addBox(-0.5F, -1.5F, -15.0F, 1.0F, 3.0F, 11.0F),
         PartPose.offsetAndRotation(0.0F, 9.0F, -0.5F, 1.0472F, 0.0F, 0.0F)
      );
      PartDefinition ragFrontPart = bodyPart.addOrReplaceChild(
         "rag_front", CubeListBuilder.create().texOffs(16, 44).addBox(-3.5F, 0.0F, 0.0F, 7.0F, 7.0F, 0.0F), PartPose.offset(0.0F, -0.5F, -2.5F)
      );
      PartDefinition ragBackPart = bodyPart.addOrReplaceChild(
         "rag_back", CubeListBuilder.create().texOffs(0, 42).addBox(-4.0F, 0.0F, 0.0F, 8.0F, 8.0F, 0.0F), PartPose.offset(0.0F, -0.5F, 2.5F)
      );
      PartDefinition rightLegPart = rootPart.addOrReplaceChild(
         "right_leg",
         CubeListBuilder.create()
            .texOffs(56, 28)
            .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F)
            .texOffs(12, 51)
            .addBox(-1.5F, 3.5F, -1.5F, 3.0F, 3.0F, 3.0F)
            .texOffs(36, 55)
            .addBox(-1.5F, 11.0F, -2.5F, 3.0F, 1.0F, 3.0F),
         PartPose.offset(-2.0F, -12.0F, 0.0F)
      );
      PartDefinition leftLegPart = rootPart.addOrReplaceChild(
         "left_leg",
         CubeListBuilder.create()
            .texOffs(24, 30)
            .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F)
            .texOffs(24, 51)
            .addBox(-1.5F, 3.5F, -1.5F, 3.0F, 3.0F, 3.0F)
            .texOffs(48, 55)
            .addBox(-1.5F, 11.0F, -2.5F, 3.0F, 1.0F, 3.0F),
         PartPose.offset(2.0F, -12.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition soulWisp() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition skullPart = rootPart.addOrReplaceChild(
         "skull", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F), PartPose.offset(0.0F, -5.0F, 0.0F)
      );
      PartDefinition jawPart = skullPart.addOrReplaceChild(
         "jaw", CubeListBuilder.create().texOffs(0, 46).addBox(-3.5F, 0.0F, -5.5F, 7.0F, 2.0F, 7.0F), PartPose.offset(0.0F, 0.0F, 2.0F)
      );
      PartDefinition flameAPart = skullPart.addOrReplaceChild(
         "flame_a",
         CubeListBuilder.create().texOffs(24, 0).addBox(-8.0F, -15.0F, 0.0F, 16.0F, 16.0F, 0.0F),
         PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, 0.7854F, 0.0F)
      );
      PartDefinition flameBPart = skullPart.addOrReplaceChild(
         "flame_b",
         CubeListBuilder.create().texOffs(24, 16).addBox(-8.0F, -15.0F, 0.0F, 16.0F, 16.0F, 0.0F),
         PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, -0.7854F, 0.0F)
      );
      PartDefinition crownPart = skullPart.addOrReplaceChild(
         "crown",
         CubeListBuilder.create().texOffs(48, 32).addBox(-4.0F, -8.0F, 0.0F, 8.0F, 8.0F, 0.0F).texOffs(0, 20).addBox(0.0F, -8.0F, -4.0F, 0.0F, 8.0F, 8.0F),
         PartPose.offset(0.0F, -6.0F, 0.0F)
      );
      PartDefinition tailPart = skullPart.addOrReplaceChild(
         "tail",
         CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -4.0F, 0.0F, 0.0F, 8.0F, 12.0F),
         PartPose.offsetAndRotation(0.0F, -2.0F, 3.5F, 0.1396F, 0.0F, 0.0F)
      );
      PartDefinition mote0Part = skullPart.addOrReplaceChild(
         "mote0", CubeListBuilder.create().texOffs(48, 40).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, -3.0F, 0.0F)
      );
      PartDefinition mote1Part = skullPart.addOrReplaceChild(
         "mote1", CubeListBuilder.create().texOffs(52, 40).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, -3.0F, 0.0F)
      );
      PartDefinition mote2Part = skullPart.addOrReplaceChild(
         "mote2", CubeListBuilder.create().texOffs(56, 40).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, -3.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition deathsGuard() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition hipsPart = rootPart.addOrReplaceChild(
         "hips",
         CubeListBuilder.create().texOffs(96, 72).addBox(-5.0F, -2.0F, -3.0F, 10.0F, 4.0F, 6.0F).texOffs(40, 84).addBox(-6.0F, -3.0F, -3.5F, 12.0F, 3.0F, 7.0F),
         PartPose.offset(0.0F, -20.0F, 0.0F)
      );
      PartDefinition chestPart = hipsPart.addOrReplaceChild(
         "chest",
         CubeListBuilder.create()
            .texOffs(42, 34)
            .addBox(-5.0F, -10.0F, -2.5F, 10.0F, 10.0F, 5.0F)
            .texOffs(84, 0)
            .addBox(-6.0F, -16.0F, -4.0F, 12.0F, 9.0F, 8.0F)
            .texOffs(0, 84)
            .addBox(-6.0F, -9.0F, -4.0F, 12.0F, 2.0F, 8.0F, new CubeDeformation(0.3F))
            .texOffs(60, 60)
            .addBox(-5.5F, -17.0F, -4.5F, 11.0F, 3.0F, 9.0F, new CubeDeformation(0.2F)),
         PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0698F, 0.0F, 0.0F)
      );
      PartDefinition headPart = chestPart.addOrReplaceChild(
         "head", CubeListBuilder.create().texOffs(44, 0).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 8.0F, 10.0F), PartPose.offset(0.0F, -16.0F, -0.5F)
      );
      PartDefinition jawPart = headPart.addOrReplaceChild(
         "jaw", CubeListBuilder.create().texOffs(0, 62).addBox(-4.5F, 0.0F, -7.0F, 9.0F, 3.0F, 9.0F), PartPose.offset(0.0F, -2.0F, 2.5F)
      );
      PartDefinition rightArmPart = chestPart.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create()
            .texOffs(44, 18)
            .addBox(-4.5F, -4.5F, -5.0F, 7.0F, 6.0F, 10.0F)
            .texOffs(36, 72)
            .addBox(-5.0F, -1.5F, -4.5F, 6.0F, 3.0F, 9.0F)
            .texOffs(100, 58)
            .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F)
            .texOffs(28, 102)
            .addBox(-2.0F, 3.0F, -2.0F, 4.0F, 2.0F, 4.0F),
         PartPose.offset(-8.0F, -13.5F, 0.0F)
      );
      PartDefinition rightForearmPart = rightArmPart.addOrReplaceChild(
         "right_forearm",
         CubeListBuilder.create()
            .texOffs(8, 40)
            .addBox(-2.5F, 0.0F, -2.5F, 5.0F, 8.0F, 5.0F)
            .texOffs(12, 94)
            .addBox(-3.0F, -0.5F, -3.0F, 6.0F, 2.0F, 6.0F)
            .texOffs(100, 100)
            .addBox(-2.0F, 8.0F, -2.0F, 4.0F, 3.0F, 4.0F),
         PartPose.offset(0.0F, 9.0F, 0.0F)
      );
      PartDefinition leftArmPart = chestPart.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create()
            .texOffs(8, 24)
            .addBox(-2.5F, -4.5F, -5.0F, 7.0F, 6.0F, 10.0F)
            .texOffs(66, 72)
            .addBox(-1.0F, -1.5F, -4.5F, 6.0F, 3.0F, 9.0F)
            .texOffs(112, 58)
            .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F)
            .texOffs(44, 102)
            .addBox(-2.0F, 3.0F, -2.0F, 4.0F, 2.0F, 4.0F),
         PartPose.offset(8.0F, -13.5F, 0.0F)
      );
      PartDefinition leftForearmPart = leftArmPart.addOrReplaceChild(
         "left_forearm",
         CubeListBuilder.create()
            .texOffs(94, 45)
            .addBox(-2.5F, 0.0F, -2.5F, 5.0F, 8.0F, 5.0F)
            .texOffs(36, 94)
            .addBox(-3.0F, -0.5F, -3.0F, 6.0F, 2.0F, 6.0F)
            .texOffs(12, 102)
            .addBox(-2.0F, 8.0F, -2.0F, 4.0F, 3.0F, 4.0F),
         PartPose.offset(0.0F, 9.0F, 0.0F)
      );
      PartDefinition weaponPart = rightForearmPart.addOrReplaceChild(
         "weapon",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-1.0F, -34.0F, -1.0F, 2.0F, 50.0F, 2.0F)
            .texOffs(104, 91)
            .addBox(-2.0F, -29.0F, -2.0F, 4.0F, 5.0F, 4.0F)
            .texOffs(0, 94)
            .addBox(-1.5F, -40.0F, -1.5F, 3.0F, 6.0F, 3.0F)
            .texOffs(122, 82)
            .addBox(-0.5F, -45.0F, -0.5F, 1.0F, 5.0F, 1.0F)
            .texOffs(116, 100)
            .addBox(-1.5F, 15.0F, -1.5F, 3.0F, 2.0F, 3.0F),
         PartPose.offset(0.0F, 9.5F, 0.0F)
      );
      PartDefinition bladePart = weaponPart.addOrReplaceChild(
         "blade",
         CubeListBuilder.create().texOffs(8, 0).addBox(-19.0F, -38.0F, 0.0F, 18.0F, 24.0F, 0.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F)
      );
      PartDefinition tabardFrontPart = hipsPart.addOrReplaceChild(
         "tabard_front", CubeListBuilder.create().texOffs(104, 17).addBox(-4.0F, 0.0F, 0.0F, 8.0F, 15.0F, 0.0F), PartPose.offset(0.0F, 0.0F, -3.8F)
      );
      PartDefinition tabardBackPart = hipsPart.addOrReplaceChild(
         "tabard_back", CubeListBuilder.create().texOffs(84, 17).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 17.0F, 0.0F), PartPose.offset(0.0F, 0.0F, 3.8F)
      );
      PartDefinition rightLegPart = rootPart.addOrReplaceChild(
         "right_leg",
         CubeListBuilder.create()
            .texOffs(114, 45)
            .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F)
            .texOffs(104, 32)
            .addBox(-3.0F, -1.0F, -3.5F, 5.0F, 7.0F, 6.0F)
            .texOffs(60, 100)
            .addBox(-2.5F, 8.0F, -3.0F, 5.0F, 3.0F, 5.0F),
         PartPose.offset(-3.0F, -20.0F, 0.0F)
      );
      PartDefinition rightShinPart = rightLegPart.addOrReplaceChild(
         "right_shin",
         CubeListBuilder.create().texOffs(72, 47).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 8.0F, 5.0F).texOffs(96, 82).addBox(-3.0F, 8.0F, -4.5F, 6.0F, 2.0F, 7.0F),
         PartPose.offset(0.0F, 10.0F, 0.0F)
      );
      PartDefinition leftLegPart = rootPart.addOrReplaceChild(
         "left_leg",
         CubeListBuilder.create()
            .texOffs(48, 49)
            .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F)
            .texOffs(72, 34)
            .addBox(-3.0F, -1.0F, -3.5F, 5.0F, 7.0F, 6.0F)
            .texOffs(80, 100)
            .addBox(-2.5F, 8.0F, -3.0F, 5.0F, 3.0F, 5.0F),
         PartPose.offset(3.0F, -20.0F, 0.0F)
      );
      PartDefinition leftShinPart = leftLegPart.addOrReplaceChild(
         "left_shin",
         CubeListBuilder.create().texOffs(28, 49).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 8.0F, 5.0F).texOffs(78, 91).addBox(-3.0F, 8.0F, -4.5F, 6.0F, 2.0F, 7.0F),
         PartPose.offset(0.0F, 10.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 128, 128);
   }

   public static LayerDefinition deathReaper() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition hipsPart = rootPart.addOrReplaceChild("hips", CubeListBuilder.create(), PartPose.offset(0.0F, -22.0F, 0.0F));
      PartDefinition chestPart = hipsPart.addOrReplaceChild(
         "chest",
         CubeListBuilder.create()
            .texOffs(96, 0)
            .addBox(-4.0F, -14.0F, -2.5F, 8.0F, 14.0F, 5.0F)
            .texOffs(64, 0)
            .addBox(-5.0F, -15.0F, -3.0F, 10.0F, 15.0F, 6.0F, new CubeDeformation(0.25F))
            .texOffs(76, 53)
            .addBox(-6.5F, -15.5F, -3.5F, 13.0F, 5.0F, 7.0F, new CubeDeformation(0.2F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition capePart = chestPart.addOrReplaceChild(
         "cape", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.0F, 0.0F, 12.0F, 37.0F, 0.0F), PartPose.offset(0.0F, -14.5F, 3.9F)
      );
      PartDefinition headPart = chestPart.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(76, 37)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
            .texOffs(24, 0)
            .addBox(-5.0F, -10.0F, -5.6F, 10.0F, 11.0F, 10.0F)
            .texOffs(8, 55)
            .addBox(-2.0F, -11.0F, -1.0F, 4.0F, 1.0F, 5.0F),
         PartPose.offset(0.0F, -15.0F, -0.5F)
      );
      PartDefinition rightArmPart = chestPart.addOrReplaceChild(
         "right_arm", CubeListBuilder.create().texOffs(108, 37).addBox(-2.5F, -2.0F, -2.5F, 5.0F, 11.0F, 5.0F), PartPose.offset(-6.5F, -13.0F, 0.0F)
      );
      PartDefinition rightForearmPart = rightArmPart.addOrReplaceChild(
         "right_forearm",
         CubeListBuilder.create().texOffs(44, 39).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 7.0F, 6.0F).texOffs(116, 53).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F),
         PartPose.offset(0.0F, 9.0F, 0.0F)
      );
      PartDefinition rightHandPart = rightForearmPart.addOrReplaceChild(
         "right_hand",
         CubeListBuilder.create().texOffs(116, 62).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F).texOffs(26, 55).addBox(-1.5F, 2.0F, -1.5F, 3.0F, 3.0F, 3.0F),
         PartPose.offset(0.0F, 6.5F, 0.0F)
      );
      PartDefinition rightOrbPart = rightHandPart.addOrReplaceChild(
         "right_orb", CubeListBuilder.create().texOffs(8, 61).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(0.0F, 4.0F, -2.5F)
      );
      PartDefinition leftArmPart = chestPart.addOrReplaceChild(
         "left_arm", CubeListBuilder.create().texOffs(24, 39).addBox(-2.5F, -2.0F, -2.5F, 5.0F, 11.0F, 5.0F), PartPose.offset(6.5F, -13.0F, 0.0F)
      );
      PartDefinition leftForearmPart = leftArmPart.addOrReplaceChild(
         "left_forearm",
         CubeListBuilder.create().texOffs(44, 52).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 7.0F, 6.0F).texOffs(0, 55).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F),
         PartPose.offset(0.0F, 9.0F, 0.0F)
      );
      PartDefinition leftHandPart = leftForearmPart.addOrReplaceChild(
         "left_hand",
         CubeListBuilder.create().texOffs(44, 65).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F).texOffs(20, 61).addBox(-1.5F, 2.0F, -1.5F, 3.0F, 3.0F, 3.0F),
         PartPose.offset(0.0F, 6.5F, 0.0F)
      );
      PartDefinition leftOrbPart = leftHandPart.addOrReplaceChild(
         "left_orb", CubeListBuilder.create().texOffs(32, 61).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(0.0F, 4.0F, -2.5F)
      );
      PartDefinition rightLegPart = hipsPart.addOrReplaceChild(
         "right_leg",
         CubeListBuilder.create()
            .texOffs(68, 39)
            .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F)
            .texOffs(52, 21)
            .addBox(-3.0F, -1.0F, -3.0F, 6.0F, 12.0F, 6.0F, new CubeDeformation(0.2F)),
         PartPose.offset(-2.5F, 0.0F, 0.0F)
      );
      PartDefinition rightShinPart = rightLegPart.addOrReplaceChild(
         "right_shin",
         CubeListBuilder.create().texOffs(96, 19).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 11.0F, 7.0F).texOffs(56, 65).addBox(-1.0F, 10.0F, -3.0F, 2.0F, 1.0F, 3.0F),
         PartPose.offset(0.0F, 11.0F, 0.0F)
      );
      PartDefinition leftLegPart = hipsPart.addOrReplaceChild(
         "left_leg",
         CubeListBuilder.create()
            .texOffs(68, 52)
            .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F)
            .texOffs(0, 37)
            .addBox(-3.0F, -1.0F, -3.0F, 6.0F, 12.0F, 6.0F, new CubeDeformation(0.2F)),
         PartPose.offset(2.5F, 0.0F, 0.0F)
      );
      PartDefinition leftShinPart = leftLegPart.addOrReplaceChild(
         "left_shin",
         CubeListBuilder.create().texOffs(24, 21).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 11.0F, 7.0F).texOffs(66, 65).addBox(-1.0F, 10.0F, -3.0F, 2.0F, 1.0F, 3.0F),
         PartPose.offset(0.0F, 11.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 128, 128);
   }

   public static LayerDefinition deathBeast() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition pelvisPart = rootPart.addOrReplaceChild(
         "pelvis", CubeListBuilder.create().texOffs(194, 41).addBox(-7.0F, -3.0F, -4.0F, 14.0F, 6.0F, 8.0F), PartPose.offset(0.0F, -32.0F, 6.0F)
      );
      PartDefinition loinFrontPart = pelvisPart.addOrReplaceChild(
         "loin_front", CubeListBuilder.create().texOffs(142, 25).addBox(-6.0F, 0.0F, 0.0F, 12.0F, 16.0F, 0.0F), PartPose.offset(0.0F, 2.0F, -4.4F)
      );
      PartDefinition torsoPart = pelvisPart.addOrReplaceChild(
         "torso",
         CubeListBuilder.create()
            .texOffs(98, 0)
            .addBox(-1.5F, -24.0F, 2.5F, 3.0F, 24.0F, 3.0F)
            .texOffs(166, 38)
            .addBox(-4.0F, -19.0F, -4.0F, 8.0F, 9.0F, 6.0F)
            .texOffs(44, 0)
            .addBox(-8.0F, -24.0F, -6.0F, 16.0F, 18.0F, 11.0F)
            .texOffs(44, 29)
            .addBox(-11.0F, -27.0F, -5.0F, 22.0F, 5.0F, 10.0F),
         PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.8378F, 0.0F, 0.0F)
      );
      PartDefinition cloakPart = torsoPart.addOrReplaceChild(
         "cloak",
         CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, 0.0F, 0.0F, 22.0F, 30.0F, 0.0F),
         PartPose.offsetAndRotation(0.0F, -26.0F, 5.5F, -0.6981F, 0.0F, 0.0F)
      );
      PartDefinition neckPart = torsoPart.addOrReplaceChild(
         "neck",
         CubeListBuilder.create().texOffs(238, 21).addBox(-2.0F, -7.0F, -2.0F, 4.0F, 8.0F, 4.0F),
         PartPose.offsetAndRotation(0.0F, -25.0F, -3.0F, 0.384F, 0.0F, 0.0F)
      );
      PartDefinition headPart = neckPart.addOrReplaceChild(
         "head",
         CubeListBuilder.create().texOffs(166, 0).addBox(-6.0F, -10.0F, -9.0F, 12.0F, 10.0F, 12.0F),
         PartPose.offsetAndRotation(0.0F, -6.0F, -1.0F, -1.3963F, 0.0F, 0.0F)
      );
      PartDefinition jawPart = headPart.addOrReplaceChild(
         "jaw", CubeListBuilder.create().texOffs(0, 30).addBox(-5.5F, 0.0F, -11.5F, 11.0F, 4.0F, 11.0F), PartPose.offset(0.0F, 0.0F, 2.0F)
      );
      PartDefinition rightArmPart = torsoPart.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create()
            .texOffs(138, 41)
            .addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F)
            .texOffs(110, 0)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 22.0F, 4.0F)
            .texOffs(166, 22)
            .addBox(-3.5F, 2.0F, -3.5F, 7.0F, 9.0F, 7.0F),
         PartPose.offsetAndRotation(-12.0F, -24.0F, 0.0F, -0.8378F, 0.0F, 0.0F)
      );
      PartDefinition rightForearmPart = rightArmPart.addOrReplaceChild(
         "right_forearm",
         CubeListBuilder.create().texOffs(142, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 22.0F, 3.0F).texOffs(24, 45).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 3.0F, 5.0F),
         PartPose.offsetAndRotation(0.0F, 21.0F, 0.0F, -0.3491F, 0.0F, 0.0F)
      );
      PartDefinition rightHandPart = rightForearmPart.addOrReplaceChild(
         "right_hand",
         CubeListBuilder.create()
            .texOffs(0, 45)
            .addBox(-3.0F, 0.0F, -3.0F, 6.0F, 3.0F, 6.0F)
            .texOffs(162, 41)
            .addBox(-2.5F, 2.5F, -3.0F, 1.0F, 9.0F, 1.0F)
            .texOffs(238, 33)
            .addBox(-1.0F, 2.5F, -3.5F, 1.0F, 10.0F, 1.0F)
            .texOffs(242, 33)
            .addBox(0.5F, 2.5F, -3.5F, 1.0F, 10.0F, 1.0F)
            .texOffs(132, 42)
            .addBox(2.0F, 2.5F, -3.0F, 1.0F, 9.0F, 1.0F)
            .texOffs(104, 44)
            .addBox(-3.5F, 1.0F, 0.0F, 1.0F, 6.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 22.0F, 0.0F, 0.1745F, 0.0F, 0.0F)
      );
      PartDefinition leftArmPart = torsoPart.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create()
            .texOffs(108, 42)
            .addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F)
            .texOffs(126, 0)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 22.0F, 4.0F)
            .texOffs(110, 26)
            .addBox(-3.5F, 2.0F, -3.5F, 7.0F, 9.0F, 7.0F),
         PartPose.offsetAndRotation(12.0F, -24.0F, 0.0F, -0.8378F, 0.0F, 0.0F)
      );
      PartDefinition leftForearmPart = leftArmPart.addOrReplaceChild(
         "left_forearm",
         CubeListBuilder.create().texOffs(154, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 22.0F, 3.0F).texOffs(24, 53).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 3.0F, 5.0F),
         PartPose.offsetAndRotation(0.0F, 21.0F, 0.0F, -0.3491F, 0.0F, 0.0F)
      );
      PartDefinition leftHandPart = leftForearmPart.addOrReplaceChild(
         "left_hand",
         CubeListBuilder.create()
            .texOffs(132, 53)
            .addBox(-3.0F, 0.0F, -3.0F, 6.0F, 3.0F, 6.0F)
            .texOffs(96, 44)
            .addBox(-2.5F, 2.5F, -3.0F, 1.0F, 9.0F, 1.0F)
            .texOffs(246, 33)
            .addBox(-1.0F, 2.5F, -3.5F, 1.0F, 10.0F, 1.0F)
            .texOffs(250, 33)
            .addBox(0.5F, 2.5F, -3.5F, 1.0F, 10.0F, 1.0F)
            .texOffs(100, 44)
            .addBox(2.0F, 2.5F, -3.0F, 1.0F, 9.0F, 1.0F)
            .texOffs(238, 44)
            .addBox(2.5F, 1.0F, 0.0F, 1.0F, 6.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 22.0F, 0.0F, 0.1745F, 0.0F, 0.0F)
      );
      PartDefinition rightThighPart = pelvisPart.addOrReplaceChild(
         "right_thigh",
         CubeListBuilder.create().texOffs(214, 0).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 17.0F, 4.0F),
         PartPose.offsetAndRotation(-5.5F, 0.0F, 0.0F, -0.6109F, 0.0F, 0.0F)
      );
      PartDefinition rightShinPart = rightThighPart.addOrReplaceChild(
         "right_shin",
         CubeListBuilder.create().texOffs(214, 21).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 17.0F, 3.0F).texOffs(156, 53).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 3.0F, 5.0F),
         PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.9599F, 0.0F, 0.0F)
      );
      PartDefinition rightFootPart = rightShinPart.addOrReplaceChild(
         "right_foot",
         CubeListBuilder.create()
            .texOffs(44, 44)
            .addBox(-2.5F, 0.0F, -6.0F, 5.0F, 2.0F, 8.0F)
            .texOffs(242, 44)
            .addBox(-2.5F, 0.0F, -8.0F, 1.0F, 2.0F, 2.0F)
            .texOffs(248, 44)
            .addBox(-0.5F, 0.0F, -8.0F, 1.0F, 2.0F, 2.0F)
            .texOffs(242, 48)
            .addBox(1.5F, 0.0F, -8.0F, 1.0F, 2.0F, 2.0F),
         PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, -0.3491F, 0.0F, 0.0F)
      );
      PartDefinition leftThighPart = pelvisPart.addOrReplaceChild(
         "left_thigh",
         CubeListBuilder.create().texOffs(230, 0).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 17.0F, 4.0F),
         PartPose.offsetAndRotation(5.5F, 0.0F, 0.0F, -0.6109F, 0.0F, 0.0F)
      );
      PartDefinition leftShinPart = leftThighPart.addOrReplaceChild(
         "left_shin",
         CubeListBuilder.create().texOffs(226, 21).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 17.0F, 3.0F).texOffs(0, 54).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 3.0F, 5.0F),
         PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.9599F, 0.0F, 0.0F)
      );
      PartDefinition leftFootPart = leftShinPart.addOrReplaceChild(
         "left_foot",
         CubeListBuilder.create()
            .texOffs(70, 44)
            .addBox(-2.5F, 0.0F, -6.0F, 5.0F, 2.0F, 8.0F)
            .texOffs(248, 48)
            .addBox(-2.5F, 0.0F, -8.0F, 1.0F, 2.0F, 2.0F)
            .texOffs(238, 52)
            .addBox(-0.5F, 0.0F, -8.0F, 1.0F, 2.0F, 2.0F)
            .texOffs(244, 52)
            .addBox(1.5F, 0.0F, -8.0F, 1.0F, 2.0F, 2.0F),
         PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, -0.3491F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 256, 128);
   }

   public static LayerDefinition lostSoul() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition bodyPart = rootPart.addOrReplaceChild(
         "body", CubeListBuilder.create().texOffs(36, 0).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F), PartPose.offset(0.0F, -16.0F, 0.0F)
      );
      PartDefinition headPart = bodyPart.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 18)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
            .texOffs(0, 0)
            .addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.1F)),
         PartPose.offset(0.0F, -12.0F, 0.0F)
      );
      PartDefinition rightArmPart = bodyPart.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create().texOffs(36, 16).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 11.0F, 3.0F),
         PartPose.offsetAndRotation(-5.0F, -11.0F, 0.0F, -0.3491F, 0.0F, 0.0F)
      );
      PartDefinition leftArmPart = bodyPart.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create().texOffs(48, 16).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 11.0F, 3.0F),
         PartPose.offsetAndRotation(5.0F, -11.0F, 0.0F, -0.3491F, 0.0F, 0.0F)
      );
      PartDefinition tailPart = bodyPart.addOrReplaceChild(
         "tail", CubeListBuilder.create().texOffs(32, 30).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 6.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition tail2Part = tailPart.addOrReplaceChild(
         "tail2", CubeListBuilder.create().texOffs(0, 34).addBox(-2.0F, 0.0F, -1.5F, 4.0F, 5.0F, 3.0F), PartPose.offset(0.0F, 6.0F, 0.0F)
      );
      PartDefinition tail3Part = tail2Part.addOrReplaceChild(
         "tail3", CubeListBuilder.create().texOffs(52, 30).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F), PartPose.offset(0.0F, 5.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition soulAnchor() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition cagePart = rootPart.addOrReplaceChild(
         "cage",
         CubeListBuilder.create()
            .texOffs(0, 28)
            .addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F)
            .texOffs(16, 0)
            .addBox(-6.0F, -9.0F, -6.0F, 12.0F, 2.0F, 12.0F)
            .texOffs(16, 14)
            .addBox(-6.0F, 7.0F, -6.0F, 12.0F, 2.0F, 12.0F)
            .texOffs(0, 0)
            .addBox(-5.0F, -7.0F, -5.0F, 1.0F, 14.0F, 1.0F)
            .texOffs(4, 0)
            .addBox(4.0F, -7.0F, -5.0F, 1.0F, 14.0F, 1.0F)
            .texOffs(8, 0)
            .addBox(-5.0F, -7.0F, 4.0F, 1.0F, 14.0F, 1.0F)
            .texOffs(12, 0)
            .addBox(4.0F, -7.0F, 4.0F, 1.0F, 14.0F, 1.0F)
            .texOffs(50, 32)
            .addBox(-1.0F, -11.0F, -7.0F, 2.0F, 2.0F, 1.0F)
            .texOffs(56, 32)
            .addBox(-1.0F, -11.0F, 6.0F, 2.0F, 2.0F, 1.0F)
            .texOffs(50, 28)
            .addBox(-7.0F, -11.0F, -1.0F, 1.0F, 2.0F, 2.0F)
            .texOffs(56, 28)
            .addBox(6.0F, -11.0F, -1.0F, 1.0F, 2.0F, 2.0F)
            .texOffs(42, 28)
            .addBox(-1.0F, -12.0F, -1.0F, 2.0F, 3.0F, 2.0F),
         PartPose.offset(0.0F, -14.0F, 0.0F)
      );
      PartDefinition chainsPart = cagePart.addOrReplaceChild(
         "chains",
         CubeListBuilder.create()
            .texOffs(30, 28)
            .addBox(-4.0F, 0.0F, -4.0F, 3.0F, 8.0F, 0.0F)
            .texOffs(36, 28)
            .addBox(1.0F, 0.0F, 3.0F, 3.0F, 6.0F, 0.0F)
            .texOffs(24, 28)
            .addBox(4.0F, 0.0F, -2.0F, 0.0F, 7.0F, 3.0F),
         PartPose.offset(0.0F, 9.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition ferryman() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition hipsPart = rootPart.addOrReplaceChild(
         "hips", CubeListBuilder.create().texOffs(72, 20).addBox(-5.0F, 0.0F, -4.0F, 10.0F, 11.0F, 8.0F), PartPose.offset(0.0F, -20.0F, 0.0F)
      );
      PartDefinition skirtPart = hipsPart.addOrReplaceChild(
         "skirt", CubeListBuilder.create().texOffs(72, 0).addBox(-6.0F, 0.0F, -5.0F, 12.0F, 10.0F, 10.0F), PartPose.offset(0.0F, 10.0F, 0.0F)
      );
      PartDefinition chestPart = hipsPart.addOrReplaceChild(
         "chest",
         CubeListBuilder.create()
            .texOffs(32, 21)
            .addBox(-4.5F, -13.0F, -3.0F, 9.0F, 13.0F, 6.0F)
            .texOffs(8, 47)
            .addBox(-6.0F, -14.0F, -4.0F, 12.0F, 5.0F, 8.0F, new CubeDeformation(0.2F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2793F, 0.0F, 0.0F)
      );
      PartDefinition headPart = chestPart.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(62, 39)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
            .texOffs(32, 0)
            .addBox(-5.0F, -10.0F, -5.5F, 10.0F, 11.0F, 10.0F),
         PartPose.offsetAndRotation(0.0F, -13.0F, -1.0F, -0.2443F, 0.0F, 0.0F)
      );
      PartDefinition hoodTipPart = headPart.addOrReplaceChild(
         "hood_tip",
         CubeListBuilder.create().texOffs(48, 55).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 3.0F, 6.0F),
         PartPose.offsetAndRotation(0.0F, -9.5F, 4.0F, -0.6632F, 0.0F, 0.0F)
      );
      PartDefinition beardPart = headPart.addOrReplaceChild(
         "beard", CubeListBuilder.create().texOffs(114, 47).addBox(-2.5F, 0.0F, 0.0F, 5.0F, 9.0F, 0.0F), PartPose.offset(0.0F, -2.0F, -4.1F)
      );
      PartDefinition rightArmPart = chestPart.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create().texOffs(108, 20).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 11.0F, 4.0F),
         PartPose.offsetAndRotation(-6.0F, -12.0F, 0.0F, -0.6632F, 0.0F, 0.1396F)
      );
      PartDefinition rightForearmPart = rightArmPart.addOrReplaceChild(
         "right_forearm",
         CubeListBuilder.create().texOffs(108, 35).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 7.0F, 5.0F).texOffs(0, 52).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F),
         PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, -0.6981F, 0.0F, 0.0F)
      );
      PartDefinition rightHandPart = rightForearmPart.addOrReplaceChild(
         "right_hand", CubeListBuilder.create().texOffs(114, 56).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(0.0F, 7.0F, 0.0F)
      );
      PartDefinition leftArmPart = chestPart.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create().texOffs(8, 32).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 11.0F, 4.0F),
         PartPose.offsetAndRotation(6.0F, -12.0F, 0.0F, -0.1745F, 0.0F, -0.1047F)
      );
      PartDefinition leftForearmPart = leftArmPart.addOrReplaceChild(
         "left_forearm",
         CubeListBuilder.create().texOffs(94, 47).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 7.0F, 5.0F).texOffs(84, 55).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F),
         PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, -0.4363F, 0.0F, 0.0F)
      );
      PartDefinition leftHandPart = leftForearmPart.addOrReplaceChild(
         "left_hand", CubeListBuilder.create().texOffs(8, 60).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(0.0F, 7.0F, 0.0F)
      );
      PartDefinition polePart = rightHandPart.addOrReplaceChild(
         "pole",
         CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -30.0F, -1.0F, 2.0F, 50.0F, 2.0F).texOffs(112, 62).addBox(-1.0F, -31.0F, -1.0F, 6.0F, 1.0F, 2.0F),
         PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, 1.1519F, 0.0F, -0.1396F)
      );
      PartDefinition cloakPart = hipsPart.addOrReplaceChild(
         "cloak",
         CubeListBuilder.create().texOffs(8, 0).addBox(-6.0F, 0.0F, 0.0F, 12.0F, 32.0F, 0.0F),
         PartPose.offsetAndRotation(0.0F, -12.0F, 4.2F, 0.0873F, 0.0F, 0.0F)
      );
      PartDefinition lanternPart = polePart.addOrReplaceChild(
         "lantern",
         CubeListBuilder.create()
            .texOffs(32, 60)
            .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F)
            .texOffs(68, 55)
            .addBox(-2.0F, 3.0F, -2.0F, 4.0F, 5.0F, 4.0F)
            .texOffs(20, 60)
            .addBox(-1.5F, 4.5F, -1.5F, 3.0F, 3.0F, 3.0F)
            .texOffs(92, 59)
            .addBox(-2.5F, 2.5F, -2.5F, 5.0F, 1.0F, 5.0F),
         PartPose.offset(4.0F, -30.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 128, 128);
   }

   public static LayerDefinition gravedigger() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition bodyPart = rootPart.addOrReplaceChild(
         "body",
         CubeListBuilder.create()
            .texOffs(100, 0)
            .addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.1F))
            .texOffs(56, 30)
            .addBox(-4.5F, -12.5F, -2.5F, 9.0F, 3.0F, 5.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.1396F, 0.0F, 0.0F)
      );
      PartDefinition coatTailPart = bodyPart.addOrReplaceChild(
         "coat_tail",
         CubeListBuilder.create().texOffs(100, 16).addBox(-4.5F, -0.5F, -2.5F, 9.0F, 8.0F, 5.0F, new CubeDeformation(0.15F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition headPart = bodyPart.addOrReplaceChild(
         "head",
         CubeListBuilder.create().texOffs(68, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
         PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.1396F, 0.0F, 0.0F)
      );
      PartDefinition hatPart = headPart.addOrReplaceChild(
         "hat",
         CubeListBuilder.create().texOffs(4, 17).addBox(-4.5F, -4.0F, -4.5F, 9.0F, 4.0F, 9.0F).texOffs(4, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 1.0F, 16.0F),
         PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, -0.0698F, 0.0F, 0.0524F)
      );
      PartDefinition rightArmPart = bodyPart.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create().texOffs(40, 17).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.15F)),
         PartPose.offsetAndRotation(-6.0F, -10.0F, 0.0F, -0.4363F, 0.0F, 0.1047F)
      );
      PartDefinition rightForearmPart = rightArmPart.addOrReplaceChild(
         "right_forearm",
         CubeListBuilder.create()
            .texOffs(0, 30)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.1F))
            .texOffs(104, 38)
            .addBox(-1.5F, 5.0F, -1.5F, 3.0F, 2.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, -0.8727F, 0.0F, 0.0F)
      );
      PartDefinition leftArmPart = bodyPart.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create().texOffs(40, 28).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.15F)),
         PartPose.offsetAndRotation(6.0F, -10.0F, 0.0F, -0.6109F, 0.0F, -0.2094F)
      );
      PartDefinition leftForearmPart = leftArmPart.addOrReplaceChild(
         "left_forearm",
         CubeListBuilder.create()
            .texOffs(16, 30)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.1F))
            .texOffs(116, 38)
            .addBox(-1.5F, 5.0F, -1.5F, 3.0F, 2.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, -0.7854F, 0.0F, 0.0F)
      );
      PartDefinition shovelPart = bodyPart.addOrReplaceChild(
         "shovel",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-0.5F, -12.0F, -0.5F, 1.0F, 22.0F, 1.0F)
            .texOffs(32, 36)
            .addBox(-1.5F, -13.0F, -0.5F, 3.0F, 1.0F, 1.0F)
            .texOffs(118, 29)
            .addBox(-2.0F, 8.0F, -0.5F, 4.0F, 6.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, -7.0F, 2.8F, 0.0F, 0.0F, 0.6632F)
      );
      PartDefinition hammerPart = rightForearmPart.addOrReplaceChild(
         "hammer",
         CubeListBuilder.create().texOffs(100, 29).addBox(-0.5F, -0.5F, -7.0F, 1.0F, 1.0F, 8.0F).texOffs(32, 30).addBox(-1.0F, -2.0F, -9.0F, 2.0F, 4.0F, 2.0F),
         PartPose.offset(0.0F, 6.5F, 0.0F)
      );
      PartDefinition lanternPart = bodyPart.addOrReplaceChild(
         "lantern",
         CubeListBuilder.create().texOffs(32, 38).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F).texOffs(92, 38).addBox(-1.5F, 1.0F, -1.5F, 3.0F, 4.0F, 3.0F),
         PartPose.offset(3.5F, -2.0F, -2.5F)
      );
      PartDefinition rightLegPart = rootPart.addOrReplaceChild(
         "right_leg",
         CubeListBuilder.create()
            .texOffs(68, 16)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F)
            .texOffs(56, 38)
            .addBox(-2.0F, 9.0F, -2.5F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.15F)),
         PartPose.offset(-2.0F, -12.0F, 0.0F)
      );
      PartDefinition leftLegPart = rootPart.addOrReplaceChild(
         "left_leg",
         CubeListBuilder.create()
            .texOffs(84, 16)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F)
            .texOffs(74, 38)
            .addBox(-2.0F, 9.0F, -2.5F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.15F)),
         PartPose.offset(2.0F, -12.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 128, 128);
   }

   public static LayerDefinition prophet() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition hipsPart = rootPart.addOrReplaceChild(
         "hips",
         CubeListBuilder.create()
            .texOffs(52, 36)
            .addBox(-4.0F, -2.0F, -2.0F, 8.0F, 4.0F, 4.0F)
            .texOffs(78, 0)
            .addBox(-4.5F, -2.5F, -2.5F, 9.0F, 9.0F, 5.0F, new CubeDeformation(0.1F)),
         PartPose.offset(0.0F, -10.0F, 0.0F)
      );
      PartDefinition bodyPart = hipsPart.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(106, 0).addBox(-3.5F, -10.0F, -2.0F, 7.0F, 10.0F, 4.0F),
         PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.1745F, 0.0F, 0.0F)
      );
      PartDefinition headPart = bodyPart.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(8, 0)
            .addBox(-3.5F, -8.0F, -3.5F, 7.0F, 8.0F, 7.0F)
            .texOffs(0, 36)
            .addBox(-3.5F, -6.0F, -3.5F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.15F)),
         PartPose.offsetAndRotation(0.0F, -10.0F, -0.5F, 0.3491F, 0.0F, 0.0F)
      );
      PartDefinition lock0Part = headPart.addOrReplaceChild(
         "lock_0",
         CubeListBuilder.create().texOffs(62, 0).addBox(0.0F, 0.0F, -2.0F, 0.0F, 11.0F, 4.0F),
         PartPose.offsetAndRotation(-3.9F, -6.0F, -0.5F, 0.0F, 0.0F, -0.1047F)
      );
      PartDefinition lock2Part = headPart.addOrReplaceChild(
         "lock_2",
         CubeListBuilder.create().texOffs(70, 0).addBox(0.0F, 0.0F, -2.0F, 0.0F, 11.0F, 4.0F),
         PartPose.offsetAndRotation(3.9F, -6.0F, -0.5F, 0.0F, 0.0F, 0.1047F)
      );
      PartDefinition hairPart = headPart.addOrReplaceChild(
         "hair", CubeListBuilder.create().texOffs(78, 14).addBox(-3.5F, 0.0F, 0.0F, 7.0F, 13.0F, 0.0F), PartPose.offset(0.0F, -6.0F, 3.7F)
      );
      PartDefinition rightArmPart = bodyPart.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create().texOffs(38, 15).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 8.0F, 3.0F),
         PartPose.offsetAndRotation(-4.5F, -9.0F, 0.0F, 0.0F, 0.0F, 2.3562F)
      );
      PartDefinition rightForearmPart = rightArmPart.addOrReplaceChild(
         "right_forearm",
         CubeListBuilder.create()
            .texOffs(104, 25)
            .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F)
            .texOffs(68, 44)
            .addBox(-2.0F, 4.0F, -2.0F, 4.0F, 3.0F, 4.0F)
            .texOffs(0, 45)
            .addBox(-1.0F, 7.0F, -1.0F, 2.0F, 2.0F, 2.0F),
         PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.0F, 0.0F, 0.2618F)
      );
      PartDefinition rightChainPart = rightForearmPart.addOrReplaceChild(
         "right_chain",
         CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 26.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.0F, 0.0F, 0.1745F)
      );
      PartDefinition rightThighPart = hipsPart.addOrReplaceChild(
         "right_thigh",
         CubeListBuilder.create().texOffs(116, 25).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
         PartPose.offsetAndRotation(-2.0F, 1.0F, 0.0F, -1.3963F, 0.0F, 0.0F)
      );
      PartDefinition rightShinPart = rightThighPart.addOrReplaceChild(
         "right_shin",
         CubeListBuilder.create().texOffs(40, 26).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 1.7453F, 0.0F, 0.0F)
      );
      PartDefinition leftArmPart = bodyPart.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create().texOffs(50, 15).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 8.0F, 3.0F),
         PartPose.offsetAndRotation(4.5F, -9.0F, 0.0F, 0.0F, 0.0F, -2.3562F)
      );
      PartDefinition leftForearmPart = leftArmPart.addOrReplaceChild(
         "left_forearm",
         CubeListBuilder.create()
            .texOffs(52, 26)
            .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F)
            .texOffs(84, 44)
            .addBox(-2.0F, 4.0F, -2.0F, 4.0F, 3.0F, 4.0F)
            .texOffs(8, 45)
            .addBox(-1.0F, 7.0F, -1.0F, 2.0F, 2.0F, 2.0F),
         PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.0F, 0.0F, -0.2618F)
      );
      PartDefinition leftChainPart = leftForearmPart.addOrReplaceChild(
         "left_chain",
         CubeListBuilder.create().texOffs(4, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 26.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.0F, 0.0F, -0.1745F)
      );
      PartDefinition leftThighPart = hipsPart.addOrReplaceChild(
         "left_thigh",
         CubeListBuilder.create().texOffs(64, 26).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
         PartPose.offsetAndRotation(2.0F, 1.0F, 0.0F, -1.3963F, 0.0F, 0.0F)
      );
      PartDefinition leftShinPart = leftThighPart.addOrReplaceChild(
         "left_shin",
         CubeListBuilder.create().texOffs(76, 27).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 1.7453F, 0.0F, 0.0F)
      );
      PartDefinition crownPart = headPart.addOrReplaceChild(
         "crown",
         CubeListBuilder.create()
            .texOffs(92, 14)
            .addBox(-4.0F, -2.5F, -4.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.05F))
            .texOffs(16, 45)
            .addBox(-4.0F, -4.5F, -4.0F, 1.0F, 2.0F, 1.0F)
            .texOffs(20, 45)
            .addBox(3.0F, -4.5F, -4.0F, 1.0F, 2.0F, 1.0F)
            .texOffs(24, 45)
            .addBox(-4.0F, -4.5F, 3.0F, 1.0F, 2.0F, 1.0F)
            .texOffs(28, 45)
            .addBox(3.0F, -4.5F, 3.0F, 1.0F, 2.0F, 1.0F)
            .texOffs(100, 44)
            .addBox(-0.5F, -5.5F, -4.0F, 1.0F, 3.0F, 1.0F)
            .texOffs(32, 45)
            .addBox(-0.5F, -4.5F, 3.0F, 1.0F, 2.0F, 1.0F)
            .texOffs(36, 45)
            .addBox(-4.0F, -4.5F, -0.5F, 1.0F, 2.0F, 1.0F)
            .texOffs(40, 45)
            .addBox(3.0F, -4.5F, -0.5F, 1.0F, 2.0F, 1.0F),
         PartPose.offset(0.0F, -8.0F, 0.0F)
      );
      PartDefinition mantlePart = bodyPart.addOrReplaceChild(
         "mantle",
         CubeListBuilder.create().texOffs(8, 26).addBox(-5.0F, -10.5F, -3.0F, 10.0F, 4.0F, 6.0F, new CubeDeformation(0.25F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition robePart = bodyPart.addOrReplaceChild(
         "robe",
         CubeListBuilder.create().texOffs(36, 0).addBox(-4.0F, -10.0F, -2.5F, 8.0F, 10.0F, 5.0F, new CubeDeformation(0.35F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition robeHipsPart = hipsPart.addOrReplaceChild(
         "robe_hips",
         CubeListBuilder.create().texOffs(8, 15).addBox(-4.5F, -2.5F, -2.8F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.3F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition rightSleevePart = rightArmPart.addOrReplaceChild(
         "right_sleeve",
         CubeListBuilder.create().texOffs(62, 15).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.35F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition rightCuffPart = rightForearmPart.addOrReplaceChild(
         "right_cuff",
         CubeListBuilder.create().texOffs(88, 36).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.25F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition rightThighRobePart = rightThighPart.addOrReplaceChild(
         "right_thigh_robe",
         CubeListBuilder.create().texOffs(104, 35).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.4F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition rightShinRobePart = rightShinPart.addOrReplaceChild(
         "right_shin_robe",
         CubeListBuilder.create().texOffs(28, 36).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.4F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition leftSleevePart = leftArmPart.addOrReplaceChild(
         "left_sleeve",
         CubeListBuilder.create().texOffs(92, 25).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.35F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition leftCuffPart = leftForearmPart.addOrReplaceChild(
         "left_cuff",
         CubeListBuilder.create().texOffs(52, 44).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.25F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition leftThighRobePart = leftThighPart.addOrReplaceChild(
         "left_thigh_robe",
         CubeListBuilder.create().texOffs(116, 35).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.4F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition leftShinRobePart = leftShinPart.addOrReplaceChild(
         "left_shin_robe",
         CubeListBuilder.create().texOffs(40, 36).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.4F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 128, 128);
   }

   public static LayerDefinition collector() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition rightLegPart = rootPart.addOrReplaceChild(
         "right_leg",
         CubeListBuilder.create()
            .texOffs(114, 0)
            .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F)
            .texOffs(84, 23)
            .addBox(-1.5F, 8.0F, -2.5F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.15F)),
         PartPose.offset(-1.8F, -11.0F, 0.0F)
      );
      PartDefinition leftLegPart = rootPart.addOrReplaceChild(
         "left_leg",
         CubeListBuilder.create()
            .texOffs(114, 12)
            .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F)
            .texOffs(98, 23)
            .addBox(-1.5F, 8.0F, -2.5F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.15F)),
         PartPose.offset(1.8F, -11.0F, 0.0F)
      );
      PartDefinition bodyPart = rootPart.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(8, 0).addBox(-3.5F, -11.0F, -2.0F, 7.0F, 11.0F, 4.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 0.4189F, 0.0F, 0.0F)
      );
      PartDefinition coatTailPart = bodyPart.addOrReplaceChild(
         "coat_tail",
         CubeListBuilder.create().texOffs(88, 0).addBox(-4.0F, -0.5F, -2.5F, 8.0F, 8.0F, 5.0F, new CubeDeformation(0.15F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3142F, 0.0F, 0.0F)
      );
      PartDefinition headPart = bodyPart.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(30, 0)
            .addBox(-3.5F, -7.0F, -4.0F, 7.0F, 7.0F, 7.0F)
            .texOffs(78, 25)
            .addBox(-0.5F, -4.0F, -5.5F, 1.0F, 2.0F, 2.0F)
            .texOffs(8, 15)
            .addBox(-4.0F, -7.5F, -4.5F, 8.0F, 2.0F, 8.0F),
         PartPose.offsetAndRotation(0.0F, -11.0F, -1.0F, -0.5236F, 0.0F, 0.0F)
      );
      PartDefinition lensPart = headPart.addOrReplaceChild(
         "lens", CubeListBuilder.create().texOffs(52, 29).addBox(-1.5F, -1.5F, -0.6F, 3.0F, 3.0F, 1.0F), PartPose.offset(-1.5F, -4.5F, -4.4F)
      );
      PartDefinition rightArmPart = bodyPart.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create().texOffs(84, 13).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.12F)),
         PartPose.offsetAndRotation(-4.8F, -9.5F, 0.0F, -0.5236F, 0.0F, 0.0F)
      );
      PartDefinition rightForearmPart = rightArmPart.addOrReplaceChild(
         "right_forearm",
         CubeListBuilder.create()
            .texOffs(40, 14)
            .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.08F))
            .texOffs(0, 25)
            .addBox(-1.5F, 5.0F, -1.5F, 3.0F, 2.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, -0.6109F, 0.0F, 0.0F)
      );
      PartDefinition leftArmPart = bodyPart.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create().texOffs(96, 13).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.12F)),
         PartPose.offsetAndRotation(4.8F, -9.5F, 0.0F, -0.5236F, 0.0F, 0.0F)
      );
      PartDefinition leftForearmPart = leftArmPart.addOrReplaceChild(
         "left_forearm",
         CubeListBuilder.create()
            .texOffs(40, 22)
            .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.08F))
            .texOffs(12, 25)
            .addBox(-1.5F, 5.0F, -1.5F, 3.0F, 2.0F, 3.0F),
         PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, -0.6109F, 0.0F, 0.0F)
      );
      PartDefinition trinketPart = rightForearmPart.addOrReplaceChild(
         "trinket", CubeListBuilder.create().texOffs(60, 29).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 7.0F, -0.5F)
      );
      PartDefinition packPart = bodyPart.addOrReplaceChild(
         "pack",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-3.5F, -14.0F, 0.0F, 1.0F, 23.0F, 1.0F)
            .texOffs(4, 0)
            .addBox(2.5F, -14.0F, 0.0F, 1.0F, 23.0F, 1.0F)
            .texOffs(58, 0)
            .addBox(-4.5F, 1.0F, -0.5F, 9.0F, 7.0F, 6.0F)
            .texOffs(58, 13)
            .addBox(-4.0F, -6.0F, 0.0F, 8.0F, 7.0F, 5.0F, new CubeDeformation(0.2F))
            .texOffs(52, 25)
            .addBox(-5.5F, -9.5F, 1.0F, 11.0F, 2.0F, 2.0F)
            .texOffs(24, 25)
            .addBox(-6.5F, -3.0F, 1.5F, 2.0F, 3.0F, 2.0F)
            .texOffs(32, 25)
            .addBox(4.5F, -1.0F, 1.5F, 2.0F, 3.0F, 2.0F)
            .texOffs(68, 29)
            .addBox(-3.0F, -14.5F, -0.5F, 6.0F, 1.0F, 2.0F),
         PartPose.offset(0.0F, -9.0F, 2.5F)
      );
      PartDefinition packLanternPart = packPart.addOrReplaceChild(
         "pack_lantern",
         CubeListBuilder.create().texOffs(124, 24).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F).texOffs(112, 24).addBox(-1.5F, 2.0F, -1.5F, 3.0F, 4.0F, 3.0F),
         PartPose.offset(2.5F, -13.5F, 0.5F)
      );
      return LayerDefinition.create(mesh, 128, 128);
   }

   public static LayerDefinition bonesmith() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition rightLegPart = rootPart.addOrReplaceChild(
         "right_leg",
         CubeListBuilder.create().texOffs(108, 31).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F).texOffs(20, 26).addBox(-2.0F, 4.0F, -2.8F, 4.0F, 3.0F, 5.0F),
         PartPose.offset(-2.6F, -7.0F, 0.0F)
      );
      PartDefinition leftLegPart = rootPart.addOrReplaceChild(
         "left_leg",
         CubeListBuilder.create().texOffs(38, 32).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F).texOffs(58, 31).addBox(-2.0F, 4.0F, -2.8F, 4.0F, 3.0F, 5.0F),
         PartPose.offset(2.6F, -7.0F, 0.0F)
      );
      PartDefinition hipsPart = rootPart.addOrReplaceChild(
         "hips", CubeListBuilder.create().texOffs(70, 22).addBox(-4.0F, -3.0F, -3.0F, 8.0F, 3.0F, 6.0F), PartPose.offset(0.0F, -7.0F, 0.0F)
      );
      PartDefinition bodyPart = hipsPart.addOrReplaceChild(
         "body",
         CubeListBuilder.create()
            .texOffs(38, 0)
            .addBox(-5.0F, -8.0F, -3.0F, 10.0F, 8.0F, 6.0F)
            .texOffs(70, 12)
            .addBox(-6.0F, -9.0F, -3.5F, 12.0F, 3.0F, 7.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.0698F, 0.0F, 0.0F)
      );
      PartDefinition headPart = bodyPart.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-5.0F, -8.0F, -4.5F, 10.0F, 8.0F, 9.0F)
            .texOffs(70, 0)
            .addBox(-5.5F, -9.0F, -5.0F, 11.0F, 2.0F, 10.0F, new CubeDeformation(0.05F)),
         PartPose.offset(0.0F, -8.5F, -0.5F)
      );
      PartDefinition jawPart = headPart.addOrReplaceChild(
         "jaw", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0F, 0.0F, -3.8F, 8.0F, 2.0F, 7.0F), PartPose.offset(0.0F, -1.0F, -1.0F)
      );
      PartDefinition rightArmPart = bodyPart.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create().texOffs(30, 17).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 7.0F, 2.0F).texOffs(76, 31).addBox(-2.0F, -2.5F, -2.0F, 4.0F, 3.0F, 4.0F),
         PartPose.offsetAndRotation(-6.5F, -7.0F, 0.0F, -0.4363F, 0.0F, 0.1047F)
      );
      PartDefinition rightForearmPart = rightArmPart.addOrReplaceChild(
         "right_forearm",
         CubeListBuilder.create().texOffs(38, 14).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
         PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.5236F, 0.0F, 0.0F)
      );
      PartDefinition rightHandPart = rightForearmPart.addOrReplaceChild(
         "right_hand", CubeListBuilder.create().texOffs(38, 24).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 3.0F, 5.0F), PartPose.offset(0.0F, 6.0F, 0.0F)
      );
      PartDefinition leftArmPart = bodyPart.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create().texOffs(114, 22).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 7.0F, 2.0F).texOffs(92, 31).addBox(-2.0F, -2.5F, -2.0F, 4.0F, 3.0F, 4.0F),
         PartPose.offsetAndRotation(6.5F, -7.0F, 0.0F, -0.1396F, 0.0F, -0.1396F)
      );
      PartDefinition leftForearmPart = leftArmPart.addOrReplaceChild(
         "left_forearm",
         CubeListBuilder.create().texOffs(54, 14).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
         PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.1745F, 0.0F, 0.0F)
      );
      PartDefinition leftHandPart = leftForearmPart.addOrReplaceChild(
         "left_hand", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 3.0F, 5.0F), PartPose.offset(0.0F, 6.0F, 0.0F)
      );
      PartDefinition tongsPart = rightHandPart.addOrReplaceChild(
         "tongs",
         CubeListBuilder.create().texOffs(108, 12).addBox(-0.5F, -0.5F, -9.0F, 1.0F, 1.0F, 9.0F).texOffs(98, 22).addBox(-0.5F, -0.8F, -16.0F, 1.0F, 2.0F, 7.0F),
         PartPose.offsetAndRotation(0.0F, 2.5F, -1.0F, 1.4835F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 128, 128);
   }

   public static LayerDefinition mira() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition skirtPart = rootPart.addOrReplaceChild(
         "skirt", CubeListBuilder.create().texOffs(28, 13).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 6.0F, 6.0F), PartPose.offset(0.0F, -6.0F, 0.0F)
      );
      PartDefinition bodyPart = skirtPart.addOrReplaceChild(
         "body", CubeListBuilder.create().texOffs(0, 14).addBox(-3.0F, -6.0F, -2.0F, 6.0F, 6.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition headPart = bodyPart.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-3.5F, -7.0F, -3.5F, 7.0F, 7.0F, 7.0F)
            .texOffs(28, 0)
            .addBox(-4.0F, -7.5F, -4.0F, 8.0F, 5.0F, 8.0F, new CubeDeformation(0.1F))
            .texOffs(0, 24)
            .addBox(-2.5F, -9.0F, -1.0F, 5.0F, 2.0F, 1.0F),
         PartPose.offset(0.0F, -6.0F, 0.0F)
      );
      PartDefinition braidPart = headPart.addOrReplaceChild(
         "braid", CubeListBuilder.create().texOffs(56, 21).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 7.0F, 1.0F), PartPose.offset(0.0F, -3.0F, 3.8F)
      );
      PartDefinition rightArmPart = bodyPart.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create().texOffs(56, 13).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F),
         PartPose.offsetAndRotation(-3.8F, -5.5F, 0.0F, -0.1745F, 0.0F, 0.0F)
      );
      PartDefinition leftArmPart = bodyPart.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create().texOffs(20, 14).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F),
         PartPose.offsetAndRotation(3.8F, -5.5F, 0.0F, -0.1745F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition lamplighter() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition bodyPart = rootPart.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(30, 0).addBox(-3.5F, -12.0F, -2.0F, 7.0F, 12.0F, 4.0F),
         PartPose.offsetAndRotation(0.0F, -14.0F, 0.0F, 0.1047F, 0.0F, 0.0F)
      );
      PartDefinition coatPart = bodyPart.addOrReplaceChild(
         "coat",
         CubeListBuilder.create().texOffs(4, 0).addBox(-4.0F, 0.0F, -2.5F, 8.0F, 12.0F, 5.0F, new CubeDeformation(0.1F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition headPart = bodyPart.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(30, 16)
            .addBox(-3.5F, -7.0F, -3.5F, 7.0F, 7.0F, 7.0F)
            .texOffs(0, 43)
            .addBox(-5.0F, -7.6F, -5.0F, 10.0F, 1.0F, 10.0F)
            .texOffs(28, 30)
            .addBox(-3.5F, -13.0F, -3.5F, 7.0F, 6.0F, 7.0F),
         PartPose.offsetAndRotation(0.0F, -12.0F, -0.5F, 0.2094F, 0.0F, 0.0F)
      );
      PartDefinition rightArmPart = bodyPart.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create().texOffs(4, 17).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 11.0F, 3.0F).texOffs(40, 50).addBox(-1.0F, 9.5F, -1.0F, 2.0F, 2.0F, 2.0F),
         PartPose.offsetAndRotation(-4.5F, -11.0F, 0.0F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition leftArmPart = bodyPart.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create().texOffs(16, 17).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 11.0F, 3.0F).texOffs(0, 54).addBox(-1.0F, 9.5F, -1.0F, 2.0F, 2.0F, 2.0F),
         PartPose.offsetAndRotation(4.5F, -11.0F, 0.0F, -0.7854F, 0.0F, 0.0F)
      );
      PartDefinition polePart = leftArmPart.addOrReplaceChild(
         "pole",
         CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -18.0F, -0.5F, 1.0F, 26.0F, 1.0F).texOffs(52, 43).addBox(-0.5F, -18.0F, -0.5F, 1.0F, 1.0F, 5.0F),
         PartPose.offsetAndRotation(0.0F, 10.5F, 0.0F, 0.6981F, 0.0F, 0.0F)
      );
      PartDefinition lampPart = polePart.addOrReplaceChild(
         "lamp",
         CubeListBuilder.create().texOffs(40, 43).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F).texOffs(52, 49).addBox(-1.0F, 0.5F, -1.0F, 2.0F, 3.0F, 2.0F),
         PartPose.offset(0.0F, -17.5F, 4.5F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition sentry() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition bodyPart = rootPart.addOrReplaceChild(
         "body",
         CubeListBuilder.create()
            .texOffs(4, 0)
            .addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F)
            .texOffs(28, 28)
            .addBox(-4.5F, -12.5F, -2.5F, 9.0F, 7.0F, 5.0F, new CubeDeformation(0.1F)),
         PartPose.offset(0.0F, -14.0F, 0.0F)
      );
      PartDefinition tailPart = bodyPart.addOrReplaceChild(
         "tail", CubeListBuilder.create().texOffs(4, 30).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 8.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition headPart = bodyPart.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(28, 14)
            .addBox(-3.5F, -7.0F, -3.5F, 7.0F, 7.0F, 7.0F)
            .texOffs(28, 0)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.15F)),
         PartPose.offset(0.0F, -12.5F, 0.0F)
      );
      PartDefinition rightArmPart = bodyPart.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create().texOffs(4, 16).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 11.0F, 3.0F).texOffs(24, 40).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 3.0F, 4.0F),
         PartPose.offsetAndRotation(-5.0F, -11.5F, 0.0F, -0.5236F, 0.0F, 0.0F)
      );
      PartDefinition leftArmPart = bodyPart.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create().texOffs(16, 16).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 11.0F, 3.0F).texOffs(40, 40).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 3.0F, 4.0F),
         PartPose.offset(5.0F, -11.5F, 0.0F)
      );
      PartDefinition spearPart = rightArmPart.addOrReplaceChild(
         "spear",
         CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -22.0F, -0.5F, 1.0F, 30.0F, 1.0F).texOffs(56, 14).addBox(-1.0F, -26.0F, -0.5F, 2.0F, 4.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 9.5F, 0.0F, 1.0472F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition skeletonKid() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition bodyPart = rootPart.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(28, 10).addBox(-2.5F, -5.0F, -1.5F, 5.0F, 5.0F, 3.0F).texOffs(0, 14).addBox(-3.0F, -5.5F, -2.0F, 6.0F, 2.0F, 4.0F),
         PartPose.offset(0.0F, -6.0F, 0.0F)
      );
      PartDefinition scarfTailPart = bodyPart.addOrReplaceChild(
         "scarf_tail", CubeListBuilder.create().texOffs(52, 10).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 1.0F), PartPose.offset(1.5F, -4.5F, 2.0F)
      );
      PartDefinition headPart = bodyPart.addOrReplaceChild(
         "head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -7.0F, -3.5F, 7.0F, 7.0F, 7.0F), PartPose.offset(0.0F, -5.5F, 0.0F)
      );
      PartDefinition capPart = headPart.addOrReplaceChild(
         "cap",
         CubeListBuilder.create()
            .texOffs(28, 0)
            .addBox(-4.0F, -7.8F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.1F))
            .texOffs(44, 16)
            .addBox(-3.5F, -6.6F, -5.5F, 7.0F, 1.0F, 2.0F),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition bowPart = headPart.addOrReplaceChild(
         "bow", CubeListBuilder.create().texOffs(20, 18).addBox(-2.0F, -9.0F, -0.5F, 4.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition rightArmPart = bodyPart.addOrReplaceChild(
         "right_arm", CubeListBuilder.create().texOffs(44, 10).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F), PartPose.offset(-3.0F, -4.5F, 0.0F)
      );
      PartDefinition rightLegPart = rootPart.addOrReplaceChild(
         "right_leg", CubeListBuilder.create().texOffs(60, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F), PartPose.offset(-1.3F, -6.0F, 0.0F)
      );
      PartDefinition leftArmPart = bodyPart.addOrReplaceChild(
         "left_arm", CubeListBuilder.create().texOffs(48, 10).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F), PartPose.offset(3.0F, -4.5F, 0.0F)
      );
      PartDefinition leftLegPart = rootPart.addOrReplaceChild(
         "left_leg", CubeListBuilder.create().texOffs(60, 7).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F), PartPose.offset(1.3F, -6.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition lanternWisp() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition lanternPart = rootPart.addOrReplaceChild(
         "lantern",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F)
            .texOffs(24, 9)
            .addBox(-3.5F, -7.0F, -3.5F, 7.0F, 1.0F, 7.0F)
            .texOffs(0, 17)
            .addBox(-3.5F, 0.0F, -3.5F, 7.0F, 1.0F, 7.0F)
            .texOffs(52, 0)
            .addBox(-1.0F, -9.0F, -0.5F, 2.0F, 2.0F, 1.0F),
         PartPose.offset(0.0F, -10.0F, 0.0F)
      );
      PartDefinition flamePart = lanternPart.addOrReplaceChild(
         "flame", CubeListBuilder.create().texOffs(24, 0).addBox(-2.0F, -2.5F, -2.0F, 4.0F, 5.0F, 4.0F), PartPose.offset(0.0F, -3.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition hollowHunter() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition rootPart = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition hipsPart = rootPart.addOrReplaceChild(
         "hips", CubeListBuilder.create().texOffs(0, 53).addBox(-3.0F, -2.0F, -2.0F, 6.0F, 3.0F, 4.0F), PartPose.offset(0.0F, -20.0F, 0.0F)
      );
      PartDefinition rightThighPart = hipsPart.addOrReplaceChild(
         "right_thigh",
         CubeListBuilder.create()
            .texOffs(64, 19)
            .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F)
            .texOffs(24, 0)
            .addBox(-3.0F, -1.0F, -3.0F, 6.0F, 13.0F, 6.0F, new CubeDeformation(0.15F)),
         PartPose.offset(-2.0F, 0.0F, 0.0F)
      );
      PartDefinition rightShinPart = rightThighPart.addOrReplaceChild(
         "right_shin",
         CubeListBuilder.create()
            .texOffs(60, 31)
            .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F)
            .texOffs(80, 32)
            .addBox(-1.5F, 1.0F, -1.5F, 3.0F, 8.0F, 3.0F)
            .texOffs(38, 59)
            .addBox(-1.5F, 9.0F, -3.0F, 3.0F, 1.0F, 4.0F),
         PartPose.offset(0.0F, 10.0F, 0.0F)
      );
      PartDefinition leftThighPart = hipsPart.addOrReplaceChild(
         "left_thigh",
         CubeListBuilder.create()
            .texOffs(0, 32)
            .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F)
            .texOffs(48, 0)
            .addBox(-3.0F, -1.0F, -3.0F, 6.0F, 13.0F, 6.0F, new CubeDeformation(0.15F)),
         PartPose.offset(2.0F, 0.0F, 0.0F)
      );
      PartDefinition leftShinPart = leftThighPart.addOrReplaceChild(
         "left_shin",
         CubeListBuilder.create()
            .texOffs(8, 32)
            .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F)
            .texOffs(102, 40)
            .addBox(-1.5F, 1.0F, -1.5F, 3.0F, 8.0F, 3.0F)
            .texOffs(98, 59)
            .addBox(-1.5F, 9.0F, -3.0F, 3.0F, 1.0F, 4.0F),
         PartPose.offset(0.0F, 10.0F, 0.0F)
      );
      PartDefinition spinePart = hipsPart.addOrReplaceChild(
         "spine",
         CubeListBuilder.create().texOffs(78, 52).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F).texOffs(102, 28).addBox(-3.5F, -6.0F, -2.5F, 7.0F, 7.0F, 5.0F),
         PartPose.offset(0.0F, -2.0F, 0.0F)
      );
      PartDefinition chestPart = spinePart.addOrReplaceChild(
         "chest",
         CubeListBuilder.create()
            .texOffs(72, 18)
            .addBox(-4.0F, -9.0F, -2.5F, 8.0F, 9.0F, 5.0F)
            .texOffs(28, 19)
            .addBox(-5.5F, -10.5F, -3.5F, 11.0F, 5.0F, 7.0F, new CubeDeformation(0.2F)),
         PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.2443F, 0.0F, 0.0F)
      );
      PartDefinition capePart = chestPart.addOrReplaceChild(
         "cape",
         CubeListBuilder.create().texOffs(0, 0).addBox(-5.5F, 0.0F, 0.0F, 11.0F, 32.0F, 0.0F),
         PartPose.offsetAndRotation(0.0F, -9.5F, 3.5F, 0.1047F, 0.0F, 0.0F)
      );
      PartDefinition headPart = chestPart.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(104, 15)
            .addBox(-3.0F, -7.5F, -3.0F, 6.0F, 7.0F, 6.0F)
            .texOffs(72, 0)
            .addBox(-4.0F, -9.0F, -5.0F, 8.0F, 10.0F, 8.0F)
            .texOffs(102, 51)
            .addBox(-3.0F, -10.5F, -4.0F, 6.0F, 2.0F, 6.0F),
         PartPose.offsetAndRotation(0.0F, -9.5F, -0.5F, -0.2094F, 0.0F, 0.0F)
      );
      PartDefinition jawPart = headPart.addOrReplaceChild(
         "jaw", CubeListBuilder.create().texOffs(112, 59).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 1.0F, 3.0F), PartPose.offset(0.0F, -1.0F, -1.0F)
      );
      PartDefinition hoodTailPart = headPart.addOrReplaceChild(
         "hood_tail",
         CubeListBuilder.create().texOffs(60, 52).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 6.0F),
         PartPose.offsetAndRotation(0.0F, -9.5F, 2.5F, -0.8727F, 0.0F, 0.0F)
      );
      PartDefinition rightAntlerPart = headPart.addOrReplaceChild(
         "right_antler",
         CubeListBuilder.create()
            .texOffs(98, 43)
            .addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F)
            .texOffs(82, 60)
            .addBox(-2.0F, -5.0F, -0.5F, 1.0F, 1.0F, 1.0F)
            .texOffs(56, 59)
            .addBox(-3.0F, -7.0F, -0.5F, 1.0F, 3.0F, 1.0F)
            .texOffs(4, 60)
            .addBox(-0.5F, -6.0F, -2.0F, 1.0F, 1.0F, 2.0F)
            .texOffs(10, 60)
            .addBox(-0.5F, -3.0F, 0.5F, 1.0F, 1.0F, 2.0F),
         PartPose.offsetAndRotation(-2.2F, -10.0F, 0.0F, -0.1745F, 0.0F, -0.4538F)
      );
      PartDefinition leftAntlerPart = headPart.addOrReplaceChild(
         "left_antler",
         CubeListBuilder.create()
            .texOffs(24, 44)
            .addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F)
            .texOffs(82, 62)
            .addBox(1.0F, -5.0F, -0.5F, 1.0F, 1.0F, 1.0F)
            .texOffs(0, 60)
            .addBox(2.0F, -7.0F, -0.5F, 1.0F, 3.0F, 1.0F)
            .texOffs(26, 60)
            .addBox(-0.5F, -6.0F, -2.0F, 1.0F, 1.0F, 2.0F)
            .texOffs(60, 60)
            .addBox(-0.5F, -3.0F, 0.5F, 1.0F, 1.0F, 2.0F),
         PartPose.offsetAndRotation(2.2F, -10.0F, 0.0F, -0.1745F, 0.0F, 0.4538F)
      );
      PartDefinition rightArmPart = chestPart.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create().texOffs(16, 32).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 10.0F, 2.0F).texOffs(28, 31).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 8.0F, 4.0F),
         PartPose.offset(-5.0F, -8.5F, 0.0F)
      );
      PartDefinition rightForearmPart = rightArmPart.addOrReplaceChild(
         "right_forearm",
         CubeListBuilder.create()
            .texOffs(114, 40)
            .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F)
            .texOffs(36, 43)
            .addBox(-1.5F, 0.5F, -1.5F, 3.0F, 7.0F, 3.0F)
            .texOffs(86, 52)
            .addBox(-1.0F, 9.0F, -1.0F, 2.0F, 4.0F, 2.0F),
         PartPose.offset(0.0F, 9.0F, 0.0F)
      );
      PartDefinition leftArmPart = chestPart.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create().texOffs(68, 32).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 10.0F, 2.0F).texOffs(44, 31).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 8.0F, 4.0F),
         PartPose.offset(5.0F, -8.5F, 0.0F)
      );
      PartDefinition leftForearmPart = leftArmPart.addOrReplaceChild(
         "left_forearm",
         CubeListBuilder.create()
            .texOffs(28, 43)
            .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F)
            .texOffs(48, 43)
            .addBox(-1.5F, 0.5F, -1.5F, 3.0F, 7.0F, 3.0F)
            .texOffs(94, 52)
            .addBox(-1.0F, 9.0F, -1.0F, 2.0F, 4.0F, 2.0F),
         PartPose.offset(0.0F, 9.0F, 0.0F)
      );
      PartDefinition bowPart = leftForearmPart.addOrReplaceChild(
         "bow",
         CubeListBuilder.create().texOffs(52, 59).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 4.0F, 1.0F).texOffs(22, 0).addBox(-0.5F, -12.0F, 2.2F, 1.0F, 24.0F, 0.0F),
         PartPose.offset(0.0F, 10.5F, 0.0F)
      );
      PartDefinition bowUpperPart = bowPart.addOrReplaceChild(
         "bow_upper",
         CubeListBuilder.create()
            .texOffs(98, 31)
            .addBox(-0.5F, -11.0F, -0.5F, 1.0F, 11.0F, 1.0F)
            .texOffs(16, 60)
            .addBox(-0.5F, -13.0F, -1.5F, 1.0F, 2.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.2094F, 0.0F, 0.0F)
      );
      PartDefinition bowLowerPart = bowPart.addOrReplaceChild(
         "bow_lower",
         CubeListBuilder.create().texOffs(24, 32).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 11.0F, 1.0F).texOffs(78, 60).addBox(-0.5F, 11.0F, -1.5F, 1.0F, 2.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.2094F, 0.0F, 0.0F)
      );
      PartDefinition stuck0Part = chestPart.addOrReplaceChild(
         "stuck_0",
         CubeListBuilder.create()
            .texOffs(98, 18)
            .addBox(-0.5F, -10.0F, -0.5F, 1.0F, 12.0F, 1.0F, new CubeDeformation(-0.3F))
            .texOffs(20, 53)
            .addBox(-1.5F, -9.7F, 0.0F, 3.0F, 6.0F, 0.0F)
            .texOffs(60, 43)
            .addBox(0.0F, -9.7F, -1.5F, 0.0F, 6.0F, 3.0F)
            .texOffs(4, 63)
            .addBox(-0.5F, -10.4F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(1.5F, -6.0F, 2.8F, -1.0123F, 0.0F, 0.2793F)
      );
      PartDefinition stuck1Part = chestPart.addOrReplaceChild(
         "stuck_1",
         CubeListBuilder.create()
            .texOffs(76, 32)
            .addBox(-0.5F, -9.0F, -0.5F, 1.0F, 11.0F, 1.0F, new CubeDeformation(-0.3F))
            .texOffs(36, 53)
            .addBox(-1.5F, -8.7F, 0.0F, 3.0F, 6.0F, 0.0F)
            .texOffs(80, 43)
            .addBox(0.0F, -8.7F, -1.5F, 0.0F, 6.0F, 3.0F)
            .texOffs(8, 63)
            .addBox(-0.5F, -9.4F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(2.8F, -3.0F, 2.8F, -1.2915F, 0.2443F, 0.2094F)
      );
      PartDefinition stuck2Part = chestPart.addOrReplaceChild(
         "stuck_2",
         CubeListBuilder.create()
            .texOffs(108, 0)
            .addBox(-0.5F, -11.0F, -0.5F, 1.0F, 13.0F, 1.0F, new CubeDeformation(-0.3F))
            .texOffs(42, 53)
            .addBox(-1.5F, -10.7F, 0.0F, 3.0F, 6.0F, 0.0F)
            .texOffs(86, 43)
            .addBox(0.0F, -10.7F, -1.5F, 0.0F, 6.0F, 3.0F)
            .texOffs(12, 63)
            .addBox(-0.5F, -11.4F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(-0.5F, -8.0F, 2.8F, -0.8029F, -0.1047F, -0.1396F)
      );
      PartDefinition stuck3Part = chestPart.addOrReplaceChild(
         "stuck_3",
         CubeListBuilder.create()
            .texOffs(92, 32)
            .addBox(-0.5F, -8.0F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(-0.3F))
            .texOffs(48, 53)
            .addBox(-1.5F, -7.7F, 0.0F, 3.0F, 6.0F, 0.0F)
            .texOffs(92, 43)
            .addBox(0.0F, -7.7F, -1.5F, 0.0F, 6.0F, 3.0F)
            .texOffs(16, 63)
            .addBox(-0.5F, -8.4F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(0.6F, -1.5F, 2.8F, -1.3963F, 0.0F, 0.0698F)
      );
      PartDefinition pullArrowPart = chestPart.addOrReplaceChild(
         "pull_arrow",
         CubeListBuilder.create()
            .texOffs(112, 0)
            .addBox(-0.5F, -11.0F, -0.5F, 1.0F, 13.0F, 1.0F, new CubeDeformation(-0.3F))
            .texOffs(54, 53)
            .addBox(-1.5F, -10.7F, 0.0F, 3.0F, 6.0F, 0.0F)
            .texOffs(0, 44)
            .addBox(0.0F, -10.7F, -1.5F, 0.0F, 6.0F, 3.0F)
            .texOffs(26, 63)
            .addBox(-0.5F, -11.4F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(-3.0F, -8.0F, 2.8F, -0.6981F, 0.0F, -0.384F)
      );
      PartDefinition stuck4Part = leftArmPart.addOrReplaceChild(
         "stuck_4",
         CubeListBuilder.create()
            .texOffs(122, 40)
            .addBox(-0.5F, -8.0F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(-0.3F))
            .texOffs(26, 54)
            .addBox(-1.5F, -7.7F, 0.0F, 3.0F, 6.0F, 0.0F)
            .texOffs(6, 44)
            .addBox(0.0F, -7.7F, -1.5F, 0.0F, 6.0F, 3.0F)
            .texOffs(56, 63)
            .addBox(-0.5F, -8.4F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(0.5F, 1.5F, 1.6F, -1.1868F, 0.0F, 0.4887F)
      );
      PartDefinition nockedArrowPart = bowPart.addOrReplaceChild(
         "nocked_arrow",
         CubeListBuilder.create()
            .texOffs(104, 0)
            .addBox(-0.5F, -5.0F, -0.5F, 1.0F, 14.0F, 1.0F, new CubeDeformation(-0.3F))
            .texOffs(86, 58)
            .addBox(-1.5F, -4.7F, 0.0F, 3.0F, 6.0F, 0.0F)
            .texOffs(12, 44)
            .addBox(0.0F, -4.7F, -1.5F, 0.0F, 6.0F, 3.0F)
            .texOffs(60, 63)
            .addBox(-0.5F, -5.4F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
            .texOffs(66, 60)
            .addBox(-1.5F, 8.5F, 0.0F, 3.0F, 3.0F, 0.0F)
            .texOffs(92, 58)
            .addBox(0.0F, 8.5F, -1.5F, 0.0F, 3.0F, 3.0F),
         PartPose.offsetAndRotation(0.9F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F)
      );
      PartDefinition handArrowPart = rightForearmPart.addOrReplaceChild(
         "hand_arrow",
         CubeListBuilder.create()
            .texOffs(24, 19)
            .addBox(-0.5F, -5.0F, -0.5F, 1.0F, 12.0F, 1.0F, new CubeDeformation(-0.3F))
            .texOffs(20, 59)
            .addBox(-1.5F, -4.7F, 0.0F, 3.0F, 6.0F, 0.0F)
            .texOffs(18, 44)
            .addBox(0.0F, -4.7F, -1.5F, 0.0F, 6.0F, 3.0F)
            .texOffs(64, 63)
            .addBox(-0.5F, -5.4F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F))
            .texOffs(72, 60)
            .addBox(-1.5F, 6.5F, 0.0F, 3.0F, 3.0F, 0.0F)
            .texOffs(32, 59)
            .addBox(0.0F, 6.5F, -1.5F, 0.0F, 3.0F, 3.0F),
         PartPose.offset(0.0F, 11.0F, -1.3F)
      );
      return LayerDefinition.create(mesh, 128, 128);
   }
}
