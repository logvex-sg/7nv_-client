package dev.sevennv.client.utils;

import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A read-only self-audit: how likely is each of your enabled modules to be
 * flagged by a typical anti-cheat?
 *
 * <p>This is a heuristic based on how much a module diverges from vanilla
 * client behaviour (rotation, attack timing, movement that ignores physics,
 * packet rewriting, automated inventory/block actions). It is deliberately
 * conservative and does not claim to predict any specific server's rules. It
 * exists so you can see your own footprint — it never talks to the server and
 * never suggests a bypass.
 */
public final class ExposureAudit {
    /** Coarse detection risk of a module. */
    public enum Risk {
        NONE("None"),
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High");

        private final String label;

        Risk(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public int weight() {
            return switch (this) {
                case HIGH -> 3;
                case MEDIUM -> 2;
                case LOW -> 1;
                case NONE -> 0;
            };
        }
    }

    /** One module's assessed risk. */
    public record Assessment(String module, Risk risk, String reason) {
    }

    // Explicit assessments for the modules whose behaviour is worth calling out.
    // Everything else falls back to a category default below.
    private static final Map<String, Assessment> EXPLICIT = buildExplicit();

    private ExposureAudit() {
    }

    private static Map<String, Assessment> buildExplicit() {
        Map<String, Assessment> map = new LinkedHashMap<>();
        // Movement that contradicts vanilla physics.
        high(map, "Speed", "moves faster than vanilla allows");
        high(map, "Long Jump", "jumps farther than vanilla allows");
        high(map, "Step", "steps up taller blocks than vanilla");
        high(map, "Spider", "climbs walls without a ladder");
        high(map, "No Fall", "cancels fall damage server-side expectations");
        high(map, "No Web", "ignores cobweb slowdown");
        high(map, "Velocity", "rewrites knockback packets");
        high(map, "Elytra Boost", "boosts elytra speed beyond vanilla");
        high(map, "Ice Speed", "exceeds ice friction limits");
        high(map, "Water Speed", "exceeds water movement limits");
        high(map, "Bunny Hop", "bhops beyond vanilla momentum");
        high(map, "Auto Parkour", "automated jump timing");
        high(map, "No Slow", "removes the item-use slowdown");
        high(map, "Anti Hunger", "negates hunger effects");
        high(map, "No Levitation", "cancels a server effect");
        // Combat automation.
        high(map, "Kill Aura", "attacks with server-side rotation and timing");
        high(map, "Crystal Aura", "automated crystal placement and detonation");
        high(map, "Auto Clicker", "clicks faster than a human");
        high(map, "Criticals", "forces critical hits");
        high(map, "Trigger Bot", "attacks on crosshair entry");
        high(map, "Aim Assist", "alters rotation toward targets");
        high(map, "Surround", "automated block placement under combat");
        high(map, "Hole Fill", "automated block placement");
        high(map, "Auto Trap", "automated block placement");
        // Block break/place automation.
        high(map, "Nuker", "breaks blocks without aiming");
        high(map, "Auto Mine", "automated block breaking");
        high(map, "Auto City", "automated block breaking");
        high(map, "Scaffold", "automated bridging placement");
        high(map, "Fast Break", "breaks blocks faster than vanilla");
        high(map, "Fast Place", "places blocks faster than vanilla");
        high(map, "Auto Build", "automated block placement");
        // Inventory and interaction automation.
        medium(map, "Auto Totem", "instant inventory swap");
        medium(map, "Auto Armor", "automated armour equipping");
        medium(map, "Auto Weapon", "automated hotbar switching");
        medium(map, "Auto Tool", "automated hotbar switching");
        medium(map, "Auto Eat", "automated item use");
        medium(map, "Auto Pot", "automated potion use");
        medium(map, "Auto Refill", "automated inventory refill");
        medium(map, "Refill", "automated inventory refill");
        medium(map, "Chest Stealer", "automated container interaction");
        medium(map, "Reach", "extends interaction range");
        medium(map, "Middle Click", "automated item pickup");
        medium(map, "Auto Tool Switch", "automated hotbar switching");
        medium(map, "Auto Armor Switch", "automated armour equipping");
        medium(map, "Auto Weapon Switch", "automated hotbar switching");
        medium(map, "Offhand Manager", "automated offhand swapping");
        medium(map, "Inventory Manager", "automated inventory actions");
        // Chat automation is a rules matter rather than a movement one.
        medium(map, "Auto EZ", "sends automated chat");
        medium(map, "Auto GG", "sends automated chat");
        medium(map, "Auto Tip", "sends automated chat");
        medium(map, "Announcer", "sends automated chat");
        // Quality-of-life that changes client state only.
        low(map, "Sprint", "client-side sprint only");
        low(map, "Auto Sprint", "client-side sprint only");
        low(map, "Keep Sprint", "client-side sprint only");
        low(map, "Sprint Reset", "client-side sprint only");
        low(map, "Fullbright", "client-side lighting only");
        low(map, "Zoom", "client-side camera only");
        low(map, "Freecam", "client-side camera only");
        low(map, "Camera Clip", "client-side camera only");
        return map;
    }

    private static void high(Map<String, Assessment> map, String name, String reason) {
        map.put(name.toLowerCase(Locale.ROOT), new Assessment(name, Risk.HIGH, reason));
    }

    private static void medium(Map<String, Assessment> map, String name, String reason) {
        map.put(name.toLowerCase(Locale.ROOT), new Assessment(name, Risk.MEDIUM, reason));
    }

    private static void low(Map<String, Assessment> map, String name, String reason) {
        map.put(name.toLowerCase(Locale.ROOT), new Assessment(name, Risk.LOW, reason));
    }

    /** Assesses a single module. */
    public static Assessment assess(Module module) {
        Assessment explicit = EXPLICIT.get(module.getName().toLowerCase(Locale.ROOT));
        if (explicit != null) {
            return explicit;
        }
        return new Assessment(module.getName(), categoryDefault(module.getCategory()),
                defaultReason(module.getCategory()));
    }

    private static Risk categoryDefault(Category category) {
        return switch (category) {
            case COMBAT, MOVEMENT -> Risk.MEDIUM;
            case WORLD, PLAYER -> Risk.LOW;
            case RENDER, MISC, HUD, CLIENT -> Risk.NONE;
            case DIAGNOSTICS -> Risk.NONE;
        };
    }

    private static String defaultReason(Category category) {
        return switch (category) {
            case COMBAT, MOVEMENT -> "category default: may alter play";
            case WORLD, PLAYER -> "category default: mostly local actions";
            case RENDER, MISC, HUD, CLIENT -> "client-side only";
            case DIAGNOSTICS -> "observational only";
        };
    }

    /** Assessments for every enabled module, riskiest first. */
    public static List<Assessment> audit(List<Module> enabled) {
        List<Assessment> results = new ArrayList<>();
        for (Module module : enabled) {
            Assessment assessment = assess(module);
            if (assessment.risk() != Risk.NONE) {
                results.add(assessment);
            }
        }
        results.sort((a, b) -> Integer.compare(b.risk().weight(), a.risk().weight()));
        return results;
    }

    public static int count(List<Assessment> assessments, Risk risk) {
        int count = 0;
        for (Assessment assessment : assessments) {
            if (assessment.risk() == risk) {
                count++;
            }
        }
        return count;
    }
}
