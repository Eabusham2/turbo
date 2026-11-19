package com.eyad.turbo.mixin.render;

import com.eyad.turbo.config.TurboConfig;
import com.eyad.turbo.util.Effective;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Clouds and weather. In 1.21.11 these are separate frame-graph passes, so cancelling
 * the pass setup removes the whole pass (works even if another mod replaces the
 * CloudRenderer itself).
 * Verified targets:
 *   private addCloudsPass(FrameGraphBuilder, CloudStatus, Vec3, long, float, int, float)
 *   private addWeatherPass(FrameGraphBuilder, GpuBufferSlice)
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject(method = "addCloudsPass", at = @At("HEAD"), cancellable = true)
    private void turbo$noClouds(CallbackInfo ci) {
        if (TurboConfig.on() && Effective.cloudsOff()) ci.cancel();
    }

    @Inject(method = "addWeatherPass", at = @At("HEAD"), cancellable = true)
    private void turbo$noWeather(CallbackInfo ci) {
        if (TurboConfig.on() && Effective.weatherOff()) ci.cancel();
    }

    /** Verified target: private renderBlockDestroyAnimation(PoseStack, BufferSource, LevelRenderState)V (mining cracks). */
    @Inject(method = "renderBlockDestroyAnimation", at = @At("HEAD"), cancellable = true)
    private void turbo$noCracks(CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().disableBreakOverlay) ci.cancel();
    }
}
