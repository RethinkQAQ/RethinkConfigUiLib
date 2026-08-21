/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.core;

import java.util.ArrayList;
import java.util.List;

/** A responsive grid which chooses its column count from the available logical width. */
public final class UiGrid extends Ui.Container {
    private float minimumColumnWidth = 168;
    private float gap = -1;
    private int columns = 1;
    private final List<Float> rowHeights = new ArrayList<>();

    public UiGrid minimumColumnWidth(float value) {
        if (value <= 0) throw new IllegalArgumentException("minimumColumnWidth must be positive");
        minimumColumnWidth = value;
        return this;
    }

    public UiGrid gap(float value) {
        if (value < 0) throw new IllegalArgumentException("gap must be non-negative");
        gap = value;
        return this;
    }

    public int columns() { return columns; }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float actualGap = gap < 0 ? theme.metrics().spacing() : gap;
        columns = Math.max(1, (int) Math.floor((maxWidth + actualGap) / (minimumColumnWidth + actualGap)));
        float cellWidth = Math.max(0, (maxWidth - actualGap * (columns - 1)) / columns);
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
        float cellWidth = Math.max(0, (value.width() - actualGap * (columns - 1)) / columns);
        float y = value.y();
        int index = 0;
        for (int row = 0; row < rowHeights.size(); row++) {
            float rowHeight = rowHeights.get(row);
            for (int column = 0; column < columns && index < children.size(); column++, index++) {
                children.get(index).layout(renderer, new UiBounds(
                    value.x() + column * (cellWidth + actualGap), y, cellWidth, rowHeight), theme);
            }
            y += rowHeight + actualGap;
        }
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        for (Ui.Node child : children) child.render(renderer, theme);
    }
}
