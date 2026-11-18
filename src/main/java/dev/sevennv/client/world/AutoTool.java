package dev.sevennv.client.world;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.Timer;

/**
 * Selects the fastest hotbar tool for the block being mined. Selection uses the
 * vanilla destroy-speed calculation, so the tool chosen is exactly the one the
 * game would reward with a faster break.
 */
public final class AutoTool extends Module {
    public AutoTool() {
        super("Auto Tool", "Selects the best tool for the block you are mining", Category.WORLD);
        requireBreaking = bool("Require mining", "Only switch while a block is being mined", true);
        restore = bool("Restore slot", "Return to the previous slot afterwards", false);
        delay = integer("Delay", "Milliseconds between switches", 0, 0, 500);
    }

    private final BooleanSetting requireBreaking;
    private final BooleanSetting restore;
    private final IntSetting delay;
    private final Timer timer = new Timer();
    private int previousSlot = -1;

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @Override
    public void onWorldChange() {
        previousSlot = -1;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || !timer.tick(delay.get())) {
            return;
        }
        if (requireBreaking.get() && (mc.gameMode == null || !mc.gameMode.isDestroying())) {
            restoreSlot();
            return;
        }
        var hit = mc.hitResult;
        if (!(hit instanceof net.minecraft.world.phys.BlockHitResult blockHit)) {
            return;
        }
        var state = mc.level.getBlockState(blockHit.getBlockPos());
        if (state.isAir()) {
            return;
        }
        int bestSlot = -1;
        float bestSpeed = 1.0f;
        var inventory = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            var stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        if (bestSlot == -1 || bestSlot == inventory.getSelectedSlot()) {
            return;
        }
        if (previousSlot == -1) {
            previousSlot = inventory.getSelectedSlot();
        }
        InventoryUtils.selectSlot(bestSlot);
    }

    private void restoreSlot() {
        if (restore.get() && previousSlot != -1 && mc.player != null) {
            InventoryUtils.selectSlot(previousSlot);
        }
        previousSlot = -1;
    }
}
