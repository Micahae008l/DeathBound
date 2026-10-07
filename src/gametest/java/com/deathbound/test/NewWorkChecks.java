package com.deathbound.test;

import com.deathbound.charm.Charms;
import com.deathbound.entity.DeathEntity;
import com.deathbound.entity.DeathsGuard;
import com.deathbound.entity.Gravebound;
import com.deathbound.entity.Hazards;
import com.deathbound.entity.HunterArrow;
import com.deathbound.entity.LostSoul;
import com.deathbound.entity.SoulWisp;
import com.deathbound.item.AldousLanternItem;
import com.deathbound.npc.UnderworldNpc;
import com.deathbound.registry.ModAttachments;
import com.deathbound.registry.ModBlocks;
import com.deathbound.registry.ModEffects;
import com.deathbound.registry.ModEntities;
import com.deathbound.registry.ModItems;
import com.deathbound.world.Layout;
import com.deathbound.world.QuestEvents;
import com.deathbound.world.UnderworldTravel;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** UW_NEW=1 ./gradlew runClientGameTest : the bug-list fixes, new charms, Warden buff, quest events, and screenshots of each. */
final class NewWorkChecks {
	private static void log(String s) {
		System.out.println("[DB-TEST] " + s);
	}

	private static String pass(boolean ok) {
		return ok ? "PASS" : "FAIL";
	}

	private static ServerPlayer player(MinecraftServer s) {
		return s.getPlayerList().getPlayers().getFirst();
	}

