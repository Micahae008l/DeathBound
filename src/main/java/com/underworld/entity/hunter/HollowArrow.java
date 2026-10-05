package com.underworld.entity.hunter;

import com.underworld.registry.ModEffects;
import com.underworld.registry.ModEntities;
import com.underworld.registry.ModItems;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * One arrow class for every "hollow" arrow in the mod. The {@link Mode} decides how it looks and behaves:
 * <ul>
 *     <li>HUNTER_SHOT   - the Hunter's normal fast arrow; purple trail, vanishes soon after landing.</li>
 *     <li>HUNTER_ANCHOR - a Hollow Arrow: glows, stays embedded for a few seconds, is a teleport anchor, marks players.</li>
 *     <li>PLAYER_SHOT   - a plain arrow fired from the Hollow Bow (Hollow Shot behaviour).</li>
 *     <li>PLAYER_HOLLOW - Hollow Arrow ammo fired by a player. From the Hollow Bow it is also a personal teleport anchor.</li>
 *     <li>DECOR         - permanent glowing arrow stuck in the Hunter's Grounds walls.</li>
 * </ul>
 */
public class HollowArrow extends AbstractArrow {
	public enum Mode {
		HUNTER_SHOT, HUNTER_ANCHOR, PLAYER_SHOT, PLAYER_HOLLOW, DECOR;

		public boolean glows() {
			return this != PLAYER_SHOT;
		}

		public boolean fromHunter() {
			return this == HUNTER_SHOT || this == HUNTER_ANCHOR;
		}

		static Mode byId(int id) {
			Mode[] values = values();
			return id >= 0 && id < values.length ? values[id] : PLAYER_HOLLOW;
		}
	}

	public static final int MARK_TICKS_FROM_HUNTER = 120;
	public static final int MARK_TICKS_FROM_BOW = 100;
	public static final int HUNTER_ANCHOR_LIFE = 220;

	private static final EntityDataAccessor<Byte> DATA_MODE = SynchedEntityData.defineId(HollowArrow.class, EntityDataSerializers.BYTE);
	private static final int PURPLE = 0xA855F7;
	private static final DustParticleOptions TRAIL = new DustParticleOptions(PURPLE, 0.7F);
	private static final DustParticleOptions TRAIL_FAINT = new DustParticleOptions(0x6B3FA0, 0.5F);

	private boolean fromHollowBow;
	private int groundTicks;
	private int anchorLife = HUNTER_ANCHOR_LIFE;

	public HollowArrow(EntityType<? extends HollowArrow> type, Level level) {
		super(type, level);
	}

	public HollowArrow(Level level, LivingEntity owner, ItemStack pickup, @Nullable ItemStack weapon, Mode mode) {
		super(ModEntities.HOLLOW_ARROW, owner, level, pickup, weapon);
		this.setMode(mode);
		if (mode.fromHunter()) {
			this.pickup = Pickup.DISALLOWED;
		}
	}

