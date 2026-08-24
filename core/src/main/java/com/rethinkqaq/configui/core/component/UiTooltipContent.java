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

import java.util.Objects;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;

/** Read-only content rendered inside a tooltip overlay. */
public interface UiTooltipContent {
    void measure(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme);
    void layout(UiRenderer renderer, UiBounds bounds, UiTheme theme);
    float measuredWidth();
    float measuredHeight();
    void render(UiRenderer renderer, UiTheme theme);
    default long layoutVersion() { return 0; }

    default UiText narration() { return null; }

    static UiTooltipContent node(Ui.Node node) {
        Objects.requireNonNull(node, "node");
        return new UiTooltipContent() {
            @Override public void measure(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
                node.measure(renderer, maxWidth, maxHeight, theme);
            }
            @Override public void layout(UiRenderer renderer, UiBounds bounds, UiTheme theme) {
                node.layout(renderer, bounds, theme);
            }
            @Override public float measuredWidth() { return node.measuredWidth(); }
            @Override public float measuredHeight() { return node.measuredHeight(); }
            @Override public void render(UiRenderer renderer, UiTheme theme) { node.render(renderer, theme); }
            @Override public long layoutVersion() { return node.layoutVersion(); }
        };
    }
}
