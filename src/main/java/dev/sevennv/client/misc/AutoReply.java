package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.config.StringListSetting;
import dev.sevennv.client.config.StringSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.ChatUtils;
import dev.sevennv.client.utils.Timer;

import java.util.Random;

/**
 * Shared machinery for the automatic chat reply modules (Auto EZ, Auto GG).
 *
 * <p>Owns the delay, cooldown, session cap, message template list, prefix and
 * suffix settings, and the send pipeline. Subclasses decide when a reply should
 * be queued and which templates are available; the anti-spam rules live here so
 * every reply module behaves the same way.
 */
public abstract class AutoReply extends Module {
    protected final IntSetting delay;
    protected final IntSetting cooldown;
    protected final IntSetting maxPerSession;
    protected final StringSetting prefix;
    protected final StringSetting suffix;
    protected final StringListSetting messages;
    protected final BooleanSetting preview;

    private final Random random = new Random();
    private final Timer cooldownTimer = new Timer();
    private final Timer delayTimer = new Timer();

    private int sentThisSession;
    private String pendingVictim;
    private boolean delayActive;

    protected AutoReply(String name, String description, String[] defaultMessages) {
        super(name, description, Category.MISC);
        delay = integer("Delay", "Milliseconds to wait before sending", 500, 0, 10000);
        cooldown = integer("Cooldown", "Milliseconds between messages", 3000, 0, 60000);
        maxPerSession = integer("Max per session", "Maximum messages per connection (0 = unlimited)", 5, 0, 100);
        prefix = text("Prefix", "Text prepended to every message", "");
        suffix = text("Suffix", "Text appended to every message", "");
        messages = list("Messages", "One message per entry; %player% is the victim", defaultMessages);
        preview = bool("Preview", "Print the next message to chat instead of sending it", false);
    }

    @Override
    public void onEnable() {
        sentThisSession = 0;
    }

    @Override
    public void onWorldChange() {
        sentThisSession = 0;
        delayActive = false;
        pendingVictim = null;
    }

    /** Queues a reply for the given victim, subject to the cooldown and cap. */
    protected void queue(String victim) {
        if (!cooldownTimer.hasElapsed(cooldown.get())) {
            return;
        }
        if (maxPerSession.get() > 0 && sentThisSession >= maxPerSession.get()) {
            return;
        }
        pendingVictim = victim;
        delayActive = true;
        delayTimer.reset();
    }

    @Override
    public void onTick() {
        if (!delayActive || !delayTimer.hasElapsed(delay.get())) {
            return;
        }
        delayActive = false;
        String victim = pendingVictim;
        pendingVictim = null;
        dispatch(victim);
    }

    private void dispatch(String victim) {
        String body = MessageProfiles.sanitise(MessageProfiles.pick(templates(), random, victim));
        if (body == null) {
            return;
        }
        String message = prefix.get() + body + suffix.get();
        if (preview.get()) {
            ChatUtils.info("[" + getName() + " preview] " + message);
            return;
        }
        ChatUtils.send(message);
        sentThisSession++;
        cooldownTimer.reset();
    }

    /** The template list used for the next message; subclasses may vary it by profile. */
    protected StringListSetting templates() {
        return messages;
    }

    /** The next message that would be sent, for the GUI preview button. */
    public String previewMessage() {
        String body = MessageProfiles.pick(templates(), random, "Steve");
        return body == null ? "(no messages)" : prefix.get() + body + suffix.get();
    }

    public int sentThisSession() {
        return sentThisSession;
    }

    @Override
    public String getArraylistSuffix() {
        return maxPerSession.get() > 0
                ? sentThisSession + "/" + maxPerSession.get()
                : String.valueOf(sentThisSession);
    }
}
