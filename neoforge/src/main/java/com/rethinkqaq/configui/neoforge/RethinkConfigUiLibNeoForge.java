/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.neoforge;

import com.rethinkqaq.configui.minecraft.DemoEntrypoint;
import com.rethinkqaq.configui.minecraft.RethinkConfigUiLib;
import net.neoforged.fml.common.Mod;
//? if >=1.21.11 {
/*import net.neoforged.fml.loading.FMLLoader;
*///?} else {
import net.neoforged.fml.loading.FMLEnvironment;
//?}
import net.neoforged.neoforge.common.NeoForge;

/** Loader marker; the optional demo is enabled automatically only in development. */
@Mod(RethinkConfigUiLib.MOD_ID)
public final class RethinkConfigUiLibNeoForge {
    public RethinkConfigUiLibNeoForge() {
        //? if >=1.21.11 {
        /*if (!FMLLoader.getCurrent().isProduction()) {
        *///?} else {
        if (!FMLEnvironment.production) {
        //?}
            DemoEntrypoint.enableDevelopmentDemo();
        }
        NeoForge.EVENT_BUS.addListener(RethinkConfigUiLibClientEvents::addDemoButton);
    }
}
