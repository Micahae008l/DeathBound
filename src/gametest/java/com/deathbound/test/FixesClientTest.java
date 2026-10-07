package com.deathbound.test;

import com.deathbound.entity.HollowHunter;
import com.deathbound.entity.LostSoul;
import com.deathbound.entity.SoulBolt;
import com.deathbound.registry.ModAttachments;
import com.deathbound.registry.ModEntities;
import com.deathbound.registry.ModItems;
import com.deathbound.world.Layout;
import com.deathbound.world.UnderworldTravel;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Exercises the fixes in a real game: ./gradlew runClientGameTest
 * Results are printed as [DB-TEST] lines; screenshots go to build/run/clientGameTest/screenshots.
 */
public class FixesClientTest implements FabricClientGameTest {
	private static void log(String s) {
		System.out.println("[DB-TEST] " + s);
	}

	private static ServerPlayer player(net.minecraft.server.MinecraftServer s) {
		return s.getPlayerList().getPlayers().getFirst();
	}

	@Override
	public void runTest(ClientGameTestContext ctx) {
		try (TestSingleplayerContext world = ctx.worldBuilder().create()) {
			TestServerContext server = world.getServer();
			world.getConnection().waitForChunksRender();
			server.runCommand("gamerule advance_time false");
			server.runCommand("time set noon");
			server.runCommand("gamemode survival @a");
			server.runCommand("effect give @a resistance infinite 255 true");
			ctx.waitTicks(10);

			if (System.getenv("UW_STORIES") != null) {
				NewWorkChecks.readAll(ctx, world, server);
				return;
			}

			if (System.getenv("UW_SHOTS") != null) {
				NewWorkChecks.shots(ctx, world, server);
				return;
			}

			if (System.getenv("UW_NEW") != null) {
				NewWorkChecks.run(ctx, world, server);
				return;
			}

			boolean onlyHunter = System.getenv("UW_HUNTER") != null;
			if (System.getenv("UW_TEXTURES") != null) {
				String tag = System.getenv("UW_TEXTURES");
				server.runCommand("gamemode spectator @a");
				server.runCommand("fill -12 199 -6 12 199 6 minecraft:smooth_stone");
				String[] mod = {"soulstone", "soulstone_bricks", "cracked_soulstone_bricks", "dark_soulstone_bricks", "soul_veined_bricks",
					"soulstone_tiles", "polished_soulstone", "chiseled_soulstone", "soulstone_pillar", "ashen_soil", "ghostwood_log",
					"ghostwood_planks", "ossified_log", "curio_shelf", "ossuary_shelf", "ferry"};
				String[] van = {"cobbled_deepslate", "stone_bricks", "cracked_stone_bricks", "deepslate_bricks", "deepslate_bricks",
					"deepslate_tiles", "polished_deepslate", "chiseled_deepslate", "quartz_pillar", "soul_soil", "oak_log",
					"oak_planks", "birch_log", "bookshelf", "bookshelf", "dark_oak_planks"};
				for (int i = 0; i < mod.length; i++) {
					int x = -8 + i;
					server.runCommand("setblock " + x + " 200 0 deathbound:" + mod[i]);
					server.runCommand("setblock " + x + " 201 0 deathbound:" + mod[i]);
					server.runCommand("setblock " + x + " 200 -2 minecraft:" + van[i]);
				}
				server.runCommand("setblock 9 200 1 deathbound:tombstone");
				server.runCommand("setblock 10 202 0 deathbound:tattered_banner");
				ctx.waitTicks(20);
				world.getConnection().waitForChunksRender();
				server.runCommand("tp @a -4.5 200.3 5.5 180 12");
				ctx.waitTicks(20);
				ctx.takeScreenshot(tag + "_row_left");
				server.runCommand("tp @a 3.5 200.3 5.5 180 12");
				ctx.waitTicks(20);
				ctx.takeScreenshot(tag + "_row_right");
				server.runCommand("tp @a -6.5 199.9 2.6 180 8");
				ctx.waitTicks(20);
				ctx.takeScreenshot(tag + "_close_bricks");
				server.runCommand("tp @a 8.0 199.9 3.4 200 5");
				ctx.waitTicks(20);
				ctx.takeScreenshot(tag + "_close_tomb_banner");
				// the Underworld itself
				server.runCommand("execute in deathbound:underworld run tp @a " + (Layout.VILLAGE.x() - 6) + " " + (Layout.VILLAGE.top() + 6) + " " + (Layout.VILLAGE.z() + 18) + " 150 18");
				ctx.waitTicks(60);
				world.getConnection().waitForChunksRender();
				ctx.takeScreenshot(tag + "_village");
				server.runCommand("execute in deathbound:underworld run tp @a " + (Layout.CRYPT.x() + 10) + " " + (Layout.CRYPT.top() + 6) + " " + (Layout.CRYPT.z() + 16) + " 160 22");
				ctx.waitTicks(60);
				world.getConnection().waitForChunksRender();
				ctx.takeScreenshot(tag + "_crypt");
				server.runCommand("execute in deathbound:underworld run tp @a " + (Layout.FOREST.x() + 8) + " " + (Layout.FOREST.top() + 4) + " " + (Layout.FOREST.z() + 14) + " 160 10");
				ctx.waitTicks(60);
				world.getConnection().waitForChunksRender();
				ctx.takeScreenshot(tag + "_forest");
				return;
			}

			if (System.getenv("UW_SOLIDS") != null) {
				server.runOnServer(s -> {
					StringBuilder solid = new StringBuilder();
					StringBuilder other = new StringBuilder();
					StringBuilder leaves = new StringBuilder();
					for (net.minecraft.world.level.block.Block b : net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
						net.minecraft.resources.Identifier id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(b);
						if (!id.getNamespace().equals("deathbound")) continue;
						boolean anySolid = b.getStateDefinition().getPossibleStates().stream().anyMatch(st -> st.isSolid());
						if (b instanceof net.minecraft.world.level.block.LeavesBlock) leaves.append(id).append(',');
						else if (anySolid) solid.append(id).append(',');
						else other.append(id).append(',');
						if (b.defaultBlockState().is(net.minecraft.tags.BlockTags.BLOCKS_MOTION_NO_LEAVES)) log("already tagged: " + id);
					}
					log("SOLID=" + solid);
					log("NONSOLID=" + other);
					log("LEAVES=" + leaves);
				});
				return;
			}
			if (!onlyHunter) {
			// ------------------------------------------------------------ lore books -> journal
			server.runOnServer(s -> {
				ServerPlayer p = player(s);
				ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
				book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough("Test Lore"), "Carved over the tombs", 3,
					List.of(Filterable.passThrough(Component.literal("page one"))), true));
				p.setItemInHand(InteractionHand.MAIN_HAND, book.copy());
				p.setShiftKeyDown(true);
				p.gameMode.useItem(p, p.level(), p.getMainHandItem(), InteractionHand.MAIN_HAND);
				int filed1 = p.getAttachedOrElse(ModAttachments.LORE_PAGES, List.of()).size();
				boolean consumed = p.getMainHandItem().isEmpty();
				p.setItemInHand(InteractionHand.MAIN_HAND, book.copy());
				p.gameMode.useItem(p, p.level(), p.getMainHandItem(), InteractionHand.MAIN_HAND);
				int filed2 = p.getAttachedOrElse(ModAttachments.LORE_PAGES, List.of()).size();
				p.setShiftKeyDown(false);
				log("lore: filed=" + filed1 + " consumed=" + consumed + " afterDuplicate=" + filed2 + " -> " + (filed1 == 1 && consumed && filed2 == 1 ? "PASS" : "FAIL"));
			});

			// ------------------------------------------------------------ reaper's charm healing
			server.runCommand("effect clear @a");
			server.runOnServer(s -> {
				ServerPlayer p = player(s);
				ItemStack relic = new ItemStack(ModItems.DEATHBOUND_RELIC);
				relic.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(ModItems.REAPERS_CHARM))));
				p.getInventory().setItem(8, relic);
				p.getFoodData().setFoodLevel(20);
				p.getFoodData().setSaturation(20.0F);
				p.setHealth(14.0F);
			});
			ctx.waitTicks(80);
			server.runOnServer(s -> {
				ServerPlayer p = player(s);
				float h = p.getHealth();
				log("reaper above 30%: 14.0 -> " + h + " -> " + (h > 14.5F ? "PASS (heals normally)" : "FAIL (no healing)"));
				p.getFoodData().setFoodLevel(20);
				p.getFoodData().setSaturation(20.0F);
				p.setHealth(3.0F);
			});
			ctx.waitTicks(120);
			server.runOnServer(s -> {
				ServerPlayer p = player(s);
				float h = p.getHealth();
				log("reaper below 30%: 3.0 -> " + h + " (cap 6.0) -> " + (h > 3.0F && h <= 6.01F ? "PASS (stops at 30%)" : "FAIL"));
				p.getInventory().setItem(8, ItemStack.EMPTY);
				p.setHealth(20.0F);
			});

			// ------------------------------------------------------------ shield vs soul bolt
			server.runOnServer(s -> {
				ServerPlayer p = player(s);
				p.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
				p.teleportTo(p.level(), p.getX(), p.getY(), p.getZ(), java.util.Set.of(), 0.0F, 0.0F, true);
				p.startUsingItem(InteractionHand.OFF_HAND);
			});
			ctx.waitTicks(15);
			server.runOnServer(s -> {
				ServerPlayer p = player(s);
				ServerLevel level = p.level();
				LostSoul dummyOwner = ModEntities.LOST_SOUL.create(level, EntitySpawnReason.COMMAND);
				dummyOwner.setPos(p.getX(), p.getY() + 1, p.getZ() + 8);
				level.addFreshEntity(dummyOwner);
				Vec3 from = p.getEyePosition().add(0, -0.3, 4.0);
				SoulBolt bolt = new SoulBolt(level, from.x, from.y, from.z, new Vec3(0, 0, -1), 6.0F);
				bolt.setOwner(dummyOwner);
				bolt.setDeltaMovement(0, 0, -0.6);
				level.addFreshEntity(bolt);
				log("shield: blocking=" + p.isBlocking() + " health before=" + p.getHealth());
			});
			ctx.waitTicks(25);
			server.runOnServer(s -> {
				ServerPlayer p = player(s);
				log("shield: health after soul bolt=" + p.getHealth() + " -> " + (p.getHealth() >= 19.9F ? "PASS (blocked)" : "FAIL (hit through shield)"));
				p.stopUsingItem();
				p.setHealth(20.0F);
				p.level().getEntitiesOfClass(LostSoul.class, p.getBoundingBox().inflate(20)).forEach(e -> e.discard());
			});

			// ------------------------------------------------------------ lost souls give souls (once each)
			server.runOnServer(s -> {
				ServerPlayer p = player(s);
				ServerLevel level = p.level();
				p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
				int before = p.getInventory().countItem(ModItems.SOUL);
				List<LostSoul> souls = new java.util.ArrayList<>();
				for (int i = 0; i < 60; i++) {
					LostSoul g = ModEntities.LOST_SOUL.create(level, EntitySpawnReason.COMMAND);
					g.setPos(p.getX() + (i % 8) - 4, p.getY(), p.getZ() + 3 + i / 8);
					level.addFreshEntity(g);
					souls.add(g);
				}
				for (LostSoul g : souls) {
					g.interact(p, InteractionHand.MAIN_HAND, g.position());
				}
				int first = p.getInventory().countItem(ModItems.SOUL) - before;
				for (LostSoul g : souls) {
					g.interact(p, InteractionHand.MAIN_HAND, g.position());
				}
				int second = p.getInventory().countItem(ModItems.SOUL) - before - first;
				log("ghosts: 60 ghosts talked to -> " + first + " souls (expect ~15), talking again -> " + second + " more (expect 0) -> "
					+ (first >= 4 && first <= 30 && second == 0 ? "PASS" : "FAIL"));
				souls.forEach(LostSoul::discard);
			});

			}

			// ------------------------------------------------------------ the underworld: bell sigil + hunter kick
			if (!onlyHunter) {
			server.runCommand("execute in deathbound:underworld run tp @a " + (Layout.VILLAGE.x() + 10) + " " + (Layout.VILLAGE.top() + 2) + " " + (Layout.VILLAGE.z() + 10));
			server.runCommand("gamemode creative @a");
			ctx.waitTicks(40);
			world.getConnection().waitForChunksRender();
			ctx.waitTicks(20);
			for (int round = 1; round <= 2; round++) {
				for (int i = 0; i < 4; i++) {
					server.runCommand("execute as @p at @p run deathbound ringbell");
					ctx.waitTicks(10);
				}
				ctx.waitTicks(80);
				final int r = round;
				server.runOnServer(s -> {
					ServerLevel uw = s.getLevel(UnderworldTravel.UNDERWORLD);
					int n = uw.getEntities(EntityTypes.ITEM, e -> e.getItem().is(ModItems.SIGIL_BELL)).size();
					log("bell puzzle round " + r + ": bell sigils in world = " + n + (r == 1 ? " (expect 1)" : " (expect still 1)"));
				});
			}
			ctx.takeScreenshot("bell_sigil");
			}

			server.runCommand("execute in deathbound:underworld run tp @a " + (Layout.HOLLOW.x() + 10) + " " + (Layout.HOLLOW.top() + 3) + " " + Layout.HOLLOW.z());
			ctx.waitTicks(40);
			world.getConnection().waitForChunksRender();
			ctx.waitTicks(20);
			server.runOnServer(s -> {
				ServerLevel uw = s.getLevel(UnderworldTravel.UNDERWORLD);
				Layout.Island h = Layout.HOLLOW;
				for (int[] o : new int[][]{{0, 0}, {h.radius() - 2, 0}, {0, h.radius() - 2}, {-(h.radius() - 2), 0}}) {
					int x = h.x() + o[0], z = h.z() + o[1];
					uw.getChunk(x >> 4, z >> 4);
					int hm = uw.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new net.minecraft.core.BlockPos(x, 0, z)).getY();
					int scan = -999;
					for (int y = h.top() + 30; y > h.top() - 60; y--) {
						if (!uw.getBlockState(new net.minecraft.core.BlockPos(x, y, z)).isAir()) {
							scan = y + 1;
							break;
						}
					}
					log("probe hollow (" + x + "," + z + "): heightmap=" + hm + " firstSolid+1=" + scan + " block=" + uw.getBlockState(new net.minecraft.core.BlockPos(x, scan - 1, z)));
					net.minecraft.world.level.block.state.BlockState top = uw.getBlockState(new net.minecraft.core.BlockPos(x, scan - 1, z));
					net.minecraft.world.level.chunk.LevelChunk ch = uw.getChunkAt(new net.minecraft.core.BlockPos(x, 0, z));
					boolean counts = net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES.isOpaque().test(top);
					log("   top block counts as solid for heightmaps: " + counts + " chunkStatus=" + ch.getPersistedStatus()
						+ " minY=" + uw.getMinY() + " height=" + uw.getHeight());

				}
			});
			server.runCommand("gamemode survival @a");
			server.runCommand("effect give @a resistance infinite 255 true");
			for (int trial = 0; trial < 5; trial++) {
				final int tr = trial;
				server.runOnServer(s -> {
					ServerLevel uw = s.getLevel(UnderworldTravel.UNDERWORLD);
					ServerPlayer p = player(s);
					uw.getEntitiesOfClass(HollowHunter.class, new AABB(p.blockPosition()).inflate(80)).forEach(e -> e.discard());
					Layout.Island h = Layout.HOLLOW;
					double a = tr * 1.25;
					// hunter right at the rim, player on the island side: the kick's back-leap points off the edge
					// walk inward from the rim until there is ground: the edge of the island is ragged
					int hx = h.x(), hz = h.z();
					for (int r = h.radius(); r > 4; r--) {
						int cx2 = (int) (h.x() + Math.cos(a) * r), cz2 = (int) (h.z() + Math.sin(a) * r);
						uw.getChunk(cx2 >> 4, cz2 >> 4);
						if (uw.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new net.minecraft.core.BlockPos(cx2, 0, cz2)).getY() > h.top() - 4) {
							hx = cx2;
							hz = cz2;
							break;
						}
					}
					for (int cx = -2; cx <= 2; cx++) {
						for (int cz = -2; cz <= 2; cz++) {
							uw.getChunk((hx >> 4) + cx, (hz >> 4) + cz);   // make sure the island is generated here
						}
					}
					int hy = uw.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new net.minecraft.core.BlockPos(hx, 0, hz)).getY();
					HollowHunter hunter = ModEntities.HOLLOW_HUNTER.create(uw, EntitySpawnReason.COMMAND);
					hunter.setPos(hx + 0.5, hy, hz + 0.5);
					uw.addFreshEntity(hunter);
					double px = hx + 0.5 - Math.cos(a) * 2.2;
					double pz = hz + 0.5 - Math.sin(a) * 2.2;
					p.teleportTo(uw, px, uw.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.core.BlockPos.containing(px, 0, pz)).getY(), pz, java.util.Set.of(), 0, 0, true);
					hunter.setTarget(p);
					hunter.debugAction(HollowHunter.LEAP);
					log("hunter trial " + tr + ": placed at rim y=" + hy + " island top=" + h.top());
				});
				ctx.waitTicks(45);
				server.runOnServer(s -> {
					ServerLevel uw = s.getLevel(UnderworldTravel.UNDERWORLD);
					ServerPlayer p = player(s);
					List<HollowHunter> hs = uw.getEntitiesOfClass(HollowHunter.class, new AABB(p.blockPosition()).inflate(80));
					if (hs.isEmpty()) {
						log("hunter trial " + tr + ": hunter missing -> FAIL");
						return;
					}
					HollowHunter hunter = hs.getFirst();
					Layout.Island h = Layout.HOLLOW;
					double d = Math.hypot(hunter.getX() - h.x(), hunter.getZ() - h.z());
					boolean onIsland = d < h.radius() + 1 && hunter.getY() > h.top() - 4;
					log("hunter trial " + tr + ": after kick y=" + String.format("%.1f", hunter.getY()) + " distFromCenter=" + String.format("%.1f", d)
						+ " -> " + (onIsland ? "PASS (stayed on the island)" : "FAIL (fell off)"));
				});
			}
			ctx.takeScreenshot("hunter_after_kick");
		}
	}
}
