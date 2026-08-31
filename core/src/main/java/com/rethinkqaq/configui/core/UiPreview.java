/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 *
 * This file is part of Rethink Config UI Lib.
 */
package com.rethinkqaq.configui.core;

import java.util.Objects;

/** Platform-neutral bounded preview slot. Platform adapters may bridge their own render API. */
public final class UiPreview extends Ui.Node implements Ui.ClipProvider {
    @FunctionalInterface public interface Renderer {
        void render(UiRenderer renderer, UiBounds bounds, UiBounds clip, UiTheme theme);
    }
    private final Renderer callback;
    private float preferredWidth;
    private float preferredHeight = 104;

    public UiPreview(Renderer callback) { this.callback = Objects.requireNonNull(callback, "renderer"); }
    public UiPreview preferredWidth(float value) { if (value < 0) throw new IllegalArgumentException("width"); preferredWidth = value; invalidateMeasure(); return this; }
    public UiPreview preferredHeight(float value) { if (value <= 0) throw new IllegalArgumentException("height"); preferredHeight = value; invalidateMeasure(); return this; }
    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        measuredWidth = preferredWidth <= 0 ? maxWidth : Math.min(maxWidth, preferredWidth);
        measuredHeight = Math.min(maxHeight, preferredHeight);
    }
    @Override public UiBounds viewportBounds() { return bounds; }
    @Override public void render(UiRenderer renderer, UiTheme theme) {
        if (!visible() || bounds.width() <= 0 || bounds.height() <= 0) return;
        UiBounds clip = bounds;
        renderer.pushClip(bounds);
        try { callback.render(renderer, bounds, clip, theme); }
        finally { renderer.popClip(); }
    }
}
