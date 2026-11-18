package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Records the flight path of projectiles and draws it as a trail.
 *
 * <p>Positions are sampled per frame into a small ring buffer per entity and
 * the map is pruned when an entity is no longer valid, so it cannot grow
 * without bound.
 */
public final class ProjectileTrails extends Module {
    public ProjectileTrails() {
        super("Projectile Trails", "Draws trails behind projectiles", Category.RENDER);
        range = number("Range", "Maximum trail distance", 64.0, 4.0, 256.0, 4.0);
        color = color("Color", "Trail colour", 0xFFFF8040);
        lineWidth = number("Line width", "Stroke thickness", 1.5, 0.5, 4.0, 0.5);
        maxPoints = integer("Max points", "Trail length in samples", 40, 4, 200);
    }

    private final DoubleSetting range;
    private final ColorSetting color;
    private final DoubleSetting lineWidth;
    private final dev.sevennv.client.config.IntSetting maxPoints;

    private final Map<Integer, java.util.ArrayDeque<Vec3>> trails = new HashMap<>();

    @Override
    public void onWorldChange() {
        trails.clear();
    }

    @Override
    public void onDisable() {
        trails.clear();
    }

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        java.util.Set<Integer> live = new java.util.HashSet<>();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Projectile) || mc.player.distanceToSqr(entity) > rangeSq) {
                continue;
            }
            live.add(entity.getId());
            java.util.ArrayDeque<Vec3> trail = trails.computeIfAbsent(entity.getId(),
                    id -> new java.util.ArrayDeque<>());
            trail.addLast(entity.position());
            while (trail.size() > maxPoints.get()) {
                trail.removeFirst();
            }
            drawTrail(trail);
        }
        Iterator<Integer> iterator = trails.keySet().iterator();
        while (iterator.hasNext()) {
            if (!live.contains(iterator.next())) {
                iterator.remove();
            }
        }
    }

    private void drawTrail(java.util.ArrayDeque<Vec3> trail) {
        Vec3 previous = null;
        for (Vec3 point : trail) {
            if (previous != null) {
                WorldRenderUtils.line(previous, point, color.get(), (float) lineWidth.get());
            }
            previous = point;
        }
    }
}
