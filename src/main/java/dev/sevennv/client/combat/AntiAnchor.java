package dev.sevennv.client.combat;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.NotificationManager;
import dev.sevennv.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;

import java.util.HashSet;
import java.util.Set;

/**
 * Warns when a charged respawn anchor is near enough to be detonated. The scan
 * is a throttled read of the client's own loaded chunks; nothing is sent and no
 * anchor state is touched.
 */
public final class AntiAnchor extends Module {
    public AntiAnchor() {
        super("Anti Anchor", "Warns about charged respawn anchors nearby", Category.COMBAT);
        range = integer("Range", "Search radius in blocks", 12, 2, 32);
        minimumCharge = integer("Min charge", "Lowest charge level that counts as armed", 1, 1, 4);
        notify = bool("Notify", "Push a notification for each new anchor", true);
        refresh = integer("Refresh", "Milliseconds between scans", 500, 100, 5000);
    }

    private final IntSetting range;
    private final IntSetting minimumCharge;
    private final BooleanSetting notify;
    private final IntSetting refresh;

    private final Set<BlockPos> known = new HashSet<>();
    private final Timer timer = new Timer();

    @Override
    public void onWorldChange() {
        known.clear();
    }

    @Override
    public void onDisable() {
        known.clear();
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null || !timer.tick(refresh.get())) {
            return;
        }
        int radius = range.get();
        BlockPos centre = mc.player.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        Set<BlockPos> live = new HashSet<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -6; dy <= 6; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    cursor.set(centre.getX() + dx, centre.getY() + dy, centre.getZ() + dz);
                    if (!mc.level.isLoaded(cursor) || !mc.level.getBlockState(cursor).is(Blocks.RESPAWN_ANCHOR)) {
                        continue;
                    }
                    int charge = mc.level.getBlockState(cursor).getValue(RespawnAnchorBlock.CHARGE);
                    if (charge < minimumCharge.get()) {
                        continue;
                    }
                    BlockPos pos = cursor.immutable();
                    live.add(pos);
                    if (notify.get() && known.add(pos)) {
                        NotificationManager.warn("Anchor", "Charged anchor (" + charge + ") at "
                                + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                    }
                }
            }
        }
        known.retainAll(live);
    }

    @Override
    public String getArraylistSuffix() {
        return known.isEmpty() ? "" : String.valueOf(known.size());
    }
}
