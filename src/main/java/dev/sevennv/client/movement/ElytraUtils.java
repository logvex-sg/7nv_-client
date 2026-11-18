package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.NotificationManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Elytra quality-of-life: automatic firework use while gliding, a glide
 * reminder when falling without an elytra deployed, and a gentle pitch assist.
 */
public final class ElytraUtils extends Module {
    public ElytraUtils() {
        super("Elytra Utilities", "Firework assist and glide helpers for elytra", Category.MOVEMENT);
        autoFirework = bool("Auto firework", "Use a firework while gliding", true);
        minSpeed = number("Min speed", "Use a firework when slower than this (b/s)", 15.0, 1.0, 40.0, 1.0);
        autoGlide = bool("Auto glide", "Deploy the elytra when falling", true);
        minFallDistance = number("Min fall", "Fall distance before auto glide", 3.0, 1.0, 20.0, 0.5);
        pitchAssist = bool("Pitch assist", "Flatten the glide pitch while descending", false);
    }

    private final BooleanSetting autoFirework;
    private final DoubleSetting minSpeed;
    private final BooleanSetting autoGlide;
    private final DoubleSetting minFallDistance;
    private final BooleanSetting pitchAssist;

    private long lastFirework;

    @Override
    public void onWorldChange() {
        lastFirework = 0L;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (mc.player.isFallFlying()) {
            if (autoFirework.get()) {
                tickFirework();
            }
            if (pitchAssist.get()) {
                tickPitch();
            }
            return;
        }
        if (autoGlide.get() && !mc.player.onGround() && mc.player.fallDistance >= minFallDistance.get()
                && hasElytra()) {
            if (mc.player.tryToStartFallFlying()) {
                NotificationManager.info("Elytra", "Deployed elytra");
            }
        }
    }

    private void tickFirework() {
        long now = System.currentTimeMillis();
        if (now - lastFirework < 1000L) {
            return;
        }
        double speed = mc.player.getDeltaMovement().length() * 20.0;
        if (speed >= minSpeed.get()) {
            return;
        }
        if (!mc.player.getOffhandItem().is(Items.FIREWORK_ROCKET)) {
            int slot = dev.sevennv.client.utils.InventoryUtils.findHotbarSlot(Items.FIREWORK_ROCKET);
            if (slot == -1) {
                return;
            }
            dev.sevennv.client.utils.InventoryUtils.selectSlot(slot);
        }
        // Right-click with the rocket in hand, which is the vanilla boost.
        mc.gameMode.useItem(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND);
        lastFirework = now;
    }

    private void tickPitch() {
        if (mc.player.getXRot() > 20.0f) {
            mc.player.setXRot(20.0f);
        } else if (mc.player.getXRot() < -60.0f) {
            mc.player.setXRot(-60.0f);
        }
    }

    private boolean hasElytra() {
        return mc.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA);
    }

    @Override
    public String getArraylistSuffix() {
        return mc.player != null && mc.player.isFallFlying() ? "gliding" : "";
    }
}
