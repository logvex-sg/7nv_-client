package dev.logvex.client.world;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.ColorSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.render.RenderUtils;
import dev.logvex.client.render.WorldRenderUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Renders stored {@link WaypointManager} entries as world-space markers with
 * optional on-screen distance labels. Read-only with respect to the world.
 */
public final class Waypoints extends Module {
    public Waypoints() {
        super("Waypoints", "Shows stored waypoints in the world", Category.WORLD);
        range = number("Range", "Maximum render distance", 512.0, 16.0, 4096.0, 16.0);
        color = color("Color", "Marker colour", 0xFFA882FF);
        lineWidth = number("Line width", "Stroke thickness", 1.5, 0.5, 4.0, 0.5);
        labels = bool("Labels", "Draw the waypoint name", true);
        beams = bool("Beams", "Draw a vertical beam to the sky", true);
        currentDimensionOnly = bool("Current dimension", "Only draw waypoints in this dimension", true);
    }

    private final DoubleSetting range;
    private final ColorSetting color;
    private final DoubleSetting lineWidth;
    private final BooleanSetting labels;
    private final BooleanSetting beams;
    private final BooleanSetting currentDimensionOnly;

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        String dimension = mc.level.dimension().identifier().getPath();
        double rangeSq = range.get() * range.get();
        for (WaypointManager.Waypoint waypoint : WaypointManager.all()) {
            if (!waypoint.isVisible()) {
                continue;
            }
            if (currentDimensionOnly.get() && !waypoint.getDimension().equals(dimension)) {
                continue;
            }
            BlockPos pos = waypoint.pos();
            if (mc.player.position().distanceToSqr(Vec3.atCenterOf(pos)) > rangeSq) {
                continue;
            }
            int markerColor = color.get();
            WorldRenderUtils.box(new net.minecraft.world.phys.AABB(pos).inflate(0.05), markerColor,
                    (float) lineWidth.get());
            if (beams.get()) {
                WorldRenderUtils.line(WorldRenderUtils.blockCenter(pos),
                        new Vec3(pos.getX() + 0.5, mc.level.getMaxY() + 1, pos.getZ() + 0.5), markerColor, 1.0f);
            }
            if (labels.get()) {
                double distance = mc.player.distanceToSqr(Vec3.atCenterOf(pos));
                String text = waypoint.getName() + " " + (int) Math.sqrt(distance) + "m";
                WorldRenderUtils.label(WorldRenderUtils.blockTop(pos), text, markerColor);
            }
        }
    }

    @Override
    public String getArraylistSuffix() {
        return String.valueOf(WaypointManager.all().size());
    }
}
