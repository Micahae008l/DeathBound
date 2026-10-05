package com.underworld.client.model;

import com.underworld.client.render.state.HollowHunterRenderState;
import com.underworld.entity.hunter.HollowHunter;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/**
 * Hand-animated (no keyframe files) so every pose is driven by the synced boss state:
 * idle stalk, bow draw (two-bone arm IK), volley summon, sky shot, slash, Last Hunt, kneel, roar, death collapse.
 * Geometry comes from {@link com.underworld.client.model.geom.HollowHunterGeometry} (generated).
 */
public class HollowHunterModel extends EntityModel<HollowHunterRenderState> {
	private static final float PI = Mth.PI;
	private static final float HUNCH = 0.30F;
	private static final float UPPER_LEN = 15.0F;
	private static final float LOWER_LEN = 15.5F;

	private final ModelPart body;
	private final ModelPart torso;
	private final ModelPart head;
	private final ModelPart soulCore;
	private final ModelPart cloak;
	private final ModelPart cloakLeft;
	private final ModelPart cloakRight;
	private final ModelPart quiver;
	private final ModelPart halo;
	private final ModelPart[] spectral = new ModelPart[7];
	private final ModelPart rightArm;
	private final ModelPart rightForearm;
	private final ModelPart leftArm;
	private final ModelPart leftForearm;
	private final ModelPart bow;
	private final ModelPart nockedArrow;
	private final ModelPart stringTop;
	private final ModelPart stringBottom;
	private final ModelPart handArrow;
	private final ModelPart rightLeg;
	private final ModelPart rightShin;
	private final ModelPart leftLeg;
	private final ModelPart leftShin;

	public HollowHunterModel(ModelPart root) {
		super(root);
		this.body = root.getChild("root");
		this.torso = this.body.getChild("torso");
		this.head = this.torso.getChild("head");
		this.soulCore = this.torso.getChild("soul_core");
		this.cloak = this.torso.getChild("cloak");
		this.cloakLeft = this.cloak.getChild("cloak_left");
		this.cloakRight = this.cloak.getChild("cloak_right");
		this.quiver = this.torso.getChild("quiver");
		this.halo = this.torso.getChild("halo");
		for (int i = 0; i < this.spectral.length; i++) {
			this.spectral[i] = this.halo.getChild("spectral_" + i);
		}
		this.rightArm = this.torso.getChild("right_arm");
		this.rightForearm = this.rightArm.getChild("right_forearm");
		this.bow = this.rightForearm.getChild("bow");
		this.nockedArrow = this.bow.getChild("nocked_arrow");
		this.stringTop = this.bow.getChild("string_top");
		this.stringBottom = this.bow.getChild("string_bottom");
		this.leftArm = this.torso.getChild("left_arm");
		this.leftForearm = this.leftArm.getChild("left_forearm");
		this.handArrow = this.leftForearm.getChild("hand_arrow");
		this.rightLeg = this.body.getChild("right_leg");
		this.rightShin = this.rightLeg.getChild("right_shin");
		this.leftLeg = this.body.getChild("left_leg");
		this.leftShin = this.leftLeg.getChild("left_shin");
	}

