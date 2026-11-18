package dev.sevennv.client.utils;

import dev.sevennv.client.events.PacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive server fingerprinting for the anti-cheat detector.
 *
 * <p>Everything here is read from data the server already sends during a normal
 * session: the brand string, the plugin channels it registers, the command
 * tree it advertises, the tab-list header/footer and the scoreboard objective
 * names. Nothing is probed, no extra packet is sent and no protection is
 * bypassed — the detector only names what the connection has already revealed.
 *
 * <p>Signals are written from the client thread (packets are drained there), so
 * the plain volatile fields and the concurrent sets are sufficient.
 */
public final class ServerFingerprint {
    /** How a matched signature is classified. */
    public enum Category {
        ANTI_CHEAT("Anti-cheat"),
        PROXY("Proxy"),
        SERVER_SOFTWARE("Server"),
        PLUGIN("Plugin"),
        UNKNOWN("Unknown");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    /** One matched signature with its evidence and a 0..1 confidence. */
    public record Match(String name, Category category, double confidence, List<String> evidence) {
    }

    // Evidence weights, chosen so a brand alone is suggestive but a channel or
    // command that names the plugin is close to conclusive.
    private static final double W_BRAND = 0.55;
    private static final double W_CHANNEL = 0.50;
    private static final double W_COMMAND = 0.35;
    private static final double W_TEXT = 0.20;

    private static volatile String brand = "";
    private static final Set<String> channels = ConcurrentHashMap.newKeySet();
    private static final Set<String> commands = ConcurrentHashMap.newKeySet();
    private static final Set<String> objectives = ConcurrentHashMap.newKeySet();
    private static volatile String tabHeader = "";
    private static volatile String tabFooter = "";
    private static volatile int playerCount;
    private static volatile int maxPlayers;

    private static long lastSample;
    private static String lastBrand = "";

    private ServerFingerprint() {
    }

    // ------------------------------------------------------------- recording

    /** Feeds one observed packet into the fingerprint. Client thread only. */
    public static void observe(PacketEvent event) {
        Object packet = event.getPacket();
        if (packet instanceof ClientboundCustomPayloadPacket custom) {
            recordPayload(custom.payload());
        } else if (packet instanceof ClientboundTabListPacket tab) {
            tabHeader = tab.header().getString();
            tabFooter = tab.footer().getString();
        } else if (packet instanceof ClientboundSetObjectivePacket objective) {
            if (objective.getMethod() != ClientboundSetObjectivePacket.METHOD_REMOVE) {
                String name = objective.getObjectiveName();
                if (name != null && !name.isEmpty()) {
                    objectives.add(name.toLowerCase(Locale.ROOT));
                }
            } else {
                objectives.remove(objective.getObjectiveName() == null
                        ? "" : objective.getObjectiveName().toLowerCase(Locale.ROOT));
            }
        }
    }

    private static void recordPayload(CustomPacketPayload payload) {
        if (payload instanceof BrandPayload brandPayload) {
            brand = brandPayload.brand() == null ? "" : brandPayload.brand();
            return;
        }
        Identifier id = payload.type().id();
        // Only plugin-namespace channels carry a server plugin identity.
        if (id != null && !"minecraft".equals(id.getNamespace())) {
            channels.add(id.getNamespace().toLowerCase(Locale.ROOT));
        }
    }

    /**
     * Reads the connection state that does not arrive as an ordinary packet:
     * the brand, the advertised command roots and the player-list size. Sampled
     * at most once a second to keep the per-tick cost trivial.
     */
    public static void sample(Minecraft mc) {
        long now = System.currentTimeMillis();
        if (now - lastSample < 1000L) {
            return;
        }
        lastSample = now;
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) {
            return;
        }
        String serverBrand = connection.serverBrand();
        if (serverBrand != null && !serverBrand.equals(lastBrand)) {
            brand = serverBrand;
            lastBrand = serverBrand;
        }
        try {
            var dispatcher = connection.getCommands();
            if (dispatcher != null) {
                for (var node : dispatcher.getRoot().getChildren()) {
                    commands.add(node.getName().toLowerCase(Locale.ROOT));
                }
            }
        } catch (Exception exception) {
            // A malformed command tree must never take the client down.
            dev.sevennv.client.SevennvClient.LOGGER.debug("Command tree sample failed", exception);
        }
        var players = connection.getOnlinePlayers();
        if (players != null) {
            playerCount = players.size();
        }
    }

    /** Updates the player-list size from a tab-list update, when available. */
    public static void onPlayerList(int online, int max) {
        playerCount = online;
        maxPlayers = max;
    }

    public static void reset() {
        brand = "";
        lastBrand = "";
        channels.clear();
        commands.clear();
        objectives.clear();
        tabHeader = "";
        tabFooter = "";
        playerCount = 0;
        maxPlayers = 0;
        lastSample = 0L;
    }

    // ------------------------------------------------------------- readout

    public static String brand() {
        return brand;
    }

    public static Set<String> channels() {
        return channels;
    }

    public static Set<String> commands() {
        return commands;
    }

    public static Set<String> objectives() {
        return objectives;
    }

