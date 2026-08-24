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

import java.util.Objects;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;

/** A responsive scaffold header with optional surface chrome. */
public final class UiHeader extends Ui.Node {
    private final UiText title;
    private UiText subtitle;
    private UiHeaderStyle style = UiHeaderStyle.CARD;
    private boolean responsive;
    private float customPadding = -1;
    private float titleGap = 6;
    private UiHeaderStyle resolvedStyle = style;

    private UiHeader(UiText title) { this.title = Objects.requireNonNull(title, "title"); }

    public static UiHeader text(UiText title) { return new UiHeader(title).style(UiHeaderStyle.TEXT); }
    public static UiHeader card(UiText title) { return new UiHeader(title).style(UiHeaderStyle.CARD); }
    public static UiHeader compact(UiText title) { return new UiHeader(title).style(UiHeaderStyle.COMPACT); }
    public static Builder builder(UiText title) { return new Builder(title); }

    public UiHeader subtitle(UiText value) {
        subtitle = Objects.requireNonNull(value, "subtitle");
        invalidateLayout();
        return this;
    }

    public UiHeader style(UiHeaderStyle value) {
        style = Objects.requireNonNull(value, "style");
        invalidateLayout();
        return this;
    }

    public UiHeader responsive(boolean value) {
        responsive = value;
        invalidateLayout();
        return this;
    }

    public UiHeader padding(float value) {
        if (value < 0) throw new IllegalArgumentException("header padding must be non-negative");
        customPadding = value;
        invalidateLayout();
        return this;
    }

    public UiHeader titleGap(float value) {
        if (value < 0) throw new IllegalArgumentException("header title gap must be non-negative");
        titleGap = value;
        invalidateLayout();
        return this;
    }

    public UiHeaderStyle style() { return style; }
    public UiHeaderStyle resolvedStyle() { return resolvedStyle; }
    public UiText title() { return title; }
    public UiText subtitle() { return subtitle; }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        resolvedStyle = resolveStyle(maxWidth);
        if (resolvedStyle == UiHeaderStyle.NONE) {
            measuredWidth = 0;
            measuredHeight = 0;
            return;
        }
        float padding = padding(theme, resolvedStyle);
        float textWidth = Math.max(0, maxWidth - padding * 2);
        float height = renderer.lineHeight();
        if (subtitle != null) height += titleGap + renderer.lineHeight();
        measuredWidth = Math.min(maxWidth, Math.max(renderer.textWidth(title),
            subtitle == null ? 0 : renderer.textWidth(subtitle)) + padding * 2);
        measuredHeight = Math.min(maxHeight, height + padding * 2);
        if (textWidth <= 0) measuredWidth = Math.min(maxWidth, padding * 2);
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        if (resolvedStyle == UiHeaderStyle.NONE || bounds.width() <= 0 || bounds.height() <= 0) return;
        float padding = padding(theme, resolvedStyle);
        if (resolvedStyle != UiHeaderStyle.TEXT) {
            float radius = resolvedStyle == UiHeaderStyle.COMPACT
                ? theme.metrics().controlRadius() : theme.metrics().cardRadius();
            renderer.fillRoundRect(bounds, radius, theme.palette().surfaceRaised());
            renderer.strokeRoundRect(bounds, radius, theme.metrics().borderWidth(), theme.palette().border());
        }
        float textWidth = Math.max(0, bounds.width() - padding * 2);
        float x = bounds.x() + padding;
        float y = bounds.y() + padding;
        Ui.drawFittedText(renderer, title, x, y, textWidth, theme.palette().textPrimary());
        if (subtitle != null) {
            Ui.drawFittedText(renderer, subtitle, x, y + renderer.lineHeight() + titleGap,
                textWidth, theme.palette().textSecondary());
        }
    }

    private UiHeaderStyle resolveStyle(float width) {
        if (!responsive) return style;
        if (width < 480) return UiHeaderStyle.TEXT;
        if (width < 760 && style == UiHeaderStyle.CARD) return UiHeaderStyle.COMPACT;
        return style;
    }

    private float padding(UiTheme theme, UiHeaderStyle value) {
        if (customPadding >= 0) return customPadding;
        return switch (value) {
            case TEXT -> theme.metrics().padding() * .25f;
            case COMPACT -> theme.metrics().padding() * .6f;
            case CARD -> theme.metrics().padding();
            case NONE -> 0;
        };
    }

    public static final class Builder {
        private final UiHeader header;

        private Builder(UiText title) { header = new UiHeader(title); }
        public Builder subtitle(UiText value) { header.subtitle(value); return this; }
        public Builder style(UiHeaderStyle value) { header.style(value); return this; }
        public Builder responsive(boolean value) { header.responsive(value); return this; }
        public Builder padding(float value) { header.padding(value); return this; }
        public Builder titleGap(float value) { header.titleGap(value); return this; }
        public UiHeader build() { return header; }
    }
}
