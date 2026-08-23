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

package com.rethinkqaq.configui.core.component;

import java.util.List;
import java.util.Objects;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;

/** A text label with optional bounded wrapping. */
public class UiLabel extends Ui.Node {
    private final UiText text;
    private boolean wrapped;
    private int maxLines = 3;

    public UiLabel(UiText text) { this.text = Objects.requireNonNull(text, "text"); }

    public UiText text() { return text; }

    public UiLabel wrap(boolean value) { wrapped = value; invalidateLayout(); return this; }
    public boolean wrapped() { return wrapped; }
    public UiLabel maxLines(int value) {
        if (value <= 0) throw new IllegalArgumentException("maxLines must be positive");
        maxLines = value;
        invalidateLayout();
        return this;
    }
    public int maxLines() { return maxLines; }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        List<UiText> lines = wrapped ? Ui.wrapLines(renderer, text, maxWidth, maxLines) : List.of(text);
        measuredWidth = Math.min(maxWidth,
            (float) lines.stream().mapToDouble(renderer::textWidth).max().orElse(0));
        measuredHeight = Math.min(maxHeight, renderer.lineHeight() * lines.size());
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        if (wrapped) Ui.drawWrappedText(renderer, text, bounds.x(), bounds.y(), bounds.width(),
            maxLines, theme.palette().textSecondary(), 0);
        else Ui.drawFittedText(renderer, text, bounds.x(), bounds.y(), bounds.width(),
            theme.palette().textSecondary());
    }
}
