package com.underworld.entity.hunter;

import com.underworld.registry.ModEffects;
import com.underworld.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A summoned spectral arrow. It first HOVERS in place (the telegraph), pointing at what it is about to hit,
 * then launches. Used by the Spectral Volley, Arrow Rain and The Last Hunt.
 */
public class SpectralBolt extends Projectile {
	public enum Kind {
		/** Fired from the formation behind the Hunter, mild homing, slightly spread. */
		VOLLEY,
		/** Falls straight down onto a telegraphed ground spot, small splash. */
		RAIN,
		/** Ring around the arena; fires straight at where the player is when it launches. */
		LAST_HUNT
	}

	private static final EntityDataAccessor<Boolean> DATA_LAUNCHED = SynchedEntityData.defineId(SpectralBolt.class, EntityDataSerializers.BOOLEAN);
	private static final DustParticleOptions DUST = new DustParticleOptions(0xB06CFF, 0.9F);
	private static final DustParticleOptions WARN = new DustParticleOptions(0xC77DFF, 1.1F);

	private Kind kind = Kind.VOLLEY;
	private @Nullable LivingEntity target;
	private int hoverTicks;
	private float spreadYaw;
	private double speed = 1.3;
	private int homingTicks;
	private float homingDegreesPerTick;
	private float damage = 5.0F;
	private Vec3 groundTarget = Vec3.ZERO;
	private int flightTicks;

	public SpectralBolt(EntityType<? extends SpectralBolt> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
		this.noPhysics = true;
	}

	public static SpectralBolt create(Level level, HollowHunter owner, Vec3 pos, Kind kind, @Nullable LivingEntity target, int hoverTicks) {
		SpectralBolt bolt = new SpectralBolt(ModEntities.SPECTRAL_BOLT, level);
		bolt.setOwner(owner);
		bolt.setPos(pos);
		bolt.kind = kind;
		bolt.target = target;
		bolt.hoverTicks = hoverTicks;
		bolt.aimAt(kind == Kind.RAIN ? pos.add(0, -1, 0) : bolt.aimPoint());
		bolt.xRotO = bolt.getXRot();
		bolt.yRotO = bolt.getYRot();
		return bolt;
	}

	public SpectralBolt spread(float yawDegrees) {
		this.spreadYaw = yawDegrees;
		return this;
	}

	public SpectralBolt speed(double speed) {
		this.speed = speed;
		return this;
	}

	public SpectralBolt homing(int ticks, float degreesPerTick) {
		this.homingTicks = ticks;
		this.homingDegreesPerTick = degreesPerTick;
		return this;
	}

	public SpectralBolt damage(float damage) {
		this.damage = damage;
		return this;
	}

