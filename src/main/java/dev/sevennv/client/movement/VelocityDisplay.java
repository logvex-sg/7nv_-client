package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Velocity readout showing the full delta-movement vector, its magnitude and
 * the vertical component. Useful for checking knockback and boost effects.
 */
public final class VelocityDisplay extends Module {
    public VelocityDisplay() {
        super("Velocity Display", "Shows your movement velocity vector", Category.MOVEMENT);
        showVector = bool("Vector", "Show the raw X/Y/Z components", true);
        showMagnitude = bool("Magnitude", "Show the total speed", true);
        horizontalOnly = bool("Horizontal only", "Ignore the vertical component", false);
    }

    private final BooleanSetting showVector;
    private final BooleanSetting showMagnitude;
    private final BooleanSetting horizontalOnly;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        if (mc.player == null) {
            return;
        }
        var motion = mc.player.getDeltaMovement();
        Theme theme = theme();
        int width = 118;
        int height = showVector.get() && showMagnitude.get() ? 22 : 13;
        int x = 4;
        int y = graphics.guiHeight() - height - 46;

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);
        int rowY = y + 3;
        if (showVector.get()) {
            String vector = String.format(java.util.Locale.ROOT, "x%.2f y%.2f z%.2f", motion.x, motion.y, motion.z);
            RenderUtils.text(graphics, vector, x + 3, rowY, theme.text);
            rowY += 9;
        }
        if (showMagnitude.get()) {
            double magnitude = horizontalOnly.get()
                    ? Math.sqrt(motion.x * motion.x + motion.z * motion.z)
                    : motion.length();
            RenderUtils.text(graphics, String.format(java.util.Locale.ROOT, "Speed %.3f b/t", magnitude),
                    x + 3, rowY, theme.textDim);
        }
    }

    private Theme theme() {
        var config = dev.sevennv.client.SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
