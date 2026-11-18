package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.CombatStats;

/**
 * Tracks nearby end crystals and reports the count. Useful for keeping an eye
 * on how many crystals an opponent has placed.
 */
public final class CrystalCounter extends Module {
    public CrystalCounter() {
        super("Crystal Counter", "Counts end crystals near you", Category.COMBAT);
        range = number("Range", "Count crystals within this distance", 8.0, 1.0, 32.0, 1.0);
        trackPlaced = bool("Track placed", "Also count crystals anywhere in the level", false);
        announce = bool("Announce changes", "Notify when the count changes", true);
    }

    private final DoubleSetting range;
    private final BooleanSetting trackPlaced;
    private final BooleanSetting announce;

    private int lastNearby = -1;
    private int lastTotal = -1;

    @Override
    public void onEnable() {
        lastNearby = -1;
        lastTotal = -1;
    }

    @Override
    public void onWorldChange() {
        lastNearby = -1;
        lastTotal = -1;
    }

    @Override
    public void onTick() {
        int nearby = CombatStats.crystalCount(range.get());
        if (nearby != lastNearby) {
            if (announce.get() && lastNearby != -1) {
                dev.sevennv.client.utils.NotificationManager.info("Crystals", nearby + " crystals nearby");
            }
            lastNearby = nearby;
        }
        if (trackPlaced.get()) {
            lastTotal = CombatStats.totalCrystals();
        }
    }

    @Override
    public String getArraylistSuffix() {
        int nearby = Math.max(0, lastNearby);
        return trackPlaced.get() ? nearby + "/" + Math.max(0, lastTotal) : String.valueOf(nearby);
    }
}
