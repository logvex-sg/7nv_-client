package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Current fall distance and the damage it would cause on landing. */
public final class FallDistanceHud extends HudElement {
    public FallDistanceHud() {
        super("falldist", "Fall Distance", 2, 155);
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
        double fall = Minecraft.getInstance().player == null ? 0.0
                : Minecraft.getInstance().player.fallDistance;
        RenderUtils.text(graphics, line(), x + 2, y + 1, fall > 3.0 ? 0xFFFF8080 : 0xFFE0E0E0);
    }

    private String line() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return "Fall 0.0";
        }
        double fall = player.fallDistance;
        int damage = (int) Math.max(0.0, Math.floor(fall - 3.0));
        return String.format("Fall %.1f (-%d)", fall, damage);
    }
}
