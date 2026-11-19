package com.eyad.turbo.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tags a config field so the GUI builder can auto-place it into a category tab
 * with a readable label and tooltip, and so we can render a tier badge.
 *
 * Tier meaning:
 *   1 = wired to a verified-stable mixin hook. Safe.
 *   2 = wired, but targets a method/field more likely to shift between MC versions.
 *   3 = experimental / aggressive; may visibly change gameplay or conflict with other mods.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Tweak {
    String category();
    String label();
    String tooltip() default "";
    int tier() default 1;
    /** true = wired to a verified 1.21.11 mixin. false = planned, no effect yet. */
    boolean live() default false;
}
