package dev.sevennv.client.render;

import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Leaves a short trail behind the player so you can retrace a path in the dark
 * or after a fight. Points are only recorded while moving and the trail is a
 * fixed-size ring, so memory use is bounded.
 */
public final class Breadcrumbs extends Module {
    public Breadcrumbs() {
        super("Breadcrumbs", "Draws a trail of your recent path", Category.RENDER);
        length = integer("Length", "Number of trail points", 64, 8, 256);
        spacing = number("Spacing", "Minimum distance between points", 0.5, 0.1, 4.0, 0.1);
        color = color("Color", "Trail colour", 0xFF55FFFF);
    }

    private final IntSetting length;
    private final DoubleSetting spacing;
    private final ColorSetting color;
    private final Deque<Vec3> points = new ArrayDeque<>();
    private Vec3 last;

    @Override
    public void onWorldChange() {
        points.clear();
        last = null;
    }

    @Override
    public void onDisable() {
        points.clear();
        last = null;
    }

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        Vec3 current = new Vec3(mc.player.getX(), mc.player.getY() + 0.1, mc.player.getZ());
        if (last == null || last.distanceTo(current) >= spacing.get()) {
            last = current;
            points.addLast(current);
            while (points.size() > length.get()) {
                points.removeFirst();
            }
        }
    }

    @Override
    public void onWorldRender() {
        if (points.size() < 2) {
            return;
        }
        Vec3 previous = null;
        for (Vec3 point : points) {
            if (previous != null) {
                WorldRenderUtils.line(previous, point, color.get(), 2.0f);
            }
            previous = point;
        }
    }
}
