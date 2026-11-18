package dev.sevennv.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.hud.HudManager;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.module.ModuleManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Owns everything under {@code .minecraft/config/sevennv/}.
 *
 * <p>Layout: {@code config.json} holds global settings (theme, GUI layout, HUD
 * layout, extras); {@code profiles/<name>.json} holds per-profile module
 * states, settings and keybinds; {@code backups/} holds timestamped copies.
 */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final Path root;
    private final Path profilesDir;
    private final Path backupsDir;
    private final ModuleManager modules;
    private final HudManager hud;
    private final Theme theme = new Theme();

    private String profileName = "default";
    private double guiScaleFactor = 1.0;
    private int guiOpacity = 255;
    private boolean arraylistEnabled = true;
    private int arraylistX = 2;
    private int arraylistY = 2;
    private boolean arraylistRainbow = false;
    private boolean arraylistSuffix = true;
    private boolean dirty;
    private long lastSave;
    private JsonObject guiLayout = new JsonObject();
    private JsonObject extra = new JsonObject();

    public ConfigManager(ModuleManager modules, HudManager hud) {
        this.modules = modules;
        this.hud = hud;
        Path base = FabricLoader.getInstance().getConfigDir().resolve(SevennvClient.MOD_ID);
        this.root = base;
        this.profilesDir = base.resolve("profiles");
        this.backupsDir = base.resolve("backups");
        try {
            Files.createDirectories(profilesDir);
            Files.createDirectories(backupsDir);
        } catch (IOException exception) {
            SevennvClient.LOGGER.error("Could not create the 7nv_ config directory", exception);
        }
    }

    public Theme getTheme() {
        return theme;
    }

    private String themePreset = Theme.PRESETS[0];

    public String getThemePreset() {
        return themePreset;
    }

    /** Replaces the active palette with the named preset and persists it. */
    public void applyTheme(String preset) {
        if (preset == null) {
            return;
        }
        themePreset = preset;
        theme.copyFrom(Theme.preset(preset));
        markDirty();
    }

    public Path getRoot() {
        return root;
    }

    public String getProfileName() {
        return profileName;
    }

    /** ClickGUI scale multiplier, clamped to a usable 0.5x - 2.0x range. */
    public double getGuiScaleFactor() {
        return guiScaleFactor;
    }

    public void setGuiScaleFactor(double factor) {
        this.guiScaleFactor = Math.max(0.5, Math.min(2.0, Math.round(factor * 4.0) / 4.0));
        markDirty();
    }

    public int getGuiOpacity() {
        return guiOpacity;
    }

    public void setGuiOpacity(int guiOpacity) {
        this.guiOpacity = Math.max(64, Math.min(255, guiOpacity));
        markDirty();
    }

    public boolean isArraylistEnabled() {
        return arraylistEnabled;
    }

    public void setArraylistEnabled(boolean value) {
        this.arraylistEnabled = value;
        markDirty();
    }

    public int getArraylistX() {
        return arraylistX;
    }

    public int getArraylistY() {
        return arraylistY;
    }

    public void setArraylistPosition(int x, int y) {
        this.arraylistX = x;
        this.arraylistY = y;
        markDirty();
    }

    public boolean isArraylistRainbow() {
        return arraylistRainbow;
    }

    public void setArraylistRainbow(boolean value) {
        this.arraylistRainbow = value;
        markDirty();
    }

    public boolean isArraylistSuffix() {
        return arraylistSuffix;
    }

    public void setArraylistSuffix(boolean value) {
        this.arraylistSuffix = value;
        markDirty();
    }

    /** Free-form section other managers may persist into (macros, waypoints, ...). */
    public JsonObject getExtra() {
        return extra;
    }

    public void setExtra(JsonObject extra) {
        this.extra = extra == null ? new JsonObject() : extra;
    }

    public void markDirty() {
        this.dirty = true;
    }

    // ------------------------------------------------------------------ load

    public void load() {
        loadGlobal();
        loadProfile(profileName);
        SevennvClient.LOGGER.info("Loaded 7nv_ config (profile '{}')", profileName);
    }

    private void loadGlobal() {
        Path file = root.resolve("config.json");
        if (!Files.exists(file)) {
            saveGlobal();
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
            profileName = string(object, "profile", "default");
            themePreset = string(object, "themePreset", themePreset);
            guiScaleFactor = number(object, "guiScaleFactor", guiScaleFactor);
            guiOpacity = integer(object, "guiOpacity", guiOpacity);
            arraylistEnabled = bool(object, "arraylistEnabled", true);
            arraylistX = integer(object, "arraylistX", 2);
            arraylistY = integer(object, "arraylistY", 2);
            arraylistRainbow = bool(object, "arraylistRainbow", false);
            arraylistSuffix = bool(object, "arraylistSuffix", true);
            if (object.has("theme") && object.get("theme").isJsonObject()) {
                theme.fromJson(object.getAsJsonObject("theme"));
            }
            if (object.has("guiLayout") && object.get("guiLayout").isJsonObject()) {
                guiLayout = object.getAsJsonObject("guiLayout");
            }
            if (object.has("hud") && object.get("hud").isJsonObject() && hud != null) {
                hud.fromJson(object.getAsJsonObject("hud"));
            }
            if (object.has("extra") && object.get("extra").isJsonObject()) {
                extra = object.getAsJsonObject("extra");
            }
            if (object.has("social") && object.get("social").isJsonObject()) {
                dev.sevennv.client.utils.FriendManager.fromJson(object.getAsJsonObject("social"));
            }
            if (object.has("waypoints") && object.get("waypoints").isJsonArray()) {
                dev.sevennv.client.world.WaypointManager.fromJson(object.getAsJsonArray("waypoints"));
            }
            if (object.has("notifications") && object.get("notifications").isJsonObject()) {
                dev.sevennv.client.utils.NotificationManager.fromJson(object.getAsJsonObject("notifications"));
            }
        } catch (Exception exception) {
            SevennvClient.LOGGER.error("Failed to read config.json, using defaults", exception);
        }
    }

    public void loadProfile(String name) {
        profileName = name;
        Path file = profilesDir.resolve(name + ".json");
        if (!Files.exists(file)) {
            saveProfile();
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.has("modules") && root.get("modules").isJsonObject()) {
                JsonObject moduleObjects = root.getAsJsonObject("modules");
                for (Module module : modules.getAll()) {
                    JsonElement element = moduleObjects.get(module.getName());
                    if (element == null || !element.isJsonObject()) {
                        continue;
                    }
                    JsonObject entry = element.getAsJsonObject();
                    // Settings are applied before toggling so an enabled module starts configured.
                    if (entry.has("settings") && entry.get("settings").isJsonObject()) {
                        JsonObject settings = entry.getAsJsonObject("settings");
                        for (Setting<?> setting : module.getSettings()) {
                            if (settings.has(setting.getName())) {
                                try {
                                    setting.fromJson(settings.get(setting.getName()));
                                } catch (Exception exception) {
                                    SevennvClient.LOGGER.warn("Bad value for {}.{}", module.getName(), setting.getName());
                                }
                            }
                        }
                    }
                    module.setVisibleInArraylist(bool(entry, "visible", true));
                    if (entry.has("keybind")) {
                        module.setKeybind(readKey(entry.get("keybind")));
                    }
                    module.setEnabled(bool(entry, "enabled", false));
                }
            }
        } catch (Exception exception) {
            SevennvClient.LOGGER.error("Failed to read profile '{}'", name, exception);
        }
    }

    // ------------------------------------------------------------------ save

    public void saveIfDirty() {
        if (!dirty) {
            return;
        }
        // Coalesce bursts of changes (dragging a slider) into one write per 2 s.
        long now = System.currentTimeMillis();
        if (now - lastSave < 2000L) {
            return;
        }
        save();
    }

    public void save() {
        dirty = false;
        lastSave = System.currentTimeMillis();
        saveGlobal();
        saveProfile();
    }

    private void saveGlobal() {
        JsonObject object = new JsonObject();
        object.addProperty("profile", profileName);
        object.addProperty("themePreset", themePreset);
        object.addProperty("guiScaleFactor", guiScaleFactor);
        object.addProperty("guiOpacity", guiOpacity);
        object.addProperty("arraylistEnabled", arraylistEnabled);
        object.addProperty("arraylistX", arraylistX);
        object.addProperty("arraylistY", arraylistY);
        object.addProperty("arraylistRainbow", arraylistRainbow);
        object.addProperty("arraylistSuffix", arraylistSuffix);
        object.add("theme", theme.toJson());
        object.add("guiLayout", guiLayout);
        if (hud != null) {
            object.add("hud", hud.toJson());
        }
        object.add("extra", extra);
        object.add("social", dev.sevennv.client.utils.FriendManager.toJson());
        object.add("waypoints", dev.sevennv.client.world.WaypointManager.toJson());
        object.add("notifications", dev.sevennv.client.utils.NotificationManager.toJson());
        write(root.resolve("config.json"), object);
    }

    private void saveProfile() {
        JsonObject root = new JsonObject();
        root.addProperty("name", profileName);
        JsonObject moduleObjects = new JsonObject();
        for (Module module : modules.getAll()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("enabled", module.isEnabled());
            entry.addProperty("visible", module.isVisibleInArraylist());
            InputConstants.Key bind = module.getKeybind();
            bind = bind == null ? InputConstants.UNKNOWN : bind;
            JsonObject key = new JsonObject();
            key.addProperty("type", bind.getType().name());
            key.addProperty("code", bind.getValue());
            entry.add("keybind", key);
            JsonObject settings = new JsonObject();
            for (Setting<?> setting : module.getSettings()) {
                try {
                    settings.add(setting.getName(), setting.toJson());
                } catch (Exception exception) {
                    SevennvClient.LOGGER.warn("Could not serialise {}.{}", module.getName(), setting.getName());
                }
            }
            entry.add("settings", settings);
            moduleObjects.add(module.getName(), entry);
        }
        root.add("modules", moduleObjects);
        write(profilesDir.resolve(profileName + ".json"), root);
    }

    // ------------------------------------------------------------------ profiles

    public List<String> listProfiles() {
        List<String> names = new ArrayList<>();
        try (Stream<Path> stream = Files.list(profilesDir)) {
            stream.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> {
                        String name = path.getFileName().toString();
                        names.add(name.substring(0, name.length() - 5));
                    });
        } catch (IOException exception) {
            SevennvClient.LOGGER.error("Could not list profiles", exception);
        }
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    /** Switches profile, saving the current one first. */
    public void switchProfile(String name) {
        save();
        backup("pre-switch-" + name);
        loadProfile(name);
        saveGlobal();
    }

    public void createProfile(String name) {
        save();
        Path file = profilesDir.resolve(name + ".json");
        if (Files.exists(file)) {
            return;
        }
        profileName = name;
        saveProfile();
        saveGlobal();
    }

    public void deleteProfile(String name) {
        if ("default".equals(name)) {
            return;
        }
        try {
            Files.deleteIfExists(profilesDir.resolve(name + ".json"));
        } catch (IOException exception) {
            SevennvClient.LOGGER.error("Could not delete profile '{}'", name, exception);
        }
    }

    // ------------------------------------------------------------------ import / export / backup

    public Path exportProfile(String targetName) {
        Path target = root.resolve(targetName + ".json");
        write(target, readJson(profilesDir.resolve(profileName + ".json")));
        return target;
    }

    public boolean importProfile(String sourceName) {
        Path source = root.resolve(sourceName + ".json");
        if (!Files.exists(source)) {
            return false;
        }
        JsonObject object = readJson(source);
        if (object == null) {
            return false;
        }
        write(profilesDir.resolve(sourceName + ".json"), object);
        loadProfile(sourceName);
        saveGlobal();
        return true;
    }

    public Path backup(String reason) {
        save();
        String stamp = LocalDateTime.now().format(STAMP);
        Path target = backupsDir.resolve(reason + "-" + stamp + ".json");
        JsonObject bundle = new JsonObject();
        bundle.add("config", readJson(root.resolve("config.json")));
        bundle.add("profile", readJson(profilesDir.resolve(profileName + ".json")));
        write(target, bundle);
        return target;
    }

    public List<Path> listBackups() {
        List<Path> result = new ArrayList<>();
        try (Stream<Path> stream = Files.list(backupsDir)) {
            stream.filter(path -> path.getFileName().toString().endsWith(".json")).forEach(result::add);
        } catch (IOException exception) {
            SevennvClient.LOGGER.error("Could not list backups", exception);
        }
        result.sort(Path::compareTo);
        return result;
    }

    /** Restores the newest backup, keeping a copy of the current state first. */
    public boolean restoreLatestBackup() {
        List<Path> backups = listBackups();
        if (backups.isEmpty()) {
            return false;
        }
        JsonObject bundle = readJson(backups.get(backups.size() - 1));
        if (bundle == null) {
            return false;
        }
        try {
            Files.copy(root.resolve("config.json"),
                    backupsDir.resolve("pre-restore-" + LocalDateTime.now().format(STAMP) + ".json"),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            // A missing config.json just means there is nothing to preserve.
        }
        if (bundle.has("config") && bundle.get("config").isJsonObject()) {
            write(root.resolve("config.json"), bundle.getAsJsonObject("config"));
        }
        if (bundle.has("profile") && bundle.get("profile").isJsonObject()) {
            write(profilesDir.resolve(profileName + ".json"), bundle.getAsJsonObject("profile"));
        }
        load();
        return true;
    }

    /** Persists a GUI window position. */
    public void setWindowPosition(String id, int x, int y) {
        JsonObject entry = guiLayout.has(id) && guiLayout.get(id).isJsonObject()
                ? guiLayout.getAsJsonObject(id) : new JsonObject();
        entry.addProperty("x", x);
        entry.addProperty("y", y);
        guiLayout.add(id, entry);
        markDirty();
    }

    public int[] getWindowPosition(String id, int defaultX, int defaultY) {
        if (guiLayout.has(id) && guiLayout.get(id).isJsonObject()) {
            JsonObject entry = guiLayout.getAsJsonObject(id);
            return new int[]{integer(entry, "x", defaultX), integer(entry, "y", defaultY)};
        }
        return new int[]{defaultX, defaultY};
    }

    // ------------------------------------------------------------------ io

    private JsonObject readJson(Path path) {
        if (!Files.exists(path)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception exception) {
            SevennvClient.LOGGER.error("Could not read {}", path, exception);
            return null;
        }
    }

    private void write(Path path, JsonObject object) {
        if (object == null) {
            return;
        }
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(object, writer);
            }
        } catch (IOException exception) {
            SevennvClient.LOGGER.error("Could not write {}", path, exception);
        }
    }

    private static InputConstants.Key readKey(JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            return InputConstants.UNKNOWN;
        }
        JsonObject object = element.getAsJsonObject();
        try {
            InputConstants.Type type = InputConstants.Type.valueOf(object.get("type").getAsString());
            return type.getOrCreate(object.get("code").getAsInt());
        } catch (Exception exception) {
            return InputConstants.UNKNOWN;
        }
    }

    private static boolean bool(JsonObject object, String key, boolean fallback) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsBoolean() : fallback;
    }

    private static double number(JsonObject object, String key, double fallback) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsDouble() : fallback;
    }

    private static int integer(JsonObject object, String key, int fallback) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsInt() : fallback;
    }

    private static String string(JsonObject object, String key, String fallback) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : fallback;
    }
}
