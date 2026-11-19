package com.eyad.turbo.mixin.world;

import com.eyad.turbo.config.TurboConfig;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Smart leaves (original reimplementation of the OptiFine / Cull Leaves idea).
 *
 * Verified 1.21.11 behaviour: LeavesBlock#skipRendering already culls leaf-against-leaf
 * faces, but ONLY when the static cutoutLeaves flag is false (opaque leaves). With
 * see-through leaves every interior face is drawn. This applies the same leaf-vs-leaf
 * rule in cutout mode too: the outer shell still renders with transparency, the hidden
 * inside does not. Sodium's block occlusion calls BlockState#skipRendering, so this
 * applies with Sodium as well.
 *
 * Verified target: LeavesBlock#skipRendering(BlockState, BlockState, Direction)Z
 */
@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin {

    @Inject(method = "skipRendering", at = @At("HEAD"), cancellable = true)
    private void turbo$smartLeaves(BlockState state, BlockState neighbor, Direction side,
                                   CallbackInfoReturnable<Boolean> cir) {
        if (TurboConfig.on() && TurboConfig.get().skipDistantLeafRender
                && neighbor.getBlock() instanceof LeavesBlock) {
            cir.setReturnValue(true);
        }
    }
}
