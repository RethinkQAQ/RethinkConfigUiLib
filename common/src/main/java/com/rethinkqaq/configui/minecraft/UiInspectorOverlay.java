/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 *
 * This file is part of Rethink Config UI Lib.
 *
 * Rethink Config UI Lib is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, version 3 of the License.
 */
package com.rethinkqaq.configui.minecraft;

import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiDebugSnapshot;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;

/** Development-only visualisation of the layout, clip and event contracts of a UI tree. */
final class UiInspectorOverlay {
    private static final int LAYOUT = 0xFF4DA3FF;
    private static final int CLIP = 0xFFFFB347;
    private static final int EVENT = 0xFF65D6A7;
    private static final int FOCUSED = 0xFFFF6FAE;

    private UiInspectorOverlay() { }

    static void render(UiRenderer renderer, UiDebugSnapshot snapshot, UiTheme theme) {
        renderer.pushOverlay();
        try {
            renderNode(renderer, snapshot, theme);
            renderer.drawText(UiText.literal("Inspector  blue: layout  orange: clip  green: event  pink: focused"),
                4, 4, theme.palette().textSecondary());
        } finally {
            renderer.popOverlay();
        }
    }

    private static void renderNode(UiRenderer renderer, UiDebugSnapshot node, UiTheme theme) {
        stroke(renderer, node.layoutBounds(), LAYOUT);
        if (!node.clipBounds().equals(node.layoutBounds())) stroke(renderer, node.clipBounds(), CLIP);
        if (!node.eventBounds().equals(node.layoutBounds())) stroke(renderer, node.eventBounds(), EVENT);
        if (node.focused()) stroke(renderer, node.layoutBounds().inset(1), FOCUSED);
        if (node.visible() && node.layoutBounds().width() >= 32 && node.layoutBounds().height() >= renderer.lineHeight()) {
            String state = node.enabled() ? (node.interactive() ? " interactive" : "") : " disabled";
            renderer.drawText(UiText.literal(node.type() + state), node.layoutBounds().x() + 2,
                node.layoutBounds().y() + 2, theme.palette().textSecondary());
        }
        for (UiDebugSnapshot child : node.children()) renderNode(renderer, child, theme);
    }

    private static void stroke(UiRenderer renderer, UiBounds bounds, int color) {
        if (bounds.width() > 0 && bounds.height() > 0) renderer.strokeRoundRect(bounds, 0, 1, color);
    }
}
