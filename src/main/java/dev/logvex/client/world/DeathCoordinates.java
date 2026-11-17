package dev.logvex.client.world;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.ColorSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.render.WorldRenderUtils;
import dev.logvex.client.utils.NotificationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Records where the player dies and draws a marker there. The last few death
 * points are kept so a trip back is easy to plan.
 */
public final class DeathCoordinates extends Module {
    public DeathCoordinates() {
        super("Death Coordinates", "Marks where you last died", Category.WORLD);
        color = color("Color", "Marker colour", 0xFFD05050);
        notify = bool("Notify", "Print the death coordinates to chat", true);
        markWaypoint = bool("Mark waypoint", "Also store the death as a waypoint", true);
        beam = bool("Beam", "Draw a vertical beam", true);
    }

    private final ColorSetting color;
    private final BooleanSetting notify;
    private final BooleanSetting markWaypoint;
    private final BooleanSetting beam;

    private Vec3 lastDeath;
    private String lastDeathDimension;

    @Override
    public void onWorldChange() {
        lastDeath = null;
        lastDeathDimension = null;
    }

    /** Called by the client event bridge when the local player dies. */
    public void onDeath(Vec3 position, String dimension) {
        lastDeath = position;
        lastDeathDimension = dimension;
        String text = String.format(java.util.Locale.ROOT, "%.0f, %.0f, %.0f",
                position.x, position.y, position.z);
        if (notify.get()) {
            NotificationManager.warn("Death", text + " in " + dimension);
        }
        if (markWaypoint.get()) {
            WaypointManager.add("Death " + System.currentTimeMillis() % 100000L, "Deaths",
                    WaypointManager.Kind.DEATH, (int) position.x, (int) position.y, (int) position.z, dimension);
        }
    }

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null || lastDeath == null) {
            return;
        }
        String dimension = mc.level.dimension().identifier().getPath();
        if (lastDeathDimension != null && !lastDeathDimension.equals(dimension)) {
            return;
        }
        BlockPos pos = BlockPos.containing(lastDeath.x, lastDeath.y, lastDeath.z);
        int markerColor = color.get();
        WorldRenderUtils.box(new AABB(pos).inflate(0.05), markerColor, 1.5f);
        if (beam.get()) {
            WorldRenderUtils.line(WorldRenderUtils.blockCenter(pos),
                    new Vec3(pos.getX() + 0.5, mc.level.getMaxY() + 1, pos.getZ() + 0.5), markerColor, 1.0f);
        }
        double distance = Math.sqrt(mc.player.distanceToSqr(lastDeath));
        WorldRenderUtils.label(WorldRenderUtils.blockTop(pos), String.format(java.util.Locale.ROOT, "Death %.0fm", distance), markerColor);
    }
}
