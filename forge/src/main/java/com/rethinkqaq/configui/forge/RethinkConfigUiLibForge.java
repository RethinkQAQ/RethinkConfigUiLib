/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.forge;

import com.rethinkqaq.configui.minecraft.DemoEntrypoint;
import com.rethinkqaq.configui.minecraft.RethinkConfigUiLib;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Loader marker; the optional demo is enabled automatically only in development. */
@Mod(RethinkConfigUiLib.MOD_ID)
public final class RethinkConfigUiLibForge {
    public RethinkConfigUiLibForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            if (!FMLEnvironment.production) {
                DemoEntrypoint.enableDevelopmentDemo();
            }
            RethinkConfigUiLib.LOGGER.info("RCUI Forge bootstrap: dist=CLIENT, production={}, demoEnabled={}",
                FMLEnvironment.production,
                DemoEntrypoint.enabled());
            //? if >=1.21.8 {
            /*RethinkConfigUiLibClientEvents.register();
            *///?} else {
            MinecraftForge.EVENT_BUS.addListener(RethinkConfigUiLibClientEvents::addDemoButton);
            //?}
        }
    }
}
