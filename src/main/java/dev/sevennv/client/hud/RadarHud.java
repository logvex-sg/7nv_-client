package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * A top-down radar of nearby entities. Players are red, hostiles orange and
 * passive mobs green; the local player is drawn as a white dot in the centre.
 */
public final class RadarHud extends HudElement {
    private static final int SIZE = 64;
    private static final double RANGE = 32.0;

    public RadarHud() {
        super("radar", "Radar", 200, 4);
    }

    @Override
    public int getWidth() {
        return SIZE + 2;
    }

    @Override
    public int getHeight() {
        return SIZE + 2;
    }

    @Override
    public void render(GuiGraphics graphics) {
        var mc = HudData.mc();
        if (mc.level == null || mc.player == null) {
            return;
        }
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.outline(graphics, x, y, getWidth(), getHeight(), 0xFF404040);
        int centerX = x + getWidth() / 2;
        int centerY = y + getHeight() / 2;
        RenderUtils.rect(graphics, centerX - 1, centerY - 1, 2, 2, 0xFFFFFFFF);
        double scale = (SIZE / 2.0) / RANGE;
        float yaw = mc.player.getYRot();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || !living.isAlive()) {
                continue;
            }
            double dx = entity.getX() - mc.player.getX();
            double dz = entity.getZ() - mc.player.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > RANGE) {
                continue;
            }
            // Rotate the offset so the radar is aligned with the player's facing.
            double radians = Math.toRadians(-yaw);
            double rotX = dx * Math.cos(radians) - dz * Math.sin(radians);
            double rotZ = dx * Math.sin(radians) + dz * Math.cos(radians);
            int px = centerX + (int) (rotX * scale);
            int py = centerY + (int) (rotZ * scale);
            int color = entity instanceof Player ? 0xFFFF5050
                    : entity instanceof net.minecraft.world.entity.Mob ? 0xFFFFA040 : 0xFF50FF50;
            RenderUtils.rect(graphics, px, py, 2, 2, color);
        }
    }
}
