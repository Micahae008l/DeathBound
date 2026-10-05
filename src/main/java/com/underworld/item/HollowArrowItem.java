package com.underworld.item;

import com.underworld.entity.hunter.HollowArrow;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class HollowArrowItem extends ArrowItem {
	public HollowArrowItem(Properties properties) {
		super(properties);
	}

	@Override
	public AbstractArrow createArrow(Level level, ItemStack itemStack, LivingEntity owner, @Nullable ItemStack firedFromWeapon) {
		return new HollowArrow(level, owner, itemStack.copyWithCount(1), firedFromWeapon, HollowArrow.Mode.PLAYER_HOLLOW);
	}

	@Override
	public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
		HollowArrow arrow = new HollowArrow(level, position.x(), position.y(), position.z(), itemStack.copyWithCount(1), HollowArrow.Mode.PLAYER_HOLLOW);
		arrow.pickup = AbstractArrow.Pickup.ALLOWED;
		return arrow;
	}
}
