package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** The address of the server currently connected to. */
public final class ServerIpHud extends HudElement {
    public ServerIpHud() {
        super("serverip", "Server IP", 2, 134);
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
        var mc = HudData.mc();
        if (mc.isSingleplayer()) {
            return "Singleplayer";
        }
        if (mc.getCurrentServer() != null) {
            return mc.getCurrentServer().ip;
        }
        return "Disconnected";
    }
}
