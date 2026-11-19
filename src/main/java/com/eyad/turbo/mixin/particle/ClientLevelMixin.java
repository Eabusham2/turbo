package com.eyad.turbo.mixin.particle;

import com.eyad.turbo.config.TurboConfig;
import com.eyad.turbo.util.Effective;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Particle filters at the spawn site.
 *
 * Every ClientLevel#addParticle / #addAlwaysVisibleParticle overload funnels into the
 * private doAddParticle, so one hook covers them all. Vanilla already rejects
 * non-forced particles beyond 32 blocks there, so our distance cull only matters
 * below 32 and never touches forced particles.
 *
 * Verified targets (1.21.11):
 *   private doAddParticle(ParticleOptions, boolean force, boolean decreased, DDDDDD)
 *   public addDestroyBlockEffect(BlockPos, BlockState)     - block break burst
 *   public addBreakingBlockEffect(BlockPos, Direction)     - mining hit particles
 *   public trackExplosionEffects(Vec3, float, int, WeightedList<ExplosionParticleInfo>) - extra blast puffs
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Inject(method = "doAddParticle", at = @At("HEAD"), cancellable = true)
    private void turbo$filter(ParticleOptions options, boolean force, boolean decreased,
                              double x, double y, double z, double vx, double vy, double vz,
                              CallbackInfo ci) {
        if (!TurboConfig.on()) return;
        if (Effective.particlesOff()) { ci.cancel(); return; }
        TurboConfig c = TurboConfig.get();

        ParticleType<?> t = options.getType();
        if ((c.disableRainSplash && t == ParticleTypes.RAIN)
                || (c.disableExplosionParticles && (t == ParticleTypes.EXPLOSION || t == ParticleTypes.EXPLOSION_EMITTER))
                || (c.disablePotionParticles && (t == ParticleTypes.ENTITY_EFFECT || t == ParticleTypes.EFFECT || t == ParticleTypes.INSTANT_EFFECT))
                || (c.disableCritParticles && (t == ParticleTypes.CRIT || t == ParticleTypes.ENCHANTED_HIT))
                || (c.disableBubbleParticles && (t == ParticleTypes.BUBBLE || t == ParticleTypes.BUBBLE_POP
                        || t == ParticleTypes.BUBBLE_COLUMN_UP || t == ParticleTypes.CURRENT_DOWN))
                || (c.disableEndParticles && (t == ParticleTypes.END_ROD || t == ParticleTypes.DRAGON_BREATH))
                || (c.cheapCampfireSmoke && (t == ParticleTypes.CAMPFIRE_COSY_SMOKE || t == ParticleTypes.CAMPFIRE_SIGNAL_SMOKE))
                || (c.reducePortalParticleRender && (t == ParticleTypes.PORTAL || t == ParticleTypes.REVERSE_PORTAL))
                || (c.reduceFireworkParticleRender && t == ParticleTypes.FIREWORK)
                || (c.skipRedstoneDustGlow && t == ParticleTypes.DUST)) {
            ci.cancel();
            return;
        }

        boolean distanceCull = c.cullDistantParticles && !force && c.particleCullDistance < 32;
        boolean farSwirls = c.limitMobParticleEffects && (t == ParticleTypes.ENTITY_EFFECT || t == ParticleTypes.EFFECT);
        if (!distanceCull && !farSwirls) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null) return;
        double d2 = mc.gameRenderer.getMainCamera().position().distanceToSqr(x, y, z);
        if (distanceCull && d2 > (double) c.particleCullDistance * c.particleCullDistance) { ci.cancel(); return; }
        if (farSwirls && d2 > (double) c.decorEntityDistance * c.decorEntityDistance) ci.cancel();
    }

    @Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
    private void turbo$noBreakBurst(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (TurboConfig.on() && (TurboConfig.get().disableBlockBreakParticles || Effective.particlesOff())) ci.cancel();
    }

    @Inject(method = "addBreakingBlockEffect", at = @At("HEAD"), cancellable = true)
    private void turbo$noMiningHits(BlockPos pos, Direction side, CallbackInfo ci) {
        if (TurboConfig.on() && (TurboConfig.get().disableBlockBreakParticles || Effective.particlesOff())) ci.cancel();
    }

    @Inject(method = "trackExplosionEffects", at = @At("HEAD"), cancellable = true)
    private void turbo$noBlastPuffs(Vec3 center, float radius, int blockCount,
                                    WeightedList<ExplosionParticleInfo> particles, CallbackInfo ci) {
        if (TurboConfig.on() && (TurboConfig.get().disableExplosionParticles || Effective.particlesOff())) ci.cancel();
    }
}
