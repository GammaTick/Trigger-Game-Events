package net.triggergameevents.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PatrolEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;

public class PillagerPatrol {
    public static int spawn(CommandContext<ServerCommandSource> context, BlockPos spawnPos, BlockPos targetPos, boolean forceSpawn) {
        ServerWorld world = context.getSource().getWorld().toServerWorld();

        if (world.getDifficulty().equals(Difficulty.PEACEFUL)) {
            return 0;
        }

        Random random = world.random;

        if (spawnPos == null) {
            int x = (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1) + context.getSource().getEntity().getBlockPos().getX();
            int z = (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1) + context.getSource().getEntity().getBlockPos().getZ();
            spawnPos = new BlockPos(x, world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x , z), z);
        } else {
            spawnPos = new BlockPos(spawnPos.getX(), world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, spawnPos.getX() , spawnPos.getZ()), spawnPos.getZ());
        }

        int spawnedPillagers = 0;
        int expectedPillagers = (int)Math.ceil(world.getLocalDifficulty(spawnPos).getLocalDifficulty()) + 1;

        for (int i = 0; i < expectedPillagers; ++i) {
            if (i == 0) {
                if (!spawnPillager(world, spawnPos, targetPos, random, true)) {
                    context.getSource().sendError(Text.of("Unable to spawn a pillager patrol because the spawn position is invalid"));
                    return 0;
                }
                ++spawnedPillagers;
            } else {
                if (spawnPillager(world, spawnPos, targetPos, random, false)) {
                    ++spawnedPillagers;
                }
            }

            int x = spawnPos.getX() + random.nextInt(5) - random.nextInt(5);
            int z = spawnPos.getZ() + random.nextInt(5) - random.nextInt(5);

            spawnPos = new BlockPos(x, world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x , z), z);
        }

        if (spawnedPillagers > 0) {
            final BlockPos finalSpawnPos = spawnPos;
            final int finalSpawnedPillagers = spawnedPillagers;

            MutableText coordinates = Texts.bracketed(Text.translatable("%s, %s, %s", finalSpawnPos.getX(), finalSpawnPos.getY(), finalSpawnPos.getZ())).styled(style -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + finalSpawnPos.getX() + " " + finalSpawnPos.getY() + " " + finalSpawnPos.getZ())));
            context.getSource().sendFeedback(() -> Text.of(Text.translatable("Successfully spawned a pillager patrol at %s with %s pillager(s)", coordinates, finalSpawnedPillagers)), true);
        } else {
            context.getSource().sendError(Text.of("Unable to spawn a pillager patrol"));
        }

        return spawnedPillagers;
    }

    private static boolean spawnPillager(ServerWorld world, BlockPos spawnPos, BlockPos targetPos, Random random, boolean captain) {
        BlockState blockState = world.getBlockState(spawnPos);
        if (!SpawnHelper.isClearForSpawn(world, spawnPos, blockState, blockState.getFluidState(), EntityType.PILLAGER)) {
            return false;
        } else if (!PatrolEntity.canSpawn(EntityType.PILLAGER, world, SpawnReason.PATROL, spawnPos, random)) {
            return false;
        } else {
            PatrolEntity patrolEntity = EntityType.PILLAGER.create(world);
            if (patrolEntity != null) {
                if (captain) {
                    patrolEntity.setPatrolLeader(true);
                    patrolEntity.setRandomPatrolTarget();
                }
                patrolEntity.setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                patrolEntity.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.PATROL, null, null);
                world.spawnEntityAndPassengers(patrolEntity);
                if (targetPos != null) {
                    patrolEntity.setPatrolTarget(targetPos);
                }
                return true;
            } else {
                return false;
            }
        }
    }
}
