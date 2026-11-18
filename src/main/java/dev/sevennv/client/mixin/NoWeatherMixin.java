package dev.sevennv.client.mixin;

import dev.sevennv.client.SevennvClient;
import dev.sevennv.client.render.NoWeather;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.WeatherRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drops the rain and snow columns the weather renderer collected this frame
 * when the No Weather module asks for it. The weather simulation itself is
 * untouched; only the client's own particle pass is skipped.
 */
@Mixin(WeatherEffectRenderer.class)
public abstract class NoWeatherMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void sevennv$hideWeather(Level level, int ticks, float partialTick, Vec3 cameraPos,
                                     WeatherRenderState state, CallbackInfo ci) {
        NoWeather module = SevennvClient.getModuleManager() == null
                ? null : SevennvClient.getModuleManager().get(NoWeather.class);
        if (module == null || !module.hidesAnything()) {
            return;
        }
        if (module.hideRain()) {
            state.rainColumns.clear();
        }
        if (module.hideSnow()) {
            state.snowColumns.clear();
        }
    }
}
