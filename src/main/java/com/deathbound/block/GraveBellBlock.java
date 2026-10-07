package com.deathbound.block;

import com.deathbound.registry.ModSounds;
import com.deathbound.world.Puzzles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;

public class GraveBellBlock extends DecorBlock {
   public GraveBellBlock(Properties properties) {
      super(properties, Block.box(3.5, 3.5, 3.5, 12.5, 16.0, 12.5));
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (level instanceof ServerLevel server) {
         ring(server, pos);
      }

      return InteractionResult.SUCCESS;
   }

   public static void ring(ServerLevel level, BlockPos pos) {
      level.playSound(null, pos, ModSounds.TOLL, SoundSource.BLOCKS, 6.0F, 0.5F);
      level.sendParticles(ParticleTypes.ASH, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, 30, 0.4, 0.2, 0.4, 0.02);
      Puzzles.onBell(level, pos);
   }
}
