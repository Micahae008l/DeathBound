package com.deathbound.npc;

import java.util.List;
import java.util.Map;

final class TalkData {
   static final Map<String, Talk.Npc> NPCS = Map.ofEntries(
      Map.entry(
         "ferryman",
         new Talk.Npc(
            "ferryman",
            List.of(
               new String[]{"end1", "ferryman.end1"},
               new String[]{"end2", "ferryman.end2"},
               new String[]{"end3", "ferryman.end3"},
               new String[]{"slain", "ferryman.slain"},
               new String[]{"warden", "ferryman.warden"},
               new String[]{"q:oar=2", "ferryman.oar"},
               new String[]{"", "ferryman"}
            ),
            List.of(
               new Talk.Topic("ferryman_who", "Who are you?", ""),
               new Talk.Topic("ferryman_where", "What is this place?", ""),
               new Talk.Topic("ferryman_river", "What happened to the river?", ""),
               new Talk.Topic("ferryman_others", "Who else came down alive?", ""),
               new Talk.Topic("ferryman_win", "What happens if I win?", "warden,!slain"),
               new Talk.Topic("ferryman_door", "What's behind the Door?", "warden"),
               new Talk.Topic("ferryman_silas", "Who was Silas?", "slain"),
               new Talk.Topic("ferryman_hunter", "The Hunter is dead.", "hunter")
            ),
            List.of(
               new Talk.Service("Do you have my things?", "custom:reclaim", ""),
               new Talk.Service("Show me what you sell.", "custom:trade", ""),
               new Talk.Service("Can I help you?", "custom:quest/ferryman", "q:oar=0"),
               new Talk.Service("About your oar...", "custom:quest/ferryman", "q:oar=1")
            ),
            List.of("Back again. The river's no wetter.", "*He leans on his pole and waits for you to speak.*", "Ask, then. I'm not going anywhere."),
            "*What do you want to know?*"
         )
      ),
      Map.entry(
         "gravedigger",
         new Talk.Npc(
            "gravedigger",
            List.of(
               new String[]{"end1", "gravedigger.end1"},
               new String[]{"end2", "gravedigger.end2"},
               new String[]{"end3", "gravedigger.end3"},
               new String[]{"slain,q:mira>=3", "gravedigger.slain_found"},
               new String[]{"slain", "gravedigger.slain"},
               new String[]{"q:mira>=3", "gravedigger.mira"},
               new String[]{"warden,q:mira>=3", "gravedigger.warden_found"},
               new String[]{"warden", "gravedigger.warden"},
               new String[]{"", "gravedigger"}
            ),
            List.of(
               new Talk.Topic("gravedigger_why", "Why are you down here?", "q:mira<3"),
               new Talk.Topic("gravedigger_chain", "Where did you get your chain?", ""),
               new Talk.Topic("gravedigger_village", "What happened to this village?", ""),
               new Talk.Topic("gravedigger_home", "Will you go home now?", "q:mira>=3"),
               new Talk.Topic("gravedigger_after", "What will you do now?", "slain")
            ),
            List.of(
               new Talk.Service("Forge my armor.", "dialog:gravedigger_forge", ""),
               new Talk.Service("Let's trade.", "custom:trade", ""),
               new Talk.Service("Can I help you find her?", "custom:quest/gravedigger", "q:mira=0"),
               new Talk.Service("About Mira...", "custom:quest/gravedigger", "q:mira=1"),
               new Talk.Service("Mira gave me something.", "custom:quest/gravedigger", "q:mira=2")
            ),
            List.of("Back again, friend? The forge is hot.", "*He wipes his hands on his apron.* What can I do for you?", "Mind the sparks."),
            "*What is it?*"
         )
      ),
      Map.entry(
         "prophet",
         new Talk.Npc(
            "prophet",
            List.of(
               new String[]{"end1", "prophet.end1"},
               new String[]{"end2", "prophet.end2"},
               new String[]{"end3", "prophet.end3"},
               new String[]{"slain", "prophet.slain"},
               new String[]{"warden", "prophet.warden"},
               new String[]{"", "prophet"}
            ),
            List.of(
               new Talk.Topic("prophet_saw", "What did you see?", "!slain"),
               new Talk.Topic("prophet_guard", "How do I get past the Warden?", "!warden"),
               new Talk.Topic("prophet_seals", "His gate is sealed.", "warden,!slain"),
               new Talk.Topic("prophet_found", "How did the chain find me?", "warden,!slain"),
               new Talk.Topic("prophet_death", "How do I beat the Death King?", "warden,!slain"),
               new Talk.Topic("prophet_mira", "Is there a girl named Mira here?", "!slain,q:mira<2"),
               new Talk.Topic("prophet_kings", "Tell me about the Kings.", ""),
               new Talk.Topic("prophet_hunter", "Who is the Hunter?", "!hunter"),
               new Talk.Topic("king_chain", "You sent my chain.", "slain"),
               new Talk.Topic("king_trust", "Why should I trust you?", "slain,!ended"),
               new Talk.Topic("prophet_tomb", "Why did he hang you here?", "slain"),
               new Talk.Topic("king_changed", "You've changed.", "end2")
            ),
            List.of(),
            List.of("You're back. I can hear your chain.", "*He turns his blind face toward you.* Ask.", "Still alive? Good."),
            "*Ask.*"
         )
      ),
      Map.entry(
         "collector",
         new Talk.Npc(
            "collector",
            List.<String[]>of(new String[]{"", "collector"}),
            List.of(
               new Talk.Topic("collector_who", "Who are you?", ""),
               new Talk.Topic("collector_things", "What do you collect?", ""),
               new Talk.Topic("collector_kings", "What do you know about the Kings?", ""),
               new Talk.Topic("collector_west", "Is there anything out west?", "!hunter"),
               new Talk.Topic("collector_hunter", "I killed the Hunter.", "hunter"),
               new Talk.Topic("collector_silas", "What did the Death King leave behind?", "slain")
            ),
            List.of(new Talk.Service("I found something for you.", "custom:collect", ""), new Talk.Service("Show me what you sell.", "custom:trade", "")),
            List.of("Mind the floor. Again.", "*He peers at your pockets.* Well?", "Back so soon? What have you found?"),
            "*Curious, are we?*"
         )
      ),
      Map.entry(
         "bonesmith",
         new Talk.Npc(
            "bonesmith",
            List.of(new String[]{"slain", "bonesmith.slain"}, new String[]{"", "bonesmith"}),
            List.of(
               new Talk.Topic("bonesmith_mere", "What is this place?", ""),
               new Talk.Topic("bonesmith_who", "Who made you?", ""),
               new Talk.Topic("bonesmith_ranks", "What does tempering do?", ""),
               new Talk.Topic("bonesmith_blades", "Who did you make blades for?", "warden")
            ),
            List.of(new Talk.Service("Temper my weapon.", "dialog:bonesmith_temper", "")),
            List.of("*Clack.*", "*He taps the water, then your weapon.*", "*Clack-clack?*"),
            "*He tilts his whole head. Go on.*"
         )
      ),
      Map.entry(
         "mira",
         new Talk.Npc(
            "mira",
            List.of(new String[]{"q:mira>=2", "mira.met"}, new String[]{"", "mira"}),
            List.of(
               new Talk.Topic("mira_who", "Who are you?", ""),
               new Talk.Topic("mira_friend", "Who's your friend?", ""),
               new Talk.Topic("mira_line", "What's the line like?", "q:mira>=2")
            ),
            List.of(new Talk.Service("Your father sent me.", "custom:quest/mira", "q:mira=1")),
            List.of("You're back! I was at seven hundred and two.", "*She waves with both hands.*", "Hello again!"),
            "*What?*"
         )
      ),
      Map.entry(
         "lamplighter",
         new Talk.Npc(
            "lamplighter",
            List.of(new String[]{"q:lamps=2", "lamplighter.lit"}, new String[]{"", "lamplighter"}),
            List.of(
               new Talk.Topic("lamplighter_who", "Who are you?", ""),
               new Talk.Topic("lamplighter_town", "What was this town like?", ""),
               new Talk.Topic("lamplighter_lamps", "Why light them at all?", "q:lamps>=2")
            ),
            List.of(
               new Talk.Service("Can I help with anything?", "custom:quest/lamplighter", "q:lamps=0"),
               new Talk.Service("About the lamps...", "custom:quest/lamplighter", "q:lamps=1")
            ),
            List.of("Evening. Still evening.", "*He lifts his lamp to see you better.*", "The lamps are holding. Barely."),
            "*Yes?*"
         )
      ),
      Map.entry(
         "sentry",
         new Talk.Npc(
            "sentry",
            List.of(new String[]{"hunter", "sentry.hunter"}, new String[]{"q:name=2", "sentry.named"}, new String[]{"", "sentry"}),
            List.of(
               new Talk.Topic("sentry_guard", "What are you guarding?", "!hunter"),
               new Talk.Topic("sentry_post", "Why do you stay?", ""),
               new Talk.Topic("sentry_corwin", "What do you remember now?", "q:name=2")
            ),
            List.of(
               new Talk.Service("Is something wrong?", "custom:quest/sentry", "q:name=0"),
               new Talk.Service("About your tag...", "custom:quest/sentry", "q:name=1")
            ),
            List.of("Halt. ...Oh. You again.", "*He doesn't take his eyes off the west.*", "Still nothing moving out there."),
            "*Quickly. I'm on watch.*"
         )
      ),
      Map.entry(
         "kid",
         new Talk.Npc(
            "kid",
            List.of(new String[]{"q:ball=2", "kid.ball"}, new String[]{"", "kid"}),
            List.of(
               new Talk.Topic("kid_who", "Who are you?", ""),
               new Talk.Topic("kid_game", "How do you play Judgement?", ""),
               new Talk.Topic("kid_dead", "Do you miss being alive?", "q:ball=2")
            ),
            List.of(
               new Talk.Service("Can I help with anything?", "custom:quest/kid", "q:ball=0"),
               new Talk.Service("About your ball...", "custom:quest/kid", "q:ball=1")
            ),
            List.of("You came back! Do you want to play?", "*Pip waves a bony arm.* Hi! Hi!", "Shh, we're hiding from Tib."),
            "*What? WHAT?*"
         )
      )
   );

   private TalkData() {
   }
}
