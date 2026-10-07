package com.deathbound.block;

import com.deathbound.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

// Pip's ball, lying where it rolled under the footbridge. Use it (or punch it) to pick it up.
public class PipsBallBlock extends DecorBlock {
   public PipsBallBlock(Properties properties) {
      super(properties, Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0));
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (!level.isClientSide()) {
         level.removeBlock(pos, false);
         ItemStack ball = new ItemStack(ModItems.PIPS_BALL);
         if (!player.getInventory().add(ball)) {
            player.drop(ball, false, Prediction.SERVER_ONLY);
         }

         level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 1.0F, 1.3F);
      }

      return InteractionResult.SUCCESS;
   }
}
