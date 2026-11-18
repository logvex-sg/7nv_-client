package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Shows the player the crosshair is pointing at (or the closest one) with
 * health, armour and distance.
 */
public final class TargetHud extends HudElement {
    public TargetHud() {
        super("target", "Target HUD", 260, 60);
    }

    /** Set each tick by the TargetHudModule so the element has no tick logic. */
    private static LivingEntity target;

    public static void setTarget(LivingEntity entity) {
        target = entity;
    }

    public static LivingEntity getTarget() {
        return target;
    }

    @Override
    public int getWidth() {
        return 118;
    }

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    public void render(GuiGraphics graphics) {
        LivingEntity entity = target;
        if (entity == null || !entity.isAlive()) {
            return;
        }
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.outline(graphics, x, y, getWidth(), getHeight(), 0xFF101010);

        String name = entity.getName().getString();
        RenderUtils.text(graphics, name, x + 2, y + 2, 0xFFFFFFFF);

        float health = entity.getHealth() + entity.getAbsorptionAmount();
        float max = Math.max(1f, entity.getMaxHealth() + entity.getAbsorptionAmount());
        float fraction = Math.max(0f, Math.min(1f, health / max));
        int barColor = fraction > 0.5f ? 0xFF55FF55 : fraction > 0.25f ? 0xFFFFFF55 : 0xFFFF5555;

        RenderUtils.rect(graphics, x + 2, y + 14, getWidth() - 4, 6, 0xFF101010);
        RenderUtils.rect(graphics, x + 2, y + 14, (int) ((getWidth() - 4) * fraction), 6, barColor);

        String detail = String.format(java.util.Locale.ROOT, "%.1f/%.1f  %.1fm", health, max,
                HudData.mc().player == null ? 0 : HudData.mc().player.distanceTo(entity));
        RenderUtils.text(graphics, detail, x + 2, y + 22, 0xFFC0C0C0);
    }
}
