package dev.sevennv.client.world;

import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.config.DoubleSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.WorldRenderUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws the text of nearby signs above them, so a sign can be read without
 * walking up to it. Only signs whose block entity is already loaded are read;
 * nothing is requested from the server.
 */
public final class SignReader extends Module {
    public SignReader() {
        super("Sign Reader", "Shows nearby sign text", Category.WORLD);
        range = number("Range", "Maximum read distance", 24.0, 2.0, 64.0, 2.0);
        color = color("Color", "Text colour", 0xFFFFE080);
        scan = integer("Scan", "Milliseconds between scans", 500, 100, 5000);
    }

    private final DoubleSetting range;
    private final ColorSetting color;
    private final dev.sevennv.client.config.IntSetting scan;

    private final List<BlockPos> signs = new ArrayList<>();
    private final dev.sevennv.client.utils.Timer timer = new dev.sevennv.client.utils.Timer();

    @Override
    public void onWorldChange() {
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
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = centre.offset(dx, dy, dz);
                    if (!mc.level.isLoaded(pos)) {
                        continue;
                    }
                    if (mc.level.getBlockEntity(pos) instanceof SignBlockEntity) {
                        signs.add(pos.immutable());
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
