package com.deathbound.registry;

import com.deathbound.DeathBound;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public final class ModAttachments {
   public static final AttachmentType<GlobalPos> RETURN_POINT = AttachmentRegistry.create(
      DeathBound.id("return_point"), b -> b.persistent(GlobalPos.CODEC).copyOnDeath()
   );
   public static final AttachmentType<List<ModAttachments.Kept>> SOUL_STASH = AttachmentRegistry.create(
      DeathBound.id("soul_stash"), b -> b.persistent(ModAttachments.Kept.CODEC.listOf()).copyOnDeath()
   );
   public static final AttachmentType<List<ItemStack>> FERRY_KEEP = AttachmentRegistry.create(
      DeathBound.id("ferry_keep"), b -> b.persistent(ItemStack.CODEC.listOf()).copyOnDeath()
   );
   public static final AttachmentType<List<String>> COLLECTED = AttachmentRegistry.create(
      DeathBound.id("collected"), b -> b.persistent(Codec.STRING.listOf()).copyOnDeath()
   );
   public static final AttachmentType<Long> CROWNED = AttachmentRegistry.create(
      DeathBound.id("crowned"), b -> b.persistent(Codec.LONG).copyOnDeath().syncWith(ByteBufCodecs.VAR_LONG.cast(), AttachmentSyncPredicate.all())
   );
   public static final AttachmentType<List<String>> HEARD = AttachmentRegistry.create(
      DeathBound.id("heard"), b -> b.persistent(Codec.STRING.listOf()).copyOnDeath()
   );
   /** Stories (Stories.BOOKS / NOTES ids) this player has read, in the order found: kept in the Journal. */
   public static final AttachmentType<List<String>> STORIES_FOUND = AttachmentRegistry.create(
      DeathBound.id("stories_found"),
      b -> b.persistent(Codec.STRING.listOf())
         .copyOnDeath()
         .syncWith(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), AttachmentSyncPredicate.targetOnly())
   );
   /** On the overworld: chest stories already handed out in this world, so each turns up only once. */
   public static final AttachmentType<List<String>> CLAIMED_STORIES = AttachmentRegistry.create(
      DeathBound.id("claimed_stories"), b -> b.persistent(Codec.STRING.listOf())
   );
   // what the Journal shows alongside a quest, e.g. the last clue about Mira ("translation key|side")
   public static final AttachmentType<Map<String, String>> QUEST_NOTES = AttachmentRegistry.create(
      DeathBound.id("quest_notes"),
      b -> b.persistent(Codec.unboundedMap(Codec.STRING, Codec.STRING))
         .copyOnDeath()
         .syncWith(ByteBufCodecs.<io.netty.buffer.ByteBuf, String, String, Map<String, String>>map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8), AttachmentSyncPredicate.targetOnly())
   );
   public static final AttachmentType<Map<String, Integer>> QUESTS = AttachmentRegistry.create(
      DeathBound.id("quests"),
      b -> b.persistent(Codec.unboundedMap(Codec.STRING, Codec.INT))
         .copyOnDeath()
         .syncWith(ByteBufCodecs.<io.netty.buffer.ByteBuf, String, Integer, Map<String, Integer>>map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT), AttachmentSyncPredicate.targetOnly())
   );

   /** Lore books filed into the Underworld Journal (see LoreFiling). */
   public static final AttachmentType<List<ItemStack>> LORE_PAGES = AttachmentRegistry.create(
      DeathBound.id("lore_pages"),
      b -> b.persistent(ItemStack.CODEC.listOf()).copyOnDeath().syncWith(ItemStack.OPTIONAL_LIST_STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
   );

   public static boolean crowned(Entity e) {
      return e.hasAttached(CROWNED);
   }

   public static void init() {
   }

   private ModAttachments() {
   }

   public record Kept(int slot, ItemStack stack) {
      public static final Codec<ModAttachments.Kept> CODEC = RecordCodecBuilder.create(
         i -> i.group(Codec.INT.fieldOf("slot").forGetter(ModAttachments.Kept::slot), ItemStack.CODEC.fieldOf("stack").forGetter(ModAttachments.Kept::stack))
            .apply(i, ModAttachments.Kept::new)
      );
   }
}
