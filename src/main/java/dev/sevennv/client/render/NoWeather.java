package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Stops the client from drawing rain and snow. Weather is still simulated by
 * the server; only the falling-particles pass is skipped.
 */
public final class NoWeather extends Module {
    public NoWeather() {
        super("No Weather", "Hides rain and snow", Category.RENDER);
        rain = bool("Rain", "Hide rain", true);
        snow = bool("Snow", "Hide snow", true);
    }

    private final BooleanSetting rain;
    private final BooleanSetting snow;

    public boolean hideRain() {
        return isEnabled() && rain.get();
    }

    public boolean hideSnow() {
        return isEnabled() && snow.get();
    }

    public boolean hidesAnything() {
        return isEnabled() && (rain.get() || snow.get());
    }
}
