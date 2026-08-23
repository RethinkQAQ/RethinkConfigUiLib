/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 *
 * This file is part of Rethink Config UI Lib.
 *
 * Rethink Config UI Lib is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, version 3 of the License.
 *
 * Rethink Config UI Lib is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Rethink Config UI Lib. If not, see <https://www.gnu.org/licenses/>.
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
