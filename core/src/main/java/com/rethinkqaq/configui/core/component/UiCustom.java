/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 *
 * This file is part of Rethink Config UI Lib.
 *
 * Rethink Config UI Lib is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, version 3 of the License.
 */

package com.rethinkqaq.configui.core.component;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiTheme;

import java.util.Objects;
import java.util.function.BiFunction;

/** A small platform-neutral extension point for one-off custom UI content. */
public final class UiCustom extends Ui.Node {
    @FunctionalInterface public interface Renderer { void render(UiRenderer renderer, UiBounds bounds, UiTheme theme); }
    @FunctionalInterface public interface Measurer { UiBounds measure(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme); }
    @FunctionalInterface public interface ClickHandler { boolean click(float x, float y, int button); }

    private final Measurer measurer;
    private final Renderer renderer;
    private final ClickHandler clickHandler;

    private UiCustom(Measurer measurer, Renderer renderer, ClickHandler clickHandler) {
        this.measurer = Objects.requireNonNull(measurer, "measurer");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.clickHandler = clickHandler;
    }

    public static Builder builder() { return new Builder(); }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        UiBounds size = measurer.measure(renderer, maxWidth, maxHeight, theme);
        measuredWidth = Math.max(0, Math.min(maxWidth, size.width()));
        measuredHeight = Math.max(0, Math.min(maxHeight, size.height()));
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) {
        if (visible()) this.renderer.render(renderer, bounds, theme);
    }

    @Override public boolean click(float x, float y, int button) {
        return visible() && enabled() && bounds.contains(x, y) && clickHandler != null && clickHandler.click(x, y, button);
    }

    public static final class Builder {
        private float width;
        private float height;
        private float minWidth;
        private float minHeight;
        private float maxWidth = Float.MAX_VALUE;
        private float maxHeight = Float.MAX_VALUE;
        private Measurer measurer;
        private Renderer renderer = (target, bounds, theme) -> { };
        private ClickHandler clickHandler;

        public Builder preferredWidth(float value) { width = requireNonNegative(value, "width"); return this; }
        public Builder preferredHeight(float value) { height = requireNonNegative(value, "height"); return this; }
        public Builder minWidth(float value) { minWidth = requireNonNegative(value, "minWidth"); return this; }
        public Builder minHeight(float value) { minHeight = requireNonNegative(value, "minHeight"); return this; }
        public Builder maxWidth(float value) { maxWidth = requireNonNegative(value, "maxWidth"); return this; }
        public Builder maxHeight(float value) { maxHeight = requireNonNegative(value, "maxHeight"); return this; }
        public Builder measure(Measurer value) { measurer = Objects.requireNonNull(value, "measurer"); return this; }
        public Builder render(Renderer value) { renderer = Objects.requireNonNull(value, "renderer"); return this; }
        public Builder click(ClickHandler value) { clickHandler = value; return this; }
        public UiCustom build() {
            Measurer resolved = measurer == null ? (target, maxWidth, maxHeight, theme) ->
                new UiBounds(0, 0, Math.min(Math.min(maxWidth, this.maxWidth), Math.max(minWidth, width)),
                    Math.min(Math.min(maxHeight, this.maxHeight), Math.max(minHeight, height))) : measurer;
            return new UiCustom(resolved, renderer, clickHandler);
        }

        private static float requireNonNegative(float value, String name) {
            if (value < 0) throw new IllegalArgumentException(name + " must be non-negative");
            return value;
        }
    }
}
