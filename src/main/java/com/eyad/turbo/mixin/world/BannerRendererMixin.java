package com.eyad.turbo.mixin.world;

import com.eyad.turbo.config.TurboConfig;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.state.BannerRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Banner look: no waving (phase drives the cloth swing) and an optional pattern-layer cap
 * (each layer is one more full banner quad drawn).
 * Verified target: BannerRenderer#extractRenderState(BannerBlockEntity, BannerRenderState, float, Vec3, CrumblingOverlay)V
 */
@Mixin(BannerRenderer.class)
public abstract class BannerRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/level/block/entity/BannerBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/BannerRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
            at = @At("TAIL"))
    private void turbo$banner(BannerBlockEntity be, BannerRenderState state, float partialTick, Vec3 camera,
                              ModelFeatureRenderer.CrumblingOverlay crumbling, CallbackInfo ci) {
        if (!TurboConfig.on()) return;
        TurboConfig c = TurboConfig.get();
        if (c.skipBannerWave) state.phase = 0.0F;
        int max = c.maxBannerLayers;
        if (max > 0 && state.patterns != null && state.patterns.layers().size() > max) {
            state.patterns = new BannerPatternLayers(List.copyOf(state.patterns.layers().subList(0, max)));
        }
    }
}
