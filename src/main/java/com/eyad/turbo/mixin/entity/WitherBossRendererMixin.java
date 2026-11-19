package com.eyad.turbo.mixin.entity;

import com.eyad.turbo.config.TurboConfig;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.client.renderer.entity.state.WitherRenderState;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * No wither armor glow: WitherArmorLayer draws only when state.isPowered.
 * Verified target: WitherBossRenderer#extractRenderState(WitherBoss, WitherRenderState, float)V
 */
@Mixin(WitherBossRenderer.class)
public abstract class WitherBossRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/boss/wither/WitherBoss;Lnet/minecraft/client/renderer/entity/state/WitherRenderState;F)V",
            at = @At("TAIL"))
    private void turbo$noArmorGlow(WitherBoss wither, WitherRenderState state, float partialTick, CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().skipWitherShieldRender) state.isPowered = false;
    }
}
