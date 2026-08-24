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
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;

/** A transparent wrapper carrying tooltip metadata for a host overlay. */
public class UiTooltip extends Ui.Node {
    public enum TextOverflow { WRAP, ELLIPSIS, CLIP, NO_WRAP }

    private final Ui.Node child;
    private final UiText tooltip;
    private final UiTooltipContent content;
    private long hoverStartedNanos = -1;
    private long delayMillis = 400;
    private float minWidth;
    private float maxWidth = 320;
    private float maxHeight = Float.MAX_VALUE;
    private int maxLines = Integer.MAX_VALUE;
    private float padding = -1;
    private float lineGap;
    private TextOverflow overflow = TextOverflow.WRAP;

    public UiTooltip(Ui.Node child, UiText tooltip) {
        this.child = Objects.requireNonNull(child, "child");
        this.tooltip = Objects.requireNonNull(tooltip, "tooltip");
        this.content = null;
    }

    public UiTooltip(Ui.Node child, UiTooltipContent content) {
        this.child = Objects.requireNonNull(child, "child");
        this.tooltip = null;
        this.content = Objects.requireNonNull(content, "content");
    }

    public Ui.Node child() { return child; }
    public UiText text() { return tooltip; }
    public UiTooltipContent content() { return content; }
    public boolean hasText() { return tooltip != null; }
    public boolean hasContent() { return content != null; }
    public UiTooltip delayMillis(long value) { if (value < 0) throw new IllegalArgumentException("delay"); delayMillis = value; return this; }
    public UiTooltip minWidth(float value) { if (value < 0) throw new IllegalArgumentException("minWidth"); minWidth = value; invalidateLayout(); return this; }
    public UiTooltip maxWidth(float value) { if (value <= 0) throw new IllegalArgumentException("maxWidth"); maxWidth = value; invalidateLayout(); return this; }
    public UiTooltip maxHeight(float value) { if (value <= 0) throw new IllegalArgumentException("maxHeight"); maxHeight = value; invalidateLayout(); return this; }
    public UiTooltip maxLines(int value) { if (value <= 0) throw new IllegalArgumentException("maxLines"); maxLines = value; invalidateLayout(); return this; }
    public UiTooltip padding(float value) { if (value < 0) throw new IllegalArgumentException("padding"); padding = value; invalidateLayout(); return this; }
    public UiTooltip lineGap(float value) { if (value < 0) throw new IllegalArgumentException("lineGap"); lineGap = value; invalidateLayout(); return this; }
    public UiTooltip overflow(TextOverflow value) { overflow = Objects.requireNonNull(value, "overflow"); invalidateLayout(); return this; }
    public float minWidth() { return minWidth; }
    public float maxWidth() { return maxWidth; }
    public float maxHeight() { return maxHeight; }
    public int maxLines() { return maxLines; }
    public float padding(UiTheme theme) { return padding < 0 ? theme.metrics().padding() / 2f : padding; }
    public float lineGap() { return lineGap; }
    public TextOverflow overflow() { return overflow; }
    public boolean visible(long nowNanos) { return (hovered() || child.focused()) && hoverStartedNanos >= 0 && (nowNanos - hoverStartedNanos) / 1_000_000L >= delayMillis; }
    @Override public void setHovered(boolean value) {
        boolean changed = value != hovered(); super.setHovered(value);
        if (value && changed) hoverStartedNanos = System.nanoTime();
        if (!value) hoverStartedNanos = -1;
    }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        child.measure(renderer, maxWidth, maxHeight, theme);
        measuredWidth = child.measuredWidth();
        measuredHeight = child.measuredHeight();
    }

    @Override public void layout(UiRenderer renderer, com.rethinkqaq.configui.core.UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        child.layout(renderer, value, theme);
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) { child.render(renderer, theme); }
    @Override public boolean click(float x, float y, int button) { return child.click(x, y, button); }
    @Override public boolean scroll(float x, float y, double amount) { return child.scroll(x, y, amount); }
    @Override public boolean drag(float x, float y, int button) { return child.drag(x, y, button); }
    @Override public boolean key(int keyCode) { return child.key(keyCode); }
    @Override public boolean release(float x, float y, int button) { return child.release(x, y, button); }
    @Override public boolean focusable() { return false; }
}
