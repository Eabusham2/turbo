package com.eyad.turbo.mixin.entity;

import com.eyad.turbo.config.TurboConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hide elytra wings. Verified target:
 *   WingsLayer#submit(PoseStack, SubmitNodeCollector, int, HumanoidRenderState, float, float)V
 * (explicit descriptor so the synthetic bridge method is not matched).
 */
@Mixin(WingsLayer.class)
public abstract class WingsLayerMixin {

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
            at = @At("HEAD"), cancellable = true)
    private void turbo$noWings(PoseStack pose, SubmitNodeCollector collector, int light, HumanoidRenderState state,
                               float yRot, float xRot, CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().cheapElytraRender) ci.cancel();
    }
}
