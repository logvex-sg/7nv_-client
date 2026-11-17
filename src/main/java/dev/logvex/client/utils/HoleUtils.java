package dev.logvex.client.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Observation helpers for hole detection and block scanning. Read-only: these
 * only inspect level state, they never place or break anything.
 */
public final class HoleUtils {
    private HoleUtils() {
    }

    /** A one-block hole the player can stand in, with the given bed level. */
    public record Hole(BlockPos pos, boolean doubleHole) {
    }

    /**
     * Finds the safe hole the given position sits in, or null when the position
     * is not inside a hole. A hole is a 1x1 (or 1x2) air column with solid
     * blocks on all four sides and a floor beneath.
     */
    public static Hole findHole(Level level, BlockPos feet) {
        if (level == null || !isHoleColumn(level, feet)) {
            return null;
        }
        return new Hole(feet, false);
    }

    public static boolean isHoleColumn(Level level, BlockPos feet) {
        if (level == null) {
            return false;
        }
        if (!isAir(level, feet) || !isAir(level, feet.above())) {
            return false;
        }
        if (!isSolidFloor(level, feet.below())) {
            return false;
        }
        return isSolidWall(level, feet.north()) && isSolidWall(level, feet.south())
                && isSolidWall(level, feet.east()) && isSolidWall(level, feet.west());
    }

    /** True when both the given position and its neighbour form a 1x2 hole. */
    public static boolean isDoubleHole(Level level, BlockPos feet, BlockPos other) {
        if (level == null || other == null) {
            return false;
        }
        return isHoleColumn(level, feet) && isHoleColumn(level, other)
                && feet.distManhattan(other) == 1;
    }

    /** Scans a cube around {@code centre} for holes. Bounded radius keeps this cheap. */
    public static List<Hole> scan(Level level, BlockPos centre, int radius) {
        List<Hole> holes = new ArrayList<>();
        if (level == null) {
            return holes;
        }
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    BlockPos pos = centre.offset(dx, dy, dz);
                    if (isHoleColumn(level, pos)) {
                        holes.add(new Hole(pos, false));
                    }
                }
            }
        }
        return holes;
    }

    private static boolean isAir(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.getCollisionShape(level, pos).isEmpty();
    }

    private static boolean isSolidFloor(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.getCollisionShape(level, pos).isEmpty();
    }

    private static boolean isSolidWall(Level level, BlockPos pos) {
        return isSolidFloor(level, pos);
    }

    /** Centre of the top face of the block, for aiming. */
    public static Vec3 topCenter(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
    }

    public static AABB box(BlockPos pos) {
        return new AABB(pos);
    }

    public static double distanceTo(Entity entity, BlockPos pos) {
        return Math.sqrt(entity.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }
}
