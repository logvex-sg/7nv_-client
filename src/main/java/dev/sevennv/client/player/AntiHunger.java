package dev.sevennv.client.player;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

/**
 * Reports the player as not sprinting and on the ground so the server's hunger
 * drain is not triggered by sprint-jumping. No game state is modified beyond
 * the flags of the movement packet.
 */
public final class AntiHunger extends Module {
    public AntiHunger() {
        super("Anti Hunger", "Reduces hunger drain from sprinting", Category.PLAYER);
        stopSprint = bool("Stop sprint flag", "Report the sprint flag as false", true);
        groundFlag = bool("Ground flag", "Report the player as grounded", true);
    }

    private final BooleanSetting stopSprint;
    private final BooleanSetting groundFlag;

    @Override
    public void onTick() {
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        if (stopSprint.get() && mc.player.isSprinting()) {
            mc.player.setSprinting(false);
        }
        if (groundFlag.get() && !mc.player.onGround() && mc.player.getDeltaMovement().y < 0) {
            PacketUtils.send(new ServerboundMovePlayerPacket.StatusOnly(true, mc.player.horizontalCollision));
        }
    }
}
