package dev.sevennv.client.movement;

import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Restores normal ground friction while standing on ice by damping the slip
 * that the server's movement model would otherwise apply.
 */
public final class IceSpeed extends Module {
    public IceSpeed() {
        super("Ice Speed", "Removes the slipperiness of ice", Category.MOVEMENT);
        friction = number("Friction", "Simulated ground friction", 0.6, 0.1, 0.9, 0.02);
    }

    private final DoubleSetting friction;

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) {
            return;
        }
        var below = mc.player.blockPosition().below();
        var state = mc.level.getBlockState(below);
        if (!state.is(Blocks.ICE) && !state.is(Blocks.PACKED_ICE)
                && !state.is(Blocks.BLUE_ICE) && !state.is(Blocks.FROSTED_ICE)) {
            return;
        }
        if (!mc.player.onGround()) {
            return;
        }
        Vec3 motion = mc.player.getDeltaMovement();
        double factor = friction.get() / 0.91;
        mc.player.setDeltaMovement(motion.x * factor, motion.y, motion.z * factor);
    }
}
