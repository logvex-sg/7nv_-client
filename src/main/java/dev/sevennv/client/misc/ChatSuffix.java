package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.StringSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Appends a configurable tag to outgoing chat, the classic client watermark.
 *
 * <p>The suffix is added on the client before the message is handed to the
 * vanilla chat path, so signing and the server's own rules still apply. It is
 * only ever appended to a message the user actually typed, never sent on its
 * own, and a small cooldown prevents it firing on rapid repeats.
 */
public final class ChatSuffix extends Module {
    public ChatSuffix() {
        super("Chat Suffix", "Adds a tag to the end of your chat", Category.MISC);
        suffix = text("Suffix", "Text appended to each message", " | 7nv");
        separator = text("Separator", "Placed between message and suffix", "");
        skipCommands = bool("Skip commands", "Do not append to /commands", true);
        respectCooldown = bool("Cooldown", "Do not append twice within a second", true);
    }

    private final StringSetting suffix;
    private final StringSetting separator;
    private final BooleanSetting skipCommands;
    private final BooleanSetting respectCooldown;

    private long lastAppend;

    @Override
    public void onWorldChange() {
        lastAppend = 0;
    }

    /** True when a message would currently get the suffix applied. */
    public boolean applies(String message) {
        if (message == null || message.isEmpty() || suffix.get().isEmpty()) {
            return false;
        }
        if (skipCommands.get() && message.startsWith("/")) {
            return false;
        }
        if (respectCooldown.get() && System.currentTimeMillis() - lastAppend < 1000L) {
            return false;
        }
        return true;
    }

    /**
     * Returns the message with the suffix appended, or the original when the
     * module does not apply. Called from the chat-send bridge.
     */
    public String decorate(String message) {
        if (!applies(message)) {
            return message;
        }
        lastAppend = System.currentTimeMillis();
        String tail = separator.get() + suffix.get();
        // Respect the vanilla 256-character chat limit; trim the base instead of
        // letting the server reject the whole line.
        int room = 256 - tail.length();
        String base = message.length() > room ? message.substring(0, Math.max(0, room)) : message;
        return base + tail;
    }

    /** Preview line for the ClickGUI so the user sees the final form. */
    public String preview() {
        return "hello world" + separator.get() + suffix.get();
    }
}
