package com.eyad.turbo.mixin.render;

import com.eyad.turbo.config.TurboConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * First-person screen overlays (finalized from the old ScreenOverlay draft).
 * Verified targets (1.21.11):
 *   private static renderFire(PoseStack, MultiBufferSource, TextureAtlasSprite)
 *   private static renderWater(Minecraft, PoseStack, MultiBufferSource)
 *   private static renderTex(TextureAtlasSprite, PoseStack, MultiBufferSource)   - inside-a-block view
 *   private renderItemActivationAnimation(PoseStack, float, SubmitNodeCollector) - totem pop
 */
@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void turbo$noFire(PoseStack pose, MultiBufferSource buffers, TextureAtlasSprite sprite, CallbackInfo ci) {
        if (!TurboConfig.on()) return;
        TurboConfig c = TurboConfig.get();
        if (c.disableFireOverlay || c.noOverlays) ci.cancel();
    }

    @Inject(method = "renderWater", at = @At("HEAD"), cancellable = true)
    private static void turbo$noWater(Minecraft mc, PoseStack pose, MultiBufferSource buffers, CallbackInfo ci) {
        if (!TurboConfig.on()) return;
        TurboConfig c = TurboConfig.get();
        if (c.disableWaterOverlay || c.noOverlays) ci.cancel();
    }

    @Inject(method = "renderTex", at = @At("HEAD"), cancellable = true)
    private static void turbo$noInWall(TextureAtlasSprite sprite, PoseStack pose, MultiBufferSource buffers, CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().noOverlays) ci.cancel();
    }

    @Inject(method = "renderItemActivationAnimation", at = @At("HEAD"), cancellable = true)
    private void turbo$noTotem(PoseStack pose, float partialTick, SubmitNodeCollector collector, CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().disableTotemAnimation) ci.cancel();
    }
}
