package dev.logvex.client.world;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.ColorSetting;
import dev.logvex.client.config.DoubleSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.render.WorldRenderUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/**
 * Stash notes: named storage locations kept as waypoints of kind STASH and
 * labelled with an optional note. Notes are stored in the waypoint record, so
 * they persist with the rest of the configuration.
 */
public final class StashNotes extends Module {
    public StashNotes() {
        super("Stash Notes", "Labels your stash locations", Category.WORLD);
        range = number("Range", "Maximum label distance", 256.0, 8.0, 2048.0, 8.0);
        color = color("Color", "Label colour", 0xFFFFC040);
        showNote = bool("Show note", "Include the stored note in the label", true);
        box = bool("Box", "Outline the stash block", true);
    }

    private final DoubleSetting range;
    private final ColorSetting color;
    private final BooleanSetting showNote;
    private final BooleanSetting box;

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        String dimension = mc.level.dimension().identifier().getPath();
        double rangeSq = range.get() * range.get();
        for (WaypointManager.Waypoint waypoint : WaypointManager.all()) {
            if (waypoint.getKind() != WaypointManager.Kind.STASH || !waypoint.isVisible()) {
                continue;
            }
            if (!waypoint.getDimension().equals(dimension)) {
                continue;
            }
            BlockPos pos = waypoint.pos();
            if (mc.player.position().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) > rangeSq) {
                continue;
            }
            if (box.get()) {
                WorldRenderUtils.box(new AABB(pos).inflate(0.05), color.get(), 1.5f);
            }
            String label = waypoint.getName();
            if (showNote.get() && !waypoint.getNote().isEmpty()) {
                label += " - " + waypoint.getNote();
            }
            WorldRenderUtils.label(WorldRenderUtils.blockTop(pos), label, color.get());
        }
    }

    @Override
    public String getArraylistSuffix() {
        long stashes = WaypointManager.all().stream()
                .filter(waypoint -> waypoint.getKind() == WaypointManager.Kind.STASH)
                .count();
        return String.valueOf(stashes);
    }
}
