package dev.sevennv.client.world;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.BlockUtils;
import dev.sevennv.client.utils.BreakUtils;
import dev.sevennv.client.utils.Rotation;
import dev.sevennv.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Automatically mines nearby ores when they are in range. The scan is limited
 * to loaded blocks and re-runs on a timer. Each block is broken through the full
 * vanilla break cycle, so the server sees ordinary mining and its own reach and
 * break-time rules still apply.
 */
public final class AutoMine extends Module {
    public AutoMine() {
        super("Auto Mine", "Mines nearby ores automatically", Category.WORLD);
        range = number("Range", "Search radius in blocks", 5.0, 1.0, 16.0, 0.5);
        includeStone = bool("Include stone", "Also mine plain stone and deepslate", false);
        delay = integer("Delay", "Milliseconds between blocks", 150, 50, 2000);
        rotate = bool("Rotate", "Face the target block", true);
    }

    private final DoubleSetting range;
    private final BooleanSetting includeStone;
    private final IntSetting delay;
    private final BooleanSetting rotate;
    private final Timer timer = new Timer();

    @Override
    public void onDisable() {
        BreakUtils.cancel();
    }

    @Override
    public void onWorldChange() {
        BreakUtils.cancel();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }
        if (BreakUtils.breaking()) {
            BreakUtils.tick();
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        BlockPos target = findTarget();
        if (target == null) {
            return;
        }
        if (rotate.get()) {
            Rotation.face(Vec3.atCenterOf(target), true, 180f);
        }
        BreakUtils.mine(target);
    }

    private BlockPos findTarget() {
        int radius = (int) Math.ceil(range.get());
        BlockPos centre = mc.player.blockPosition();
        double bestDistance = Double.MAX_VALUE;
        BlockPos best = null;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = centre.offset(dx, dy, dz);
                    if (!mc.level.isLoaded(pos)) {
                        continue;
                    }
                    var state = mc.level.getBlockState(pos);
                    boolean wanted = BlockUtils.isOre(state)
                            || (includeStone.get() && (state.is(net.minecraft.world.level.block.Blocks.STONE)
                            || state.is(net.minecraft.world.level.block.Blocks.DEEPSLATE)));
                    if (!wanted) {
                        continue;
                    }
                    double distance = mc.player.distanceToSqr(Vec3.atCenterOf(pos));
                    if (distance < bestDistance && distance <= range.get() * range.get()) {
                        bestDistance = distance;
                        best = pos;
                    }
                }
            }
        }
        return best;
    }
}
