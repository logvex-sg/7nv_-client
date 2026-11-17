package dev.logvex.client.world;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.hud.HudData;

/**
 * Adds the current biome to the array list and, optionally, a world-space label
 * under the player. The biome is read from the level at the player's position.
 */
public final class BiomeDisplay extends Module {
    public BiomeDisplay() {
        super("Biome Display", "Shows the biome you are standing in", Category.WORLD);
        worldLabel = bool("World label", "Draw the biome under your feet", false);
        color = color("Color", "Label colour", 0xFF90FF90);
    }

    private final BooleanSetting worldLabel;
    private final dev.logvex.client.config.ColorSetting color;

    @Override
    public void onWorldRender() {
        if (mc.player == null || !worldLabel.get()) {
            return;
        }
        dev.logvex.client.render.WorldRenderUtils.label(
                mc.player.position().add(0, -1.0, 0), HudData.biomeText(), color.get());
    }

    @Override
    public String getArraylistSuffix() {
        return HudData.biomeText();
    }
}
