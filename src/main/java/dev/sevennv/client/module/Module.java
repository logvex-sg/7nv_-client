package dev.sevennv.client.module;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.EnumSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.config.KeybindSetting;
import dev.sevennv.client.config.Setting;
import dev.sevennv.client.config.StringListSetting;
import dev.sevennv.client.config.StringSetting;
import dev.sevennv.client.utils.ChatUtils;
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
    private boolean alwaysOn;

    // Which hooks this module actually overrides. Detected once at construction
    // so the dispatcher can skip modules that would only run an empty method.
    private final boolean overridesTick;
    private final boolean overridesRender2D;
    private final boolean overridesWorldRender;
    private final boolean overridesPacket;
    private final boolean overridesChat;
    private final boolean overridesWorldChange;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        Class<?> type = getClass();
        this.overridesTick = overrides(type, "onTick");
        this.overridesRender2D = overrides(type, "onRender2D", net.minecraft.client.gui.GuiGraphics.class, float.class);
        this.overridesWorldRender = overrides(type, "onWorldRender");
        this.overridesPacket = overrides(type, "onPacket", dev.sevennv.client.events.PacketEvent.class);
        this.overridesChat = overrides(type, "onChat", dev.sevennv.client.events.ChatReceiveEvent.class);
        this.overridesWorldChange = overrides(type, "onWorldChange");
    }

    private static boolean overrides(Class<?> type, String method, Class<?>... params) {
        try {
            return type.getMethod(method, params).getDeclaringClass() != Module.class;
        } catch (NoSuchMethodException exception) {
            return false;
        }
    }

    /** True when this module implements the tick hook (so it is worth calling). */
    public boolean wantsTick() {
        return overridesTick;
    }

    public boolean wantsRender2D() {
        return overridesRender2D;
    }

    public boolean wantsWorldRender() {
        return overridesWorldRender;
    }

    public boolean wantsPackets() {
        return overridesPacket;
    }

    public boolean wantsChat() {
        return overridesChat;
    }

    public boolean wantsWorldChange() {
        return overridesWorldChange;
    }

    /**
     * Performance modules declare themselves always-on: they cannot be switched
     * off, they start enabled and they survive a disconnect. Only non-visual
     * optimisations should do this — anything that changes what you see stays a
     * normal, toggleable module.
     */
    protected void setAlwaysOn() {
        this.alwaysOn = true;
    }

    public boolean isAlwaysOn() {
        return alwaysOn;
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
        // An always-on module can never be switched off; a stray disable call
        // (disconnect sweep, config load) is ignored.
        if (alwaysOn) {
            state = true;
        }
        if (this.enabled == state) {
            return;
        }
        this.enabled = state;
        if (dev.sevennv.client.SevennvClient.getModuleManager() != null) {
            dev.sevennv.client.SevennvClient.getModuleManager().invalidate();
        }
        try {
            if (state) {
                onEnable();
            } else {
                onDisable();
            }
        } catch (Exception exception) {
            // A module throwing must never take the whole client down; log and carry on.
            dev.sevennv.client.SevennvClient.LOGGER.error("Module {} failed to {}", name, state ? "enable" : "disable", exception);
        }
        if (dev.sevennv.client.SevennvClient.getConfigManager() != null) {
            dev.sevennv.client.SevennvClient.getConfigManager().markDirty();
        }
    }

    public void toggle() {
        if (alwaysOn) {
            return;
        }
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
        if (dev.sevennv.client.SevennvClient.getModuleManager() != null) {
            dev.sevennv.client.SevennvClient.getModuleManager().invalidate();
        }
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
     * emitting gizmos (see {@link dev.sevennv.client.render.WorldRenderUtils}).
     */
    public void onWorldRender() {
    }

    /** Called for every packet in either direction while the module is enabled. */
    public void onPacket(dev.sevennv.client.events.PacketEvent event) {
    }

    /** Called for every incoming chat or system message while the module is enabled. */
    public void onChat(dev.sevennv.client.events.ChatReceiveEvent event) {
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
        return alwaysOn;
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

    // Suffix computation can be expensive (some modules count chunks), and the
    // array list asks for it on every frame. Caching it for a short interval
    // keeps the array list smooth without making the values feel stale.
    private static final long LABEL_TTL_MS = 250L;
    private String cachedLabel;
    private long cachedLabelAt = -LABEL_TTL_MS;

    public String getArraylistLabel() {
        long now = System.currentTimeMillis();
        if (cachedLabel != null && now - cachedLabelAt < LABEL_TTL_MS) {
            return cachedLabel;
        }
        String suffix = getArraylistSuffix();
        cachedLabel = suffix.isEmpty() ? name : name + " \u00a77" + suffix;
        cachedLabelAt = now;
        return cachedLabel;
    }
}