	private static ItemStack relicWith(ItemStack charm) {
		ItemStack relic = new ItemStack(ModItems.DEATHBOUND_RELIC);
		relic.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(charm)));
		return relic;
	}

	private static void tp(TestServerContext server, String where) {
		server.runCommand("execute in deathbound:underworld run tp @a " + where);
	}

	private static void settle(ClientGameTestContext ctx, TestSingleplayerContext world, int ticks) {
		ctx.waitTicks(ticks);
		world.getConnection().waitForChunksRender();
		ctx.waitTicks(10);
	}

	static void shots(ClientGameTestContext ctx, TestSingleplayerContext world, TestServerContext server) {
		// light the screenshots (test camera only): night vision and full brightness
		server.runCommand("effect give @a night_vision infinite 0 true");
		ctx.runOnClient(mc -> mc.options.gamma().set(1.0));
		server.runCommand("gamemode survival @a");
		tp(server, "4.5 " + (Layout.ARRIVAL.top() + 1) + " 4.5 180 10");
		settle(ctx, world, 40);
		// ---- stories: chest books never repeat in a world
		server.runOnServer(s -> {
			ServerLevel uw = s.getLevel(UnderworldTravel.UNDERWORLD);
			net.minecraft.world.level.storage.loot.LootTable table = s.reloadableRegistries().getLootTable(
				net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, com.deathbound.DeathBound.id("journal")));
			java.util.List<String> got = new java.util.ArrayList<>();
			int souls = 0;
			for (int i = 0; i < 30; i++) {
				net.minecraft.world.level.storage.loot.LootParams params = new net.minecraft.world.level.storage.loot.LootParams.Builder(uw)
					.withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, Vec3.ZERO)
					.create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
				for (ItemStack st : table.getRandomItems(params)) {
					if (st.has(ModItems.STORY)) {
						got.add(st.get(ModItems.STORY));
					} else if (st.is(ModItems.SOUL)) {
						souls++;
					} else {
						log("stories: unexpected drop " + st);
					}
				}
			}
			boolean unique = new java.util.HashSet<>(got).size() == got.size();
			log("stories: 30 chest rolls -> " + got.size() + " stories (" + new java.util.HashSet<>(got).size() + " different), then " + souls + " souls -> "
				+ pass(unique && got.size() == com.deathbound.story.Stories.BOOKS.size() && souls == 30 - got.size()));
		});

		// ---- notes in the world
		server.runCommand("gamemode creative @a");
		java.util.List<int[]> notes = new java.util.ArrayList<>();
		for (Layout.Island is : new Layout.Island[]{Layout.ARRIVAL, Layout.FOREST, Layout.VILLAGE}) {
			tp(server, is.x() + " " + (is.top() + 30) + " " + is.z());
			settle(ctx, world, 60);
			server.runOnServer(s -> {
				ServerLevel uw = s.getLevel(UnderworldTravel.UNDERWORLD);
				for (BlockPos q : BlockPos.betweenClosed(is.x() - is.radius() - 4, is.top() - 12, is.z() - is.radius() - 4, is.x() + is.radius() + 4, is.top() + 16, is.z() + is.radius() + 4)) {
					if (uw.getBlockState(q).is(ModBlocks.STORY_NOTE)) {
						var st = uw.getBlockState(q);
						log("note " + st.getValue(com.deathbound.block.StoryNoteBlock.STORY) + " (" + com.deathbound.story.Stories.NOTES.get(st.getValue(com.deathbound.block.StoryNoteBlock.STORY))
							+ ") at " + q.toShortString() + (st.getValue(com.deathbound.block.StoryNoteBlock.FLAT) ? " on " + uw.getBlockState(q.below()).getBlock() : " pinned to " + uw.getBlockState(q.relative(st.getValue(com.deathbound.block.StoryNoteBlock.FACING).getOpposite())).getBlock()));
						notes.add(new int[]{q.getX(), q.getY(), q.getZ(), st.getValue(com.deathbound.block.StoryNoteBlock.FACING).get2DDataValue(), st.getValue(com.deathbound.block.StoryNoteBlock.FLAT) ? 1 : 0});
					}
				}
			});
		}
		log("notes found: " + notes.size() + " of " + com.deathbound.story.Stories.NOTES.size() + " -> " + pass(notes.size() == com.deathbound.story.Stories.NOTES.size()));
		// look at a pinned one and a lying one
		for (int[] n : notes) {
			if (n[4] == 0 && n[0] != 7) {
				Direction f = Direction.from2DDataValue(n[3]);
				tp(server, (n[0] + 0.5 + f.getStepX() * 2.2) + " " + (n[1] - 0.6) + " " + (n[2] + 0.5 + f.getStepZ() * 2.2) + " facing " + (n[0] + 0.5) + " " + (n[1] + 0.5) + " " + (n[2] + 0.5));
				settle(ctx, world, 120);
				ctx.takeScreenshot("shot_note_tree");
				break;
			}
		}
		for (int[] n : notes) {
			if (n[4] == 1) {
				tp(server, (n[0] + 1.8) + " " + (n[1] + 0.9) + " " + (n[2] + 1.8) + " facing " + (n[0] + 0.5) + " " + (n[1]) + " " + (n[2] + 0.5));
				settle(ctx, world, 120);
				ctx.takeScreenshot("shot_note_table");
				break;
			}
		}
		for (int[] n : notes) {
			if (n[0] == 7) {
				tp(server, "7.5 " + (n[1] - 0.6) + " " + (n[2] + 2.7) + " facing 7.5 " + (n[1] + 0.5) + " " + (n[2] + 0.5));
				settle(ctx, world, 120);
				ctx.takeScreenshot("shot_note_dock");
				break;
			}
		}

		// ---- reading: right-click a note (server files it), then the screens
		server.runOnServer(s -> {
			ServerLevel uw = s.getLevel(UnderworldTravel.UNDERWORLD);
			ServerPlayer p = player(s);
			for (int[] n : notes) {
				BlockPos q = new BlockPos(n[0], n[1], n[2]);
				p.gameMode.useItemOn(p, uw, ItemStack.EMPTY, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(q), Direction.UP, q, false));
				break;
			}
			com.deathbound.story.Stories.found(p, "last_torch");
			com.deathbound.story.Stories.found(p, "wings");
			log("stories: in the Journal now " + p.getAttachedOrElse(ModAttachments.STORIES_FOUND, List.of()));
		});
		ctx.waitTicks(10);
		for (String id : new String[]{"note_ferry", "note_shed", "last_torch", "bad_omen"}) {
			ctx.runOnClient(mc -> mc.gui.setScreen(new com.deathbound.client.StoryScreen(id)));
			ctx.waitTicks(5);
			ctx.takeScreenshot("shot_read_" + id);
		}
		ctx.runOnClient(mc -> mc.gui.setScreen(null));
		server.runOnServer(s -> player(s).giveExperienceLevels(0));
		ctx.runOnClient(mc -> com.deathbound.client.Journal.open(mc.player));
		ctx.waitTicks(5);
		ctx.runOnClient(mc -> {
			if (mc.gui.screen() instanceof net.minecraft.client.gui.screens.inventory.BookViewScreen book) {
				book.setPage(2);
			}
		});
		ctx.waitTicks(5);
		ctx.takeScreenshot("shot_journal_stories");
		ctx.runOnClient(mc -> {
			if (mc.gui.screen() instanceof net.minecraft.client.gui.screens.inventory.BookViewScreen book) {
				book.setPage(3);
			}
		});
		ctx.waitTicks(5);
		ctx.takeScreenshot("shot_journal_story_page");
		ctx.runOnClient(mc -> mc.gui.setScreen(null));
		server.runOnServer(s -> player(s).getInventory().add(com.deathbound.story.Stories.book("channeling")));
		ctx.waitTicks(5);
		ctx.takeScreenshot("shot_book_in_hotbar");
		log("shots done");
	}

	static void run(ClientGameTestContext ctx, TestSingleplayerContext world, TestServerContext server) {
		// ---------------------------------------------------------------- wood tags + recipes
		server.runOnServer(s -> {
			ServerLevel lvl = s.overworld();
			ItemStack planks = new ItemStack(ModBlocks.GHOSTWOOD_PLANKS);
			boolean tagged = planks.is(ItemTags.PLANKS) && new ItemStack(ModBlocks.GHOSTWOOD_LOG).is(ItemTags.LOGS_THAT_BURN);
			CraftingInput table = CraftingInput.of(2, 2, List.of(planks.copy(), planks.copy(), planks.copy(), planks.copy()));
			CraftingInput sticks = CraftingInput.of(1, 2, List.of(planks.copy(), planks.copy()));
			String t = s.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, table, lvl).map(h -> h.value().assemble(table).toString()).orElse("none");
			String k = s.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, sticks, lvl).map(h -> h.value().assemble(sticks).toString()).orElse("none");
			log("ghostwood: in planks/logs tags=" + tagged + ", 4 planks -> " + t + ", 2 planks -> " + k + " -> "
				+ pass(tagged && t.contains("crafting_table") && k.contains("stick")));
		});

		// ---------------------------------------------------------------- Ferryman's charm: one crossing
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			p.getInventory().setItem(8, relicWith(new ItemStack(ModItems.FERRYMANS_CHARM)));
			Charms.spendFerryman(p);
			boolean gone = !Charms.has(p, com.deathbound.charm.Charm.FERRYMAN);
			log("ferryman's charm: after one crossing it is " + (gone ? "gone" : "still there") + " -> " + pass(gone));
			p.getInventory().setItem(8, ItemStack.EMPTY);
		});

		// ---------------------------------------------------------------- Phantom charm
		server.runCommand("effect clear @a");
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			ServerLevel lvl = p.level();
			p.getInventory().setItem(8, relicWith(new ItemStack(ModItems.PHANTOM_CHARM)));
			p.setHealth(20.0F);
			Zombie z = EntityTypes.ZOMBIE.create(lvl, EntitySpawnReason.COMMAND);
			z.setPos(p.getX() + 1, p.getY(), p.getZ());
			p.hurtServer(lvl, lvl.damageSources().mobAttack(z), 4.0F);
			log("phantom: first hit -> health " + p.getHealth() + " -> " + pass(p.getHealth() >= 19.99F));
		});
		ctx.waitTicks(30);
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			ServerLevel lvl = p.level();
			Zombie z = EntityTypes.ZOMBIE.create(lvl, EntitySpawnReason.COMMAND);
			z.setPos(p.getX() + 1, p.getY(), p.getZ());
			p.hurtServer(lvl, lvl.damageSources().mobAttack(z), 4.0F);
			log("phantom: second hit inside 30s -> health " + p.getHealth() + " -> " + pass(p.getHealth() < 19.0F));
			p.getInventory().setItem(8, ItemStack.EMPTY);
			p.setHealth(20.0F);
		});

		// ---------------------------------------------------------------- Aldous's lantern gutters and relights
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			p.setAttached(ModAttachments.QUESTS, Map.of("mira", 1));
			p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ALDOUS_LANTERN));
		});
		ctx.waitTicks(30);
		for (int i = 0; i < 3; i++) {
			server.runOnServer(s -> {
				ServerPlayer p = player(s);
				ServerLevel lvl = p.level();
				Zombie z = EntityTypes.ZOMBIE.create(lvl, EntitySpawnReason.COMMAND);
				z.setPos(p.getX() + 1, p.getY(), p.getZ());
				p.hurtServer(lvl, lvl.damageSources().mobAttack(z), 1.0F);
				p.setHealth(20.0F);
			});
			ctx.waitTicks(30);
		}
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			ServerLevel lvl = p.level();
			ItemStack lantern = p.getMainHandItem();
			int hits = AldousLanternItem.hits(lantern);
			log("lantern: after 3 hits -> hits=" + hits + " out=" + AldousLanternItem.isOut(lantern) + " -> " + pass(AldousLanternItem.isOut(lantern)));
			BlockPos at = p.blockPosition().offset(2, 0, 0);
			lvl.setBlockAndUpdate(at, Blocks.SOUL_LANTERN.defaultBlockState());
			p.gameMode.useItemOn(p, lvl, lantern, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(at), Direction.WEST, at, false));
			log("lantern: used on a soul lantern -> hits=" + AldousLanternItem.hits(p.getMainHandItem()) + " -> " + pass(AldousLanternItem.hits(p.getMainHandItem()) == 0));
			lvl.setBlockAndUpdate(at, Blocks.AIR.defaultBlockState());
			p.setAttached(ModAttachments.QUESTS, Map.of());
		});

		// ---------------------------------------------------------------- Warden: stats, aggro from range, leash
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			ServerLevel lvl = p.level();
			BlockPos post = p.blockPosition().offset(30, 0, 0);
			DeathsGuard g = ModEntities.DEATHS_GUARD.spawn(lvl, post, EntitySpawnReason.COMMAND);
			log("warden: max health " + g.getMaxHealth() + " damage " + g.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)
				+ " -> " + pass(g.getMaxHealth() >= 379.0F));
			g.hurtServer(lvl, lvl.damageSources().playerAttack(p), 4.0F);
			log("warden: shot from 30 blocks -> target is player: " + (g.getTarget() == p) + " -> " + pass(g.getTarget() == p));
			p.teleportTo(lvl, post.getX() - 70, p.getY(), post.getZ(), Set.of(), 0.0F, 0.0F, true);
		});
		ctx.waitTicks(10);
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			ServerLevel lvl = p.level();
			List<DeathsGuard> gs = lvl.getEntitiesOfClass(DeathsGuard.class, new AABB(p.blockPosition()).inflate(120));
			boolean let = !gs.isEmpty() && gs.getFirst().getTarget() == null;
			log("warden: player 70 blocks from his post -> gave up: " + let + " -> " + pass(let));
			gs.forEach(DeathsGuard::discard);
			p.teleportTo(lvl, p.getX() + 70, p.getY(), p.getZ(), Set.of(), 0.0F, 0.0F, true);
		});

		// ---------------------------------------------------------------- Underworld mobs vs the Death King
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			ServerLevel lvl = p.level();
			DeathEntity king = ModEntities.DEATH.create(lvl, EntitySpawnReason.COMMAND);
			king.setPos(p.getX() + 12, p.getY(), p.getZ());
			king.setNoAi(true);
			lvl.addFreshEntity(king);
			Gravebound gb = ModEntities.GRAVEBOUND.create(lvl, EntitySpawnReason.COMMAND);
			gb.setPos(p.getX() + 15, p.getY(), p.getZ());
			gb.setNoAi(true);
			lvl.addFreshEntity(gb);
			float kingHp = king.getHealth();
			boolean hurtKing = king.hurtServer(lvl, lvl.damageSources().mobAttack(gb), 6.0F);
			log("death king: hit by a Gravebound -> took damage: " + hurtKing + " (hp " + kingHp + " -> " + king.getHealth() + ") -> " + pass(!hurtKing));
			log("death king: same side as Gravebound=" + Hazards.sameSide(king, gb) + ", as player=" + Hazards.sameSide(king, p)
				+ " -> " + pass(Hazards.sameSide(king, gb) && !Hazards.sameSide(king, p)));
			Hazards.eruption(lvl, gb.position(), 1, 10.0F, 2.0F, king);
		});
		ctx.waitTicks(20);
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			ServerLevel lvl = p.level();
			List<Gravebound> gbs = lvl.getEntitiesOfClass(Gravebound.class, new AABB(p.blockPosition()).inflate(30));
			boolean ok = !gbs.isEmpty() && gbs.getFirst().getHealth() >= gbs.getFirst().getMaxHealth();
			log("death king: his eruption under a Gravebound -> unhurt: " + ok + " -> " + pass(ok));
			gbs.forEach(Gravebound::discard);
			lvl.getEntitiesOfClass(DeathEntity.class, new AABB(p.blockPosition()).inflate(30)).forEach(DeathEntity::discard);
		});
		server.runCommand("effect give @a resistance infinite 255 true");

		// ---------------------------------------------------------------- the Ferryman's boat at the Landing
		server.runCommand("gamemode creative @a");
		tp(server, "11.5 " + (Layout.ARRIVAL.top() + 3) + " -4.5 facing 11.5 " + (Layout.ARRIVAL.top() + 0) + " -11");
		settle(ctx, world, 80);
		ctx.takeScreenshot("new_ferry_boat");
		tp(server, "17.5 " + (Layout.ARRIVAL.top() + 2) + " -7.5 facing 11.5 " + (Layout.ARRIVAL.top() + 0) + " -11");
		settle(ctx, world, 20);
		ctx.takeScreenshot("new_ferry_boat_side");

		// ---------------------------------------------------------------- 3D lantern, every flame state, + scythe
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			ItemStack lit = new ItemStack(ModItems.ALDOUS_LANTERN);
			p.setItemInHand(InteractionHand.MAIN_HAND, lit);
			for (int h = 1; h <= 3; h++) {
				ItemStack st = new ItemStack(ModItems.ALDOUS_LANTERN);
				AldousLanternItem.setHits(st, h);
				p.getInventory().setItem(h, st);
			}
			p.getInventory().setItem(4, new ItemStack(ModItems.PHANTOM_CHARM));
			p.getInventory().setItem(5, new ItemStack(ModItems.COLLECTORS_CHARM));
			p.getInventory().setItem(6, new ItemStack(ModItems.REAPER_SCYTHE));
		});
		server.runCommand("gamemode survival @a");
		tp(server, "4.5 " + (Layout.ARRIVAL.top() + 1) + " 4.5 180 10");
		settle(ctx, world, 40);
		ctx.takeScreenshot("new_lantern_first_person");
		ctx.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
		ctx.waitTicks(10);
		ctx.takeScreenshot("new_lantern_third_person");
		server.runOnServer(s -> player(s).setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.REAPER_SCYTHE)));
		ctx.waitTicks(10);
		ctx.takeScreenshot("new_scythe_third_person");
		ctx.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
		ctx.waitTicks(5);
		ctx.takeScreenshot("new_scythe_first_person");

		// ---------------------------------------------------------------- Mira: her spot in the line, and the clues
		server.runCommand("gamemode creative @a");
		server.runOnServer(s -> {
			ServerLevel uw = s.getLevel(UnderworldTravel.UNDERWORLD);
			BlockPos m = QuestEvents.miraSpot(uw);
			log("mira: spot this world = " + m);
		});
		BlockPos miraGuess = new BlockPos(0, Layout.GATE.top() + 1, Layout.GATE.z() + Layout.GATE.radius() - 18);
		tp(server, "8.5 " + (Layout.GATE.top() + 5) + " " + (miraGuess.getZ() + 14) + " facing 0 " + (Layout.GATE.top() + 1) + " " + (miraGuess.getZ() - 6));
		settle(ctx, world, 100);
		server.runOnServer(s -> {
			ServerLevel uw = s.getLevel(UnderworldTravel.UNDERWORLD);
			ServerPlayer p = player(s);
			BlockPos m = QuestEvents.miraSpot(uw);
			List<UnderworldNpc> miras = uw.getEntities(ModEntities.MIRA, new AABB(m).inflate(40), e -> true);
			boolean there = !miras.isEmpty() && miras.getFirst().blockPosition().distManhattan(m) <= 4;
			log("mira: standing at her spot: " + (miras.isEmpty() ? "missing" : miras.getFirst().blockPosition().toShortString()) + " -> " + pass(there));
			p.setAttached(ModAttachments.QUESTS, Map.of("mira", 1));
			for (int dz : new int[]{60, 20, 0, -12}) {
				LostSoul soul = ModEntities.LOST_SOUL.create(uw, EntitySpawnReason.COMMAND);
				soul.setPos(0.5, Layout.GATE.top() + 1, m.getZ() + dz + 0.5);
				Component clue = QuestEvents.miraClue(p, soul);
				log("mira: soul " + dz + " blocks " + (dz > 0 ? "behind" : "ahead of") + " her says: " + (clue == null ? "null" : clue.getString()));
			}
			p.setAttached(ModAttachments.QUESTS, Map.of());
		});
		server.runOnServer(s -> {
			ServerLevel uw = s.getLevel(UnderworldTravel.UNDERWORLD);
			BlockPos m = QuestEvents.miraSpot(uw);
			s.getCommands().performPrefixedCommand(s.createCommandSourceStack(),
				"execute in deathbound:underworld run tp @a " + (m.getX() + (m.getX() < 0 ? 5 : -5) + 0.5) + " " + (m.getY() + 2) + " " + (m.getZ() + 6.5) + " facing " + (m.getX() + 0.5) + " " + (m.getY() + 0.8) + " " + (m.getZ() + 0.5));
		});
		settle(ctx, world, 30);
		ctx.takeScreenshot("new_mira_in_line");

		// ---------------------------------------------------------------- Sentry's tag: the ambush on the broken bridge
		int ax = Layout.WATCH.x() - Layout.WATCH.radius() + 2, az = Layout.WATCH.z();
		int bx = Layout.HOLLOW.x() + Layout.HOLLOW.radius() - 3, bz = Layout.HOLLOW.z() + 6;
		int n = Math.abs(ax - bx), i = n / 2;
		double t = (double) i / n;
		int midX = ax - i, midZ = (int) Math.round(az + (bz - az) * t);
		int midY = (int) Math.round(Layout.WATCH.top() + (Layout.HOLLOW.top() - Layout.WATCH.top()) * t) - (int) Math.round(Math.sin(t * Math.PI) * 3.0);
		server.runCommand("gamemode survival @a");
		tp(server, (midX + 0.5) + " " + (midY + 1) + " " + (midZ + 0.5) + " facing " + bx + " " + (Layout.HOLLOW.top() + 6) + " " + bz);
		settle(ctx, world, 60);
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			log("ambush: standing on " + p.level().getBlockState(p.blockPosition().below()) + " at " + p.blockPosition().toShortString());
			p.setAttached(ModAttachments.QUESTS, Map.of("name", 1));
			p.getInventory().add(new ItemStack(ModItems.SENTRYS_TAG));
		});
		ctx.waitTicks(95);
		ctx.takeScreenshot("new_ambush");
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			int arrows = p.level().getEntitiesOfClass(HunterArrow.class, new AABB(p.blockPosition()).inflate(60)).size();
			boolean marked = p.hasEffect(ModEffects.MARKED);
			log("ambush: arrows from the Hollow=" + arrows + " marked=" + marked + " -> " + pass(arrows > 0));
		});
		tp(server, (ax + 0.5) + " " + (Layout.WATCH.top() + 1) + " " + (az + 0.5));
		ctx.waitTicks(20);
		server.runOnServer(s -> player(s).level().getEntitiesOfClass(HunterArrow.class, new AABB(player(s).blockPosition()).inflate(80)).forEach(HunterArrow::discard));
		ctx.waitTicks(80);
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			int arrows = p.level().getEntitiesOfClass(HunterArrow.class, new AABB(p.blockPosition()).inflate(80)).size();
			log("ambush: back at the Watch -> new arrows " + arrows + " -> " + pass(arrows == 0));
			for (int k = 0; k < p.getInventory().getContainerSize(); k++) {
				if (p.getInventory().getItem(k).is(ModItems.SENTRYS_TAG)) {
					p.getInventory().setItem(k, ItemStack.EMPTY);
				}
			}
			p.setAttached(ModAttachments.QUESTS, Map.of());
			p.removeEffect(ModEffects.MARKED);
		});

		// ---------------------------------------------------------------- Ferryman's oar: roots, then the wood wakes
		int x0 = Layout.FOREST.x() + 6, z0 = Layout.FOREST.z() + 15;
		server.runCommand("gamemode creative @a");
		tp(server, (x0 + 4.5) + " " + (Layout.FOREST.top() + 4) + " " + (z0 + 7.5) + " facing " + (x0 + 1.5) + " " + (Layout.FOREST.top()) + " " + (z0 + 2.5));
		settle(ctx, world, 80);
		ctx.takeScreenshot("new_shed_roots");
		server.runCommand("gamemode survival @a");
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			p.setAttached(ModAttachments.QUESTS, Map.of("oar", 1));
			p.getInventory().add(new ItemStack(ModItems.FERRYMANS_OAR));
		});
		ctx.waitTicks(150);
		server.runOnServer(s -> {
			ServerPlayer p = player(s);
			int risen = p.level().getEntitiesOfClass(LivingEntity.class, new AABB(p.blockPosition()).inflate(24),
				e -> e instanceof Gravebound || e instanceof SoulWisp).size();
			log("ghostwood: things risen around the player=" + risen + " -> " + pass(risen > 0));
		});
		ctx.takeScreenshot("new_wood_wakes");
		tp(server, (Layout.FOREST.x() + Layout.FOREST.radius() + 14) + " " + (Layout.FOREST.top() + 2) + " " + Layout.FOREST.z());
		ctx.waitTicks(30);
		server.runOnServer(s -> {
			ServerLevel uw = s.getLevel(UnderworldTravel.UNDERWORLD);
			int left = uw.getEntitiesOfClass(LivingEntity.class, new AABB(Layout.FOREST.x(), Layout.FOREST.top(), Layout.FOREST.z(), Layout.FOREST.x(), Layout.FOREST.top(), Layout.FOREST.z()).inflate(60),
				e -> e instanceof Gravebound || e instanceof SoulWisp).size();
			log("ghostwood: out of the trees -> risen left=" + left + " -> " + pass(left == 0));
		});
		log("done");
	}

	private NewWorkChecks() {
	}
}
