package dev.sevennv.client.world;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.config.StringListSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.BreakUtils;
import dev.sevennv.client.utils.Rotation;
import dev.sevennv.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Locale;

/**
 * Mines the blocks around you. Each block is broken through the full vanilla
 * break cycle (start, continue each tick, stop) so the server sees ordinary
 * block-breaking and its own reach and break-time rules still apply. Only one
 * block is mined at a time: switching targets mid-break would abort the break
 * and never finish anything.
 *
 * <p>Only blocks that a bare hand or your held tool can actually break are
 * targeted, and the radius is capped small so this stays a convenience for
 * clearing around you rather than a world-edit. A block list filters what is
 * mined; leaving it empty mines everything in range.
 */
public final class Nuker extends Module {
    public Nuker() {
        super("Nuker", "Mines the blocks around you", Category.WORLD);
        range = number("Range", "Radius in blocks", 3.0, 1.0, 6.0, 0.5);
        delay = integer("Delay", "Milliseconds between blocks", 120, 0, 1000);
        rotate = bool("Rotate", "Face each block before mining", true);
        include = list("Include", "Only mine block ids containing one of these (empty = all)",
                "stone", "deepslate", "dirt", "grass", "sand", "gravel", "netherrack", "obsidian");
        onlyInstant = bool("Only fast blocks", "Skip blocks that take more than a moment", false);
    }

    private final DoubleSetting range;
    private final IntSetting delay;
    private final BooleanSetting rotate;
    private final StringListSetting include;
    private final BooleanSetting onlyInstant;

    private final Timer timer = new Timer();
    private BlockPos current;

    @Override
    public void onDisable() {
        current = null;
        BreakUtils.cancel();
    }

    @Override
    public void onWorldChange() {
        current = null;
        BreakUtils.cancel();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }
        // Drive the break in progress every tick; only pick a new target once the
        // previous one has finished.
        if (BreakUtils.breaking()) {
            BreakUtils.tick();
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        BlockPos target = findClosestTarget();
        if (target == null) {
            return;
        }
        if (rotate.get()) {
            Rotation.face(Vec3.atCenterOf(target), true, 180f);
        }
        current = target;
        BreakUtils.mine(target);
    }

    private BlockPos findClosestTarget() {
        BlockPos centre = mc.player.blockPosition();
        int radius = (int) Math.ceil(range.get());
        double rangeSq = range.get() * range.get();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = centre.offset(dx, dy, dz);
                    if (!mc.level.isLoaded(pos)) {
                        continue;
                    }
                    double distance = mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos));
                    if (distance > rangeSq) {
                        continue;
                    }
                    BlockState state = mc.level.getBlockState(pos);
                    if (state.isAir() || !wanted(state)) {
                        continue;
                    }
                    if (onlyInstant.get() && state.getDestroySpeed(mc.level, pos) > 0.4f) {
                        continue;
                    }
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = pos;
                    }
                }
            }
        }
        return best;
    }

    private boolean wanted(BlockState state) {
        List<String> terms = include.get();
        if (terms.isEmpty()) {
            return true;
        }
        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().toLowerCase(Locale.ROOT);
        for (String term : terms) {
            if (!term.isBlank() && id.contains(term.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getArraylistSuffix() {
        return current == null ? "" : current.toShortString();
    }
}
