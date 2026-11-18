package dev.sevennv.client.world;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.NotificationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/**
 * Locates spawner blocks near the player and announces newly found ones. The
 * scan walks loaded chunks only and keeps a bounded list, so it stays cheap on
 * the tick thread.
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

    private final List<BlockPos> found = new ArrayList<>();
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
        for (int dx = -radius; dx <= radius; dx += 2) {
            for (int dy = -radius; dy <= radius; dy += 2) {
                for (int dz = -radius; dz <= radius; dz += 2) {
                    BlockPos pos = centre.offset(dx, dy, dz);
                    if (!mc.level.isLoaded(pos) || found.contains(pos)) {
                        continue;
                    }
                    var state = mc.level.getBlockState(pos);
                    if (state.is(Blocks.SPAWNER) || state.is(Blocks.TRIAL_SPAWNER)) {
                        if (found.size() >= maxResults.get()) {
                            return;
                        }
                        found.add(pos.immutable());
                        if (notify.get()) {
                            NotificationManager.info("Spawner", "Spawner at " + pos.getX() + ", "
                                    + pos.getY() + ", " + pos.getZ());
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
