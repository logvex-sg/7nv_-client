package dev.logvex.client.command;

import com.mojang.blaze3d.platform.InputConstants;
import dev.logvex.client.module.Module;

/**
 * Holds the module awaiting a keybind from the {@code .bind} command or the
 * ClickGUI keybind editor. The key mixin consumes it on the next key press.
 */
public final class PendingBind {
    private static Module module;

    private PendingBind() {
    }

    public static void set(Module target) {
        module = target;
    }

    public static boolean isActive() {
        return module != null;
    }

    public static Module get() {
        return module;
    }

    /** Applies the pressed key and clears the pending state. */
    public static boolean apply(InputConstants.Key key) {
        if (module == null) {
            return false;
        }
        module.setKeybind(key == InputConstants.UNKNOWN ? InputConstants.UNKNOWN : key);
        module = null;
        return true;
    }

    public static void clear() {
        module = null;
    }
}
