package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.ChatUtils;
import dev.sevennv.client.utils.Timer;

/**
 * Sends a configurable tip message to chat periodically. Rate-limited by both a
 * cooldown and a per-session cap so it cannot be used to flood the server.
 */
public final class AutoTip extends Module {
    public AutoTip() {
        super("Auto Tip", "Sends a tip message to chat on an interval", Category.MISC);
        message = text("Message", "Text to send", "gg");
        interval = integer("Interval", "Seconds between messages", 300, 10, 3600);
        random = bool("Randomize", "Send at a random point within the interval", true);
        maxPerSession = integer("Max per session", "Hard cap of messages per session", 20, 1, 100);
    }

    private final dev.sevennv.client.config.StringSetting message;
    private final IntSetting interval;
    private final BooleanSetting random;
    private final IntSetting maxPerSession;
    private final Timer timer = new Timer();
    private int sent;

    @Override
    public void onWorldChange() {
        sent = 0;
        timer.reset();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        if (sent >= maxPerSession.get()) {
            return;
        }
        if (!timer.hasElapsed(interval.get() * 1000L)) {
            return;
        }
        timer.reset();
        if (random.get() && Math.random() < 0.5) {
            return;
        }
        sent++;
        ChatUtils.send(message.get());
    }
}
