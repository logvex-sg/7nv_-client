package dev.sevennv.client.command;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.config.ConfigManager;
import dev.sevennv.client.hud.HudElement;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.ChatUtils;
import dev.sevennv.client.utils.SocialManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Client-side dot commands, e.g. {@code .toggle sprint} or {@code .friend add}.
 * A leading dot keeps them out of the server's command namespace entirely.
 */
public final class CommandManager {
    public static final String PREFIX = ".";

    private final List<Command> commands = new ArrayList<>();

    public CommandManager() {
        commands.add(new Command("toggle", "toggle <module>", "Toggles a module", args -> {
            Module module = resolve(args);
            if (module == null) {
                return "Unknown module";
            }
            module.toggle();
            return null;
        }));
        commands.add(new Command("bind", "bind <module>", "Binds a module to the next key you press", args -> {
            Module module = resolve(args);
            if (module == null) {
                return "Unknown module";
            }
            PendingBind.set(module);
            return "Press a key to bind " + module.getName();
        }));
        commands.add(new Command("friend", "friend <add|remove|list> [name]", "Manages the friend list", this::friends));
        commands.add(new Command("enemy", "enemy <add|remove|list> [name]", "Manages the enemy list", this::enemies));
        commands.add(new Command("config", "config <save|reload|profile <name>>", "Config control", this::config));
        commands.add(new Command("hud", "hud <element|editor|on|off>", "Toggles a HUD element, or opens the layout editor", args -> {
            if (args.length == 0) {
                return "Usage: hud <element|editor|on|off>";
            }
            String action = args[0].toLowerCase(Locale.ROOT);
            if (action.equals("editor")) {
                net.minecraft.client.Minecraft.getInstance().setScreen(new dev.sevennv.client.gui.HudEditorScreen());
                return null;
            }
            if (action.equals("on") || action.equals("off")) {
                boolean on = action.equals("on");
                SevennvClient.getHudManager().setAllEnabled(on);
                SevennvClient.getConfigManager().markDirty();
                return "All HUD elements " + (on ? "ON" : "OFF");
            }
            HudElement element = SevennvClient.getHudManager().getById(action);
            if (element == null) {
                return "Unknown HUD element: " + args[0];
            }
            element.setEnabled(!element.isEnabled());
            SevennvClient.getConfigManager().markDirty();
            return element.getName() + " " + (element.isEnabled() ? "ON" : "OFF");
        }));
        commands.add(new Command("waypoint", "waypoint <add|remove|list|clear> [name] [x y z]",
                "Manages stored waypoints", this::waypoints));
        commands.add(new Command("marker", "marker <name|remove <name>|clear>",
                "Places a temporary map marker", this::markers));
        commands.add(new Command("screenshot", "screenshot", "Captures a screenshot with the configured name", args -> {
            var manager = SevennvClient.getModuleManager().get(dev.sevennv.client.misc.ScreenshotManager.class);
            if (manager == null) {
                return "Screenshot Manager is not registered";
            }
            manager.capture();
            return null;
        }));
        commands.add(new Command("effects", "effects", "Lists your active status effects", args -> {
            var manager = SevennvClient.getModuleManager().get(dev.sevennv.client.player.StatusEffects.class);
            if (manager == null) {
                return "Status Effects is not registered";
            }
            String summary = manager.summary();
            return summary.isEmpty() ? "No active effects" : "Effects: " + summary;
        }));
        commands.add(new Command("help", "help", "Lists client commands", args -> {
            StringBuilder builder = new StringBuilder("Commands: ");
            for (Command command : commands) {
                builder.append(PREFIX).append(command.name).append("  ");
            }
            return builder.toString();
        }));
    }

    private Module resolve(String[] args) {
        if (args.length == 0) {
            return null;
        }
        return SevennvClient.getModuleManager().getByName(String.join(" ", args));
    }

    private String friends(String[] args) {
        return social(args, true);
    }

