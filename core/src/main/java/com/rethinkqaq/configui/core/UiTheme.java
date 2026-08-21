/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.core;

import java.util.Objects;

/** Immutable semantic styling tokens used by every stock control. */
public record UiTheme(UiPalette palette, UiMetrics metrics, UiMotion motion) {
    public UiTheme {
        Objects.requireNonNull(palette, "palette");
        Objects.requireNonNull(metrics, "metrics");
        Objects.requireNonNull(motion, "motion");
    }
    public UiTheme withPalette(UiPalette value) { return new UiTheme(value, metrics, motion); }
    public UiTheme withAccent(int value) { return withPalette(palette.withAccent(value)); }
    public static UiTheme roseLight() {
        return new UiTheme(new UiPalette(0xFFF6F7F9, 0xFFFFFFFF, 0xFF353335, 0xFF4A4749, 0xFFF39ABA,
            0xFFFFB2CB, 0xFFE184A6, 0xFFFFFFFF, 0xFF29272A, 0xFF77747A, 0xFFD8D9DE, 0xFF6C8DFF),
            // The generous metrics are intentional: the reference UI uses a spacious, card-based
            // composition rather than the compact 20px-high vanilla controls.
            new UiMetrics(14, 14, 16, 42, 1), new UiMotion(180, 110));
    }

    public record UiPalette(int surface, int surfaceRaised, int control, int controlHover, int accent,
                            int accentHover, int accentPressed, int onAccent, int textPrimary,
                            int textSecondary, int border, int focusRing) {
        public UiPalette withAccent(int value) { return new UiPalette(surface, surfaceRaised, control, controlHover, value, value, value, onAccent, textPrimary, textSecondary, border, focusRing); }
    }
    public record UiMetrics(float radius, float spacing, float padding, float controlHeight, float borderWidth) { }
    public record UiMotion(int hoverMillis, int pressMillis) { }
}
