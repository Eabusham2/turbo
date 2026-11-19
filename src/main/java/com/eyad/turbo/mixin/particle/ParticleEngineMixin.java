package com.eyad.turbo.mixin.particle;

import com.eyad.turbo.config.TurboConfig;
import com.eyad.turbo.util.Effective;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Queue;

/**
 * Global particle cap using the engine's REAL counts (replaces the old estimated counter).
 * Every particle, including ones created directly by other code, goes through add().
 * Verified targets (1.21.11):
 *   ParticleEngine#add(Particle)V
 *   fields particles: Map<ParticleRenderType, ParticleGroup<?>>, particlesToAdd: Queue<Particle>
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Shadow @Final private Map<ParticleRenderType, ParticleGroup<?>> particles;
    @Shadow @Final private Queue<Particle> particlesToAdd;

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void turbo$cap(Particle particle, CallbackInfo ci) {
        if (!TurboConfig.on()) return;
        if (Effective.particlesOff()) { ci.cancel(); return; }

        TurboConfig c = TurboConfig.get();
        // The item pickup fly-in is a particle; the item itself is removed separately by the packet handler.
        if (c.disableItemPickupAnim && particle instanceof ItemPickupParticle) { ci.cancel(); return; }
        if (!c.limitParticles) return;
        int live = this.particlesToAdd.size();
        for (ParticleGroup<?> group : this.particles.values()) live += group.size();
        if (live >= c.maxParticles) ci.cancel();
    }
}
