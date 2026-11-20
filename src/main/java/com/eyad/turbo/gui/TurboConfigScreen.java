package com.eyad.turbo.gui;

import com.eyad.turbo.config.Preset;
import com.eyad.turbo.config.TurboConfig;
import com.eyad.turbo.config.Tweak;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Settings screen (ModMenu -> Turbo). First tab picks a preset; the other tabs are built by
 * reflection from every @Tweak field in TurboConfig, grouped by category.
 *
 * Saving: if a different preset was picked, it is applied (and wins over individual edits in
 * the same save). Otherwise, if any single setting was changed, the preset becomes Custom.
 */
public final class TurboConfigScreen {

    public static Screen create(Screen parent) {
        TurboConfig cfg = TurboConfig.get();
        TurboConfig defaults = new TurboConfig();
        Map<String, Object> before = snapshot(cfg);
        Preset[] picked = { cfg.preset };

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Turbo Settings"));
        ConfigEntryBuilder eb = builder.entryBuilder();

        ConfigCategory presets = builder.getOrCreateCategory(Component.literal("Presets"));
        presets.addEntry(eb.startEnumSelector(Component.literal("Preset"), Preset.class, cfg.preset)
                .setDefaultValue(Preset.MINIMAL)
                .setEnumNameProvider(p -> Component.literal(((Preset) p).label))
                .setTooltip(Component.literal("Pick one and press Save. It resets every setting and applies the preset."))
                .setSaveConsumer(p -> picked[0] = p)
                .build());
        presets.addEntry(eb.startTextDescription(Component.literal(
                "Minimal: looks and sounds like vanilla. Only skips work you can't see or hear: entities and block entities completely hidden behind walls "
                + "or by fog, sounds too far away to hear, and the world while minimized.\n\n"
                + "Optimized: looks about the same. Minimal plus: hides far-away clutter (item frames, armor stands, dropped items, "
                + "far block entities), caps particles and sounds, smart leaves, half-speed water/lava/fire animation. "
                + "Recommended for most people.\n\n"
                + "Potato: every setting on with the smallest distances. Chests, signs, banners and other block "
                + "entities stay visible and are only hidden behind walls. Also mutes all sounds and hides the "
                + "scoreboard; turn those back off if you want them.\n\n"
                + "No setting changes how the game plays: only what is drawn or heard.")).build());

        Map<String, ConfigCategory> cats = new LinkedHashMap<>();
        for (Field f : TurboConfig.class.getDeclaredFields()) {
            Tweak t = f.getAnnotation(Tweak.class);
            if (t == null) continue;
            f.setAccessible(true);
            ConfigCategory cat = cats.computeIfAbsent(t.category(),
                    c -> builder.getOrCreateCategory(Component.literal(c)));
            Component label = Component.literal(t.label());
            Component[] tip = t.tooltip().isEmpty() ? new Component[0] : new Component[]{Component.literal(t.tooltip())};
            try {
                if (f.getType() == boolean.class) {
                    cat.addEntry(eb.startBooleanToggle(label, f.getBoolean(cfg))
                            .setDefaultValue(f.getBoolean(defaults))
                            .setTooltip(tip)
                            .setSaveConsumer(v -> setBool(f, cfg, v))
                            .build());
                } else if (f.getType() == int.class) {
                    cat.addEntry(eb.startIntField(label, f.getInt(cfg))
                            .setDefaultValue(f.getInt(defaults))
                            .setMin(0)
                            .setTooltip(tip)
                            .setSaveConsumer(v -> setInt(f, cfg, v))
                            .build());
                }
            } catch (IllegalAccessException ignored) { }
        }

        builder.setSavingRunnable(() -> {
            Preset p = picked[0];
            if (p != null && p != cfg.preset && p != Preset.CUSTOM) {
                p.applyTo(cfg);
            } else if (!snapshot(cfg).equals(before)) {
                cfg.preset = Preset.CUSTOM;
            }
            cfg.save();
        });

        return builder.build();
    }

    private static Map<String, Object> snapshot(TurboConfig cfg) {
        Map<String, Object> m = new HashMap<>();
        for (Field f : TurboConfig.class.getDeclaredFields()) {
            if (f.getAnnotation(Tweak.class) == null) continue;
            try {
                f.setAccessible(true);
                m.put(f.getName(), f.get(cfg));
            } catch (IllegalAccessException ignored) { }
        }
        return m;
    }

    private static void setBool(Field f, TurboConfig cfg, boolean v) {
        try { f.setBoolean(cfg, v); } catch (IllegalAccessException ignored) { }
    }

    private static void setInt(Field f, TurboConfig cfg, int v) {
        try { f.setInt(cfg, v); } catch (IllegalAccessException ignored) { }
    }

    private TurboConfigScreen() {}
}
