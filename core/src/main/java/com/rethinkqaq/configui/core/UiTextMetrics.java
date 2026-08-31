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

import java.util.List;
import java.util.Objects;

/** Shared text measurement semantics for scaled RCUI text. */
public final class UiTextMetrics {
    private UiTextMetrics() { }

    public static float scale(UiTheme.UiMetrics metrics) {
        return Math.min(1f, metrics.controlHeight() / UiTheme.UiMetrics.comfortable().controlHeight());
    }

    public static float buttonScale(UiTheme.UiMetrics metrics) {
        return Math.max(.6f, scale(metrics));
    }

    /** Returns the body-text scale for the current density. */
    public static float bodyScale(UiTheme.UiMetrics metrics) {
        return Math.max(.75f, scale(metrics));
    }

    public static float lineHeight(UiRenderer renderer, float scale) {
        return renderer.lineHeight(Math.max(0f, scale));
    }

    /** Returns the largest readable scale that keeps text within the available width. */
    public static float fitScale(UiRenderer renderer, UiText text, float width) {
        if (width <= 0 || renderer.textWidth(text) <= 0) return 1f;
        return Math.min(1f, Math.max(.6f, width / renderer.textWidth(text)));
    }

    public static UiText fit(UiRenderer renderer, UiText text, float width, float scale) {
        return Ui.fitText(renderer, text, width, scale);
    }

    public static void draw(UiRenderer renderer, UiText text, float x, float y,
                            float width, int color, float scale) {
        Ui.drawFittedText(renderer, text, x, y, width, color, scale);
    }

    /** Measures, wraps and draws a text block with one consistent text scale. */
    public static Block block(UiRenderer renderer, UiText text, float width, int maxLines,
                              float scale, float lineGap, boolean ellipsis) {
        Objects.requireNonNull(renderer, "renderer");
        Objects.requireNonNull(text, "text");
        if (width <= 0 || maxLines <= 0 || scale <= 0) return new Block(List.of(), scale, 0, lineGap);
        // Wrap at the requested density first.  Shrinking an entire paragraph before wrapping
        // makes long English translations look unexpectedly smaller than equivalent CJK text.
        float resolvedScale = scale;
        List<UiText> lines = Ui.wrapLines(renderer, text, width / resolvedScale, maxLines, ellipsis);
        for (UiText line : lines) {
            resolvedScale = Math.min(resolvedScale, fitScale(renderer, line, width));
        }
        if (resolvedScale < scale) {
            lines = Ui.wrapLines(renderer, text, width / Math.max(.0001f, resolvedScale), maxLines, ellipsis);
        }
        return new Block(lines, resolvedScale, lineHeight(renderer, resolvedScale), Math.max(0, lineGap));
    }

    public static void drawBlock(UiRenderer renderer, Block block, float x, float y, float width,
                                 int color, boolean centered) {
        for (int index = 0; index < block.lines().size(); index++) {
            UiText line = fit(renderer, block.lines().get(index), width, block.scale());
            float lineX = centered ? x + Math.max(0, width - renderer.textWidth(line, block.scale())) / 2f : x;
            draw(renderer, line, lineX, y + index * (block.lineHeight() + block.lineGap()), width, color, block.scale());
        }
    }

    public record Block(List<UiText> lines, float scale, float lineHeight, float lineGap) {
        public Block { lines = List.copyOf(lines); }
        public float height() {
            return lines.isEmpty() ? 0 : lines.size() * lineHeight + Math.max(0, lines.size() - 1) * lineGap;
        }
    }
}
