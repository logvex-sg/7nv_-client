package dev.sevennv.client.world;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.CombatUtils;
import dev.sevennv.client.utils.InventoryUtils;
import dev.sevennv.client.utils.Rotation;
import dev.sevennv.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Places blocks under you as you walk, so you can bridge across gaps and tower
 * upward without falling. Placement uses the ordinary block interaction path,
 * so the server sees a normal place.
 *
 * <p>By default it only places a block when there is nothing under your feet and
 * you are moving; a sneak toggle lets you build while holding sneak, and the
 * auto-switch finds a block in your hotbar so you do not have to hold one.
 */
public final class Scaffold extends Module {
    public Scaffold() {
        super("Scaffold", "Places blocks under you while you walk or tower", Category.WORLD);
        autoSwitch = bool("Auto switch", "Find a block in your hotbar automatically", true);
        tower = bool("Tower", "Place a block above you while jumping", true);
        delay = integer("Delay", "Milliseconds between placements", 60, 0, 500);
        rotate = bool("Rotate", "Face the placement position", true);
        sneakOnly = bool("Sneak only", "Only build while holding sneak", false);
        switchBack = bool("Switch back", "Return to your old hotbar slot when done", true);
    }

    private final BooleanSetting autoSwitch;
    private final BooleanSetting tower;
    private final IntSetting delay;
    private final BooleanSetting rotate;
    private final BooleanSetting sneakOnly;
    private final BooleanSetting switchBack;

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
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }
        if (sneakOnly.get() && !mc.player.isShiftKeyDown()) {
            return;
        }
        if (mc.player.isInWater() || mc.player.isInLava() || mc.player.onClimbable()
                || mc.player.getAbilities().flying || mc.options.keyAttack.isDown()) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        if (autoSwitch.get()) {
            ensureHeld();
        }
        if (!isBlock(mc.player.getMainHandItem())) {
            return;
        }
        // The block under the feet is air both while bridging over a gap and
        // while rising in a jump; one placement covers both. The jump case is
        // gated on the tower toggle so bridging still works with tower off.
        BlockPos below = mc.player.blockPosition().below();
        if (!mc.level.getBlockState(below).isAir()) {
            return;
        }
        boolean jumping = mc.player.getDeltaMovement().y > 0.0
                || (mc.options.keyJump.isDown() && !mc.player.onGround());
        if (jumping && !tower.get()) {
            return;
        }
        placeAt(below);
    }

    private boolean placeAt(BlockPos target) {
        if (!mc.level.getBlockState(target).isAir()) {
            return false;
        }
        Vec3 centre = Vec3.atCenterOf(target);
        if (mc.player.getEyePosition().distanceTo(centre) > 4.5) {
            return false;
        }
        Direction face = faceToward(target);
        if (face == null) {
            return false;
        }
        if (rotate.get()) {
            Rotation.face(centre, true, 180f);
        }
        CombatUtils.placeBlock(target.relative(face), face.getOpposite());
        if (rotate.get()) {
            Rotation.restore();
        }
        return true;
    }

    /** The face of an adjacent solid block to click, or null when there is none. */
    private Direction faceToward(BlockPos target) {
        for (Direction direction : Direction.values()) {
            if (!mc.level.getBlockState(target.relative(direction)).isAir()) {
                return direction;
            }
        }
        return null;
    }

    private boolean isBlock(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem;
    }

    private void ensureHeld() {
        if (isBlock(mc.player.getMainHandItem())) {
            return;
        }
        int slot = bestBlockSlot();
        if (slot == -1) {
            return;
        }
        if (previousSlot == -1) {
            previousSlot = mc.player.getInventory().getSelectedSlot();
        }
        InventoryUtils.selectSlot(slot);
    }

    private int bestBlockSlot() {
        var inventory = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (isBlock(inventory.getItem(i))) {
                return i;
            }
        }
        return -1;
    }

    private void restoreSlot() {
        if (switchBack.get() && previousSlot != -1 && mc.player != null) {
            InventoryUtils.selectSlot(previousSlot);
        }
        previousSlot = -1;
    }

    @Override
    public String getArraylistSuffix() {
        if (mc.player == null) {
            return "";
        }
        return isBlock(mc.player.getMainHandItem()) ? "" : "no block";
    }
}
