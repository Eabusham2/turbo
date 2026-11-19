package com.eyad.turbo.util;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * "Hidden behind walls" test for entities. Render thread only.
 *
 * An entity counts as hidden only when no straight line from the camera reaches its (padded)
 * culling box without passing through an opaque block. Rays go to the box centre first, then to
 * points spaced at most 1 block apart on every face of the box that faces the camera, so any
 * opening of at least one block between the camera and the box lets at least one ray through.
 *
 * Kept on the safe side on purpose:
 *  - Only full, opaque, drawn cubes block a ray (BlockState#isSolidRender with a MODEL shape).
 *    Glass, leaves, slabs, stairs, fences, barriers etc. never block.
 *  - A block only blocks once its chunk section is built and shown (vanilla or Sodium), so an
 *    entity seen through a section that hasn't loaded in yet is still drawn.
 *  - If the camera itself is inside an opaque block (spectator), nothing is hidden.
 *  - "Visible" is remembered for up to 3 frames, "hidden" never: an entity that comes into view
 *    is drawn on that same frame, an entity that just went behind a wall may be drawn for up to
 *    3 more frames (invisible anyway).
 * The test is re-run every frame, so there is no delay or pop-in.
 */
public final class Occlusion {

    private static final double SPACING = 1.0;
    private static final int MAX_POINTS = 512;
    private static final int VISIBLE_FRAMES = 3;

    // Per-frame cache of "does this block stop a ray".
    private static final int CACHE_SIZE = 1 << 14;
    private static final int CACHE_MASK = CACHE_SIZE - 1;
    private static final long[] keys = new long[CACHE_SIZE];
    private static final int[] stamps = new int[CACHE_SIZE];
    private static final boolean[] values = new boolean[CACHE_SIZE];

    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private static final Int2IntOpenHashMap lastVisible = new Int2IntOpenHashMap();

    private static int frame = 1;
    private static int cameraCheckedFrame;
    private static boolean cameraInsideBlock;

    /** Called once at the start of each world frame. */
    public static void newFrame() {
        frame++;
        if (lastVisible.size() > 8192) lastVisible.clear();
    }

    /** True if every line of sight from the camera to the box is blocked. */
    public static boolean isHidden(Minecraft mc, int entityId, AABB box, double cx, double cy, double cz) {
        Level level = mc.level;
        if (level == null || mc.levelRenderer == null) return false;

        if (cameraCheckedFrame != frame) {
            cameraCheckedFrame = frame;
            cameraInsideBlock = blocks(mc, level, floor(cx), floor(cy), floor(cz));
        }
        if (cameraInsideBlock) return false;

        if (box.contains(cx, cy, cz)) return false;

        int seen = lastVisible.getOrDefault(entityId, Integer.MIN_VALUE);
        if (seen != Integer.MIN_VALUE && frame - seen <= VISIBLE_FRAMES && frame - seen >= 0) return false;

        if (anyRayReaches(mc, level, box, cx, cy, cz)) {
            lastVisible.put(entityId, frame);
            return false;
        }
        return true;
    }

    private static boolean anyRayReaches(Minecraft mc, Level level, AABB b, double cx, double cy, double cz) {
        // Centre first: cheap, and the usual answer for visible entities.
        if (clear(mc, level, cx, cy, cz, (b.minX + b.maxX) * 0.5, (b.minY + b.maxY) * 0.5, (b.minZ + b.maxZ) * 0.5)) {
            return true;
        }

        int nx = steps(b.maxX - b.minX), ny = steps(b.maxY - b.minY), nz = steps(b.maxZ - b.minZ);
        int faces = (cx < b.minX || cx > b.maxX ? 1 : 0) + (cy < b.minY || cy > b.maxY ? 1 : 0) + (cz < b.minZ || cz > b.maxZ ? 1 : 0);
        int estimate = faces * (Math.max(nx, ny) + 1) * (Math.max(ny, nz) + 1);
        if (estimate > MAX_POINTS) return true; // huge box: don't spend the time, just draw it

        if (cx < b.minX || cx > b.maxX) {
            double x = cx < b.minX ? b.minX : b.maxX;
            for (int i = 0; i <= ny; i++) {
                double y = lerp(b.minY, b.maxY, i, ny);
                for (int k = 0; k <= nz; k++) {
                    if (clear(mc, level, cx, cy, cz, x, y, lerp(b.minZ, b.maxZ, k, nz))) return true;
                }
            }
        }
        if (cy < b.minY || cy > b.maxY) {
            double y = cy < b.minY ? b.minY : b.maxY;
            for (int i = 0; i <= nx; i++) {
                double x = lerp(b.minX, b.maxX, i, nx);
                for (int k = 0; k <= nz; k++) {
                    if (clear(mc, level, cx, cy, cz, x, y, lerp(b.minZ, b.maxZ, k, nz))) return true;
                }
            }
        }
        if (cz < b.minZ || cz > b.maxZ) {
            double z = cz < b.minZ ? b.minZ : b.maxZ;
            for (int i = 0; i <= nx; i++) {
                double x = lerp(b.minX, b.maxX, i, nx);
                for (int k = 0; k <= ny; k++) {
                    if (clear(mc, level, cx, cy, cz, x, lerp(b.minY, b.maxY, k, ny), z)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Walks the blocks the segment camera -> target passes through (Amanatides-Woo voxel
     * traversal). The camera's own block and anything at or past the target are not checked.
     */
    private static boolean clear(Minecraft mc, Level level, double x0, double y0, double z0, double x1, double y1, double z1) {
        double dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
        int x = floor(x0), y = floor(y0), z = floor(z0);

        int stepX = dx > 0 ? 1 : (dx < 0 ? -1 : 0);
        int stepY = dy > 0 ? 1 : (dy < 0 ? -1 : 0);
        int stepZ = dz > 0 ? 1 : (dz < 0 ? -1 : 0);
        double tDeltaX = stepX != 0 ? Math.abs(1.0 / dx) : Double.POSITIVE_INFINITY;
        double tDeltaY = stepY != 0 ? Math.abs(1.0 / dy) : Double.POSITIVE_INFINITY;
        double tDeltaZ = stepZ != 0 ? Math.abs(1.0 / dz) : Double.POSITIVE_INFINITY;
        double tMaxX = stepX > 0 ? (x + 1 - x0) / dx : stepX < 0 ? (x0 - x) / -dx : Double.POSITIVE_INFINITY;
        double tMaxY = stepY > 0 ? (y + 1 - y0) / dy : stepY < 0 ? (y0 - y) / -dy : Double.POSITIVE_INFINITY;
        double tMaxZ = stepZ > 0 ? (z + 1 - z0) / dz : stepZ < 0 ? (z0 - z) / -dz : Double.POSITIVE_INFINITY;

        int guard = (int) (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) + 4;
        while (guard-- > 0) {
            double t;
            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
                t = tMaxX; x += stepX; tMaxX += tDeltaX;
            } else if (tMaxY < tMaxZ) {
                t = tMaxY; y += stepY; tMaxY += tDeltaY;
            } else {
                t = tMaxZ; z += stepZ; tMaxZ += tDeltaZ;
            }
            if (t >= 1.0 - 1.0e-7) return true; // reached the target point
            if (blocks(mc, level, x, y, z)) return false;
        }
        return true;
    }

    /** Opaque, drawn full cube in a section that is built and shown. Cached for this frame. */
    private static boolean blocks(Minecraft mc, Level level, int x, int y, int z) {
        long key = BlockPos.asLong(x, y, z);
        int slot = (int) HashCommon.mix(key) & CACHE_MASK;
        for (int probe = 0; probe < 4; probe++) {
            int i = (slot + probe) & CACHE_MASK;
            if (stamps[i] == frame) {
                if (keys[i] == key) return values[i];
                continue;
            }
            boolean v = compute(mc, level, x, y, z);
            stamps[i] = frame;
            keys[i] = key;
            values[i] = v;
            return v;
        }
        return compute(mc, level, x, y, z);
    }

    private static boolean compute(Minecraft mc, Level level, int x, int y, int z) {
        pos.set(x, y, z);
        BlockState state = level.getBlockState(pos);
        if (!state.isSolidRender() || state.getRenderShape() != RenderShape.MODEL) return false;
        return mc.levelRenderer.isSectionCompiledAndVisible(pos);
    }

    private static int steps(double length) {
        return Math.max(1, (int) Math.ceil(length / SPACING));
    }

    private static double lerp(double a, double b, int i, int n) {
        return i == n ? b : a + (b - a) * i / n;
    }

    private static int floor(double v) {
        int i = (int) v;
        return v < i ? i - 1 : i;
    }

    private Occlusion() {}
}
