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

import com.rethinkqaq.configui.RethinkConfigUiLib;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Loader marker; the optional demo is enabled only by an explicit JVM property. */
@Mod(RethinkConfigUiLib.MOD_ID)
public final class RethinkConfigUiLibForge {
    public RethinkConfigUiLibForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            RethinkConfigUiLib.LOGGER.info("RCUI Forge bootstrap: dist=CLIENT, production={}, demoEnabled={}",
                FMLEnvironment.production,
                Boolean.getBoolean("rethink_config_ui_lib_example"));
            //? if >=1.21.6 {
            /*RethinkConfigUiLibClientEvents.register();
            *///?} else {
            MinecraftForge.EVENT_BUS.addListener(RethinkConfigUiLibClientEvents::addDemoButton);
            //?}
        }
    }
}
