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

import java.util.Objects;

/** Small status pill for selected, success, warning and error states. */
public final class UiBadge extends Ui.Node {
    public enum Tone { NEUTRAL, ACCENT, SUCCESS, WARNING, DANGER }

    private final UiText text;
    private Tone tone = Tone.NEUTRAL;

    UiBadge(UiText value) { text = Objects.requireNonNull(value, "text"); }
    public UiBadge tone(Tone value) { tone = Objects.requireNonNull(value, "tone"); return this; }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float padding = theme.metrics().padding() * .55f;
        float scale = UiTextMetrics.buttonScale(theme.metrics());
        measuredWidth = Math.min(maxWidth, renderer.textWidth(text, scale) + padding * 2);
        measuredHeight = Math.min(maxHeight, UiTextMetrics.lineHeight(renderer, scale) + padding);
    }
    @Override public void render(UiRenderer renderer, UiTheme theme) {
        int color = switch (tone) {
            case NEUTRAL -> theme.palette().border();
            case ACCENT -> theme.palette().accent();
            case SUCCESS -> theme.palette().success();
            case WARNING -> theme.palette().warning();
            case DANGER -> theme.palette().danger();
        };
        renderer.fillRoundRect(bounds, bounds.height() / 2f, color);
        float scale = UiTextMetrics.buttonScale(theme.metrics());
        float x = bounds.x() + (bounds.width() - renderer.textWidth(text, scale)) / 2f;
        UiTextMetrics.draw(renderer, text, x,
            bounds.y() + (bounds.height() - UiTextMetrics.lineHeight(renderer, scale)) / 2f,
            bounds.width(), theme.palette().onAccent(), scale);
    }
}
