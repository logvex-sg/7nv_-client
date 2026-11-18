package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/**
 * A compact ping readout that colours the value by connection quality, so a bad
 * link is obvious at a glance without reading the number.
 */
public final class PingBarHud extends HudElement {
    public PingBarHud() {
        super("pingbar", "Ping Bar", 2, 190);
    }

    @Override
    public int getWidth() {
        return Math.max(RenderUtils.font().width(line()) + 4, 74);
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        int ping = HudData.ping();
        int color = ping < 0 ? 0xFF808080 : ping < 80 ? 0xFF5FBF5F : ping < 160 ? 0xFFFFC040 : 0xFFFF5050;
        drawLine(graphics, line(), color);
    }

    private String line() {
        int ping = HudData.ping();
        return "Ping " + (ping < 0 ? "--" : ping + "ms");
    }
}
