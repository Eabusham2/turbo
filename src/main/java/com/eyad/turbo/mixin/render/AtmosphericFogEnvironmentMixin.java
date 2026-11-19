package com.eyad.turbo.mixin.render;

import com.eyad.turbo.config.TurboConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * No Nether / End fog. In 1.21.11 dimension fog comes from environment attributes inside
 * AtmosphericFogEnvironment#setupFog; we push its environmental fog out to "never" in those
 * dimensions only. The render-distance edge fade still applies.
 * Verified target: AtmosphericFogEnvironment#setupFog(FogData, Camera, ClientLevel, float, DeltaTracker)V
 */
@Mixin(AtmosphericFogEnvironment.class)
public abstract class AtmosphericFogEnvironmentMixin {

    @Inject(method = "setupFog", at = @At("TAIL"))
    private void turbo$noDimensionFog(FogData fog, Camera camera, ClientLevel level, float renderDistance,
                                      DeltaTracker delta, CallbackInfo ci) {
        if (!TurboConfig.on() || level == null) return;
        TurboConfig c = TurboConfig.get();
        boolean nether = c.skipNetherFogDensity && level.dimension() == Level.NETHER;
        boolean end = c.skipEndFogRender && level.dimension() == Level.END;
        if (nether || end) {
            fog.environmentalStart = Float.MAX_VALUE;
            fog.environmentalEnd = Float.MAX_VALUE;
        }
    }
}
