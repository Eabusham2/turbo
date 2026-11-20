package com.eyad.turbo.config;

import java.lang.reflect.Field;

/**
 * One-click profiles.
 *
 *   MINIMAL   - looks and sounds like vanilla; only skips work you can't see or hear
 *               (entities behind walls or hidden by fog, inaudible sounds, the world while minimized).
 *   OPTIMIZED - looks about the same; hides far-away clutter. Recommended for most people.
 *   POTATO    - every setting on, smallest distances.
 *   CUSTOM    - you changed individual settings after picking a preset.
 *
 * Applying a preset first resets every setting to its default (= Minimal), then layers the
 * preset on top. masterEnable, logStartup and debugOverlay are personal and never touched.
 */
public enum Preset {
    MINIMAL("Minimal: looks identical, faster"),
    OPTIMIZED("Optimized: looks about the same, much faster (recommended)"),
    POTATO("Potato: everything on"),
    CUSTOM("Custom");

    public final String label;

    Preset(String label) {
        this.label = label;
    }

    private static final java.util.Set<String> PERSONAL = java.util.Set.of("masterEnable", "logStartup", "debugOverlay");

    public void applyTo(TurboConfig c) {
        if (this == CUSTOM) return;
        TurboConfig defaults = new TurboConfig();
        for (Field f : TurboConfig.class.getDeclaredFields()) {
            if (f.getAnnotation(Tweak.class) == null || PERSONAL.contains(f.getName())) continue;
            try {
                f.setAccessible(true);
                f.set(c, f.get(defaults));
            } catch (IllegalAccessException ignored) { }
        }

        switch (this) {
            case MINIMAL -> { /* defaults already are Minimal */ }
            case OPTIMIZED -> optimized(c);
            case POTATO -> potato(c);
            default -> { }
        }
        c.preset = this;
    }

    private static void optimized(TurboConfig c) {
        // Frame
        c.unfocusedFpsLimit = true;  c.unfocusedFps = 30;
        c.noPauseBlur = true;
        // Far-away clutter
        c.limitNametags = true;      c.nametagDistance = 32;
        c.decorEntityDistance = 32;
        c.cullItemFrameMaps = true;  c.cheapPaintingRender = true;  c.cheapArmorStands = true;
        c.hideFarDroppedItems = true; c.disableXpOrbRender = true;  c.skipFallingBlockRender = true;
        c.reduceProjectileParticles = true;
        c.flattenItemEntities = true;
        c.limitBlockEntityRender = true; c.blockEntityDistance = 48;
        c.cheapSignTextRender = true;    c.signTextDistance = 32;
        c.cheapBeaconBeamFar = true;     c.beaconBeamDistance = 128;
        // Particles
        c.limitParticles = true;     c.maxParticles = 4000;
        c.cullDistantParticles = true; c.particleCullDistance = 24;
        c.limitMobParticleEffects = true;
        // Textures and terrain
        c.reduceWaterAnimFps = true; c.reduceLavaAnimFps = true; c.reduceFireAnimFps = true;
        c.skipDistantLeafRender = true;
        // Sound
        c.capConcurrentSounds = true; c.maxConcurrentSounds = 96;
        c.dedupeSimultaneousSounds = true;
    }

    private static void potato(TurboConfig c) {
        for (Field f : TurboConfig.class.getDeclaredFields()) {
            if (f.getAnnotation(Tweak.class) == null || f.getType() != boolean.class || PERSONAL.contains(f.getName())) continue;
            try {
                f.setAccessible(true);
                f.setBoolean(c, true);
            } catch (IllegalAccessException ignored) { }
        }
        c.unfocusedFps = 10;        c.menuFps = 30;
        c.entityRenderDistance = 32; c.nametagDistance = 16; c.decorEntityDistance = 16;
        c.blockEntityDistance = 16;  c.signTextDistance = 12; c.beaconBeamDistance = 32;
        c.maxBannerLayers = 1;
        c.maxParticles = 1000;       c.particleCullDistance = 12;
        c.soundCullDistance = 24;    c.maxConcurrentSounds = 32;
    }
}
