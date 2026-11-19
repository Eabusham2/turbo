package com.eyad.turbo.util;

import com.eyad.turbo.config.TurboConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;

/**
 * "Hidden by fog" culling that is visually exact.
 *
 * 1.21.11 shaders blend every fragment toward the fog colour using spherical distance from
 * the camera; at environmentalEnd the blend is 100%, i.e. the object is drawn in pure fog
 * colour. That is only invisible if whatever is behind it is ALSO pure fog colour. Verified
 * in the decompiled fog environments: water, lava, powder snow, blindness and darkness all
 * set skyEnd/cloudEnd <= environmentalEnd, and the Nether has no sky (background = fog colour).
 * In the Overworld and End the sky is drawn with its own gradient, so we never cull there.
 *
 * FogRendererMixin records environmentalEnd every frame right before entities are extracted.
 */
public final class FogCull {

    private static volatile float environmentalEnd = Float.MAX_VALUE;

    /** Extra blocks beyond the bounding box, covering geometry that pokes outside it. */
    public static final double MARGIN = 2.0;

    public static void record(float end) {
        environmentalEnd = end;
    }

    /**
     * Squared distance beyond which everything is pure fog colour, or -1 when culling by fog
     * would not be exact right now (or the setting is off).
     */
    public static double cutoffSq() {
        TurboConfig c = TurboConfig.get();
        if (!c.fogCulling || c.disableFog) return -1;
        float end = environmentalEnd;
        if (end <= 0 || end >= 1.0e6f) return -1;

        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null || mc.level == null) return -1;
        Camera camera = mc.gameRenderer.getMainCamera();

        FogType fluid = camera.getFluidInCamera();
        boolean exact = fluid == FogType.WATER || fluid == FogType.LAVA || fluid == FogType.POWDER_SNOW;
        if (!exact) {
            Entity e = camera.entity();
            if (e instanceof LivingEntity le
                    && (le.hasEffect(MobEffects.BLINDNESS) || le.hasEffect(MobEffects.DARKNESS))) {
                exact = true;
            }
        }
        if (!exact && mc.level.dimension() == Level.NETHER) {
            exact = true;
        }
        if (!exact) return -1;

        double d = end + MARGIN;
        return d * d;
    }

    private FogCull() {}
}
