package com.deathbound.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FerryBlock extends DecorBlock {
   public static final IntegerProperty PART = IntegerProperty.create("part", 0, 3);
   private static final VoxelShape ALONG_Z = Shapes.or(
      Block.box(1.0, 0.0, 0.0, 15.0, 1.0, 16.0), Block.box(0.5, 0.0, 0.0, 2.0, 7.0, 16.0), Block.box(14.0, 0.0, 0.0, 15.5, 7.0, 16.0)
   );
   private static final VoxelShape ALONG_X = Shapes.or(
      Block.box(0.0, 0.0, 1.0, 16.0, 1.0, 15.0), Block.box(0.0, 0.0, 0.5, 16.0, 7.0, 2.0), Block.box(0.0, 0.0, 14.0, 16.0, 7.0, 15.5)
   );

   public FerryBlock(Properties properties) {
      super(properties, Shapes.block());
      this.registerDefaultState(this.defaultBlockState().setValue(PART, 1));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(PART);
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return state.getValue(FACING).getAxis() == Axis.Z ? ALONG_Z : ALONG_X;
   }
}
