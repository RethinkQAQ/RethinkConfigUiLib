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

/** Shared text measurement semantics for scaled RCUI text. */
public final class UiTextMetrics {
    private UiTextMetrics() { }

    public static float scale(UiTheme.UiMetrics metrics) {
        return Math.min(1f, metrics.controlHeight() / UiTheme.UiMetrics.comfortable().controlHeight());
    }

    public static float buttonScale(UiTheme.UiMetrics metrics) {
        return Math.max(.6f, scale(metrics));
    }

    public static UiText fit(UiRenderer renderer, UiText text, float width, float scale) {
        return Ui.fitText(renderer, text, width, scale);
    }

    public static void draw(UiRenderer renderer, UiText text, float x, float y,
                            float width, int color, float scale) {
        Ui.drawFittedText(renderer, text, x, y, width, color, scale);
    }
}
