package com.eyad.turbo.mixin.render;

import com.eyad.turbo.util.NamedAnimation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tags each animation state with its sprite's path so half-speed water/lava/fire can tell
 * which sprites to slow down.
 * Verified target: SpriteContents#createAnimationState(GpuBufferSlice, int)SpriteContents$AnimationState
 */
@Mixin(SpriteContents.class)
public abstract class SpriteContentsMixin {

    @Shadow public abstract Identifier name();

    @Inject(method = "createAnimationState", at = @At("RETURN"))
    private void turbo$tag(GpuBufferSlice slice, int i, CallbackInfoReturnable<SpriteContents.AnimationState> cir) {
        SpriteContents.AnimationState state = cir.getReturnValue();
        if (state instanceof NamedAnimation named) named.turbo$setSpritePath(this.name().getPath());
    }
}