	public SpectralBolt groundTarget(Vec3 groundTarget) {
		this.groundTarget = groundTarget;
		return this;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_LAUNCHED, false);
	}

	public boolean isLaunched() {
		return this.entityData.get(DATA_LAUNCHED);
	}

	private Vec3 aimPoint() {
		if (this.target == null) {
			return this.position().add(this.getLookAngle());
		}
		return this.target.position().add(0, this.target.getBbHeight() * 0.55, 0);
	}

	private void aimAt(Vec3 point) {
		Vec3 d = point.subtract(this.position());
		double h = d.horizontalDistance();
		// same convention as ProjectileUtil.rotateTowardsMovement, so nothing flips on launch
		this.setYRot((float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) + 90.0F);
		this.setXRot((float) (Mth.atan2(h, d.y) * Mth.RAD_TO_DEG) - 90.0F);
	}

	@Override
	public void tick() {
		super.tick();
		Level level = this.level();

		if (level.isClientSide()) {
			if (this.isLaunched()) {
				Vec3 m = this.getDeltaMovement();
				for (int i = 0; i < 3; i++) {
					double f = i / 3.0;
					level.addParticle(DUST, this.getX() - m.x * f, this.getY() - m.y * f, this.getZ() - m.z * f, 0, 0, 0);
				}
			} else if (this.random.nextInt(3) == 0) {
				level.addParticle(DUST, this.getRandomX(0.6), this.getRandomY(), this.getRandomZ(0.6), 0, 0.01, 0);
			}
			return;
		}

		ServerLevel server = (ServerLevel) level;
		if (this.getOwner() == null || this.tickCount > 260 || (this.target != null && !this.target.isAlive() && !this.isLaunched())) {
			this.vanish(server);
			return;
		}

		if (!this.isLaunched()) {
			this.setDeltaMovement(Vec3.ZERO);
			if (this.kind == Kind.RAIN) {
				this.aimAt(this.position().add(0, -1, 0));
				if (this.hoverTicks % 2 == 0) {
					this.drawWarningRing(server);
				}
			} else {
				this.aimAt(this.aimPoint()); // head always points at the target while hovering
			}
			if (--this.hoverTicks <= 0) {
				this.launch(server);
			}
			return;
		}

		this.flightTicks++;
		if (this.homingTicks > 0 && this.target != null && this.target.isAlive()) {
			this.homingTicks--;
			float rate = this.homingDegreesPerTick * (this.target.hasEffect(ModEffects.HOLLOW_MARK) ? 1.8F : 1.0F);
			this.steerToward(this.aimPoint(), rate);
		}

		HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
		Vec3 movement = this.getDeltaMovement();
		this.setPos(this.position().add(movement));
		ProjectileUtil.rotateTowardsMovement(this, 1.0F);
		if (hit.getType() != HitResult.Type.MISS && this.isAlive()) {
			this.hitTargetOrDeflectSelf(hit);
		}
	}

	private void steerToward(Vec3 point, float maxDegrees) {
		Vec3 v = this.getDeltaMovement();
		double len = v.length();
		if (len < 1.0E-4) {
			return;
		}
		Vec3 want = point.subtract(this.position()).normalize();
		Vec3 cur = v.scale(1.0 / len);
		double cos = Mth.clamp(cur.dot(want), -1.0, 1.0);
		double angle = Math.acos(cos);
		double max = maxDegrees * Mth.DEG_TO_RAD;
		Vec3 next = angle <= max ? want : cur.add(want.subtract(cur).scale(max / angle)).normalize();
		this.setDeltaMovement(next.scale(len));
	}

	private void launch(ServerLevel level) {
		this.entityData.set(DATA_LAUNCHED, true);
		Vec3 dir;
		if (this.kind == Kind.RAIN) {
			dir = new Vec3(0, -1, 0);
		} else {
			// spread is applied only to the flight path, so every bolt can still look straight at you
			dir = this.aimPoint().subtract(this.position()).normalize().yRot(this.spreadYaw * Mth.DEG_TO_RAD);
		}
		this.setDeltaMovement(dir.scale(this.speed));
		float pitch = this.kind == Kind.LAST_HUNT ? 0.7F + this.random.nextFloat() * 0.2F : 1.3F + this.random.nextFloat() * 0.3F;
		level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE, 0.6F, pitch);
	}

	private void drawWarningRing(ServerLevel level) {
		Vec3 g = this.groundTarget;
		for (int i = 0; i < 10; i++) {
			double a = (i / 10.0) * Math.PI * 2 + this.tickCount * 0.15;
			level.sendParticles(WARN, g.x + Math.cos(a) * 1.1, g.y + 0.1, g.z + Math.sin(a) * 1.1, 1, 0, 0, 0, 0);
		}
		level.sendParticles(ParticleTypes.WITCH, g.x, g.y + 0.15, g.z, 1, 0.3, 0.0, 0.3, 0.0);
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		if (entity instanceof HollowHunter || entity instanceof SpectralBolt || entity instanceof HollowArrow) {
			return false;
		}
		if (entity instanceof Player p && (p.isSpectator() || p.isCreative())) {
			return false;
		}
		return super.canHitEntity(entity) && entity instanceof LivingEntity;
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		if (!(this.level() instanceof ServerLevel level)) {
			return;
		}
		Entity hit = hitResult.getEntity();
		Entity owner = this.getOwner();
		DamageSource source = this.damageSources().mobProjectile(this, owner instanceof LivingEntity l ? l : null);
		if (this.kind == Kind.RAIN) {
			this.splash(level);
		} else if (hit.hurtServer(level, source, this.damage)) {
			level.playSound(null, hit.getX(), hit.getY(), hit.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.HOSTILE, 0.6F, 0.8F);
		}
		this.vanish(level);
	}

	@Override
	protected void onHitBlock(BlockHitResult hitResult) {
		super.onHitBlock(hitResult);
		if (this.level() instanceof ServerLevel level) {
			if (this.kind == Kind.RAIN) {
				this.splash(level);
			}
			level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.HOSTILE, 0.7F, 0.6F);
			this.vanish(level);
		}
	}

	/** Arrow Rain impact: hurts whoever is standing in the telegraphed circle. */
	private void splash(ServerLevel level) {
		Entity owner = this.getOwner();
		DamageSource source = this.damageSources().mobProjectile(this, owner instanceof LivingEntity l ? l : null);
		Vec3 c = this.position();
		for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1.2, 1.6, 1.2), this::canHitEntity)) {
			if (e.position().subtract(c).horizontalDistanceSqr() <= 1.44 + 0.3) {
				e.hurtServer(level, source, this.damage);
			}
		}
		level.sendParticles(ParticleTypes.SOUL, c.x, c.y + 0.2, c.z, 6, 0.4, 0.1, 0.4, 0.03);
		level.sendParticles(WARN, c.x, c.y + 0.2, c.z, 12, 0.6, 0.15, 0.6, 0.0);
	}

	private void vanish(ServerLevel level) {
		level.sendParticles(DUST, this.getX(), this.getY(), this.getZ(), 5, 0.15, 0.15, 0.15, 0.0);
		this.discard();
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		return distance < 16384.0;
	}

	@Override
	public boolean isOnFire() {
		return false;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.discard(); // bolts only make sense mid-fight
	}
}
