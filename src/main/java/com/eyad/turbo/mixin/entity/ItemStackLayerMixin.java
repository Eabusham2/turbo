package com.eyad.turbo.mixin.entity;

import com.eyad.turbo.util.DroppedItemContext;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * No enchant glint on dropped items: while a dropped item is being extracted, every model
 * layer's foil type is forced to NONE. Items in hands, GUIs, frames etc. are unaffected.
 * Verified target: ItemStackRenderState$LayerRenderState#setFoilType(ItemStackRenderState$FoilType)V
 */
@Mixin(ItemStackRenderState.LayerRenderState.class)
public abstract class ItemStackLayerMixin {

    @ModifyVariable(method = "setFoilType", at = @At("HEAD"), argsOnly = true)
    private ItemStackRenderState.FoilType turbo$noGlint(ItemStackRenderState.FoilType type) {
        return DroppedItemContext.active ? ItemStackRenderState.FoilType.NONE : type;
    }
}
