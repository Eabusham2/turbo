package com.eyad.turbo.mixin.render;

import com.eyad.turbo.config.TurboConfig;
import com.eyad.turbo.util.RenderStats;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Verified targets (1.21.11):
 *   bobHurt(PoseStack, float), bobView(PoseStack, float)
 *   renderLevel(DeltaTracker)  - the 3D world pass
 *
 * Render gate: vanilla Minecraft#runTick still calls GameRenderer#render -> renderLevel
 * every frame while minimized (it only skips the final blit). We skip the world pass
 * while minimized. renderLevel also calls pick(), which updates the crosshair target,
 * so we still call it to keep auto-clickers / AFK setups behaving exactly as before.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void turbo$noViewBob(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().disableViewBob) ci.cancel();
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void turbo$noHurtTilt(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().disableScreenShake) ci.cancel();
    }

    /** Verified target: GameRenderer#processBlurEffect()V (the menu background blur pass). */
    @Inject(method = "processBlurEffect", at = @At("HEAD"), cancellable = true)
    private void turbo$noBlur(CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().noPauseBlur) ci.cancel();
    }

    @Inject(method = "renderLevel", at = @At("HEAD"), cancellable = true)
    private void turbo$skipWhenMinimized(DeltaTracker deltaTracker, CallbackInfo ci) {
        RenderStats.newFrame();
        if (!TurboConfig.on() || !TurboConfig.get().skipRenderWhenMinimized) return;
        if (this.minecraft.getWindow() == null || !this.minecraft.getWindow().isMinimized()) return;
        // Keep the two non-drawing jobs renderLevel does, so game state stays identical:
        // the crosshair target (auto-clickers/AFK) and the client light update queue, which
        // LevelRenderer#renderLevel drains every frame and would otherwise pile up.
        ((GameRenderer) (Object) this).pick(deltaTracker.getGameTimeDeltaPartialTick(true));
        if (this.minecraft.level != null) {
            this.minecraft.level.pollLightUpdates();
            this.minecraft.level.getChunkSource().getLightEngine().runLightUpdates();
        }
        ci.cancel();
    }
}
