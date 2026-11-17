package dev.logvex.client.module;

import com.mojang.blaze3d.platform.InputConstants;
import dev.logvex.client.gui.ClickGuiScreen;
import dev.logvex.client.utils.InputUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Central keybind handling: owns the ClickGUI key and polls every module
 * keybind once per client tick.
 *
 * <p>Binds are read straight from GLFW rather than through {@code KeyMapping}
 * so that binding a module to E or R does not unbind the vanilla action.
 */
public final class KeybindManager {
    private final ModuleManager modules;
    private InputConstants.Key guiKey = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_RIGHT_SHIFT);
    private InputConstants.Key hudEditorKey = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_RIGHT_CONTROL);

    public KeybindManager(ModuleManager modules) {
        this.modules = modules;
    }

    public InputConstants.Key getGuiKey() {
        return guiKey;
    }

    public void setGuiKey(InputConstants.Key key) {
        this.guiKey = key == null ? InputConstants.UNKNOWN : key;
    }

    public InputConstants.Key getHudEditorKey() {
        return hudEditorKey;
    }

    public void setHudEditorKey(InputConstants.Key key) {
        this.hudEditorKey = key == null ? InputConstants.UNKNOWN : key;
    }

    /** Polled every client tick. */
    public void poll() {
        Minecraft mc = Minecraft.getInstance();
        // While a screen is open our raw polling would fire on menu keys; skip it.
        if (mc.screen != null) {
            InputUtils.reset();
            return;
        }
        if (guiKey != InputConstants.UNKNOWN && InputUtils.consumePress(guiKey)) {
            mc.setScreen(new ClickGuiScreen());
            return;
        }
        if (hudEditorKey != InputConstants.UNKNOWN && InputUtils.consumePress(hudEditorKey)) {
            mc.setScreen(new dev.logvex.client.gui.HudEditorScreen());
            return;
        }
        modules.pollKeybinds();
    }

    public void reset() {
        InputUtils.reset();
    }
}
