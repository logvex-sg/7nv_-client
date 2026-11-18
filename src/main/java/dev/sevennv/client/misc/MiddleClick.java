package dev.sevennv.client.misc;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.ChatUtils;
import dev.sevennv.client.utils.FriendManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Middle-click a player to add or remove them from your friend list. The
 * pick-block key is the trigger; when the crosshair is on another player the
 * local list is toggled. The vanilla pick-block still runs, so no input is
 * consumed.
 *
 * <p>Friend lists are local only: nothing is sent to the server and no player is
 * told they were added.
 */
public final class MiddleClick extends Module {
    public MiddleClick() {
        super("Middle Click", "Middle-click a player to friend or unfriend them", Category.MISC);
        addFriends = bool("Add friends", "Middle-click adds to the friend list", true);
        announce = bool("Announce", "Show a local chat message on change", true);
        requireShift = bool("Require shift", "Only act while holding sneak", false);
    }

    private final BooleanSetting addFriends;
    private final BooleanSetting announce;
    private final BooleanSetting requireShift;

    private boolean wasDown;

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) {
            return;
        }
        boolean down = mc.options.keyPickItem.isDown();
        if (down && !wasDown) {
            onPress();
        }
        wasDown = down;
    }

    private void onPress() {
        if (requireShift.get() && !mc.player.isShiftKeyDown()) {
            return;
        }
        Entity target = dev.sevennv.client.utils.PlayerUtils.crosshairTarget();
        if (!(target instanceof Player player) || player == mc.player) {
            return;
        }
        String name = player.getGameProfile().name();
        if (FriendManager.isFriend(name)) {
            FriendManager.remove(name);
            if (announce.get()) {
                ChatUtils.info("Removed " + name + " from friends");
            }
        } else if (addFriends.get()) {
            FriendManager.addFriend(name);
            if (announce.get()) {
                ChatUtils.info("Added " + name + " to friends");
            }
        }
    }

    @Override
    public void onWorldChange() {
        wasDown = false;
    }

    @Override
    public void onDisable() {
        wasDown = false;
    }
}