	@Override
	public void setupAnim(HollowHunterRenderState s) {
		super.setupAnim(s);
		float age = s.ageInTicks;
		float t = s.animTime;
		int phase = Math.max(1, s.phase);
		float walk = s.walkAnimationPos;
		float walkAmt = Math.min(1.0F, s.walkAnimationSpeed * 1.6F);
		float headYaw = s.yRot * Mth.DEG_TO_RAD;
		float headPitch = s.xRot * Mth.DEG_TO_RAD;

		// ------------------------------------------------------------ base: hunched, long arms hanging, stalking gait
		float breathe = Mth.sin(age * 0.06F) * 0.025F;
		this.torso.xRot = HUNCH + breathe;
		this.torso.zRot = Mth.sin(age * 0.035F) * 0.025F;        // slow weight shift
		this.body.x = Mth.sin(age * 0.035F) * 0.35F;
		this.head.xRot = -HUNCH * 0.6F + headPitch * 0.7F;
		this.head.yRot = headYaw;

		float stride = Mth.cos(walk * 0.45F) * 0.55F * walkAmt;
		this.rightLeg.xRot = -0.15F + stride;
		this.leftLeg.xRot = -0.15F - stride;
		this.rightShin.xRot = 0.28F + Math.max(0, -Mth.sin(walk * 0.45F)) * 0.7F * walkAmt;
		this.leftShin.xRot = 0.28F + Math.max(0, Mth.sin(walk * 0.45F)) * 0.7F * walkAmt;
		this.body.y = Math.abs(Mth.sin(walk * 0.45F)) * -0.8F * walkAmt;

		float armSway = Mth.sin(age * 0.05F) * 0.03F;
		this.rightArm.xRot = -HUNCH - stride * 0.5F + armSway;
		this.leftArm.xRot = -HUNCH + stride * 0.5F - armSway;
		this.rightArm.zRot = 0.07F;
		this.leftArm.zRot = -0.07F;
		this.rightForearm.xRot = -0.18F;
		this.leftForearm.xRot = -0.22F;
		this.bow.xRot = 0.0F;
		this.bow.zRot = 0.05F;

		// cloak hangs from the shoulders; more violent in later phases
		float sway = Mth.sin(age * 0.07F) * (0.03F + 0.025F * (phase - 1)) + walkAmt * 0.25F;
		this.cloak.xRot = -HUNCH + 0.08F + sway;
		this.cloak.zRot = Mth.sin(age * 0.045F) * 0.02F * phase;
		this.cloakLeft.yRot = -0.2F + Mth.sin(age * 0.09F) * 0.04F * phase;
		this.cloakRight.yRot = 0.2F - Mth.sin(age * 0.09F + 1) * 0.04F * phase;
		if (phase >= 3) { // unstable
			this.cloak.xRot += Mth.sin(age * 1.3F) * 0.035F;
			this.cloak.zRot += Mth.cos(age * 1.7F) * 0.03F;
		}

		// floating spectral arrows behind the shoulder
		int arrows = !s.eyeLit ? 0 : phase == 1 ? 3 : phase == 2 ? 5 : 7;
		if (s.anim == HollowHunter.ANIM_SUMMON) {
			arrows = 0; // they become real projectiles for the volley
		}
		if (s.anim == HollowHunter.ANIM_LAST_HUNT) {
			arrows = 7;
		}
		this.halo.xRot = -HUNCH;
		for (int i = 0; i < this.spectral.length; i++) {
			ModelPart a = this.spectral[i];
			a.visible = i < arrows;
			a.y += Mth.sin(age * 0.1F + i * 1.3F) * 0.35F; // they hover in place, they do not orbit
		}
		this.soulCore.visible = phase >= 2 && s.eyeLit;
		float pulse = 1.0F + Mth.sin(age * 0.25F) * 0.12F + (phase >= 3 ? 0.25F : 0.0F);
		this.soulCore.xScale = this.soulCore.yScale = this.soulCore.zScale = pulse;
		this.soulCore.yRot = age * 0.1F;
		this.nockedArrow.visible = false;
		this.handArrow.visible = false;
		this.setString(0.0F);

		// ------------------------------------------------------------ attack poses
		switch (s.anim) {
			case HollowHunter.ANIM_DRAW -> this.poseShotCycle(t / HollowHunter.drawTimeFor(phase), headYaw, headPitch);
			case HollowHunter.ANIM_RELEASE -> this.poseRelease(t, headYaw, headPitch);
			case HollowHunter.ANIM_INTRO -> this.poseIntro(t);
			case HollowHunter.ANIM_RAIN -> {
				this.poseShotCycle(t / 16.0F, headYaw * 0.3F, -1.1F);
				this.head.xRot = Mth.lerp(ease(t / 10.0F), this.head.xRot, -0.85F);
			}
			case HollowHunter.ANIM_SUMMON -> this.poseSummon(t, age);
			case HollowHunter.ANIM_SLASH -> this.poseSlash(t);
			case HollowHunter.ANIM_LAST_HUNT -> this.poseLastHunt(t, age);
			case HollowHunter.ANIM_VULNERABLE -> this.poseKneel(Mth.clamp(t / 8.0F, 0, 1), age);
			case HollowHunter.ANIM_ROAR -> this.poseRoar(t, age);
			default -> {
			}
		}

		if (s.deathTime > 0) {
			this.poseDeath(s.deathTime);
		}
	}

	// ================================================================ poses
	// ================================================================ the shot: grab from back -> nock -> draw -> release
	private static float ease(float x) {
		x = Mth.clamp(x, 0.0F, 1.0F);
		return x * x * (3.0F - 2.0F * x);
	}

	/** Everything the shot animation needs, worked out in the torso's frame. */
	private final class Aim {
		final Vector3f dir;
		final Vector3f grip;
		final Vector3f restHand;
		final Vector3f backHand;
		final float drawLen;

