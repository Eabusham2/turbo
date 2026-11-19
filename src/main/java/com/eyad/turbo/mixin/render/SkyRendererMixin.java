package com.eyad.turbo.mixin.render;

import com.eyad.turbo.config.TurboConfig;
import com.eyad.turbo.util.Effective;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sky pieces. Verified targets (1.21.11):
 *   private renderStars(float, PoseStack)
 *   public renderEndSky()
 */
@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {

    @Inject(method = "renderStars", at = @At("HEAD"), cancellable = true)
    private void turbo$noStars(float brightness, PoseStack pose, CallbackInfo ci) {
        if (TurboConfig.on() && Effective.starsOff()) ci.cancel();
    }

    @Inject(method = "renderEndSky", at = @At("HEAD"), cancellable = true)
    private void turbo$noEndSky(CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().disableEndSky) ci.cancel();
    }
}
