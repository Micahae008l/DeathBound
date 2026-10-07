package com.deathbound.world;

import com.deathbound.DeathBound;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Awards the DeathBound advancements (data/deathbound/advancement, built by tools/advancements.py) that have no vanilla
 * trigger: their criteria use minecraft:impossible and are granted from code when the moment happens.
 */
public final class Milestones {
   public static void award(ServerPlayer player, String id) {
      award(player, id, "done");
   }

   public static void award(ServerPlayer player, String id, String criterion) {
      AdvancementHolder adv = player.level().getServer().getAdvancements().get(DeathBound.id(id));
      if (adv != null) {
         player.getAdvancements().award(adv, criterion);
      }
   }

   /** Everyone who was there: boss kills and endings are shared by the players nearby. */
   public static void awardNear(ServerLevel level, Vec3 at, double radius, String id) {
      for (ServerPlayer p : level.getPlayers(p -> !p.isSpectator() && p.position().distanceTo(at) <= radius)) {
         award(p, id);
      }
   }

   private Milestones() {
   }
}
