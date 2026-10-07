package com.deathbound.block;

import com.deathbound.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SoulBrazierBlock extends Block {
   private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 11.0);

   public SoulBrazierBlock(Properties properties) {
      super(properties);
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   @Override
   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      double x = pos.getX() + 0.5;
      double y = pos.getY() + 0.75;
      double z = pos.getZ() + 0.5;
      if (random.nextInt(2) == 0) {
         level.addParticle(ModParticles.SOUL_FLAME, x + (random.nextDouble() - 0.5) * 0.5, y + 0.2, z + (random.nextDouble() - 0.5) * 0.5, 0.0, 0.05, 0.0);
      }

      if (random.nextInt(4) == 0) {
         level.addParticle(ParticleTypes.LARGE_SMOKE, x, y + 0.6, z, 0.0, 0.04, 0.0);
      }

      if (random.nextInt(30) == 0) {
         level.playLocalSound(x, y, z, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 0.4F, 0.6F + random.nextFloat() * 0.3F, false);
      }
   }
}
