package com.eyad.turbo.mixin.entity;

import com.eyad.turbo.config.TurboConfig;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.client.renderer.entity.state.GuardianRenderState;
import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * No guardian laser: GuardianRenderer#submit only draws the beam when
 * state.attackTargetPosition != null (vanilla sets it to null when there is no target).
 * Verified target: GuardianRenderer#extractRenderState(Guardian, GuardianRenderState, float)V
 */
@Mixin(GuardianRenderer.class)
public abstract class GuardianRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/Guardian;Lnet/minecraft/client/renderer/entity/state/GuardianRenderState;F)V",
            at = @At("TAIL"))
    private void turbo$noBeam(Guardian guardian, GuardianRenderState state, float partialTick, CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().skipGuardianBeamRender) state.attackTargetPosition = null;
    }
}
