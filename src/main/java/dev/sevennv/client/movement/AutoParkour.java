package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Keeps you moving across flat ground with gaps by jumping just before the
 * edge, and by sprint-jumping when a gap is wide enough to need it.
 *
 * <p>It only presses the ordinary jump key; no movement is forced and no
 * position is altered. When it is unsure (an obstacle, a wall, or a gap it
 * cannot clear) it does nothing, so it never walks you into lava on purpose.
 */
public final class AutoParkour extends Module {
    public AutoParkour() {
        super("Auto Parkour", "Jumps gaps while you run so you never stop", Category.MOVEMENT);
        lookAhead = integer("Look ahead", "Blocks ahead to scan for a gap", 2, 1, 4);
        maxGap = integer("Max gap", "Widest gap to attempt, in blocks", 3, 1, 5);
        sprintJump = bool("Sprint jump", "Sprint-jump for the wider gaps", true);
        onlyWhileRunning = bool("Only while running", "Only act while moving forward", true);
        avoidLava = bool("Avoid lava", "Do not jump toward a gap that lands in lava", true);
        delay = integer("Delay", "Minimum milliseconds between jumps", 120, 0, 500);
    }

    private final IntSetting lookAhead;
    private final IntSetting maxGap;
    private final BooleanSetting sprintJump;
    private final BooleanSetting onlyWhileRunning;
    private final BooleanSetting avoidLava;
    private final IntSetting delay;

    private final Timer timer = new Timer();
    private int heldTicks;

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.options == null) {
            return;
        }
        // Release a jump pressed on a previous tick so each gap is one jump.
        if (heldTicks > 0 && --heldTicks == 0) {
            mc.options.keyJump.setDown(false);
            return;
        }
        if (!mc.player.onGround() || mc.player.isInWater() || mc.player.onClimbable()) {
            return;
        }
        if (onlyWhileRunning.get() && mc.player.zza <= 0.0f) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        int gap = gapAhead();
        if (gap <= 0 || gap > maxGap.get()) {
            return;
        }
        if (sprintJump.get() && gap >= 2) {
            mc.player.setSprinting(true);
        }
        mc.options.keyJump.setDown(true);
        heldTicks = 2;
    }

    /**
     * Width of the gap directly ahead (0 when the ground is continuous), or -1
     * when the far side cannot be reached or the jump would be unsafe.
     */
    private int gapAhead() {
        Direction facing = mc.player.getDirection();
        BlockPos feet = mc.player.blockPosition();
        // Find the first block ahead with no ground under it.
        int edge = -1;
        for (int step = 1; step <= lookAhead.get() + maxGap.get(); step++) {
            if (!isSolid(feet.relative(facing, step).below())) {
                edge = step;
                break;
            }
        }
        if (edge < 0 || edge > lookAhead.get()) {
            return 0;
        }
        // Measure the gap width, looking for the landing.
        for (int step = edge; step <= edge + maxGap.get(); step++) {
            BlockPos ahead = feet.relative(facing, step);
            if (isSolid(ahead.below())) {
                if (avoidLava.get() && isLava(ahead.below())) {
                    return -1;
                }
                return step - edge + 1;
            }
            if (isLava(ahead.below()) || isLava(ahead)) {
                return -1;
            }
        }
        return -1;
    }

    private boolean isSolid(BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        return state.isSolidRender();
    }

    private boolean isLava(BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        return state.is(net.minecraft.world.level.block.Blocks.LAVA)
                || state.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK);
    }

    @Override
    public void onDisable() {
        release();
    }

    @Override
    public void onWorldChange() {
        release();
    }

    private void release() {
        heldTicks = 0;
        if (mc.options != null) {
            mc.options.keyJump.setDown(false);
        }
    }
}
