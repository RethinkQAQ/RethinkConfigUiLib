/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 *
 * This file is part of Rethink Config UI Lib.
 */

package com.rethinkqaq.configui.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UiDebugSnapshotTest {
    @Test
    void snapshotIncludesNestedBoundsAndVisibility() {
        Ui.Column root = Ui.column().add(Ui.button(UiText.literal("Done"), () -> { }));
        UiRenderer renderer = new UiRenderer() {
            @Override public void fillRect(UiBounds bounds, int color) { }
            @Override public void fillRoundRect(UiBounds bounds, float radius, int color) { }
            @Override public void strokeRoundRect(UiBounds bounds, float radius, float width, int color) { }
            @Override public void drawText(UiText text, float x, float y, int color) { }
            @Override public float textWidth(UiText text) { return text.value().length() * 6; }
            @Override public float lineHeight() { return 10; }
            @Override public void pushClip(UiBounds bounds) { }
            @Override public void popClip() { }
        };
        root.measure(renderer, 100, 50, UiTheme.roseLight());
        root.layout(renderer, new UiBounds(5, 6, 100, 50), UiTheme.roseLight());

        UiDebugSnapshot snapshot = UiDebugSnapshot.of(root);
        assertEquals("Column", snapshot.type());
        assertTrue(snapshot.visible());
        assertEquals(new UiBounds(5, 6, 100, 50), snapshot.layoutBounds());
        assertEquals(snapshot.layoutBounds(), snapshot.contentBounds());
        assertEquals(snapshot.layoutBounds(), snapshot.clipBounds());
        assertEquals(snapshot.layoutBounds(), snapshot.eventBounds());
        assertEquals(1, snapshot.children().size());
        assertEquals(1, snapshot.children().get(0).depth());
        assertTrue(snapshot.children().get(0).interactive());
    }
}
