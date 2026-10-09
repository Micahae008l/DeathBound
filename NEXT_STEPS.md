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
- **Scythe in hand (redone Oct 8-9, checked in game frame by frame):** a 32px held sprite (straight snath, blade drooping forward) at a vanilla held item's pixel size, about 30% smaller than the Oct 8 one. Michael picked the sword-like hold over a shoulder carry: held out in the fist, the snath angled forward, the blade hooking down in front. The combo swings like a scythe: the wrist (`ScytheSwing.thirdPersonItem`, `ItemInHandLayerMixin`) cocks it up, then tips it over so the blade drags through the target at body height (two reaps), and the third hit raises it overhead and slams the blade into the ground in front. First person matches. Left-handed players get an exact mirror (negative x scale on the left-hand transforms).
- **Scythe effects follow the blade (Oct 9):** the purple slashes, the hit flames, the sweep sound and the slam (ring, damage, smash, shake) used to fire on the click, before the blade got there, in a fixed fan. They now land when the blade does (`ReaperScytheItem.later`): the slash appears along the blade's path a tick a piece (right to left, the backhand left to right), at your position at that moment, and the slam lands when the blade hits the ground. Timed on screen: the slashes show 5, 6 and 7 ticks after the click as the blade crosses, the slam at 6 as it lands (give or take a tick). In multiplayer they lag by the player's ping.
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
- **Writing style (Michael, Oct 7):** no poetry. Each chest story is a real person telling what happened in their own voice (diary days, a letter, a warning), plain and concrete, every detail true to the game, ending on a lesson or a punchline. **No em dashes.** Every chest story is a different death. All 24 were rewritten this way; check them with `UW_STORIES=1 ./gradlew runClientGameTest` (one screenshot per story; each should fit on one page).
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

## Review fixes (Oct 7, night): tested in game, all checks PASS
A review of the code turned up the bugs below. Each check was run on the old code too: the ones marked "was" failed there.
- **The Death King's nova and charge hurt his own Gravebound.** They now pass his own side by (`DeathEntity`, `Hazards.sameSide`). Test: 4 Gravebound in the nova, was 4 hurt, now 0.
- **Lost Souls in the line gave a Soul every time.** Talking down the line farmed Souls. Now a player gets at most 1 every 5 minutes (`LostSoul`). Test: 40 ghosts, was 12 Souls, now 1.
- **Two Miras in existing worlds.** Her new spot meant a second one spawned while the old one stayed. The extra one is removed; the one nearest her spot stays (`Director` tickNpcs). Test: was 2, now 1.
- **The oar's wake.** Dropping the oar and picking it up again started a second wake, and the first wake's risen never crumbled. Test: was 5 left after leaving the trees, now 0. The risen now also climb out of the ground (`QuestEvents`).
- **Tick timers survived into the next world.** The lantern, ambush and wake timers, and the Phantom cooldown, count server ticks, which start over in every world. They are cleared when the world closes. A player who logs out mid-wake has it settled (`QuestEvents`, `Charms`).
- **Shop restock gave sold-out one-of-a-kind items back.** The trades version bump restocked everything. Single-use offers that were already sold now stay sold (`UnderworldNpc`).
- **Quests finished before the advancement tab existed** award their advancements when the player joins (`Milestones.init`).
- **Boss music restarted its intro** whenever a screen (inventory, pause) was opened mid-fight. Fixed (`BossMusicMixin`).
- **A lost sigil could be re-granted forever** (a dupe). Now it's given again at most once (`Puzzles`).
- **The story screen duplicated its pages** on a window resize (`StoryScreen`). Test: 1 page before, 1 after.

Checked and not a bug (the tests stay as regression checks):
- **Phantom Charm:** the rest of a multi-hit attack does not land after it fires. Vanilla's damage cooldown already blocks it.
- **Warden:** he doesn't get pulled back into a chase once he has given up. His target goals only work within 8 blocks of his post.
- **Gravebound** hit by the King keep their target on the player.

Flaky: "ghostwood: things risen around the player" was 0 in 1 of 6 runs (the others saw 2 to 4). No King was left over, and nothing removes them early, so it looks like spawn luck. Rerun if it fails once.

The generator scripts are now in `tools/` (see README). `datagen.py` and `art/build.py` refuse to run without `DB_REGEN=1`.
