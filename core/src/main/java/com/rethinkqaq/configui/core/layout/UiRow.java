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
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiTheme;

/** A horizontal flow container. */
public class UiRow extends Ui.Container {
    private float gap = -1;

    public UiRow gap(float value) {
        gap = value;
        invalidateLayout();
        return this;
    }

    public float gap() { return gap; }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float width = 0;
        float height = 0;
        float actualGap = gap < 0 ? theme.metrics().spacing() : gap;
        for (Ui.Node child : children) {
            child.measure(renderer, maxWidth, maxHeight, theme);
            width += child.measuredWidth();
            height = Math.max(height, child.measuredHeight());
        }
        measuredWidth = Math.min(maxWidth, width + actualGap * Math.max(0, children.size() - 1));
        measuredHeight = height;
    }

    @Override
    public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        float x = value.x();
        float actualGap = gap < 0 ? theme.metrics().spacing() : gap;
        for (Ui.Node child : children) {
            child.layout(renderer, new UiBounds(x, value.y(), child.measuredWidth(), value.height()), theme);
            x += child.measuredWidth() + actualGap;
        }
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        for (Ui.Node child : children) child.render(renderer, theme);
    }
}
