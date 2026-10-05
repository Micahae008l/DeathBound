package com.underworld.world;

import com.underworld.entity.collector.Collector;
import com.underworld.entity.hunter.HollowArrow;
import com.underworld.entity.hunter.HollowHunter;
import com.underworld.block.CuriosityBlock;
import com.underworld.registry.ModBlocks;
import com.underworld.registry.ModEntities;
import com.underworld.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Builds the two test locations in code so they can be dropped anywhere (e.g. a void world).
 * When merging into the real mod these can be replaced with saved structure templates / jigsaw pieces;
 * the entities only need {@code setHome(...)}.
 */
public final class StructureBuilder {
	private static final int FLAGS = Block.UPDATE_CLIENTS;

	private StructureBuilder() {
	}

	// =============================================================================== THE HUNTER'S GROUNDS
	/**
	 * @param center floor-level centre of the arena (players stand at center.getY()).
	 * @return the dormant Hunter.
	 */
	public static HollowHunter buildHuntersGrounds(ServerLevel level, BlockPos center) {
		RandomSource rnd = RandomSource.create(center.asLong());
		int r = HollowHunter.ARENA_RADIUS + 2;
		int y0 = center.getY();

		// clear the volume, then lay a ruined, slightly uneven floor (3 thick, crumbling at the rim)
		for (int dx = -r - 2; dx <= r + 2; dx++) {
			for (int dz = -r - 2; dz <= r + 2; dz++) {
				for (int dy = -3; dy <= 14; dy++) {
					set(level, center.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
				}
				double d = Math.sqrt(dx * dx + dz * dz);
				if (d > r + 0.5 || (d > r - 2.5 && rnd.nextFloat() < (d - (r - 2.5)) / 3.0)) {
					continue;
				}
				for (int dy = -3; dy <= -1; dy++) {
					set(level, center.offset(dx, dy, dz), dy == -1 ? floorBlock(rnd) : Blocks.COBBLED_DEEPSLATE.defaultBlockState());
				}
				if (rnd.nextFloat() < 0.03F) {
					set(level, center.offset(dx, -1, dz), Blocks.SOUL_SOIL.defaultBlockState());
					if (rnd.nextBoolean()) {
						set(level, center.offset(dx, 0, dz), Blocks.SOUL_FIRE.defaultBlockState());
					}
				}
			}
		}

		// the Hunter's dais at the far (north) end
		BlockPos dais = center.offset(0, 0, -14);
		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				set(level, dais.offset(dx, 0, dz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
				set(level, dais.offset(dx, 1, dz), (Math.abs(dx) == 3 || Math.abs(dz) == 3) ? Blocks.POLISHED_DEEPSLATE.defaultBlockState()
					: rnd.nextFloat() < 0.3F ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
			}
		}
		for (int dx = -2; dx <= 2; dx++) {
			set(level, dais.offset(dx, 0, 4), Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
			set(level, dais.offset(dx, 1, 4), Blocks.AIR.defaultBlockState());
		}
		set(level, dais.offset(-3, 2, -3), Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
		set(level, dais.offset(3, 2, -3), Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));

		// broken pillars
		for (int i = 0; i < 12; i++) {
			double a = i * Math.PI * 2 / 12 + 0.13;
			int px = (int) Math.round(Math.cos(a) * 17);
			int pz = (int) Math.round(Math.sin(a) * 17);
			if (pz > 15) {
				continue; // keep the southern entrance open
			}
			pillar(level, rnd, center.offset(px, 0, pz), 3 + rnd.nextInt(8));
		}

		// raised ruins the Hunter likes to teleport onto
		ruinedPlatform(level, rnd, center.offset(13, 0, -3), 4);
		ruinedPlatform(level, rnd, center.offset(-13, 0, -5), 5);
		ruinedPlatform(level, rnd, center.offset(-8, 0, 10), 3);

		// old practice targets
		int[][] targets = {{6, -8}, {-6, -9}, {9, 6}, {-10, 3}, {3, 9}};
		for (int[] t : targets) {
			BlockPos base = center.offset(t[0], 0, t[1]);
			set(level, base, Blocks.DARK_OAK_FENCE.defaultBlockState());
			set(level, base.above(), Blocks.TARGET.defaultBlockState());
		}

		// skeleton remains, abandoned bows
		for (int i = 0; i < 14; i++) {
			BlockPos p = randomFloor(center, rnd, 18);
			if (!level.getBlockState(p).isAir() || level.getBlockState(p.below()).isAir()) {
				continue;
			}
			switch (rnd.nextInt(4)) {
				case 0 -> set(level, p, Blocks.BONE_BLOCK.defaultBlockState());
				case 1 -> set(level, p, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, rnd.nextInt(16)));
				case 2 -> set(level, p, Blocks.COBWEB.defaultBlockState());
				default -> lyingItem(level, Vec3.atBottomCenterOf(p), new ItemStack(Items.BOW), rnd.nextFloat() * 360F);
			}
		}
		for (int i = 0; i < 4; i++) {
			BlockPos p = randomFloor(center, rnd, 16);
			if (level.getBlockState(p).isAir() && !level.getBlockState(p.below()).isAir()) {
				lyingItem(level, Vec3.atBottomCenterOf(p), new ItemStack(Items.BOW), rnd.nextFloat() * 360F);
				lyingItem(level, Vec3.atBottomCenterOf(p).add(0.3, 0.01, 0.2), new ItemStack(Items.BONE), rnd.nextFloat() * 360F);
			}
		}

		// broken entrance arch on the south side
		BlockPos arch = center.offset(0, 0, r - 1);
		for (int dy = 0; dy < 6; dy++) {
			set(level, arch.offset(-3, dy, 0), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
			if (dy < 4) {
				set(level, arch.offset(3, dy, 0), Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState());
			}
		}
		for (int dx = -3; dx <= 0; dx++) {
			set(level, arch.offset(dx, 6, 0), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
		}
		set(level, arch.offset(-2, 5, 0), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));

		// arrows stuck everywhere - shot into pillars and targets, they embed on the first tick
		for (int[] t : targets) {
			stuckArrow(level, Vec3.atCenterOf(center.offset(t[0], 1, t[1])), rnd);
		}
		for (int i = 0; i < 18; i++) {
			BlockPos p = randomFloor(center, rnd, 19);
			stuckArrow(level, Vec3.atBottomCenterOf(p).add(0, 0.1, 0), rnd);
		}

		// the Hunter himself, waiting unseen on his dais
		BlockPos home = dais.above(2);
		for (HollowHunter old : level.getEntitiesOfClass(HollowHunter.class, new net.minecraft.world.phys.AABB(center).inflate(40))) {
			old.discard();
		}
		HollowHunter hunter = ModEntities.HOLLOW_HUNTER.create(level, EntitySpawnReason.STRUCTURE);
		hunter.setHome(home);
		hunter.snapTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0, 0);
		level.addFreshEntity(hunter);
		return hunter;
	}

	private static BlockState floorBlock(RandomSource rnd) {
		float f = rnd.nextFloat();
		if (f < 0.35F) return Blocks.DEEPSLATE_TILES.defaultBlockState();
		if (f < 0.55F) return Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState();
		if (f < 0.72F) return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
		if (f < 0.84F) return Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
		if (f < 0.93F) return Blocks.COBBLED_DEEPSLATE.defaultBlockState();
		return Blocks.BLACKSTONE.defaultBlockState();
	}

	private static void pillar(ServerLevel level, RandomSource rnd, BlockPos base, int height) {
		for (int dy = 0; dy < height; dy++) {
			BlockState s = dy == height - 1 && height > 5 ? Blocks.CHISELED_DEEPSLATE.defaultBlockState()
				: rnd.nextFloat() < 0.25F ? Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState();
			set(level, base.above(dy), s);
			if (dy < 2 && height > 4) {
				for (Direction d : Direction.Plane.HORIZONTAL) {
					if (rnd.nextFloat() < 0.6F) {
						set(level, base.relative(d).above(dy), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
					}
				}
			}
		}
		if (height > 6 && rnd.nextBoolean()) {
			set(level, base.above(height), Blocks.DYED_CANDLE.pick(DyeColor.PURPLE).defaultBlockState()
				.setValue(CandleBlock.CANDLES, 1 + rnd.nextInt(3)).setValue(CandleBlock.LIT, true));
		} else if (rnd.nextFloat() < 0.4F) {
			Direction d = Direction.Plane.HORIZONTAL.getRandomDirection(rnd);
			set(level, base.relative(d).above(Math.min(height - 1, 3)), Blocks.SOUL_WALL_TORCH.defaultBlockState()
				.setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, d));
		}
		// rubble at the foot
		for (int i = 0; i < 3; i++) {
			BlockPos p = base.offset(rnd.nextInt(5) - 2, 0, rnd.nextInt(5) - 2);
			if (level.getBlockState(p).isAir()) {
				set(level, p, Blocks.COBBLED_DEEPSLATE_SLAB.defaultBlockState());
			}
		}
	}

	private static void ruinedPlatform(ServerLevel level, RandomSource rnd, BlockPos base, int height) {
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				boolean corner = Math.abs(dx) == 2 && Math.abs(dz) == 2;
				if (corner) {
					for (int dy = 0; dy < height; dy++) {
						set(level, base.offset(dx, dy, dz), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
					}
				}
				if (rnd.nextFloat() < 0.88F || corner) {
					set(level, base.offset(dx, height, dz), rnd.nextFloat() < 0.3F ? Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState());
				}
			}
		}
		// broken stair up one side so players can follow
		for (int i = 0; i < height; i++) {
			set(level, base.offset(3 + i, i, 0), Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
		}
		set(level, base.offset(-2, height + 1, -2), Blocks.SOUL_LANTERN.defaultBlockState());
	}

	private static BlockPos randomFloor(BlockPos center, RandomSource rnd, int radius) {
		double a = rnd.nextDouble() * Math.PI * 2;
		double d = 3 + rnd.nextDouble() * (radius - 3);
		return center.offset((int) Math.round(Math.cos(a) * d), 0, (int) Math.round(Math.sin(a) * d));
	}

	private static void stuckArrow(ServerLevel level, Vec3 near, RandomSource rnd) {
		double a = rnd.nextDouble() * Math.PI * 2;
		Vec3 from = near.add(Math.cos(a) * 1.4, 0.6 + rnd.nextDouble() * 0.6, Math.sin(a) * 1.4);
		HollowArrow arrow = new HollowArrow(level, from.x, from.y, from.z, new ItemStack(Items.ARROW), HollowArrow.Mode.DECOR);
		Vec3 d = near.subtract(from);
		arrow.shoot(d.x, d.y, d.z, 1.2F, 0.0F);
		level.addFreshEntity(arrow);
	}

	private static void lyingItem(ServerLevel level, Vec3 pos, ItemStack stack, float yaw) {
		Display.ItemDisplay display = new Display.ItemDisplay(EntityTypes.ITEM_DISPLAY, level);
		display.setItemStack(stack);
		display.setItemTransform(ItemDisplayContext.FIXED);
		Quaternionf flat = new Quaternionf().rotateY((float) Math.toRadians(yaw)).rotateX((float) Math.toRadians(-90));
		display.setTransformation(new Transformation(new Vector3f(0, 0.03F, 0), flat, new Vector3f(0.7F, 0.7F, 0.7F), null));
		display.setPos(pos);
		level.addFreshEntity(display);
	}

	private static void standingItem(ServerLevel level, Vec3 pos, ItemStack stack, float yaw, float scale) {
		Display.ItemDisplay display = new Display.ItemDisplay(EntityTypes.ITEM_DISPLAY, level);
		display.setItemStack(stack);
		display.setItemTransform(ItemDisplayContext.FIXED);
		Quaternionf rot = new Quaternionf().rotateY((float) Math.toRadians(yaw));
		display.setTransformation(new Transformation(new Vector3f(0, scale * 0.5F, 0), rot, new Vector3f(scale, scale, scale), null));
		display.setPos(pos);
		level.addFreshEntity(display);
	}

	// =============================================================================== THE COLLECTION ROOM
	/**
	 * A small, cramped hidden room (9x7 inside) crammed with jars and bottles, entered through a narrow dark
	 * passage on its south side.
	 * @param origin floor-level centre of the room.
	 */
	public static Collector buildCollectionRoom(ServerLevel level, BlockPos origin) {
		RandomSource rnd = RandomSource.create(origin.asLong() ^ 0x5EEDL);
		int hx = 4;  // interior half width (x)
		int hz = 3;  // interior half depth (z)
		int h = 5;   // interior height
		int passage = 6;

		// shell + secret passage
		for (int dx = -hx - 2; dx <= hx + 2; dx++) {
			for (int dz = -hz - 2; dz <= hz + 2 + passage; dz++) {
				for (int dy = -1; dy <= h + 1; dy++) {
					BlockPos p = origin.offset(dx, dy, dz);
					if (dz > hz + 1) {
						boolean inside = dx == 0 && dy >= 0 && dy <= 1;
						boolean shell = Math.abs(dx) <= 1 && dy >= -1 && dy <= 2;
						set(level, p, inside || !shell ? Blocks.AIR.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState());
						continue;
					}
					boolean wall = Math.abs(dx) >= hx + 1 || Math.abs(dz) >= hz + 1 || dy <= -1 || dy >= h;
					if (Math.abs(dx) > hx + 1 || Math.abs(dz) > hz + 1 || dy > h) {
						set(level, p, Blocks.AIR.defaultBlockState());
					} else if (dy == -1) {
						set(level, p, (dx + dz) % 3 == 0 ? Blocks.DEEPSLATE_TILES.defaultBlockState() : Blocks.DARK_OAK_PLANKS.defaultBlockState());
					} else if (dy == h) {
						set(level, p, Math.abs(dx) % 3 == 0 ? Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState());
					} else if (wall) {
						set(level, p, rnd.nextFloat() < 0.2F ? Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState()
							: (dy == 0 ? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState()));
					} else {
						set(level, p, Blocks.AIR.defaultBlockState());
					}
				}
			}
		}
		for (int dy = 0; dy <= 1; dy++) {
			set(level, origin.offset(0, dy, hz + 1), Blocks.AIR.defaultBlockState());
			set(level, origin.offset(0, dy, hz + 1 + passage), Blocks.AIR.defaultBlockState());
		}

		// small rug
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				set(level, origin.offset(dx, 0, dz), (Math.abs(dx) == 2 || Math.abs(dz) == 1)
					? Blocks.CARPET.pick(DyeColor.PURPLE).defaultBlockState() : Blocks.CARPET.pick(DyeColor.BLACK).defaultBlockState());
			}
		}

		// north wall: three rows of shelves crammed with jars, bottles and skulls
		ItemStack[] stock = {
			jar(ModBlocks.SOUL_JAR), jar(ModBlocks.DUSTY_JAR), new ItemStack(Items.SKELETON_SKULL),
			jar(ModBlocks.SPECIMEN_JAR), jar(ModBlocks.POTION_BOTTLES), jar(ModBlocks.TALL_POTION),
			jar(ModBlocks.DUSTY_JAR), new ItemStack(Items.AMETHYST_CLUSTER), jar(ModBlocks.SOUL_JAR),
			jar(ModBlocks.TALL_POTION), new ItemStack(Items.WITHER_SKELETON_SKULL), jar(ModBlocks.POTION_BOTTLES),
			new ItemStack(Items.BOOK), jar(ModBlocks.SPECIMEN_JAR), jar(ModBlocks.DUSTY_JAR),
			jar(ModBlocks.SOUL_JAR), new ItemStack(Items.CLOCK), jar(ModBlocks.TALL_POTION),
		};
		int n = 0;
		for (int row = 1; row <= 3; row++) {
			for (int dx = -hx; dx <= hx; dx++) {
				BlockPos p = origin.offset(dx, row, -hz);
				set(level, p, Blocks.DARK_OAK_SHELF.defaultBlockState().setValue(ShelfBlock.FACING, Direction.SOUTH));
				if (level.getBlockEntity(p) instanceof ShelfBlockEntity shelf) {
					for (int slot = 0; slot < 3; slot++) {
						if (rnd.nextFloat() < 0.85F) {
							shelf.getItems().set(slot, stock[n++ % stock.length].copy());
						}
					}
					shelf.setChanged();
				}
			}
		}
		// jars on the floor under the shelves
		Block[] floorJars = {ModBlocks.SPECIMEN_JAR, ModBlocks.DUSTY_JAR, ModBlocks.POTION_BOTTLES, ModBlocks.SOUL_JAR, ModBlocks.TALL_POTION, ModBlocks.DUSTY_JAR};
		int[] floorX = {-4, -3, -1, 1, 3, 4};
		for (int i = 0; i < floorX.length; i++) {
			placeJar(level, origin.offset(floorX[i], 0, -hz), floorJars[i], rnd);
		}

		// west wall: the mysterious door (iron, no button) between two bookcases topped with jars
		BlockPos door = origin.offset(-hx - 1, 0, 0);
		set(level, door, Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.EAST).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
		set(level, door.above(), Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.EAST).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
		for (int dz : new int[]{-1, 1}) {
			set(level, door.offset(0, 0, dz), Blocks.CRYING_OBSIDIAN.defaultBlockState());
			set(level, door.offset(0, 1, dz), Blocks.CRYING_OBSIDIAN.defaultBlockState());
		}
		set(level, door.above(2), Blocks.CHISELED_DEEPSLATE.defaultBlockState());
		for (int dz : new int[]{-2, 2}) {
			for (int dy = 0; dy <= 1; dy++) {
				set(level, origin.offset(-hx, dy, dz), dy == 0 ? Blocks.CHISELED_BOOKSHELF.defaultBlockState() : Blocks.BOOKSHELF.defaultBlockState());
			}
			placeJar(level, origin.offset(-hx, 2, dz), dz < 0 ? ModBlocks.SOUL_JAR : ModBlocks.TALL_POTION, rnd);
		}
		set(level, origin.offset(-hx, 0, 1), Blocks.DYED_CANDLE.pick(DyeColor.PURPLE).defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));

