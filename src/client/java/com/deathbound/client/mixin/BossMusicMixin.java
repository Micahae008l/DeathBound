package com.deathbound.client.mixin;

import com.deathbound.client.StoryMusic;
import com.deathbound.entity.DeathEntity;
import com.deathbound.entity.HollowHunter;
import com.deathbound.registry.ModSounds;
import com.deathbound.world.UnderworldTravel;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
abstract class BossMusicMixin {
   @Unique
   private static final Music GUARD = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.MUSIC_GUARD), 0, 0, true);
   @Unique
   private static final Music DEATH_INTRO = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.MUSIC_DEATH_INTRO), 0, 0, true);
   @Unique
   private static final Music DEATH = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.MUSIC_DEATH), 0, 0, true);
   @Unique
   private static final Music DEATH_AFTER_INTRO = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.MUSIC_DEATH), 0, 0, false);
   @Unique
   private static final Music HUNTER = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.MUSIC_HUNTER), 0, 0, true);
   @Unique
   private static final Music THRONE = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.MUSIC_THRONE), 0, 0, true);
   @Unique
   private static final Music SILENCE = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.MUSIC_SILENCE), 0, 0, true);
   @Unique
   private static boolean deathbound$introHeard;

   @Inject(method = "getSituationalMusic", at = @At("HEAD"), cancellable = true)
   private void deathbound$bossMusic(CallbackInfoReturnable<Music> cir) {
      Minecraft mc = (Minecraft)(Object)this;
      int story = StoryMusic.now();
      if (story != 0) {
         cir.setReturnValue(story == 1 ? THRONE : SILENCE);
      } else {
         if (mc.player != null && UnderworldTravel.inUnderworld(mc.player) && mc.gui.hud.getBossOverlay().shouldPlayMusic()) {
            if (mc.gui.screen() != null) {
               return;   // a screen open mid-fight: vanilla picks for now, and the fight keeps its place in the music
            }

            List<DeathEntity> deaths = mc.player.level().getEntitiesOfClass(DeathEntity.class, mc.player.getBoundingBox().inflate(80.0));
            if (!mc.player.level().getEntitiesOfClass(HollowHunter.class, mc.player.getBoundingBox().inflate(80.0)).isEmpty()) {
               deathbound$introHeard = false;
               cir.setReturnValue(HUNTER);
            } else if (deaths.isEmpty()) {
               deathbound$introHeard = false;
               cir.setReturnValue(GUARD);
            } else {
               cir.setReturnValue(deathbound$deathKing(mc.getMusicManager()));
            }
         } else {
            deathbound$introHeard = false;
         }
      }
   }

   // Death's Requiem: the intro plays once when the fight starts, then the loop repeats until it ends.
   // While the intro is still playing the loop is asked for without replacing, so it starts the moment the intro finishes.
   @Unique
   private static Music deathbound$deathKing(MusicManager music) {
      if (music.isPlayingMusic(DEATH_INTRO)) {
         deathbound$introHeard = true;
         return DEATH_AFTER_INTRO;
      } else {
         return deathbound$introHeard || music.isPlayingMusic(DEATH) ? DEATH : DEATH_INTRO;
      }
   }
}
