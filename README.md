# DeathBound

Fabric mod for Minecraft 26.3 (Java 25). Mod id `deathbound`.

```
./gradlew runClient    # play in a dev client
./gradlew build        # jar -> build/libs/DeathBound-1.0.0.jar
```

## This repo is the main copy

The Java source was restored from `DeathBound-1.0.0.jar` (Oct 7) with Vineflower, and all work since has been done here.
The original 1.0.0 source still exists on Michael's PC (`C:\Users\Michael\DeathBound`). It has the original comments, but it
has none of the work from Oct 7 on, so don't copy it back over this repo.

## Tools

| Script | What it does | Safe to run? |
|---|---|---|
| `tools/stories.py`, `tools/advancements.py` | Story and advancement text and JSON | Yes |
| `tools/blocks16.py`, `tools/charms16.py`, `tools/lantern3d.py`, `tools/story_art.py` | Textures and models made since Oct 7 | Yes |
| `tools/datagen.py` with `lore.py` and `talk.py` | Every dialog, the language file, models, block states, loot, `sounds.json`, `TalkData.java` | **Guarded** |
| `tools/art/build.py` (and the modules in `tools/art/`) | The original textures and entity models (`ModelMeshes.java`) | **Guarded** |
| `tools/trailer/` | Trailer, endings and characters videos (shot lists, score, edit). `trailer4.py` is the latest cut | Yes; filming needs a template world in `run/world` (see below) |

The two guarded scripts generated 1.0.0's resources. Run as they are now, they would overwrite everything changed since:
- the stories and advancements in the language file
- the new charms
- Death's Requiem in `sounds.json`
- the 16px textures

So they refuse to run unless `DB_REGEN=1` is set. Before setting it, port the newer changes into `lore.py` / `talk.py` / the art modules first.

## Tests

In-game checks with screenshots, in `src/gametest`:

```
UW_NEW=1 ./gradlew runClientGameTest       # fixes, charms, Warden, quests: every line should say PASS
UW_STORIES=1 ./gradlew runClientGameTest   # every story's reading screen, one screenshot per page
UW_SHOTS=1 ./gradlew runClientGameTest     # screenshots only
```

Results print as `[DB-TEST] ... -> PASS/FAIL` lines in the game log.

## Filming

`tools/trailer/shots.py <session>` writes `run/test-script.txt`; `./gradlew runTestClient` plays it in a copy of
`run/world` (copy it to `run/saves/dbtest` and delete `dimensions/deathbound` first, so the Underworld generates fresh).
Clips land in `run/trailer/`. `run/` isn't in git, so `run/world` (any small flat world) has to be made once per machine.
Film at Minecraft's default brightness: the trailers are graded dark on purpose.
