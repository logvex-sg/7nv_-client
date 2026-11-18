package dev.sevennv.client.misc;

import dev.sevennv.client.config.StringListSetting;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Shared message-template handling for the chat reply modules.
 *
 * <p>A profile is just an ordered list of candidate messages; one is chosen per
 * trigger. The active profile is selected by name from a fixed profile list, so
 * a module can keep several separate sets of lines without duplicating them.
 */
public final class MessageProfiles {
    /** Profile names available to the reply modules. */
    public static final List<String> PROFILE_NAMES = List.of("default", "short", "taunt", "polite");

    private static final Map<String, List<String>> TEMPLATES = Map.of(
            "default", List.of("ez", "gg", "e z", "too easy", "%player% ez"),
            "short", List.of("ez", "gg"),
            "taunt", List.of("ez", "%player% ez", "get good", "sit", "too easy"),
            "polite", List.of("good fight", "gg", "well played", "nice try %player%")
    );

    private MessageProfiles() {
    }

    /** The template set for a profile name, or an empty list for an unknown name. */
    public static List<String> templates(String profile) {
        return TEMPLATES.getOrDefault(profile == null ? "" : profile.toLowerCase(Locale.ROOT), List.of());
    }

    /**
     * Picks a message from the given list, expanding the placeholders
     * {@code %player%}, {@code %name%} and {@code %killer%} with the supplied
     * victim name. Returns null when the list is empty.
     */
    public static String pick(StringListSetting templates, Random random, String victim) {
        if (templates == null || templates.isEmpty()) {
            return null;
        }
        String chosen = templates.random(random);
        return expand(chosen, victim);
    }

    /** Picks a message from a plain list, expanding the victim placeholders. */
    public static String pick(List<String> templates, Random random, String victim) {
        if (templates == null || templates.isEmpty()) {
            return null;
        }
        return expand(templates.get(random.nextInt(templates.size())), victim);
    }

    public static String expand(String template, String victim) {
        if (template == null) {
            return null;
        }
        String name = victim == null || victim.isBlank() ? "you" : victim;
        return template
                .replace("%player%", name)
                .replace("%name%", name)
                .replace("%killer%", name);
    }

    /** Collapses runs of whitespace and trims, so generated messages stay tidy. */
    public static String sanitise(String message) {
        if (message == null) {
            return null;
        }
        String cleaned = message.replaceAll("\\s+", " ").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
