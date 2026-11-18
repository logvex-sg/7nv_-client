package dev.sevennv.client.world;

import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.mixin.MinecraftAccessor;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Shortens the delay between block placements by writing the game's own
 * right-click cooldown. Only the local cooldown is touched; every placement
 * still goes through the ordinary use path.
 */
public final class FastPlace extends Module {
    public FastPlace() {
        super("Fast Place", "Removes the delay between block placements", Category.WORLD);
        delay = integer("Delay", "Right-click cooldown in ticks", 0, 0, 4);
        onlyBlocks = bool("Only blocks", "Only shorten the cooldown while placing blocks", false);
    }

    private final IntSetting delay;
    private final dev.sevennv.client.config.BooleanSetting onlyBlocks;

    @Override
    public void onTick() {
        if (mc.player == null || !(mc instanceof MinecraftAccessor accessor)) {
            return;
        }
        if (onlyBlocks.get() && mc.options.keyUse.isDown()
                && mc.player.getMainHandItem().isEmpty()) {
            return;
        }
        if (mc.options.keyUse.isDown()) {
            accessor.sevennv$setRightClickDelay(delay.get());
        }
    }
}
