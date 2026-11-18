package dev.sevennv.client.mixin;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.module.ModuleManager;
import dev.sevennv.client.render.DamageTint;
import dev.sevennv.client.render.NoRender;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Post-processing for the in-game HUD. Runs at the end of {@code Gui.render} so
 * it can recolour the damage vignette and, when configured, hide it. No vanilla
 * HUD element is removed; only the overlay colour is changed.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    public float vignetteBrightness;

    @Inject(method = "render", at = @At("TAIL"))
    private void sevennv$postProcess(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo info) {
        ModuleManager manager = SevennvClient.getModuleManager();
        if (manager == null || Minecraft.getInstance().options.hideGui) {
            return;
        }
        NoRender noRender = manager.get(NoRender.class);
        if (noRender != null && noRender.hideNausea() && Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.portalEffectIntensity = 0f;
            Minecraft.getInstance().player.oPortalEffectIntensity = 0f;
        }
        DamageTint tint = manager.get(DamageTint.class);
        if (tint == null) {
            return;
        }
        int color = tint.overlayColor();
        if (color == 0) {
            return;
        }
        // Scale the tint by the current vignette brightness so it tracks damage.
        float brightness = Math.max(0f, Math.min(1f, vignetteBrightness));
        int alpha = (int) (((color >>> 24) & 0xFF) * brightness);
        if (alpha <= 0) {
            return;
        }
        int rgba = (alpha << 24) | (color & 0x00FFFFFF);
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), rgba);
    }
}
