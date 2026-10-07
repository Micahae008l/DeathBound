package com.deathbound.npc;

import com.deathbound.DeathBound;
import com.deathbound.registry.ModAttachments;
import com.deathbound.registry.ModBlocks;
import com.deathbound.registry.ModItems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class Quests {
   public static final List<Quests.Quest> ALL = List.of(
      new Quests.Quest("mira", "gravedigger", 3),
      new Quests.Quest("oar", "ferryman", 2),
      new Quests.Quest("lamps", "lamplighter", 2),
      new Quests.Quest("ball", "kid", 2),
      new Quests.Quest("name", "sentry", 2)
   );

   public static Map<String, Integer> all(ServerPlayer p) {
      return p.getAttachedOrElse(ModAttachments.QUESTS, Map.of());
   }

   public static int stage(ServerPlayer p, String id) {
      return all(p).getOrDefault(id, 0);
   }

   static void set(ServerPlayer p, String id, int stage) {
      Map<String, Integer> m = new HashMap<>(all(p));
      m.put(id, stage);
      p.setAttached(ModAttachments.QUESTS, m);
   }

   static void open(ServerPlayer p, String key) {
      p.level().registryAccess().lookupOrThrow(Registries.DIALOG).get(ResourceKey.create(Registries.DIALOG, DeathBound.id(key))).ifPresent(p::openDialog);
   }

   static boolean has(ServerPlayer p, Item item, int n) {
      return Soulforge.count(p, item) >= n;
   }

   static void give(ServerPlayer p, ItemStack s) {
      if (!p.getInventory().add(s)) {
         p.drop(s, false, Prediction.SERVER_ONLY);
      }
   }

   static void note(ServerPlayer p, String id, boolean done) {
      p.sendSystemMessage(
         Component.translatable(
               done ? "quest.deathbound.journal_done" : "quest.deathbound.journal_new", Component.translatable("quest.deathbound." + id + ".title")
            )
            .withStyle(ChatFormatting.LIGHT_PURPLE)
      );
      p.level()
         .playSound(null, p.blockPosition(), done ? SoundEvents.PLAYER_LEVELUP : SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8F, done ? 0.7F : 1.0F);
   }

   public static void talk(ServerPlayer p, String who) {
      if (who.equals("mira")) {
         int s = stage(p, "mira");
         if (s == 1 && has(p, ModItems.ALDOUS_LANTERN, 1)) {
            Soulforge.take(p, ModItems.ALDOUS_LANTERN, 1);
            Rewards.give(p, from("mira"), new ItemStack(ModItems.MIRAS_RIBBON));
            set(p, "mira", 2);
            open(p, "quest.mira.met");
         } else {
            open(p, s >= 2 ? "quest.mira.met_after" : "quest.mira.alone");
         }
      } else {
         for (Quests.Quest q : ALL) {
            if (q.giver().equals(who)) {
               int s = stage(p, q.id());
               String id = q.id();
               if (s == 0) {
                  open(p, "quest." + id + ".offer");
               } else if (s >= q.last()) {
                  open(p, "quest." + id + ".after");
               } else if (turnIn(p, q, s)) {
                  set(p, id, q.last());
                  note(p, id, true);
                  open(p, "quest." + id + ".done");
               } else {
                  open(p, "quest." + id + ".wait" + (id.equals("mira") && s == 2 ? "2" : ""));
               }

               return;
            }
         }
      }
   }

   private static boolean turnIn(ServerPlayer p, Quests.Quest q, int s) {
      List<ItemStack> pay = new ArrayList<>();
      switch (q.id()) {
         case "mira":
            if (s != 2 || !has(p, ModItems.MIRAS_RIBBON, 1)) {
               return false;
            }

            Soulforge.take(p, ModItems.MIRAS_RIBBON, 1);
            pay.add(new ItemStack(ModItems.GRAVE_RUNE, 2));
            pay.add(new ItemStack(ModItems.SOUL, 12));
            break;
         case "oar":
            if (!has(p, ModItems.FERRYMANS_OAR, 1)) {
               return false;
            }

            Soulforge.take(p, ModItems.FERRYMANS_OAR, 1);
            pay.add(new ItemStack(ModItems.FERRYMANS_CHARM));
            pay.add(new ItemStack(ModItems.SOUL, 8));
            break;
         case "lamps":
            if (!has(p, ModBlocks.SOUL_JAR.asItem(), 3)) {
               return false;
            }

            Soulforge.take(p, ModBlocks.SOUL_JAR.asItem(), 3);
            pay.add(new ItemStack(ModItems.LANTERN_WISP));
            pay.add(new ItemStack(ModItems.SOUL, 6));
            break;
         case "ball":
            if (!has(p, ModItems.PIPS_BALL, 1)) {
               return false;
            }

            Soulforge.take(p, ModItems.PIPS_BALL, 1);
            pay.add(new ItemStack(ModItems.GRAVE_RUNE, 1));
            pay.add(new ItemStack(ModItems.SOUL, 8));
            break;
         case "name":
            if (!has(p, ModItems.SENTRYS_TAG, 1)) {
               return false;
            }

            Soulforge.take(p, ModItems.SENTRYS_TAG, 1);
            pay.add(new ItemStack(ModItems.GRAVE_RUNE, 2));
            pay.add(new ItemStack(ModItems.SOUL, 10));
            break;
         default:
            return false;
      }

      Rewards.give(p, from(q.giver()), pay.toArray(ItemStack[]::new));
      return true;
   }

   static Component from(String who) {
      Component name = who.equals("kid") ? Component.literal("Pip") : Component.translatable("entity.deathbound." + who);
      return Component.translatable("rewards.deathbound.from", name);
   }

   public static void accept(ServerPlayer p, String id) {
      if (!ALL.stream().noneMatch(q -> q.id().equals(id)) && stage(p, id) == 0) {
         set(p, id, 1);
         List<ItemStack> handed = new ArrayList<>();
         if (id.equals("mira")) {
            handed.add(new ItemStack(ModItems.ALDOUS_LANTERN));
         }

         if (!has(p, ModItems.UNDERWORLD_JOURNAL, 1)) {
            handed.add(new ItemStack(ModItems.UNDERWORLD_JOURNAL));
         }

         Quests.Quest quest = ALL.stream().filter(x -> x.id().equals(id)).findFirst().orElseThrow();
         Rewards.give(p, from(quest.giver()), handed.toArray(ItemStack[]::new));
         note(p, id, false);
         p.closeContainer();
      }
   }

   private Quests() {
   }

   public record Quest(String id, String giver, int last) {
   }
}
