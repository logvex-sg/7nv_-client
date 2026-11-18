package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

/**
 * Prevents fall damage by reporting the player as grounded while falling.
 *
 * <p>Only the "on ground" flag of the movement packet is affected; the position
 * is the player's real position, so no teleport is involved.
 */
public final class NoFall extends Module {
    public NoFall() {
        super("No Fall", "Prevents fall damage", Category.MOVEMENT);
        minDistance = number("Min fall", "Only act after falling this many blocks", 3.0, 1.0, 32.0, 0.5);
        onlyAirborne = bool("Only airborne", "Skip while standing on the ground", true);
        mode = option("Mode", "How the ground flag is reported", Mode.PACKET);
    }

    public enum Mode {
        PACKET,
        ALWAYS
    }

    private final DoubleSetting minDistance;
    private final BooleanSetting onlyAirborne;
    private final dev.sevennv.client.config.EnumSetting<Mode> mode;

    @Override
    public void onTick() {
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        if (onlyAirborne.get() && mc.player.onGround()) {
            return;
        }
        if (mc.player.fallDistance < minDistance.get() && mode.get() == Mode.PACKET) {
            return;
        }
        PacketUtils.send(new ServerboundMovePlayerPacket.StatusOnly(true, mc.player.horizontalCollision));
        mc.player.fallDistance = 0f;
    }
}
