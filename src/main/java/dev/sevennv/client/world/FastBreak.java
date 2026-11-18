package dev.sevennv.client.world;

import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.mixin.MultiPlayerGameModeAccessor;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Reduces the per-block break delay. The block is still broken through the
 * vanilla destroy path and the server's own block-breaking validation is
 * unchanged.
 */
public final class FastBreak extends Module {
    public FastBreak() {
        super("Fast Break", "Breaks blocks faster", Category.WORLD);
        delay = integer("Delay", "Break delay in ticks", 0, 0, 4);
    }

    private final IntSetting delay;

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null || !(mc.gameMode instanceof MultiPlayerGameModeAccessor accessor)) {
            return;
        }
        if (mc.gameMode.isDestroying()) {
            accessor.sevennv$setDestroyDelay(delay.get());
        }
    }
}
