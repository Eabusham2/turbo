package com.eyad.turbo.mixin.world;

import com.eyad.turbo.config.TurboConfig;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Chest lids don't animate: the lid angle comes only from ChestRenderState.open.
 * Opening a chest still opens its menu; only the drawn lid stays shut.
 * Verified target: ChestRenderer#extractRenderState(BlockEntity, ChestRenderState, float, Vec3, CrumblingOverlay)V
 */
@Mixin(ChestRenderer.class)
public abstract class ChestRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
            at = @At("TAIL"))
    private void turbo$noLid(BlockEntity be, ChestRenderState state, float partialTick, Vec3 camera,
                             ModelFeatureRenderer.CrumblingOverlay crumbling, CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().disableChestAnim) state.open = 0.0F;
    }
}
