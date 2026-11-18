package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the predicted flight path of arrows and thrown pearls, using the same
 * gravity and drag the game applies. This is a read-only visual aid: nothing is
 * simulated on the server.
 */
public final class Trajectories extends Module {
    public Trajectories() {
        super("Trajectories", "Predicts the path of arrows and pearls", Category.RENDER);
        steps = integer("Steps", "Number of simulated steps", 80, 10, 300);
        onlyOwn = bool("Only own", "Only predict your own projectiles", false);
    }

    private final IntSetting steps;
    private final BooleanSetting onlyOwn;

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        for (Entity entity : mc.level.entitiesForRendering()) {
            boolean arrow = entity instanceof AbstractArrow;
            boolean pearl = entity instanceof ThrownEnderpearl;
            if (!arrow && !pearl) {
                continue;
            }
            if (onlyOwn.get() && ownerOf(entity) != mc.player) {
                continue;
            }
            predict(entity, arrow);
        }
    }

    private static Entity ownerOf(Entity entity) {
        return entity instanceof net.minecraft.world.entity.projectile.Projectile projectile
                ? projectile.getOwner() : null;
    }

    private void predict(Entity entity, boolean arrow) {
        double gravity = arrow ? 0.05 : 0.03;
        double drag = arrow ? 0.99 : 0.99;
        Vec3 position = entity.position();
        Vec3 velocity = entity.getDeltaMovement();
        int color = arrow ? 0xFF55FF55 : 0xFFAA66FF;
        for (int i = 0; i < steps.get(); i++) {
            Vec3 next = position.add(velocity);
            WorldRenderUtils.line(position, next, color, 1.5f);
            velocity = velocity.scale(drag).subtract(0.0, gravity, 0.0);
            position = next;
            net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.containing(position);
            if (mc.level.getBlockState(pos).isCollisionShapeFullBlock(mc.level, pos)) {
                break;
            }
        }
    }
}
