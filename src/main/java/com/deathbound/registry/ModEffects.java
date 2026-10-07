package com.deathbound.registry;

import com.deathbound.DeathBound;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.alchemy.Potion;

public final class ModEffects {
   public static final Holder<MobEffect> WRAITH_FURY = effect(
      "wraith_fury",
      new ModEffects.Plain(MobEffectCategory.BENEFICIAL, 9059568)
         .addAttributeModifier(Attributes.MOVEMENT_SPEED, DeathBound.id("effect.wraith_fury.speed"), 0.05, Operation.ADD_MULTIPLIED_TOTAL)
         .addAttributeModifier(Attributes.ATTACK_DAMAGE, DeathBound.id("effect.wraith_fury.damage"), 1.0, Operation.ADD_VALUE)
   );
   public static final Holder<MobEffect> GRAVE_SIGHT = effect("grave_sight", new ModEffects.GraveSight());
   public static final Holder<MobEffect> WARDING = effect("warding", new ModEffects.Plain(MobEffectCategory.BENEFICIAL, 7101086));
   public static final Holder<MobEffect> MARKED = effect("marked", new ModEffects.Plain(MobEffectCategory.HARMFUL, 8007654));
   public static final Holder<MobEffect> LAST_BREATH = effect("last_breath", new ModEffects.Plain(MobEffectCategory.BENEFICIAL, 14075893));
   public static final Holder<Potion> GRAVE_SIGHT_POTION = potion("grave_sight", new MobEffectInstance(GRAVE_SIGHT, 3600));
   public static final Holder<Potion> LONG_GRAVE_SIGHT_POTION = potion("long_grave_sight", "grave_sight", new MobEffectInstance(GRAVE_SIGHT, 9600));
   public static final Holder<Potion> WARDING_POTION = potion("warding", new MobEffectInstance(WARDING, 3600));
   public static final Holder<Potion> LONG_WARDING_POTION = potion("long_warding", "warding", new MobEffectInstance(WARDING, 9600));
   public static final Holder<Potion> LAST_BREATH_POTION = potion("last_breath", new MobEffectInstance(LAST_BREATH, 6000));

   private static Holder<MobEffect> effect(String name, MobEffect effect) {
      return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, DeathBound.id(name), effect);
   }

   private static Holder<Potion> potion(String id, MobEffectInstance effect) {
      return potion(id, id, effect);
   }

   private static Holder<Potion> potion(String id, String name, MobEffectInstance effect) {
      return Registry.registerForHolder(BuiltInRegistries.POTION, DeathBound.id(id), new Potion("deathbound." + name, effect));
   }

   public static void init() {
      ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
         if (entity.hasEffect(LAST_BREATH) && entity.level() instanceof ServerLevel level) {
            entity.removeEffect(LAST_BREATH);
            entity.setHealth(6.0F);
            entity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 3, false, false, true));
            level.playSound(null, entity.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.7F, 1.4F);
            level.sendParticles(ModParticles.SOUL_FLAME, entity.getX(), entity.getY() + 1.0, entity.getZ(), 60, 0.4, 0.8, 0.4, 0.06);
            return false;
         } else {
            return true;
         }
      });
   }

   private ModEffects() {
   }

   private static class GraveSight extends MobEffect {
      GraveSight() {
         super(MobEffectCategory.BENEFICIAL, 10189792);
      }

      @Override
      public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
         return tickCount % 20 == 0;
      }

      @Override
      public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplification) {
         for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(24.0), ex -> ex instanceof Enemy && ex.isAlive())) {
            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, true, false, false));
         }

         return true;
      }
   }

   private static class Plain extends MobEffect {
      Plain(MobEffectCategory category, int color) {
         super(category, color);
      }
   }
}
