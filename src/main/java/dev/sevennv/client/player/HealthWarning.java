package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.NotificationManager;

/**
 * Warns when the player's health drops below a threshold and again when it
 * recovers, so a close call is never missed. Reads the local health value only.
 */
public final class HealthWarning extends Module {
    public HealthWarning() {
        super("Health Warning", "Warns when your health drops low", Category.PLAYER);
        threshold = number("Threshold", "Warn at or below this health", 8.0, 1.0, 20.0, 0.5);
        warnRegen = bool("Warn on recovery", "Notify when health recovers above the threshold", true);
        playSound = bool("Sound", "Play a note block ping with the warning", false);
    }

    private final DoubleSetting threshold;
    private final BooleanSetting warnRegen;
    private final BooleanSetting playSound;

    private boolean low;

    @Override
    public void onWorldChange() {
        low = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        float health = mc.player.getHealth();
        if (health <= threshold.get()) {
            if (!low) {
                low = true;
                NotificationManager.warn("Health", "Low health: " + Math.round(health) + " HP");
                if (playSound.get()) {
                    mc.player.playSound(net.minecraft.sounds.SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 1.4f);
                }
            }
        } else if (low && warnRegen.get()) {
            low = false;
            NotificationManager.info("Health", "Recovered to " + Math.round(health) + " HP");
        } else if (health > threshold.get()) {
            low = false;
        }
    }
}
