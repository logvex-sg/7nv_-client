package dev.sevennv.client.events;

import net.minecraft.client.gui.GuiGraphics;

/** Fired once per frame after the vanilla HUD has been drawn. */
public class HudRenderEvent {
    private final GuiGraphics graphics;
    private final float partialTick;

    public HudRenderEvent(GuiGraphics graphics, float partialTick) {
        this.graphics = graphics;
        this.partialTick = partialTick;
    }

    public GuiGraphics getGraphics() {
        return graphics;
    }

    public float getPartialTick() {
        return partialTick;
    }
}
