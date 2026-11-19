package com.eyad.turbo.mixin.client;

import com.eyad.turbo.config.TurboConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Sound tweaks. Audio only: game logic never depends on whether a client sound played.
 *
 * Verified target: SoundEngine#play(SoundInstance)SoundEngine$PlayResult, field
 * instanceToChannel. Vanilla flow (decompiled): resolve the sound -> notify listeners
 * (subtitles) with range = max(volume, 1) * attenuationDistance -> start an OpenAL channel
 * (createHandle(...).join(), which blocks the render thread briefly) with LINEAR attenuation
 * that reaches zero at that same range.
 */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    @Shadow @Final private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

    @Unique private final Set<Long> turbo$tickSounds = new HashSet<>();
    @Unique private long turbo$tick = Long.MIN_VALUE;

    /** Early filters: mute-all, user distance cull, concurrent cap, same-tick duplicates. */
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void turbo$filters(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (!TurboConfig.on()) return;
        TurboConfig c = TurboConfig.get();
        if (c.disableAllSounds) { cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED); return; }

        boolean positional = !sound.isRelative() && sound.getAttenuation() != SoundInstance.Attenuation.NONE;
        Minecraft mc = Minecraft.getInstance();

        if (c.cullDistantSounds && positional && mc.gameRenderer != null) {
            Vec3 cam = mc.gameRenderer.getMainCamera().position();
            double max = c.soundCullDistance;
            if (cam.distanceToSqr(sound.getX(), sound.getY(), sound.getZ()) > max * max) {
                cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
                return;
            }
        }

        if (c.capConcurrentSounds && sound.getSource() != SoundSource.MUSIC
                && this.instanceToChannel.size() >= Math.max(1, c.maxConcurrentSounds)) {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
            return;
        }

        if (c.dedupeSimultaneousSounds && positional && !(sound instanceof TickableSoundInstance) && mc.level != null) {
            long now = mc.level.getGameTime();
            if (now != this.turbo$tick) { this.turbo$tick = now; this.turbo$tickSounds.clear(); }
            long key = sound.getIdentifier().hashCode() * 31L
                    + (((long) Math.floor(sound.getX()) & 0x3FFFFFL) << 42)
                    + (((long) Math.floor(sound.getY()) & 0xFFFL) << 30)
                    + ((long) Math.floor(sound.getZ()) & 0x3FFFFFFL);
            if (!this.turbo$tickSounds.add(key)) cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
        }
    }

    /**
     * Skip sounds that are out of hearing range. Runs after the sound is resolved and the
     * subtitle listeners were notified (so subtitles are unchanged), right before vanilla
     * starts a channel. Only short, one-shot, non-moving sounds are skipped, with an 8-block
     * margin so walking toward a sound mid-play can't make a difference.
     */
    @Inject(method = "play",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundEngine;shouldLoopAutomatically(Lnet/minecraft/client/resources/sounds/SoundInstance;)Z"),
            cancellable = true)
    private void turbo$inaudible(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (!TurboConfig.on() || !TurboConfig.get().skipInaudibleSounds) return;
        if (sound.isRelative() || sound.getAttenuation() != SoundInstance.Attenuation.LINEAR) return;
        if (sound.isLooping() || sound instanceof TickableSoundInstance || sound.getDelay() > 0) return;
        Sound s = sound.getSound();
        if (s == null || s.shouldStream()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null) return;
        double range = Math.max(sound.getVolume(), 1.0F) * s.getAttenuationDistance() + 8.0;
        Vec3 cam = mc.gameRenderer.getMainCamera().position();
        if (cam.distanceToSqr(sound.getX(), sound.getY(), sound.getZ()) > range * range) {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
        }
    }
}
