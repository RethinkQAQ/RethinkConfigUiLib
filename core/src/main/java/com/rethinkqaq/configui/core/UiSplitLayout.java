/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.core;

import java.util.List;
import java.util.Objects;

/** Two panes side by side on wide screens and vertically stacked on compact screens. */
public final class UiSplitLayout extends Ui.Node implements Ui.ChildProvider {
    private final Ui.Node primary;
    private final Ui.Node secondary;
    private float primaryShare = .56f;
    private float compactBelow = 620;
    private float gap = -1;
    private boolean compact;

    UiSplitLayout(Ui.Node primary, Ui.Node secondary) {
        this.primary = Objects.requireNonNull(primary, "primary");
        this.secondary = Objects.requireNonNull(secondary, "secondary");
    }

    public UiSplitLayout primaryShare(float value) {
        if (value <= 0 || value >= 1) throw new IllegalArgumentException("primary share must be between zero and one");
        primaryShare = value;
        return this;
    }
    public UiSplitLayout compactBelow(float value) {
        if (value <= 0) throw new IllegalArgumentException("compact width must be positive");
        compactBelow = value;
        return this;
    }
    public UiSplitLayout gap(float value) {
        if (value < 0) throw new IllegalArgumentException("gap must be non-negative");
        gap = value;
        return this;
    }
    @Override public List<Ui.Node> childNodes() { return List.of(primary, secondary); }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        compact = maxWidth < compactBelow;
        float actualGap = gap < 0 ? theme.metrics().spacing() : gap;
        if (compact) {
            primary.measure(renderer, maxWidth, maxHeight, theme);
            secondary.measure(renderer, maxWidth, maxHeight, theme);
            measuredWidth = maxWidth;
            measuredHeight = Math.min(maxHeight, primary.measuredHeight() + actualGap + secondary.measuredHeight());
        } else {
            float primaryWidth = Math.max(0, (maxWidth - actualGap) * primaryShare);
            float secondaryWidth = Math.max(0, maxWidth - actualGap - primaryWidth);
            primary.measure(renderer, primaryWidth, maxHeight, theme);
            secondary.measure(renderer, secondaryWidth, maxHeight, theme);
            measuredWidth = maxWidth;
            measuredHeight = Math.min(maxHeight, Math.max(primary.measuredHeight(), secondary.measuredHeight()));
        }
    }

    @Override
    public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        float actualGap = gap < 0 ? theme.metrics().spacing() : gap;
        if (compact) {
            primary.layout(renderer, new UiBounds(value.x(), value.y(), value.width(), primary.measuredHeight()), theme);
            secondary.layout(renderer, new UiBounds(value.x(), value.y() + primary.measuredHeight() + actualGap,
                value.width(), secondary.measuredHeight()), theme);
        } else {
            float primaryWidth = Math.max(0, (value.width() - actualGap) * primaryShare);
            primary.layout(renderer, new UiBounds(value.x(), value.y(), primaryWidth, value.height()), theme);
            secondary.layout(renderer, new UiBounds(value.x() + primaryWidth + actualGap, value.y(),
                Math.max(0, value.width() - actualGap - primaryWidth), value.height()), theme);
        }
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) { primary.render(renderer, theme); secondary.render(renderer, theme); }
    @Override public boolean click(float x, float y, int button) { return secondary.click(x, y, button) || primary.click(x, y, button); }
    @Override public boolean scroll(float x, float y, double amount) { return secondary.scroll(x, y, amount) || primary.scroll(x, y, amount); }
}
