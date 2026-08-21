/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.minecraft;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

/** Vanilla title screen with an opt-in RCUI demo entry. */
public final class RethinkTitleScreen extends TitleScreen {
    @Override protected void init() {
        super.init();
        int y = Math.min(height - 35, height / 4 + 180);
        addRenderableWidget(Button.builder(Component.literal("RCUI Demo"), button -> minecraft.setScreen(new DemoScreen(this)))
            .bounds(width / 2 - 100, y, 200, 20)
            .build());
    }
}
