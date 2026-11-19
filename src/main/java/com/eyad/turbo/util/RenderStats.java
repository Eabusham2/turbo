package com.eyad.turbo.util;

/** Per-frame counters for the optional debug line. Render thread only. */
public final class RenderStats {

    private static int entities, blockEntities;
    public static int lastEntities, lastBlockEntities;

    public static void entitySkipped() { entities++; }

    public static void blockEntitySkipped() { blockEntities++; }

    /** Called once at the start of each world frame. */
    public static void newFrame() {
        lastEntities = entities;
        lastBlockEntities = blockEntities;
        entities = 0;
        blockEntities = 0;
    }

    private RenderStats() {}
}
