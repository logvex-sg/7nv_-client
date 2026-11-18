package dev.sevennv.client.world;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.NotificationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Locates spawner blocks near the player and announces newly found ones. The
 * scan walks loaded chunks only, is throttled to once a second and keeps a
 * bounded set, so it stays cheap on the tick thread.
 */
public final class SpawnerFinder extends Module {
    public SpawnerFinder() {
        super("Spawner Finder", "Finds spawners near you", Category.WORLD);
        range = integer("Range", "Search radius in blocks", 32, 8, 96);
        notify = bool("Notify", "Announce a spawner the first time it is found", true);
        maxResults = integer("Max results", "Maximum remembered spawners", 64, 4, 512);
    }

    private final IntSetting range;
    private final BooleanSetting notify;
    private final IntSetting maxResults;

    private final Set<BlockPos> found = new LinkedHashSet<>();
    private int scanCooldown;

    @Override
    public void onWorldChange() {
        found.clear();
        scanCooldown = 0;
    }

    @Override
    public void onDisable() {
        found.clear();
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        // Scanning every tick would be wasteful; once a second is plenty.
        if (--scanCooldown > 0) {
            return;
        }
        scanCooldown = 20;
        int radius = range.get();
        BlockPos centre = mc.player.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx += 2) {
            for (int dy = -radius; dy <= radius; dy += 2) {
                for (int dz = -radius; dz <= radius; dz += 2) {
                    cursor.set(centre.getX() + dx, centre.getY() + dy, centre.getZ() + dz);
                    if (!mc.level.isLoaded(cursor) || found.contains(cursor)) {
                        continue;
                    }
                    var state = mc.level.getBlockState(cursor);
                    if (state.is(Blocks.SPAWNER) || state.is(Blocks.TRIAL_SPAWNER)) {
                        if (found.size() >= maxResults.get()) {
                            return;
                        }
                        found.add(cursor.immutable());
                        if (notify.get()) {
                            NotificationManager.info("Spawner", "Spawner at " + cursor.getX() + ", "
                                    + cursor.getY() + ", " + cursor.getZ());
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getArraylistSuffix() {
        return String.valueOf(found.size());
    }
}
