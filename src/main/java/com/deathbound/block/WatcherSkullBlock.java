package com.deathbound.block;

import com.deathbound.world.Puzzles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WatcherSkullBlock extends DecorBlock {
   public WatcherSkullBlock(Properties properties, VoxelShape shape) {
      super(properties, shape);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (level instanceof ServerLevel server) {
         level.setBlock(pos, state.setValue(FACING, state.getValue(FACING).getClockWise()), 3);
         level.playSound(null, pos, SoundEvents.SKELETON_STEP, SoundSource.BLOCKS, 0.9F, 0.6F);
         Puzzles.onSkullTurned(server, pos, player);
      }

      return InteractionResult.SUCCESS;
   }
}
