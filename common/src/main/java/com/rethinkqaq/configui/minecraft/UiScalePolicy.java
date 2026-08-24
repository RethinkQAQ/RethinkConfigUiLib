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

/** Describes how a UI surface relates to Minecraft's GUI Scale setting. */
public final class UiScalePolicy {
    private static final UiScalePolicy MINECRAFT = new UiScalePolicy();

    private UiScalePolicy() { }

    /** Uses Minecraft's selected GUI scale without additional UI scaling. */
    public static UiScalePolicy minecraft() { return MINECRAFT; }

    /**
     * Returns the identity transform. Minecraft has already converted the framebuffer into the
     * logical {@code Screen} coordinate system before a Screen is rendered; applying another
     * inverse transform makes high GUI scales grow the canvas and causes clipping.
     */
    public float contentScale(double minecraftGuiScale) { return 1f; }
}
