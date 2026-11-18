package dev.sevennv.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Drives a complete block-break cycle through the ordinary player-action
 * packets, the same ones vanilla sends when you hold left-click.
 *
 * <p>A break on a server is three parts: a START packet, then one tick of break
 * progress per client tick, then a STOP packet the server turns into the actual
 * removal. The client's own {@code MultiPlayerGameMode} only keeps that cycle
 * alive for the block under the crosshair — every tick it aborts anything else —
 * so it cannot break an arbitrary block. This helper sends the same packets
 * directly, for the block the module chose, and lets the server apply its own
 * reach, tool and break-time rules. Nothing is bypassed: a block out of reach or
 * protected by the server is simply not broken.
 */
public final class BreakUtils {
    private BreakUtils() {
    }

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    private static BlockPos target;
    private static boolean breaking;
    private static boolean started;
    private static float progress;
    private static float lastReportedStage = -1f;

    /** True while a block is being mined. */
    public static boolean breaking() {
        return breaking;
    }

    /**
     * Starts mining {@code pos}. A request for a different block replaces the
     * current one; the caller does not need to keep calling this, {@link #tick()}
     * drives the cycle to completion.
     */
    public static void mine(BlockPos pos) {
        if (pos == null) {
            return;
        }
        if (breaking && target != null && !target.equals(pos)) {
            abort();
        }
        target = pos.immutable();
        breaking = true;
        started = false;
        progress = 0.0f;
        lastReportedStage = -1f;
    }

    /** Aborts the current break and tells the server it was aborted. */
    public static void cancel() {
        if (breaking) {
            abort();
        }
        reset();
    }

    /**
     * Advances the current break. Call once per client tick. Returns true while a
     * block is still being mined.
     */
    public static boolean tick() {
        Minecraft mc = mc();
        if (!breaking) {
            return false;
        }
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            reset();
            return false;
        }
        // If the player is mining by hand, yield to vanilla so the two do not
        // fight over the same break.
        if (mc.options != null && mc.options.keyAttack.isDown()) {
            cancel();
            return false;
        }
        BlockState state = mc.level.getBlockState(target);
        if (state.isAir()) {
            reset();
            return false;
        }
        if (mc.player.getEyePosition().distanceTo(Vec3.atCenterOf(target)) > 4.5) {
            cancel();
            return false;
        }
        Direction face = faceToward(target);
        if (!started) {
            mc.player.connection.send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, target, face));
            started = true;
        }
        progress += state.getDestroyProgress(mc.player, mc.level, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
        if (progress >= 1.0f) {
            mc.player.connection.send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, target, face));
            reset();
            return false;
        }
        reportStage(mc, face);
        return true;
    }

    /** Sends the progress overlay at most when it changes by a tenth. */
    private static void reportStage(Minecraft mc, Direction face) {
        float stage = Math.min(1.0f, progress);
        if (Math.abs(stage - lastReportedStage) < 0.1f) {
            return;
        }
        lastReportedStage = stage;
        // The server broadcasts its own progress; this only refreshes the local
        // crack overlay so the block looks like it is being mined.
        if (mc.level != null && mc.player != null) {
            mc.level.destroyBlockProgress(mc.player.getId(), target, (int) (stage * 10.0f) - 1);
        }
    }

    private static void abort() {
        Minecraft mc = mc();
        if (started && mc.player != null && mc.player.connection != null && target != null) {
            mc.player.connection.send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, target, faceToward(target)));
        }
    }

    private static void reset() {
        breaking = false;
        started = false;
        progress = 0.0f;
        lastReportedStage = -1f;
        if (target != null && mc().level != null && mc().player != null) {
            mc().level.destroyBlockProgress(mc().player.getId(), target, -1);
        }
        target = null;
    }

    /** The face of the block nearest the player, used as the break direction. */
    private static Direction faceToward(BlockPos pos) {
        Minecraft mc = mc();
        if (mc.player == null) {
            return Direction.UP;
        }
        Vec3 eye = mc.player.getEyePosition();
        double dx = eye.x - (pos.getX() + 0.5);
        double dy = eye.y - (pos.getY() + 0.5);
        double dz = eye.z - (pos.getZ() + 0.5);
        double ax = Math.abs(dx);
        double ay = Math.abs(dy);
        double az = Math.abs(dz);
        if (ax >= ay && ax >= az) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        }
        if (ay >= az) {
            return dy > 0 ? Direction.UP : Direction.DOWN;
        }
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }
}
