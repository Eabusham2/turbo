package com.eyad.turbo.mixin.entity;

import com.eyad.turbo.config.TurboConfig;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Player look tweaks, all on the extracted render state (players and mannequins).
 * Verified target: AvatarRenderer#extractRenderState(Avatar, AvatarRenderState, float)V
 * Fields: showCape, capeFlap/capeLean/capeLean2, showHat/showJacket/show*Sleeve/show*Pants.
 */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("TAIL"))
    private void turbo$playerLook(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        if (!TurboConfig.on()) return;
        TurboConfig c = TurboConfig.get();
        if (c.skipThirdPersonCapeRender) state.showCape = false;
        if (c.disableCapeFlutter) {
            state.capeFlap = 0.0F;
            state.capeLean = 0.0F;
            state.capeLean2 = 0.0F;
        }
        if (c.skipPlayerModelLayers) {
            state.showHat = false;
            state.showJacket = false;
            state.showLeftSleeve = false;
            state.showRightSleeve = false;
            state.showLeftPants = false;
            state.showRightPants = false;
        }
    }
}
