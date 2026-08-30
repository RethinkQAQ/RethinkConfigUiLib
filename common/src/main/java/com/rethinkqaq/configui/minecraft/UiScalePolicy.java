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

import com.rethinkqaq.configui.core.UiDensity;

/** Describes how a UI surface relates to Minecraft's GUI Scale setting. */
public final class UiScalePolicy {
    private static final UiScalePolicy MINECRAFT = new UiScalePolicy(null);
    private final UiDensity fixedDensity;

    private UiScalePolicy(UiDensity fixedDensity) { this.fixedDensity = fixedDensity; }

    /** Uses Minecraft's selected GUI scale without additional UI scaling. */
    public static UiScalePolicy minecraft() { return MINECRAFT; }

    /** Compatibility policy for hosts that explicitly opt into adaptive sizing. */
    public static UiScalePolicy adaptive() { return MINECRAFT; }

    /** Uses one density regardless of Minecraft's selected GUI scale. */
    public static UiScalePolicy fixed(UiDensity density) {
        return new UiScalePolicy(java.util.Objects.requireNonNull(density, "density"));
    }

    /** Resolves the template density for the selected Minecraft GUI scale. */
    public UiDensity density(double minecraftGuiScale) {
        if (fixedDensity != null) return fixedDensity;
        int scale = (int) Math.round(minecraftGuiScale);
        if (scale <= 2) return UiDensity.COMFORTABLE;
        if (scale <= 4) return UiDensity.NORMAL;
        return UiDensity.COMPACT;
    }

    /**
     * Returns the identity transform. Minecraft has already converted the framebuffer into the
     * logical {@code Screen} coordinate system before a Screen is rendered; applying another
     * inverse transform makes high GUI scales grow the canvas and causes clipping.
     */
    public float contentScale(double minecraftGuiScale) { return 1f; }
}
