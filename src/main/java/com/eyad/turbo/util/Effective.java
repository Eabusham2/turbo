package com.eyad.turbo.util;

import com.eyad.turbo.config.TurboConfig;

/** Combines individual toggles with their "disable everything" aggregates. */
public final class Effective {

    public static boolean particlesOff() {
        return TurboConfig.get().noParticlesAtAll;
    }

    public static boolean texturesFrozen() {
        return TurboConfig.get().disableTextureAnimations;
    }

    public static boolean cloudsOff() {
        TurboConfig c = TurboConfig.get();
        return c.disableClouds || c.noAtmosphere;
    }

    public static boolean weatherOff() {
        TurboConfig c = TurboConfig.get();
        return c.disableWeatherRender || c.noAtmosphere;
    }

    public static boolean starsOff() {
        TurboConfig c = TurboConfig.get();
        return c.disableStars || c.noAtmosphere;
    }

    /** Max entity render distance in blocks, or 0 for vanilla behaviour. */
    public static int entityDistance() {
        return Math.max(0, TurboConfig.get().entityRenderDistance);
    }

    /** Max block-entity render distance in blocks, or 0 for vanilla behaviour. */
    public static int blockEntityDistance() {
        TurboConfig c = TurboConfig.get();
        return c.limitBlockEntityRender ? Math.max(0, c.blockEntityDistance) : 0;
    }

    private Effective() {}
}