		// east wall: odds-and-ends chest, a barrel, and a chest that is locked forever; jars piled on top
		BlockPos junk = origin.offset(hx, 0, -1);
		set(level, junk, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST));
		if (level.getBlockEntity(junk) instanceof ChestBlockEntity chest) {
			chest.setItem(4, new ItemStack(Items.PAPER, 5));
			chest.setItem(10, new ItemStack(Items.BONE, 3));
			chest.setItem(13, new ItemStack(Items.MAP));
			chest.setItem(16, new ItemStack(ModBlocks.DUSTY_JAR, 2));
			chest.setItem(22, new ItemStack(Items.STRING, 4));
		}
		set(level, origin.offset(hx, 0, 0), Blocks.BARREL.defaultBlockState());
		BlockPos locked = origin.offset(hx, 0, 1);
		set(level, locked, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST));
		level.getServer().getCommands().performPrefixedCommand(
			level.getServer().createCommandSourceStack().withSuppressedOutput().withLevel(level),
			"data merge block " + locked.getX() + " " + locked.getY() + " " + locked.getZ() + " {lock:{items:\"underworld:sealed_relic\"}}"
		);
		placeJar(level, origin.offset(hx, 1, -1), ModBlocks.SPECIMEN_JAR, rnd);
		placeJar(level, origin.offset(hx, 1, 0), ModBlocks.POTION_BOTTLES, rnd);
		placeJar(level, origin.offset(hx, 1, 1), ModBlocks.DUSTY_JAR, rnd);
		set(level, origin.offset(hx, 2, 0), Blocks.SKELETON_WALL_SKULL.defaultBlockState().setValue(WallSkullBlock.FACING, Direction.WEST));

		// pedestals by the entrance: something red and alive, and the one he won't let you take
		Object[][] pedestals = {
			{-3, 2, new ItemStack(Items.CREAKING_HEART), 0.55F},
			{3, 2, new ItemStack(ModItems.SEALED_RELIC), 0.7F},
		};
		for (Object[] pd : pedestals) {
			BlockPos p = origin.offset((Integer) pd[0], 0, (Integer) pd[1]);
			set(level, p, Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState());
			set(level, p.above(), Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState());
			standingItem(level, Vec3.atBottomCenterOf(p.above()).add(0, 0.5, 0), (ItemStack) pd[2], 180F, (Float) pd[3]);
		}
		Interaction touch = new Interaction(EntityTypes.INTERACTION, level);
		touch.setWidth(0.8F);
		touch.setHeight(0.9F);
		touch.setResponse(true);
		touch.addTag(RELIC_TAG);
		touch.setPos(Vec3.atBottomCenterOf(origin.offset(3, 2, 2)));
		level.addFreshEntity(touch);

		// clutter on the floor
		placeJar(level, origin.offset(-3, 0, 3), ModBlocks.POTION_BOTTLES, rnd);
		placeJar(level, origin.offset(4, 0, 3), ModBlocks.DUSTY_JAR, rnd);
		placeJar(level, origin.offset(-4, 0, -1), ModBlocks.TALL_POTION, rnd);
		lyingItem(level, Vec3.atBottomCenterOf(origin.offset(-2, 0, 2)), new ItemStack(Items.MAP), 20F);
		lyingItem(level, Vec3.atBottomCenterOf(origin.offset(2, 0, -2)), damaged(Items.IRON_SWORD), 115F);
		lyingItem(level, Vec3.atBottomCenterOf(origin.offset(-1, 0, 2)), new ItemStack(ModItems.COLLECTOR_LEDGER), 200F);

		// light: two hanging soul lanterns, a candle or two, cobwebs in the corners
		for (int dx : new int[]{-2, 2}) {
			set(level, origin.offset(dx, h - 1, 0), Blocks.IRON_CHAIN.defaultBlockState());
			set(level, origin.offset(dx, h - 2, 0), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
		}
		set(level, origin.offset(2, 0, 2), Blocks.DYED_CANDLE.pick(DyeColor.PURPLE).defaultBlockState().setValue(CandleBlock.CANDLES, 2).setValue(CandleBlock.LIT, true));
		for (int[] c : new int[][]{{-hx, -hz}, {hx, -hz}, {-hx, hz}, {hx, hz}}) {
			set(level, origin.offset(c[0], h - 1, c[1]), Blocks.COBWEB.defaultBlockState());
		}

		// the Collector
		for (Collector old : level.getEntitiesOfClass(Collector.class, new net.minecraft.world.phys.AABB(origin).inflate(20))) {
			old.discard();
		}
		Collector collector = ModEntities.COLLECTOR.create(level, EntitySpawnReason.STRUCTURE);
		BlockPos home = origin.offset(0, 0, -1);
		collector.setHome(home);
		collector.snapTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0, 0);
		level.addFreshEntity(collector);
		return collector;
	}

	private static ItemStack jar(Block block) {
		return new ItemStack(block);
	}

	private static void placeJar(ServerLevel level, BlockPos pos, Block jar, RandomSource rnd) {
		set(level, pos, jar.defaultBlockState().setValue(CuriosityBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(rnd)));
	}

	public static final String RELIC_TAG = "underworld_sealed_relic";

	private static ItemStack damaged(net.minecraft.world.item.Item item) {
		ItemStack s = new ItemStack(item);
		s.setDamageValue(Math.max(0, s.getMaxDamage() - 3));
		return s;
	}

	private static void set(ServerLevel level, BlockPos pos, BlockState state) {
		level.setBlock(pos, state, FLAGS);
		BlockEntity be = level.getBlockEntity(pos);
		if (be != null) {
			be.setChanged();
		}
	}
}
