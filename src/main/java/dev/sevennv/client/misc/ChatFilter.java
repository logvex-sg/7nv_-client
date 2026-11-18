package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Filters unwanted messages out of chat: repeated spam, join and leave
 * announcements and a configurable list of phrases. Messages are dropped before
 * they reach the chat history, so a hidden message never reappears.
 *
 * <p>Own chat messages are always kept so you can still see what you sent.
 */
public final class ChatFilter extends Module {
    public ChatFilter() {
        super("Chat Filter", "Hides spam and unwanted chat messages", Category.MISC);
        hideJoins = bool("Hide joins", "Hide player join and leave messages", true);
        hideDeaths = bool("Hide deaths", "Hide death messages", false);
        antiSpam = bool("Anti spam", "Hide repeated identical messages", true);
        spamWindow = integer("Spam window", "Milliseconds within which repeats are spam", 3000, 200, 30000);
        hideOwnName = bool("Hide own name", "Hide messages that mention your name", false);
    }

    private final BooleanSetting hideJoins;
    private final BooleanSetting hideDeaths;
    private final BooleanSetting antiSpam;
    private final dev.sevennv.client.config.IntSetting spamWindow;
    private final BooleanSetting hideOwnName;

    private String lastMessage = "";
    private long lastTime;

    /** Returns true when the message should be shown. */
    public boolean allow(String text) {
        if (text == null || text.isEmpty()) {
            return true;
        }
        String lower = text.toLowerCase(java.util.Locale.ROOT);
        if (hideJoins.get() && (lower.contains("joined the game") || lower.contains("left the game"))) {
            return false;
        }
        if (hideDeaths.get() && (lower.contains("was slain") || lower.contains("was shot")
                || lower.contains("drowned") || lower.contains("blew up")
                || lower.contains("fell from") || lower.contains("hit the ground")
                || lower.contains("withered away") || lower.contains("burned to death"))) {
            return false;
        }
        if (hideOwnName.get() && mc.player != null
                && lower.contains(mc.player.getGameProfile().name().toLowerCase(java.util.Locale.ROOT))) {
            return false;
        }
        if (antiSpam.get() && isRepeat(text)) {
            return false;
        }
        return true;
    }

    private boolean isRepeat(String text) {
        long now = System.currentTimeMillis();
        boolean repeat = text.equals(lastMessage) && now - lastTime < spamWindow.get();
        if (!repeat) {
            lastMessage = text;
            lastTime = now;
        }
        return repeat;
    }

    @Override
    public void onWorldChange() {
        lastMessage = "";
        lastTime = 0;
    }
}
