package dev.logvex.client.module;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.ColorSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.config.EnumSetting;
import dev.logvex.client.config.IntSetting;
import dev.logvex.client.config.KeybindSetting;
import dev.logvex.client.config.Setting;
import dev.logvex.client.config.StringListSetting;
import dev.logvex.client.config.StringSetting;
import dev.logvex.client.utils.ChatUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for every feature. Modules are self-contained: they own their
 * settings, their own enable/disable state and their tick/render hooks.
 */
public abstract class Module {
    protected static final Minecraft mc = Minecraft.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();

    private boolean enabled;
    private boolean visibleInArraylist = true;
    private InputConstants.Key keybind = InputConstants.UNKNOWN;
    private boolean expanded;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean state) {
        if (this.enabled == state) {
            return;
        }
        this.enabled = state;
        try {
            if (state) {
                onEnable();
            } else {
                onDisable();
            }
        } catch (Exception exception) {
            // A module throwing must never take the whole client down; log and carry on.
            dev.logvex.client.LogvexClient.LOGGER.error("Module {} failed to {}", name, state ? "enable" : "disable", exception);
        }
        if (dev.logvex.client.LogvexClient.getConfigManager() != null) {
            dev.logvex.client.LogvexClient.getConfigManager().markDirty();
        }
    }

    public void toggle() {
        setEnabled(!enabled);
        ChatUtils.moduleState(this);
    }

    public boolean isVisibleInArraylist() {
        return visibleInArraylist;
    }

    public void setVisibleInArraylist(boolean visible) {
        this.visibleInArraylist = visible;
    }

    public InputConstants.Key getKeybind() {
        return keybind;
    }

    public void setKeybind(InputConstants.Key key) {
        this.keybind = key == null ? InputConstants.UNKNOWN : key;
    }

    public boolean isBound() {
        return keybind != null && keybind != InputConstants.UNKNOWN;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    /** Called every client tick while the module is enabled. */
    public void onTick() {
    }

    /** Called once per frame while the module is enabled. GUI-space drawing. */
    public void onRender2D(net.minecraft.client.gui.GuiGraphics graphics, float partialTick) {
    }

    /**
     * Called once per frame inside the world render callback. Only valid for
     * emitting gizmos (see {@link dev.logvex.client.render.WorldRenderUtils}).
     */
    public void onWorldRender() {
    }

    /** Called for every packet in either direction while the module is enabled. */
    public void onPacket(dev.logvex.client.events.PacketEvent event) {
    }

    /** Called for every incoming chat or system message while the module is enabled. */
    public void onChat(dev.logvex.client.events.ChatReceiveEvent event) {
    }

    /** Called when the module is switched on. */
    public void onEnable() {
    }

    /** Called when the module is switched off. */
    public void onDisable() {
    }

    /** Called when the player disconnects or changes dimension. */
    public void onWorldChange() {
    }

    /**
     * Whether this module keeps running across a disconnect. Reconnect-style
     * modules override this; everything else is switched off when the player
     * leaves a world.
     */
    public boolean survivesDisconnect() {
        return false;
    }

    protected <S extends Setting<?>> S register(S setting) {
        settings.add(setting);
        return setting;
    }

    protected BooleanSetting bool(String name, String description, boolean defaultValue) {
        return register(new BooleanSetting(name, description, defaultValue));
    }

    protected DoubleSetting number(String name, String description, double defaultValue, double min, double max, double step) {
        return register(new DoubleSetting(name, description, defaultValue, min, max, step));
    }

    protected DoubleSetting number(String name, String description, double defaultValue, double min, double max, double step, String suffix) {
        return register(new DoubleSetting(name, description, defaultValue, min, max, step, suffix));
    }

    protected IntSetting integer(String name, String description, int defaultValue, int min, int max) {
        return register(new IntSetting(name, description, defaultValue, min, max));
    }

    protected <E extends Enum<E>> EnumSetting<E> option(String name, String description, E defaultValue) {
        return register(new EnumSetting<>(name, description, defaultValue));
    }

    protected StringSetting text(String name, String description, String defaultValue) {
        return register(new StringSetting(name, description, defaultValue));
    }

    protected StringListSetting list(String name, String description, String... defaultValue) {
        return register(new StringListSetting(name, description, defaultValue));
    }

    protected ColorSetting color(String name, String description, int defaultValue) {
        return register(new ColorSetting(name, description, defaultValue));
    }

    protected KeybindSetting keybindSetting(String name, String description) {
        return register(new KeybindSetting(name, description, InputConstants.UNKNOWN));
    }

    /** Human-readable summary shown in the array list suffix, may be empty. */
    public String getArraylistSuffix() {
        return "";
    }

    public String getArraylistLabel() {
        String suffix = getArraylistSuffix();
        return suffix.isEmpty() ? name : name + " \u00a77" + suffix;
    }
}
