package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A text list of nearby players and hostile mobs, sorted by distance. Each line
 * shows the name, distance and health. The list is rebuilt each frame because
 * the data is live, but the entity buffer and the row list are reused so a
 * crowded area does not churn the allocator every frame.
 */
public final class TextRadarHud extends HudElement {
    private static final int MAX_LINES = 8;
    private static final double RANGE = 48.0;

    private final List<String> lines = new ArrayList<>();
    private final List<LivingEntity> nearby = new ArrayList<>();
    private final StringBuilder row = new StringBuilder(40);

    public TextRadarHud() {
        super("textradar", "Text Radar", 220, 80);
    }

    @Override
    public int getWidth() {
        return 110;
    }

    @Override
    public int getHeight() {
        return Math.max(12, (lines.size() + 1) * 10 + 2);
    }

    @Override
    public void render(GuiGraphics graphics) {
        var mc = HudData.mc();
        lines.clear();
        nearby.clear();
        if (mc.level != null && mc.player != null) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity == mc.player || !(entity instanceof LivingEntity living) || !living.isAlive()) {
                    continue;
                }
                if (entity instanceof Player || entity instanceof net.minecraft.world.entity.Mob) {
                    nearby.add(living);
                }
            }
            nearby.sort(Comparator.comparingDouble(mc.player::distanceTo));
            for (LivingEntity living : nearby) {
                if (lines.size() >= MAX_LINES) {
                    break;
                }
                double distance = mc.player.distanceTo(living);
                if (distance > RANGE) {
                    continue;
                }
                String name = living.getName().getString();
                if (name.length() > 14) {
                    name = name.substring(0, 14);
                }
                row.setLength(0);
                row.append(name).append(' ');
                appendRounded(row, distance);
                row.append("m ");
                appendRounded(row, living.getHealth());
                row.append('\u2764');
                lines.add(row.toString());
            }
        }
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.text(graphics, "Radar (" + lines.size() + ")", x + 2, y + 1, 0xFFFFD050);
        int rowY = y + 11;
        for (String line : lines) {
            RenderUtils.text(graphics, line, x + 2, rowY, 0xFFE0E0E0);
            rowY += 10;
        }
    }

    /** Appends {@code value} rounded to the nearest whole number, no allocation. */
    private static void appendRounded(StringBuilder target, double value) {
        long rounded = Math.round(value);
        target.append(rounded);
    }
}
