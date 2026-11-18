package dev.sevennv.client.world;

import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.config.IntSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.WorldRenderUtils;
import dev.sevennv.client.utils.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * Draws the text of nearby signs above them, so a sign can be read without
 * walking up to it. Only signs whose block entity is already loaded are read;
 * nothing is requested from the server. The scan is throttled and confined to a
 * vertical band around the player, because a full cube at range 24 would be
 * over a hundred thousand block lookups.
 */
public final class SignReader extends Module {
    private static final int VERTICAL_BAND = 8;

    public SignReader() {
        super("Sign Reader", "Shows nearby sign text", Category.WORLD);
        range = number("Range", "Maximum read distance", 16.0, 2.0, 48.0, 2.0);
        color = color("Color", "Text colour", 0xFFFFE080);
        scan = integer("Scan", "Milliseconds between scans", 500, 100, 5000);
    }

    private final DoubleSetting range;
    private final ColorSetting color;
    private final IntSetting scan;

    private final Set<BlockPos> signs = new HashSet<>();
    private final Timer timer = new Timer();

    @Override
    public void onWorldChange() {
        signs.clear();
    }

    @Override
    public void onDisable() {
        signs.clear();
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null || !timer.tick(scan.get())) {
            return;
        }
        signs.clear();
        int radius = (int) Math.ceil(range.get());
        BlockPos centre = mc.player.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -VERTICAL_BAND; dy <= VERTICAL_BAND; dy++) {
                    cursor.set(centre.getX() + dx, centre.getY() + dy, centre.getZ() + dz);
                    if (!mc.level.isLoaded(cursor)) {
                        continue;
                    }
                    if (mc.level.getBlockEntity(cursor) instanceof SignBlockEntity) {
                        signs.add(cursor.immutable());
                    }
                }
            }
        }
    }

    @Override
    public void onWorldRender() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        double rangeSq = range.get() * range.get();
        for (BlockPos pos : signs) {
            if (mc.player.position().distanceToSqr(Vec3.atCenterOf(pos)) > rangeSq) {
                continue;
            }
            if (!(mc.level.getBlockEntity(pos) instanceof SignBlockEntity sign)) {
                continue;
            }
            StringBuilder text = new StringBuilder();
            for (var message : sign.getFrontText().getMessages(false)) {
                String line = message.getString();
                if (!line.isBlank()) {
                    if (text.length() > 0) {
                        text.append(" | ");
                    }
                    text.append(line);
                }
            }
            if (text.length() > 0) {
                WorldRenderUtils.label(Vec3.atCenterOf(pos).add(0, 1.0, 0), text.toString(), color.get());
            }
        }
    }
}
