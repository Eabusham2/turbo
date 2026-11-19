package com.eyad.turbo.mixin.render;

import com.eyad.turbo.config.TurboConfig;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * No map markers: clears the extracted decoration list (player arrows, banners, etc.).
 * The map image and the saved map data are untouched.
 * Verified target: MapRenderer#extractRenderState(MapId, MapItemSavedData, MapRenderState)V
 */
@Mixin(MapRenderer.class)
public abstract class MapRendererMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void turbo$noMarkers(MapId id, MapItemSavedData data, MapRenderState state, CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().skipMapDecorationRender) state.decorations.clear();
    }
}
