package dev.sevennv.client.render;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.ColorSetting;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Replaces the vanilla crosshair with a flat, configurable one. Drawn in GUI
 * space so it stays crisp at every GUI scale.
 */
public final class CustomCrosshair extends Module {
    public CustomCrosshair() {
        super("Custom Crosshair", "Replaces the vanilla crosshair", Category.RENDER);
        style = option("Style", "Crosshair shape", Style.CROSS);
        color = color("Color", "Crosshair colour", 0xFFFFFFFF);
        size = number("Size", "Arm length in pixels", 5.0, 2.0, 20.0, 1.0);
        gap = number("Gap", "Centre gap in pixels", 2.0, 0.0, 8.0, 1.0);
        thickness = number("Thickness", "Line thickness in pixels", 1.0, 1.0, 4.0, 1.0);
        outline = bool("Outline", "Draw a dark outline for contrast", true);
        hideVanilla = bool("Hide vanilla", "Hide the vanilla crosshair", true);
    }

    public enum Style {
        CROSS, DOT, CIRCLE, DIAMOND
    }

    private final dev.sevennv.client.config.EnumSetting<Style> style;
    private final ColorSetting color;
    private final dev.sevennv.client.config.DoubleSetting size;
    private final dev.sevennv.client.config.DoubleSetting gap;
    private final dev.sevennv.client.config.DoubleSetting thickness;
    private final BooleanSetting outline;
    private final BooleanSetting hideVanilla;

    /** True when the vanilla crosshair should be suppressed this frame. */
    public boolean shouldHideVanilla() {
        return hideVanilla.get();
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        int centreX = graphics.guiWidth() / 2;
        int centreY = graphics.guiHeight() / 2;
        int armLength = (int) size.get();
        int gapPixels = (int) gap.get();
        int lineThickness = (int) thickness.get();
        int primary = color.get();
        int outlineColor = 0xC0000000;

        if (outline.get()) {
            drawShape(graphics, centreX, centreY, armLength, gapPixels, lineThickness + 1, outlineColor);
        }
        drawShape(graphics, centreX, centreY, armLength, gapPixels, lineThickness, primary);
    }

    private void drawShape(GuiGraphics graphics, int centreX, int centreY, int armLength, int gapPixels,
                           int lineThickness, int drawColor) {
        switch (style.get()) {
            case CROSS -> {
                RenderUtils.rect(graphics, centreX - gapPixels - armLength, centreY - lineThickness / 2,
                        armLength, lineThickness, drawColor);
                RenderUtils.rect(graphics, centreX + gapPixels, centreY - lineThickness / 2,
                        armLength, lineThickness, drawColor);
                RenderUtils.rect(graphics, centreX - lineThickness / 2, centreY - gapPixels - armLength,
                        lineThickness, armLength, drawColor);
                RenderUtils.rect(graphics, centreX - lineThickness / 2, centreY + gapPixels,
                        lineThickness, armLength, drawColor);
            }
            case DOT -> RenderUtils.rect(graphics, centreX - lineThickness / 2, centreY - lineThickness / 2,
                    lineThickness, lineThickness, drawColor);
            case CIRCLE -> drawCircle(graphics, centreX, centreY, armLength, lineThickness, drawColor);
            case DIAMOND -> {
                for (int offset = -armLength; offset <= armLength; offset++) {
                    int distance = armLength - Math.abs(offset);
                    RenderUtils.rect(graphics, centreX + offset, centreY - distance, 1, lineThickness, drawColor);
                    RenderUtils.rect(graphics, centreX + offset, centreY + distance, 1, lineThickness, drawColor);
                }
            }
        }
    }

    private void drawCircle(GuiGraphics graphics, int centreX, int centreY, int radius, int lineThickness, int drawColor) {
        int samples = Math.max(24, radius * 8);
        for (int index = 0; index < samples; index++) {
            double angle = (2.0 * Math.PI * index) / samples;
            int px = centreX + (int) Math.round(Math.cos(angle) * radius);
            int py = centreY + (int) Math.round(Math.sin(angle) * radius);
            RenderUtils.rect(graphics, px, py, lineThickness, lineThickness, drawColor);
        }
    }

    /** Crosshair tint that turns red when a living target is under the crosshair. */
    public int crosshairColor() {
        if (mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity living && !(living instanceof Player player && player == mc.player)) {
            return 0xFFFF5050;
        }
        return color.get();
    }
}
