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

/** Shared ARGB colour and unit-interval utilities. */
public final class UiColor {
    private UiColor() { }
    public static int withOpacity(int rgb, float opacity) { return (channel(opacity, "opacity") << 24) | (rgb & 0x00FFFFFF); }
    public static int withAlpha(int argb, float alpha) { return (channel(alpha, "alpha") << 24) | (argb & 0x00FFFFFF); }
    public static float opacity(int argb) { return ((argb >>> 24) & 0xFF) / 255f; }
    public static int mix(int from, int to, float strength) {
        validateUnit(strength, "strength");
        return mixChannel(from >>> 24, to >>> 24, strength) << 24
            | mixChannel(from >>> 16, to >>> 16, strength) << 16
            | mixChannel(from >>> 8, to >>> 8, strength) << 8
            | mixChannel(from, to, strength);
    }
    public static float validateUnit(float value, String name) {
        if (!Float.isFinite(value) || value < 0f || value > 1f)
            throw new IllegalArgumentException(name + " must be between 0 and 1");
        return value;
    }
    private static int channel(float value, String name) { return Math.round(validateUnit(value, name) * 255f); }
    private static int mixChannel(int from, int to, float amount) { return Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * amount); }
}
