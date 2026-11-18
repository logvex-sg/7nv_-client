package dev.sevennv.client.player;

import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.mixin.FishingHookAccessor;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

/**
 * Reels in the fishing rod as soon as a fish bites and casts again afterwards.
 *
 * <p>The bite is read from the hook's synced flag, so this works on a normal
 * server without any packet trickery. The reel and cast both go through the
 * ordinary item-use path.
 */
public final class AutoFish extends Module {
    public AutoFish() {
        super("Auto Fish", "Reels in and recasts the fishing rod for you", Category.PLAYER);
        recastDelay = integer("Recast delay", "Milliseconds before casting again", 750, 100, 5000);
        reelDelay = integer("Reel delay", "Milliseconds between reels", 250, 0, 2000);
    }

    private final IntSetting recastDelay;
    private final IntSetting reelDelay;
    private final Timer castTimer = new Timer();
    private final Timer reelTimer = new Timer();

    private boolean reeled;

    @Override
    public void onWorldChange() {
        reeled = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }
        if (!mc.player.getMainHandItem().is(Items.FISHING_ROD)) {
            return;
        }
        var hook = mc.player.fishing;
        if (hook == null) {
            reeled = false;
            if (castTimer.tick(recastDelay.get())) {
                use();
            }
            return;
        }
        boolean biting = hook instanceof FishingHookAccessor accessor && accessor.sevennv$isBiting();
        if (biting && !reeled && reelTimer.tick(reelDelay.get())) {
            reeled = true;
            castTimer.reset();
            use();
        }
    }

    private void use() {
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }
}
