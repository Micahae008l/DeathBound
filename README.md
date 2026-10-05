# Underworld Characters (standalone Fabric 26.3 dev mod)

The Hollow Hunter + The Collector, built outside the main Underworld mod so they can be tested in a void world and merged later.

## Run it
```
./gradlew runClient            # normal dev client
./gradlew runClientGameTest    # automated showcase: builds everything, screenshots to build/run/clientGameTest/screenshots
```
In game: create a Creative world (Superflat -> "The Void" preset works), then:

| Command | What it does |
|---|---|
| `/underworld setup` | Builds the Hunter's Grounds (north) and the Collector's room (east, via the secret passage) next to you |
| `/underworld goto hunter` / `goto collector` | Teleport to either |
| `/underworld kit` | Survival test gear (the Hunter only wakes for survival/adventure players) |
| `/underworld artifacts` | All 5 artifacts, Hollow Arrows, Hunter's Eye, Hollow Bow, emeralds |
| `/underworld hunter start / reset / phase <1-3>` | Fight control |
| `/underworld hunter showcase <1-3> <pose>` | Freeze him in a pose: idle draw summon rain slash last_hunt kneel intro roar release |
| `/underworld hunter volley` | Make him summon a volley at you |
| `/underworld collector forget` | Wipe what the Collector remembers about you |

## Where things live
- `entity/hunter/HollowHunter.java` - boss AI state machine (balancing constants at the top)
- `entity/hunter/HollowArrow.java`, `SpectralBolt.java` - Hollow Arrows (anchors/marks) and summoned spectral arrows
- `entity/collector/Collector.java` - dialogue, artifacts, rewards, trades, vanish
- `item/HollowBowItem.java` - Hollow Shot + sneak-use teleport to your arrow
- `block/CuriosityBlock.java` + `registry/ModBlocks.java` - the Collector's jars & bottles
- `world/StructureBuilder.java` - both locations, built in code
- `client/model/*Model.java` - all animations (hand-written; bow draw uses 2-bone IK)
- `tools/gen_assets.py` - **single source of truth** for model geometry + all textures/jar models. Edit and re-run `python3 tools/gen_assets.py`.
- `assets/underworld/sounds.json` - custom bow sounds (re-pitched vanilla layers; swap in real .ogg files later)

## Merging into the main mod
Change the mod id `underworld` / package `com.underworld` to yours, move the registries into your existing ones,
swap `/underworld setup` for your real structures (entities only need `setHome(pos)`), and replace emerald rewards
with Souls / Charms in `Collector#rewardFor` and `#buildOffers`.
