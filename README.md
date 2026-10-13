# DeathBound

Fabric mod for Minecraft 26.3 (Java 25). Mod id `deathbound`.

```
./gradlew runClient    # play in a dev client
./gradlew build        # jar -> build/libs/DeathBound-1.0.0.jar
```

## Setting up on a new PC

1. Install a JDK 25 (e.g. Eclipse Temurin) and Git, then `git clone https://github.com/Micahae008l/DeathBound`.
2. `./gradlew build` (downloads Minecraft and Fabric the first time), then `./gradlew runClient` to play.
3. For the tools in `tools/`: Python 3.12 and `pip install numpy pillow imageio-ffmpeg`.
4. Claude Code picks up `CLAUDE.md` (how to work here, Michael's direction) and `NEXT_STEPS.md` (what's done and open).

Not in git: your worlds (`run/saves`), the footage (`run/trailer`, `run/trailer_keep`; re-film it with the sessions
named in each trailer script) and the rendered videos (`build/`).

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
| `tools/trailer/` | Trailer, endings and characters videos (shot lists, score, edit). `trailer4.py` is the latest cut | Yes (see Filming below) |

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
Clips land in `run/trailer/`. `run/world` (the template world) and `run/options.txt` (default brightness: the trailers
are graded dark on purpose) are in git; the rest of `run/` isn't.

Trailer 5 is filmed with the Mellow shader pack. From Modrinth put `iris-fabric-1.11.7+mc26.3.jar` and
`sodium-fabric-0.9.2+mc26.3.jar` in `run/mods` and `Mellow v3.4.1a.zip` in `run/shaderpacks`, copy
`tools/trailer/mellow_settings.txt` to `run/shaderpacks/Mellow v3.4.1a.zip.txt` (it turns Mellow's warm torch light cold
violet for the Underworld), and set `enableShaders=true` and `shaderPack=Mellow v3.4.1a.zip` in `run/config/iris.properties`.
Remove the two jars from `run/mods` to film without shaders. Then the sessions listed in `tools/trailer/trailer5.py`.
