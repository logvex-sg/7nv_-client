package dev.sevennv.client.world;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.NotificationManager;
import dev.sevennv.client.utils.Timer;
import net.minecraft.core.BlockPos;

/**
 * Records the first position seen in a world and reports how far the player has
 * travelled from it. Purely a local measurement of the client's own position.
 */
public final class DistanceTracker extends Module {
    public DistanceTracker() {
        super("Distance Tracker", "Measures distance from your first position", Category.WORLD);
        notifyEvery = integer("Notify every", "Announce distance every N blocks (0 = never)", 0, 0, 10000);
        trackY = bool("Track Y", "Include the vertical axis in the distance", false);
    }

    private final IntSetting notifyEvery;
    private final BooleanSetting trackY;

    private BlockPos origin;
    private int lastAnnounced;
    private final Timer timer = new Timer();

    @Override
    public void onEnable() {
        origin = mc.player == null ? null : mc.player.blockPosition();
        lastAnnounced = 0;
    }

    @Override
    public void onWorldChange() {
        origin = mc.player == null ? null : mc.player.blockPosition();
        lastAnnounced = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        if (origin == null) {
            origin = mc.player.blockPosition();
            return;
        }
        int distance = distance();
        if (notifyEvery.get() > 0 && distance - lastAnnounced >= notifyEvery.get() && timer.hasElapsed(500)) {
            lastAnnounced = distance;
            timer.reset();
            NotificationManager.info("Distance", "Travelled " + distance + " blocks from origin");
        }
    }

    private int distance() {
        BlockPos now = mc.player.blockPosition();
        double dx = now.getX() - origin.getX();
        double dz = now.getZ() - origin.getZ();
        double dy = trackY.get() ? now.getY() - origin.getY() : 0;
        return (int) Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz));
    }

    @Override
    public String getArraylistSuffix() {
        return mc.player == null || origin == null ? "" : distance() + "m";
    }
}
