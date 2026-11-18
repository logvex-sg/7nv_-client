package dev.sevennv.client.combat;

import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.ReachTracker;

/**
 * Extends interaction range via ordinary attribute modifiers. The server still
 * validates every interaction; the bonus only widens what the client will aim
 * at, so it can never be used to touch something the server rejects.
 */
public final class Reach extends Module {
    public Reach() {
        super("Reach", "Extends block and entity interaction range", Category.COMBAT);
        blockReach = number("Block reach", "Extra block reach in blocks", 1.0, 0.0, 5.0, 0.1);
        entityReach = number("Entity reach", "Extra entity reach in blocks", 1.0, 0.0, 5.0, 0.1);
    }

    private final DoubleSetting blockReach;
    private final DoubleSetting entityReach;

    @Override
    public void onEnable() {
        ReachTracker.apply(blockReach.get(), entityReach.get());
    }

    @Override
    public void onTick() {
        ReachTracker.apply(blockReach.get(), entityReach.get());
    }

    @Override
    public void onDisable() {
        ReachTracker.clear();
    }

    @Override
    public void onWorldChange() {
        ReachTracker.clear();
    }

    @Override
    public String getArraylistSuffix() {
        return String.format(java.util.Locale.ROOT, "%.1f", blockReach.get());
    }
}
