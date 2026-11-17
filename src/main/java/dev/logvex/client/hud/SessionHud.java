package dev.logvex.client.hud;

import dev.logvex.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** Session uptime, formatted as H:MM:SS. */
public final class SessionHud extends HudElement {
    private final long started = System.currentTimeMillis();

    public SessionHud() {
        super("session", "Session Timer", 2, 170);
    }

    @Override
    public int getWidth() {
        return RenderUtils.font().width(text()) + 4;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    private String text() {
        return "Session " + dev.logvex.client.utils.MathUtils.formatDuration(System.currentTimeMillis() - started);
    }

    @Override
    public void render(GuiGraphics graphics) {
        drawLine(graphics, text(), 0xFFE0E0E0);
    }
}
