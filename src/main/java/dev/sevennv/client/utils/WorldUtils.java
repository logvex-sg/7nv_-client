package dev.sevennv.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Geometry and world-position helpers shared by many modules. */
public final class WorldUtils {
    private WorldUtils() {
    }

    public static BlockPos playerPos() {
        Entity player = Minecraft.getInstance().player;
        return player == null ? BlockPos.ZERO : player.blockPosition();
    }

    /** A {@link Vec3} at the centre of the block. */
    public static Vec3 center(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    /** Direction the player is facing, as one of the four cardinals. */
    public static Direction horizontalFacing(Entity entity) {
        return Direction.fromYRot(entity.getYRot());
    }

    /** Compass name for a yaw angle, e.g. "North" or "North-East". */
    public static String compass(float yaw) {
        String[] names = {"South", "South-West", "West", "North-West", "North", "North-East", "East", "South-East"};
        int index = Mth.floor(((yaw + 180f) * 8f / 360f) + 0.5f) & 7;
        return names[index];
    }

    public static String axisLabel(Entity entity) {
        Direction facing = horizontalFacing(entity);
        return switch (facing) {
            case NORTH -> "-Z";
            case SOUTH -> "+Z";
            case WEST -> "-X";
            case EAST -> "+X";
            default -> "+Y";
        };
    }

    /** Formats a number with a fixed one-decimal precision and no locale surprises. */
    public static String fmt1(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    public static String fmt2(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}
