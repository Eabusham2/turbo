package com.eyad.turbo.mixin.client;

import com.eyad.turbo.config.TurboConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * HUD overlay skips. All targets verified against 1.21.11 Mojang mappings:
 *   renderVignette(GuiGraphics, Entity)
 *   renderSpyglassOverlay(GuiGraphics, float)
 *   displayScoreboardSidebar(GuiGraphics, Objective)
 *   renderTextureOverlay(GuiGraphics, Identifier, float)  - pumpkin blur + powder snow
 *   renderPortalOverlay(GuiGraphics, float)
 *   renderConfusionOverlay(GuiGraphics, float)            - nausea
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
    private void turbo$vignette(GuiGraphics g, Entity e, CallbackInfo ci) {
        if (!TurboConfig.on()) return;
        TurboConfig c = TurboConfig.get();
        if (c.skipVignetteRender || c.noOverlays) ci.cancel();
    }

    @Inject(method = "renderSpyglassOverlay", at = @At("HEAD"), cancellable = true)
    private void turbo$spyglass(GuiGraphics g, float scale, CallbackInfo ci) {
        if (!TurboConfig.on()) return;
        TurboConfig c = TurboConfig.get();
        if (c.disableSpyglassOverlay || c.noOverlays) ci.cancel();
    }

    @Inject(method = "displayScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void turbo$scoreboard(GuiGraphics g, Objective objective, CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().disableScoreboard) ci.cancel();
    }

    @Inject(method = "renderTextureOverlay", at = @At("HEAD"), cancellable = true)
    private void turbo$textureOverlay(GuiGraphics g, Identifier texture, float alpha, CallbackInfo ci) {
        if (!TurboConfig.on()) return;
        TurboConfig c = TurboConfig.get();
        if (c.noOverlays) { ci.cancel(); return; }
        boolean powderSnow = texture.getPath().contains("powder_snow");
        if (powderSnow ? c.disablePowderSnowOverlay : c.disablePumpkinOverlay) ci.cancel();
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void turbo$portal(GuiGraphics g, float alpha, CallbackInfo ci) {
        if (!TurboConfig.on()) return;
        TurboConfig c = TurboConfig.get();
        if (c.disablePortalDistortion || c.noOverlays) ci.cancel();
    }

    @Inject(method = "renderConfusionOverlay", at = @At("HEAD"), cancellable = true)
    private void turbo$nausea(GuiGraphics g, float strength, CallbackInfo ci) {
        if (!TurboConfig.on()) return;
        TurboConfig c = TurboConfig.get();
        if (c.disablePortalDistortion || c.noOverlays) ci.cancel();
    }

    /** Verified target: Gui#renderVehicleHealth(GuiGraphics)V. Hidden only while the mount is at full health. */
    @Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true)
    private void turbo$mountHealth(GuiGraphics g, CallbackInfo ci) {
        if (!TurboConfig.on() || !TurboConfig.get().skipMountHealthRender) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof LivingEntity mount
                && mount.getHealth() >= mount.getMaxHealth()) {
            ci.cancel();
        }
    }
}
