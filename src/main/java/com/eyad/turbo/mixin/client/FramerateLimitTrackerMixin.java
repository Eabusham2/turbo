package com.eyad.turbo.mixin.client;

import com.eyad.turbo.config.TurboConfig;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Extra frame caps on top of vanilla's FramerateLimitTracker.
 * Vanilla (1.21.11) already returns 10 when minimized or long-AFK, min(limit,30) when
 * short-AFK and 60 on out-of-world menus. We only ever LOWER the value it returns.
 * Verified target: FramerateLimitTracker#getFramerateLimit()I
 */
@Mixin(FramerateLimitTracker.class)
public abstract class FramerateLimitTrackerMixin {

    @Inject(method = "getFramerateLimit", at = @At("RETURN"), cancellable = true)
    private void turbo$extraCaps(CallbackInfoReturnable<Integer> cir) {
        if (!TurboConfig.on()) return;
        TurboConfig c = TurboConfig.get();
        if (!c.unfocusedFpsLimit && !c.menuFpsLimit) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        int current = cir.getReturnValueI();
        int target = current;
        if (c.unfocusedFpsLimit && !mc.isWindowActive()) {
            target = Math.min(target, Math.max(1, c.unfocusedFps));
        }
        if (c.menuFpsLimit && mc.screen != null && mc.level != null) {
            target = Math.min(target, Math.max(1, c.menuFps));
        }
        if (target != current) cir.setReturnValue(target);
    }
}
