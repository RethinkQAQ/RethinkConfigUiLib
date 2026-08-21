/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class UiBindingTest {
    @Test
    void bindingWritesImmediately() {
        AtomicBoolean value = new AtomicBoolean();
        UiBinding<Boolean> binding = UiBinding.of(value::get, value::set);
        binding.set(true);
        assertEquals(true, value.get());
    }

    @Test
    void roseThemeCanOverrideAccent() {
        assertEquals(0xFF112233, UiTheme.roseLight().withAccent(0xFF112233).palette().accent());
    }

    @Test
    void columnMeasuresAndLaysOutChildren() {
        Ui.Column column = Ui.column().gap(2);
        column.add(Ui.label(UiText.literal("A"))).add(Ui.label(UiText.literal("B")));
        column.measure(RENDERER, 100, 100, UiTheme.roseLight());
        column.layout(RENDERER, new UiBounds(0, 0, 100, column.measuredHeight()), UiTheme.roseLight());
        assertEquals(22, column.measuredHeight());
        assertEquals(0, column.children().get(0).bounds().y());
        assertEquals(12, column.children().get(1).bounds().y());
    }

    @Test
    void scrollViewKeepsFullContentHeightAndMovesItsChild() {
        Ui.Column content = Ui.column().gap(0);
        for (int index = 0; index < 5; index++) content.add(Ui.label(UiText.literal("Line" + index)));
        Ui.ScrollView view = Ui.scrollView(content);
        view.measure(RENDERER, 100, 20, UiTheme.roseLight());
        view.layout(RENDERER, new UiBounds(0, 0, 100, 20), UiTheme.roseLight());
        assertEquals(20, view.measuredHeight());
        assertTrue(view.scroll(10, 10, -1));
        view.layout(RENDERER, new UiBounds(0, 0, 100, 20), UiTheme.roseLight());
        assertEquals(-14, content.bounds().y());
    }

    @Test
    void disabledButtonCannotActivate() {
        AtomicInteger calls = new AtomicInteger();
        Ui.Button button = Ui.button(UiText.literal("Apply"), calls::incrementAndGet);
        button.enabled(false);
        button.measure(RENDERER, 100, 100, UiTheme.roseLight());
        button.layout(RENDERER, new UiBounds(0, 0, 100, 20), UiTheme.roseLight());
        assertFalse(button.click(10, 10, 0));
        assertEquals(0, calls.get());
    }

    @Test
    void toggleWritesThroughBindingImmediately() {
        AtomicBoolean value = new AtomicBoolean();
        Ui.Toggle toggle = Ui.toggle(UiText.literal("Enabled"), UiBinding.of(value::get, value::set));
        toggle.measure(RENDERER, 100, 100, UiTheme.roseLight());
        toggle.layout(RENDERER, new UiBounds(0, 0, 100, 20), UiTheme.roseLight());
        assertTrue(toggle.click(10, 10, 0));
        assertTrue(value.get());
    }

    private static final UiRenderer RENDERER = new UiRenderer() {
        @Override public void fillRoundRect(UiBounds bounds, float radius, int color) { }
        @Override public void strokeRoundRect(UiBounds bounds, float radius, float width, int color) { }
        @Override public void drawText(UiText text, float x, float y, int color) { }
        @Override public float textWidth(UiText text) { return text.value().length() * 6f; }
        @Override public float lineHeight() { return 10; }
        @Override public void pushClip(UiBounds bounds) { }
        @Override public void popClip() { }
    };
}
