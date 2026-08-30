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

package com.rethinkqaq.configui.core.layout;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiCrossAxisAlignment;
import com.rethinkqaq.configui.core.UiMainAxisAlignment;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiTheme;

/** A vertical flow container. */
public class UiColumn extends Ui.Container {
    private float gap = -1;
    private UiMainAxisAlignment mainAxisAlignment = UiMainAxisAlignment.START;
    private UiCrossAxisAlignment crossAxisAlignment = UiCrossAxisAlignment.STRETCH;

    public UiColumn gap(float value) {
        gap = value;
        invalidateLayout();
        return this;
    }

    public float gap() { return gap; }

    public UiColumn mainAxisAlignment(UiMainAxisAlignment value) {
        mainAxisAlignment = java.util.Objects.requireNonNull(value, "mainAxisAlignment");
        invalidateLayout();
        return this;
    }

    public UiMainAxisAlignment mainAxisAlignment() { return mainAxisAlignment; }

    public UiColumn crossAxisAlignment(UiCrossAxisAlignment value) {
        crossAxisAlignment = java.util.Objects.requireNonNull(value, "crossAxisAlignment");
        invalidateLayout();
        return this;
    }

    public UiCrossAxisAlignment crossAxisAlignment() { return crossAxisAlignment; }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float width = 0;
        float height = 0;
        float actualGap = gap < 0 ? theme.metrics().spacing() : gap;
        for (Ui.Node child : children) {
            child.measure(renderer, maxWidth, maxHeight, theme);
            width = Math.max(width, child.measuredWidth());
            height += child.measuredHeight();
        }
        measuredWidth = Math.min(maxWidth, width);
        measuredHeight = Math.min(maxHeight,
            Math.max(0, height + actualGap * Math.max(0, children.size() - 1)));
    }

    @Override
    public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        float childrenHeight = 0;
        for (Ui.Node child : children) childrenHeight += child.measuredHeight();
        float actualGap = gap < 0 ? theme.metrics().spacing() : gap;
        float baseGaps = actualGap * Math.max(0, children.size() - 1);
        float free = Math.max(0, value.height() - childrenHeight - baseGaps);
        float y = switch (mainAxisAlignment) {
            case START, SPACE_BETWEEN -> value.y();
            case CENTER -> value.y() + free / 2f;
            case END -> value.y() + free;
        };
        if (mainAxisAlignment == UiMainAxisAlignment.SPACE_BETWEEN && children.size() > 1) {
            actualGap += free / (children.size() - 1);
        }
        for (Ui.Node child : children) {
            float width = crossAxisAlignment == UiCrossAxisAlignment.STRETCH
                ? value.width() : Math.min(value.width(), child.measuredWidth());
            float x = switch (crossAxisAlignment) {
                case START, STRETCH -> value.x();
                case CENTER -> value.x() + Math.max(0, value.width() - width) / 2f;
                case END -> value.x() + Math.max(0, value.width() - width);
            };
            child.layout(renderer, new UiBounds(x, y, width, child.measuredHeight()), theme);
            y += child.measuredHeight() + actualGap;
        }
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        for (Ui.Node child : children) child.render(renderer, theme);
    }
}
