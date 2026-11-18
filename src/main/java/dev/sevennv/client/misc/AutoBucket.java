package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Picks up fluid source blocks near you with a bucket, and optionally places
 * them back down. Uses the vanilla item-use path so the server sees a normal
 * bucket click.
 */
public final class AutoBucket extends Module {
    public AutoBucket() {
        super("Auto Bucket", "Picks up fluids near you with a bucket", Category.MISC);
        range = number("Range", "Maximum bucket distance", 4.0, 1.0, 6.0, 0.5);
        delay = integer("Delay", "Milliseconds between actions", 300, 50, 2000);
        empty = bool("Empty", "Pick up fluid source blocks", true);
        autoSwitch = bool("Auto switch", "Select a bucket automatically", true);
    }

    private final dev.sevennv.client.config.DoubleSetting range;
    private final IntSetting delay;
    private final BooleanSetting empty;
    private final BooleanSetting autoSwitch;

    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }
        if (!empty.get() || !timer.tick(delay.get())) {
            return;
        }
        BlockPos target = findFluid();
        if (target == null) {
            return;
        }
        if (autoSwitch.get() && !mc.player.getMainHandItem().is(Items.BUCKET)) {
            InventoryUtils.switchTo(Items.BUCKET);
        }
        if (!mc.player.getMainHandItem().is(Items.BUCKET)) {
            return;
        }
        Vec3 hit = Vec3.atCenterOf(target);
        BlockHitResult result = new BlockHitResult(hit, Direction.UP, target, false);
        mc.gameMode.useItemOn(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND, result);
        mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    private BlockPos findFluid() {
        int radius = (int) Math.ceil(range.get());
        BlockPos centre = mc.player.blockPosition();
        BlockPos best = null;
        double bestDistance = range.get() * range.get();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = centre.offset(dx, dy, dz);
                    if (!mc.level.isLoaded(pos)) {
                        continue;
                    }
                    var fluid = mc.level.getFluidState(pos);
                    if (fluid.isEmpty() || !fluid.isSource()) {
                        continue;
                    }
                    double distance = mc.player.position().distanceToSqr(Vec3.atCenterOf(pos));
                    if (distance <= bestDistance) {
                        bestDistance = distance;
                        best = pos;
                    }
                }
            }
        }
        return best;
    }
}