    private String enemies(String[] args) {
        return social(args, false);
    }

    private String social(String[] args, boolean friend) {
        if (args.length == 0) {
            return "Usage: " + (friend ? "friend" : "enemy") + " <add|remove|list> [name]";
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        if (action.equals("list")) {
            var set = friend ? SocialManager.getFriends() : SocialManager.getEnemies();
            return (friend ? "Friends" : "Enemies") + ": " + (set.isEmpty() ? "none" : String.join(", ", set));
        }
        if (args.length < 2) {
            return "Missing name";
        }
        String name = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        switch (action) {
            case "add" -> {
                if (friend) {
                    SocialManager.addFriend(name);
                } else {
                    SocialManager.addEnemy(name);
                }
                SevennvClient.getConfigManager().markDirty();
                return "Added " + name;
            }
            case "remove" -> {
                SocialManager.remove(name);
                SevennvClient.getConfigManager().markDirty();
                return "Removed " + name;
            }
            default -> {
                return "Unknown action: " + action;
            }
        }
    }

    private String config(String[] args) {
        ConfigManager config = SevennvClient.getConfigManager();
        if (args.length == 0) {
            return "Usage: config <save|reload|profile <name>|list|create <name>|delete <name>|export <name>|import <name>|backup|restore>";
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "save" -> {
                config.save();
                return "Config saved";
            }
            case "reload" -> {
                config.load();
                return "Config reloaded";
            }
            case "profile" -> {
                if (args.length < 2) {
                    return "Current profile: " + config.getProfileName();
                }
                config.switchProfile(args[1]);
                return "Switched to profile " + args[1];
            }
            case "list" -> {
                java.util.List<String> profiles = config.listProfiles();
                return "Profiles: " + (profiles.isEmpty() ? "none" : String.join(", ", profiles));
            }
            case "create" -> {
                if (args.length < 2) {
                    return "Usage: config create <name>";
                }
                config.createProfile(args[1]);
                return "Created profile " + args[1];
            }
            case "delete" -> {
                if (args.length < 2) {
                    return "Usage: config delete <name>";
                }
                if ("default".equalsIgnoreCase(args[1])) {
                    return "The default profile cannot be deleted";
                }
                config.deleteProfile(args[1]);
                return "Deleted profile " + args[1];
            }
            case "export" -> {
                if (args.length < 2) {
                    return "Usage: config export <file>";
                }
                return "Exported to " + config.exportProfile(args[1]).getFileName();
            }
            case "import" -> {
                if (args.length < 2) {
                    return "Usage: config import <file>";
                }
                return config.importProfile(args[1])
                        ? "Imported " + args[1]
                        : "No such export: " + args[1];
            }
            case "backup" -> {
                return "Backup written: " + config.backup("manual").getFileName();
            }
            case "restore" -> {
                return config.restoreLatestBackup() ? "Restored the newest backup" : "No backup found";
            }
            case "reset" -> {
                SevennvClient.getModuleManager().disableAll();
                SevennvClient.getHudManager().setAllEnabled(false);
                config.markDirty();
                config.save();
                return "Reset: all modules and HUD elements disabled";
            }
            default -> {
                return "Unknown config action";
            }
        }
    }

    private String waypoints(String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            var all = dev.sevennv.client.world.WaypointManager.all();
            if (all.isEmpty()) {
                return "No waypoints";
            }
            StringBuilder builder = new StringBuilder("Waypoints: ");
            for (var waypoint : all) {
                builder.append(waypoint.getName()).append(" (").append(waypoint.getX()).append(' ')
                        .append(waypoint.getY()).append(' ').append(waypoint.getZ()).append(") ");
            }
            return builder.toString().trim();
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        switch (action) {
            case "clear" -> {
                dev.sevennv.client.world.WaypointManager.clear();
                SevennvClient.getConfigManager().markDirty();
                return "Cleared all waypoints";
            }
            case "remove" -> {
                if (args.length < 2) {
                    return "Usage: waypoint remove <name>";
                }
                boolean removed = dev.sevennv.client.world.WaypointManager.remove(String.join(" ",
                        java.util.Arrays.copyOfRange(args, 1, args.length)));
                if (removed) {
                    SevennvClient.getConfigManager().markDirty();
                }
                return removed ? "Removed waypoint" : "No such waypoint";
            }
            case "add" -> {
                var player = net.minecraft.client.Minecraft.getInstance().player;
                if (player == null) {
                    return "Not in a world";
                }
                String dimension = net.minecraft.client.Minecraft.getInstance().level == null
                        ? "overworld"
                        : net.minecraft.client.Minecraft.getInstance().level.dimension().identifier().getPath();
                int x;
                int y;
                int z;
                String name;
                if (args.length >= 5) {
                    try {
                        x = (int) Double.parseDouble(args[args.length - 3]);
                        y = (int) Double.parseDouble(args[args.length - 2]);
                        z = (int) Double.parseDouble(args[args.length - 1]);
                        name = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length - 3));
                    } catch (NumberFormatException exception) {
                        return "Coordinates must be numbers: waypoint add <name> <x y z>";
                    }
                } else {
                    name = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                    x = player.blockPosition().getX();
                    y = player.blockPosition().getY();
                    z = player.blockPosition().getZ();
                }
                if (name.isBlank()) {
                    return "Usage: waypoint add <name> [x y z]";
                }
                dev.sevennv.client.world.WaypointManager.add(name, "default",
                        dev.sevennv.client.world.WaypointManager.Kind.WAYPOINT, x, y, z, dimension);
                SevennvClient.getConfigManager().markDirty();
                return "Added waypoint " + name + " at " + x + " " + y + " " + z;
            }
            default -> {
                return "Unknown waypoint action";
            }
        }
    }

    private String markers(String[] args) {
        var markers = SevennvClient.getModuleManager().get(dev.sevennv.client.world.MapMarkers.class);
        if (markers == null) {
            return "Map Markers is not registered";
        }
        var player = net.minecraft.client.Minecraft.getInstance().player;
        if (args.length == 0) {
            return "Usage: marker <name|remove <name>|clear>";
        }
        if (args[0].equalsIgnoreCase("clear")) {
            markers.clearMarkers();
            return "Cleared all markers";
        }
        if (args[0].equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                return "Usage: marker remove <name>";
            }
            return markers.removeMarker(String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)))
                    ? "Removed marker" : "No such marker";
        }
        if (player == null) {
            return "Not in a world";
        }
        String name = String.join(" ", args);
        markers.addMarker(name, player.position());
        return "Placed marker " + name;
    }

    /** Handles a raw chat line. Returns true when it was a client command. */
    public boolean handle(String rawLine) {
        if (!rawLine.startsWith(PREFIX)) {
            return false;
        }
        String body = rawLine.substring(PREFIX.length()).trim();
        if (body.isEmpty()) {
            return false;
        }
        String[] parts = body.split("\\s+");
        String name = parts[0].toLowerCase(Locale.ROOT);
        String[] args = java.util.Arrays.copyOfRange(parts, 1, parts.length);
        for (Command command : commands) {
            if (command.name.equals(name)) {
                String result;
                try {
                    result = command.handler.run(args);
                } catch (Exception exception) {
                    SevennvClient.LOGGER.error("Command {} failed", name, exception);
                    ChatUtils.error("Command failed: " + exception.getMessage());
                    return true;
                }
                if (result != null) {
                    ChatUtils.info(result);
                }
                return true;
            }
        }
        ChatUtils.error("Unknown command: " + name + " (try " + PREFIX + "help)");
        return true;
    }

    public List<Command> getCommands() {
        return commands;
    }

    private record Command(String name, String usage, String description, Handler handler) {
    }

    @FunctionalInterface
    private interface Handler {
        String run(String[] args);
    }
}
