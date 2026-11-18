package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/** Name of the block or entity the crosshair is currently over. */
public final class LookingAtHud extends HudElement {
    public LookingAtHud() {
        super("looking_at", "Looking At", 2, 188);
    }

    @Override
    public int getWidth() {
        return Math.max(RenderUtils.font().width(line()) + 4, 80);
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        drawLine(graphics, line(), 0xFFD0D0FF);
    }

    private String line() {
        var hit = HudData.mc().hitResult;
        if (hit instanceof BlockHitResult block) {
            var state = HudData.mc().level == null ? null : HudData.mc().level.getBlockState(block.getBlockPos());
            String name = state == null ? "Block" : state.getBlock().getName().getString();
            return "Block: " + name;
        }
        if (hit instanceof EntityHitResult entity) {
            return "Entity: " + entity.getEntity().getName().getString();
        }
        return "Looking: -";
    }
}
