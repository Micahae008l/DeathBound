package com.underworld.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.underworld.client.render.state.CollectorRenderState;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/** Hunched, shuffling, hands folded; lifts an artifact up to his hood to inspect it. */
public class CollectorModel extends EntityModel<CollectorRenderState> implements ArmedModel<CollectorRenderState> {
	private static final float HUNCH = 0.38F;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart skirt;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;
	private final ModelPart beltSkull;
	private final ModelPart beltScroll;
	private final ModelPart beltKey;
	private final ModelPart satchel;
	private final ModelPart rootPart;

	public CollectorModel(ModelPart root) {
		super(root);
		this.rootPart = root.getChild("root");
		this.body = this.rootPart.getChild("body");
		this.head = this.body.getChild("head");
		this.skirt = this.body.getChild("skirt");
		this.rightArm = this.body.getChild("right_arm");
		this.leftArm = this.body.getChild("left_arm");
		this.rightLeg = this.rootPart.getChild("right_leg");
		this.leftLeg = this.rootPart.getChild("left_leg");
		this.beltSkull = this.body.getChild("belt_skull");
		this.beltScroll = this.body.getChild("belt_scroll");
		this.beltKey = this.body.getChild("belt_key");
		this.satchel = this.body.getChild("satchel");
	}

	@Override
	public void setupAnim(CollectorRenderState s) {
		super.setupAnim(s);
		float age = s.ageInTicks;
		float walk = s.walkAnimationPos;
		float amt = Math.min(1.0F, s.walkAnimationSpeed * 1.5F);

		this.body.xRot = HUNCH + Mth.sin(age * 0.07F) * 0.02F;
		this.head.xRot = -HUNCH * 0.7F + s.xRot * Mth.DEG_TO_RAD * 0.8F;
		this.head.yRot = s.yRot * Mth.DEG_TO_RAD;
		this.skirt.xRot = -HUNCH + Mth.cos(walk * 0.6F) * 0.12F * amt;

		float step = Mth.cos(walk * 0.6F) * 0.6F * amt;
		this.rightLeg.xRot = step;
		this.leftLeg.xRot = -step;
		this.rootPart.y = Math.abs(Mth.sin(walk * 0.6F)) * -0.4F * amt;

		// trinkets swing a little
		float jingle = Mth.sin(age * 0.11F) * 0.06F + Mth.cos(walk * 0.6F) * 0.25F * amt;
		this.beltSkull.xRot = -HUNCH + jingle;
		this.beltScroll.xRot = -HUNCH + jingle * 0.8F;
		this.beltKey.xRot = -HUNCH - jingle;
		this.satchel.xRot = -HUNCH * 0.7F + jingle * 0.3F;

		if (s.inspecting) {
			// holds the object close to the hood, turning it over
			this.rightArm.xRot = -1.75F + Mth.sin(age * 0.15F) * 0.05F;
			this.rightArm.yRot = -0.45F;
			this.rightArm.zRot = 0.1F;
			this.leftArm.xRot = -1.2F;
			this.leftArm.yRot = 0.55F;
			this.head.xRot = 0.15F;
			this.head.yRot = -0.2F + Mth.sin(age * 0.05F) * 0.15F;
		} else if (amt > 0.15F) {
			this.rightArm.xRot = -HUNCH - step * 0.6F;
			this.leftArm.xRot = -HUNCH + step * 0.6F;
		} else {
			// hands folded in front, idly fidgeting
			this.rightArm.xRot = -HUNCH - 0.55F;
			this.rightArm.yRot = -0.45F;
			this.leftArm.xRot = -HUNCH - 0.5F + Mth.sin(age * 0.09F) * 0.03F;
			this.leftArm.yRot = 0.45F;
		}
	}

	@Override
	public void translateToHand(CollectorRenderState state, HumanoidArm arm, PoseStack poseStack) {
		this.root.translateAndRotate(poseStack);
		this.rootPart.translateAndRotate(poseStack);
		this.body.translateAndRotate(poseStack);
		(arm == HumanoidArm.RIGHT ? this.rightArm : this.leftArm).translateAndRotate(poseStack);
		poseStack.translate(arm == HumanoidArm.RIGHT ? -0.0625F : 0.0625F, 0.05F, 0.0F);
	}
}
