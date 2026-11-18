package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

/** Raw yaw and pitch of the camera. */
public final class RotationHud extends HudElement {
    public RotationHud() {
        super("rotation", "Rotation", 110, 199);
    }

    @Override
    public int getWidth() {
        return Math.max(RenderUtils.font().width(line()) + 4, 90);
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        drawLine(graphics, line(), 0xFFE0E0E0);
    }

    private String line() {
        Player player = HudData.mc().player;
        if (player == null) {
            return "Yaw -  Pitch -";
        }
        return String.format(java.util.Locale.ROOT, "Yaw %.0f  Pitch %.0f", player.getYRot(), player.getXRot());
    }
}
