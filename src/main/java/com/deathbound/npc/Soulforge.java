package com.deathbound.npc;

import com.deathbound.DeathBound;
import com.deathbound.registry.ModEntities;
import com.deathbound.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;

public final class Soulforge {
   public static final Soulforge.Tier[] TIERS = new Soulforge.Tier[]{
      new Soulforge.Tier(EquipmentSlot.HEAD, "helm", 1, 10, 1.0, 1.0, 0.04),
      new Soulforge.Tier(EquipmentSlot.CHEST, "chestplate", 2, 22, 2.0, 2.0, 0.08),
      new Soulforge.Tier(EquipmentSlot.LEGS, "leggings", 2, 16, 2.0, 1.0, 0.06),
      new Soulforge.Tier(EquipmentSlot.FEET, "boots", 1, 10, 1.0, 1.0, 0.04)
   };
   static final ResourceKey<TrimPattern> PATTERN = ResourceKey.create(Registries.TRIM_PATTERN, DeathBound.id("soulforged"));
   static final ResourceKey<TrimMaterial> MATERIAL = ResourceKey.create(Registries.TRIM_MATERIAL, DeathBound.id("soul"));

   public static boolean isForged(ItemStack stack) {
      ArmorTrim trim = stack.get(DataComponents.TRIM);
      return trim != null && trim.pattern().is(PATTERN);
   }

   public static double ward(Player player) {
      double w = 0.0;

      for (Soulforge.Tier t : TIERS) {
         if (isForged(player.getItemBySlot(t.slot()))) {
            w += t.ward();
         }
      }

      return w;
   }

   public static void forge(ServerPlayer player, String slotName) {
      Soulforge.Tier tier = null;

      for (Soulforge.Tier t : TIERS) {
         if (t.slot().getName().equals(slotName)) {
            tier = t;
         }
      }

      if (tier != null) {
         if (!Handiwork.busy(player)) {
            ItemStack piece = player.getItemBySlot(tier.slot());
            String key;
            if (piece.isEmpty() || !piece.has(DataComponents.EQUIPPABLE)) {
               key = "message.deathbound.soulforge.none";
            } else if (isForged(piece)) {
               key = "message.deathbound.soulforge.all";
            } else {
               if (count(player, ModItems.SOUL) >= tier.souls() && count(player, ModItems.GRAVE_RUNE) >= tier.runes()) {
                  take(player, ModItems.SOUL, tier.souls());
                  take(player, ModItems.GRAVE_RUNE, tier.runes());
                  Soulforge.Tier t = tier;
                  UnderworldNpc smith = player.level()
                     .getEntitiesOfClass(UnderworldNpc.class, player.getBoundingBox().inflate(10.0), n -> n.getType() == ModEntities.GRAVEDIGGER)
                     .stream()
                     .findFirst()
                     .orElse(null);
                  Handiwork.forge(player, smith, tier.slot(), s -> bind(player, s, t));
                  return;
               }

               key = "message.deathbound.soulforge.wanting";
            }

            player.sendSystemMessage(
               Component.translatable(key, Component.translatable("soulforge.deathbound." + tier.name()), tier.runes(), tier.souls())
                  .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            );
         }
      }
   }

   private static void bind(ServerPlayer player, ItemStack piece, Soulforge.Tier tier) {
      RegistryAccess reg = player.level().registryAccess();
      piece.set(
         DataComponents.TRIM,
         new ArmorTrim(reg.lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(MATERIAL), reg.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(PATTERN))
      );
      piece.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
      EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(tier.slot());
      String id = "soulforged_" + tier.slot().getName();
      piece.set(
         DataComponents.ATTRIBUTE_MODIFIERS,
         piece.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
            .withModifierAdded(Attributes.ARMOR, new AttributeModifier(DeathBound.id(id), tier.armor(), Operation.ADD_VALUE), group)
            .withModifierAdded(
               Attributes.ARMOR_TOUGHNESS, new AttributeModifier(DeathBound.id(id + "_toughness"), tier.toughness(), Operation.ADD_VALUE), group
            )
      );
   }

   public static ItemStack find(Player p, Item item) {
      for (ItemStack s : p.getInventory()) {
         if (s.is(item)) {
            return s;
         }
      }

      return ItemStack.EMPTY;
   }

   public static int count(Player p, Item item) {
      int n = 0;

      for (ItemStack s : p.getInventory()) {
         if (s.is(item)) {
            n += s.getCount();
         }
      }

      return n;
   }

   static void take(Player p, Item item, int amount) {
      for (int i = 0; i < p.getInventory().getContainerSize() && amount > 0; i++) {
         ItemStack s = p.getInventory().getItem(i);
         if (s.is(item)) {
            int t = Math.min(amount, s.getCount());
            s.shrink(t);
            amount -= t;
         }
      }
   }

   private Soulforge() {
   }

   public record Tier(EquipmentSlot slot, String name, int runes, int souls, double armor, double toughness, double ward) {
   }
}
