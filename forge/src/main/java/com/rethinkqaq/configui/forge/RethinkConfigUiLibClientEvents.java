/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.forge;

import com.rethinkqaq.configui.minecraft.DemoEntrypoint;
import com.rethinkqaq.configui.minecraft.FlatDemoButton;
import com.rethinkqaq.configui.minecraft.RethinkConfigUiLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
//? if >=1.21.8 {
/*import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
*///?} else {
import net.minecraftforge.eventbus.api.SubscribeEvent;
//?}
import net.minecraftforge.fml.common.Mod;

/** Adds a button to vanilla's title screen; it never substitutes that screen. */
@Mod.EventBusSubscriber(modid = RethinkConfigUiLib.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RethinkConfigUiLibClientEvents {
    private RethinkConfigUiLibClientEvents() { }

    @SubscribeEvent
    public static void addDemoButton(ScreenEvent.Init.Post event) {
        if (!DemoEntrypoint.enabled() || !(event.getScreen() instanceof TitleScreen)) return;
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        event.addListener(new FlatDemoButton(width / 2 - 100, Math.min(height - 35, height / 4 + 180), 200, 24,
            () -> DemoEntrypoint.open(event.getScreen())));
    }
}
