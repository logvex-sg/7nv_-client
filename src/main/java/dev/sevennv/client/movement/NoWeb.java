package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Restores normal movement speed inside cobwebs. The vanilla slowdown is
 * applied as a velocity multiplier each tick; this re-scales the horizontal
 * velocity back to the pre-web value without touching the server state.
 */
public final class NoWeb extends Module {
    public NoWeb() {
        super("No Web", "Removes the cobweb movement slowdown", Category.MOVEMENT);
        onlyHorizontal = bool("Only horizontal", "Leave the vertical slowdown intact", true);
        factor = number("Factor", "Velocity multiplier to restore", 2.0, 1.0, 5.0, 0.1);
    }

    private final BooleanSetting onlyHorizontal;
    private final dev.sevennv.client.config.DoubleSetting factor;

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (!inWeb()) {
            return;
        }
        Vec3 motion = mc.player.getDeltaMovement();
        double x = motion.x * factor.get();
        double z = motion.z * factor.get();
        double y = onlyHorizontal.get() ? motion.y : motion.y * factor.get();
        mc.player.setDeltaMovement(x, y, z);
    }

    private boolean inWeb() {
        var pos = mc.player.blockPosition();
        return mc.level.getBlockState(pos).is(Blocks.COBWEB)
                || mc.level.getBlockState(pos.above()).is(Blocks.COBWEB);
    }
}
