package com.eyad.turbo.util;

/** Per-frame counters for the optional debug line. Render thread only. */
public final class RenderStats {

    private static int entities, blockEntities, occluded;
    public static int lastEntities, lastBlockEntities, lastOccluded;

    public static void entitySkipped() { entities++; }

    public static void blockEntitySkipped() { blockEntities++; }

    public static void entityOccluded() { occluded++; }

    /** Called once at the start of each world frame. */
    public static void newFrame() {
        lastEntities = entities;
        lastBlockEntities = blockEntities;
        lastOccluded = occluded;
        occluded = 0;
        entities = 0;
        blockEntities = 0;
    }

    private RenderStats() {}
}
