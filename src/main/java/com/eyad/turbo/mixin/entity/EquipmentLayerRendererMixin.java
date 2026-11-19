package com.eyad.turbo.mixin.entity;

import com.eyad.turbo.config.TurboConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Worn armor glint and trims. In the full EquipmentLayerRenderer#renderLayers overload,
 * vanilla reads itemStack.hasFoil() to decide on the glint pass and
 * itemStack.get(DataComponents.TRIM) for the trim pass. We change only those two reads.
 * The armor item itself is untouched.
 */
@Mixin(EquipmentLayerRenderer.class)
public abstract class EquipmentLayerRendererMixin {

    private static final String RENDER_LAYERS =
            "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V";

    @ModifyExpressionValue(method = RENDER_LAYERS,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hasFoil()Z"))
    private boolean turbo$noArmorGlint(boolean hasFoil) {
        return hasFoil && !(TurboConfig.on() && TurboConfig.get().skipGlintOnArmor);
    }

    @ModifyExpressionValue(method = RENDER_LAYERS,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object turbo$noTrim(Object component) {
        if (component instanceof ArmorTrim && TurboConfig.on() && TurboConfig.get().skipArmorTrimRender) return null;
        return component;
    }
}
