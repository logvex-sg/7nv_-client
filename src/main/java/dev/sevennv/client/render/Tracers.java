package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.world.entity.player.Player;

/**
 * Draws a line from the crosshair to every nearby player, coloured by friend
 * status.
 */
public final class Tracers extends Module {
    public Tracers() {
        super("Tracers", "Draws lines from you to nearby players", Category.RENDER);
        range = number("Range", "Maximum tracer distance", 64.0, 4.0, 256.0, 4.0);
        playerColor = color("Player color", "Tracer colour for players", 0xFFFF5050);
        friendColor = color("Friend color", "Tracer colour for friends", 0xFF50FF50);
        lineWidth = number("Line width", "Stroke thickness", 1.0, 0.5, 4.0, 0.5);
        fromEyes = bool("From eyes", "Start the tracer at eye level", true);
    }

    private final DoubleSetting range;
    private final ColorSetting playerColor;
    private final ColorSetting friendColor;
    private final DoubleSetting lineWidth;
    private final BooleanSetting fromEyes;

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        for (Player player : mc.level.players()) {
            if (player == mc.player || !player.isAlive() || mc.player.distanceToSqr(player) > rangeSq) {
                continue;
            }
            int color = dev.sevennv.client.utils.FriendManager.isFriend(player.getGameProfile().name())
                    ? friendColor.get() : playerColor.get();
            var target = WorldRenderUtils.entityCenter(player);
            if (fromEyes.get()) {
                var start = mc.player.getEyePosition();
                WorldRenderUtils.lineThroughWalls(start, target, color, (float) lineWidth.get());
            } else {
                WorldRenderUtils.tracer(target, color);
            }
        }
    }
}
