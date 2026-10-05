package com.underworld.registry;

import com.mojang.serialization.Codec;
import com.underworld.Underworld;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class ModAttachments {
	/**
	 * Per-player record of what the Collector knows about them: artifact ids handed in, plus
	 * one-off flags such as "met", "seen_hunter_arrow", "seen_hunters_eye", "relic_1".
	 */
	@SuppressWarnings("UnstableApiUsage")
	public static final AttachmentType<List<String>> COLLECTOR_RECORD = AttachmentRegistry.create(
		Underworld.id("collector_record"),
		builder -> builder.persistent(Codec.STRING.listOf()).copyOnDeath().initializer(List::of)
	);

	/** Where /underworld setup built things in this level: "hunter_grounds", "collector_room". */
	@SuppressWarnings("UnstableApiUsage")
	public static final AttachmentType<Map<String, BlockPos>> SITES = AttachmentRegistry.create(
		Underworld.id("sites"),
		builder -> builder.persistent(Codec.unboundedMap(Codec.STRING, BlockPos.CODEC)).initializer(Map::of)
	);

	private ModAttachments() {
	}

	public static void init() {
	}
}
