package dev.logvex.client.command;

import dev.logvex.client.LogvexClient;
import dev.logvex.client.config.ConfigManager;
import dev.logvex.client.hud.HudElement;
import dev.logvex.client.module.Module;
import dev.logvex.client.utils.ChatUtils;
import dev.logvex.client.utils.SocialManager;

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
        commands.add(new Command("hud", "hud <element|editor>", "Toggles a HUD element, or opens the layout editor", args -> {
            if (args.length == 0) {
                return "Usage: hud <element|editor>";
            }
            if (args[0].equalsIgnoreCase("editor")) {
                net.minecraft.client.Minecraft.getInstance().setScreen(new dev.logvex.client.gui.HudEditorScreen());
                return null;
            }
            HudElement element = LogvexClient.getHudManager().getById(args[0].toLowerCase(Locale.ROOT));
            if (element == null) {
                return "Unknown HUD element: " + args[0];
            }
            element.setEnabled(!element.isEnabled());
            LogvexClient.getConfigManager().markDirty();
            return element.getName() + " " + (element.isEnabled() ? "ON" : "OFF");
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
        return LogvexClient.getModuleManager().getByName(String.join(" ", args));
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
                LogvexClient.getConfigManager().markDirty();
                return "Added " + name;
            }
            case "remove" -> {
                SocialManager.remove(name);
                LogvexClient.getConfigManager().markDirty();
                return "Removed " + name;
            }
            default -> {
                return "Unknown action: " + action;
            }
        }
    }

    private String config(String[] args) {
        ConfigManager config = LogvexClient.getConfigManager();
        if (args.length == 0) {
            return "Usage: config <save|reload|profile <name>>";
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
            default -> {
                return "Unknown config action";
            }
        }
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
                    LogvexClient.LOGGER.error("Command {} failed", name, exception);
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
