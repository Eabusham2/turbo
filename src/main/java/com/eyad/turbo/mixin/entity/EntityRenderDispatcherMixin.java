package com.eyad.turbo.mixin.entity;

import com.eyad.turbo.config.TurboConfig;
import com.eyad.turbo.util.Effective;
import com.eyad.turbo.util.FogCull;
import com.eyad.turbo.util.RenderStats;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ONE entity cull hook replacing the ~27 per-renderer mixins from the old build.
 *
 * Why: in 1.21.9+ entity renderers no longer have render(...); they extract a render
 * state and submit(...) it. Every old "render" mixin targeted a method that does not
 * exist in 1.21.11. The single place that decides whether ANY entity is drawn is
 * EntityRenderDispatcher#shouldRender(Entity, Frustum, double camX, double camY, double camZ).
 * Returning false there skips extraction and submission for that entity (model,
 * nametag, shadow, leash) for this frame only. Ticking/physics are untouched.
 *
 * Verified target: EntityRenderDispatcher#shouldRender(Entity, Frustum, DDD)Z
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void turbo$cull(Entity entity, Frustum frustum, double camX, double camY, double camZ,
                            CallbackInfoReturnable<Boolean> cir) {
        if (!TurboConfig.on()) return;
        TurboConfig c = TurboConfig.get();

        // Never hide the player, what they ride, or what rides them.
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && (entity == mc.player || entity == mc.getCameraEntity()
                || entity.hasPassenger(mc.player) || mc.player.hasPassenger(entity))) {
            return;
        }

        if (c.skipLightningBoltDetail && entity instanceof LightningBolt) {
            skip(cir);
            return;
        }

        double d2 = entity.distanceToSqr(camX, camY, camZ);

        int maxDist = Effective.entityDistance();
        if (maxDist > 0 && d2 > (double) maxDist * maxDist) {
            skip(cir);
            return;
        }

        double decor = c.decorEntityDistance;
        if (d2 > decor * decor && isHiddenDecor(c, entity)) {
            skip(cir);
            return;
        }

        // Exact: skip only if the whole (padded) culling box is past the point where fog is 100%.
        double fogSq = FogCull.cutoffSq();
        if (fogSq > 0 && fogSafe(mc, entity)) {
            AABB box = entity.getBoundingBox();
            double dx = Math.max(0, Math.max(box.minX - camX, camX - box.maxX));
            double dy = Math.max(0, Math.max(box.minY - camY, camY - box.maxY));
            double dz = Math.max(0, Math.max(box.minZ - camZ, camZ - box.maxZ));
            if (dx * dx + dy * dy + dz * dz > fogSq) skip(cir);
        }
    }

    private static void skip(CallbackInfoReturnable<Boolean> cir) {
        RenderStats.entitySkipped();
        cir.setReturnValue(false);
    }

    /**
     * Entities whose drawing can reach outside their own box or ignore fog are never fog-culled:
     * players and visible names (nametags), glowing outlines, leashes, displays, and beams/lines
     * that connect to something else (guardian, end crystal, fishing hook, lightning), and the
     * multi-part ender dragon. Distance is to the entity's box; FogCull adds a 2-block margin.
     */
    private static boolean fogSafe(Minecraft mc, Entity e) {
        if (e instanceof Player || e instanceof Display || e instanceof Guardian || e instanceof EndCrystal
                || e instanceof FishingHook || e instanceof LightningBolt || e instanceof EnderDragon) return false;
        if (e.isCustomNameVisible() || mc.shouldEntityAppearGlowing(e)) return false;
        if (e instanceof Leashable leashable && leashable.isLeashed()) return false;
        return true;
    }

    private static boolean isHiddenDecor(TurboConfig c, Entity e) {
        return (c.cullItemFrameMaps && e instanceof ItemFrame)
                || (c.cheapPaintingRender && e instanceof Painting)
                || (c.cheapArmorStands && e instanceof ArmorStand)
                || (c.disableXpOrbRender && e instanceof ExperienceOrb)
                || (c.hideFarDroppedItems && e instanceof ItemEntity)
                || (c.skipFallingBlockRender && e instanceof FallingBlockEntity)
                || (c.cullTntFlashRender && e instanceof PrimedTnt)
                || (c.reduceProjectileParticles && (e instanceof AbstractArrow
                        || e instanceof FireworkRocketEntity || e instanceof ShulkerBullet))
                || (c.disableFishingLine && e instanceof FishingHook)
                || (c.cheapBoatPaddleAnim && (e instanceof AbstractBoat || e instanceof AbstractMinecart))
                || (c.disablePotionParticles && e instanceof AreaEffectCloud);
    }
}
