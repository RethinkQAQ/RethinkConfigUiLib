/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 *
 * This file is part of Rethink Config UI Lib.
 */

package com.rethinkqaq.configui.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rethinkqaq.configui.core.component.UiCustom;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

class UiCustomTest {
    private static final UiRenderer RENDERER = new UiRenderer() {
        @Override public void fillRect(UiBounds bounds, int color) { }
        @Override public void fillRoundRect(UiBounds bounds, float radius, int color) { }
        @Override public void strokeRoundRect(UiBounds bounds, float radius, float width, int color) { }
        @Override public void drawText(UiText text, float x, float y, int color) { }
        @Override public float textWidth(UiText text) { return text.value().length() * 6; }
        @Override public float lineHeight() { return 10; }
        @Override public void pushClip(UiBounds bounds) { }
        @Override public void popClip() { }
    };

    @Test
    void customNodeUsesPreferredSizeAndClickHandler() {
        AtomicInteger clicks = new AtomicInteger();
        UiCustom node = Ui.custom().preferredWidth(80).preferredHeight(24)
            .click((x, y, button) -> { clicks.incrementAndGet(); return true; }).build();
        node.measure(RENDERER, 100, 100, UiTheme.roseLight());
        node.layout(RENDERER, new UiBounds(10, 20, 80, 24), UiTheme.roseLight());

        assertEquals(80, node.measuredWidth());
        assertTrue(node.click(20, 30, 0));
        assertEquals(1, clicks.get());
    }

    @Test
    void darkThemeKeepsRoseAccentAndChangesSurface() {
        assertEquals(UiTheme.roseLight().palette().accent(), UiTheme.roseDark().palette().accent());
        assertTrue(UiTheme.roseDark().palette().surface() != UiTheme.roseLight().palette().surface());
    }
}
