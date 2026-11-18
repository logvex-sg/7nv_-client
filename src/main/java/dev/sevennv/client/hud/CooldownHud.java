package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Attack cooldown bar, so you can time hits at full charge. */
public final class CooldownHud extends HudElement {
    private static final int BAR_WIDTH = 60;

    public CooldownHud() {
        super("cooldown", "Attack Cooldown", 110, 134);
    }

    @Override
    public int getWidth() {
        return BAR_WIDTH + 4;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public void render(GuiGraphics graphics) {
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.text(graphics, "Cooldown", x + 2, y + 1, 0xFFE0E0E0);
        float progress = 1.0f;
        var player = Minecraft.getInstance().player;
        if (player != null) {
            progress = Math.min(1.0f, player.getAttackStrengthScale(0.0f));
        }
        int filled = Math.round(progress * BAR_WIDTH);
        RenderUtils.rect(graphics, x + 2, y + 11, BAR_WIDTH, 4, 0x80000000);
        RenderUtils.rect(graphics, x + 2, y + 11, filled, 4, progress >= 1.0f ? 0xFF55FF55 : 0xFFFFC040);
    }
}
