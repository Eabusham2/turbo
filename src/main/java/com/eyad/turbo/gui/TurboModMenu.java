package com.eyad.turbo.gui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Registers the Turbo config screen with ModMenu's mod-list config button. */
public class TurboModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TurboConfigScreen::create;
    }
}
