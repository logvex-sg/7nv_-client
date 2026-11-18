package dev.sevennv.client.gui;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.config.ConfigManager;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.hud.HudElement;
import dev.sevennv.client.hud.HudManager;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Layout editor for the HUD: drag any element to move it, right-click to
 * toggle it. Positions are written straight to the config, so the layout
 * survives a restart.
 */
public final class HudEditorScreen extends Screen {
    private HudElement dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public HudEditorScreen() {
        super(Component.literal("7nv_ HUD Editor"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Theme theme = theme();
        RenderUtils.rect(graphics, 0, 0, width, height, RenderUtils.withAlpha(theme.background, 0xB0));

        HudManager hud = SevennvClient.getHudManager();
        if (hud != null) {
            hud.renderEditor(graphics, theme.text, theme.border, dragging);
        }

        RenderUtils.rect(graphics, 0, height - 14, width, 14, theme.panel);
        RenderUtils.text(graphics,
                "Drag to move  \u2022  Right-click to toggle  \u2022  Escape to save and close",
                6, height - 11, theme.textDim);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        HudManager hud = SevennvClient.getHudManager();
        if (hud == null) {
            return super.mouseClicked(event, doubleClick);
        }
        HudElement element = hud.elementAt(event.x(), event.y());
        if (element == null) {
            return super.mouseClicked(event, doubleClick);
        }
        if (event.button() == 0) {
            dragging = element;
            dragOffsetX = (int) event.x() - element.getX();
            dragOffsetY = (int) event.y() - element.getY();
            return true;
        }
        if (event.button() == 1) {
            element.setEnabled(!element.isEnabled());
            ConfigManager config = SevennvClient.getConfigManager();
            if (config != null) {
                config.markDirty();
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging == null) {
            return super.mouseDragged(event, dragX, dragY);
        }
        dragging.setPosition((int) event.x() - dragOffsetX, (int) event.y() - dragOffsetY);
        HudManager.clamp(dragging, width, height);
        return true;
    }

    @Override
    public void onClose() {
        HudManager hud = SevennvClient.getHudManager();
        if (hud != null) {
            for (HudElement element : hud.getElements()) {
                HudManager.clamp(element, width, height);
            }
        }
        ConfigManager config = SevennvClient.getConfigManager();
        if (config != null) {
            config.markDirty();
            config.save();
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Theme theme() {
        ConfigManager config = SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
