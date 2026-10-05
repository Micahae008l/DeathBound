package com.underworld.registry;

import com.underworld.Underworld;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * The Hollow Bow does not sound like a normal bow. Each event is a re-pitched blend of vanilla audio
 * (see assets/underworld/sounds.json); the code layers two or three of them at once.
 * Drop real .ogg files into assets/underworld/sounds/ later and point sounds.json at them.
 */
public final class ModSounds {
	public static final SoundEvent HUNTER_ARROW_GRAB = register("entity.hollow_hunter.arrow_grab");
	public static final SoundEvent HUNTER_BOW_DRAW = register("entity.hollow_hunter.bow_draw");
	public static final SoundEvent HUNTER_BOW_SHOOT = register("entity.hollow_hunter.bow_shoot");
	public static final SoundEvent HUNTER_BOW_WHISPER = register("entity.hollow_hunter.bow_whisper");
	public static final SoundEvent HOLLOW_BOW_SHOOT = register("item.hollow_bow.shoot");

	private ModSounds() {
	}

	private static SoundEvent register(String name) {
		Identifier id = Underworld.id(name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void init() {
	}
}
