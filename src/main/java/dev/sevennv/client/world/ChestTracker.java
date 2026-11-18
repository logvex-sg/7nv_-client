package dev.sevennv.client.world;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.BlockUtils;
import net.minecraft.core.BlockPos;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Remembers storage blocks it has seen near the player so a stash can be found
 * again later. Records are keyed by block position and the map is bounded, so
 * memory stays flat across long sessions.
 */
public final class ChestTracker extends Module {
    public ChestTracker() {
        super("Chest Tracker", "Remembers storage blocks seen near you", Category.WORLD);
        range = integer("Range", "Search radius in blocks", 24, 4, 96);
        maxEntries = integer("Max entries", "Maximum remembered containers", 128, 8, 1024);
        chat = bool("Chat", "Print a line when a new container is recorded", false);
    }

    private final IntSetting range;
    private final IntSetting maxEntries;
    private final BooleanSetting chat;

    private final Map<BlockPos, Long> opened = new LinkedHashMap<>();
    private int scanCooldown;

    @Override
    public void onWorldChange() {
        opened.clear();
        scanCooldown = 0;
    }

    @Override
    public void onDisable() {
        opened.clear();
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        if (--scanCooldown > 0) {
            return;
        }
        scanCooldown = 40;
        int radius = range.get();
        BlockPos centre = mc.player.blockPosition();
        for (int dx = -radius; dx <= radius; dx += 2) {
            for (int dy = -radius; dy <= radius; dy += 2) {
                for (int dz = -radius; dz <= radius; dz += 2) {
                    BlockPos pos = centre.offset(dx, dy, dz);
                    if (!mc.level.isLoaded(pos) || opened.containsKey(pos)) {
                        continue;
                    }
                    if (BlockUtils.isStorage(mc.level.getBlockState(pos))) {
                        record(pos);
                        if (opened.size() >= maxEntries.get()) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private void record(BlockPos pos) {
        opened.put(pos.immutable(), System.currentTimeMillis());
        while (opened.size() > maxEntries.get()) {
            BlockPos oldest = opened.keySet().iterator().next();
            opened.remove(oldest);
        }
        if (chat.get()) {
            dev.sevennv.client.utils.ChatUtils.info("Container recorded at " + pos.getX() + ", "
                    + pos.getY() + ", " + pos.getZ());
        }
    }

    public int count() {
        return opened.size();
    }

    @Override
    public String getArraylistSuffix() {
        return String.valueOf(opened.size());
    }
}
