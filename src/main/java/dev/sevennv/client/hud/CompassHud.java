package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** Cardinal direction plus the raw yaw and pitch angles. */
public final class CompassHud extends HudElement {
    public CompassHud() {
        super("compass", "Compass", 2, 90);
    }

    @Override
    public int getWidth() {
        return RenderUtils.font().width(line()) + 4;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.text(graphics, line(), x + 2, y + 1, 0xFFE0E0E0);
    }

    private String line() {
        var player = HudData.mc().player;
        if (player == null) {
            return "South 0/0";
        }
        return String.format(java.util.Locale.ROOT, "%s %.0f/%.0f",
                HudData.directionText(), player.getYRot(), player.getXRot());
    }
}
