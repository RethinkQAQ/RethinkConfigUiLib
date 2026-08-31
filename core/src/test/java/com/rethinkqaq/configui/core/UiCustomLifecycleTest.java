/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 *
 * This file is part of Rethink Config UI Lib.
 */
package com.rethinkqaq.configui.core;

import static org.junit.jupiter.api.Assertions.*;

import com.rethinkqaq.configui.core.component.UiComponent;
import com.rethinkqaq.configui.core.component.UiCustom;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class UiCustomLifecycleTest {
    private static final class TestComponent extends UiComponent {
        private final AtomicInteger mounted;
        TestComponent(AtomicInteger mounted) { this.mounted = mounted; }
        void addChild(Ui.Node value) { child(value); }
        @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
            measuredWidth = maxWidth; measuredHeight = maxHeight;
        }
        @Override public void layout(UiRenderer renderer, UiBounds bounds, UiTheme theme) {
            super.layout(renderer, bounds, theme);
        }
        @Override public void render(UiRenderer renderer, UiTheme theme) { }
        @Override protected void onMount() { mounted.incrementAndGet(); }
    }

    @Test
    void customNodeHonoursVisibilityAndInvalidatesPreferredSize() {
        UiCustom node = Ui.custom().preferredWidth(20).preferredHeight(10).build();
        long revision = node.layoutVersion();
        assertEquals(revision, node.layoutVersion());
        node.visible(false);
        assertFalse(node.visible());
        assertTrue(node.layoutVersion() > revision);
    }

    @Test
    void lifecycleIsIdempotentAndContainerMountsChildren() {
        AtomicInteger mounted = new AtomicInteger();
        AtomicInteger disposed = new AtomicInteger();
        UiCustom child = Ui.custom().build();
        child.onDispose(disposed::incrementAndGet);
        TestComponent component = new TestComponent(mounted);
        component.addChild(child);
        component.mount();
        component.mount();
        assertTrue(child.mounted());
        assertEquals(1, mounted.get());
        component.dispose();
        component.dispose();
        assertEquals(1, disposed.get());
    }
}
