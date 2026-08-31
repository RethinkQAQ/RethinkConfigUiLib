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

package com.rethinkqaq.configui.core;

import java.util.Objects;

/** Describes how a host paints the surface behind its UI tree. */
public record UiBackground(Mode mode, int color) {
    public enum Mode { OPAQUE, TRANSLUCENT, TRANSPARENT }

    public UiBackground {
        Objects.requireNonNull(mode, "mode");
        if (mode == Mode.OPAQUE) color = UiColor.withAlpha(color, 1f);
    }

    public static UiBackground opaque(int color) { return new UiBackground(Mode.OPAQUE, color); }
    public static UiBackground translucent(int color) { return new UiBackground(Mode.TRANSLUCENT, color); }
    public static UiBackground opaqueRgb(int rgb) { return opaque(UiColor.withOpacity(rgb, 1f)); }
    public static UiBackground translucent(int rgb, float opacity) {
        return new UiBackground(Mode.TRANSLUCENT, UiColor.withOpacity(rgb, opacity));
    }
    public static UiBackground transparent() { return new UiBackground(Mode.TRANSPARENT, 0); }

    public boolean paintsSurface() { return mode != Mode.TRANSPARENT; }
}
