package com.eyad.turbo.mixin.client;

import com.eyad.turbo.config.TurboConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Toast popups hidden. Verified target: ToastManager#render(GuiGraphics). Toasts still queue/expire normally. */
@Mixin(ToastManager.class)
public abstract class ToastManagerMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void turbo$noToasts(GuiGraphics g, CallbackInfo ci) {
        if (TurboConfig.on() && TurboConfig.get().disableToasts) ci.cancel();
    }
}
