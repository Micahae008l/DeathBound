package com.deathbound.block;

import com.deathbound.registry.ModBlocks;
import com.deathbound.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GloomGrassBlock extends VegetationBlock {
   private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 10.0);
   private final boolean motes;

   public GloomGrassBlock(Properties properties) {
      this(properties, true);
   }

   public GloomGrassBlock(Properties properties, boolean motes) {
      super(properties);
      this.motes = motes;
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   @Override
   protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
      return state.is(ModBlocks.ASHEN_SOIL) || state.is(BlockTags.SUPPORTS_VEGETATION);
   }

   @Override
   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (this.motes && random.nextInt(40) == 0) {
         level.addParticle(ModParticles.SOUL_MOTE, pos.getX() + random.nextDouble(), pos.getY() + 0.4, pos.getZ() + random.nextDouble(), 0.0, 0.015, 0.0);
      }
   }
}
