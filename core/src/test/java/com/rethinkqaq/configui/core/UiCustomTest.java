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

    @Test
    void midnightBlueThemeProvidesDarkSurfacesAndBlueAccent() {
        UiTheme theme = UiTheme.midnightBlue();

        assertEquals(UiTheme.UiPalette.midnightBlue(), theme.palette());
        assertEquals(0xFF111827, theme.palette().surface());
        assertEquals(0xFF60A5FA, theme.palette().accent());
        assertEquals(0xFF93C5FD, theme.palette().accentHover());
        assertEquals(0xFF3B82F6, theme.palette().accentPressed());
        assertEquals(0xFF93C5FD, theme.palette().focusRing());
        assertEquals(UiTheme.UiMetrics.comfortable(), theme.metrics());
    }

    @Test
    void customThemeUsesSuppliedVisualTokensAndDefaultMotionAndStates() {
        UiTheme base = UiTheme.roseLight();
        UiTheme.UiPalette palette = UiTheme.UiPalette.builder()
            .accent(0xFF5B8CFF)
            .build();
        UiTheme.UiMetrics metrics = new UiTheme.UiMetrics(8, 6, 10, 32, 1);

        UiTheme custom = UiTheme.custom(palette, metrics);

        assertEquals(palette, custom.palette());
        assertEquals(metrics, custom.metrics());
        assertEquals(base.motion(), custom.motion());
        assertEquals(base.states(), custom.states());
    }

    @Test
    void hoverVisualsGrowFromNormalToThemeScale() {
        UiTheme.UiStateVisuals states = UiTheme.UiStateVisuals.defaults()
            .withHoverRadiusScale(1.3f);

        assertEquals(10f, states.hoverRadius(10f, 0f), .0001f);
        assertEquals(11.5f, states.hoverRadius(10f, .5f), .0001f);
        assertEquals(12.4f, UiTheme.UiStateVisuals.defaults().hoverRadius(10f, 1f), .0001f);
    }

    @Test
    void hoveredButtonGrowsRadiusWithoutScalingText() {
        float[] radius = {0};
        float[] textScale = {0};
        UiRenderer renderer = new UiRenderer() {
            @Override public void fillRect(UiBounds bounds, int color) { }
            @Override public void fillRoundRect(UiBounds bounds, float value, int color) { radius[0] = value; }
            @Override public void strokeRoundRect(UiBounds bounds, float value, float width, int color) { radius[0] = value; }
            @Override public void drawText(UiText text, float x, float y, int color) { }
            @Override public void drawText(UiText text, float x, float y, int color, float scale) { textScale[0] = scale; }
            @Override public float textWidth(UiText text) { return text.value().length() * 6; }
            @Override public float lineHeight() { return 10; }
            @Override public void pushClip(UiBounds bounds) { }
            @Override public void popClip() { }
        };
        UiTheme theme = UiTheme.roseLight();
        Ui.Button button = Ui.button(UiText.literal("Hover"), () -> { });
        button.measure(renderer, 100, 34, theme);
        button.layout(renderer, new UiBounds(0, 0, 100, 34), theme);
        button.setHovered(true);
        button.advanceMotion(System.nanoTime() + 1_000_000_000L, theme);

        button.render(renderer, theme);

        assertEquals(theme.metrics().controlRadius() * theme.states().hoverRadiusScale(), radius[0], .001f);
        assertEquals(UiTextMetrics.buttonScale(theme.metrics()), textScale[0], .001f);
    }
}
