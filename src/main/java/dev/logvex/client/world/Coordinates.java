package dev.logvex.client.world;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.utils.MathUtils;
import net.minecraft.core.BlockPos;

/**
 * Adds precise coordinates to the array list and can draw a small world-space
 * marker at the player's feet. The HUD coordinates element is the primary
 * readout; this module exists for array-list and label use.
 */
public final class Coordinates extends Module {
    public Coordinates() {
        super("Coordinates", "Shows your exact coordinates", Category.WORLD);
        decimals = integer("Decimals", "Decimal places to show", 1, 0, 3);
        includeY = bool("Include Y", "Show the vertical coordinate", true);
        worldLabel = bool("World label", "Draw the coordinates below you", false);
        color = color("Color", "Label colour", 0xFFE0E0E0);
    }

    private final dev.logvex.client.config.IntSetting decimals;
    private final BooleanSetting includeY;
    private final BooleanSetting worldLabel;
    private final dev.logvex.client.config.ColorSetting color;

    @Override
    public void onWorldRender() {
        if (mc.player == null || !worldLabel.get()) {
            return;
        }
        dev.logvex.client.render.WorldRenderUtils.label(
                mc.player.position().add(0, -1.2, 0), text(), color.get());
    }

    @Override
    public String getArraylistSuffix() {
        return text();
    }

    private String text() {
        if (mc.player == null) {
            return "0, 0, 0";
        }
        String x = MathUtils.round(mc.player.getX(), decimals.get()) + "";
        String z = MathUtils.round(mc.player.getZ(), decimals.get()) + "";
        if (!includeY.get()) {
            return x + ", " + z;
        }
        return x + ", " + MathUtils.round(mc.player.getY(), decimals.get()) + ", " + z;
    }

    /** The block position under the player, for other world modules. */
    public BlockPos currentBlock() {
        return mc.player == null ? BlockPos.ZERO : mc.player.blockPosition();
    }
}
