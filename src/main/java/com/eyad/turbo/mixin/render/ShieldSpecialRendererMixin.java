package com.eyad.turbo.mixin.render;

import com.eyad.turbo.config.TurboConfig;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Plain shields: the shield renderer reads banner patterns and base colour from the map
 * returned here. Returning an empty map draws the plain shield. The item keeps its data.
 * Verified target: ShieldSpecialRenderer#extractArgument(ItemStack)DataComponentMap
 */
@Mixin(ShieldSpecialRenderer.class)
public abstract class ShieldSpecialRendererMixin {

    @Inject(method = "extractArgument(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/core/component/DataComponentMap;",
            at = @At("HEAD"), cancellable = true)
    private void turbo$plainShield(ItemStack stack, CallbackInfoReturnable<DataComponentMap> cir) {
        if (TurboConfig.on() && TurboConfig.get().skipShieldDecorationRender) cir.setReturnValue(DataComponentMap.EMPTY);
    }
}
