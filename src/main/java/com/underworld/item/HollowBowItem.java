package com.underworld.item;

import com.underworld.entity.hunter.HollowArrow;
import com.underworld.registry.ModItems;
import com.underworld.registry.ModSounds;
import java.util.Comparator;
import java.util.List;
import net.minecraft.stats.Stats;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Hunter's bow.
 * <ul>
 *     <li>Plain arrows and Hollow Arrows become Hollow Shots: first hit marks, second hit on a marked enemy = soul burst.</li>
 *     <li>Hollow Arrows fired from it stay embedded and glowing; sneak + use teleports you to the newest one.</li>
 *     <li>Other arrows (tipped, spectral) fire normally.</li>
 * </ul>
 */
public class HollowBowItem extends BowItem {
	public static final int TELEPORT_RANGE = 40;
	public static final int TELEPORT_COOLDOWN = 80;
	private static final DustParticleOptions DUST = new DustParticleOptions(0xA855F7, 1.2F);

	public HollowBowItem(Properties properties) {
		super(properties);
	}

	@Override
	protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack projectile, boolean isCrit) {
		boolean hollowAmmo = projectile.is(ModItems.HOLLOW_ARROW);
		if (!hollowAmmo && !projectile.is(Items.ARROW)) {
			return super.createProjectile(level, shooter, weapon, projectile, isCrit);
		}
		HollowArrow arrow = new HollowArrow(level, shooter, projectile.copyWithCount(1), weapon,
			hollowAmmo ? HollowArrow.Mode.PLAYER_HOLLOW : HollowArrow.Mode.PLAYER_SHOT);
		arrow.setFromHollowBow(true);
		arrow.setCritArrow(isCrit);
		return arrow;
	}

	/** Same as the vanilla bow, but with the Hollow Bow's own release sound. */
	@Override
	public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int remainingTime) {
		if (!(entity instanceof Player player)) {
			return false;
		}
		ItemStack projectile = player.getProjectile(itemStack);
		if (projectile.isEmpty()) {
			return false;
		}
		float pow = getPowerForTime(this.getUseDuration(itemStack, entity) - remainingTime);
		if (pow < 0.1F) {
			return false;
		}
		List<ItemStack> fired = draw(itemStack, projectile, player);
		if (level instanceof ServerLevel serverLevel && !fired.isEmpty()) {
			this.shoot(serverLevel, player, player.getUsedItemHand(), itemStack, fired, pow * 3.0F, 1.0F, pow == 1.0F, null);
		}
		level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.HOLLOW_BOW_SHOOT, SoundSource.PLAYERS,
			1.0F, 0.85F + pow * 0.25F + level.getRandom().nextFloat() * 0.1F);
		player.awardStat(Stats.ITEM_USED.get(this));
		return true;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player.isShiftKeyDown()) {
			if (level instanceof ServerLevel server) {
				Optional<HollowArrow> anchor = server.getEntitiesOfClass(
						HollowArrow.class, new AABB(player.blockPosition()).inflate(TELEPORT_RANGE), a -> a.isPlayerAnchorFor(player))
					.stream()
					.min(Comparator.comparingInt(a -> a.tickCount));
				if (anchor.isEmpty()) {
					return InteractionResult.FAIL;
				}
				HollowArrow arrow = anchor.get();
				Vec3 dest = safeSpot(server, player, arrow.position());
				if (dest == null) {
					return InteractionResult.FAIL;
				}
				Vec3 from = player.position();
				server.sendParticles(ParticleTypes.REVERSE_PORTAL, from.x, from.y + 1, from.z, 24, 0.3, 0.6, 0.3, 0.1);
				server.sendParticles(DUST, from.x, from.y + 1, from.z, 12, 0.3, 0.6, 0.3, 0);
				player.teleportTo(dest.x, dest.y, dest.z);
				player.resetFallDistance();
				server.sendParticles(ParticleTypes.PORTAL, dest.x, dest.y + 1, dest.z, 24, 0.3, 0.6, 0.3, 0.4);
				server.sendParticles(DUST, dest.x, dest.y + 1, dest.z, 12, 0.3, 0.6, 0.3, 0);
				server.playSound(null, dest.x, dest.y, dest.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 0.7F);
				arrow.fizzle();
				player.getCooldowns().addCooldown(stack, TELEPORT_COOLDOWN);
			}
			return InteractionResult.SUCCESS;
		}
		return super.use(level, player, hand);
	}

	private static Vec3 safeSpot(ServerLevel level, Player player, Vec3 near) {
		for (int dy = 0; dy <= 3; dy++) {
			for (int[] o : new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
				BlockPos p = BlockPos.containing(near.x + o[0], near.y + dy, near.z + o[1]);
				Vec3 spot = new Vec3(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
				AABB box = player.getDimensions(player.getPose()).makeBoundingBox(spot);
				if (level.noCollision(player, box) && !level.getBlockState(p.below()).getCollisionShape(level, p.below()).isEmpty()) {
					return spot;
				}
			}
		}
		return null;
	}
}
