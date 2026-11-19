package com.eyad.turbo.mixin.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Calls the renderer's own culling helpers, so the behind-walls test uses exactly the box
 * vanilla's frustum culling uses (renderers widen it for big models) and respects renderers
 * that opt out of culling (ender dragon, lightning, fishing line).
 *
 * Verified targets (1.21.11, EntityRenderer):
 *   protected AABB getBoundingBoxForCulling(T)
 *   protected boolean affectedByCulling(T)
 *   protected boolean shouldShowName(T, double)
 *   protected float shadowRadius
 */
@Mixin(EntityRenderer.class)
public interface EntityRendererInvoker {

    @Invoker("getBoundingBoxForCulling")
    AABB turbo$cullingBox(Entity entity);

    @Invoker("affectedByCulling")
    boolean turbo$affectedByCulling(Entity entity);

    @Invoker("shouldShowName")
    boolean turbo$shouldShowName(Entity entity, double distanceSq);

    @Accessor("shadowRadius")
    float turbo$shadowRadius();
}
