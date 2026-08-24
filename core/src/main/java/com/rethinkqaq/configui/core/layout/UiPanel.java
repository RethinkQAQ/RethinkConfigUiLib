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

import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiTheme;

/** A padded, themed surface containing vertically arranged children. */
public class UiPanel extends UiColumn {
    private int color = Integer.MIN_VALUE;
    private float customPadding = -1;

    public UiPanel color(int value) {
        color = value;
        return this;
    }

    public int color() { return color; }

    /** Overrides the theme padding for this panel, useful for compact headers and toolbars. */
    public UiPanel padding(float value) {
        if (value < 0) throw new IllegalArgumentException("panel padding must be non-negative");
        customPadding = value;
        invalidateLayout();
        return this;
    }

    public float padding(UiTheme theme) { return customPadding < 0 ? theme.metrics().padding() : customPadding; }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float padding = padding(theme);
        super.measureSelf(renderer, Math.max(0, maxWidth - padding * 2), maxHeight, theme);
        measuredWidth = Math.min(Math.max(0, maxWidth), measuredWidth + padding * 2);
        measuredHeight = Math.min(Math.max(0, maxHeight), measuredHeight + padding * 2);
    }

    @Override
    public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        super.layout(renderer, value.inset(padding(theme)), theme);
        bounds = value;
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        if (theme.metrics().shadowOffset() > 0) {
            renderer.fillRoundRect(bounds.offset(0, theme.metrics().shadowOffset()),
                theme.metrics().cardRadius(), 0x10000000);
        }
        renderer.fillRoundRect(bounds, theme.metrics().cardRadius(),
            color == Integer.MIN_VALUE ? theme.palette().surfaceRaised() : color);
        renderer.strokeRoundRect(bounds, theme.metrics().cardRadius(),
            theme.metrics().borderWidth(), theme.palette().border());
        super.render(renderer, theme);
    }
}
