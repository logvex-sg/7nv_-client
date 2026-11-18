package dev.sevennv.client.movement;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * Climbs walls like a spider: while pressing into a wall the player is lifted
 * steadily. Movement is applied through the normal delta-movement path, so the
 * server receives ordinary position updates.
 */
public final class Spider extends Module {
    public Spider() {
        super("Spider", "Climbs up walls while pushing into them", Category.MOVEMENT);
        climbSpeed = number("Climb speed", "Upward velocity while climbing", 0.2, 0.05, 0.6, 0.05);
        onlyOnWall = bool("Only on wall", "Require a horizontal collision", true);
        delay = integer("Delay", "Milliseconds between boosts", 0, 0, 500);
    }

    private final DoubleSetting climbSpeed;
    private final BooleanSetting onlyOnWall;
    private final IntSetting delay;

    private final Timer timer = new Timer();

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (mc.player.isInWater() || mc.player.isFallFlying()) {
            return;
        }
        if (!mc.player.horizontalCollision && onlyOnWall.get()) {
            return;
        }
        if (!isFacingWall()) {
            return;
        }
        if (!timer.tick(delay.get())) {
            return;
        }
        Vec3 motion = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(motion.x, Math.max(motion.y, climbSpeed.get()), motion.z);
    }

    /** True when a solid block is directly in front of the player. */
    private boolean isFacingWall() {
        Direction facing = Direction.fromYRot(mc.player.getYRot());
        BlockPos ahead = mc.player.blockPosition().relative(facing);
        if (!mc.level.getBlockState(ahead).getCollisionShape(mc.level, ahead).isEmpty()) {
            return true;
        }
        // Diagonal travel can hit a corner; check the adjacent faces too.
        BlockPos north = mc.player.blockPosition().north();
        BlockPos south = mc.player.blockPosition().south();
        BlockPos east = mc.player.blockPosition().east();
        BlockPos west = mc.player.blockPosition().west();
        return mc.player.horizontalCollision
                && (!mc.level.getBlockState(north).getCollisionShape(mc.level, north).isEmpty()
                || !mc.level.getBlockState(south).getCollisionShape(mc.level, south).isEmpty()
                || !mc.level.getBlockState(east).getCollisionShape(mc.level, east).isEmpty()
                || !mc.level.getBlockState(west).getCollisionShape(mc.level, west).isEmpty());
    }
}
