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
package com.rethinkqaq.configui.neoforge;

import com.rethinkqaq.configui.minecraft.RethinkConfigUiLib;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Loader marker; the optional demo is enabled only by an explicit JVM property. */
@Mod(RethinkConfigUiLib.MOD_ID)
public final class RethinkConfigUiLibNeoForge {
    public RethinkConfigUiLibNeoForge() {
        NeoForge.EVENT_BUS.addListener(RethinkConfigUiLibClientEvents::addDemoButton);
    }
}
