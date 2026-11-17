package dev.logvex.client.world;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.hud.HudData;

/**
 * Adds the in-game world time and day to the array list. The HUD time element
 * covers the on-screen readout.
 */
public final class WorldTime extends Module {
    public WorldTime() {
        super("World Time", "Shows the current in-game time", Category.WORLD);
        showDay = bool("Show day", "Include the day number", true);
        nightWarning = bool("Night warning", "Notify when night starts", true);
    }

    private final BooleanSetting showDay;
    private final BooleanSetting nightWarning;

    private boolean wasNight;

    @Override
    public void onWorldChange() {
        wasNight = false;
    }

    @Override
    public void onTick() {
        if (mc.level == null) {
            return;
        }
        boolean night = HudData.isNight();
        if (nightWarning.get() && night && !wasNight) {
            dev.logvex.client.utils.NotificationManager.info("World", "Night has fallen");
        }
        wasNight = night;
    }

    @Override
    public String getArraylistSuffix() {
        if (mc.level == null) {
            return "--:--";
        }
        String time = HudData.worldTimeText();
        if (showDay.get()) {
            return time + " d" + (mc.level.getDayTime() / 24000L);
        }
        return time;
    }
}
