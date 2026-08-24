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

/**
 * Immutable semantic styling tokens used by stock controls.
 *
 * <p>A theme describes intent instead of component-specific colours. A host can replace the
 * rose palette without changing a control implementation.</p>
 */
public record UiTheme(UiPalette palette, UiMetrics metrics, UiMotion motion) {
    public UiTheme {
        Objects.requireNonNull(palette, "palette");
        Objects.requireNonNull(metrics, "metrics");
        Objects.requireNonNull(motion, "motion");
    }

    public UiTheme withPalette(UiPalette value) { return new UiTheme(value, metrics, motion); }
    public UiTheme withMetrics(UiMetrics value) { return new UiTheme(palette, value, motion); }
    public UiTheme withMotion(UiMotion value) { return new UiTheme(palette, metrics, value); }
    public UiTheme withAccent(int value) { return withPalette(palette.withAccent(value)); }

    public static Builder builder() { return new Builder(); }

    /** Default light theme inspired by the project visual direction. */
    public static UiTheme roseLight() { return builder().build(); }

    public static final class Builder {
        private UiPalette palette = UiPalette.roseLight();
        private UiMetrics metrics = UiMetrics.comfortable();
        private UiMotion motion = UiMotion.defaults();

        public Builder palette(UiPalette value) { palette = Objects.requireNonNull(value, "palette"); return this; }
        public Builder metrics(UiMetrics value) { metrics = Objects.requireNonNull(value, "metrics"); return this; }
        public Builder motion(UiMotion value) { motion = Objects.requireNonNull(value, "motion"); return this; }
        public UiTheme build() { return new UiTheme(palette, metrics, motion); }
    }

    /** Semantic colour tokens. Values use the Minecraft ARGB integer format. */
    public record UiPalette(
        int surface,
        int surfaceRaised,
        int control,
        int controlHover,
        int controlPressed,
        int controlDisabled,
        int accent,
        int accentHover,
        int accentPressed,
        int onAccent,
        int textPrimary,
        int textSecondary,
        int textDisabled,
        int border,
        int focusRing,
        int success,
        int warning,
        int danger
    ) {
        /** Compatibility constructor for the initial, smaller palette. */
        public UiPalette(int surface, int surfaceRaised, int control, int controlHover, int accent,
                         int accentHover, int accentPressed, int onAccent, int textPrimary,
                         int textSecondary, int border, int focusRing) {
            this(surface, surfaceRaised, control, controlHover, 0xFF242224, 0xFFBEBBC0,
                accent, accentHover, accentPressed, onAccent, textPrimary, textSecondary,
                0xFFAAA6AC, border, focusRing, 0xFF4FA96B, 0xFFF0A53B, 0xFFE35D6A);
        }

        public static UiPalette roseLight() {
            return new UiPalette(
                0xFFF6F7F9, 0xFFFFFFFF,
                0xFF353335, 0xFF4A4749, 0xFF242224, 0xFFBAB7BC,
                0xFFF39ABA, 0xFFFFB2CB, 0xFFE184A6, 0xFFFFFFFF,
                0xFF29272A, 0xFF77747A, 0xFFAAA6AC,
                0xFFE1E2E6, 0x00000000,
                0xFF4FA96B, 0xFFF0A53B, 0xFFE35D6A
            );
        }

        public static Builder builder() { return new Builder(roseLight()); }
        public int background() { return surface; }
        public int card() { return surfaceRaised; }
        public UiPalette withAccent(int value) {
            return new UiPalette(surface, surfaceRaised, control, controlHover, controlPressed, controlDisabled,
                value, value, value, onAccent, textPrimary, textSecondary, textDisabled, border, focusRing,
                success, warning, danger);
        }

        public static final class Builder {
            private int surface, surfaceRaised, control, controlHover, controlPressed, controlDisabled;
            private int accent, accentHover, accentPressed, onAccent;
            private int textPrimary, textSecondary, textDisabled, border, focusRing, success, warning, danger;

            private Builder(UiPalette source) {
                surface = source.surface; surfaceRaised = source.surfaceRaised;
                control = source.control; controlHover = source.controlHover;
                controlPressed = source.controlPressed; controlDisabled = source.controlDisabled;
                accent = source.accent; accentHover = source.accentHover; accentPressed = source.accentPressed;
                onAccent = source.onAccent; textPrimary = source.textPrimary; textSecondary = source.textSecondary;
                textDisabled = source.textDisabled; border = source.border; focusRing = source.focusRing;
                success = source.success; warning = source.warning; danger = source.danger;
            }

            public Builder background(int value) { surface = value; return this; }
            public Builder surfaceRaised(int value) { surfaceRaised = value; return this; }
            public Builder control(int value) { control = value; return this; }
            public Builder controlHover(int value) { controlHover = value; return this; }
            public Builder controlPressed(int value) { controlPressed = value; return this; }
            public Builder controlDisabled(int value) { controlDisabled = value; return this; }
            public Builder accent(int value) { accent = value; return this; }
            public Builder accentHover(int value) { accentHover = value; return this; }
            public Builder accentPressed(int value) { accentPressed = value; return this; }
            public Builder onAccent(int value) { onAccent = value; return this; }
            public Builder textPrimary(int value) { textPrimary = value; return this; }
            public Builder textSecondary(int value) { textSecondary = value; return this; }
            public Builder textDisabled(int value) { textDisabled = value; return this; }
            public Builder border(int value) { border = value; return this; }
            public Builder focusRing(int value) { focusRing = value; return this; }
            public Builder success(int value) { success = value; return this; }
            public Builder warning(int value) { warning = value; return this; }
            public Builder danger(int value) { danger = value; return this; }
            public UiPalette build() {
                return new UiPalette(surface, surfaceRaised, control, controlHover, controlPressed, controlDisabled,
                    accent, accentHover, accentPressed, onAccent, textPrimary, textSecondary, textDisabled,
                    border, focusRing, success, warning, danger);
            }
        }
    }

