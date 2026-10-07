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

   public static void init() {
      // favors finished before the advancements existed count once the player comes back
      net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
         ServerPlayer p = handler.getPlayer();
         for (com.deathbound.npc.Quests.Quest q : com.deathbound.npc.Quests.ALL) {
            if (com.deathbound.npc.Quests.stage(p, q.id()) >= q.last()) {
               award(p, "errand");
               award(p, "every_errand", q.id());
            }
         }
      });
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
