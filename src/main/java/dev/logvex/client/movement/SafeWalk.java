package dev.logvex.client.movement;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Prevents walking off ledges by zeroing the movement impulse when the heading
 * leads over a drop. This is an input-level guard: it never touches the player's
 * position directly, so the server remains authoritative.
 */
public final class SafeWalk extends Module {
    public SafeWalk() {
        super("Safe Walk", "Stops you walking off ledges", Category.MOVEMENT);
        lookAhead = number("Look ahead", "Blocks ahead to test", 0.7, 0.3, 2.0, 0.1);
        allowSneak = bool("Allow sneak", "Keep working while sneaking", false);
    }

    private final DoubleSetting lookAhead;
    private final BooleanSetting allowSneak;

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (!allowSneak.get() && mc.player.isShiftKeyDown()) {
            return;
        }
        if (mc.player.onGround() && wouldFall()) {
            mc.player.zza = 0.0f;
            mc.player.xxa = 0.0f;
        }
    }

    /** True when the player's horizontal heading leads off a ledge. */
    private boolean wouldFall() {
        Vec3 eye = mc.player.getEyePosition();
        Vec3 look = mc.player.getLookAngle();
        Vec3 target = new Vec3(eye.x + look.x * lookAhead.get(), mc.player.getY() - 1.0, eye.z + look.z * lookAhead.get());
        BlockPos below = BlockPos.containing(target.x, target.y, target.z);
        return !isSolid(below) && !isSolid(below.below()) && !isSolid(below.below(2));
    }

    private boolean isSolid(BlockPos pos) {
        return !mc.level.getBlockState(pos).getCollisionShape(mc.level, pos).isEmpty();
    }

    /** Exposed for diagnostics: whether the last check found a ledge. */
    public boolean isLedgeAhead() {
        return mc.level != null && mc.player != null && wouldFall();
    }
}
