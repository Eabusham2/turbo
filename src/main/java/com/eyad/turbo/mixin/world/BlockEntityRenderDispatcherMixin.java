package com.eyad.turbo.mixin.world;

import com.eyad.turbo.config.TurboConfig;
import com.eyad.turbo.util.Effective;
import com.eyad.turbo.util.FogCull;
import com.eyad.turbo.util.Occlusion;
import com.eyad.turbo.util.RenderStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ONE block-entity cull hook replacing the ~16 per-renderer mixins from the old build
 * (block entity renderers no longer have render(...) in 1.21.9+; they extract + submit).
 *
 * tryExtractRenderState already returns null for block entities that should not draw
 * (no renderer, invalid state, outside the renderer's own view distance), and every
 * caller handles null. We return null for the extra cases below. Render only.
 *
 * Verified target: BlockEntityRenderDispatcher#tryExtractRenderState(BlockEntity, float,
 *   ModelFeatureRenderer$CrumblingOverlay)BlockEntityRenderState, field cameraPos: Vec3
 */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    @Shadow private Vec3 cameraPos;

    @Inject(method = "tryExtractRenderState", at = @At("HEAD"), cancellable = true)
    private void turbo$cull(BlockEntity be, float partialTick, ModelFeatureRenderer.CrumblingOverlay crumbling,
                            CallbackInfoReturnable<BlockEntityRenderState> cir) {
        if (!TurboConfig.on()) return;
        TurboConfig c = TurboConfig.get();

        if (c.noBlockEntities) { skip(cir); return; }
        if (c.disableEnchantBook && (be instanceof EnchantingTableBlockEntity || be instanceof LecternBlockEntity)) {
            skip(cir); return;
        }
        if (c.disableSpawnerRender && (be instanceof SpawnerBlockEntity || be instanceof TrialSpawnerBlockEntity)) {
            skip(cir); return;
        }
        if (c.disableBeaconBeam && be instanceof BeaconBlockEntity) { skip(cir); return; }
        if (this.cameraPos == null) return;

        BlockPos p = be.getBlockPos();
        double dx = p.getX() + 0.5 - this.cameraPos.x;
        double dy = p.getY() + 0.5 - this.cameraPos.y;
        double dz = p.getZ() + 0.5 - this.cameraPos.z;
        double d2 = dx * dx + dy * dy + dz * dz;

        if (be instanceof BeaconBlockEntity) {
            // Beams are tall: judge by horizontal distance only. Never fog-culled (beam leaves the block).
            double b = c.beaconBeamDistance;
            if (c.cheapBeaconBeamFar && dx * dx + dz * dz > b * b) skip(cir);
            return;
        }

        if (be instanceof SignBlockEntity) {
            double s = c.signTextDistance;
            if (c.cheapSignTextRender && d2 > s * s) { skip(cir); return; }
        }

        double max = c.blockEntityDistance;
        if (d2 > max * max && isHiddenType(c, be)) { skip(cir); return; }

        int all = Effective.blockEntityDistance();
        if (all > 0 && d2 > (double) all * all) { skip(cir); return; }

        // Behind walls: renderers that draw outside their block (beams, structure blocks...) are
        // never tested. The box is the block padded by 1 (2 upward) so standing banners, wall
        // banners, open shulker lids, floating books and items all stay inside it.
        if (c.occlusionCulling) {
            BlockEntityRenderer<BlockEntity, BlockEntityRenderState> r =
                    ((BlockEntityRenderDispatcher) (Object) this).getRenderer(be);
            if (r != null && !r.shouldRenderOffScreen()) {
                AABB box = new AABB(p.getX() - 1.0, p.getY() - 1.0, p.getZ() - 1.0, p.getX() + 2.0, p.getY() + 3.0, p.getZ() + 2.0);
                if (Occlusion.isBlockHidden(Minecraft.getInstance(), p.asLong(), box,
                        this.cameraPos.x, this.cameraPos.y, this.cameraPos.z)) {
                    RenderStats.blockEntityOccluded();
                    cir.setReturnValue(null);
                    return;
                }
            }
        }

        // Exact: whole padded block past 100% fog. Renderers that draw outside their block
        // (shouldRenderOffScreen: beams, structure blocks...) are never fog-culled.
        double fogSq = FogCull.cutoffSq();
        if (fogSq > 0) {
            BlockEntityRenderer<BlockEntity, BlockEntityRenderState> r =
                    ((BlockEntityRenderDispatcher) (Object) this).getRenderer(be);
            if (r != null && !r.shouldRenderOffScreen()) {
                double ex = Math.max(0, Math.abs(dx) - 0.5), ey = Math.max(0, Math.abs(dy) - 0.5), ez = Math.max(0, Math.abs(dz) - 0.5);
                if (ex * ex + ey * ey + ez * ez > fogSq) skip(cir);
            }
        }
    }

    private static void skip(CallbackInfoReturnable<BlockEntityRenderState> cir) {
        RenderStats.blockEntitySkipped();
        cir.setReturnValue(null);
    }

    private static boolean isHiddenType(TurboConfig c, BlockEntity be) {
        return (c.skipBellSwingAnim && be instanceof BellBlockEntity)
                || (c.skipConduitAnim && be instanceof ConduitBlockEntity)
                || (c.skipSkullRotation && be instanceof SkullBlockEntity)
                || (c.skipChestLidAnimFar && (be instanceof ChestBlockEntity
                        || be instanceof EnderChestBlockEntity || be instanceof ShulkerBoxBlockEntity))
                || (c.limitBannerLayers && be instanceof BannerBlockEntity)
                || (c.cheapEndGatewayAnim && be instanceof TheEndGatewayBlockEntity);
    }
}
