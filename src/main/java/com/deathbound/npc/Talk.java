package com.deathbound.npc;

import com.deathbound.DeathBound;
import com.deathbound.registry.ModAttachments;
import com.deathbound.world.Director;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Custom;
import net.minecraft.network.chat.ClickEvent.ShowDialog;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.MultiActionDialog;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.level.ServerPlayer;

public final class Talk {
   private static final int INLINE = 2;
   private static final Pattern QUEST = Pattern.compile("(\\w+)(>=|<=|=|<|>)(\\d+)");

   static Set<String> heard(ServerPlayer p) {
      return new HashSet<>(p.getAttachedOrElse(ModAttachments.HEARD, List.of()));
   }

   static void hear(ServerPlayer p, String key) {
      Set<String> s = heard(p);
      if (s.add(key)) {
         p.setAttached(ModAttachments.HEARD, new ArrayList<>(s));
      }
   }

   static boolean when(ServerPlayer p, String cond) {
      if (cond.isEmpty()) {
         return true;
      }

      Director.State st = Director.storyState(p.level());

      for (String c : cond.split(",")) {
         boolean not = c.startsWith("!");
         String t = not ? c.substring(1) : c;

         boolean v = switch (t) {
            case "warden" -> st.progress() >= 1;
            case "slain" -> st.progress() >= 2;
            case "hunter" -> st.hunterSlain();
            case "ended" -> st.ending() > 0;
            case "end1", "end2", "end3" -> st.ending() == t.charAt(3) - '0';
            default -> !t.startsWith("q:") || quest(p, t.substring(2));
         };
         if (v == not) {
            return false;
         }
      }

      return true;
   }

   private static boolean quest(ServerPlayer p, String expr) {
      Matcher m = QUEST.matcher(expr);
      if (!m.matches()) {
         return false;
      }

      int s = Quests.stage(p, m.group(1));
      int n = Integer.parseInt(m.group(3));

      return switch (m.group(2)) {
         case ">=" -> s >= n;
         case "<=" -> s <= n;
         case "<" -> s < n;
         case ">" -> s > n;
         default -> s == n;
      };
   }

   static List<Talk.Topic> topics(ServerPlayer p, Talk.Npc npc) {
      return npc.topics().stream().filter(t -> when(p, t.when())).toList();
   }

   static Optional<Reference<Dialog>> page(ServerPlayer p, String key) {
      return p.level().registryAccess().lookupOrThrow(Registries.DIALOG).get(ResourceKey.create(Registries.DIALOG, DeathBound.id(key)));
   }

   public static void open(ServerPlayer p, String id) {
      Talk.Npc npc = TalkData.NPCS.get(id);
      if (npc != null) {
         Set<String> heard = heard(p);
         String fresh = null;
         String current = null;

         for (String[] g : npc.greet()) {
            if (g[0].startsWith("q:") && when(p, g[0]) && !heard.contains(g[1])) {
               fresh = g[1];
               break;
            }
         }

         for (String[] g : npc.greet()) {
            if (!g[0].startsWith("q:") && when(p, g[0])) {
               current = g[1];
               break;
            }
         }

         if (fresh == null && current != null && !heard.contains(current)) {
            fresh = current;
         }

         String key = fresh != null ? fresh : current;
         if (key != null) {
            MultiActionDialog greet = page(p, key).map(h -> h.value()).filter(d -> d instanceof MultiActionDialog).map(d -> (MultiActionDialog)d).orElse(null);
            if (greet != null) {
               List<DialogBody> body;
               if (fresh != null) {
                  body = greet.common().body();
                  hear(p, key);
               } else {
                  body = List.of(line(greet, npc.backs().get(p.getRandom().nextInt(npc.backs().size()))));
               }

               List<ActionButton> buttons = new ArrayList<>();
               List<Talk.Topic> topics = topics(p, npc);
               if (topics.size() > 2) {
                  boolean anyNew = topics.stream().anyMatch(t -> !heard.contains(t.key()));
                  buttons.add(button(mark(Component.translatable("talk.deathbound.ask"), anyNew), custom("ask", id)));
               } else {
                  topics.forEach(t -> buttons.add(topicButton(id, t, heard)));
               }

               for (Talk.Service s : npc.services()) {
                  if (when(p, s.when())) {
                     buttons.add(button(Component.literal(s.label()), target(p, s.target())));
                  }
               }

               show(p, key, greet.common().title(), body, buttons, button(Component.translatable("talk.deathbound.leave"), null));
            }
         }
      }
   }

