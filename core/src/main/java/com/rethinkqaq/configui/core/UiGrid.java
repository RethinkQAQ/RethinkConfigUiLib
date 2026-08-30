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

import java.util.ArrayList;
import java.util.List;

/** A responsive grid which chooses its column count from the available logical width. */
public final class UiGrid extends Ui.Container {
    /** Horizontal alignment of each row's cards inside the grid viewport. */
    public enum Alignment { START, CENTER, END }

    private float minimumColumnWidth = 156;
    // Keep preview cards portrait-oriented on wide screens by default. Hosts can override this
    // for specialized content through maximumColumnWidth(...).
    private float maximumColumnWidth = 188;
    private float gap = -1;
    private Alignment alignment = Alignment.CENTER;
    private int columns = 1;
    private float cellWidth;
    private final List<Float> rowHeights = new ArrayList<>();

    public UiGrid minimumColumnWidth(float value) {
        if (value <= 0) throw new IllegalArgumentException("minimumColumnWidth must be positive");
        minimumColumnWidth = value;
        return this;
    }

    /** Caps card width while retaining responsive column selection. */
    public UiGrid maximumColumnWidth(float value) {
        if (value <= 0) throw new IllegalArgumentException("maximumColumnWidth must be positive");
        maximumColumnWidth = value;
        return this;
    }

    public float maximumColumnWidth() { return maximumColumnWidth; }

    public UiGrid gap(float value) {
        if (value < 0) throw new IllegalArgumentException("gap must be non-negative");
        gap = value;
        return this;
    }

    public int columns() { return columns; }

    /** Sets how each row is positioned when it does not fill the grid width. */
    public UiGrid alignment(Alignment value) {
        alignment = java.util.Objects.requireNonNull(value, "alignment");
        invalidateLayout();
        return this;
    }

    public Alignment alignment() { return alignment; }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float actualGap = gap < 0 ? theme.metrics().spacing() : gap;
        columns = Math.max(1, (int) Math.floor((maxWidth + actualGap) / (minimumColumnWidth + actualGap)));
        cellWidth = Math.min(maximumColumnWidth,
            Math.max(0, (maxWidth - actualGap * (columns - 1)) / columns));
        rowHeights.clear();

        for (int index = 0; index < children.size(); index++) {
            Ui.Node child = children.get(index);
            child.measure(renderer, cellWidth, maxHeight, theme);
            int row = index / columns;
            while (rowHeights.size() <= row) rowHeights.add(0f);
            rowHeights.set(row, Math.max(rowHeights.get(row), child.measuredHeight()));
        }

        float height = 0;
        for (float rowHeight : rowHeights) height += rowHeight;
        height += actualGap * Math.max(0, rowHeights.size() - 1);
        measuredWidth = maxWidth;
        measuredHeight = Math.min(maxHeight, height);
    }

    @Override
    public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        float actualGap = gap < 0 ? theme.metrics().spacing() : gap;
        cellWidth = Math.min(maximumColumnWidth,
            Math.max(0, (value.width() - actualGap * (columns - 1)) / columns));
        float y = value.y();
        int index = 0;
        for (int row = 0; row < rowHeights.size(); row++) {
            float rowHeight = rowHeights.get(row);
            int rowItems = Math.min(columns, children.size() - index);
            float rowWidth = rowItems * cellWidth + actualGap * Math.max(0, rowItems - 1);
            float freeWidth = Math.max(0, value.width() - rowWidth);
            float rowX = value.x() + switch (alignment) {
                case START -> 0;
                case CENTER -> freeWidth / 2f;
                case END -> freeWidth;
            };
            for (int column = 0; column < rowItems && index < children.size(); column++, index++) {
                children.get(index).layout(renderer, new UiBounds(
                    rowX + column * (cellWidth + actualGap), y, cellWidth, rowHeight), theme);
            }
            y += rowHeight + actualGap;
        }
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        for (Ui.Node child : children) child.render(renderer, theme);
    }
}
