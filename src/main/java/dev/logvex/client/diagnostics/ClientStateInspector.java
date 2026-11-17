package dev.logvex.client.diagnostics;

import dev.logvex.client.config.BooleanSetting;
import dev.logvex.client.config.Theme;
import dev.logvex.client.module.Category;
import dev.logvex.client.module.Module;
import dev.logvex.client.utils.PacketLog;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Client state inspector: a single readout of the client's current state
 * (screen, world, connection, module count). Useful when something looks wrong
 * and you want one place to see what the client believes is true.
 */
public final class ClientStateInspector extends Module {
    public ClientStateInspector() {
        super("Client State Inspector", "Summarises the client's current state", Category.DIAGNOSTICS);
        showWorld = bool("World", "Show the current dimension and world state", true);
        showScreen = bool("Screen", "Show the open screen", true);
        showModules = bool("Modules", "Show the enabled module count", true);
        showPackets = bool("Packets", "Show the packet totals", true);
    }

    private final BooleanSetting showWorld;
    private final BooleanSetting showScreen;
    private final BooleanSetting showModules;
    private final BooleanSetting showPackets;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        DiagPanel panel = new DiagPanel().title("Client State");

        panel.row("Focused", mc.isWindowActive() ? "yes" : "no");
        panel.row("Paused", mc.isPaused() ? "yes" : "no");
        if (showScreen.get()) {
            panel.row("Screen", mc.screen == null ? "none" : mc.screen.getClass().getSimpleName());
        }
        if (showWorld.get()) {
            if (mc.level == null) {
                panel.row("World", "none");
            } else {
                panel.row("World", mc.level.dimension().identifier().getPath());
                panel.row("Local time", String.valueOf(mc.level.getDayTime() % 24000L));
            }
        }
        if (showModules.get()) {
            var modules = dev.logvex.client.LogvexClient.getModuleManager();
            panel.row("Modules", modules == null ? "-" : modules.getEnabledCount() + "/" + modules.getAll().size());
        }
        if (showPackets.get()) {
            panel.row("Packets", PacketLog.inboundCount() + " / " + PacketLog.outboundCount());
        }
        panel.render(graphics, theme, 4, 4);
    }

    private Theme theme() {
        var config = dev.logvex.client.LogvexClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
