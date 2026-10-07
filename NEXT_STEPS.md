# Where we stopped (Oct 7)

Everything below compiles. Tested in game with `UW_NEW=1 ./gradlew runClientGameTest`
(checks + screenshots, `src/gametest/.../NewWorkChecks.java`). Screenshot-only run: `UW_SHOTS=1`.
Last run: all checks PASS except the lantern's 3-hit check, which was a test-timing problem
(its first hit landed during damage cooldown). The test now waits; rerun to confirm.
Not covered by the test: the double Death King, the rift on the BREAK ending, the music.

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
- Worldgen changes (boat, shed roots, Pip's ball, the bridge) only appear in newly generated worlds.
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
1. ~~3D Aldous's Lantern~~ Done. It's built from the vanilla lantern with a softer golden-white light (`tools/lantern3d.py`). Michael said the first custom version looked like a bell. It hangs from the hand in third person. The old flat `textures/item/aldous_lantern.png` is now unused.
2. ~~Ferryman's boat~~ Done. It has the vanilla boat shape (straight spruce hull, dark oak floor) and stands on its end by the dock, leaning on a log (`FerryBlock.UPRIGHT`, `Landmarks.landing`). Michael approved the look.
3. ~~Pip's ball~~ Done. It's a 3D ball block on the dry bed under the footbridge (`PipsBallBlock`); use or punch it to pick it up. This also fixed an old bug: the barrel holding the ball was overwritten by the riverbed fill, so the quest couldn't be finished. The river now runs under the bridge.
4. Check that Mira's 12 spots are clear of structures, and that the clues read right.
5. Test pass for everything above, then tune the numbers.

## Added Oct 7 (afternoon): tested in game with screenshots
- **DeathBound advancement tab:** 18 advancements (`tools/advancements.py` writes the JSON and English text). Anything without a vanilla trigger is awarded from code through `world/Milestones.java`: return to life, charm binding, the Phantom dodge, Soulforge, quests, the Collector's 8 artifacts, the Warden, the seals, the Death King and the endings. The Hollow Hunter one is hidden and uses the vanilla kill trigger.
- **Journal:** a TASKS checklist page, plus live progress per open quest: lantern flames, Mira's last clue (saved in the synced `QUEST_NOTES` attachment), found or not found, and the Soul Jar count. Progress moves to its own page when it doesn't fit (14 lines × 114px).

## Stories rework (Oct 7, evening): tested in game, all checks PASS
Michael wanted three things: each story only once per world, more stories, notes you right-click in the world, and a better reading screen than the plain book page.
- `story/Stories.java` holds the catalog: 24 chest stories and 8 notes. The text lives in the lang file, written by `tools/stories.py` (run it after editing a story; it also writes the note signature line).
- **Writing style (Michael, Oct 7):** simple words and short sentences, but keep some poetry and mystery: one strange image each, and an ending that leaves something unanswered. **No em dashes.** Every chest story is a different death; six that repeated another one (Warden, Guardian, elytra, raid, powder snow, the void) were replaced with: digging straight down, the swamp witch, a ghast on a Nether bridge, silverfish, hitting a villager (iron golem), and a cactus fence.
- Chest loot table `deathbound:journal` now drops a **Lost Journal** (`deathbound:story_book`). `LootTableEvents.MODIFY_DROPS` gives it a story nobody in this world has had yet (`CLAIMED_STORIES` on the overworld); once all 24 are out, a Soul drops instead.
- **Notes** (`StoryNoteBlock`, `story` = index in `Stories.NOTES`, pinned or `flat`), unbreakable, placed in worldgen:
  - the Ferryman's notice on the dock post at the Landing
  - 3 on Ghostwood trees (nearest the middle, furthest out, nearest the boat-shed)
  - 4 on house tables in Lantern's End (houses 2, 5, 8, 11)
- **Reading screen** (`client/StoryScreen.java`): a parchment sheet sized to the text with title, author, divider and paragraphs. Long stories turn pages (arrows, Page Up/Down, Space, or the arrow buttons). Notes get a smaller sheet with a nail. Use the item to read; sneak + use files it away. Reading anything adds it to `STORIES_FOUND`.
- **Journal:** a "STORIES OF THE DEAD" page (found X of 32) listing every story you've read. Click a title to read it again on the parchment screen; closing it goes back to the Journal (`Journal.READ` click event). The old copy of each story into plain book pages is gone.
- Test: `UW_SHOTS=1 ./gradlew runClientGameTest`. Last run (Oct 7) all PASS:
  - 30 chest rolls give 24 different stories and then 6 Souls
  - all 8 notes are in the world, on the right blocks
  - clicking a Journal title opens the story, closing it returns to the Journal
  - screenshots of the notes, reading screens and Journal look right
- Only shows up in **new worlds** (the notes are worldgen).
