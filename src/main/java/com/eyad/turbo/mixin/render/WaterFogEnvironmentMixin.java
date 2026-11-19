package com.eyad.turbo.mixin.render;

import com.eyad.turbo.config.TurboConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * No underwater fog: after vanilla fills the water fog values, push the environmental,
 * sky and cloud fog out to "never" (the same Float.MAX_VALUE vanilla uses for no fog).
 * The normal render-distance edge fade still applies.
 * Verified target: WaterFogEnvironment#setupFog(FogData, Camera, ClientLevel, float, DeltaTracker)V
 */
@Mixin(WaterFogEnvironment.class)
public abstract class WaterFogEnvironmentMixin {

    @Inject(method = "setupFog", at = @At("TAIL"))
    private void turbo$noWaterFog(FogData fog, Camera camera, ClientLevel level, float renderDistance,
                                  DeltaTracker delta, CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().cheapWaterFogRender) {
            fog.environmentalStart = Float.MAX_VALUE;
            fog.environmentalEnd = Float.MAX_VALUE;
            fog.skyEnd = Float.MAX_VALUE;
            fog.cloudEnd = Float.MAX_VALUE;
        }
    }
}