	public HollowArrow(Level level, double x, double y, double z, ItemStack pickup, Mode mode) {
		super(ModEntities.HOLLOW_ARROW, x, y, z, level, pickup, null);
		this.setMode(mode);
		if (mode == Mode.DECOR) {
			this.pickup = Pickup.DISALLOWED;
		}
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_MODE, (byte) Mode.PLAYER_HOLLOW.ordinal());
	}

	public Mode getMode() {
		return Mode.byId(this.entityData.get(DATA_MODE));
	}

	public void setMode(Mode mode) {
		this.entityData.set(DATA_MODE, (byte) mode.ordinal());
	}

	public void setFromHollowBow(boolean fromHollowBow) {
		this.fromHollowBow = fromHollowBow;
	}

	public boolean isFromHollowBow() {
		return this.fromHollowBow;
	}

	public void setAnchorLife(int ticks) {
		this.anchorLife = ticks;
	}

	public boolean isEmbedded() {
		return this.isInGround();
	}

	/** True for an embedded Hunter arrow he may teleport to. */
	public boolean isHunterAnchor() {
		return this.getMode() == Mode.HUNTER_ANCHOR && this.isInGround() && !this.isRemoved();
	}

	/** True for a Hollow Arrow a player fired from the Hollow Bow and may teleport to. */
	public boolean isPlayerAnchorFor(Player player) {
		return this.getMode() == Mode.PLAYER_HOLLOW && this.fromHollowBow && this.isInGround() && !this.isRemoved() && this.getOwner() == player;
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(ModItems.HOLLOW_ARROW);
	}

	@Override
	public void tick() {
		super.tick();
		Mode mode = this.getMode();
		Level level = this.level();
		if (level.isClientSide()) {
			if (!this.isInGround()) {
				Vec3 m = this.getDeltaMovement();
				int steps = mode == Mode.PLAYER_SHOT ? 1 : 3;
				for (int i = 0; i < steps; i++) {
					double f = i / (double) steps;
					level.addParticle(mode == Mode.PLAYER_SHOT ? TRAIL_FAINT : TRAIL, this.getX() - m.x * f, this.getY() - m.y * f, this.getZ() - m.z * f, 0, 0, 0);
				}
				if (mode.glows() && this.random.nextInt(3) == 0) {
					level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY(), this.getZ(), 0, 0.01, 0);
				}
			} else if (mode.glows() && this.random.nextInt(mode == Mode.HUNTER_ANCHOR ? 3 : 12) == 0) {
				level.addParticle(TRAIL, this.getRandomX(0.3), this.getY() + 0.1, this.getRandomZ(0.3), 0, 0.02, 0);
			}
			return;
		}

		if (this.isInGround()) {
			this.groundTicks++;
			if (mode == Mode.HUNTER_SHOT && this.groundTicks > 30) {
				this.discard();
			} else if (mode == Mode.HUNTER_ANCHOR && this.groundTicks > this.anchorLife) {
				this.fizzle();
			}
		} else if (mode.fromHunter() && this.tickCount > 200) {
			this.discard();
		}
	}

	@Override
	protected void tickDespawn() {
		if (this.getMode() != Mode.DECOR) {
			super.tickDespawn();
		}
	}

	/** Removes the arrow with a small soul puff (anchor expired or consumed by a teleport). */
	public void fizzle() {
		if (this.level() instanceof ServerLevel level) {
			level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 0.1, this.getZ(), 4, 0.1, 0.1, 0.1, 0.02);
			level.sendParticles(TRAIL, this.getX(), this.getY() + 0.1, this.getZ(), 6, 0.15, 0.15, 0.15, 0.0);
		}
		this.discard();
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		if (this.getMode().fromHunter() && (entity instanceof HollowHunter || entity instanceof SpectralBolt)) {
			return false;
		}
		return super.canHitEntity(entity);
	}

	@Override
	protected void doPostHurtEffects(LivingEntity target) {
		super.doPostHurtEffects(target);
		if (!(this.level() instanceof ServerLevel level)) {
			return;
		}

		if (this.getMode() == Mode.HUNTER_ANCHOR) {
			target.addEffect(new MobEffectInstance(ModEffects.HOLLOW_MARK, MARK_TICKS_FROM_HUNTER, 0), this.getEffectSource());
			level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 1.0F, 0.6F);
			return;
		}

		if (this.fromHollowBow && target.isAlive()) {
			if (target.hasEffect(ModEffects.HOLLOW_MARK)) {
				target.removeEffect(ModEffects.HOLLOW_MARK);
				this.soulBurst(level, target);
			} else {
				target.addEffect(new MobEffectInstance(ModEffects.HOLLOW_MARK, MARK_TICKS_FROM_BOW, 0), this.getEffectSource());
				level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8F, 1.4F);
			}
		}
	}

	/** Hollow Shot payoff: second hit on a marked enemy releases a burst of soul energy. */
	private void soulBurst(ServerLevel level, LivingEntity target) {
		Entity owner = this.getOwner();
		Vec3 c = target.position().add(0, target.getBbHeight() * 0.5, 0);
		target.setInvulnerableTime(0);
		target.hurtServer(level, this.damageSources().indirectMagic(this, owner), 6.0F);
		AABB area = target.getBoundingBox().inflate(3.0);
		for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != target && e != owner && e.isAlive())) {
			if (other instanceof Player p && (p.isCreative() || p.isSpectator() || owner instanceof Player)) {
				continue;
			}
			other.hurtServer(level, this.damageSources().indirectMagic(this, owner), 3.0F);
		}
		level.sendParticles(ParticleTypes.SOUL, c.x, c.y, c.z, 18, 0.5, 0.6, 0.5, 0.06);
		level.sendParticles(TRAIL, c.x, c.y, c.z, 30, 0.9, 0.9, 0.9, 0.0);
		level.sendParticles(ParticleTypes.REVERSE_PORTAL, c.x, c.y, c.z, 20, 0.3, 0.3, 0.3, 0.3);
		level.playSound(null, c.x, c.y, c.z, SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.6F, 0.8F);
		level.playSound(null, c.x, c.y, c.z, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.0F, 0.6F);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putByte("HollowMode", this.entityData.get(DATA_MODE));
		output.putBoolean("FromHollowBow", this.fromHollowBow);
		output.putInt("AnchorLife", this.anchorLife);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.entityData.set(DATA_MODE, input.getByteOr("HollowMode", (byte) Mode.PLAYER_HOLLOW.ordinal()));
		this.fromHollowBow = input.getBooleanOr("FromHollowBow", false);
		this.anchorLife = input.getIntOr("AnchorLife", HUNTER_ANCHOR_LIFE);
		if (this.getMode().fromHunter()) {
			this.discard(); // fight-only arrows never survive a reload
		}
	}
}
