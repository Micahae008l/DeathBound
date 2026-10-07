package com.deathbound.block;

import com.deathbound.registry.ModParticles;
import com.deathbound.world.UnderworldTravel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SoulRiftBlock extends Block {
   public SoulRiftBlock(Properties properties) {
      super(properties);
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return Shapes.block();
   }

   @Override
   protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
      if (entity instanceof ServerPlayer player && !player.isOnPortalCooldown()) {
         player.setPortalCooldown();
         UnderworldTravel.returnToLife(player, false);
      }
   }

   @Override
   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      for (int i = 0; i < 3; i++) {
         level.addParticle(
            ModParticles.SOUL_MOTE,
            pos.getX() + random.nextDouble(),
            pos.getY() + random.nextDouble(),
            pos.getZ() + random.nextDouble(),
            (random.nextDouble() - 0.5) * 0.05,
            0.06,
            (random.nextDouble() - 0.5) * 0.05
         );
      }
   }
}
