package com.underworld.test;

import com.underworld.entity.collector.Collector;
import com.underworld.registry.ModItems;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Boots the client, builds both locations high in the sky (void-like), and screenshots the characters
 * in their key poses, a scripted Collector conversation, a short live fight and the death sequence.
 * Run with: ./gradlew runClientGameTest   - screenshots land in build/run/clientGameTest/screenshots.
 */
public class ShowcaseClientTest implements FabricClientGameTest {
	private static final String AT_HUNTER = "execute as @p at @p run underworld hunter ";

	@Override
	public void runTest(ClientGameTestContext ctx) {
		try (TestSingleplayerContext world = ctx.worldBuilder().create()) {
			TestServerContext server = world.getServer();
			world.getConnection().waitForChunksRender();
			ctx.runOnClient(mc -> mc.gui.hud.toggle());
			server.runCommand("time set noon");
			server.runCommand("gamerule advance_time false");
			server.runCommand("gamerule advance_weather false");
			server.runCommand("weather clear");
			server.runCommand("gamemode creative @a");
			server.runCommand("tp @a 0 150 0 180 0");
			ctx.waitTicks(5);
			server.runCommand("execute as @p at @p run underworld setup");
			ctx.waitTicks(20);

			if (System.getenv("UW_QUICK") != null) {
				System.setProperty("underworld.debugBolts", "true");
				server.runCommand("gamemode spectator @a");
				server.runCommand(AT_HUNTER + "showcase 2 idle");
				server.runCommand("tp @a 0.5 154.5 -43.5 180 10");
				ctx.waitTicks(5);
				server.runCommand(AT_HUNTER + "volley");
				ctx.waitTicks(8);
				ctx.takeScreenshot("quick_volley");
				server.runOnServer(s -> s.overworld().getEntities(com.underworld.registry.ModEntities.SPECTRAL_BOLT, e -> true)
					.forEach(b -> System.out.println("[UW-TEST] server bolt " + b.position() + " rot " + b.getYRot() + "/" + b.getXRot())));
				ctx.runOnClient(mc -> mc.level.entitiesForRendering().forEach(e -> {
					if (e instanceof com.underworld.entity.hunter.SpectralBolt b) {
						System.out.println("[UW-TEST] client bolt " + b.position() + " invisible=" + b.isInvisible());
					}
				}));
				return;
			}

			// ---------------------------------------------------------------- Hunter turntable (daylight)
			server.runCommand("gamemode spectator @a");
			server.runCommand(AT_HUNTER + "showcase 1 idle");
			// hunter stands at 0.5 152 -47.5
			shot(ctx, world, server, "tp @a 0.5 153.2 -42.5 180 2", "day_hunter_front", 30);
			shot(ctx, world, server, "tp @a 4.0 153.2 -44.0 135 2", "day_hunter_34", 30);
			shot(ctx, world, server, "tp @a 5.5 153.2 -47.5 90 2", "day_hunter_left", 30);
			shot(ctx, world, server, "tp @a 0.5 153.2 -52.5 0 2", "day_hunter_back", 30);
			shot(ctx, world, server, "tp @a -4.5 153.2 -47.5 -90 2", "day_hunter_right", 30);
			// the shot cycle, frame by frame (phase 1: 24 ticks to full draw)
			server.runCommand("tp @a 5.0 153.4 -43.0 140 4");
			ctx.waitTicks(10);
			server.runCommand(AT_HUNTER + "showcase 1 draw");
			int[] frames = {3, 6, 9, 12, 15, 19, 24};
			int prev = 0;
			for (int f : frames) {
				ctx.waitTicks(f - prev);
				prev = f;
				ctx.takeScreenshot("shot_t" + f);
			}
			server.runCommand(AT_HUNTER + "showcase 1 release");
			ctx.waitTicks(2);
			ctx.takeScreenshot("shot_release_2");
			ctx.waitTicks(4);
			ctx.takeScreenshot("shot_release_6");
			server.runCommand(AT_HUNTER + "showcase 2 draw");
			shot(ctx, world, server, "tp @a 0.5 153.2 -40.5 180 0", "day_draw_front", 40);
			shot(ctx, world, server, "tp @a 7.0 153.2 -47.0 90 0", "day_draw_side", 10);
			shot(ctx, world, server, "tp @a -5.0 153.2 -45.0 -115 0", "day_draw_side2", 10);
			// volley hovering behind him, heads aimed at the camera
			server.runCommand(AT_HUNTER + "showcase 3 summon");
			server.runCommand("tp @a 0.5 153.6 -39.5 180 -8");
			ctx.waitTicks(5);
			server.runCommand(AT_HUNTER + "volley");
			ctx.waitTicks(12);
			ctx.takeScreenshot("volley_hover");
			server.runCommand("tp @a 6.5 153.6 -41.5 135 -8");
			ctx.waitTicks(3);
			ctx.takeScreenshot("volley_hover_side");
			ctx.waitTicks(16);
			ctx.takeScreenshot("volley_launch");
			server.runCommand(AT_HUNTER + "showcase 2 summon");
			shot(ctx, world, server, "tp @a 0.5 153.2 -41.5 180 0", "day_summon", 30);
			server.runCommand(AT_HUNTER + "showcase 3 last_hunt");
			shot(ctx, world, server, "tp @a 0.5 153.2 -41.5 180 0", "day_last_hunt", 30);
			server.runCommand(AT_HUNTER + "showcase 3 kneel");
			shot(ctx, world, server, "tp @a 3.5 152.6 -43.5 145 5", "day_kneel", 30);
			server.runCommand(AT_HUNTER + "showcase 3 rain");
			shot(ctx, world, server, "tp @a 5.0 153.2 -44.0 130 -15", "day_rain", 30);

			// ---------------------------------------------------------------- Collector (creative so he looks at you)
			server.runCommand("gamemode creative @a");
			shot(ctx, world, server, "tp @a 48.5 151.1 2.2 180 10", "day_collector_front", 50);
			shot(ctx, world, server, "tp @a 51.0 151.1 1.6 130 12", "day_collector_34", 30);
			shot(ctx, world, server, "tp @a 48.5 153.5 2.9 180 28", "day_collection_room", 20);
			shot(ctx, world, server, "tp @a 46.0 152.0 2.5 220 15", "day_collection_room_west", 20);
			shot(ctx, world, server, "tp @a 51.0 152.0 2.5 140 15", "day_collection_room_east", 20);
			this.collectorConversation(ctx, world, server);

			// ---------------------------------------------------------------- night atmosphere
			server.runCommand("time set midnight");
			server.runCommand("gamemode spectator @a");
			server.runCommand(AT_HUNTER + "showcase 2 idle");
			shot(ctx, world, server, "tp @a 0.5 153.0 -41.0 180 -2", "night_hunter_p2", 30);
			server.runCommand(AT_HUNTER + "showcase 3 last_hunt");
			shot(ctx, world, server, "tp @a 0.5 153.0 -40.0 180 -4", "night_hunter_p3", 30);
			shot(ctx, world, server, "tp @a 0.5 168 -8 180 38", "night_arena_overview", 30);

			// ---------------------------------------------------------------- live fight (player can't die)
			server.runCommand(AT_HUNTER + "reset");
			server.runCommand("time set noon");
			server.runCommand("gamemode survival @a");
			server.runCommand("effect give @a resistance infinite 4 true");
			server.runCommand("effect give @a regeneration infinite 4 true");
			server.runCommand("tp @a 0.5 150 -24 180 -8");
			ctx.waitTicks(30);
			ctx.takeScreenshot("fight_intro_a");
			ctx.waitTicks(40);
			ctx.takeScreenshot("fight_intro_b");
			for (int i = 0; i < 14; i++) {
				ctx.waitTicks(i < 4 ? 20 : 30);
				server.runCommand("execute as @p at @p facing entity @e[type=underworld:hollow_hunter,limit=1] eyes run tp @s ~ ~ ~ ~ ~");
				ctx.waitTicks(2);
				ctx.takeScreenshot("fight_" + i);
			}
			server.runCommand(AT_HUNTER + "phase 2");
			for (int i = 0; i < 6; i++) {
				ctx.waitTicks(30);
				server.runCommand("execute as @p at @p facing entity @e[type=underworld:hollow_hunter,limit=1] eyes run tp @s ~ ~ ~ ~ ~");
				ctx.waitTicks(2);
				ctx.takeScreenshot("fight_p2_" + i);
			}
			server.runCommand(AT_HUNTER + "phase 3");
			for (int i = 0; i < 10; i++) {
				ctx.waitTicks(i < 6 ? 12 : 25);
				server.runCommand("execute as @p at @p facing entity @e[type=underworld:hollow_hunter,limit=1] eyes run tp @s ~ ~ ~ ~ ~");
				ctx.waitTicks(1);
				ctx.takeScreenshot("fight_p3_" + i);
			}

			// ---------------------------------------------------------------- death sequence
			server.runCommand("gamemode spectator @a");
			server.runCommand(AT_HUNTER + "showcase 3 idle");
			server.runCommand("execute as @e[type=underworld:hollow_hunter] at @s run tp @a ~ ~1.6 ~6 180 5");
			ctx.waitTicks(20);
			server.runCommand("kill @e[type=underworld:hollow_hunter]");
			int elapsed = 0;
			for (int t : new int[]{8, 20, 32, 44, 56, 66, 76}) {
				ctx.waitTicks(t - elapsed);
				elapsed = t;
				ctx.takeScreenshot("death_" + t);
			}
			ctx.waitTicks(20);
			server.runCommand("execute as @p at @p run tp @a ~ ~1 ~-1 180 50");
			ctx.waitTicks(5);
			ctx.takeScreenshot("death_loot");
		}
	}

