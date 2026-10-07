package com.deathbound.registry;

import com.deathbound.DeathBound;
import com.deathbound.block.DecorBlock;
import com.deathbound.block.FerryBlock;
import com.deathbound.block.PipsBallBlock;
import com.deathbound.block.StoryNoteBlock;
import com.deathbound.block.GloomGrassBlock;
import com.deathbound.block.GraveBellBlock;
import com.deathbound.block.GraveLampBlock;
import com.deathbound.block.RemainsBlock;
import com.deathbound.block.RibBlock;
import com.deathbound.block.SealLockBlock;
import com.deathbound.block.SoulBrazierBlock;
import com.deathbound.block.SoulRiftBlock;
import com.deathbound.block.WatcherSkullBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LilyPadBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class ModBlocks {
   public static final List<Block> WITH_ITEMS = new ArrayList<>();
   public static final Block SOULSTONE = register("soulstone", Block::new, stone());
   public static final Block SOULSTONE_BRICKS = register("soulstone_bricks", Block::new, stone().sound(SoundType.DEEPSLATE_BRICKS));
   public static final Block CRACKED_SOULSTONE_BRICKS = register("cracked_soulstone_bricks", Block::new, stone().sound(SoundType.DEEPSLATE_BRICKS));
   public static final Block CHISELED_SOULSTONE = register("chiseled_soulstone", Block::new, stone().sound(SoundType.DEEPSLATE_BRICKS).lightLevel(s -> 7));
   public static final Block SOULSTONE_TILES = register("soulstone_tiles", Block::new, stone().sound(SoundType.DEEPSLATE_TILES));
   public static final Block SOULSTONE_PILLAR = register("soulstone_pillar", RotatedPillarBlock::new, stone().sound(SoundType.DEEPSLATE_BRICKS));
   public static final Block SOULSTONE_BRICK_STAIRS = register(
      "soulstone_brick_stairs", p -> new StairBlock(SOULSTONE_BRICKS.defaultBlockState(), p), stone().sound(SoundType.DEEPSLATE_BRICKS)
   );
   public static final Block SOULSTONE_BRICK_SLAB = register("soulstone_brick_slab", SlabBlock::new, stone().sound(SoundType.DEEPSLATE_BRICKS));
   public static final Block SOULSTONE_BRICK_WALL = register("soulstone_brick_wall", WallBlock::new, stone().sound(SoundType.DEEPSLATE_BRICKS).forceSolidOn());
   public static final Block VEILED_SOULSTONE = register(
      "veiled_soulstone", Block::new, Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).strength(2.0F, 6.0F).sound(SoundType.DEEPSLATE).noCollision()
   );
   public static final Block ASHEN_SOIL = register(
      "ashen_soil", Block::new, Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.6F).sound(SoundType.SOUL_SOIL)
   );
   public static final Block GLOOM_GRASS = register(
      "gloom_grass",
      GloomGrassBlock::new,
      Properties.of()
         .mapColor(MapColor.COLOR_PURPLE)
         .replaceable()
         .noCollision()
         .instabreak()
         .sound(SoundType.NETHER_SPROUTS)
         .offsetType(OffsetType.XZ)
         .pushReaction(PushReaction.POPPED)
   );
   public static final Block GHOSTWOOD_LOG = register(
      "ghostwood_log", RotatedPillarBlock::new, Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F).sound(SoundType.NETHER_WOOD)
   );
   public static final Block GHOSTWOOD_PLANKS = register(
      "ghostwood_planks", Block::new, Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0F).sound(SoundType.NETHER_WOOD)
   );
   public static final Block SOUL_CRYSTAL = register(
      "soul_crystal",
      p -> new AmethystClusterBlock(7.0F, 10.0F, p),
      Properties.of()
         .mapColor(MapColor.COLOR_PURPLE)
         .forceSolidOn()
         .noOcclusion()
         .strength(1.5F)
         .sound(SoundType.AMETHYST_CLUSTER)
         .lightLevel(s -> 10)
         .pushReaction(PushReaction.POPPED)
   );
   public static final Block WRAITH_LANTERN = register(
      "wraith_lantern",
      LanternBlock::new,
      Properties.of()
         .mapColor(MapColor.METAL)
         .forceSolidOn()
         .strength(3.5F)
         .sound(SoundType.LANTERN)
         .lightLevel(s -> 12)
         .noOcclusion()
         .pushReaction(PushReaction.POPPED)
   );
   public static final Block POLISHED_SOULSTONE = register("polished_soulstone", Block::new, stone().sound(SoundType.POLISHED_DEEPSLATE));
   public static final Block DARK_SOULSTONE_BRICKS = register(
      "dark_soulstone_bricks", Block::new, stone().sound(SoundType.DEEPSLATE_BRICKS).strength(3.0F, 8.0F)
   );
   public static final Block SOUL_VEINED_BRICKS = register("soul_veined_bricks", Block::new, stone().sound(SoundType.DEEPSLATE_BRICKS).lightLevel(s -> 5));
   public static final Block SMALL_SOUL_SHARD = register("small_soul_shard", p -> new AmethystClusterBlock(3.0F, 4.0F, p), shardProps(3));
   public static final Block MEDIUM_SOUL_SHARD = register("medium_soul_shard", p -> new AmethystClusterBlock(4.0F, 3.0F, p), shardProps(5));
   public static final Block LARGE_SOUL_SHARD = register("large_soul_shard", p -> new AmethystClusterBlock(5.0F, 3.0F, p), shardProps(7));
   public static final Block SOUL_BRAZIER = register(
      "soul_brazier", SoulBrazierBlock::new, Properties.of().mapColor(MapColor.METAL).strength(2.5F).sound(SoundType.LANTERN).lightLevel(s -> 15).noOcclusion()
   );
   public static final Block REMAINS = register("remains", p -> new RemainsBlock(p, Block.box(1.0, 0.0, 1.0, 15.0, 3.0, 15.0)), bones());
   public static final Block SLUMPED_REMAINS = register("slumped_remains", p -> new RemainsBlock(p, Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 15.0)), bones());
   public static final Block HANGING_REMAINS = register("hanging_remains", p -> new DecorBlock(p, Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0)), bones());
   public static final Block GIBBET_CAGE = register(
      "gibbet_cage", p -> new DecorBlock(p, Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0)), bones().sound(SoundType.CHAIN)
   );
   public static final Block BONE_PILE = register("bone_pile", p -> new DecorBlock(p, Block.box(1.0, 0.0, 1.0, 15.0, 4.0, 15.0)), bones());
   public static final Block SKULL_SPIKE = register(
      "skull_spike", p -> new DecorBlock(p, Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0)), bones().sound(SoundType.CHAIN)
   );
   public static final Block GHOSTWOOD_CHAIR = register("ghostwood_chair", p -> new DecorBlock(p, Block.box(2.0, 0.0, 2.0, 14.0, 9.0, 14.0)), furniture());
   public static final Block GHOSTWOOD_TABLE = register("ghostwood_table", p -> new DecorBlock(p, Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)), furniture());
   public static final Block BONE_CANDELABRA = register(
      "bone_candelabra",
      p -> new DecorBlock(p, Block.box(3.0, 0.0, 6.0, 13.0, 15.0, 10.0)),
      Properties.of().mapColor(MapColor.SAND).strength(0.8F).sound(SoundType.BONE_BLOCK).noOcclusion().lightLevel(s -> 12).pushReaction(PushReaction.POPPED)
   );
   public static final Block TOMBSTONE = register("tombstone", p -> new DecorBlock(p, Block.box(3.0, 0.0, 6.0, 13.0, 15.0, 10.0)), stone().noOcclusion());
   public static final Block OSSUARY_SHELF = register(
      "ossuary_shelf", Block::new, Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(1.5F).sound(SoundType.BONE_BLOCK)
   );
   public static final Block SKULL = register("skull", p -> new DecorBlock(p, Block.box(4.0, 0.0, 5.0, 12.0, 9.0, 13.0)), bones());
   public static final Block PIERCED_SKULL = register(
      "pierced_skull", p -> new DecorBlock(p, Block.box(4.0, 0.0, 5.0, 12.0, 9.0, 13.0)), bones().lightLevel(s -> 3)
   );
   public static final Block FERRY = register(
      "ferry",
      FerryBlock::new,
      Properties.of()
         .mapColor(MapColor.COLOR_BLACK)
         .strength(2.0F, 3.0F)
         .sound(SoundType.WOOD)
         .noOcclusion()
   );
   public static final Block STORY_NOTE = registerNoItem(
      "story_note",
      StoryNoteBlock::new,
      Properties.of().mapColor(MapColor.WOOL).noCollision().strength(-1.0F, 3600000.0F).sound(SoundType.WOOL).noOcclusion().pushReaction(PushReaction.IMMOVEABLE)
   );
   public static final Block PIPS_BALL = registerNoItem(
      "pips_ball", PipsBallBlock::new, Properties.of().mapColor(MapColor.WOOL).instabreak().sound(SoundType.WOOL).noOcclusion().pushReaction(PushReaction.POPPED)
   );
   public static final Block DEAD_GRASS = register("dead_grass", p -> new GloomGrassBlock(p, false), plant());
   public static final Block TALL_DEAD_GRASS = register("tall_dead_grass", p -> new GloomGrassBlock(p, false), plant());
   public static final Block GHOST_BLOOM = register("ghost_bloom", GloomGrassBlock::new, plant().lightLevel(s -> 4));
   public static final Block ASHEN_LILY = registerNoItem(
      "ashen_lily",
      LilyPadBlock::new,
      Properties.of().mapColor(MapColor.COLOR_GRAY).instabreak().sound(SoundType.LILY_PAD).noOcclusion().pushReaction(PushReaction.POPPED)
   );
   public static final Block WHETSTONE = register("whetstone", p -> new DecorBlock(p, Block.box(2.0, 0.0, 4.0, 14.0, 14.0, 12.0)), furniture());
   public static final Block HANGING_BLADE = register(
      "hanging_blade", p -> new DecorBlock(p, Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0)), furniture().noCollision()
   );
   public static final Block COFFIN = register("coffin", p -> new DecorBlock(p, Block.box(2.0, 0.0, 9.0, 14.0, 16.0, 16.0)), furniture());
   public static final Block URN = register(
      "urn",
      p -> new DecorBlock(p, Block.box(4.0, 0.0, 4.0, 12.0, 13.0, 12.0)),
      Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1.0F).sound(SoundType.DECORATED_POT).noOcclusion()
   );
   public static final Block TATTERED_BANNER = register(
      "tattered_banner",
      p -> new DecorBlock(p, Block.box(1.0, 0.0, 14.0, 15.0, 16.0, 16.0)),
      Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.5F).sound(SoundType.WOOL).noOcclusion().noCollision()
   );
   public static final Block GIANT_RIB = register(
      "giant_rib", RibBlock::new, Properties.of().mapColor(MapColor.SAND).strength(2.0F).sound(SoundType.BONE_BLOCK).noOcclusion()
   );
   public static final Block BONE_CHANDELIER = register(
      "bone_chandelier", p -> new DecorBlock(p, Block.box(2.0, 2.0, 2.0, 14.0, 16.0, 14.0)), bones().lightLevel(s -> 12)
   );
   public static final Block HUNTER_STAKE = register(
      "hunter_stake", p -> new DecorBlock(p, Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0)), bones().lightLevel(s -> 4)
   );
   public static final Block STUCK_ARROWS = register(
      "stuck_arrows", p -> new DecorBlock(p, Block.box(3.0, 0.0, 3.0, 13.0, 9.0, 13.0)), bones().lightLevel(s -> 5)
   );
   public static final Block OSSIFIED_LOG = register(
      "ossified_log", RotatedPillarBlock::new, Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(2.0F).sound(SoundType.BONE_BLOCK)
   );
   public static final Block CURIO_SHELF = register(
      "curio_shelf", Block::new, Properties.of().mapColor(MapColor.COLOR_GRAY).strength(1.5F).sound(SoundType.WOOD).lightLevel(s -> 3)
   );
   public static final Block GLOWCAP = register("glowcap", p -> new DecorBlock(p, Block.box(3.0, 0.0, 4.0, 13.0, 8.0, 13.0)), glowcap(11));
   public static final Block WALL_GLOWCAP = register("wall_glowcap", p -> new DecorBlock(p, Block.box(3.0, 3.0, 11.0, 13.0, 11.0, 16.0)), glowcap(9));
   public static final Block GRAVE_LAMP = register(
      "grave_lamp",
      GraveLampBlock::new,
      Properties.of()
         .mapColor(MapColor.COLOR_GRAY)
         .strength(-1.0F, 3600000.0F)
         .sound(SoundType.STONE)
         .noOcclusion()
         .lightLevel(s -> s.getValue(GraveLampBlock.LIT) ? 11 : 0)
   );
   public static final Block WATCHER_SKULL = register(
      "watcher_skull",
      p -> new WatcherSkullBlock(p, Block.box(4.0, 0.0, 5.0, 12.0, 9.0, 13.0)),
      Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(-1.0F, 3600000.0F).sound(SoundType.BONE_BLOCK).noOcclusion()
   );
   public static final Block ARMOR_RACK = register("armor_rack", p -> new DecorBlock(p, Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0)), furniture());
   public static final Block WEAPON_RACK = register("weapon_rack", p -> new DecorBlock(p, Block.box(1.0, 0.0, 11.0, 15.0, 16.0, 16.0)), furniture());
   public static final Block GRAVE_ANVIL = register(
      "grave_anvil",
      p -> new DecorBlock(p, Block.box(1.0, 0.0, 4.0, 15.0, 10.0, 12.0)),
      Properties.of().mapColor(MapColor.COLOR_BLACK).strength(5.0F, 1200.0F).sound(SoundType.ANVIL).noOcclusion()
   );
   public static final Block BEDROLL = register(
      "bedroll",
      p -> new DecorBlock(p, Block.box(1.0, 0.0, 2.0, 15.0, 3.0, 14.0)),
      Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.4F).sound(SoundType.WOOL).noOcclusion()
   );
   public static final Block FARE_BOWL = register(
      "fare_bowl",
      p -> new DecorBlock(p, Block.box(3.0, 0.0, 3.0, 13.0, 4.0, 13.0)),
      Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1.0F).sound(SoundType.METAL).noOcclusion()
   );
   public static final Block GRAVE_BELL = register(
      "grave_bell", GraveBellBlock::new, Properties.of().mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 3600000.0F).sound(SoundType.ANVIL).noOcclusion()
   );
   public static final Block LOCK_KINGS = lock("lock_kings", 0);
   public static final Block LOCK_WATCHERS = lock("lock_watchers", 1);
   public static final Block LOCK_BELL = lock("lock_bell", 2);
   public static final Block JAR = jar("jar", 0);
   public static final Block SOUL_JAR = jar("soul_jar", 10);
   public static final Block BONE_JAR = jar("bone_jar", 0);
   public static final Block EYE_JAR = jar("eye_jar", 3);
   public static final Block JAR_CROWN = jar("jar_crown", 0);
   public static final Block JAR_KEYS = jar("jar_keys", 0);
   public static final Block JAR_HEART = jar("jar_heart", 4);
   public static final Block JAR_MOTH = jar("jar_moth", 6);
   public static final Block BOTTLED_SHIP = register(
      "bottled_ship",
      p -> new DecorBlock(p, Block.box(3.0, 0.0, 5.0, 16.0, 5.0, 11.0)),
      Properties.of().mapColor(MapColor.NONE).strength(0.3F).sound(SoundType.GLASS).noOcclusion().pushReaction(PushReaction.POPPED)
   );
   public static final Block SOULWATER = registerNoItem(
      "soulwater", p -> new LiquidBlock(ModFluids.SOULWATER, p), Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.COLOR_PURPLE).lightLevel(s -> 5)
   );
   public static final Block SOUL_SEAL = registerNoItem(
      "soul_seal",
      Block::new,
      Properties.of()
         .mapColor(MapColor.COLOR_PURPLE)
         .strength(-1.0F, 3600000.0F)
         .noLootTable()
         .lightLevel(s -> 11)
         .noOcclusion()
         .sound(SoundType.AMETHYST)
         .isValidSpawn((s, l, p, e) -> false)
   );
   public static final Block SOUL_RIFT = registerNoItem(
      "soul_rift",
      SoulRiftBlock::new,
      Properties.of()
         .mapColor(MapColor.COLOR_PURPLE)
         .strength(-1.0F, 3600000.0F)
         .noLootTable()
         .lightLevel(s -> 15)
         .noCollision()
         .noOcclusion()
         .pushReaction(PushReaction.IMMOVEABLE)
   );

   private static Properties stone() {
      return Properties.of()
         .mapColor(MapColor.TERRACOTTA_PURPLE)
         .instrument(NoteBlockInstrument.BASEDRUM)
         .requiresCorrectToolForDrops()
         .strength(2.0F, 6.0F)
         .sound(SoundType.DEEPSLATE);
   }

   private static Properties glowcap(int light) {
      return Properties.of()
         .mapColor(MapColor.COLOR_PURPLE)
         .noCollision()
         .instabreak()
         .sound(SoundType.FUNGUS)
         .lightLevel(s -> light)
         .pushReaction(PushReaction.POPPED);
   }

   private static Block lock(String name, int kind) {
      return register(
         name,
         p -> new SealLockBlock(p, kind),
         Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(-1.0F, 3600000.0F)
            .sound(SoundType.STONE)
            .noLootTable()
            .lightLevel(s -> s.getValue(SealLockBlock.FILLED) ? 13 : 4)
      );
   }

   private static Block jar(String name, int light) {
      return register(
         name,
         p -> new DecorBlock(p, Block.box(5.0, 0.0, 5.0, 11.0, 11.0, 11.0)),
         Properties.of().mapColor(MapColor.NONE).strength(0.3F).sound(SoundType.GLASS).noOcclusion().lightLevel(s -> light).pushReaction(PushReaction.POPPED)
      );
   }

   private static Properties furniture() {
      return Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F).sound(SoundType.NETHER_WOOD).noOcclusion();
   }

   private static Properties shardProps(int light) {
      return Properties.of()
         .mapColor(MapColor.COLOR_PURPLE)
         .forceSolidOn()
         .noOcclusion()
         .strength(1.0F)
         .sound(SoundType.AMETHYST_CLUSTER)
         .lightLevel(s -> light)
         .pushReaction(PushReaction.POPPED);
   }

   private static Properties plant() {
      return Properties.of()
         .mapColor(MapColor.COLOR_GRAY)
         .replaceable()
         .noCollision()
         .instabreak()
         .sound(SoundType.NETHER_SPROUTS)
         .offsetType(OffsetType.XZ)
         .pushReaction(PushReaction.POPPED);
   }

   private static Properties bones() {
      return Properties.of().mapColor(MapColor.SAND).strength(0.8F).sound(SoundType.BONE_BLOCK).noOcclusion().noCollision().pushReaction(PushReaction.POPPED);
   }

   private static Block register(String name, Function<Properties, Block> factory, Properties props) {
      Block block = registerNoItem(name, factory, props);
      ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, DeathBound.id(name));
      Registry.register(
         BuiltInRegistries.ITEM, key, new BlockItem(block, new net.minecraft.world.item.Item.Properties().setId(key).useBlockDescriptionPrefix())
      );
      WITH_ITEMS.add(block);
      return block;
   }

   private static Block registerNoItem(String name, Function<Properties, Block> factory, Properties props) {
      ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, DeathBound.id(name));
      return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(props.setId(key)));
   }

   public static void init() {
   }

   private ModBlocks() {
   }
}