    public static String tabText() {
        return (tabHeader + " " + tabFooter).toLowerCase(Locale.ROOT);
    }

    public static int playerCount() {
        return playerCount;
    }

    /** True once any identifying signal has been observed. */
    public static boolean hasSignals() {
        return !brand.isEmpty() || !channels.isEmpty() || !commands.isEmpty();
    }

    // ------------------------------------------------------------- detection

    /** Ranked matches for every signature with at least one piece of evidence. */
    public static List<Match> detect() {
        return detect(brand, channels, commands, tabText());
    }

    /**
     * Pure matcher over explicit inputs. Split out from {@link #detect()} so the
     * knowledge base can be exercised without a live connection.
     */
    public static List<Match> detect(String brand, Set<String> channels, Set<String> commands, String text) {
        String brandLower = brand == null ? "" : brand.toLowerCase(Locale.ROOT);
        String textLower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        List<Match> matches = new ArrayList<>();
        for (Signature signature : SIGNATURES) {
            List<String> evidence = new ArrayList<>();
            double confidence = 0.0;
            for (String key : signature.brandKeys) {
                if (brandLower.contains(key)) {
                    evidence.add("brand ~ \"" + key + "\"");
                    confidence += W_BRAND;
                    break;
                }
            }
            for (String key : signature.channelKeys) {
                if (channels.contains(key)) {
                    evidence.add("channel " + key);
                    confidence += W_CHANNEL;
                    break;
                }
            }
            for (String key : signature.commandKeys) {
                if (commands.contains(key)) {
                    evidence.add("command /" + key);
                    confidence += W_COMMAND;
                    break;
                }
            }
            if (!signature.textKeys.isEmpty()) {
                for (String key : signature.textKeys) {
                    if (textLower.contains(key)) {
                        evidence.add("tab text ~ \"" + key + "\"");
                        confidence += W_TEXT;
                        break;
                    }
                }
            }
            if (confidence <= 0.0) {
                continue;
            }
            matches.add(new Match(signature.name, signature.category,
                    Math.min(1.0, confidence), List.copyOf(evidence)));
        }
        matches.sort(Comparator.comparingDouble(Match::confidence).reversed());
        return matches;
    }

    /** Highest-confidence match of a given category, or null. */
    public static Match best(Category category) {
        for (Match match : detect()) {
            if (match.category() == category) {
                return match;
            }
        }
        return null;
    }

    /** The top anti-cheat match, or null when none was identified. */
    public static Match bestAntiCheat() {
        return best(Category.ANTI_CHEAT);
    }

    /** A stable one-line summary of the strongest detection, for notifications. */
    public static String summary() {
        Match match = bestAntiCheat();
        if (match == null) {
            return brand.isEmpty() ? "unknown" : "none (brand " + brand + ")";
        }
        return match.name() + " (" + Math.round(match.confidence() * 100) + "%)";
    }

    private record Signature(String name, Category category, Set<String> brandKeys,
                             Set<String> channelKeys, Set<String> commandKeys, Set<String> textKeys) {
    }

    private static Set<String> keys(String... values) {
        return new LinkedHashSet<>(List.of(values));
    }

