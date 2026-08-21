/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.fabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the opt-in button after vanilla has finished building TitleScreen. */
@Mixin(TitleScreen.class)
abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void rethinkConfigUiLib$addDemoButton(CallbackInfo callbackInfo) {
        if (Boolean.getBoolean("rethink_config_ui_lib_example") || Boolean.getBoolean("rethink_config_ui_lib.example")) {
            addRenderableWidget(rethinkConfigUiLib$createButton((Screen) (Object) this, width / 2 - 100, Math.min(height - 35, height / 4 + 180)));
        }
    }

    private static AbstractWidget rethinkConfigUiLib$createButton(Screen parent, int x, int y) {
        try {
            Class<?> type = Class.forName("com.rethinkqaq.configui.minecraft.FlatDemoButton");
            return (AbstractWidget) type.getConstructor(int.class, int.class, int.class, int.class, Runnable.class)
                .newInstance(x, y, 200, 24, (Runnable) () -> Minecraft.getInstance().setScreen(rethinkConfigUiLib$createDemo(parent)));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("RCUI FlatDemoButton is unavailable", exception);
        }
    }

    private static Screen rethinkConfigUiLib$createDemo(Screen parent) {
        try {
            return (Screen) Class.forName("com.rethinkqaq.configui.minecraft.DemoScreen")
                .getConstructor(Screen.class)
                .newInstance(parent);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("RCUI DemoScreen is unavailable", exception);
        }
    }
}
