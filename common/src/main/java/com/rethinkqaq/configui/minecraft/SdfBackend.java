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
package com.rethinkqaq.configui.minecraft;

import com.rethinkqaq.configui.core.UiBounds;

/** Internal contract shared by compile-time-selected Minecraft SDF backends. */
interface SdfBackend<G> {
    /** Returns false when the caller should draw the safe rounded fallback instead. */
    boolean draw(G graphics, UiBounds box, float radius, float stroke, int color, boolean outline, float coordinateScale);

    /** Most backends initialize lazily and need no explicit prewarm step. */
    default void prewarm() { }
}
