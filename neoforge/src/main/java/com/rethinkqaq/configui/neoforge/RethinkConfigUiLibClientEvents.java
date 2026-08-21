/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.neoforge;

import com.rethinkqaq.configui.minecraft.DemoEntrypoint;
import com.rethinkqaq.configui.minecraft.FlatDemoButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Adds a button to vanilla's title screen; it never substitutes that screen. */
public final class RethinkConfigUiLibClientEvents {
    private RethinkConfigUiLibClientEvents() { }

    public static void addDemoButton(ScreenEvent.Init.Post event) {
        if (!DemoEntrypoint.enabled() || !(event.getScreen() instanceof TitleScreen)) return;
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        event.addListener(new FlatDemoButton(width / 2 - 100, Math.min(height - 35, height / 4 + 180), 200, 24,
            () -> DemoEntrypoint.open(event.getScreen())));
    }
}