    // Knowledge base of the markers each anti-cheat, proxy, server flavour and
    // common plugin exposes through the channels above. Keys are lower case.
    private static final List<Signature> SIGNATURES = List.of(
            // ---------------------------------------------------- anti-cheats
            new Signature("Vulcan", Category.ANTI_CHEAT,
                    keys("vulcan"), keys("vulcan"), keys("vulcan", "vulcanproxy", "alerts"), keys("vulcan")),
            new Signature("Grim", Category.ANTI_CHEAT,
                    keys("grim"), keys("grim", "grimac"), keys("grim", "grimac"), keys("grim")),
            new Signature("Matrix", Category.ANTI_CHEAT,
                    keys("matrix"), keys("matrix"), keys("matrix"), keys("matrix")),
            new Signature("Spartan", Category.ANTI_CHEAT,
                    keys("spartan"), keys("spartan"), keys("spartan"), keys("spartan")),
            new Signature("Intave", Category.ANTI_CHEAT,
                    keys("intave"), keys("intave"), keys("intave"), keys("intave")),
            new Signature("Verus", Category.ANTI_CHEAT,
                    keys("verus"), keys("verus"), keys("verus"), keys("verus")),
            new Signature("AAC", Category.ANTI_CHEAT,
                    keys("aac", "advancedanticheat"), keys("aac"), keys("aac"), keys("aac")),
            new Signature("NoCheatPlus", Category.ANTI_CHEAT,
                    keys("nocheatplus", "ncp"), keys("nocheatplus", "ncp"), keys("nocheatplus", "ncp"), keys()),
            new Signature("Polar", Category.ANTI_CHEAT,
                    keys("polar"), keys("polar"), keys("polar"), keys("polar")),
            new Signature("Reflex", Category.ANTI_CHEAT,
                    keys("reflex"), keys("reflex"), keys("reflex"), keys("reflex")),
            new Signature("Horizon", Category.ANTI_CHEAT,
                    keys("horizon"), keys("horizon"), keys("horizon"), keys("horizon")),
            new Signature("Karhu", Category.ANTI_CHEAT,
                    keys("karhu"), keys("karhu"), keys("karhu"), keys()),
            new Signature("Themis", Category.ANTI_CHEAT,
                    keys("themis"), keys("themis"), keys("themis"), keys("themis")),
            new Signature("Negativity", Category.ANTI_CHEAT,
                    keys("negativity"), keys("negativity"), keys("negativity"), keys("negativity")),
            new Signature("Kauri", Category.ANTI_CHEAT,
                    keys("kauri"), keys("kauri"), keys("kauri"), keys()),
            new Signature("OpenEye", Category.ANTI_CHEAT,
                    keys("openeye"), keys("openeye"), keys("openeye"), keys()),

            // -------------------------------------------------------- proxies
            new Signature("Velocity", Category.PROXY,
                    keys("velocity"), keys("velocity"), keys("velocity"), keys("velocity")),
            new Signature("BungeeCord", Category.PROXY,
                    keys("bungeecord"), keys("bungeecord"), keys("bungee", "bungeecord"), keys("bungee")),
            new Signature("Waterfall", Category.PROXY,
                    keys("waterfall"), keys("waterfall"), keys(), keys("waterfall")),

            // ----------------------------------------------- server software
            new Signature("Paper", Category.SERVER_SOFTWARE,
                    keys("paper"), keys("paper"), keys(), keys("paper")),
            new Signature("Purpur", Category.SERVER_SOFTWARE,
                    keys("purpur"), keys("purpur"), keys(), keys("purpur")),
            new Signature("Pufferfish", Category.SERVER_SOFTWARE,
                    keys("pufferfish"), keys("pufferfish"), keys(), keys()),
            new Signature("Folia", Category.SERVER_SOFTWARE,
                    keys("folia"), keys("folia"), keys(), keys()),
            new Signature("Leaf", Category.SERVER_SOFTWARE,
                    keys("leaf"), keys(), keys(), keys()),
            new Signature("Spigot", Category.SERVER_SOFTWARE,
                    keys("spigot"), keys(), keys(), keys("spigot")),
            new Signature("CraftBukkit", Category.SERVER_SOFTWARE,
                    keys("craftbukkit", "bukkit"), keys(), keys(), keys()),
            new Signature("Mohist", Category.SERVER_SOFTWARE,
                    keys("mohist"), keys(), keys(), keys()),
            new Signature("Arclight", Category.SERVER_SOFTWARE,
                    keys("arclight"), keys(), keys(), keys()),
            new Signature("Vanilla", Category.SERVER_SOFTWARE,
                    keys("vanilla"), keys(), keys(), keys()),
            new Signature("Fabric", Category.SERVER_SOFTWARE,
                    keys("fabric"), keys("fabric"), keys(), keys()),
            new Signature("Forge", Category.SERVER_SOFTWARE,
                    keys("forge"), keys("forge"), keys(), keys()),
            new Signature("NeoForge", Category.SERVER_SOFTWARE,
                    keys("neoforge"), keys(), keys(), keys()),
            new Signature("Quilt", Category.SERVER_SOFTWARE,
                    keys("quilt"), keys(), keys(), keys()),

            // -------------------------------------------------------- plugins
            new Signature("ViaVersion", Category.PLUGIN,
                    keys(), keys("viaversion", "vv"), keys("viaversion", "vv"), keys()),
            new Signature("ViaBackwards", Category.PLUGIN,
                    keys(), keys("viabackwards"), keys(), keys()),
            new Signature("GeyserMC", Category.PLUGIN,
                    keys("geyser"), keys("geyser"), keys("geyser"), keys()),
            new Signature("Floodgate", Category.PLUGIN,
                    keys(), keys("floodgate"), keys(), keys()),
            new Signature("LuckPerms", Category.PLUGIN,
                    keys(), keys("luckperms"), keys("luckperms"), keys()),
            new Signature("WorldEdit", Category.PLUGIN,
                    keys(), keys("worldedit"), keys("worldedit"), keys()),
            new Signature("PlaceholderAPI", Category.PLUGIN,
                    keys(), keys("placeholderapi"), keys("placeholderapi", "papi"), keys()),
            new Signature("ProtocolLib", Category.PLUGIN,
                    keys(), keys("protocollib"), keys(), keys()),
            new Signature("Essentials", Category.PLUGIN,
                    keys(), keys("essentials"), keys("essentials", "ess"), keys()),
            new Signature("Citizens", Category.PLUGIN,
                    keys(), keys("citizens"), keys("citizens", "npc"), keys()),
            new Signature("Multiverse", Category.PLUGIN,
                    keys(), keys("multiverse"), keys("multiverse", "mv"), keys()),
            new Signature("AuthMe", Category.PLUGIN,
                    keys(), keys("authme"), keys("authme"), keys()),
            new Signature("TAB", Category.PLUGIN,
                    keys(), keys("tab"), keys(), keys()),
            new Signature("Vault", Category.PLUGIN,
                    keys(), keys(), keys("vault"), keys()));
}
