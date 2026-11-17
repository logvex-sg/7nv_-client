package dev.logvex.client.movement;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.config.Theme;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Fall distance readout, with an optional warning colour when the fall is
 * dangerous.
 */
public final class FallDistance extends Module {
    public FallDistance() {
        super("Fall Distance", "Shows how far you have fallen", Category.MOVEMENT);
        warnDistance = number("Warn distance", "Colour the text above this fall distance", 3.0, 1.0, 20.0, 0.5);
        alwaysShow = bool("Always show", "Show even when not falling", false);
    }

    private final DoubleSetting warnDistance;
    private final BooleanSetting alwaysShow;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        if (mc.player == null) {
            return;
        }
        double fall = mc.player.fallDistance;
        if (!alwaysShow.get() && fall <= 0.0) {
            return;
        }
        Theme theme = theme();
        String text = String.format(java.util.Locale.ROOT, "Fall %.1f", fall);
        int width = RenderUtils.font().width(text) + 6;
        int x = 4;
        int y = graphics.guiHeight() - 60;
        RenderUtils.rect(graphics, x, y, width, 11, theme.panel);
        RenderUtils.outline(graphics, x, y, width, 11, theme.border);
        int color = fall >= warnDistance.get() ? 0xFFFF5050 : theme.text;
        RenderUtils.text(graphics, text, x + 3, y + 2, color);
    }

    private Theme theme() {
        var config = dev.logvex.client.LogvexClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
