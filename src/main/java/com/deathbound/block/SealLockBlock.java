package com.deathbound.block;

import com.deathbound.world.Puzzles;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;

public class SealLockBlock extends DecorBlock {
   public static final BooleanProperty FILLED = BooleanProperty.create("filled");
   public final int kind;

   public SealLockBlock(Properties properties, int kind) {
      super(properties, Shapes.block());
      this.kind = kind;
      this.registerDefaultState(this.defaultBlockState().setValue(FILLED, false));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(FILLED);
   }

   @Override
   protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (state.getValue(FILLED)) {
         return InteractionResult.PASS;
      }

      if (!stack.is(Puzzles.sigil(this.kind))) {
         return InteractionResult.TRY_WITH_EMPTY_HAND;
      }

      if (level instanceof ServerLevel server) {
         stack.consume(1, player);
         level.setBlock(pos, state.setValue(FILLED, true), 3);
         Puzzles.onLockFilled(server, pos, this.kind, player);
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (!state.getValue(FILLED) && !level.isClientSide()) {
         player.sendOverlayMessage(Component.translatable("puzzle.deathbound.lock_wants", Component.translatable(Puzzles.sigil(this.kind).getDescriptionId())));
      }

      return InteractionResult.SUCCESS;
   }
}
