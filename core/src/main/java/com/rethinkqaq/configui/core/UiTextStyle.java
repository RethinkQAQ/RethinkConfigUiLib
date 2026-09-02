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

/** Immutable, platform-independent visual style for text. */
public final class UiTextStyle {
    private final float scale;
    private final Integer color;
    private final UiTextRole role;
    private final UiTextOverflow overflow;

    private UiTextStyle(float scale, Integer color, UiTextRole role, UiTextOverflow overflow) {
        if (!Float.isFinite(scale) || scale <= 0) throw new IllegalArgumentException("text scale must be finite and positive");
        this.scale = scale;
        this.color = color;
        this.role = Objects.requireNonNull(role, "role");
        this.overflow = Objects.requireNonNull(overflow, "overflow");
    }

    public static UiTextStyle of(UiTextRole role) { return new UiTextStyle(1f, null, role, UiTextOverflow.NO_WRAP); }
    public static UiTextStyle title() { return of(UiTextRole.TITLE); }
    public static UiTextStyle subtitle() { return of(UiTextRole.SUBTITLE); }
    public static UiTextStyle body() { return of(UiTextRole.PRIMARY); }
    public static UiTextStyle secondary() { return of(UiTextRole.SECONDARY); }
    public static UiTextStyle button() { return of(UiTextRole.BUTTON); }
    public static UiTextStyle caption() { return of(UiTextRole.CAPTION); }
    public static UiTextStyle error() { return of(UiTextRole.ERROR); }
    public static UiTextStyle success() { return of(UiTextRole.SUCCESS); }

    public UiTextStyle scale(float value) { return new UiTextStyle(value, color, role, overflow); }
    public UiTextStyle color(int value) { return new UiTextStyle(scale, value, role, overflow); }
    public UiTextStyle role(UiTextRole value) { return new UiTextStyle(scale, color, value, overflow); }
    public UiTextStyle overflow(UiTextOverflow value) { return new UiTextStyle(scale, color, role, value); }
    public float scale() { return scale; }
    public Integer colorOverride() { return color; }
    public UiTextRole role() { return role; }
    public UiTextOverflow overflow() { return overflow; }
}
