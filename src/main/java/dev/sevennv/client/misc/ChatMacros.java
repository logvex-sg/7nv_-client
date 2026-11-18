package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.StringListSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.ChatUtils;

import java.util.Locale;

/**
 * User-defined chat macros, expanded from a command-style trigger.
 *
 * <p>A macro is a line of the form {@code name=message}. Typing {@code .name}
 * or the configured prefix plus the macro name sends the message. Macros never
 * override a real server command, because the prefix is client-local.
 */
public final class ChatMacros extends Module {
    public ChatMacros() {
        super("Chat Macros", "Sends saved messages from a short trigger", Category.MISC);
        prefix = text("Prefix", "Trigger prefix for a macro", ".");
        macros = list("Macros", "One per entry, in name=message form",
                "gm=Good morning", "ty=Thanks!", "brb=Be right back");
        caseSensitive = bool("Case sensitive", "Match macro names case sensitively", false);
        notify = bool("Notify", "Report an unknown macro in chat", true);
    }

    private final dev.sevennv.client.config.StringSetting prefix;
    private final StringListSetting macros;
    private final BooleanSetting caseSensitive;
    private final BooleanSetting notify;

    /**
     * Attempts to run the message as a macro. Returns true when a macro
     * matched, in which case the original message must not be sent.
     */
    public boolean handle(String message) {
        if (message == null || !message.startsWith(prefix.get())) {
            return false;
        }
        String body = message.substring(prefix.get().length());
        for (String entry : macros.get()) {
            int separator = entry.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String name = entry.substring(0, separator);
            String value = entry.substring(separator + 1);
            boolean matches = caseSensitive.get()
                    ? body.startsWith(name)
                    : body.toLowerCase(Locale.ROOT).startsWith(name.toLowerCase(Locale.ROOT));
            if (matches) {
                ChatUtils.send(value);
                return true;
            }
        }
        if (notify.get() && !body.isBlank()) {
            ChatUtils.error("[ChatMacros] no macro named " + body);
        }
        // The prefix is client-local: swallow the message even when no macro
        // matched, so a typo never leaks to the server as chat.
        return true;
    }

    /** Macro names, for the GUI list. */
    public java.util.List<String> names() {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (String entry : macros.get()) {
            int separator = entry.indexOf('=');
            if (separator > 0) {
                names.add(entry.substring(0, separator));
            }
        }
        return names;
    }
}
