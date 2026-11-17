package dev.logvex.client.movement;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import net.minecraft.world.item.Items;

/**
 * Removes the item-use movement penalty (eating, drinking, blocking). The
 * vanilla slowdown is applied by {@code LocalPlayer.modifyInput} while an item
 * is in use; this restores the impulse on the same tick so movement stays at
 * full speed.
 */
public final class NoSlow extends Module {
    public NoSlow() {
        super("No Slow", "Removes the item-use movement slowdown", Category.MOVEMENT);
        onlyWhileMoving = bool("Only while moving", "Do nothing when standing still", true);
        includeSneak = bool("Include sneak", "Also counter the sneak slowdown", false);
    }

    private final BooleanSetting onlyWhileMoving;
    private final BooleanSetting includeSneak;

    @Override
    public void onTick() {
        if (mc.player == null) {
            return;
        }
        boolean usingItem = mc.player.isUsingItem();
        boolean sneaking = includeSneak.get() && mc.player.isShiftKeyDown();
        if (!usingItem && !sneaking) {
            return;
        }
        if (onlyWhileMoving.get() && mc.player.zza == 0.0f && mc.player.xxa == 0.0f) {
            return;
        }
        // Re-apply the raw impulse after the vanilla slowdown has been applied.
        // The values are the unmodified key impulses, which is what the player
        // would have had without the item-use penalty.
        if (mc.options.keyUp.isDown()) {
            mc.player.zza = 1.0f;
        }
        if (mc.options.keyDown.isDown()) {
            mc.player.zza = -1.0f;
        }
        if (mc.options.keyLeft.isDown()) {
            mc.player.xxa = 1.0f;
        }
        if (mc.options.keyRight.isDown()) {
            mc.player.xxa = -1.0f;
        }
        if (mc.player.getMainHandItem().is(Items.SHIELD) || mc.player.getOffhandItem().is(Items.SHIELD)) {
            // Blocking applies its own penalty; the impulse fix above covers it.
            mc.player.setSprinting(mc.player.zza > 0.0f);
        }
    }
}
