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

/** Rendering capability implemented by the host platform. */
public interface UiRenderer {
    void fillRect(UiBounds bounds, int color);
    void fillRoundRect(UiBounds bounds, float radius, int color);
    void strokeRoundRect(UiBounds bounds, float radius, float width, int color);
    void drawText(UiText text, float x, float y, int color);
    /** Draws text at a scale while keeping the unscaled API source-compatible. */
    default void drawText(UiText text, float x, float y, int color, float scale) {
        drawText(text, x, y, color);
    }
    default void drawCenteredText(UiText text, float centerX, float y, int color) {
        drawText(text, centerX - textWidth(text) / 2f, y, color);
    }
    float textWidth(UiText text);
    default float textWidth(UiText text, float scale) { return textWidth(text) * scale; }
    float lineHeight();
    default float lineHeight(float scale) { return lineHeight() * scale; }
    void pushClip(UiBounds bounds);
    void popClip();
}
