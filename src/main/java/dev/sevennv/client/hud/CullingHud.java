package dev.sevennv.client.hud;

import dev.sevennv.client.render.EntityCulling;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

/** Shows how many entities the culling modules skipped this frame. */
public final class CullingHud extends HudElement {
    public CullingHud() {
        super("culling", "Culling", 110, 34);
    }

    @Override
    public int getWidth() {
        return 96;
    }

    @Override
    public int getHeight() {
        return 22;
    }

    @Override
    public void render(GuiGraphics graphics) {
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        int chunks = HudData.mc().options == null ? 0 : HudData.mc().options.renderDistance().get();
        RenderUtils.text(graphics, "Culled " + EntityCulling.culledThisFrame(), x + 2, y + 1, 0xFFE0E0E0);
        RenderUtils.text(graphics, "Total " + EntityCulling.culledTotal(), x + 2, y + 11, 0xFFA0A0A0);
        RenderUtils.text(graphics, "RD " + chunks, x + 60, y + 1, 0xFFE0E0E0);
    }
}
