package com.deathbound.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FerryBlock extends DecorBlock {
   public static final IntegerProperty PART = IntegerProperty.create("part", 0, 3);
   // stood on its end (stern at the bottom, bow up), the open side facing FACING
   public static final BooleanProperty UPRIGHT = BooleanProperty.create("upright");
   private static final VoxelShape UPRIGHT_SOUTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 9.0);
   private static final VoxelShape UPRIGHT_NORTH = Block.box(0.0, 0.0, 7.0, 16.0, 16.0, 16.0);
   private static final VoxelShape UPRIGHT_EAST = Block.box(0.0, 0.0, 0.0, 9.0, 16.0, 16.0);
   private static final VoxelShape UPRIGHT_WEST = Block.box(7.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   private static final VoxelShape ALONG_Z = Shapes.or(
      Block.box(2.0, 0.0, 0.0, 14.0, 2.0, 16.0), Block.box(0.0, 0.0, 0.0, 2.0, 9.0, 16.0), Block.box(14.0, 0.0, 0.0, 16.0, 9.0, 16.0)
   );
   private static final VoxelShape ALONG_X = Shapes.or(
      Block.box(0.0, 0.0, 2.0, 16.0, 2.0, 14.0), Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 2.0), Block.box(0.0, 0.0, 14.0, 16.0, 9.0, 16.0)
   );

   public FerryBlock(Properties properties) {
      super(properties, Shapes.block());
      this.registerDefaultState(this.defaultBlockState().setValue(PART, 1).setValue(UPRIGHT, false));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(PART, UPRIGHT);
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      if (state.getValue(UPRIGHT)) {
         return switch (state.getValue(FACING)) {
            case NORTH -> UPRIGHT_NORTH;
            case EAST -> UPRIGHT_EAST;
            case WEST -> UPRIGHT_WEST;
            default -> UPRIGHT_SOUTH;
         };
      }

      return state.getValue(FACING).getAxis() == Axis.Z ? ALONG_Z : ALONG_X;
   }
}
