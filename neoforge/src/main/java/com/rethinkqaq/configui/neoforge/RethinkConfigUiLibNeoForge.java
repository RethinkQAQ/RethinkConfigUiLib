/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.neoforge;

import com.rethinkqaq.configui.minecraft.RethinkConfigUiLib;
import net.neoforged.fml.common.Mod;
import com.rethinkqaq.configui.minecraft.DemoLauncher;

/** Loader marker; the optional demo is deliberately opt-in. */
@Mod(RethinkConfigUiLib.MOD_ID)
public final class RethinkConfigUiLibNeoForge {
    public RethinkConfigUiLibNeoForge() {
        if (Boolean.getBoolean("rethink_config_ui_lib_example") || Boolean.getBoolean("rethink_config_ui_lib.example")) {
            DemoLauncher.install();
        }
    }
}
