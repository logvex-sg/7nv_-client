package dev.sevennv.client.movement;

import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.mixin.LivingEntityAccessor;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Clears the short cooldown the game applies between jumps, so held jump keeps
 * hopping without the usual pause. Only the local cooldown field is written.
 */
public final class NoJumpDelay extends Module {
    public NoJumpDelay() {
        super("No Jump Delay", "Removes the pause between jumps", Category.MOVEMENT);
        delay = integer("Delay", "Cooldown to set, in ticks", 0, 0, 10);
    }

    private final IntSetting delay;

    @Override
    public void onTick() {
        if (mc.player instanceof LivingEntityAccessor accessor) {
            accessor.sevennv$setNoJumpDelay(delay.get());
        }
    }
}
