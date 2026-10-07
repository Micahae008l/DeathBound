package com.deathbound.client.mixin;

import com.deathbound.client.StoryMusic;
import com.deathbound.entity.DeathEntity;
import com.deathbound.entity.HollowHunter;
import com.deathbound.registry.ModSounds;
import com.deathbound.world.UnderworldTravel;
import java.util.List;
import net.minecraft.client.Minecraft;
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
   private static final Music DEATH = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.MUSIC_DEATH), 0, 0, true);
   @Unique
   private static final Music HUNTER = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.MUSIC_HUNTER), 0, 0, true);
   @Unique
   private static final Music BEAST = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.MUSIC_BEAST), 0, 0, true);
   @Unique
   private static final Music REAPER = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.MUSIC_REAPER), 0, 0, true);
   @Unique
   private static final Music THRONE = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.MUSIC_THRONE), 0, 0, true);
   @Unique
   private static final Music SILENCE = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.MUSIC_SILENCE), 0, 0, true);

   @Inject(method = "getSituationalMusic", at = @At("HEAD"), cancellable = true)
   private void deathbound$bossMusic(CallbackInfoReturnable<Music> cir) {
      Minecraft mc = (Minecraft)(Object)this;
      int story = StoryMusic.now();
      if (story != 0) {
         cir.setReturnValue(story == 1 ? THRONE : SILENCE);
      } else {
         if (mc.player != null && mc.gui.screen() == null && UnderworldTravel.inUnderworld(mc.player) && mc.gui.hud.getBossOverlay().shouldPlayMusic()) {
            List<DeathEntity> deaths = mc.player.level().getEntitiesOfClass(DeathEntity.class, mc.player.getBoundingBox().inflate(80.0));
            if (!mc.player.level().getEntitiesOfClass(HollowHunter.class, mc.player.getBoundingBox().inflate(80.0)).isEmpty()) {
               cir.setReturnValue(HUNTER);
            } else if (deaths.isEmpty()) {
               cir.setReturnValue(GUARD);
            } else {
               int phase = deaths.getFirst().phase();
               cir.setReturnValue(phase == 3 ? BEAST : (phase == 2 ? REAPER : DEATH));
            }
         }
      }
   }
}
