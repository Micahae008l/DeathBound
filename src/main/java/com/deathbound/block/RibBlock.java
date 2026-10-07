package com.deathbound.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class RibBlock extends DecorBlock {
   public static final IntegerProperty SHAPE = IntegerProperty.create("shape", 0, 2);

   public RibBlock(Properties properties) {
      super(properties, Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0));
      this.registerDefaultState(this.defaultBlockState().setValue(SHAPE, 0));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(SHAPE);
   }
}