	private void collectorConversation(ClientGameTestContext ctx, TestSingleplayerContext world, TestServerContext server) {
		server.runCommand("tp @a 48.5 151.0 1.6 180 15");
		ctx.waitTicks(10);
		interact(server, null);                       // intro: 3 lines
		ctx.waitTicks(100);
		interact(server, ModItems.WARDENS_SEAL);      // artifact -> reaction + reward
		ctx.waitTicks(30);
		ctx.takeScreenshot("day_collector_inspecting");
		ctx.waitTicks(80);
		interact(server, ModItems.WARDENS_SEAL);      // duplicate
		ctx.waitTicks(60);
		interact(server, ModItems.HOLLOW_ARROW);      // "You found one of his arrows."
		ctx.waitTicks(60);
		interact(server, ModItems.HUNTERS_EYE);       // "You actually found him."
		ctx.waitTicks(110);
		for (Item artifact : List.of(ModItems.BROKEN_SOUL_LANTERN, ModItems.ANCIENT_DEATH_COIN, ModItems.HOLLOW_CRYSTAL, ModItems.FORGOTTEN_CROWN)) {
			interact(server, artifact);
			ctx.waitTicks(115);
		}
		ctx.waitTicks(120);                           // completion bonus
		server.runOnServer(s -> {
			ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
			collector(p).onRelicTouched(p);
		});
		ctx.waitTicks(20);
		server.runOnServer(s -> {
			ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
			collector(p).onRelicTouched(p);
		});
		ctx.waitTicks(20);
		server.runOnServer(s -> {                     // attack -> "You're making a mistake." + vanish
			ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
			Collector c = collector(p);
			c.hurtServer((ServerLevel) p.level(), p.damageSources().playerAttack(p), 4.0F);
		});
		ctx.waitTicks(10);
		ctx.takeScreenshot("day_collector_vanished");
		server.runOnServer(s -> {
			ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
			System.out.println("[UW-TEST] inventory after collector: " + p.getInventory().getNonEquipmentItems().stream()
				.filter(st -> !st.isEmpty()).map(st -> st.getCount() + "x" + st.getItem()).toList());
		});
	}

	private static void interact(TestServerContext server, Item held) {
		server.runOnServer(s -> {
			ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
			p.setItemInHand(InteractionHand.MAIN_HAND, held == null ? ItemStack.EMPTY : new ItemStack(held));
			Collector c = collector(p);
			System.out.println("[UW-TEST] interact holding " + held + " -> " + c.interact(p, InteractionHand.MAIN_HAND, c.position()));
		});
	}

	private static Collector collector(ServerPlayer p) {
		return p.level().getEntitiesOfClass(Collector.class, new AABB(p.blockPosition()).inflate(30)).getFirst();
	}

	private static void shot(ClientGameTestContext ctx, TestSingleplayerContext world, TestServerContext server, String tp, String name, int wait) {
		server.runCommand(tp);
		ctx.waitTicks(wait);
		world.getConnection().waitForChunksRender();
		ctx.takeScreenshot(name);
	}
}
