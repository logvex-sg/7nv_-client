package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.PacketLog;
import net.minecraft.client.gui.GuiGraphics;

/** Inbound/outbound packet totals and the number of distinct types seen. */
public final class PacketHud extends HudElement {
    public PacketHud() {
        super("packets", "Packets", 2, 57);
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
        return "Packets " + PacketLog.inboundCount() + "/" + PacketLog.outboundCount();
    }
}
