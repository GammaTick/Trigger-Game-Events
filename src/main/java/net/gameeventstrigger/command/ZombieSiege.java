package net.gameeventstrigger.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;

public class ZombieSiege {
    private static int startX;
    private static int startZ;
    private static int remaining;

    public static int spawn(CommandContext<ServerCommandSource> context, BlockPos pos, boolean forceSpawn) {
        ServerWorld world = context.getSource().getWorld().toServerWorld();

        if (pos == null) {
            if (!spawn(context, world)) {
                return 0;
            }
        } else {
            pos = new BlockPos(pos.getX(), world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos.getX() ,pos.getZ()), pos.getZ());
            remaining = 20;
            startX = pos.getX();
            startZ = pos.getZ();
        }

        context.getSource().sendFeedback(() -> Text.of("Well"), false);

        while (remaining > 0) {
            trySpawnZombie(context, world);
            --remaining;
        }

        return 1;
    }

    private static boolean spawn(CommandContext<ServerCommandSource> context, World world) {
        BlockPos blockPos = context.getSource().getEntity().getBlockPos();
        for (int i = 0; i < 10; ++i) {
            float f = world.random.nextFloat() * 6.2831855F;
            startX = blockPos.getX() + MathHelper.floor(MathHelper.cos(f) * 32.0F);
            startZ = blockPos.getZ() + MathHelper.floor(MathHelper.sin(f) * 32.0F);

            if (getSpawnVector(context.getSource().getWorld().toServerWorld(), new BlockPos(startX, world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, startX ,startZ), startZ)) != null) {
                remaining = 20;
                return true;
            }
        }

        return false;
    }

    private static void trySpawnZombie(CommandContext<ServerCommandSource> context, ServerWorld world) {
        Vec3d vec3d = getSpawnVector(world, new BlockPos(startX, world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, startX ,startZ), startZ));
        if (vec3d != null) {
            ZombieEntity zombieEntity;
            try {
                zombieEntity = new ZombieEntity(world);
                zombieEntity.initialize(world, world.getLocalDifficulty(zombieEntity.getBlockPos()), SpawnReason.EVENT, null, null);
            } catch (Exception var5) {
                context.getSource().sendFeedback(() -> Text.translatable("Failed to create zombie for village siege at %s %s", vec3d, var5), true);
                return;
            }

            zombieEntity.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, world.random.nextFloat() * 360.0F, 0.0F);
            world.spawnEntityAndPassengers(zombieEntity);
        }
    }

    private static Vec3d getSpawnVector(ServerWorld world, BlockPos pos) {
        for (int i = 0; i < 10; ++i) {

            int j = pos.getX() + world.random.nextInt(16) - 8;
            int k = pos.getZ() + world.random.nextInt(16) - 8;
            int l = world.getTopY(Heightmap.Type.WORLD_SURFACE, j, k);
            BlockPos blockPos = new BlockPos(j, l, k);

            BlockState blockState = world.getBlockState(pos);
            if (SpawnHelper.isClearForSpawn(world, pos, blockState, blockState.getFluidState(), EntityType.ZOMBIE)) {
                return Vec3d.ofBottomCenter(blockPos);

            }
        }

        return null;
    }
}
