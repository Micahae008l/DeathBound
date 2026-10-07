package com.deathbound.npc;

import com.deathbound.registry.ModBlocks;
import com.deathbound.registry.ModEffects;
import com.deathbound.registry.ModEntities;
import com.deathbound.registry.ModItems;
import com.deathbound.registry.ModParticles;
import com.deathbound.registry.ModSounds;
import java.util.Locale;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class UnderworldNpc extends AbstractVillager {
   public static final byte TALK_EVENT = 93;
   public static final byte TEMPER_EVENT = 94;
   public static final byte FORGE_EVENT = 95;
   public final AnimationState temperAnim = new AnimationState();
   private static final EntityDataAccessor<Float> CORRUPTION = SynchedEntityData.defineId(UnderworldNpc.class, EntityDataSerializers.FLOAT);
   public final AnimationState talkAnim = new AnimationState();
   public final AnimationState idleAnim = new AnimationState();
   private float work;
   private float workO;
   private int forgingUntil;

   public float work(float partialTicks) {
      return Mth.lerp(partialTicks, this.workO, this.work);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.kind() == UnderworldNpc.Kind.PROPHET) {
         this.setYRot(0.0F);
         this.yRotO = 0.0F;
         this.yBodyRot = 0.0F;
         this.yBodyRotO = 0.0F;
         this.yHeadRot = Mth.clamp(Mth.wrapDegrees(this.yHeadRot), -55.0F, 55.0F);
         this.yHeadRotO = Mth.clamp(Mth.wrapDegrees(this.yHeadRotO), -55.0F, 55.0F);
      }

      if (this.level().isClientSide()) {
         if (this.random.nextInt(320) == 0) {
            this.idleAnim.start(this.tickCount);
         }

         this.workO = this.work;
         boolean forging = this.tickCount < this.forgingUntil;
         boolean company = !forging && this.level().getNearestPlayer(this, 4.5) != null;
         this.work = this.work + ((company ? 0.0F : 1.0F) - this.work) * 0.08F;
         if (!(this.work < 0.5F)) {
            UnderworldNpc.Kind kind = this.kind();
            Vec3 front = this.position().add(Vec3.directionFromRotation(0.0F, this.yBodyRot).scale(0.9));
            if (kind == UnderworldNpc.Kind.GRAVEDIGGER && this.tickCount % 26 == 18) {
               this.level()
                  .playLocalSound(
                     front.x,
                     this.getY() + 0.9,
                     front.z,
                     SoundEvents.ANVIL_USE,
                     SoundSource.NEUTRAL,
                     forging ? 0.9F : 0.35F,
                     0.9F + this.random.nextFloat() * 0.2F,
                     false
                  );

               for (int i = 0; i < (forging ? 18 : 6); i++) {
                  this.level()
                     .addParticle(
                        ParticleTypes.ELECTRIC_SPARK,
                        front.x,
                        this.getY() + 1.0,
                        front.z,
                        (this.random.nextDouble() - 0.5) * 0.3,
                        0.15,
                        (this.random.nextDouble() - 0.5) * 0.3
                     );
               }
            } else if (kind == UnderworldNpc.Kind.COLLECTOR && this.tickCount % 140 == 80) {
               this.level().playLocalSound(front.x, this.getY() + 0.9, front.z, SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 0.25F, 1.4F, false);
            } else if (kind == UnderworldNpc.Kind.BONESMITH) {
               int c = this.tickCount % 160;
               Vec3 water = this.position().add(Vec3.directionFromRotation(0.0F, this.yBodyRot).scale(1.6));
               if (c == 52) {
                  this.level().playLocalSound(water.x, water.y, water.z, SoundEvents.GENERIC_SPLASH, SoundSource.NEUTRAL, 0.4F, 1.3F, false);
                  this.level().playLocalSound(water.x, water.y, water.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.35F, 0.7F, false);
               }

               if (c > 52 && c < 92 && c % 3 == 0) {
                  this.level()
                     .addParticle(
                        ParticleTypes.CLOUD,
                        water.x + (this.random.nextDouble() - 0.5) * 0.4,
                        water.y + 0.2,
                        water.z + (this.random.nextDouble() - 0.5) * 0.4,
                        0.0,
                        0.06,
                        0.0
                     );
                  this.level().addParticle(ModParticles.SOUL_MOTE, water.x, water.y + 0.1, water.z, 0.0, 0.03, 0.0);
               }

               if (c == 118 || c == 126) {
                  this.level().playLocalSound(front.x, this.getY() + 0.8, front.z, SoundEvents.SKELETON_STEP, SoundSource.NEUTRAL, 0.6F, 0.5F, false);
               }
            } else if (kind == UnderworldNpc.Kind.FERRYMAN && this.tickCount % 90 == 20) {
               this.level().playLocalSound(front.x, this.getY(), front.z, SoundEvents.WOOD_HIT, SoundSource.NEUTRAL, 0.3F, 0.6F, false);
            }
         }
      }
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(CORRUPTION, 0.0F);
   }

   public float corruption() {
      return this.entityData.get(CORRUPTION);
   }

   public void setCorruption(float c) {
      this.entityData.set(CORRUPTION, Mth.clamp(c, 0.0F, 1.0F));
   }

   // bump when updateTrades changes so NPCs in existing worlds pick up the new shop
   private static final int TRADES = 2;

   @Override
   public void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putFloat("corruption", this.corruption());
      output.putInt("trades", TRADES);
   }

   @Override
   public void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.setCorruption(input.getFloatOr("corruption", 0.0F));
      if (input.getIntOr("trades", 0) < TRADES) {
         // the shop changed since this NPC was saved: restock with the current trades
         this.offers = null;
      }
   }

   public UnderworldNpc(EntityType<? extends UnderworldNpc> type, Level level) {
      super(type, level);
      if (type == ModEntities.PROPHET || type == ModEntities.MIRA || type == ModEntities.SENTRY) {
         this.setNoGravity(true);
      }
   }

   public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
   }

   public UnderworldNpc.Kind kind() {
      EntityType<?> t = this.getType();
      return t == ModEntities.FERRYMAN
         ? UnderworldNpc.Kind.FERRYMAN
         : (
            t == ModEntities.GRAVEDIGGER
               ? UnderworldNpc.Kind.GRAVEDIGGER
               : (
                  t == ModEntities.COLLECTOR
                     ? UnderworldNpc.Kind.COLLECTOR
                     : (
                        t == ModEntities.BONESMITH
                           ? UnderworldNpc.Kind.BONESMITH
                           : (
                              t == ModEntities.MIRA
                                 ? UnderworldNpc.Kind.MIRA
                                 : (
                                    t == ModEntities.LAMPLIGHTER
                                       ? UnderworldNpc.Kind.LAMPLIGHTER
                                       : (t == ModEntities.SENTRY ? UnderworldNpc.Kind.SENTRY : UnderworldNpc.Kind.PROPHET)
                                 )
                           )
                     )
               )
         );
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 10.0F, 1.0F));
      this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (hand == InteractionHand.MAIN_HAND && this.isAlive()) {
         if (player instanceof ServerPlayer sp && this.level() instanceof ServerLevel level) {
            this.getLookControl().setLookAt(player);
            this.playSound(ModSounds.MURMUR, 0.8F, this.kind().voice);
            level.broadcastEntityEvent(this, (byte)93);
            Talk.open(sp, this.kind().id());
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public void openTrade(ServerPlayer player) {
      if (!this.isTrading() && !this.getOffers().isEmpty()) {
         this.setTradingPlayer(player);
         this.openTradingScreen(player, this.getDisplayName(), 1);
      }
   }

   @Override
   public void handleEntityEvent(byte id) {
      if (id == 93) {
         this.talkAnim.start(this.tickCount);
      } else if (id == 94) {
         this.temperAnim.start(this.tickCount);
      } else if (id == 95) {
         this.forgingUntil = this.tickCount + 58;
      } else {
         super.handleEntityEvent(id);
      }
   }

   @Override
   protected void updateTrades(ServerLevel level) {
      MerchantOffers o = this.getOffers();
      switch (this.kind()) {
         case FERRYMAN:
            o.add(sell(2, new ItemStack(ModBlocks.WRAITH_LANTERN, 3), 999));
            o.add(sell(5, PotionContents.createItemStack(Items.POTION, Potions.LONG_NIGHT_VISION), 999));
            o.add(sell(4, PotionContents.createItemStack(Items.SPLASH_POTION, Potions.STRONG_HEALING), 999));
            o.add(sell(3, new ItemStack(Items.ENDER_PEARL, 2), 999));
            o.add(sell(8, new ItemStack(ModItems.FERRYMANS_CHARM), 999));
            break;
         case GRAVEDIGGER:
            o.add(new MerchantOffer(new ItemCost(Items.BONE, 12), new ItemStack(ModItems.SOUL, 1), 999, 0, 0.0F));
            o.add(sell(1, new ItemStack(Items.BREAD, 6), 999));
            o.add(sell(2, new ItemStack(Items.COOKED_MUTTON, 5), 999));
            o.add(sell(2, new ItemStack(Items.ARROW, 16), 999));
            o.add(sell(3, new ItemStack(Blocks.TORCH, 16), 999));
            o.add(sell(4, new ItemStack(Items.IRON_SWORD), 999));
            o.add(sell(3, new ItemStack(Items.SHIELD), 999));
            o.add(sell(3, new ItemStack(Items.BOW), 999));
            o.add(sell(3, new ItemStack(Items.CHAINMAIL_HELMET), 999));
            o.add(sell(5, new ItemStack(Items.CHAINMAIL_CHESTPLATE), 999));
            o.add(sell(4, new ItemStack(Items.CHAINMAIL_LEGGINGS), 999));
            o.add(sell(3, new ItemStack(Items.CHAINMAIL_BOOTS), 999));
            o.add(sell(6, new ItemStack(Items.GOLDEN_APPLE, 1), 999));
            o.add(sell(24, new ItemStack(Items.TOTEM_OF_UNDYING), 1));
         case PROPHET:
         default:
            break;
         case COLLECTOR:
            o.add(sell(2, new ItemStack(ModBlocks.SOUL_JAR, 2), 999));
            o.add(sell(3, new ItemStack(ModBlocks.EYE_JAR), 999));
            o.add(sell(8, PotionContents.createItemStack(Items.POTION, ModEffects.GRAVE_SIGHT_POTION), 999));
            o.add(sell(6, new ItemStack(Items.SPYGLASS), 999));
            o.add(sell(10, new ItemStack(Items.RECOVERY_COMPASS), 999));
            o.add(sell(16, new ItemStack(ModItems.COLLECTORS_CHARM), 1));
      }
   }

   private static MerchantOffer sell(int souls, ItemStack result, int uses) {
      return new MerchantOffer(new ItemCost(ModItems.SOUL, souls), result, uses, 0, 0.0F);
   }

   @Override
   protected void rewardTradeXp(MerchantOffer offer) {
   }

   @Override
   public boolean showProgressBar() {
      return false;
   }

   @Override
   public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
      return null;
   }

   @Override
   public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
      return false;
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   protected void doPush(Entity entity) {
   }

   @Override
   public boolean removeWhenFarAway(double distSqr) {
      return false;
   }

   @Override
   public boolean requiresCustomPersistence() {
      return true;
   }

   @Override
   public boolean canBeLeashed() {
      return false;
   }

   @Override
   protected @Nullable SoundEvent getAmbientSound() {
      return this.kind() == UnderworldNpc.Kind.PROPHET ? ModSounds.WHISPER : null;
   }

   @Override
   public int getAmbientSoundInterval() {
      return 300;
   }

   @Override
   protected @Nullable SoundEvent getHurtSound(DamageSource source) {
      return null;
   }

   @Override
   public SoundEvent getNotifyTradeSound() {
      return ModSounds.MURMUR;
   }

   @Override
   protected SoundEvent getTradeUpdatedSound(boolean validTrade) {
      return ModSounds.MURMUR;
   }

   public enum Kind {
      FERRYMAN(0.75F),
      GRAVEDIGGER(1.0F),
      PROPHET(0.6F),
      COLLECTOR(1.15F),
      BONESMITH(0.45F),
      MIRA(1.6F),
      LAMPLIGHTER(0.85F),
      SENTRY(0.7F);

      final float voice;

      Kind(float voice) {
         this.voice = voice;
      }

      public String id() {
         return this.name().toLowerCase(Locale.ROOT);
      }
   }
}
