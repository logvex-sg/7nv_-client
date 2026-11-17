package dev.logvex.client.player;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;

/**
 * Adds a status-effects readout to the array list. The HUD potion element is
 * the on-screen equivalent; this module keeps the information available when
 * the HUD is off.
 */
public final class StatusEffects extends Module {
    public StatusEffects() {
        super("Status Effects", "Lists your active status effects", Category.PLAYER);
        showAmplifier = bool("Amplifier", "Include the effect level", true);
        showDuration = bool("Duration", "Include the remaining duration", false);
    }

    private final BooleanSetting showAmplifier;
    private final BooleanSetting showDuration;

    @Override
    public String getArraylistSuffix() {
        if (mc.player == null) {
            return "";
        }
        var effects = mc.player.getActiveEffects();
        return effects.isEmpty() ? "none" : String.valueOf(effects.size());
    }

    /** Human-readable summary of the active effects, for the HUD or commands. */
    public String summary() {
        if (mc.player == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (var effect : mc.player.getActiveEffects()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(effect.getEffect().value().getDisplayName().getString());
            if (showAmplifier.get() && effect.getAmplifier() > 0) {
                builder.append(' ').append(dev.logvex.client.hud.HudFormat.toRoman(effect.getAmplifier() + 1));
            }
            if (showDuration.get()) {
                builder.append(' ').append(dev.logvex.client.hud.HudFormat.effectDuration(effect));
            }
        }
        return builder.toString();
    }
}
