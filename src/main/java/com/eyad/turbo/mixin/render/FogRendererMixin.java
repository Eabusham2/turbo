package com.eyad.turbo.mixin.render;

import com.eyad.turbo.config.TurboConfig;
import com.eyad.turbo.util.FogCull;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;

/**
 * Fog.
 *
 * Remove all fog: 1.21.11 has a built-in fog switch: when its static fogEnabled flag is
 * false, getBuffer(FogMode) returns emptyBuffer.slice(0, FOG_UBO_SIZE). We take that same
 * branch when the toggle is on, so the result is exactly the game's own no-fog path.
 *
 * Fog culling: setupFog writes the frame's fog values through updateBuffer(...) right before
 * the level (and its entities) is extracted. We record environmentalEnd there for FogCull.
 * The constructor also calls updateBuffer for the empty buffer with Float.MAX_VALUE, which
 * FogCull treats as "no fog".
 *
 * Verified targets: FogRenderer#getBuffer(FogRenderer$FogMode)GpuBufferSlice,
 *   private FogRenderer#updateBuffer(ByteBuffer, int, Vector4f, float x6)V
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Shadow @Final private GpuBuffer emptyBuffer;

    @Inject(method = "getBuffer", at = @At("HEAD"), cancellable = true)
    private void turbo$noFog(FogRenderer.FogMode mode, CallbackInfoReturnable<GpuBufferSlice> cir) {
        if (TurboConfig.on() && TurboConfig.get().disableFog) {
            cir.setReturnValue(this.emptyBuffer.slice(0L, FogRenderer.FOG_UBO_SIZE));
        }
    }

    @Inject(method = "updateBuffer", at = @At("HEAD"))
    private void turbo$recordFog(ByteBuffer buffer, int offset, Vector4f color,
                                 float environmentalStart, float environmentalEnd,
                                 float renderDistanceStart, float renderDistanceEnd,
                                 float skyEnd, float cloudEnd, CallbackInfo ci) {
        FogCull.record(environmentalEnd); // MAX_VALUE (no fog) disables fog culling
    }
}
