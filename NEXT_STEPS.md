# Where we stopped (Oct 7)

Everything below compiles (`./gradlew build`), but **none of it has been tested in game yet**.
The next session should start with a test pass (`./gradlew runClientGameTest`, extend
`src/gametest/java/com/deathbound/test/FixesClientTest.java`) and screenshots.

## Bug list: coded, needs testing
- **Warden (Death's Guard) didn't react to arrows from far away.** Anyone who hurts him becomes his target, and he gives up beyond 48 blocks from his post (`entity/DeathsGuard.java` hurtServer + customServerAiStep).
- **Underworld mobs attacked the Death King.** His area attacks (eruptions, shockwaves, sweeps, bolts, orbs) no longer hit other Underworld monsters, and any that still hurt him are ignored (`Hazards.sameSide`, `SoulBolt`, `DeathOrb`, `DeathEntity.hurtServer`).
- **Death King spawned twice after leaving the towers.** The count is now level-wide, and strays get pulled back to the throne (`world/Director.java` arena tick).
- **Portal stayed open after choosing to break the throne.** The rift now closes for the BREAK ending (`Director` wantRift).
- **Bell clue** now reads "TOLL FOR ALL EXISTING KINGS." (still 4 tolls).
- **Ghostwood planks/logs** are added to the vanilla `planks`, `logs` and `logs_that_burn` tags, so they work in all vanilla recipes (crafting table, sticks, chests, tools, fuel).
- **Scythe in-hand texture** flipped to match the icon (`textures/item/reaper_scythe_in_hand.png`). Check it in third person.
- **Final boss music** is now Death's Requiem: the intro plays once, then the loop repeats for the whole Death King fight (`client/mixin/BossMusicMixin.java`, `sounds/music/deaths_requiem_*.ogg`).

## Charms
- **Ferryman's Charm** is single use: it shatters after one crossing. The Ferryman sells it for 8 souls, unlimited stock.
- **Phantom Charm (new, epic).** Once every 30 seconds, a hit from a mob or projectile passes through you. A quiet sound tells you when it's ready again. Drops from Death's Guard (in its random charm pool) and tower chests (10%). Icon: screaming wraith.
- **Collector's Charm (new).** Underworld kills have a 35% chance to drop an extra Soul. Sold by the Collector for 16 souls. **Still waiting on Michael:** keep it, or swap it for Bonecage / Featherbone / another idea.
- Rejected and removed: Lamplighter's, Gravedigger's, Shade's, Gravecaller's.
- NPC shops restock once in existing worlds (trades version bump in `UnderworldNpc`).

## Death's Guard buff (coded, needs testing and tuning)
- 380 HP, 13 melee, slam 18, Judgement eruptions 14, lunge 8, speed 0.30, shorter rests between attacks.
- **Enraged below 50% health:** soul-flame aura, 20% faster, a sweep can chain into a slam, lunges and Judgement more often.
- The further away you are, the more often he uses Judgement.
- A Guard saved in an existing world gets the new stats when it loads.

## Quests (coded, needs testing)
- **Mira**
  - Aldous's lantern gutters with every hit you take and goes out after 3. Hold it to a soul flame (brazier, grave lamp, soul torch or campfire...) to relight it. Mira won't take it while it's out (`quest.mira.dark` dialog).
  - Mira's spot in the line is now one of 12 places on the Gate island, chosen per world. While the quest is active, souls in the line give hot/cold clues (`world/QuestEvents.java`, `LostSoul.mobInteract`).
- **Sentry's tag:** taking it on the broken bridge Marks you, and arrows rain from the Hollow until you get back to the Watch.
- **Ferryman's oar:** ghostwood roots cover the chest in the boat-shed (chop them). Carrying the oar wakes the Ghostwood: Darkness, Gravebound and Soul Wisps rise around you until you leave the trees, then they crumble.
  - The roots are **worldgen**, so they only appear in newly generated worlds.

## Not done yet
1. **3D Aldous's Lantern.** The model and 4 flame states exist (`tools/lantern3d.py`), but the display transforms (hand, first person, GUI) haven't been looked at. Take screenshots and tune. The old flat `textures/item/aldous_lantern.png` is now unused.
2. **Ferryman's boat at the Landing** (`ferry_0..3` block models, ~200 elements). Michael wants it much simpler, resting on a few blocks. Not started (`Landmarks.java` ~line 105 places it).
3. **Pip's ball** is still a flat item in a barrel under the footbridge. A 3D ball lying under the bridge was offered; no answer yet.
4. Check that Mira's 12 spots are clear of structures, and that the clues read right.
5. Test pass for everything above, then tune the numbers.
