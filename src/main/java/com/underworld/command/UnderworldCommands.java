package com.underworld.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.underworld.entity.hunter.HollowHunter;
import com.underworld.registry.ModAttachments;
import com.underworld.registry.ModItems;
import com.underworld.world.StructureBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/**
 * Test helpers. Everything lives under /underworld:
 * <pre>
 * /underworld setup                 build the Hunter's Grounds + Collection Room next to you (void-world friendly)
 * /underworld goto hunter|collector
 * /underworld kit                   survival test gear
 * /underworld artifacts             all five artifacts + Hollow Arrow + Hunter's Eye
 * /underworld hunter start|reset|phase &lt;1-3&gt;
 * /underworld hunter showcase &lt;1-3&gt; idle|draw|summon|rain|slash|last_hunt|kneel|intro|roar
 * /underworld collector forget      wipe what the Collector remembers about you
 * </pre>
 */
public final class UnderworldCommands {
	private static final String HUNTER = "hunter_grounds";
	private static final String COLLECTOR = "collector_room";

	private UnderworldCommands() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("underworld")
			.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
			.then(Commands.literal("setup").executes(UnderworldCommands::setup))
			.then(Commands.literal("goto")
				.then(Commands.literal("hunter").executes(c -> goTo(c, HUNTER)))
				.then(Commands.literal("collector").executes(c -> goTo(c, COLLECTOR))))
			.then(Commands.literal("kit").executes(UnderworldCommands::kit))
			.then(Commands.literal("artifacts").executes(UnderworldCommands::artifacts))
			.then(Commands.literal("hunter")
				.then(Commands.literal("start").executes(UnderworldCommands::hunterStart))
				.then(Commands.literal("reset").executes(UnderworldCommands::hunterReset))
				.then(Commands.literal("phase").then(Commands.argument("phase", IntegerArgumentType.integer(1, 3)).executes(UnderworldCommands::hunterPhase)))
				.then(Commands.literal("volley").executes(c -> {
					HollowHunter h = findHunter(c);
					if (h == null) return 0;
					h.debugVolley(c.getSource().getLevel(), c.getSource().getPlayerOrException());
					return 1;
				}))
				.then(showcase()))
			.then(Commands.literal("collector")
				.then(Commands.literal("forget").executes(UnderworldCommands::collectorForget))))
		;
	}

	private static int setup(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = c.getSource().getPlayerOrException();
		ServerLevel level = c.getSource().getLevel();
		BlockPos at = player.blockPosition();

		BlockPos arena = at.offset(0, 0, -34);
		BlockPos room = at.offset(48, 0, 0);
		// stone walkways from where you stand to the arena entrance and to the room's secret passage
		for (int dz = 0; dz >= -12; dz--) {
			for (int dx = -1; dx <= 1; dx++) {
				level.setBlockAndUpdate(at.offset(dx, -1, dz), Blocks.COBBLED_DEEPSLATE.defaultBlockState());
			}
		}
		for (int dx = 0; dx <= 48; dx++) {
			level.setBlockAndUpdate(at.offset(dx, -1, 11), Blocks.COBBLED_DEEPSLATE.defaultBlockState());
		}
		for (int dz = 0; dz <= 11; dz++) {
			level.setBlockAndUpdate(at.offset(0, -1, dz), Blocks.COBBLED_DEEPSLATE.defaultBlockState());
		}

		StructureBuilder.buildHuntersGrounds(level, arena);
		StructureBuilder.buildCollectionRoom(level, room);

		Map<String, BlockPos> sites = new HashMap<>(level.getAttachedOrElse(ModAttachments.SITES, Map.of()));
		sites.put(HUNTER, arena);
		sites.put(COLLECTOR, room);
		level.setAttached(ModAttachments.SITES, Map.copyOf(sites));

		c.getSource().sendSuccess(() -> Component.literal("Built the Hunter's Grounds (north) and the Collection Room (east, through the passage). ")
			.append(Component.literal("The Hunter only wakes for survival players - try /underworld kit, /gamemode survival, then walk north.").withStyle(ChatFormatting.GRAY)), false);
		return 1;
	}

	private static int goTo(CommandContext<CommandSourceStack> c, String site) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = c.getSource().getPlayerOrException();
		ServerLevel level = c.getSource().getLevel();
		BlockPos pos = level.getAttachedOrElse(ModAttachments.SITES, Map.of()).get(site);
		if (pos == null) {
			c.getSource().sendFailure(Component.literal("Nothing built yet - run /underworld setup first."));
			return 0;
		}
		BlockPos dest = site.equals(HUNTER) ? pos.offset(0, 0, HollowHunter.ARENA_RADIUS + 4) : pos.offset(0, 0, 11);
		float yaw = 180.0F;
		player.teleportTo(level, dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5, java.util.Set.of(), yaw, 0.0F, true);
		return 1;
	}

	private static int kit(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer p = c.getSource().getPlayerOrException();
		p.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
		p.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
		p.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
		p.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
		p.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
		for (ItemStack s : List.of(new ItemStack(Items.DIAMOND_SWORD), new ItemStack(Items.BOW), new ItemStack(Items.ARROW, 64),
			new ItemStack(Items.COOKED_BEEF, 32), new ItemStack(Items.GOLDEN_APPLE, 4), new ItemStack(Items.ENDER_PEARL, 8))) {
			p.getInventory().add(s);
		}
		c.getSource().sendSuccess(() -> Component.literal("Kit given."), false);
		return 1;
	}

	private static int artifacts(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer p = c.getSource().getPlayerOrException();
		for (var item : ModItems.ARTIFACTS) {
			p.getInventory().add(new ItemStack(item));
		}
		p.getInventory().add(new ItemStack(ModItems.HOLLOW_ARROW, 16));
		p.getInventory().add(new ItemStack(ModItems.HUNTERS_EYE));
		p.getInventory().add(new ItemStack(ModItems.HOLLOW_BOW));
		p.getInventory().add(new ItemStack(Items.EMERALD, 64));
		return 1;
	}

	private static HollowHunter findHunter(CommandContext<CommandSourceStack> c) {
		ServerLevel level = c.getSource().getLevel();
		List<HollowHunter> list = level.getEntitiesOfClass(HollowHunter.class, new AABB(BlockPos.containing(c.getSource().getPosition())).inflate(96), h -> h.isAlive());
		if (list.isEmpty()) {
			c.getSource().sendFailure(Component.literal("No Hollow Hunter nearby."));
			return null;
		}
		return list.getFirst();
	}

	private static int hunterStart(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		HollowHunter h = findHunter(c);
		if (h == null) return 0;
		h.debugStart(c.getSource().getLevel(), c.getSource().getPlayerOrException());
		return 1;
	}

	private static int hunterReset(CommandContext<CommandSourceStack> c) {
		HollowHunter h = findHunter(c);
		if (h == null) return 0;
		h.resetFight(c.getSource().getLevel());
		return 1;
	}

	private static int hunterPhase(CommandContext<CommandSourceStack> c) {
		HollowHunter h = findHunter(c);
		if (h == null) return 0;
		h.debugForcePhase(c.getSource().getLevel(), IntegerArgumentType.getInteger(c, "phase"));
		return 1;
	}

	private static final String[] POSES = {"idle", "draw", "summon", "rain", "slash", "last_hunt", "kneel", "intro", "roar", "release"};

	private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> showcase() {
		var phaseArg = Commands.argument("phase", IntegerArgumentType.integer(1, 3));
		for (int i = 0; i < POSES.length; i++) {
			final int anim = i;
			phaseArg.then(Commands.literal(POSES[i]).executes(c -> {
				HollowHunter h = findHunter(c);
				if (h == null) return 0;
				h.debugShowcase(IntegerArgumentType.getInteger(c, "phase"), anim);
				return 1;
			}));
		}
		return Commands.literal("showcase").then(phaseArg);
	}

	private static int collectorForget(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer p = c.getSource().getPlayerOrException();
		p.removeAttached(ModAttachments.COLLECTOR_RECORD);
		c.getSource().sendSuccess(() -> Component.literal("The Collector has forgotten you."), false);
		return 1;
	}
}