    /** Shared dimensions in logical Minecraft GUI units. */
    public record UiMetrics(
        float radius, float spacing, float padding, float controlHeight, float borderWidth,
        float cardRadius, float controlRadius, float shadowOffset
    ) {
        public UiMetrics {
            if (radius < 0 || spacing < 0 || padding < 0 || controlHeight <= 0 || borderWidth <= 0
                || cardRadius < 0 || controlRadius < 0 || shadowOffset < 0) {
                throw new IllegalArgumentException("UI metrics must be non-negative and control height positive");
            }
        }
        /** Compatibility constructor for the initial metrics model. */
        public UiMetrics(float radius, float spacing, float padding, float controlHeight, float borderWidth) {
            this(radius, spacing, padding, controlHeight, borderWidth, radius + 2, radius, 3);
        }
        /**
         * Flat by default: separation comes from the surface and a subtle border, not a
         * hard offset shadow. Hosts which need elevation can still opt in per theme.
         */
        /**
         * Balanced default dimensions: compact enough for high GUI scales while retaining
         * comfortable hit targets and breathing room around labels.
         */
        public static UiMetrics comfortable() { return new UiMetrics(10, 9, 12, 34, .5f, 13, 10, 0); }
    }

    /** Motion durations. Renderers may ignore motion when animation is disabled by a host. */
    public record UiMotion(int hoverMillis, int pressMillis, int toggleMillis, int focusMillis) {
        public UiMotion {
            if (hoverMillis < 0 || pressMillis < 0 || toggleMillis < 0 || focusMillis < 0) {
                throw new IllegalArgumentException("Motion durations must be non-negative");
            }
        }
        public UiMotion(int hoverMillis, int pressMillis) { this(hoverMillis, pressMillis, hoverMillis, 100); }
        public static UiMotion defaults() { return new UiMotion(180, 110, 140, 100); }
    }
}
