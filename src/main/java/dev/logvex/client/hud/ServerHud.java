package dev.logvex.client.hud;

import dev.logvex.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** Server name, version and ping summary. */
public final class ServerHud extends HudElement {
    public ServerHud() {
        super("server", "Server Info", 2, 126);
    }

    @Override
    public int getWidth() {
        return Math.max(RenderUtils.font().width(HudData.serverText()) + 4, 90);
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public void render(GuiGraphics graphics) {
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.text(graphics, HudData.serverText(), x + 1, y + 1, 0xFFE0E0E0);
        RenderUtils.text(graphics, HudData.versionText() + "  " + HudData.pingText(), x + 1, y + 11, 0xFFA0A0A0);
    }
}
