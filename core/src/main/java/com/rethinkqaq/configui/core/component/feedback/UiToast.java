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

package com.rethinkqaq.configui.core.component.feedback;

import com.rethinkqaq.configui.core.UiText;
import java.util.Objects;

/** Immutable toast request. The host owns scheduling and display. */
public record UiToast(UiFeedbackType type, UiText text, long durationMillis, int customColor) {
    public UiToast {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(text, "text");
        if (durationMillis <= 0) throw new IllegalArgumentException("durationMillis");
    }

    /** Returns a non-theme-driven notification colour. */
    public int color() { return type.color(customColor); }

    public static UiToast info(UiText text) { return new UiToast(UiFeedbackType.INFO, text, 3500, 0); }
    public static UiToast success(UiText text) { return new UiToast(UiFeedbackType.SUCCESS, text, 3500, 0); }
    public static UiToast warning(UiText text) { return new UiToast(UiFeedbackType.WARNING, text, 5000, 0); }
    public static UiToast error(UiText text) { return new UiToast(UiFeedbackType.ERROR, text, 6000, 0); }
    public static UiToast custom(UiText text, int color) { return new UiToast(UiFeedbackType.CUSTOM, text, 3500, color); }
    public static UiToast custom(UiText text, int color, long durationMillis) { return new UiToast(UiFeedbackType.CUSTOM, text, durationMillis, color); }
}
