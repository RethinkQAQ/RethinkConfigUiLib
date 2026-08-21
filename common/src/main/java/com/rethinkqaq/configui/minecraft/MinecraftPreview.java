/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.minecraft;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiTheme;
import java.util.Objects;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}

/**
 * Minecraft-only preview slot for use inside a core {@code UiPreviewCard}.
 *
 * <p>The callback is deliberately outside core: a host can draw an item, model or texture using
 * its own Minecraft renderer without making the reusable component tree depend on those types.</p>
 */
public final class MinecraftPreview extends Ui.Node {
    //? if >=26.1 {
    /*@FunctionalInterface
    public interface Renderer {
        void render(GuiGraphicsExtractor graphics, UiBounds bounds);
    }
    *///?} else {
    @FunctionalInterface
    public interface Renderer {
        void render(GuiGraphics graphics, UiBounds bounds);
    }
    //?}

    private final Renderer renderer;
    private float preferredHeight = 104;

    public MinecraftPreview(Renderer renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer");
    }

    public MinecraftPreview preferredHeight(float value) {
        if (value <= 0) throw new IllegalArgumentException("preferred height must be positive");
        preferredHeight = value;
        return this;
    }

    @Override protected void measureSelf(UiRenderer ignored, float maxWidth, float maxHeight, UiTheme theme) {
        measuredWidth = maxWidth;
        measuredHeight = Math.min(maxHeight, preferredHeight);
    }

    @Override public void render(UiRenderer target, UiTheme theme) {
        if (target instanceof MinecraftUiRenderer minecraftRenderer) minecraftRenderer.renderPreview(renderer, bounds);
    }
}
