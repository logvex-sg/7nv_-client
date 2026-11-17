package dev.logvex.client.utils;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/**
 * Raw input helpers.
 *
 * <p>Module keybinds are polled with our own edge detection instead of
 * {@link net.minecraft.client.KeyMapping}: {@code KeyMapping.setKey} rewrites
 * the global key-to-mapping table, so reusing one mapping for every module
 * would steal vanilla binds (E, R, ...) whenever a module was bound to them.
 */
public final class InputUtils {
    private static final Map<InputConstants.Key, Boolean> PRESSED = new HashMap<>();

    private InputUtils() {
    }

    /** True while the given raw key or mouse button is held. */
    public static boolean isDown(InputConstants.Key key) {
        if (key == null || key == InputConstants.UNKNOWN) {
            return false;
        }
        long handle = Minecraft.getInstance().getWindow().handle();
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(handle, key.getValue()) == GLFW.GLFW_PRESS;
        }
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), key.getValue());
    }

    /**
     * True exactly once per physical press of {@code key}. Must be polled every
     * client tick for the state to stay accurate.
     */
    public static boolean consumePress(InputConstants.Key key) {
        if (key == null || key == InputConstants.UNKNOWN) {
            return false;
        }
        boolean down = isDown(key);
        Boolean previous = PRESSED.put(key, down);
        return down && (previous == null || !previous);
    }

    /** Drops all remembered key state, e.g. when the game loses focus. */
    public static void reset() {
        PRESSED.clear();
    }
}
