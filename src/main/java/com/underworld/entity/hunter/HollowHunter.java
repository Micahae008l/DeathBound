package com.underworld.entity.hunter;

import com.underworld.registry.ModEffects;
import com.underworld.registry.ModSounds;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * THE HOLLOW HUNTER - secret optional boss of the Underworld.
 *
 * <p>Lifecycle: DORMANT (hidden, waiting in his grounds) -> INTRO (appears, turns, eye lights, draws) -> FIGHT.
 * If every player leaves or dies the fight resets and he goes dormant again at full health.
 *
 * <p>Phases: 1 "The Hunt" (100-60%), 2 "The Hollow Hunt" (60-20%), 3 "The Last Hunt" (&lt;20%).
 * All behaviour is a small hand-written state machine in {@link #customServerAiStep} so it stays readable
 * and tunable - see the constants block below for balancing.
 */
public class HollowHunter extends Monster {
	// ---------------------------------------------------------------- balancing
	public static final float MAX_HEALTH = 500.0F;
	public static final int ARENA_RADIUS = 22;
	private static final double PREFERRED_MIN = 9.0;
	private static final double PREFERRED_MAX = 17.0;
	private static final double TOO_CLOSE = 5.5;
	private static final double MELEE_RANGE = 2.9;
	private static final float PHASE_2_AT = 0.60F;
	private static final float PHASE_3_AT = 0.20F;
	private static final float ARROW_DAMAGE = 2.2F; // x ~2.8 velocity -> ~6-7 damage
	private static final float ARROW_SPEED = 2.8F;
	private static final float SLASH_DAMAGE = 7.0F;
	private static final int VULNERABLE_TICKS = 90;
	private static final float VULNERABLE_DAMAGE_MULT = 1.5F;
	private static final int DEATH_TICKS = 80;

	// ---------------------------------------------------------------- synced state
	public static final int ANIM_IDLE = 0;
	public static final int ANIM_DRAW = 1;
	public static final int ANIM_SUMMON = 2;
	public static final int ANIM_RAIN = 3;
	public static final int ANIM_SLASH = 4;
	public static final int ANIM_LAST_HUNT = 5;
	public static final int ANIM_VULNERABLE = 6;
	public static final int ANIM_INTRO = 7;
	public static final int ANIM_ROAR = 8;
	public static final int ANIM_RELEASE = 9;

	/** Ticks from "reach for an arrow" to release. Shared with the client model so the animation lines up. */
	public static int drawTimeFor(int phase) {
		return switch (Math.max(1, phase)) {
			case 1 -> 24;
			case 2 -> 18;
			default -> 14;
		};
	}

	private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(HollowHunter.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_ANIM = SynchedEntityData.defineId(HollowHunter.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_EYE = SynchedEntityData.defineId(HollowHunter.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_HIDDEN = SynchedEntityData.defineId(HollowHunter.class, EntityDataSerializers.BOOLEAN);

	private static final int PURPLE = 0xA855F7;
	private static final DustParticleOptions DUST = new DustParticleOptions(PURPLE, 1.0F);
	private static final DustParticleOptions DUST_BIG = new DustParticleOptions(0xC084FC, 1.15F);

	// ---------------------------------------------------------------- server state
	private enum Stage { DORMANT, INTRO, FIGHT, SHOWCASE }

	private enum Attack { NONE, SHOT, HOLLOW, VOLLEY, RAIN, SLASH, LAST_HUNT, VULNERABLE, ROAR }

	private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
		this.getUUID(),
		Component.literal("THE HOLLOW HUNTER").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
		BossEvent.BossBarColor.PURPLE,
		BossEvent.BossBarOverlay.NOTCHED_10
	).setDarkenScreen(true);

	private Stage stage = Stage.DORMANT;
	private @Nullable BlockPos home;
	private int stageTicks;
	private @Nullable Player introTarget;

	private Attack attack = Attack.NONE;
	private int attackTicks;
	private int attackCooldown = 30;
	private int teleportCooldown = 160;
	private int slashCooldown;
	private int lastHuntCooldown;
	private int shotsLeft;
	private int noTargetTicks;
	private int stuckTicks;
	private int strafeTicks;
	private int strafeDir = 1;
	private Vec3 lastPos = Vec3.ZERO;
	private Vec3 lastTargetPos = Vec3.ZERO;
	private Vec3 targetVelocity = Vec3.ZERO;
	private @Nullable DamageSource pendingDeathSource;

	// ---------------------------------------------------------------- client state
	private int clientAnimStart;

	public HollowHunter(EntityType<? extends Monster> type, Level level) {
		super(type, level);
		this.xpReward = 350;
		this.setPersistenceRequired();
		this.bossEvent.setVisible(false);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, MAX_HEALTH)
			.add(Attributes.ARMOR, 6.0)
			.add(Attributes.MOVEMENT_SPEED, 0.30)
			.add(Attributes.ATTACK_DAMAGE, SLASH_DAMAGE)
			.add(Attributes.FOLLOW_RANGE, 48.0)
			.add(Attributes.ATTACK_KNOCKBACK, 1.6)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.85)
			.add(Attributes.STEP_HEIGHT, 1.1);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_PHASE, 0);
		builder.define(DATA_ANIM, ANIM_IDLE);
		builder.define(DATA_EYE, false);
		builder.define(DATA_HIDDEN, true);
	}

	// ================================================================= accessors used by the renderer
	public int getPhase() {
		return this.entityData.get(DATA_PHASE);
	}

	public int getAnim() {
		return this.entityData.get(DATA_ANIM);
	}

	public boolean isEyeLit() {
		return this.entityData.get(DATA_EYE);
	}

	public boolean isHidden() {
		return this.entityData.get(DATA_HIDDEN);
	}

	/** Ticks since the current animation started, client side. */
	public float getAnimTime(float partialTicks) {
		return this.tickCount - this.clientAnimStart + partialTicks;
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
		super.onSyncedDataUpdated(accessor);
		if (DATA_ANIM.equals(accessor)) {
			this.clientAnimStart = this.tickCount;
		}
	}

	private void setAnim(int anim) {
		if (this.getAnim() == anim) {
			this.entityData.set(DATA_ANIM, ANIM_IDLE, true);
		}
		this.entityData.set(DATA_ANIM, anim, true);
	}

	public void setHome(BlockPos home) {
		this.home = home.immutable();
	}

	private BlockPos home() {
		if (this.home == null) {
			this.home = this.blockPosition();
		}
		return this.home;
	}

	// ================================================================= tick
	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			this.clientEffects();
		}
	}

	private void clientEffects() {
		if (this.isHidden() || this.isDeadOrDying()) {
			return;
		}
		int phase = this.getPhase();
		int rolls = phase <= 1 ? 1 : phase == 2 ? 2 : 4;
		for (int i = 0; i < rolls; i++) {
			if (this.random.nextInt(4) == 0) {
				this.level().addParticle(ParticleTypes.SOUL, this.getRandomX(0.8), this.getY() + this.random.nextDouble() * 3.0, this.getRandomZ(0.8), 0, 0.02, 0);
			}
			if (phase >= 2 && this.random.nextInt(3) == 0) {
				this.level().addParticle(DUST, this.getRandomX(1.0), this.getY() + 0.5 + this.random.nextDouble() * 2.8, this.getRandomZ(1.0), 0, 0.03, 0);
			}
		}
		if (phase >= 3 && this.random.nextInt(2) == 0) {
			this.level().addParticle(ParticleTypes.REVERSE_PORTAL, this.getRandomX(0.6), this.getY() + 1.0 + this.random.nextDouble() * 2.0, this.getRandomZ(0.6), 0, 0.02, 0);
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		this.stageTicks++;
		switch (this.stage) {
			case DORMANT -> this.tickDormant(level);
			case INTRO -> this.tickIntro(level);
			case FIGHT -> this.tickFight(level);
			case SHOWCASE -> {
				this.getNavigation().stop();
				Player p = level.getNearestPlayer(this, 32);
				if (p != null) {
					this.getLookControl().setLookAt(p, 10.0F, 10.0F);
				}
			}
		}
		this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
	}

	// ----------------------------------------------------------------- dormant & intro
	private void tickDormant(ServerLevel level) {
		this.getNavigation().stop();
		this.setDeltaMovement(this.getDeltaMovement().multiply(0, 1, 0));
		if (!this.isHidden()) {
			this.entityData.set(DATA_HIDDEN, true);
		}
		if (this.stageTicks % 10 != 0) {
			return;
		}
		for (Player player : level.players()) {
			if (this.isValidTarget(player) && this.inArena(player.position(), ARENA_RADIUS - 3)) {
				this.startIntro(level, player);
				return;
			}
		}
	}

	/** Starts the encounter. Also used by /underworld hunter start. */
	public void startIntro(ServerLevel level, Player player) {
		if (this.stage != Stage.DORMANT) {
			return;
		}
		this.stage = Stage.INTRO;
		this.stageTicks = 0;
		this.introTarget = player;
		BlockPos h = this.home();
		this.teleportTo(h.getX() + 0.5, h.getY(), h.getZ() + 0.5);
		// face away from the player so he can slowly turn around
		double away = Mth.atan2(this.getZ() - player.getZ(), this.getX() - player.getX()) * Mth.RAD_TO_DEG - 90.0;
		this.snapRotation((float) away);
		for (ServerPlayer p : this.nearbyPlayers(level, ARENA_RADIUS + 16)) {
			p.connection.send(new ClientboundStopSoundPacket(null, SoundSource.MUSIC));
			p.connection.send(new ClientboundStopSoundPacket(null, SoundSource.AMBIENT));
		}
	}

	private void tickIntro(ServerLevel level) {
		Player player = this.introTarget;
		this.getNavigation().stop();
		if (player == null || !player.isAlive()) {
			this.resetFight(level);
			return;
		}
		int t = this.stageTicks;
		if (t == 1) {
			level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, SoundSource.HOSTILE, 2.5F, 0.5F);
		}
		if (t == 22) {
			this.entityData.set(DATA_HIDDEN, false);
			this.setAnim(ANIM_INTRO);
			level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 1.6, this.getZ(), 40, 0.5, 1.2, 0.5, 0.04);
			level.sendParticles(DUST_BIG, this.getX(), this.getY() + 1.6, this.getZ(), 40, 0.6, 1.4, 0.6, 0.0);
			level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 2.0F, 0.5F);
		}
		if (t > 30 && t < 70) {
			// slow, deliberate turn toward the intruder
			float want = this.yawToward(player.position());
			float next = Mth.approachDegrees(this.getYRot(), want, 4.5F);
			this.snapRotation(next);
			this.setXRot(0);
		}
		if (t == 62) {
			this.entityData.set(DATA_EYE, true);
			level.playSound(null, this.getX(), this.getY() + 2.8, this.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 2.5F, 0.5F);
			level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, PURPLE), this.getX(), this.getEyeY(), this.getZ(), 1, 0, 0, 0, 0);
		}
		if (t == 72) {
			this.setAnim(ANIM_DRAW);
			level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), SoundSource.HOSTILE, 1.5F, 0.5F);
		}
		if (t >= 88) {
			this.beginFight(level);
		}
	}

	private void beginFight(ServerLevel level) {
		this.stage = Stage.FIGHT;
		this.stageTicks = 0;
		this.entityData.set(DATA_PHASE, 1);
		this.entityData.set(DATA_EYE, true);
		this.entityData.set(DATA_HIDDEN, false);
		this.bossEvent.setVisible(true);
		this.attack = Attack.NONE;
		this.attackCooldown = 6;
		this.teleportCooldown = 140;
		this.lastHuntCooldown = 0;
		this.setTarget(this.introTarget);
		this.setAnim(ANIM_IDLE);
	}

	/** Puts him back to sleep at full health (everyone left or died). */
	public void resetFight(ServerLevel level) {
		this.stage = Stage.DORMANT;
		this.stageTicks = 0;
		this.attack = Attack.NONE;
		this.introTarget = null;
		this.setTarget(null);
		this.setHealth(this.getMaxHealth());
		this.entityData.set(DATA_PHASE, 0);
		this.entityData.set(DATA_EYE, false);
		this.entityData.set(DATA_HIDDEN, true);
		this.setAnim(ANIM_IDLE);
		this.bossEvent.removeAllPlayers();
		this.bossEvent.setVisible(false);
		this.clearArenaProjectiles(level);
		BlockPos h = this.home();
		this.teleportTo(h.getX() + 0.5, h.getY(), h.getZ() + 0.5);
	}

	// ----------------------------------------------------------------- fight
	private void tickFight(ServerLevel level) {
		this.updateBossBarPlayers(level);
		Player target = this.pickTarget(level);
		if (target == null) {
			if (++this.noTargetTicks > 160) {
				this.resetFight(level);
			}
			return;
		}
		this.noTargetTicks = 0;
		this.setTarget(target);
		this.trackTargetVelocity(target);

		// never leave the grounds
		if (!this.inArena(this.position(), ARENA_RADIUS + 3)) {
			this.tacticalTeleport(level, target, true);
		}

		this.updatePhase(level);

		if (this.attackCooldown > 0) this.attackCooldown--;
		if (this.teleportCooldown > 0) this.teleportCooldown--;
		if (this.slashCooldown > 0) this.slashCooldown--;
		if (this.lastHuntCooldown > 0) this.lastHuntCooldown--;

		if (this.attack == Attack.NONE) {
			this.getLookControl().setLookAt(target, 30.0F, 30.0F);
			this.moveTactically(level, target);
			this.chooseAttack(level, target);
		}
		if (this.attack != Attack.NONE) {
			this.attackTicks++;
			this.tickAttack(level, target);
		}
	}

	private void updatePhase(ServerLevel level) {
		float frac = this.getHealth() / this.getMaxHealth();
		int phase = this.getPhase();
		int want = frac <= PHASE_3_AT ? 3 : frac <= PHASE_2_AT ? 2 : 1;
		if (want > phase) {
			this.entityData.set(DATA_PHASE, want);
			if (this.attack != Attack.LAST_HUNT && this.attack != Attack.VULNERABLE) {
				this.startAttack(Attack.ROAR);
			}
			if (want == 3) {
				this.lastHuntCooldown = 0;
			}
			level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 1.8, this.getZ(), 60, 0.8, 1.4, 0.8, 0.08);
			level.sendParticles(DUST_BIG, this.getX(), this.getY() + 1.8, this.getZ(), 60, 1.2, 1.6, 1.2, 0.0);
		}
	}

	private void chooseAttack(ServerLevel level, LivingEntity target) {
		double dist = this.distanceTo(target);
		int phase = Math.max(1, this.getPhase());

		if (dist < MELEE_RANGE && this.slashCooldown <= 0) {
			this.startAttack(Attack.SLASH);
			return;
		}
		if (phase >= 3 && this.lastHuntCooldown <= 0) {
			this.startAttack(Attack.LAST_HUNT);
			return;
		}
		if (this.teleportCooldown <= 0 || (dist < TOO_CLOSE && this.teleportCooldown < 60)) {
			this.tacticalTeleport(level, target, false);
			this.teleportCooldown = (phase == 1 ? 190 : phase == 2 ? 140 : 105) + this.random.nextInt(50);
			this.attackCooldown = Math.max(this.attackCooldown, 6);
			return;
		}
		if (this.attackCooldown > 0 || !this.hasLineOfSight(target)) {
			return;
		}

		int wShot = phase == 1 ? 40 : 28;
		int wHollow = phase == 1 ? 30 : 30;
		int wVolley = phase == 1 ? 14 : 24;
		int wRain = phase == 1 ? 0 : 18;
		int roll = this.random.nextInt(wShot + wHollow + wVolley + wRain);
		if ((roll -= wShot) < 0) {
			this.startAttack(Attack.SHOT);
		} else if ((roll -= wHollow) < 0) {
			this.startAttack(Attack.HOLLOW);
		} else if ((roll -= wVolley) < 0) {
			this.startAttack(Attack.VOLLEY);
		} else {
			this.startAttack(Attack.RAIN);
		}
	}

	private void startAttack(Attack attack) {
		this.attack = attack;
		this.attackTicks = 0;
		int phase = Math.max(1, this.getPhase());
		switch (attack) {
			case SHOT -> {
				this.shotsLeft = phase == 1 ? 1 : 2;
				this.setAnim(ANIM_DRAW);
			}
			case HOLLOW -> {
				this.shotsLeft = phase == 1 ? 1 : phase == 2 ? 2 : 3;
				this.setAnim(ANIM_DRAW);
			}
			case VOLLEY -> this.setAnim(ANIM_SUMMON);
			case RAIN -> this.setAnim(ANIM_RAIN);
			case SLASH -> this.setAnim(ANIM_SLASH);
			case LAST_HUNT -> this.setAnim(ANIM_LAST_HUNT);
			case VULNERABLE -> this.setAnim(ANIM_VULNERABLE);
			case ROAR -> this.setAnim(ANIM_ROAR);
			case NONE -> this.setAnim(ANIM_IDLE);
		}
	}

	private void endAttack(int cooldown) {
		this.attack = Attack.NONE;
		this.attackCooldown = cooldown;
		this.setAnim(ANIM_IDLE);
	}

	private int drawTime() {
		return drawTimeFor(this.getPhase());
	}

	/**
	 * Shared shot rhythm: reach over the shoulder for a spectral arrow, nock, draw, release.
	 * Negative {@code t} is the short pause before a follow-up shot.
	 * @return true on the tick the arrow should be fired
	 */
	private boolean tickDrawCycle(int t, int draw) {
		if (t == 0) {
			this.setAnim(ANIM_DRAW); // follow-up shot restarts the cycle
		}
		if (t == 1) {
			this.playSound(ModSounds.HUNTER_ARROW_GRAB, 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
		}
		if (t == draw / 2) {
			this.playSound(ModSounds.HUNTER_BOW_DRAW, 1.3F, 0.9F + this.random.nextFloat() * 0.15F);
		}
		return t == draw;
	}

	private void playShootSound() {
		this.playSound(ModSounds.HUNTER_BOW_SHOOT, 1.5F, 0.9F + this.random.nextFloat() * 0.15F);
		this.playSound(ModSounds.HUNTER_BOW_WHISPER, 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
	}

	private void tickAttack(ServerLevel level, LivingEntity target) {
		int t = this.attackTicks;
		int phase = Math.max(1, this.getPhase());
		if (this.attack != Attack.LAST_HUNT && this.attack != Attack.VULNERABLE) {
			this.getLookControl().setLookAt(target, 40.0F, 40.0F);
			this.faceBodyToward(target.position());
		}
		switch (this.attack) {
			case SHOT -> {
				this.getNavigation().stop();
				int draw = this.drawTime();
				if (this.tickDrawCycle(t, draw)) {
					this.fireArrow(level, target, HollowArrow.Mode.HUNTER_SHOT, 0.0);
					this.setAnim(ANIM_RELEASE);
					if (--this.shotsLeft > 0) {
						this.attackTicks = -7; // brief release, then reach for the next arrow
					}
				}
				if (t >= draw + 10) {
					this.endAttack(phase == 1 ? 26 : phase == 2 ? 18 : 12);
				}
			}
			case HOLLOW -> {
				this.getNavigation().stop();
				int draw = this.drawTime() + 4;
				if (t > 3 && t < draw) {
					level.sendParticles(DUST, this.getX(), this.getY() + 2.4, this.getZ(), 1, 0.4, 0.2, 0.4, 0);
				}
				if (this.tickDrawCycle(t, draw)) {
					// first arrow tries to hit (and mark) the player, others plant anchors around them
					boolean first = this.shotsLeft == (phase == 1 ? 1 : phase == 2 ? 2 : 3);
					double offset = first && this.random.nextFloat() < 0.6F ? 0.0 : 2.0 + this.random.nextDouble() * 3.0;
					this.fireArrow(level, target, HollowArrow.Mode.HUNTER_ANCHOR, offset);
					this.setAnim(ANIM_RELEASE);
					if (--this.shotsLeft > 0) {
						this.attackTicks = -7;
					}
				}
				if (t >= draw + 10) {
					this.endAttack(phase == 1 ? 30 : 20);
				}
			}
			case VOLLEY -> {
				this.getNavigation().stop();
				if (t == 1) {
					this.summonVolley(level, target, phase);
				}
				if (t >= 40) {
					this.endAttack(phase == 1 ? 40 : 26);
				}
			}
			case RAIN -> {
				this.getNavigation().stop();
				if (this.tickDrawCycle(t, 16)) {
					this.playShootSound();
					this.setAnim(ANIM_RELEASE);
					this.summonRain(level, target, phase);
				}
				if (t >= 30) {
					this.endAttack(phase == 2 ? 40 : 30);
				}
			}
			case SLASH -> {
				this.getNavigation().stop();
				if (t == 6) {
					this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.4F, 0.6F);
					if (this.distanceTo(target) < MELEE_RANGE + 1.2) {
						this.doHurtTarget(level, target); // ATTACK_DAMAGE + ATTACK_KNOCKBACK attributes
					}
					level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + this.getLookAngle().x * 1.4, this.getY() + 1.6, this.getZ() + this.getLookAngle().z * 1.4, 1, 0, 0, 0, 0);
				}
				if (t >= 16) {
					this.slashCooldown = 50;
					this.endAttack(8);
					if (this.random.nextFloat() < 0.65F) {
						this.tacticalTeleport(level, target, false);
						this.teleportCooldown = Math.max(this.teleportCooldown, 60);
					}
				}
			}
			case LAST_HUNT -> this.tickLastHunt(level, target, t);
			case VULNERABLE -> {
				this.getNavigation().stop();
				if (t % 4 == 0) {
					level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 1.5, this.getZ(), 2, 0.4, 0.6, 0.4, 0.02);
				}
				if (t >= VULNERABLE_TICKS) {
					this.lastHuntCooldown = 520;
					this.endAttack(20);
					this.tacticalTeleport(level, target, false);
				}
			}
			case ROAR -> {
				this.getNavigation().stop();
				if (t == 2) {
					this.playSound(SoundEvents.WITHER_SKELETON_AMBIENT, 2.0F, 0.35F);
					this.playSound(SoundEvents.SOUL_ESCAPE.value(), 2.0F, 0.5F);
				}
				if (t >= 26) {
					this.endAttack(10);
					this.tacticalTeleport(level, target, false);
				}
			}
			case NONE -> {
			}
		}
	}

	// ----------------------------------------------------------------- attacks
	private void fireArrow(ServerLevel level, LivingEntity target, HollowArrow.Mode mode, double groundOffset) {
		HollowArrow arrow = new HollowArrow(level, this, new ItemStack(Items.ARROW), null, mode);
		arrow.setBaseDamage(ARROW_DAMAGE + (this.getPhase() >= 3 ? 0.4 : 0.0));
		if (mode == HollowArrow.Mode.HUNTER_ANCHOR) {
			arrow.setAnchorLife(this.getPhase() >= 2 ? 260 : HollowArrow.HUNTER_ANCHOR_LIFE);
		}

		Vec3 from = new Vec3(this.getX(), this.getEyeY() - 0.4, this.getZ());
		arrow.setPos(from);
		Vec3 aim;
		if (groundOffset > 0) {
			double a = this.random.nextDouble() * Math.PI * 2;
			aim = new Vec3(target.getX() + Math.cos(a) * groundOffset, target.getY() + 0.1, target.getZ() + Math.sin(a) * groundOffset);
		} else {
			double flight = from.distanceTo(target.position()) / ARROW_SPEED;
			// partial lead: good accuracy, but strafing still dodges
			aim = target.position().add(0, target.getBbHeight() * 0.55, 0).add(this.targetVelocity.scale(flight * 0.35));
		}
		Vec3 d = aim.subtract(from);
		double horiz = d.horizontalDistance();
		double ticks = horiz / ARROW_SPEED;
		double drop = 0.5 * 0.05 * ticks * ticks; // arrow gravity compensation
		float inaccuracy = switch (this.getPhase()) {
			case 1 -> 2.6F;
			case 2 -> 2.0F;
			default -> 1.6F;
		};
		arrow.shoot(d.x, d.y + drop, d.z, ARROW_SPEED, inaccuracy);
		level.addFreshEntity(arrow);
		this.playShootSound();
		if (mode == HollowArrow.Mode.HUNTER_ANCHOR) {
			this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 0.5F);
		}
	}

	private void summonVolley(ServerLevel level, LivingEntity target, int phase) {
		int count = phase == 1 ? 3 : phase == 2 ? 5 : 7;
		float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
		Vec3 forward = new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw));
		Vec3 right = new Vec3(-forward.z, 0, forward.x);
		boolean marked = target.hasEffect(ModEffects.HOLLOW_MARK);
		for (int i = 0; i < count; i++) {
			float s = count == 1 ? 0 : (i / (float) (count - 1)) * 2 - 1; // -1..1
			Vec3 pos = this.position()
				.add(forward.scale(-0.8))
				.add(right.scale(s * 2.2))
				.add(0, 3.1 + (1 - Math.abs(s)) * 0.9, 0);
			SpectralBolt bolt = SpectralBolt.create(level, this, pos, SpectralBolt.Kind.VOLLEY, target, 22 + i * 3)
				.spread(s * 14.0F)
				.speed(1.25 + phase * 0.08)
				.homing(marked ? 16 : 10, 2.2F)
				.damage(phase == 1 ? 5.0F : 6.0F);
			level.addFreshEntity(bolt);
		}
		this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.5F, 0.6F);
		level.sendParticles(DUST_BIG, this.getX(), this.getY() + 3.2, this.getZ(), 20, 1.4, 0.5, 1.4, 0);
	}

	private void summonRain(ServerLevel level, LivingEntity target, int phase) {
		int count = phase == 2 ? 6 : 9;
		List<Vec3> spots = new ArrayList<>();
		spots.add(target.position());
		for (int i = 1; i < count; i++) {
			double a = this.random.nextDouble() * Math.PI * 2;
			double r = 1.8 + this.random.nextDouble() * 4.0;
			spots.add(target.position().add(Math.cos(a) * r, 0, Math.sin(a) * r));
		}
		for (int i = 0; i < spots.size(); i++) {
			Vec3 ground = this.findGround(level, spots.get(i));
			if (ground == null) {
				continue;
			}
			Vec3 sky = ground.add(0, 11 + this.random.nextInt(3), 0);
			if (!level.noCollision(new AABB(sky, sky).inflate(0.3))) {
				sky = ground.add(0, 4.5, 0);
			}
			SpectralBolt bolt = SpectralBolt.create(level, this, sky, SpectralBolt.Kind.RAIN, null, 30 + i * 2)
				.groundTarget(ground)
				.speed(1.6)
				.damage(phase == 2 ? 6.0F : 7.0F);
			level.addFreshEntity(bolt);
		}
		this.playSound(SoundEvents.ILLUSIONER_CAST_SPELL, 1.5F, 0.6F);
	}

	private void tickLastHunt(ServerLevel level, LivingEntity target, int t) {
		this.getNavigation().stop();
		this.setDeltaMovement(Vec3.ZERO);
		int windup = 50;
		if (t == 1) {
			this.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 3.0F, 0.6F);
			this.playSound(SoundEvents.TRIAL_SPAWNER_OMINOUS_ACTIVATE, 2.0F, 0.6F);
			int ring = 18;
			double base = this.random.nextDouble() * Math.PI * 2;
			for (int i = 0; i < ring; i++) {
				double a = base + i * (Math.PI * 2 / ring);
				double r = 9.0 + (i % 2) * 1.5;
				Vec3 p = target.position().add(Math.cos(a) * r, 1.4 + (i % 3) * 1.4, Math.sin(a) * r);
				if (!level.noCollision(new AABB(p, p).inflate(0.3))) {
					p = p.add(0, 3, 0);
				}
				// three waves: 0, +7, +14 ticks after the warning ends
				int wave = i % 3;
				SpectralBolt bolt = SpectralBolt.create(level, this, p, SpectralBolt.Kind.LAST_HUNT, target, windup + wave * 7)
					.speed(1.15)
					.damage(6.0F);
				level.addFreshEntity(bolt);
			}
			for (int i = 0; i < 4; i++) { // a few straight above, so standing still is never safe
				Vec3 p = target.position().add((this.random.nextDouble() - 0.5) * 3, 8, (this.random.nextDouble() - 0.5) * 3);
				SpectralBolt bolt = SpectralBolt.create(level, this, p, SpectralBolt.Kind.LAST_HUNT, target, windup + 10 + i * 4)
					.speed(1.0)
					.damage(6.0F);
				level.addFreshEntity(bolt);
			}
		}
		if (t > 1 && t < windup && t % 10 == 0) {
			this.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 2.5F, 0.5F + t / 100.0F);
		}
		if (t == windup - 10) {
			this.playSound(SoundEvents.BELL_RESONATE, 2.5F, 0.6F);
		}
		if (t >= windup + 30) {
			this.startAttack(Attack.VULNERABLE);
			this.playSound(SoundEvents.WITHER_SKELETON_HURT, 1.6F, 0.5F);
		}
	}

	// ----------------------------------------------------------------- movement & teleport
	private void moveTactically(ServerLevel level, LivingEntity target) {
		double dist = this.distanceTo(target);
		Vec3 pos = this.position();

		// stuck detection: wanted to move but did not
		if (this.getNavigation().isInProgress() && pos.distanceToSqr(this.lastPos) < 0.0025) {
			if (++this.stuckTicks > 40) {
				this.stuckTicks = 0;
				this.tacticalTeleport(level, target, true);
				return;
			}
		} else {
			this.stuckTicks = 0;
		}
		this.lastPos = pos;

		if (dist < TOO_CLOSE) {
			Vec3 away = pos.subtract(target.position()).multiply(1, 0, 1).normalize().scale(7);
			Vec3 dest = pos.add(away);
			if (this.inArena(dest, ARENA_RADIUS)) {
				this.getNavigation().moveTo(dest.x, dest.y, dest.z, 1.25);
			} else if (this.teleportCooldown < 80) {
				this.tacticalTeleport(level, target, false);
				this.teleportCooldown = 100;
			}
		} else if (dist > PREFERRED_MAX || !this.hasLineOfSight(target)) {
			this.getNavigation().moveTo(target, 1.0);
		} else {
			// slow sideways stalk while keeping distance
			if (--this.strafeTicks <= 0) {
				this.strafeTicks = 40 + this.random.nextInt(40);
				this.strafeDir = this.random.nextBoolean() ? 1 : -1;
			}
			Vec3 toTarget = target.position().subtract(pos).multiply(1, 0, 1).normalize();
			Vec3 side = new Vec3(-toTarget.z, 0, toTarget.x).scale(this.strafeDir * 3.0);
			Vec3 dest = pos.add(side).add(toTarget.scale(dist < PREFERRED_MIN ? -2.0 : 0.0));
			if (this.inArena(dest, ARENA_RADIUS)) {
				this.getNavigation().moveTo(dest.x, dest.y, dest.z, 0.8);
			} else {
				this.strafeDir = -this.strafeDir;
			}
		}
	}

	/**
	 * Picks a destination: an embedded Hollow Arrow if a good one exists, otherwise a high/far spot in the arena.
	 * Never inside or right behind the player, always with line of sight to them.
	 */
	private void tacticalTeleport(ServerLevel level, LivingEntity target, boolean forced) {
		boolean marked = target.hasEffect(ModEffects.HOLLOW_MARK);
		double minDist = marked ? 5.5 : 7.0;
		double maxDist = marked ? 11.0 : 18.0;

		List<HollowArrow> anchors = level.getEntitiesOfClass(HollowArrow.class, this.arenaBox(), a -> a.isHunterAnchor() && a.getOwner() == this);
		anchors.sort(Comparator.comparingDouble(a -> -this.scoreSpot(level, target, this.standSpotNear(level, a.position()), minDist, maxDist)));
		for (HollowArrow anchor : anchors) {
			Vec3 spot = this.standSpotNear(level, anchor.position());
			if (spot != null && this.scoreSpot(level, target, spot, minDist, maxDist) > 0) {
				this.doTeleport(level, spot);
				anchor.fizzle();
				return;
			}
		}

		Vec3 best = null;
		double bestScore = 0;
		for (int i = 0; i < 24; i++) {
			double a = this.random.nextDouble() * Math.PI * 2;
			double r = minDist + this.random.nextDouble() * (maxDist - minDist);
			Vec3 probe = target.position().add(Math.cos(a) * r, 6, Math.sin(a) * r);
			Vec3 spot = this.findGround(level, probe);
			if (spot == null) {
				continue;
			}
			double score = this.scoreSpot(level, target, spot, minDist, maxDist);
			if (score > bestScore) {
				bestScore = score;
				best = spot;
			}
		}
		if (best != null) {
			this.doTeleport(level, best);
		} else if (forced) {
			BlockPos h = this.home();
			this.doTeleport(level, new Vec3(h.getX() + 0.5, h.getY(), h.getZ() + 0.5));
		}
	}

	private double scoreSpot(ServerLevel level, LivingEntity target, @Nullable Vec3 spot, double minDist, double maxDist) {
		if (spot == null || !this.inArena(spot, ARENA_RADIUS) || !this.canStandAt(level, spot)) {
			return -1;
		}
		double dist = spot.distanceTo(target.position());
		if (dist < minDist || dist > maxDist + 3) {
			return -1;
		}
		Vec3 eye = spot.add(0, this.getEyeHeight(), 0);
		if (level.clip(new ClipContext(eye, target.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() != HitResult.Type.MISS) {
			return -1;
		}
		// do not appear right behind the player's back at close range - that feels cheap
		Vec3 look = target.getLookAngle().multiply(1, 0, 1).normalize();
		Vec3 toSpot = spot.subtract(target.position()).multiply(1, 0, 1).normalize();
		if (look.dot(toSpot) < -0.45 && dist < 10) {
			return -1;
		}
		double score = 10.0;
		score += Math.max(0, spot.y - target.getY()) * 1.5;           // likes high ground
		score -= Math.abs(dist - (minDist + maxDist) / 2) * 0.4;         // likes medium range
		score -= spot.distanceTo(this.position()) < 4 ? 6 : 0;           // actually move
		score += this.random.nextDouble() * 2;
		return Math.max(0.01, score);
	}

	private boolean canStandAt(ServerLevel level, Vec3 spot) {
		AABB box = this.getDimensions(this.getPose()).makeBoundingBox(spot);
		return level.noCollision(this, box) && !level.containsAnyLiquid(box)
			&& !level.getBlockState(BlockPos.containing(spot.x, spot.y - 0.5, spot.z)).getCollisionShape(level, BlockPos.containing(spot.x, spot.y - 0.5, spot.z)).isEmpty();
	}

	/** Where to stand for an anchor arrow stuck in a floor or wall. */
	private @Nullable Vec3 standSpotNear(ServerLevel level, Vec3 arrowPos) {
		for (Vec3 offset : new Vec3[]{Vec3.ZERO, new Vec3(1, 0, 0), new Vec3(-1, 0, 0), new Vec3(0, 0, 1), new Vec3(0, 0, -1)}) {
			Vec3 ground = this.findGround(level, arrowPos.add(offset).add(0, 1.5, 0));
			if (ground != null && ground.distanceTo(arrowPos) < 4 && this.canStandAt(level, ground)) {
				return ground;
			}
		}
		return null;
	}

	/** First solid floor at or below {@code from} (searches 12 blocks down). */
	private @Nullable Vec3 findGround(ServerLevel level, Vec3 from) {
		BlockPos.MutableBlockPos p = BlockPos.containing(from).mutable();
		for (int i = 0; i < 12; i++) {
			BlockPos below = p.below();
			if (!level.getBlockState(below).getCollisionShape(level, below).isEmpty() && level.getBlockState(p).getCollisionShape(level, p).isEmpty()) {
				double top = level.getBlockState(below).getCollisionShape(level, below).max(net.minecraft.core.Direction.Axis.Y);
				return new Vec3(from.x, below.getY() + top, from.z);
			}
			p.move(0, -1, 0);
		}
		return null;
	}

	private void doTeleport(ServerLevel level, Vec3 dest) {
		Vec3 from = this.position();
		level.sendParticles(ParticleTypes.REVERSE_PORTAL, from.x, from.y + 1.6, from.z, 40, 0.4, 1.2, 0.4, 0.15);
		level.sendParticles(DUST_BIG, from.x, from.y + 1.6, from.z, 25, 0.5, 1.2, 0.5, 0);
		level.sendParticles(ParticleTypes.SOUL, from.x, from.y + 1.6, from.z, 8, 0.4, 1.0, 0.4, 0.03);
		level.playSound(null, from.x, from.y, from.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.2F, 0.55F);
		this.getNavigation().stop();
		this.teleportTo(dest.x, dest.y, dest.z);
		this.setDeltaMovement(Vec3.ZERO);
		level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, PURPLE), dest.x, dest.y + 1.6, dest.z, 1, 0, 0, 0, 0);
		level.sendParticles(ParticleTypes.PORTAL, dest.x, dest.y + 1.6, dest.z, 40, 0.4, 1.2, 0.4, 0.6);
		level.sendParticles(DUST_BIG, dest.x, dest.y + 1.6, dest.z, 25, 0.5, 1.2, 0.5, 0);
		level.playSound(null, dest.x, dest.y, dest.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.2F, 0.45F);
		LivingEntity target = this.getTarget();
		if (target != null) {
			this.snapRotation(this.yawToward(target.position()));
		}
	}

	// ----------------------------------------------------------------- targeting helpers
	private @Nullable Player pickTarget(ServerLevel level) {
		Player best = null;
		double bestScore = Double.MAX_VALUE;
		for (Player p : level.players()) {
			if (!this.isValidTarget(p) || !this.inArena(p.position(), ARENA_RADIUS + 10)) {
				continue;
			}
			double score = p.distanceToSqr(this);
			if (p.hasEffect(ModEffects.HOLLOW_MARK)) {
				score *= 0.1; // marked prey first
			}
			if (score < bestScore) {
				bestScore = score;
				best = p;
			}
		}
		return best;
	}

	private boolean isValidTarget(Player p) {
		return p.isAlive() && !p.isSpectator() && !p.isCreative();
	}

	private void trackTargetVelocity(LivingEntity target) {
		Vec3 now = target.position();
		if (this.lastTargetPos != Vec3.ZERO) {
			this.targetVelocity = this.targetVelocity.scale(0.6).add(now.subtract(this.lastTargetPos).scale(0.4));
		}
		this.lastTargetPos = now;
	}

	private boolean inArena(Vec3 pos, double radius) {
		BlockPos h = this.home();
		double dx = pos.x - (h.getX() + 0.5);
		double dz = pos.z - (h.getZ() + 0.5);
		return dx * dx + dz * dz <= radius * radius && Math.abs(pos.y - h.getY()) < 16;
	}

	private AABB arenaBox() {
		BlockPos h = this.home();
		return new AABB(h).inflate(ARENA_RADIUS + 4, 16, ARENA_RADIUS + 4);
	}

	private List<ServerPlayer> nearbyPlayers(ServerLevel level, double radius) {
		List<ServerPlayer> list = new ArrayList<>();
		for (ServerPlayer p : level.players()) {
			if (this.inArena(p.position(), radius)) {
				list.add(p);
			}
		}
		return list;
	}

	private void updateBossBarPlayers(ServerLevel level) {
		List<ServerPlayer> inRange = this.nearbyPlayers(level, ARENA_RADIUS + 16);
		for (ServerPlayer p : new ArrayList<>(this.bossEvent.getPlayers())) {
			if (!inRange.contains(p)) {
				this.bossEvent.removePlayer(p);
			}
		}
		for (ServerPlayer p : inRange) {
			this.bossEvent.addPlayer(p);
		}
	}

	private void clearArenaProjectiles(ServerLevel level) {
		for (HollowArrow a : level.getEntitiesOfClass(HollowArrow.class, this.arenaBox(), a -> a.getMode().fromHunter())) {
			a.discard();
		}
		for (SpectralBolt b : level.getEntitiesOfClass(SpectralBolt.class, this.arenaBox(), b -> true)) {
			b.discard();
		}
	}

	private float yawToward(Vec3 p) {
		return (float) (Mth.atan2(p.z - this.getZ(), p.x - this.getX()) * Mth.RAD_TO_DEG) - 90.0F;
	}

	private void faceBodyToward(Vec3 p) {
		float want = this.yawToward(p);
		this.setYRot(Mth.approachDegrees(this.getYRot(), want, 20.0F));
		this.yBodyRot = Mth.approachDegrees(this.yBodyRot, want, 20.0F);
	}

	private void snapRotation(float yaw) {
		this.setYRot(yaw);
		this.yBodyRot = yaw;
		this.yHeadRot = yaw;
		this.yBodyRotO = yaw;
		this.yHeadRotO = yaw;
	}

	// ================================================================= damage, death, persistence
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypes.GENERIC_KILL)) {
			return super.hurtServer(level, source, damage);
		}
		if (this.stage == Stage.SHOWCASE) {
			return super.hurtServer(level, source, damage);
		}
		if (this.stage != Stage.FIGHT) {
			// hidden/watching: cannot be cheesed before the fight starts
			if (source.getEntity() instanceof Player p && this.stage == Stage.DORMANT && !p.isCreative()) {
				this.startIntro(level, p);
			}
			return false;
		}
		if (source.getDirectEntity() instanceof HollowArrow a && a.getMode().fromHunter()) {
			return false;
		}
		if (this.attack == Attack.VULNERABLE) {
			damage *= VULNERABLE_DAMAGE_MULT;
		}
		return super.hurtServer(level, source, damage);
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
		return false;
	}

	@Override
	public boolean isPushable() {
		return !this.isHidden() && super.isPushable();
	}

	@Override
	public boolean isPickable() {
		return !this.isHidden() && super.isPickable();
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return false;
	}

	@Override
	protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
		// deferred until the body has crumbled - see tickDeath
		this.pendingDeathSource = source;
	}

	@Override
	protected void tickDeath() {
		this.deathTime++;
		if (!(this.level() instanceof ServerLevel level)) {
			return;
		}
		int t = this.deathTime;
		double cx = this.getX();
		double cy = this.getY();
		double cz = this.getZ();
		if (t == 1) {
			this.bossEvent.removeAllPlayers();
			this.bossEvent.setVisible(false);
			this.clearArenaProjectiles(level);
			this.playSound(SoundEvents.WITHER_SKELETON_DEATH, 2.0F, 0.5F);
		}
		// soul energy escapes, more and more
		level.sendParticles(ParticleTypes.SOUL, cx, cy + 1.6, cz, 1 + t / 20, 0.4, 0.9, 0.4, 0.03);
		if (t > 15 && t < 55 && t % 3 == 0) {
			level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), cx, cy + 1.2, cz, 8, 0.4, 0.8, 0.4, 0.1);
			level.playSound(null, cx, cy, cz, SoundEvents.SKELETON_STEP, SoundSource.HOSTILE, 0.8F, 0.5F);
		}
		if (t == 30) {
			this.playSound(SoundEvents.SOUL_ESCAPE.value(), 2.0F, 0.6F);
		}
		if (t == 62) {
			level.sendParticles(DUST_BIG, cx, cy + 0.6, cz, 60, 0.8, 0.4, 0.8, 0);
			level.sendParticles(ParticleTypes.SOUL, cx, cy + 0.5, cz, 50, 0.7, 0.3, 0.7, 0.12);
			this.playSound(SoundEvents.AMETHYST_CLUSTER_BREAK, 2.0F, 0.4F);
			DamageSource src = this.pendingDeathSource != null ? this.pendingDeathSource : this.damageSources().generic();
			super.dropAllDeathLoot(level, src);
		}
		if (t >= DEATH_TICKS && !this.isRemoved()) {
			level.broadcastEntityEvent(this, (byte) 60);
			this.remove(RemovalReason.KILLED);
		}
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		this.bossEvent.removePlayer(player);
	}

	@Override
	public void remove(RemovalReason reason) {
		this.bossEvent.removeAllPlayers();
		super.remove(reason);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return this.isHidden() ? null : SoundEvents.WITHER_SKELETON_AMBIENT;
	}

	@Override
	public float getVoicePitch() {
		return 0.5F + this.random.nextFloat() * 0.1F;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.SKELETON_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WITHER_SKELETON_DEATH;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		BlockPos h = this.home();
		output.putInt("HomeX", h.getX());
		output.putInt("HomeY", h.getY());
		output.putInt("HomeZ", h.getZ());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.home = new BlockPos(input.getIntOr("HomeX", this.getBlockX()), input.getIntOr("HomeY", this.getBlockY()), input.getIntOr("HomeZ", this.getBlockZ()));
		// a reload mid-fight always returns him to waiting in his grounds
		this.stage = Stage.DORMANT;
		this.setHealth(this.getMaxHealth());
	}

	// ================================================================= debug hooks for /underworld hunter ...
	public void debugForcePhase(ServerLevel level, int phase) {
		if (this.stage != Stage.FIGHT) {
			Player p = level.getNearestPlayer(this, 64);
			if (p != null) {
				this.introTarget = p;
				this.beginFight(level);
			}
		}
		float frac = phase == 1 ? 1.0F : phase == 2 ? 0.55F : 0.18F;
		this.setHealth(this.getMaxHealth() * frac);
	}

	/** Freezes him visible in a pose - for screenshots / checking animations: /underworld hunter showcase. */
	public void debugShowcase(int phase, int anim) {
		this.stage = Stage.SHOWCASE;
		this.attack = Attack.NONE;
		this.entityData.set(DATA_HIDDEN, false);
		this.entityData.set(DATA_EYE, true);
		this.entityData.set(DATA_PHASE, phase);
		this.setAnim(anim);
	}

	public void debugVolley(ServerLevel level, LivingEntity target) {
		this.summonVolley(level, target, Math.max(1, this.getPhase()));
	}

	public void debugStart(ServerLevel level, Player player) {
		this.startIntro(level, player);
	}

	public boolean isFighting() {
		return this.stage == Stage.FIGHT;
	}

	public boolean isDormant() {
		return this.stage == Stage.DORMANT;
	}

	public boolean isVulnerable() {
		return this.attack == Attack.VULNERABLE;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@SuppressWarnings("unused")
	private static boolean isEntityValid(@Nullable Entity e) {
		return e != null && e.isAlive();
	}
}
