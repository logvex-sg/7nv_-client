package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.ChatUtils;
import dev.sevennv.client.utils.Timer;

/**
 * Announces significant events (deaths, low health) in chat using configurable
 * templates. Every message passes through a cooldown so chat cannot be spammed.
 */
public final class Announcer extends Module {
    public Announcer() {
        super("Announcer", "Announces events in chat with templates", Category.MISC);
        announceDeaths = bool("Deaths", "Announce nearby player deaths", true);
        announceLowHealth = bool("Low health", "Announce when your health is low", true);
        deathTemplate = text("Death template", "Text sent on a death", "nice fight");
        healthTemplate = text("Health template", "Text sent when low", "need help");
        healthThreshold = integer("Health threshold", "Announce below this health", 6, 1, 20);
        cooldown = integer("Cooldown", "Milliseconds between messages", 5000, 1000, 60000);
    }

    private final BooleanSetting announceDeaths;
    private final BooleanSetting announceLowHealth;
    private final dev.sevennv.client.config.StringSetting deathTemplate;
    private final dev.sevennv.client.config.StringSetting healthTemplate;
    private final IntSetting healthThreshold;
    private final IntSetting cooldown;
    private final Timer timer = new Timer();
    private boolean healthWarned;

    @Override
    public void onWorldChange() {
        healthWarned = false;
    }

    @Override
    public void onChat(dev.sevennv.client.events.ChatReceiveEvent event) {
        if (!announceDeaths.get()) {
            return;
        }
        String text = event.getText();
        if (text == null || (!text.contains("was slain") && !text.contains("died"))) {
            return;
        }
        if (!timer.tick(cooldown.get())) {
            return;
        }
        ChatUtils.send(deathTemplate.get());
    }

    @Override
    public void onTick() {
        if (mc.player == null || !announceLowHealth.get()) {
            return;
        }
        boolean low = mc.player.getHealth() <= healthThreshold.get();
        if (low && !healthWarned && timer.tick(cooldown.get())) {
            healthWarned = true;
            ChatUtils.send(healthTemplate.get());
        } else if (!low) {
            healthWarned = false;
        }
    }
}
