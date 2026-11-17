package dev.logvex.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * Flat 2D drawing helpers for the ClickGUI and the HUD.
 *
 * <p>Everything is intentionally untextured and unshadowed: the 1.8 look is
 * solid rectangles, 1px borders and plain text.
 */
public final class RenderUtils {
    private RenderUtils() {
    }

    public static Font font() {
        return Minecraft.getInstance().font;
    }

    /** Filled rectangle. */
    public static void rect(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, color);
    }

    /** 1px outline drawn inside the given bounds. */
    public static void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y + 1, x + 1, y + height - 1, color);
        graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    /** Rectangle with a 1px border of a second colour. */
    public static void panel(GuiGraphics graphics, int x, int y, int width, int height, int fill, int border) {
        rect(graphics, x, y, width, height, fill);
        outline(graphics, x, y, width, height, border);
    }

    /** Vertical gradient from {@code top} to {@code bottom}. */
    public static void gradient(GuiGraphics graphics, int x, int y, int width, int height, int top, int bottom) {
        graphics.fillGradient(x, y, x + width, y + height, top, bottom);
    }

    public static void text(GuiGraphics graphics, String value, int x, int y, int color) {
        graphics.drawString(font(), value, x, y, color, false);
    }

    public static void text(GuiGraphics graphics, Component value, int x, int y, int color) {
        graphics.drawString(font(), value, x, y, color, false);
    }

    public static void text(GuiGraphics graphics, FormattedCharSequence value, int x, int y, int color) {
        graphics.drawString(font(), value, x, y, color, false);
    }

    /** Centred text within a box. */
    public static void textCentered(GuiGraphics graphics, String value, int centerX, int y, int color) {
        graphics.drawString(font(), value, centerX - font().width(value) / 2, y, color, false);
    }

    public static void textRight(GuiGraphics graphics, String value, int right, int y, int color) {
        graphics.drawString(font(), value, right - font().width(value), y, color, false);
    }

    /** Vertically centres a line of text inside a row. */
    public static int centeredY(int top, int height) {
        return top + (height - 8) / 2;
    }

    /** Mixes two ARGB colours, {@code t} in 0..1. */
    public static int mix(int from, int to, float t) {
        int a = (int) (alpha(from) + (alpha(to) - alpha(from)) * t);
        int r = (int) (red(from) + (red(to) - red(from)) * t);
        int g = (int) (green(from) + (green(to) - green(from)) * t);
        int b = (int) (blue(from) + (blue(to) - blue(from)) * t);
        return argb(a, r, g, b);
    }

    public static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public static int scaleAlpha(int color, float factor) {
        int a = (int) (alpha(color) * factor);
        return withAlpha(color, Math.max(0, Math.min(255, a)));
    }

    public static int alpha(int color) {
        return (color >> 24) & 0xFF;
    }

    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    /** Full-saturation hue colour, used by the array list rainbow mode. */
    public static int rainbow(int index, int speed, int alpha) {
        float hue = ((System.currentTimeMillis() / (long) speed + index * 20L) % 3600L) / 3600f;
        return hsb(hue, 0.65f, 1.0f, alpha);
    }

    public static int hsb(float hue, float saturation, float brightness, int alpha) {
        int rgb = java.awt.Color.HSBtoRGB(hue, saturation, brightness);
        return withAlpha(rgb, alpha);
    }

    public static boolean hovered(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
