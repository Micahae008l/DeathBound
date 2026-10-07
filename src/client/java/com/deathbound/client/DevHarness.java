package com.deathbound.client;

import com.deathbound.DeathBound;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

final class DevHarness {
   private static final Deque<String> LINES = new ArrayDeque<>();
   private static int wait;
   private static int holdUse;
   private static final Map<KeyMapping, Integer> held = new HashMap<>();
   private static String aim;
   private static boolean noDark;
   private static boolean started;

   static void init() {
      String script = System.getProperty("deathbound.script");
      if (script != null) {
         try {
            for (String line : Files.readAllLines(Path.of(script))) {
               if (!line.isBlank() && !line.startsWith("#")) {
                  LINES.add(line.trim());
               }
            }
         } catch (IOException e) {
            DeathBound.LOG.error("dev script", e);
            return;
         }

         ClientTickEvents.END_CLIENT_TICK.register(DevHarness::tick);
      }
   }

   private static void tick(Minecraft mc) {
      if (mc.player != null && mc.getSingleplayerServer() != null) {
         if (!started) {
            started = true;
            wait = 60;
         }

         if (mc.gui.screen() instanceof DeathScreen) {
            mc.player.respawn();
            mc.gui.setScreen(null);
         }

         if (holdUse > 0) {
            mc.options.keyUse.setDown(--holdUse > 0);
         }

         held.replaceAll((key, left) -> left - 1);
         held.entrySet().removeIf(e -> {
            if (e.getValue() <= 0) {
               e.getKey().setDown(false);
               return true;
            } else {
               e.getKey().setDown(true);
               return false;
            }
         });
         Cine.Pose film = Cine.pose(1.0F);
         if (film != null && mc.options.getCameraType().isFirstPerson()) {
            mc.player.setPos(film.pos().subtract(0.0, 1.62, 0.0));
            mc.player.setDeltaMovement(Vec3.ZERO);
         }

         if (noDark) {
            mc.player.removeEffectNoUpdate(MobEffects.DARKNESS);
         }

         if (aim != null) {
            Entity target = Cine.nearest(aim);
            if (target != null) {
               Vec3 d = target.getBoundingBox().getCenter().subtract(mc.player.getEyePosition());
               mc.player.setYRot((float)(Math.atan2(-d.x, d.z) * 180.0 / 3.141592653589793));
               mc.player.setXRot((float)(-Math.atan2(d.y, d.horizontalDistance()) * 180.0 / 3.141592653589793));
            }
         }

         if (wait > 0) {
            wait--;
         } else {
            while (!LINES.isEmpty() && wait == 0) {
               String line = LINES.poll();
               DeathBound.LOG.info("[harness] {}", line);
               String[] p = line.split(" ", 2);
               String arg = p.length > 1 ? p[1] : "";
               switch (p[0]) {
                  case "wait":
                     wait = Integer.parseInt(arg);
                     break;
                  case "shot":
                     Screenshot.grab(mc.gameDirectory, arg + ".png", mc.gameRenderer.mainRenderTarget(), 1, msg -> {});
                     wait = 2;
                     break;
                  case "hud":
                     if (mc.gui.hud.isHidden() != arg.equals("off")) {
                        mc.gui.hud.toggle();
                     }
                     break;
                  case "view":
                     Options var24 = mc.options;

                     var24.setCameraType(switch (arg) {
                        case "back" -> CameraType.THIRD_PERSON_BACK;
                        case "front" -> CameraType.THIRD_PERSON_FRONT;
                        default -> CameraType.FIRST_PERSON;
                     });
                     break;
                  case "look":
                     String[] a = arg.split(" ");
                     mc.player.setYRot(Float.parseFloat(a[0]));
                     mc.player.setXRot(Float.parseFloat(a[1]));
                     mc.player.yRotO = mc.player.getYRot();
                     mc.player.xRotO = mc.player.getXRot();
                     break;
                  case "use":
                     holdUse = Integer.parseInt(arg);
                     mc.options.keyUse.setDown(true);
                     break;
                  case "attack":
                     mc.options.keyAttack.setDown(arg.equals("down"));
                     break;
                  case "swing":
                     mc.player.swing(InteractionHand.MAIN_HAND, mc.player.getMainHandItem().getAttackAnimation(), false);
                     mc.player.connection.send(ServerboundPunchPacket.INSTANCE);
                     break;
                  case "music":
                     DeathBound.LOG
                        .info(
                           "[harness] music: situational={} playing={} bossbar={}",
                           new Object[]{
                              mc.getSituationalMusic() == null ? null : mc.getSituationalMusic().sound().value().location(),
                              mc.getMusicManager().getCurrentMusicTranslationKey(),
                              mc.gui.hud.getBossOverlay().shouldPlayMusic()
                           }
                        );
                     break;
                  case "rec":
                     try {
                        String[] ax = arg.split(" ");
                        Cine.record(mc, ax[0], ax.length > 1 ? Integer.parseInt(ax[1]) : 30);
                     } catch (IOException e) {
                        DeathBound.LOG.error("[cine] record", e);
                     }
                     break;
                  case "stop":
                     Cine.stop();
                     break;
                  case "cam":
                     Cine.cam(arg);
                     break;
                  case "pan":
                     Cine.pan(arg);
                     break;
                  case "orbit":
                     Cine.orbit(arg);
                     break;
                  case "follow":
                     Cine.follow(arg);
                     break;
                  case "camoff":
                     Cine.off();
                     break;
                  case "fov":
                     mc.options.fov().set(Integer.parseInt(arg));
                     mc.options.fovEffectScale().set(0.0);
                     break;
                  case "gui":
                     mc.options.guiScale().set(Integer.parseInt(arg));
                     break;
                  case "close":
                     mc.gui.setScreen(null);
                     break;
                  case "inv":
                     mc.gui.setScreen(new InventoryScreen(mc.player));
                     break;
                  case "nodark":
                     noDark = arg.equals("on");
                     break;
                  case "next":
                     if (mc.gui.screen() instanceof NpcDialogScreen npc) {
                        npc.harnessNext();
                     }
                     break;
                  case "pick":
                     if (mc.gui.screen() instanceof NpcDialogScreen npc) {
                        npc.harnessPick(Integer.parseInt(arg));
                     }
                     break;
                  case "click":
                     KeyMapping.click(mc.options.keyAttack.getDefaultKey());
                     break;
                  case "aim":
                     aim = arg.equals("off") ? null : (arg.contains(":") ? arg : "minecraft:" + arg);
                     break;
                  case "hold":
                     String[] parts = arg.split(" ");

                     KeyMapping key = switch (parts[0]) {
                        case "back" -> mc.options.keyDown;
                        case "left" -> mc.options.keyLeft;
                        case "right" -> mc.options.keyRight;
                        case "jump" -> mc.options.keyJump;
                        case "sneak" -> mc.options.keyShift;
                        case "sprint" -> mc.options.keySprint;
                        default -> mc.options.keyUp;
                     };
                     key.setDown(true);
                     held.put(key, Integer.parseInt(parts[1]));
                     break;
                  case "shoulder":
                     Cine.shoulder(arg);
                     break;
                  case "handheld":
                     Cine.handheld = Float.parseFloat(arg);
                     break;
                  case "filmlight":
                     Cine.clearAir = arg.equals("on");
                     break;
                  case "quit":
                     Cine.stop();
                     mc.stop();
                     break;
                  default:
                     MinecraftServer server = mc.getSingleplayerServer();
                     String cmd = line;
                     server.execute(() -> {
                        ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                        CommandSourceStack source = server.createCommandSourceStack().withPermission(LevelBasedPermissionSet.OWNER);
                        if (sp != null) {
                           source = source.withEntity(sp).withPosition(sp.position()).withLevel(sp.level()).withRotation(sp.getRotationVector());
                        }

                        server.getCommands().performPrefixedCommand(source, cmd);
                     });
               }
            }
         }
      }
   }

   private DevHarness() {
   }
}