		Aim(float yaw, float pitch, float stance) {
			HollowHunterModel m = HollowHunterModel.this;
			this.dir = new Vector3f(0, Mth.sin(pitch), -Mth.cos(pitch)).rotateY(yaw).rotateY(-m.torso.yRot).rotateX(-m.torso.xRot);
			Vector3f shoulderR = new Vector3f(m.rightArm.x, m.rightArm.y, m.rightArm.z);
			Vector3f shoulderL = new Vector3f(m.leftArm.x, m.leftArm.y, m.leftArm.z);
			this.grip = new Vector3f(this.dir).mul(UPPER_LEN + LOWER_LEN - 1.5F).add(shoulderR);
			this.restHand = new Vector3f(shoulderL).add(0.5F, 29.0F, -4.0F);
			this.backHand = new Vector3f(shoulderL).add(-1.5F, -11.0F, 7.5F);  // up behind the left shoulder, among the arrows
			// anchor: the string comes back to the hood, on the drawing-hand side
			Vector3f cheek = new Vector3f(2.5F, -3.0F, -5.5F).rotateX(m.head.xRot).rotateY(m.head.yRot).add(m.head.x, m.head.y, m.head.z);
			this.drawLen = Mth.clamp(new Vector3f(this.grip).sub(cheek).dot(this.dir) - 3.0F, 6.0F, 25.0F);
		}

		Vector3f stringHand(float draw) {
			return new Vector3f(this.dir).mul(-(3.0F + this.drawLen * draw)).add(this.grip);
		}
	}

	/**
	 * @param u 0..1 progress of the shot (1 = fully drawn, held there until release)
	 */
	private void poseShotCycle(float u, float yaw, float pitch) {
		float inW = ease(u / 0.12F);                    // blend out of whatever pose came before
		float raise = ease((u - 0.12F) / 0.33F);         // bow arm up + side-on stance
		float toBack = ease(u / 0.28F);                  // free hand travels to the arrows on his back
		float toString = ease((u - 0.30F) / 0.22F);      // ... then brings the arrow to the string
		float draw = ease((u - 0.52F) / 0.40F);          // ... and draws to the hood

		this.applyStance(raise, yaw);
		Aim aim = new Aim(yaw, pitch, raise);
		this.applyBowArm(aim, raise, 0.0F);

		Vector3f hand = new Vector3f(aim.restHand).lerp(aim.backHand, toBack).lerp(aim.stringHand(draw), toString);
		Vector3f pole = new Vector3f(1.0F, -0.4F, 0.3F).lerp(new Vector3f(1.0F, 0.7F, 0.8F), toString);
		this.applyFreeArm(hand, pole, inW);

		boolean holding = u >= 0.22F && u < 0.52F;
		this.handArrow.visible = holding;
		this.nockedArrow.visible = u >= 0.52F;
		this.spectral[0].visible &= u < 0.22F;          // that one is in his hand now
		float d = aim.drawLen * draw;
		this.setString(d);
		this.nockedArrow.z += d;
		if (draw > 0.95F) { // strain at full draw
			this.bow.zRot += Mth.sin(u * 90.0F) * 0.01F;
		}
	}

	private void poseRelease(float t, float yaw, float pitch) {
		float hold = 1.0F - ease((t - 3.0F) / 8.0F);     // relax back toward idle over ~10 ticks
		float flick = ease(t / 2.0F) * (1.0F - ease((t - 3.0F) / 5.0F));
		float recoil = 1.0F - ease(t / 5.0F);

		this.applyStance(hold, yaw);
		Aim aim = new Aim(yaw, pitch, hold);
		this.applyBowArm(aim, hold, recoil * 0.22F);

		Vector3f hand = aim.stringHand(0.0F).add(new Vector3f(aim.dir).mul(-(aim.drawLen + 5.0F) * flick)).add(3.0F * flick, -1.5F * flick, 0);
		hand = new Vector3f(aim.restHand).lerp(hand, hold);
		this.applyFreeArm(hand, new Vector3f(1.0F, 0.7F, 0.8F), hold);

		// the string snaps forward and shivers
		this.setString(t < 7 ? Mth.sin(t * 2.6F) * 1.6F * (1.0F - t / 7.0F) : 0.0F);
		// the arrow he took regrows on his back
		ModelPart regrow = this.spectral[0];
		float g = ease((t - 4.0F) / 8.0F);
		regrow.visible &= g > 0.01F;
		regrow.xScale *= g;
		regrow.yScale *= g;
		regrow.zScale *= g;
	}

