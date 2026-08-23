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
package com.rethinkqaq.configui.forge;

import com.rethinkqaq.configui.minecraft.DemoEntrypoint;
import com.rethinkqaq.configui.minecraft.FlatDemoButton;
import com.rethinkqaq.configui.minecraft.RethinkConfigUiLib;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.client.event.ScreenEvent;

/** Adds the development demo entry after vanilla has initialized the title screen. */
public final class RethinkConfigUiLibClientEvents {
    private static final int DEMO_BUTTON_X = 6;
    private static final int DEMO_BUTTON_Y = 6;
    private static final int DEMO_BUTTON_WIDTH = 200;
    private static final int DEMO_BUTTON_HEIGHT = 24;

    private RethinkConfigUiLibClientEvents() { }

    public static void addDemoButton(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!DemoEntrypoint.enabled() || !(screen instanceof TitleScreen)) return;

        // Keep the shared RCUI button in the top-left corner, outside the
        // centered logo and vanilla title actions at every GUI scale.
        event.addListener(new FlatDemoButton(DEMO_BUTTON_X, DEMO_BUTTON_Y,
            DEMO_BUTTON_WIDTH, DEMO_BUTTON_HEIGHT, () -> DemoEntrypoint.open(screen)));
        RethinkConfigUiLib.LOGGER.info(
            "RCUI Forge title demo entry added at ({}, {}, {}, {})",
            DEMO_BUTTON_X,
            DEMO_BUTTON_Y,
            DEMO_BUTTON_WIDTH,
            DEMO_BUTTON_HEIGHT
        );
    }

    // Forge 1.21.8+ exposes a typed bus for each screen event. Older nodes
    // register the same listener on MinecraftForge.EVENT_BUS in the mod entrypoint.
    //? if >=1.21.8 {
    /*
    public static void register() {
        ScreenEvent.Init.Post.BUS.addListener(RethinkConfigUiLibClientEvents::addDemoButton);
    }
    *///?}
}
