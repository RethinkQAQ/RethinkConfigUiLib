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
import com.rethinkqaq.configui.core.UiKey;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiTheme;

/** A single-child scrolling viewport. */
public class UiScrollView extends Ui.Container {
    private float offset;

    public UiScrollView(Ui.Node child) { add(child); }

    protected Ui.Node child() { return children.get(0); }
    public float offset() { return offset; }
    public void reset() { offset = 0; }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        child().measure(renderer, maxWidth, Float.MAX_VALUE, theme);
        measuredWidth = child().measuredWidth();
        measuredHeight = Math.min(maxHeight, child().measuredHeight());
    }

    @Override
    public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        child().layout(renderer, new UiBounds(value.x(), value.y() - offset, value.width(), child().measuredHeight()), theme);
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        renderer.pushClip(bounds);
        child().render(renderer, theme);
        renderer.popClip();
    }

    @Override
    public boolean scroll(float x, float y, double amount) {
        if (!bounds.contains(x, y)) return false;
        offset = Math.max(0, Math.min(Math.max(0, child().measuredHeight() - bounds.height()),
            (float) (offset - amount * 14)));
        return true;
    }

    @Override
    public boolean key(int keyCode) {
        float max = Math.max(0, child().measuredHeight() - bounds.height());
        float page = Math.max(1, bounds.height() - 14);
        if (keyCode == UiKey.UP) { offset = Math.max(0, offset - 14); return true; }
        if (keyCode == UiKey.DOWN) { offset = Math.min(max, offset + 14); return true; }
        if (keyCode == UiKey.PAGE_UP) { offset = Math.max(0, offset - page); return true; }
        if (keyCode == UiKey.PAGE_DOWN) { offset = Math.min(max, offset + page); return true; }
        if (keyCode == UiKey.HOME) { offset = 0; return true; }
        if (keyCode == UiKey.END) { offset = max; return true; }
        return child().key(keyCode);
    }
}
