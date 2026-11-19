package com.eyad.turbo.mixin.entity;

import com.eyad.turbo.config.TurboConfig;
import com.eyad.turbo.util.DroppedItemContext;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dropped item look. Verified target:
 *   ItemEntityRenderer#extractRenderState(ItemEntity, ItemEntityRenderState, float)V
 * Fields: ItemClusterRenderState.count (copies drawn for a stack), EntityRenderState.ageInTicks
 * (only used by ItemEntityRenderer#submit for the bob and spin).
 * The glint flag is read by ItemStackLayerMixin while this extraction resolves the item model.
 */
@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    private static final String EXTRACT =
            "extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V";

    @Inject(method = EXTRACT, at = @At("HEAD"))
    private void turbo$begin(ItemEntity item, ItemEntityRenderState state, float partialTick, CallbackInfo ci) {
        DroppedItemContext.active = TurboConfig.on() && TurboConfig.get().noDroppedItemGlint;
    }

    @Inject(method = EXTRACT, at = @At("TAIL"))
    private void turbo$end(ItemEntity item, ItemEntityRenderState state, float partialTick, CallbackInfo ci) {
        DroppedItemContext.active = false;
        if (!TurboConfig.on()) return;
        TurboConfig c = TurboConfig.get();
        if (c.flattenItemEntities && state.count > 1) state.count = 1;
        if (c.cheapItemEntityBob) state.ageInTicks = 0.0F;
    }
}
