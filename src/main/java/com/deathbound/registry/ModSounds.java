package com.deathbound.registry;

import com.deathbound.DeathBound;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
   public static final SoundEvent WHISPER = register("dread.whisper");
   public static final SoundEvent MURMUR = register("npc.murmur");
   public static final SoundEvent FOOTSTEP = register("dread.footstep");
   public static final SoundEvent TWITCH = register("dread.twitch");
   public static final SoundEvent TOLL = register("dread.toll");
   public static final SoundEvent HEARTBEAT = register("dread.heartbeat");
   public static final SoundEvent WAIL = register("dread.wail");
   public static final SoundEvent AIR = register("dread.air");
   public static final SoundEvent MUSIC_GUARD = register("music.guard");
   public static final SoundEvent MUSIC_DEATH = register("music.death");
   public static final SoundEvent MUSIC_DEATH_INTRO = register("music.death_intro");
   public static final SoundEvent MUSIC_BEAST = register("music.beast");
   public static final SoundEvent MUSIC_REAPER = register("music.reaper");
   public static final SoundEvent MUSIC_HUNTER = register("music.hunter");
   public static final SoundEvent MUSIC_THRONE = register("music.throne");
   public static final SoundEvent MUSIC_SILENCE = register("music.silence");

   private static SoundEvent register(String name) {
      return Registry.register(BuiltInRegistries.SOUND_EVENT, DeathBound.id(name), SoundEvent.createVariableRangeEvent(DeathBound.id(name)));
   }

   public static void init() {
   }

   private ModSounds() {
   }
}