	private void applyStance(float w, float yaw) {
		this.torso.xRot = Mth.lerp(w, this.torso.xRot, 0.1F);
		this.torso.yRot = Mth.lerp(w, this.torso.yRot, -0.65F);   // side-on, bow shoulder toward the target
		this.head.yRot = Mth.lerp(w, this.head.yRot, yaw + 0.65F);
		this.head.xRot = Mth.lerp(w, this.head.xRot, this.head.xRot + 0.05F);
		this.rightLeg.yRot = -0.35F * w;
		this.leftLeg.yRot = 0.25F * w;
		this.leftLeg.xRot = Mth.lerp(w, this.leftLeg.xRot, 0.15F);
	}

	private void applyBowArm(Aim aim, float w, float recoil) {
		float rx = this.rightArm.xRot;
		float ry = this.rightArm.yRot;
		float rz = this.rightArm.zRot;
		float fx = this.rightForearm.xRot;
		pointAlong(this.rightArm, aim.dir);
		this.rightArm.xRot = Mth.lerp(w, rx, this.rightArm.xRot - recoil);
		this.rightArm.yRot = Mth.lerp(w, ry, this.rightArm.yRot);
		this.rightArm.zRot = Mth.lerp(w, rz, 0.0F);
		this.rightForearm.xRot = Mth.lerp(w, fx, -0.08F);
		this.bow.xRot = Mth.lerp(w, 0.0F, PI / 2);
		this.bow.zRot = Mth.lerp(w, this.bow.zRot, 0.18F);
	}

	private void applyFreeArm(Vector3f hand, Vector3f pole, float w) {
		float lx = this.leftArm.xRot;
		float ly = this.leftArm.yRot;
		float lz = this.leftArm.zRot;
		float fx = this.leftForearm.xRot;
		float fy = this.leftForearm.yRot;
		twoBone(this.leftArm, this.leftForearm, new Vector3f(this.leftArm.x, this.leftArm.y, this.leftArm.z), hand, pole);
		this.leftArm.xRot = Mth.lerp(w, lx, this.leftArm.xRot);
		this.leftArm.yRot = Mth.lerp(w, ly, this.leftArm.yRot);
		this.leftArm.zRot = Mth.lerp(w, lz, this.leftArm.zRot);
		this.leftForearm.xRot = Mth.lerp(w, fx, this.leftForearm.xRot);
		this.leftForearm.yRot = Mth.lerp(w, fy, this.leftForearm.yRot);
	}

	/** Bends the two string halves back to the nock point, {@code pull} pixels behind the bow. */
	private void setString(float pull) {
		float len = Mth.sqrt(256.0F + pull * pull) / 16.0F;
		float angle = (float) Mth.atan2(pull, 16.0);
		this.stringTop.xRot = angle;
		this.stringTop.yScale = len;
		this.stringBottom.xRot = -angle;
		this.stringBottom.yScale = len;
	}

	private void poseIntro(float t) {
		// materialising: he straightens up from a deeper crouch while the cloak settles
		float p = Mth.clamp(t / 30.0F, 0, 1);
		this.torso.xRot += (1 - p) * 0.35F;
		this.head.xRot += (1 - p) * 0.4F;
		this.cloak.xRot += (1 - p) * Mth.sin(t * 0.8F) * 0.1F;
	}

	private void poseSummon(float t, float age) {
		float p = Mth.clamp(t / 8.0F, 0, 1);
		this.torso.xRot = Mth.lerp(p, this.torso.xRot, -0.05F);
		this.head.xRot = Mth.lerp(p, this.head.xRot, -0.35F);
		// free hand raised high, fingers spread toward the formation behind him
		this.leftArm.xRot = Mth.lerp(p, this.leftArm.xRot, -2.7F);
		this.leftArm.zRot = Mth.lerp(p, this.leftArm.zRot, -0.35F);
		this.leftForearm.xRot = Mth.lerp(p, this.leftForearm.xRot, -0.3F + Mth.sin(age * 0.6F) * 0.05F);
		this.rightArm.zRot = Mth.lerp(p, this.rightArm.zRot, 0.35F);
		if (t > 22) { // command the launch
			float q = Mth.clamp((t - 22) / 4.0F, 0, 1);
			this.leftArm.xRot = Mth.lerp(q, this.leftArm.xRot, -1.6F);
		}
	}

