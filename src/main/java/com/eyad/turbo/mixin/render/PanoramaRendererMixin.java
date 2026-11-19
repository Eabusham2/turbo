package com.eyad.turbo.mixin.render;

import com.eyad.turbo.config.TurboConfig;
import net.minecraft.client.renderer.PanoramaRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** No title-screen panorama. Verified target: PanoramaRenderer#render(GuiGraphics, int, int, boolean)V */
@Mixin(PanoramaRenderer.class)
public abstract class PanoramaRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void turbo$noPanorama(CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().skipBackgroundPanorama) ci.cancel();
    }
}
