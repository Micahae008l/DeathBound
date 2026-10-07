package com.deathbound;

import com.deathbound.charm.Charms;
import com.deathbound.entity.Hazards;
import com.deathbound.entity.Speech;
import com.deathbound.npc.Handiwork;
import com.deathbound.npc.Temper;
import com.deathbound.registry.ModAttachments;
import com.deathbound.registry.ModBlocks;
import com.deathbound.registry.ModEffects;
import com.deathbound.registry.ModEntities;
import com.deathbound.registry.ModFluids;
import com.deathbound.registry.ModItems;
import com.deathbound.registry.ModMenus;
import com.deathbound.registry.ModNet;
import com.deathbound.registry.ModParticles;
import com.deathbound.registry.ModSounds;
import com.deathbound.registry.ModWorldgen;
import com.deathbound.world.DebugCommand;
import com.deathbound.world.Director;
import com.deathbound.world.Dread;
import com.deathbound.world.QuestEvents;
import com.deathbound.world.Endings;
import com.deathbound.world.LootInjection;
import com.deathbound.world.Puzzles;
import com.deathbound.world.UnderworldTravel;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathBound implements ModInitializer {
   public static final String MOD_ID = "deathbound";
   public static final Logger LOG = LoggerFactory.getLogger("deathbound");

   public static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("deathbound", path);
   }

   public void onInitialize() {
      ModParticles.init();
      ModSounds.init();
      ModFluids.init();
      ModBlocks.init();
      ModEntities.init();
      ModItems.init();
      ModEffects.init();
      ModMenus.init();
      ModAttachments.init();
      ModNet.init();
      ModWorldgen.init();
      UnderworldTravel.init();
      Charms.init();
      Director.init();
      Dread.init();
      QuestEvents.init();
      Puzzles.init();
      com.deathbound.item.LoreFiling.init();
      Endings.init();
      Speech.init();
      Hazards.init();
      DebugCommand.init();
      LootInjection.init();
      Temper.init();
      Handiwork.init();
   }
}
