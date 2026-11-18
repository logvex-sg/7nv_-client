package dev.sevennv.client.hud;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** Server, dimension and enabled-module count in one compact line. */
public final class SessionInfoHud extends HudElement {
    public SessionInfoHud() {
        super("sessioninfo", "Session Info", 110, 237);
    }

    @Override
    public int getWidth() {
        return Math.max(RenderUtils.font().width(line()) + 4, 130);
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        drawLine(graphics, line(), 0xFFC0C0C0);
    }

    private String line() {
        int modules = SevennvClient.getModuleManager() == null
                ? 0 : SevennvClient.getModuleManager().getEnabledCount();
        return HudData.serverText() + " | " + HudData.dimensionText() + " | " + modules + " on";
    }
}