	private void poseSlash(float t) {
		this.torso.xRot = 0.2F;
		float wind = Mth.clamp(t / 5.0F, 0, 1);
		float sweep = Mth.clamp((t - 5) / 4.0F, 0, 1);
		float back = Mth.clamp((t - 10) / 6.0F, 0, 1);
		this.rightArm.xRot = -1.3F;
		this.rightArm.yRot = Mth.lerp(sweep, Mth.lerp(wind, 0, -1.3F), 1.1F) * (1 - back);
		this.rightArm.zRot = 0;
		this.rightForearm.xRot = -0.2F;
		this.bow.xRot = PI / 2 * (1 - back);
		this.torso.yRot = Mth.lerp(sweep, Mth.lerp(wind, 0, -0.35F), 0.3F) * (1 - back);
		this.leftArm.xRot = -0.7F;
		this.leftArm.zRot = -0.5F;
	}

	private void poseLastHunt(float t, float age) {
		float p = Mth.clamp(t / 12.0F, 0, 1);
		this.body.y -= p * (3.0F + Mth.sin(age * 0.15F) * 0.8F);
		this.torso.xRot = Mth.lerp(p, this.torso.xRot, -0.12F);
		this.head.xRot = Mth.lerp(p, this.head.xRot, -0.5F);
		this.rightArm.xRot = Mth.lerp(p, this.rightArm.xRot, -0.5F);
		this.rightArm.zRot = Mth.lerp(p, this.rightArm.zRot, 1.25F);
		this.leftArm.xRot = Mth.lerp(p, this.leftArm.xRot, -0.5F);
		this.leftArm.zRot = Mth.lerp(p, this.leftArm.zRot, -1.25F);
		this.leftForearm.xRot = -0.4F;
		this.rightForearm.xRot = -0.2F;
		this.bow.xRot = 0.4F;
		this.halo.y -= p * 2.0F;
		for (int i = 0; i < this.spectral.length; i++) {
			this.spectral[i].zRot += p * 0.06F * i; // the fan opens wider
		}
		this.cloak.xRot += Mth.sin(age * 0.9F) * 0.08F * p;
		// trembling right before release
		if (t > 38 && t < 52) {
			this.torso.zRot = Mth.sin(age * 3.1F) * 0.03F;
		}
	}

	private void poseKneel(float p, float age) {
		this.body.y += p * 9.0F;
		this.rightLeg.xRot = Mth.lerp(p, this.rightLeg.xRot, -1.45F);
		this.rightShin.xRot = Mth.lerp(p, this.rightShin.xRot, 1.55F);
		this.leftLeg.xRot = Mth.lerp(p, this.leftLeg.xRot, -0.5F);
		this.leftShin.xRot = Mth.lerp(p, this.leftShin.xRot, 1.9F);
		this.torso.xRot = Mth.lerp(p, this.torso.xRot, 0.75F + Mth.sin(age * 0.1F) * 0.04F);
		this.head.xRot = Mth.lerp(p, this.head.xRot, 0.3F);
		this.rightArm.xRot = Mth.lerp(p, this.rightArm.xRot, -0.6F);
		this.rightArm.zRot = Mth.lerp(p, this.rightArm.zRot, 0.25F);
		this.leftArm.xRot = Mth.lerp(p, this.leftArm.xRot, -0.75F);
		this.bow.xRot = Mth.lerp(p, 0, 0.6F);
		this.cloak.xRot = Mth.lerp(p, this.cloak.xRot, -0.45F);
	}

	private void poseRoar(float t, float age) {
		float p = Mth.clamp(t / 6.0F, 0, 1) * (1 - Mth.clamp((t - 20) / 6.0F, 0, 1));
		this.torso.xRot = Mth.lerp(p, this.torso.xRot, -0.25F);
		this.head.xRot = Mth.lerp(p, this.head.xRot, -0.8F);
		this.head.zRot = Mth.sin(age * 2.2F) * 0.08F * p;
		this.rightArm.zRot = Mth.lerp(p, this.rightArm.zRot, 0.9F);
		this.leftArm.zRot = Mth.lerp(p, this.leftArm.zRot, -0.9F);
		this.rightArm.xRot = Mth.lerp(p, this.rightArm.xRot, -0.3F);
		this.leftArm.xRot = Mth.lerp(p, this.leftArm.xRot, -0.3F);
		this.cloak.xRot += Mth.sin(age * 1.1F) * 0.12F * p;
	}

