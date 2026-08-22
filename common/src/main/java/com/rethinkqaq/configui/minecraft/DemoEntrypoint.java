/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Shared demo gate used by the small loader-specific title hooks. */
public final class DemoEntrypoint {
    private static volatile boolean developmentDemoEnabled;

    private DemoEntrypoint() { }

    /**
     * Enables the bundled visual demo for a loader's local development run.
     * Production packages never call this; they remain opt-in through the JVM
     * properties handled by {@link #enabled()}.
     */
    public static void enableDevelopmentDemo() {
        developmentDemoEnabled = true;
    }

    public static boolean enabled() {
        boolean enabled = developmentDemoEnabled
            || Boolean.getBoolean("rethink_config_ui_lib_example")
            || Boolean.getBoolean("rethink_config_ui_lib.example");
        if (enabled) MinecraftSdfRenderer.prewarm();
        return enabled;
    }

    public static void open(Screen parent) {
        //? if >=26.2 {
        /*Minecraft.getInstance().gui.setScreen(new DemoScreen(parent));
        *///?} else {
        Minecraft.getInstance().setScreen(new DemoScreen(parent));
        //?}
    }
}
