package com.deathbound.block;

import com.deathbound.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RemainsBlock extends DecorBlock {
   public static final BooleanProperty TWITCH = BooleanProperty.create("twitch");

   public RemainsBlock(Properties properties, VoxelShape shape) {
      super(properties, shape);
      this.registerDefaultState(this.defaultBlockState().setValue(TWITCH, false));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(TWITCH);
   }

   public static void twitch(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
      level.setBlock(pos, state.setValue(TWITCH, true), 2);
      level.scheduleTick(pos, state.getBlock(), 3 + random.nextInt(6));
      level.playSound(null, pos, ModSounds.TWITCH, SoundSource.AMBIENT, 0.7F, 0.8F + random.nextFloat() * 0.4F);
   }

   @Override
   protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (state.getValue(TWITCH)) {
         level.setBlock(pos, state.setValue(TWITCH, false), 2);
      }
   }
}
