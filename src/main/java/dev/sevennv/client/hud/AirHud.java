package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

/** Remaining breath while underwater. */
public final class AirHud extends HudElement {
    public AirHud() {
        super("air", "Air", 110, 155);
    }

    @Override
    public int getWidth() {
        return Math.max(RenderUtils.font().width(line()) + 4, 70);
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        Player player = HudData.mc().player;
        if (player == null) {
            return;
        }
        int color = player.getAirSupply() <= 60 ? 0xFFFF6060 : 0xFF80C0FF;
        drawLine(graphics, line(), color);
    }

    private String line() {
        Player player = HudData.mc().player;
        if (player == null) {
            return "Air -";
        }
        return "Air " + player.getAirSupply() + "/" + player.getMaxAirSupply();
    }
}
