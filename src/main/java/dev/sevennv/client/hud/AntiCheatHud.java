package dev.sevennv.client.hud;

import dev.sevennv.client.render.RenderUtils;
import dev.sevennv.client.utils.ServerFingerprint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Locale;

/** The identified anti-cheat and its confidence, as a movable HUD line. */
public final class AntiCheatHud extends HudElement {
    public AntiCheatHud() {
        super("anticheat", "Anti-cheat", 110, 90);
    }

    @Override
    public int getWidth() {
        return Math.max(RenderUtils.font().width(line()) + 4, 96);
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics) {
        RenderUtils.rect(graphics, x, y, getWidth(), getHeight(), background());
        RenderUtils.text(graphics, line(), x + 2, y + 1, color());
    }

    private String line() {
        if (Minecraft.getInstance().getConnection() == null) {
            return "AC offline";
        }
        ServerFingerprint.Match match = ServerFingerprint.bestAntiCheat();
        if (match == null) {
            return "AC " + (ServerFingerprint.hasSignals() ? "none" : "...");
        }
        return "AC " + match.name() + " " + Math.round(match.confidence() * 100) + "%";
    }

    private int color() {
        ServerFingerprint.Match match = ServerFingerprint.bestAntiCheat();
        if (match == null) {
            return 0xFFA0A0A0;
        }
        if (match.confidence() >= 0.75) {
            return 0xFFFF5555;
        }
        if (match.confidence() >= 0.5) {
            return 0xFFFFFF55;
        }
        return 0xFF55FF55;
    }
}
