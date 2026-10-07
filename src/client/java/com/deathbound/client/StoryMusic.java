package com.deathbound.client;

import com.deathbound.world.Layout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class StoryMusic {
   private static int track = 0;

   public static void set(int t) {
      if (t != track && t != 0) {
         Minecraft.getInstance().getMusicManager().stopPlaying();
      }

      track = t;
   }

   public static int now() {
      LocalPlayer p = Minecraft.getInstance().player;
      if (p != null && track != 0) {
         return track == 1 && p.blockPosition().distSqr(Layout.ARENA_CENTER) > 12100.0 ? 0 : track;
      } else {
         return 0;
      }
   }

   private StoryMusic() {
   }
}
