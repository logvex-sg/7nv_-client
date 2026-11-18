package dev.sevennv.client.diagnostics;

import dev.sevennv.client.config.BooleanSetting;
import dev.sevennv.client.config.Theme;
import dev.sevennv.client.module.Category;
import dev.sevennv.client.module.Module;
import dev.sevennv.client.render.RenderUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;

/**
 * Read-only protocol summary: game version, protocol number, server brand and
 * the negotiated chunk radius. Everything comes from data the client already
 * holds after login; no extra traffic is generated.
 */
public final class ProtocolInformation extends Module {
    public ProtocolInformation() {
        super("Protocol Information", "Shows version and protocol details", Category.DIAGNOSTICS);
        showBrand = bool("Server brand", "Show the server brand string", true);
        showVersion = bool("Version", "Show the game and protocol version", true);
        showAddress = bool("Address", "Show the remote address", false);
    }

    private final BooleanSetting showBrand;
    private final BooleanSetting showVersion;
    private final BooleanSetting showAddress;

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Theme theme = theme();
        int rows = 2 + (showVersion.get() ? 1 : 0) + (showBrand.get() ? 1 : 0) + (showAddress.get() ? 1 : 0);
        int width = 168;
        int height = 14 + rows * 9;
        int x = 4;
        int y = graphics.guiHeight() / 2 - height / 2;

        RenderUtils.rect(graphics, x, y, width, height, theme.panel);
        RenderUtils.outline(graphics, x, y, width, height, theme.border);
        RenderUtils.text(graphics, "Protocol Information", x + 3, y + 3, theme.accent);

        int rowY = y + 14;
        if (showVersion.get()) {
            rowY = line(graphics, theme, x, rowY, width, "Game", SharedConstants.getCurrentVersion().name());
            rowY = line(graphics, theme, x, rowY, width, "Protocol", String.valueOf(SharedConstants.getCurrentVersion().protocolVersion()));
        }
        if (showBrand.get()) {
            rowY = line(graphics, theme, x, rowY, width, "Brand", brand());
        }
        if (showAddress.get()) {
            line(graphics, theme, x, rowY, width, "Address", remoteAddress());
        }
    }

    private String brand() {
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) {
            return "-";
        }
        String brand = connection.serverBrand();
        return brand == null || brand.isBlank() ? "-" : brand;
    }

    private String remoteAddress() {
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) {
            return "-";
        }
        var address = connection.getConnection().getRemoteAddress();
        return address == null ? "-" : address.toString();
    }

    private int line(GuiGraphics graphics, Theme theme, int x, int rowY, int width, String label, String value) {
        RenderUtils.text(graphics, label, x + 3, rowY, theme.textDim);
        RenderUtils.textRight(graphics, trim(value, width / 2 + 30), x + width - 3, rowY, theme.text);
        return rowY + 9;
    }

    private static String trim(String value, int maxWidth) {
        while (RenderUtils.font().width(value) > maxWidth && value.length() > 3) {
            value = value.substring(0, value.length() - 2) + "\u2026";
        }
        return value;
    }

    private Theme theme() {
        var config = dev.sevennv.client.SevennvClient.getConfigManager();
        return config == null ? new Theme() : config.getTheme();
    }
}
