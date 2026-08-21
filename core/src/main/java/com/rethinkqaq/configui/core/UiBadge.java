/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
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
        measuredWidth = Math.min(maxWidth, renderer.textWidth(text) + padding * 2);
        measuredHeight = Math.min(maxHeight, renderer.lineHeight() + padding);
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
        float x = bounds.x() + (bounds.width() - renderer.textWidth(text)) / 2f;
        renderer.drawText(text, x, bounds.y() + (bounds.height() - renderer.lineHeight()) / 2f, theme.palette().onAccent());
    }
}
