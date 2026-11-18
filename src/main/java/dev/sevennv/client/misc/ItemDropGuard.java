package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.ChatUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Warns before you throw away something valuable. When the drop key is held and
 * a valuable stack is selected it prints a reminder and, optionally, releases
 * the drop key for that tick.
 */
public final class ItemDropGuard extends Module {
    public ItemDropGuard() {
        super("Drop Guard", "Warns when you are about to drop something valuable", Category.MISC);
        blockDrop = bool("Block drop", "Release the drop key while holding a valuable item", true);
        watchShulkers = bool("Shulkers", "Protect shulker boxes", true);
        watchGear = bool("Gear", "Protect enchanted gear", true);
        cooldown = integer("Cooldown", "Milliseconds between warnings", 2000, 200, 60000);
    }

    private final BooleanSetting blockDrop;
    private final BooleanSetting watchShulkers;
    private final BooleanSetting watchGear;
    private final IntSetting cooldown;
    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) {
            return;
        }
        if (!mc.options.keyDrop.isDown()) {
            return;
        }
        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty() || !valuable(stack)) {
            return;
        }
        if (blockDrop.get()) {
            mc.options.keyDrop.setDown(false);
        }
        if (timer.tick(cooldown.get())) {
            ChatUtils.info("Drop guard: " + stack.getHoverName().getString());
        }
    }

    private boolean valuable(ItemStack stack) {
        if (watchShulkers.get() && stack.toString().contains("shulker_box")) {
            return true;
        }
        if (stack.is(Items.TOTEM_OF_UNDYING) || stack.is(Items.ELYTRA)
                || stack.is(Items.NETHERITE_INGOT) || stack.is(Items.DIAMOND_BLOCK)) {
            return true;
        }
        return watchGear.get() && stack.isEnchanted();
    }
}
