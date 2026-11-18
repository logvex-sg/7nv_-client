package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.utils.ChatUtils;
import net.minecraft.world.entity.LivingEntity;

import java.util.Locale;

/**
 * Sends a "gg" style reply when a game ends or when the player is killed.
 *
 * <p>Triggers: a chat message matching the end-of-game patterns, or the local
 * player's own death. Both share the delay, cooldown and session cap inherited
 * from {@link AutoReply}.
 */
public final class AutoGg extends AutoReply {
    public AutoGg() {
        super("Auto GG", "Sends a configurable message when a game ends",
                new String[]{"gg", "good game", "gg wp"});
        onMatchEnd = bool("On match end", "Trigger when the server announces the match is over", true);
        onOwnDeath = bool("On own death", "Trigger when you die", true);
        matchEndMessage = text("Match end trigger", "Chat text that counts as the match ending", "winner");
        useRegex = bool("Regex", "Treat the trigger as a regular expression", false);
        caseSensitive = bool("Case sensitive", "Match the trigger case sensitively", false);
    }

    private final BooleanSetting onMatchEnd;
    private final BooleanSetting onOwnDeath;
    private final dev.sevennv.client.config.StringSetting matchEndMessage;
    private final BooleanSetting useRegex;
    private final BooleanSetting caseSensitive;

    /** Called by the combat bridge when the local player dies. */
    public void onDeath() {
        if (onOwnDeath.get()) {
            queue(null);
        }
    }

    @Override
    public void onChat(dev.sevennv.client.events.ChatReceiveEvent event) {
        if (!onMatchEnd.get() || event.getText().isEmpty()) {
            return;
        }
        String trigger = matchEndMessage.get();
        if (trigger.isEmpty()) {
            return;
        }
        String text = event.getText();
        boolean match;
        if (useRegex.get()) {
            try {
                int flags = caseSensitive.get() ? 0 : java.util.regex.Pattern.CASE_INSENSITIVE;
                match = java.util.regex.Pattern.compile(trigger, flags).matcher(text).find();
            } catch (java.util.regex.PatternSyntaxException exception) {
                ChatUtils.error("[AutoGG] invalid regex: " + exception.getDescription());
                return;
            }
        } else if (caseSensitive.get()) {
            match = text.contains(trigger);
        } else {
            match = text.toLowerCase(Locale.ROOT).contains(trigger.toLowerCase(Locale.ROOT));
        }
        if (match) {
            queue(null);
        }
    }
}
