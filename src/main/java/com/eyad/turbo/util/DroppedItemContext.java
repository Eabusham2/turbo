package com.eyad.turbo.util;

/** True only while a dropped item's render state is being extracted (render thread). */
public final class DroppedItemContext {
    public static boolean active;

    private DroppedItemContext() {}
}
