package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

/** Movement readouts: fall distance, vertical velocity and on-ground state. */
public final class MovementHud extends HudElement {
    public MovementHud() {
        super("movement", "Movement HUD", 110, 142);
    }

    @Override
    public int getWidth() {
        return 108;
    }

    @Override
    public int getHeight() {
        return 30;
    }

    @Override
    public void render(GuiGraphics graphics) {
        Player player = HudData.mc().player;
        if (player == null) {
            return;
        }
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.text(graphics, "Fall " + String.format(java.util.Locale.ROOT, "%.2f", player.fallDistance), x + 1, y + 1, 0xFFE0E0E0);
        RenderUtils.text(graphics, "Vert " + String.format(java.util.Locale.ROOT, "%.3f", player.getDeltaMovement().y), x + 1, y + 11, 0xFFE0E0E0);
        RenderUtils.text(graphics, "Ground " + (player.onGround() ? "yes" : "no"), x + 1, y + 21, 0xFFE0E0E0);
    }
}
