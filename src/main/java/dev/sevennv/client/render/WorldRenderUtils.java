package dev.sevennv.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * World-space drawing built on the 1.21.9+ {@code Gizmos} collector.
 *
 * <p>Everything added here is emitted once per frame by {@code LevelRenderer}
 * while it renders the world, so these calls are only valid inside a world
 * render callback. Coordinates are absolute world coordinates.
 */
public final class WorldRenderUtils {
    private WorldRenderUtils() {
    }

    /** Outline box with the default 1px stroke. */
    public static void box(AABB bounds, int color) {
        Gizmos.cuboid(bounds, GizmoStyle.stroke(color));
    }

    /** Outline box, thicker stroke. */
    public static void box(AABB bounds, int color, float width) {
        Gizmos.cuboid(bounds, GizmoStyle.stroke(color, width));
    }

    /** Outline box plus a translucent fill. */
    public static void filledBox(AABB bounds, int stroke, float width, int fill) {
        Gizmos.cuboid(bounds, GizmoStyle.strokeAndFill(stroke, width, fill));
    }

    /** Filled-only box. */
    public static void fill(AABB bounds, int color) {
        Gizmos.cuboid(bounds, GizmoStyle.fill(color));
    }

    public static void line(Vec3 from, Vec3 to, int color) {
        Gizmos.line(from, to, color);
    }

    public static void line(Vec3 from, Vec3 to, int color, float width) {
        Gizmos.line(from, to, color, width);
    }

    /** A line that ignores depth, so it is visible through terrain. */
    public static void lineThroughWalls(Vec3 from, Vec3 to, int color, float width) {
        Gizmos.line(from, to, color, width).setAlwaysOnTop();
    }

    /** Tracer from the camera to a point. */
    public static void tracer(Vec3 to, int color) {
        Vec3 camera = cameraPos();
        Gizmos.line(camera, to, color).setAlwaysOnTop();
    }

    public static void rect(Vec3 a, Vec3 b, Vec3 c, Vec3 d, GizmoStyle style) {
        Gizmos.rect(a, b, c, d, style);
    }

    /** Floating world-space text that always faces the camera. */
    public static void billboardText(String text, Vec3 pos, int color, float scale) {
        GizmoProperties properties = Gizmos.billboardText(text, pos, TextGizmo.Style.forColorAndCentered(color).withScale(scale));
        properties.setAlwaysOnTop();
    }

    /** Floating world-space text at the default scale. */
    public static void label(Vec3 pos, String text, int color) {
        billboardText(text, pos, color, 1.0f);
    }

    public static Vec3 cameraPos() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null || mc.gameRenderer.getMainCamera() == null) {
            return Vec3.ZERO;
        }
        return mc.gameRenderer.getMainCamera().position();
    }

    /** AABB for an entity, expanded slightly so the outline does not z-fight. */
    public static AABB entityBox(net.minecraft.world.entity.Entity entity) {
        return entity.getBoundingBox().inflate(0.02);
    }

    /** Centre of an entity's bounding box. */
    public static Vec3 entityCenter(net.minecraft.world.entity.Entity entity) {
        AABB box = entity.getBoundingBox();
        return new Vec3((box.minX + box.maxX) / 2.0, (box.minY + box.maxY) / 2.0, (box.minZ + box.maxZ) / 2.0);
    }

    /** Centre of the block at {@code pos}. */
    public static Vec3 blockCenter(net.minecraft.core.BlockPos pos) {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    /** Top-centre of the block at {@code pos}, used for stacked labels. */
    public static Vec3 blockTop(net.minecraft.core.BlockPos pos) {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
    }
}
