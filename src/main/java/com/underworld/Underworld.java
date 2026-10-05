package com.underworld;

import com.underworld.command.UnderworldCommands;
import com.underworld.entity.collector.Collector;
import com.underworld.registry.ModAttachments;
import com.underworld.registry.ModBlocks;
import com.underworld.registry.ModEffects;
import com.underworld.registry.ModEntities;
import com.underworld.registry.ModItems;
import com.underworld.registry.ModSounds;
import com.underworld.world.StructureBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Interaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Underworld implements ModInitializer {
	public static final String MOD_ID = "underworld";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final DustParticleOptions MARK_DUST = new DustParticleOptions(0xC084FC, 0.9F);

	@Override
	public void onInitialize() {
		ModSounds.init();
		ModEffects.init();
		ModEntities.init();
		ModItems.init();
		ModBlocks.init();
		ModAttachments.init();

		CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> UnderworldCommands.register(dispatcher));
		ServerTickEvents.END_LEVEL_TICK.register(Underworld::showMarks);
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (entity instanceof Interaction i && i.entityTags().contains(StructureBuilder.RELIC_TAG)) {
				if (!level.isClientSide()) {
					level.getEntitiesOfClass(Collector.class, i.getBoundingBox().inflate(20)).stream().findFirst()
						.ifPresent(c -> c.onRelicTouched(player));
				}
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		});
	}

	/** Marked players get a slowly spinning purple ring over their head and a short action-bar warning. */
	private static void showMarks(ServerLevel level) {
		long t = level.getGameTime();
		if (t % 3 != 0) {
			return;
		}
		for (ServerPlayer p : level.players()) {
			if (!p.hasEffect(ModEffects.HOLLOW_MARK)) {
				continue;
			}
			double y = p.getY() + p.getBbHeight() + 0.45;
			for (int i = 0; i < 4; i++) {
				double a = t * 0.25 + i * Math.PI / 2;
				level.sendParticles(MARK_DUST, p.getX() + Math.cos(a) * 0.45, y, p.getZ() + Math.sin(a) * 0.45, 1, 0, 0, 0, 0);
			}
			if (t % 30 == 0) {
				p.sendSystemMessage(Component.literal("◆ MARKED ◆").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), true);
			}
		}
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
