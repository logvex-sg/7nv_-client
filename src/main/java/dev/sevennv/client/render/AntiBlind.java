package dev.sevennv.client.render;

import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;

/**
 * Removes the darkness and blindness screen overlay. The effect itself is still
 * active on the server; only the client's overlay is hidden.
 */
public final class AntiBlind extends Module {
    public AntiBlind() {
        super("Anti Blind", "Removes the blindness and darkness overlay", Category.RENDER);
    }
}
