package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.StringSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Prefixes incoming chat messages with the local wall-clock time.
 *
 * <p>The prefix is applied by rewriting the component as it arrives, before it
 * reaches the chat history, so the timestamp is part of the message itself and
 * survives scrolling and chat re-opens.
 */
public final class ChatTimestamps extends Module {
    public ChatTimestamps() {
        super("Chat Timestamps", "Prefixes chat messages with the time", Category.MISC);
        format = text("Format", "Java time pattern", "HH:mm:ss");
        brackets = text("Brackets", "Characters wrapped around the time", "[]");
        colorCode = text("Color", "Minecraft colour code for the timestamp", "\u00a77");
    }

    private final StringSetting format;
    private final StringSetting brackets;
    private final StringSetting colorCode;

    private DateTimeFormatter formatter;
    private String cachedPattern;

    /**
     * Adds the timestamp to the message. Called from the chat-receive bridge,
     * which rewrites the component before it is displayed.
     */
    public Component decorate(Component message) {
        String stamp = prefix();
        if (stamp.isEmpty() || message == null) {
            return message;
        }
        MutableComponent combined = Component.literal(stamp).append(message.copy());
        return combined;
    }

    /** The formatted prefix for the current moment, or empty on a bad pattern. */
    public String prefix() {
        DateTimeFormatter current = resolveFormatter();
        if (current == null) {
            return "";
        }
        String time = LocalTime.now().format(current);
        String bracketsValue = brackets.get();
        String open = bracketsValue.isEmpty() ? "" : bracketsValue.substring(0, 1);
        String close = bracketsValue.length() < 2 ? "" : bracketsValue.substring(1, 2);
        return colorCode.get() + open + time + close + " ";
    }

    private DateTimeFormatter resolveFormatter() {
        String pattern = format.get();
        if (!pattern.equals(cachedPattern)) {
            try {
                formatter = DateTimeFormatter.ofPattern(pattern);
                cachedPattern = pattern;
            } catch (IllegalArgumentException exception) {
                formatter = null;
                cachedPattern = null;
            }
        }
        return formatter;
    }
}