   public static void ask(ServerPlayer p, String id) {
      Talk.Npc npc = TalkData.NPCS.get(id);
      if (npc != null) {
         Set<String> heard = heard(p);
         String key = npc.greet().stream().filter(g -> when(p, g[0])).findFirst().map(g -> (String)g[1]).orElse(id);
         MultiActionDialog greet = page(p, key).map(h -> h.value()).filter(d -> d instanceof MultiActionDialog).map(d -> (MultiActionDialog)d).orElse(null);
         if (greet != null) {
            List<ActionButton> buttons = new ArrayList<>();
            topics(p, npc).forEach(t -> buttons.add(topicButton(id, t, heard)));
            show(
               p,
               id + "_ask",
               greet.common().title(),
               List.of(line(greet, npc.ask())),
               buttons,
               button(Component.translatable("talk.deathbound.back"), custom("talk", id))
            );
         }
      }
   }

   public static void back(ServerPlayer p, String id) {
      Talk.Npc npc = TalkData.NPCS.get(id);
      if (npc != null && topics(p, npc).size() > 2) {
         ask(p, id);
      } else {
         open(p, id);
      }
   }

   public static void topic(ServerPlayer p, String payload) {
      String key = payload.substring(payload.indexOf(47) + 1);
      hear(p, key);
      page(p, key).ifPresent(p::openDialog);
   }

   private static DialogBody line(MultiActionDialog like, String text) {
      Style style = like.common()
         .body()
         .stream()
         .filter(b -> b instanceof PlainMessage)
         .map(b -> ((PlainMessage)b).contents().getStyle())
         .findFirst()
         .orElse(Style.EMPTY);
      boolean aside = text.startsWith("*");
      String words = aside ? text.replace("*", "") : text;
      return new PlainMessage(Component.literal(words).withStyle(style.withItalic(aside)), 310);
   }

   private static Component mark(Component label, boolean fresh) {
      return fresh ? Component.literal("◆ ").withColor(12160255).append(label.copy().withColor(16777215)) : label;
   }

   private static ActionButton topicButton(String npc, Talk.Topic t, Set<String> heard) {
      boolean fresh = !heard.contains(t.key());
      Component label = fresh ? mark(Component.literal(t.label()), true) : Component.literal(t.label()).withColor(10985397);
      return button(label, custom("topic", npc + "/" + t.key()));
   }

   private static ClickEvent custom(String what, String payload) {
      return new Custom(DeathBound.id(what), Optional.of(StringTag.valueOf(payload)));
   }

   private static ClickEvent target(ServerPlayer p, String t) {
      if (t.startsWith("dialog:")) {
         return page(p, t.substring(7)).map(h -> new ShowDialog(h)).orElse(null);
      }

      String body = t.substring(t.indexOf(58) + 1);
      int slash = body.indexOf(47);
      return slash < 0
         ? new Custom(DeathBound.id(body), Optional.empty())
         : new Custom(DeathBound.id(body.substring(0, slash)), Optional.of(StringTag.valueOf(body.substring(slash + 1))));
   }

   private static ActionButton button(Component label, ClickEvent click) {
      return new ActionButton(new CommonButtonData(label, Optional.empty(), 250), Optional.ofNullable(click).map(StaticAction::new));
   }

   private static void show(ServerPlayer p, String key, Component title, List<DialogBody> body, List<ActionButton> buttons, ActionButton exit) {
      CommonDialogData common = new CommonDialogData(
         title, Optional.of(Component.literal("deathbound:" + key)), true, false, DialogAction.CLOSE, body, List.of()
      );
      p.openDialog(Holder.direct(new MultiActionDialog(common, buttons, Optional.of(exit), 1)));
   }

   private Talk() {
   }

   public record Npc(String id, List<String[]> greet, List<Talk.Topic> topics, List<Talk.Service> services, List<String> backs, String ask) {
   }

   public record Service(String label, String target, String when) {
   }

   public record Topic(String key, String label, String when) {
   }
}
