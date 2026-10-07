package com.deathbound.block;

import com.deathbound.registry.ModParticles;
import com.deathbound.world.Puzzles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GraveLampBlock extends Block {
   public static final BooleanProperty LIT = BlockStateProperties.LIT;
   private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 9.0, 12.0);

   public GraveLampBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(LIT);
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (level instanceof ServerLevel server) {
         boolean lit = !state.getValue(LIT);
         level.setBlock(pos, state.setValue(LIT, lit), 3);
         level.playSound(null, pos, lit ? SoundEvents.FIRECHARGE_USE : SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, lit ? 0.6F : 0.8F);
         if (lit) {
            Puzzles.onLampLit(server, pos, player);
         }
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (state.getValue(LIT) && random.nextInt(3) == 0) {
         level.addParticle(ModParticles.SOUL_FLAME, pos.getX() + 0.5, pos.getY() + 0.75, pos.getZ() + 0.5, 0.0, 0.01, 0.0);
      }
   }
}