	/** Bow drops, body sags and kneels, then the skeleton comes apart and the cloak settles flat before fading. */
	private void poseDeath(float d) {
		float drop = Mth.clamp(d / 12.0F, 0, 1);
		float sag = Mth.clamp((d - 8) / 26.0F, 0, 1);
		float apart = Mth.clamp((d - 36) / 22.0F, 0, 1);
		float fade = Mth.clamp((d - 58) / 20.0F, 0, 1);

		this.nockedArrow.visible = false;
		this.handArrow.visible = false;
		this.rightForearm.xRot = Mth.lerp(drop, this.rightForearm.xRot, 0.35F);
		this.bow.xRot = Mth.lerp(drop, this.bow.xRot, 1.0F);
		this.bow.y += drop * 3.0F + apart * 10.0F;

		this.poseKneel(sag, 0);
		this.torso.xRot = Mth.lerp(sag, this.torso.xRot, 1.05F);
		this.head.xRot = Mth.lerp(sag, this.head.xRot, 0.7F);

		// pieces separate and fall
		this.head.y += apart * 9.0F;
		this.head.z -= apart * 6.0F;
		this.head.zRot += apart * 0.9F;
		this.rightArm.x -= apart * 3.0F;
		this.rightArm.y += apart * 6.0F;
		this.rightArm.zRot += apart * 0.9F;
		this.leftArm.x += apart * 3.0F;
		this.leftArm.y += apart * 6.0F;
		this.leftArm.zRot -= apart * 0.9F;
		this.body.y += apart * 6.0F;
		this.cloak.xRot = Mth.lerp(apart, this.cloak.xRot, -1.45F);
		this.cloak.y += apart * 4.0F;

		float bones = 1.0F - Mth.clamp((d - 48) / 20.0F, 0, 1);
		for (ModelPart part : new ModelPart[]{this.head, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.soulCore, this.quiver}) {
			part.xScale *= bones;
			part.yScale *= bones;
			part.zScale *= bones;
		}
		this.halo.visible = d < 6;
		float cloakScale = 1.0F - fade;
		this.cloak.xScale *= cloakScale;
		this.cloak.yScale *= cloakScale;
		this.cloak.zScale *= cloakScale;
		this.torso.xScale *= Math.max(bones, cloakScale);
		this.torso.yScale *= Math.max(bones, cloakScale);
		this.torso.zScale *= Math.max(bones, cloakScale);
	}

	// ================================================================ tiny IK helpers
	/** Rotates a limb (whose rest direction is +Y, model space) to point along {@code dir} given in its parent's frame. */
	private static void pointAlong(ModelPart part, Vector3f dir) {
		Vector3f d = new Vector3f(dir).normalize();
		part.xRot = -(float) Math.acos(Mth.clamp(d.y, -1.0F, 1.0F));
		part.yRot = (float) Mth.atan2(-d.x, -d.z);
		part.zRot = 0;
	}

	/**
	 * Classic two-bone solve. Positions are in the frame of {@code upper}'s parent.
	 * @param pole rough direction the elbow should bend toward
	 */
	private static void twoBone(ModelPart upper, ModelPart lower, Vector3f shoulder, Vector3f target, Vector3f pole) {
		Vector3f toT = new Vector3f(target).sub(shoulder);
		float dist = Mth.clamp(toT.length(), Math.abs(UPPER_LEN - LOWER_LEN) + 0.1F, UPPER_LEN + LOWER_LEN - 0.1F);
		Vector3f u = new Vector3f(toT).normalize();
		Vector3f w = new Vector3f(pole).sub(new Vector3f(u).mul(pole.dot(u)));
		if (w.lengthSquared() < 1.0E-4F) {
			w.set(0, 1, 0);
		}
		w.normalize();
		float cosA = (UPPER_LEN * UPPER_LEN + dist * dist - LOWER_LEN * LOWER_LEN) / (2 * UPPER_LEN * dist);
		float a = (float) Math.acos(Mth.clamp(cosA, -1.0F, 1.0F));
		Vector3f upperDir = new Vector3f(u).mul(Mth.cos(a)).add(new Vector3f(w).mul(Mth.sin(a)));
		Vector3f elbow = new Vector3f(upperDir).mul(UPPER_LEN).add(shoulder);
		Vector3f lowerDir = new Vector3f(u).mul(dist).add(shoulder).sub(elbow).normalize();

		pointAlong(upper, upperDir);
		// express the forearm direction in the upper arm's local frame (undo Ry then Rx)
		Vector3f local = new Vector3f(lowerDir).rotateY(-upper.yRot).rotateX(-upper.xRot);
		pointAlong(lower, local);
	}
}
