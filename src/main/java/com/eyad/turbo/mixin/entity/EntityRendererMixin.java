package com.eyad.turbo.mixin.entity;

import com.eyad.turbo.config.TurboConfig;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Nametag distance + leash hiding, applied to the extracted render state.
 *
 * Hooking the base extractRenderState (which every renderer calls via super) covers
 * player, mob and custom-name tags even when a subclass overrides shouldShowName.
 * Vanilla only shows nametags inside 64 blocks; this lowers that. Leashes: vanilla
 * itself sets leashStates to null when there is no leash, so null is a normal state.
 *
 * Verified target: EntityRenderer#extractRenderState(Entity, EntityRenderState, float)V
 * Fields used: EntityRenderState.nameTag, distanceToCameraSq, leashStates
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void turbo$trimState(Entity entity, EntityRenderState state, float partialTick, CallbackInfo ci) {
        if (!TurboConfig.on()) return;
        TurboConfig c = TurboConfig.get();

        if (c.limitNametags && state.nameTag != null) {
            double max = c.nametagDistance;
            if (state.distanceToCameraSq > max * max) state.nameTag = null;
        }
        if (c.disableLeashRender) state.leashStates = null;
        if (c.skipEntityFireRender) state.displayFireAnimation = false;
    }

    /**
     * Entity shadows: skip the per-entity block scan entirely. extractShadow is what clears and
     * refills shadowPieces, so when we skip it we clear them ourselves.
     * Verified target: private EntityRenderer#extractShadow(EntityRenderState, Minecraft, Level)V
     */
    @Inject(method = "extractShadow", at = @At("HEAD"), cancellable = true)
    private void turbo$noShadow(EntityRenderState state, Minecraft mc, Level level, CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().disableShadowsEntirely) {
            state.shadowPieces.clear();
            state.shadowRadius = 0.0F;
            ci.cancel();
        }
    }
}
