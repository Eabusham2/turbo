package com.eyad.turbo.mixin.render;

import com.eyad.turbo.config.TurboConfig;
import com.eyad.turbo.util.Effective;
import com.eyad.turbo.util.NamedAnimation;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Animated texture control (freeze, or half speed for water / lava / fire).
 *
 * How 1.21.11 animates: TextureAtlas#cycleAnimationFrames calls AnimationState#tick on
 * every animated sprite 20x/s, then opens a render pass per mip level and redraws each
 * sprite whose needsToDraw() is true (isDirty, or always for interpolated sprites).
 *
 * A naive "cancel tick()" would be WORSE: isDirty starts true and only tick() clears it,
 * so every sprite would redraw forever. So whenever we skip a tick we also clear isDirty,
 * and needsToDraw() reports only isDirty for that tick. The first frame is still drawn once
 * at atlas load. Half speed = skip every other tick for matching sprites.
 *
 * Verified targets: SpriteContents$AnimationState#tick()V, #needsToDraw()Z, field isDirty:Z
 */
@Mixin(SpriteContents.AnimationState.class)
public abstract class SpriteAnimationStateMixin implements NamedAnimation {

    @Shadow private boolean isDirty;

    @Unique private String turbo$path = "";
    @Unique private boolean turbo$skippedThisTick;
    @Unique private boolean turbo$parity;

    @Override
    public void turbo$setSpritePath(String path) {
        this.turbo$path = path == null ? "" : path;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void turbo$throttle(CallbackInfo ci) {
        this.turbo$skippedThisTick = false;
        if (!TurboConfig.on()) return;
        boolean skip = Effective.texturesFrozen();
        if (!skip && halfSpeed()) {
            this.turbo$parity = !this.turbo$parity;
            skip = this.turbo$parity;
        }
        if (skip) {
            this.isDirty = false;
            this.turbo$skippedThisTick = true;
            ci.cancel();
        }
    }

    @Inject(method = "needsToDraw", at = @At("HEAD"), cancellable = true)
    private void turbo$noRedraw(CallbackInfoReturnable<Boolean> cir) {
        if (this.turbo$skippedThisTick) cir.setReturnValue(this.isDirty);
    }

    @Unique
    private boolean halfSpeed() {
        TurboConfig c = TurboConfig.get();
        String p = this.turbo$path;
        return (c.reduceWaterAnimFps && p.contains("water"))
                || (c.reduceLavaAnimFps && p.contains("lava"))
                || (c.reduceFireAnimFps && p.contains("fire"));
    }
}
