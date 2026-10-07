package com.deathbound.block;

import com.deathbound.story.Stories;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// A note left by the dead: pinned to whatever is behind it (FACING points away from it) or lying FLAT on a table.
// STORY is the note's index in Stories.NOTES. Right-click to read it.
public class StoryNoteBlock extends DecorBlock {
   public static final IntegerProperty STORY = IntegerProperty.create("story", 0, 15);
   public static final BooleanProperty FLAT = BooleanProperty.create("flat");
   private static final VoxelShape PINNED_NORTH = Block.box(3.0, 2.0, 15.0, 13.0, 14.0, 16.0);
   private static final VoxelShape PINNED_SOUTH = Block.box(3.0, 2.0, 0.0, 13.0, 14.0, 1.0);
   private static final VoxelShape PINNED_EAST = Block.box(0.0, 2.0, 3.0, 1.0, 14.0, 13.0);
   private static final VoxelShape PINNED_WEST = Block.box(15.0, 2.0, 3.0, 16.0, 14.0, 13.0);
   private static final VoxelShape LYING = Block.box(2.0, 0.0, 2.0, 14.0, 1.0, 14.0);

   public StoryNoteBlock(Properties properties) {
      super(properties, LYING);
      this.registerDefaultState(this.defaultBlockState().setValue(STORY, 0).setValue(FLAT, false));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(STORY, FLAT);
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      if (state.getValue(FLAT)) {
         return LYING;
      }

      return switch (state.getValue(FACING)) {
         case SOUTH -> PINNED_SOUTH;
         case EAST -> PINNED_EAST;
         case WEST -> PINNED_WEST;
         default -> PINNED_NORTH;
      };
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return Shapes.empty();
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      int i = state.getValue(STORY);
      if (i >= Stories.NOTES.size()) {
         return InteractionResult.PASS;
      }

      String id = Stories.NOTES.get(i);
      if (level.isClientSide()) {
         Stories.opener.accept(id);
      } else if (player instanceof ServerPlayer sp) {
         Stories.found(sp, id);
      }

      return InteractionResult.SUCCESS;
   }
}
