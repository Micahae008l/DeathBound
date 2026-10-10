# DeathBound: notes for Claude

Fabric mod for Minecraft 26.3 (Java 25, mod id `deathbound`). Michael playtests and sends feedback in batches; check
`NEXT_STEPS.md` for what was done last and what's open. The README covers setup, tools, tests and filming.

## Working in this repo
- The Java source was decompiled from the 1.0.0 jar: 3-space indents, CRLF line endings, few comments. Match that. When
  scripting edits, read with `newline=''`, normalize CRLF to LF, edit, and write back CRLF.
- Build: `./gradlew compileJava compileClientJava --no-configuration-cache -q`. Set `JAVA_HOME` to a JDK 25 if the
  default `java` is older.
- `tools/datagen.py` and `tools/art/build.py` regenerate resources from 1.0.0 and would overwrite newer work; they refuse
  to run without `DB_REGEN=1`. Port changes into them before using them.
- Commit with a short subject and a body that says what changed and why.

## Testing: always see it in game before calling it done
- In-game checks: `UW_NEW=1 ./gradlew runClientGameTest` (every line should say PASS), `UW_STORIES=1`, `UW_SHOTS=1`.
  Results print as `[DB-TEST] ... -> PASS/FAIL`. A temporary scene added to `src/gametest/.../FixesClientTest.java`
  (screenshots to `build/run/clientGameTest/screenshots`) is the quickest way to look at something; remove it after.
- A check for a fix should fail on the old code: run it on the stashed code once.
- Scripted test client: `tools/trailer/shots.py <session>` writes `run/test-script.txt`, then reset `run/saves/dbtest`
  from `run/world` (delete its `dimensions/deathbound`) and `./gradlew runTestClient`. Harness verbs are in
  `DevHarness.java` (wait, shot, rec/stop, pan/orbit/follow/shoulder, view, hold, click, aim, camoff...). Debug commands
  live in `world/DebugCommand.java` (`deathbound temper/quest/accept/story/seals/ending/rise/...`).
- Gotchas that cost runs:
  - `tp` sets the feet: a camera height is eye height, 1.62 above.
  - The film camera keeps teleporting the player to its last pose, even after a `tp`. NPCs are only put at their posts
    when a player is really near, so `camoff` and stand there first.
  - Killing the Death King ends the story and sends the player home; film his empty throne before he ever spawns.
  - NoAI mobs summoned at an island's nominal top end up buried: summon high, let them fall, then set NoAI.
  - A follow camera can clip through blocks (a black second in the clip): sample clips densely, not 4 frames.
  - A screenshot pauses the client while the server keeps ticking: one screenshot per event when timing matters.
  - To test attacks, call Minecraft's private `startAttack` by reflection; a client-only swing skips server effects.
  - Kill a stuck test client by its command line (`-Ddeathbound.script` or `-Dfabric.client.gametest`), nothing else.

## Michael's direction
**Look: scary, dark, "the world of the dead".**
- Darkness first: low light, dark fog, light only in pools (braziers, crystals, lanterns). Stone near-black with a faint
  violet tint; bright purple only on things that glow.
- No vanilla blocks in Underworld builds. Cubic, whole-block shapes ("we live in minecraft"); no smooth curves made of
  tiny model boxes. No carved skull-face blocks.
- Effects use Minecraft's own textures recolored purple (fire, flame, fire charge). No custom blobs, halos or flat-edged
  fire. No cyan or white particles: use `ModParticles.SOUL_FLAME` / `SOUL_MOTE`.
- Held items at vanilla pixel density (a 16px sprite blown up to weapon size looks blocky). The scythe is held like a
  sword and swings like a scythe; its effects land when the blade does.

**Review standard: "be harsher on yourself".** Before showing any visual, check it the way a player sees it: F5 behind
and in front, first person, mid-action, in the dark Underworld. Write down what's wrong in each frame first. For
weapons, look at the actual attack frame by frame, not a pose.

**Gameplay.** Sparse enemies; bosses fought alone; difficulty scales with gear; exploring and puzzles stand between the
Warden and the Death King. Text in plain short sentences; stories are real people in their own voice, no em dashes. Lore
canon is in the `tools/lore.py` docstring.

**Trailers.** Dark: default brightness, no night vision, subjects lit by hidden `minecraft:light` blocks. Slow pushes
only, nothing orbiting or turning fast. No spoilers: no boss fights or movesets, no Death King forms past the throne, no
endings. The soul dust goes only over black screens and cards. The mod is a beta: BETA over the logo, and the trailer
ends with "The beta drops at 100 likes."

## Open
- Collector's Charm: keep it, or swap it for another idea? Michael hasn't decided.
