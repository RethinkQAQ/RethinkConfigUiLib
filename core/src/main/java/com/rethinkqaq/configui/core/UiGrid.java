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
    private float minimumColumnWidth = 156;
    // Keep preview cards portrait-oriented on wide screens by default. Hosts can override this
    // for specialized content through maximumColumnWidth(...).
    private float maximumColumnWidth = 188;
    private float gap = -1;
    private UiMainAxisAlignment rowAlignment = UiMainAxisAlignment.CENTER;
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

    @Override public UiGrid add(Ui.Node child) {
        super.add(child);
        return this;
    }

    public int columns() { return columns; }

    /** Sets how items are placed within each row. The grid itself is aligned by its parent. */
    public UiGrid rowAlignment(UiMainAxisAlignment value) {
        rowAlignment = java.util.Objects.requireNonNull(value, "rowAlignment");
        invalidateLayout();
        return this;
    }

    public UiMainAxisAlignment rowAlignment() { return rowAlignment; }

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
        // Expose the grid's real footprint so a parent Column can center the whole grid.
        // Row alignment then only affects incomplete rows inside that footprint.
        measuredWidth = Math.min(maxWidth, columns * cellWidth + actualGap * Math.max(0, columns - 1));
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
            float rowGap = actualGap;
            float rowX = value.x() + switch (rowAlignment) {
                case START, SPACE_BETWEEN -> 0;
                case CENTER -> freeWidth / 2f;
                case END -> freeWidth;
            };
            if (rowAlignment == UiMainAxisAlignment.SPACE_BETWEEN && rowItems > 1) {
                rowGap += freeWidth / (rowItems - 1);
            }
            for (int column = 0; column < rowItems && index < children.size(); column++, index++) {
                children.get(index).layout(renderer, new UiBounds(
                    rowX + column * (cellWidth + rowGap), y, cellWidth, rowHeight), theme);
            }
            y += rowHeight + actualGap;
        }
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        for (Ui.Node child : children) child.render(renderer, theme);
    }
}
