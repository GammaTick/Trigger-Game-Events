package net.triggergameevents.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.passive.TraderLlamaEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.WorldView;
import net.minecraft.world.level.ServerWorldProperties;

import java.util.Iterator;

public class WanderingTrader {
    public static int spawn(CommandContext<ServerCommandSource> context, BlockPos spawnPos, BlockPos targetPos, boolean forceSpawn) {
        ServerWorld world = context.getSource().getWorld();
        Random random = world.random;


        if (spawnPos == null) {
            spawnPos = getNearbySpawnPos(world, targetPos, 48, random);
        } else {
            spawnPos = new BlockPos(spawnPos.getX(), world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, spawnPos.getX() , spawnPos.getZ()), spawnPos.getZ());
        }

        if (spawnPos == null) {
            context.getSource().sendError(Text.of("Unable to summon a wandering trader because the spawn position is invalid"));
            return 0;
        }

        if (!doesNotSuffocateAt(world, spawnPos)) {
            context.getSource().sendError(Text.of("Unable to summon a wandering trader because he may suffocate"));
            return 0;
        }

        if (targetPos == null) {
            targetPos = context.getSource().getEntity().getBlockPos();
        }
        targetPos = new BlockPos(targetPos.getX(), world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, targetPos.getX() , targetPos.getZ()), targetPos.getZ());

        int x = spawnPos.getX();
        int z = spawnPos.getZ();

        WanderingTraderEntity wanderingTraderEntity = EntityType.WANDERING_TRADER.spawn(world, new BlockPos(x, world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x , z), z), SpawnReason.EVENT);

        if (wanderingTraderEntity != null) {
            for (int j = 0; j < 2; ++j) {
                spawnLlama(world, wanderingTraderEntity, 4);
            }
            ServerWorldProperties properties = context.getSource().getServer().getSaveProperties().getMainWorldProperties();
            properties.setWanderingTraderId(wanderingTraderEntity.getUuid());
            wanderingTraderEntity.setDespawnDelay(48000);
            wanderingTraderEntity.setWanderTarget(targetPos);
            wanderingTraderEntity.setPositionTarget(targetPos, spawnPos.getManhattanDistance(targetPos));


            final BlockPos spawnSpawnPos = spawnPos;
            MutableText coordinates = Texts.bracketed(Text.translatable("%s, %s, %s", spawnPos.getX(), spawnPos.getY(), spawnPos.getZ())).styled(style -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + spawnSpawnPos.getX() + " " + spawnSpawnPos.getY() + " " + spawnSpawnPos.getZ())));
            context.getSource().sendFeedback(() -> Text.translatable("Successfully spawned a wandering trader at %s", coordinates), true);
            return 1;
        }

        context.getSource().sendError(Text.of("Unable to spawn a wandering trader"));
        return 0;
    }

    private static void spawnLlama(ServerWorld world, WanderingTraderEntity wanderingTrader, int range) {
        BlockPos blockPos = getNearbySpawnPos(world, wanderingTrader.getBlockPos(), range, world.random);
        if (blockPos != null) {
            TraderLlamaEntity traderLlamaEntity = EntityType.TRADER_LLAMA.spawn(world, blockPos, SpawnReason.EVENT);
            if (traderLlamaEntity != null) {
                traderLlamaEntity.attachLeash(wanderingTrader, true);
            }
        }
    }

    private static BlockPos getNearbySpawnPos(WorldView world, BlockPos pos, int range, Random random) {
        BlockPos blockPos = null;

        for(int i = 0; i < 10; ++i) {
            int x = pos.getX() + random.nextInt(range * 2) - range;
            int z = pos.getZ() + random.nextInt(range * 2) - range;
            int l = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
            BlockPos blockPos2 = new BlockPos(x, l, z);
            if (SpawnHelper.canSpawn(SpawnRestriction.Location.ON_GROUND, world, blockPos2, EntityType.WANDERING_TRADER)) {
                blockPos = blockPos2;
                break;
            }
        }

        return blockPos;
    }

    private static boolean doesNotSuffocateAt(BlockView world, BlockPos pos) {
        Iterator var3 = BlockPos.iterate(pos, pos.add(1, 2, 1)).iterator();

        BlockPos blockPos;
        do {
            if (!var3.hasNext()) {
                return true;
            }

            blockPos = (BlockPos)var3.next();
        } while(world.getBlockState(blockPos).getCollisionShape(world, blockPos).isEmpty());

        return false;
    }
}
