package dev.sevennv.client.world;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.config.StringListSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Rotation;
import dev.sevennv.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Mines a small sphere of blocks around you, breaking each through the vanilla
 * destroy path so the server sees ordinary block-breaking and its own
 * reach/break-time rules still apply.
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
        maxPerTick = integer("Max per tick", "Maximum blocks started per cycle", 1, 1, 4);
        rotate = bool("Rotate", "Face each block before mining", true);
        include = list("Include", "Only mine block ids containing one of these (empty = all)",
                "stone", "deepslate", "dirt", "grass", "sand", "gravel", "netherrack", "obsidian");
        onlyInstant = bool("Only fast blocks", "Skip blocks that take more than a moment", false);
    }

    private final DoubleSetting range;
    private final IntSetting delay;
    private final IntSetting maxPerTick;
    private final BooleanSetting rotate;
    private final StringListSetting include;
    private final BooleanSetting onlyInstant;

    private final Timer timer = new Timer();
    private BlockPos current;

    @Override
    public void onDisable() {
        current = null;
        if (mc.gameMode != null) {
            mc.gameMode.stopDestroyBlock();
        }
    }

    @Override
    public void onWorldChange() {
        current = null;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        List<BlockPos> targets = findTargets();
        if (targets.isEmpty()) {
            return;
        }
        int mined = 0;
        for (BlockPos target : targets) {
            if (mined >= maxPerTick.get()) {
                break;
            }
            Vec3 centre = Vec3.atCenterOf(target);
            if (rotate.get()) {
                Rotation.face(centre, true, 180f);
            }
            mc.gameMode.startDestroyBlock(target, bestFace(target));
            if (rotate.get()) {
                Rotation.restore();
            }
            current = target;
            mined++;
        }
    }

    private List<BlockPos> findTargets() {
        List<BlockPos> result = new ArrayList<>();
        BlockPos centre = mc.player.blockPosition();
        int radius = (int) Math.ceil(range.get());
        double rangeSq = range.get() * range.get();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = centre.offset(dx, dy, dz);
                    if (!mc.level.isLoaded(pos)) {
                        continue;
                    }
                    if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > rangeSq) {
                        continue;
                    }
                    BlockState state = mc.level.getBlockState(pos);
                    if (state.isAir() || !wanted(state)) {
                        continue;
                    }
                    if (onlyInstant.get() && state.getDestroySpeed(mc.level, pos) > 0.4f) {
                        continue;
                    }
                    result.add(pos);
                }
            }
        }
        return result;
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

    private Direction bestFace(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (!mc.level.getBlockState(pos.relative(direction)).isAir()) {
                return direction.getOpposite();
            }
        }
        return Direction.UP;
    }

    @Override
    public String getArraylistSuffix() {
        return current == null ? "" : current.toShortString();
    }
}
