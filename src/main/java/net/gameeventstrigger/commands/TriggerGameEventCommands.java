package net.gameeventstrigger.commands;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TriggerGameEventCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("gameevent").requires(source -> source.hasPermissionLevel(2))
                .then(literal("spawnpillagerpatrol")
                        .executes(context -> spawnPillagerPatrol(context, null, null, false))
                        .then(literal("at")
                                .then(argument("entity", EntityArgumentType.entity())
                                        .executes(context -> spawnPillagerPatrol(context, EntityArgumentType.getEntity(context, "entity").getBlockPos(), null, false))
                                        .then(argument("targetposition", BlockPosArgumentType.blockPos())
                                                .executes(context -> spawnPillagerPatrol(context, BlockPosArgumentType.getBlockPos(context, "blockposition"), BlockPosArgumentType.getBlockPos(context, "targetposition"), false))))

                                .then(argument("blockposition", BlockPosArgumentType.blockPos())
                                        .executes(context -> spawnPillagerPatrol(context, BlockPosArgumentType.getBlockPos(context, "blockposition"), null, false))
                                        .then(argument("targetposition", BlockPosArgumentType.blockPos())
                                                .executes(context -> spawnPillagerPatrol(context, BlockPosArgumentType.getBlockPos(context, "blockposition"), BlockPosArgumentType.getBlockPos(context, "targetposition"), false))))

                .then(literal("spawnwanderingtrader")
                        .executes(context -> spawnWanderingTrader(context, null, null, false))
                        .then(literal("at")
                                .then(argument("entity", EntityArgumentType.entity())
                                        .executes(context -> spawnWanderingTrader(context, EntityArgumentType.getEntity(context, "entity").getBlockPos(), null, false)))

                                .then(argument("blockposition", BlockPosArgumentType.blockPos())
                                        .executes(context -> spawnWanderingTrader(context, BlockPosArgumentType.getBlockPos(context, "blockposition"), null, false)))))

                .then(literal("spawnzombiesiege")
                        .executes(context -> spawnZombieSiege(context, null, false)))
        ))));
    }

    public static int spawnPillagerPatrol(CommandContext<ServerCommandSource> context, BlockPos spawnPos, BlockPos targetPos, boolean forceSpawn) {
        return PillagerPatrol.spawn(context, spawnPos, targetPos, forceSpawn);
    }

    public static int spawnWanderingTrader(CommandContext<ServerCommandSource> context, BlockPos spawnPos, BlockPos targetPos, boolean forceSpawn) {
        return WanderingTrader.spawn(context, spawnPos, targetPos, forceSpawn);
    }

    public static int spawnZombieSiege(CommandContext<ServerCommandSource> context, BlockPos spawnPos, boolean forceSpawn) {
        return ZombieSiege.spawn(context, spawnPos, forceSpawn);
    }
}
