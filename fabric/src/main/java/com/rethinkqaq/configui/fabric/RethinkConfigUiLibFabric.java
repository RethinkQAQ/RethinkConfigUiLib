/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.fabric;

import com.rethinkqaq.configui.minecraft.DemoEntrypoint;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/** Client bootstrap; the optional demo button is installed by the title mixin. */
public final class RethinkConfigUiLibFabric implements ClientModInitializer {
    @Override public void onInitializeClient() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            DemoEntrypoint.enableDevelopmentDemo();
        }
    }
}
