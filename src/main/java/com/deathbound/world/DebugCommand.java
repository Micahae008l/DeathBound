package com.deathbound.world;

import com.deathbound.block.GraveBellBlock;
import com.deathbound.entity.DeathEntity;
import com.deathbound.entity.DeathsGuard;
import com.deathbound.entity.Gravebound;
import com.deathbound.entity.HollowHunter;
import com.deathbound.entity.SoulWisp;
import com.deathbound.npc.Collector;
import com.deathbound.npc.Quests;
import com.deathbound.npc.Soulforge;
import com.deathbound.npc.Temper;
import com.deathbound.registry.ModBlocks;
import com.deathbound.registry.ModNet;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult.TryEmptyHandInteraction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class DebugCommand {
   public static void init() {
      CommandRegistrationCallback.EVENT
         .register(
            (dispatcher, context, selection) -> dispatcher.register(
               (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                                              "deathbound"
                                                                           )
                                                                           .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
                                                                        .then(Commands.literal("descend").executes(c -> {
                                                                           UnderworldTravel.beginDescent(
                                                                              ((CommandSourceStack)c.getSource()).getPlayerOrException()
                                                                           );
                                                                           return 1;
                                                                        })))
                                                                     .then(Commands.literal("return").executes(c -> {
                                                                        UnderworldTravel.returnToLife(
                                                                           ((CommandSourceStack)c.getSource()).getPlayerOrException(), false
                                                                        );
                                                                        return 1;
                                                                     })))
                                                                  .then(
                                                                     Commands.literal("cinematic")
                                                                        .then(Commands.argument("kind", IntegerArgumentType.integer(0, 11)).executes(c -> {
                                                                           ModNet.cinematic(
                                                                              ((CommandSourceStack)c.getSource()).getPlayerOrException(),
                                                                              IntegerArgumentType.getInteger(c, "kind")
                                                                           );
                                                                           return 1;
                                                                        }))
                                                                  ))
                                                               .then(
                                                                  Commands.literal("phase")
                                                                     .then(Commands.argument("phase", IntegerArgumentType.integer(1, 3)).executes(c -> {
                                                                        DeathEntity d = nearest(c, DeathEntity.class);
                                                                        if (d != null) {
                                                                           d.debugPhase(IntegerArgumentType.getInteger(c, "phase"));
                                                                        }

                                                                        return d == null ? 0 : 1;
                                                                     }))
                                                               ))
                                                            .then(
                                                               Commands.literal("action")
                                                                  .then(Commands.argument("action", IntegerArgumentType.integer(0, 40)).executes(c -> {
                                                                     int a = IntegerArgumentType.getInteger(c, "action");
                                                                     DeathEntity d = nearest(c, DeathEntity.class);
                                                                     if (d != null) {
                                                                        d.debugAction(a);
                                                                        return 1;
                                                                     } else {
                                                                        DeathsGuard g = nearest(c, DeathsGuard.class);
                                                                        if (g != null) {
                                                                           g.debugAction(a);
                                                                           return 1;
                                                                        } else {
                                                                           HollowHunter h = nearest(c, HollowHunter.class);
                                                                           if (h != null) {
                                                                              h.debugAction(a);
                                                                              return 1;
                                                                           } else {
                                                                              return 0;
                                                                           }
                                                                        }
                                                                     }
                                                                  }))
                                                            ))
                                                         .then(Commands.literal("animate").executes(c -> {
                                                            int n = 0;

                                                            for (Mob m : around(c, Mob.class)) {
                                                               if (m instanceof Gravebound || m instanceof SoulWisp) {
                                                                  m.level().broadcastEntityEvent(m, (byte)4);
                                                                  n++;
                                                               }
                                                            }

                                                            return n;
                                                         })))
                                                      .then(
                                                         Commands.literal("story")
                                                            .then(
                                                               Commands.argument("progress", IntegerArgumentType.integer(0, 2))
                                                                  .then(
                                                                     Commands.argument("ending", IntegerArgumentType.integer(0, 3))
                                                                        .executes(
                                                                           c -> {
                                                                              Director.debugStory(
                                                                                 ((CommandSourceStack)c.getSource()).getLevel(),
                                                                                 IntegerArgumentType.getInteger(c, "progress"),
                                                                                 IntegerArgumentType.getInteger(c, "ending")
                                                                              );
                                                                              return 1;
                                                                           }
                                                                        )
                                                                  )
                                                            )
                                                      ))
                                                   .then(
                                                      Commands.literal("seals")
                                                         .then(Commands.argument("mask", IntegerArgumentType.integer(0, 7)).executes(c -> {
                                                            Director.debugSeals(
                                                               ((CommandSourceStack)c.getSource()).getLevel(), IntegerArgumentType.getInteger(c, "mask")
                                                            );
                                                            return 1;
                                                         }))
                                                   ))
                                                .then(Commands.literal("click").then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(c -> {
                                                   ServerPlayer p = ((CommandSourceStack)c.getSource()).getPlayerOrException();
                                                   BlockPos pos = BlockPosArgument.getLoadedBlockPos(c, "pos");
                                                   BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.SOUTH, pos, false);
                                                   BlockState state = p.level().getBlockState(pos);
                                                   InteractionResult used = state.useItemOn(p.getMainHandItem(), p.level(), p, InteractionHand.MAIN_HAND, hit);
                                                   if (used instanceof TryEmptyHandInteraction || p.getMainHandItem().isEmpty()) {
                                                      state.useWithoutItem(p.level(), p, hit);
                                                   }

                                                   return 1;
                                                }))))
                                             .then(Commands.literal("ringbell").executes(c -> {
                                                ServerPlayer p = ((CommandSourceStack)c.getSource()).getPlayerOrException();

                                                for (int y = Layout.VILLAGE.top() + 40; y > Layout.VILLAGE.top() - 20; y--) {
                                                   BlockPos pos = new BlockPos(Layout.VILLAGE.x() + 18, y, Layout.VILLAGE.z() + 14);
                                                   BlockState state = p.level().getBlockState(pos);
                                                   if (state.is(ModBlocks.GRAVE_BELL)) {
                                                      GraveBellBlock.ring(p.level(), pos);
                                                      return 1;
                                                   }
                                                }

                                                return 0;
                                             })))
                                          .then(Commands.literal("shade").executes(c -> {
                                             ServerPlayer p = ((CommandSourceStack)c.getSource()).getPlayerOrException();
                                             Dread.shade(p.level(), p, p.getRandom());
                                             return 1;
                                          })))
                                       .then(Commands.literal("quest").then(Commands.argument("who", StringArgumentType.word()).executes(c -> {
                                          Quests.talk(((CommandSourceStack)c.getSource()).getPlayerOrException(), StringArgumentType.getString(c, "who"));
                                          return 1;
                                       }))))
                                    .then(Commands.literal("accept").then(Commands.argument("id", StringArgumentType.word()).executes(c -> {
                                       Quests.accept(((CommandSourceStack)c.getSource()).getPlayerOrException(), StringArgumentType.getString(c, "id"));
                                       return 1;
                                    }))))
                                 .then(Commands.literal("temper").then(Commands.argument("rank", StringArgumentType.word()).executes(c -> {
                                    Temper.temper(((CommandSourceStack)c.getSource()).getPlayerOrException(), StringArgumentType.getString(c, "rank"));
                                    return 1;
                                 }))))
                              .then(Commands.literal("forge").then(Commands.argument("slot", StringArgumentType.word()).executes(c -> {
                                 Soulforge.forge(((CommandSourceStack)c.getSource()).getPlayerOrException(), StringArgumentType.getString(c, "slot"));
                                 return 1;
                              }))))
                           .then(
                              Commands.literal("audit")
                                 .then(
                                    Commands.argument("radius", IntegerArgumentType.integer(1, 16))
                                       .executes(
                                          c -> {
                                             ServerLevel level = ((CommandSourceStack)c.getSource()).getLevel();
                                             ChunkPos at = ChunkPos.containing(BlockPos.containing(((CommandSourceStack)c.getSource()).getPosition()));
                                             int r = IntegerArgumentType.getInteger(c, "radius");
                                             int chunks = 0;
                                             List<BlockPos> bad = new ArrayList<>();

                                             for (int dx = -r; dx <= r; dx++) {
                                                for (int dz = -r; dz <= r; dz++) {
                                                   LevelChunk ch = level.getChunkSource().getChunkNow(at.x() + dx, at.z() + dz);
                                                   if (ch != null) {
                                                      chunks++;
                                                      bad.addAll(Settle.problems(level, ch));
                                                   }
                                                }
                                             }

                                             int n = bad.size();
                                             int seen = chunks;
                                             String first = bad.stream()
                                                .limit(12L)
                                                .map(p -> p.toShortString() + " " + BuiltInRegistries.BLOCK.getKey(level.getBlockState(p).getBlock()).getPath())
                                                .collect(Collectors.joining("; "));
                                             ((CommandSourceStack)c.getSource())
                                                .sendSuccess(() -> Component.literal("audit: " + n + " in " + seen + " chunks. " + first), false);
                                             return n;
                                          }
                                       )
                                 )
                           ))
                        .then(Commands.literal("collect").executes(c -> {
                           Collector.collect(((CommandSourceStack)c.getSource()).getPlayerOrException());
                           return 1;
                        })))
                     .then(Commands.literal("ending").then(Commands.argument("which", StringArgumentType.word()).executes(c -> {
                        Director.chooseEnding(((CommandSourceStack)c.getSource()).getPlayerOrException(), StringArgumentType.getString(c, "which"));
                        return 1;
                     }))))
                  .then(Commands.literal("rise").executes(c -> {
                     around(c, Gravebound.class).forEach(Gravebound::rise);
                     return 1;
                  }))
            )
         );
   }

   private static <T extends Mob> List<T> around(CommandContext<CommandSourceStack> c, Class<T> type) {
      CommandSourceStack src = (CommandSourceStack)c.getSource();
      return src.getLevel().getEntitiesOfClass(type, new AABB(src.getPosition(), src.getPosition()).inflate(40.0));
   }

   private static <T extends Mob> T nearest(CommandContext<CommandSourceStack> c, Class<T> type) {
      T best = null;
      double bd = 1.7976931348623157E308;

      for (T m : around(c, type)) {
         double d = m.position().distanceToSqr(((CommandSourceStack)c.getSource()).getPosition());
         if (d < bd) {
            bd = d;
            best = m;
         }
      }

      if (best == null) {
         ((CommandSourceStack)c.getSource()).sendFailure(Component.literal("No " + type.getSimpleName() + " nearby"));
      }

      return best;
   }

   private DebugCommand() {
   }
}
