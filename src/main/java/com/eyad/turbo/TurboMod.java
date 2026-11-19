package com.eyad.turbo;

import com.eyad.turbo.config.TurboConfig;
import com.eyad.turbo.config.Tweak;
import com.eyad.turbo.util.RenderStats;
import com.eyad.turbo.gui.TurboConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.lang.reflect.Field;

public class TurboMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Turbo");

    @Override
    public void onInitializeClient() {
        TurboConfig cfg = TurboConfig.get();
        if (cfg.logStartup) {
            int settings = 0;
            for (Field f : TurboConfig.class.getDeclaredFields()) {
                if (f.getAnnotation(Tweak.class) != null) settings++;
            }
            LOGGER.info("[Turbo] loaded for 1.21.11: {} settings, preset={}, masterEnable={}",
                    settings, cfg.preset, cfg.masterEnable);
        }

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("turbo", "debug"), (graphics, delta) -> {
            TurboConfig c = TurboConfig.get();
            if (!c.debugOverlay) return;
            Minecraft mc = Minecraft.getInstance();
            String text = "Turbo " + c.preset.name().toLowerCase()
                    + " | " + mc.getFps() + " fps"
                    + " | particles " + mc.particleEngine.countParticles()
                    + " | skipped: " + RenderStats.lastEntities + " entities, "
                    + RenderStats.lastBlockEntities + " block entities, "
                    + RenderStats.lastOccluded + " behind walls";
            graphics.drawString(mc.font, text, graphics.guiWidth() - mc.font.width(text) - 2, 2, 0xFFFFFFFF);
        });

        // Dev check only: -Dturbo.mixinAudit=true forces every Turbo mixin to apply now,
        // so a bad target fails at startup instead of when that class first loads.
        if (Boolean.getBoolean("turbo.mixinAudit")) {
            LOGGER.info("[Turbo] running Mixin audit...");
            MixinEnvironment.getCurrentEnvironment().audit();
            LOGGER.info("[Turbo] Mixin audit finished");
        }

        // Dev check only: -Dturbo.openConfigOnJoin=true opens the settings screen once you are in a world.
        if (Boolean.getBoolean("turbo.openConfigOnJoin")) {
            ClientTickEvents.END_CLIENT_TICK.register(new ClientTickEvents.EndTick() {
                private int ticksInWorld;

                @Override
                public void onEndTick(Minecraft mc) {
                    if (mc.level == null || ticksInWorld < 0) return;
                    if (++ticksInWorld == 100) {
                        mc.setScreen(TurboConfigScreen.create(mc.screen));
                        LOGGER.info("[Turbo] opened settings screen");
                        ticksInWorld = -1;
                    }
                }
            });
        }
    }
}
