package com.deathbound.block;

import com.deathbound.registry.ModBlocks;
import com.deathbound.registry.ModFluids;
import com.deathbound.registry.ModItems;
import com.deathbound.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.WaterFluid;

public abstract class SoulwaterFluid extends WaterFluid {
   @Override
   public Fluid getFlowing() {
      return ModFluids.FLOWING_SOULWATER;
   }

   @Override
   public Fluid getSource() {
      return ModFluids.SOULWATER;
   }

   @Override
   public Item getBucket() {
      return ModItems.SOULWATER_BUCKET;
   }

   @Override
   public BlockState createLegacyBlock(FluidState state) {
      return ModBlocks.SOULWATER.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
   }

   @Override
   public boolean isSame(Fluid fluid) {
      return fluid == ModFluids.SOULWATER || fluid == ModFluids.FLOWING_SOULWATER;
   }

   @Override
   public void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
      if (state.isSource() && random.nextInt(36) == 0 && level.getBlockState(pos.above()).isAir()) {
         level.addParticle(ModParticles.SOUL_MOTE, pos.getX() + random.nextDouble(), pos.getY() + 0.92, pos.getZ() + random.nextDouble(), 0.0, 0.015, 0.0);
      }
   }

   @Override
   public ParticleOptions getDripParticle() {
      return ParticleTypes.DRIPPING_OBSIDIAN_TEAR;
   }

   public static class Flowing extends SoulwaterFluid {
      @Override
      protected void createFluidStateDefinition(Builder<Fluid, FluidState> builder) {
         super.createFluidStateDefinition(builder);
         builder.add(LEVEL);
      }

      @Override
      public int getAmount(FluidState state) {
         return state.getValue(LEVEL);
      }

      @Override
      public boolean isSource(FluidState state) {
         return false;
      }
   }

   public static class Source extends SoulwaterFluid {
      @Override
      public int getAmount(FluidState state) {
         return 8;
      }

      @Override
      public boolean isSource(FluidState state) {
         return true;
      }
   }
}
