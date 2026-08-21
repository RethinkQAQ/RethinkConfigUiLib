/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;

/** Installs the opt-in demo only after Minecraft has created its title screen. */
public final class DemoLauncher {
    private DemoLauncher() { }

    public static void install() {
        Thread waiter = new Thread(() -> {
            Minecraft client = Minecraft.getInstance();
            while (client.screen == null || !(client.screen instanceof TitleScreen) || client.screen instanceof RethinkTitleScreen) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            client.execute(() -> {
                if (client.screen instanceof TitleScreen && !(client.screen instanceof RethinkTitleScreen)) {
                    //? if >=26.2 {
                    /*client.gui.setScreen(new RethinkTitleScreen());
                    *///?} else {
                    client.setScreen(new RethinkTitleScreen());
                    //?}
                }
            });
        }, "RCUI-Demo-TitleScreen-Waiter");
        waiter.setDaemon(true);
        waiter.start();
    }
}
