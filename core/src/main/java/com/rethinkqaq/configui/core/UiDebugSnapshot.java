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

package com.rethinkqaq.configui.core;

import java.util.List;

/** Immutable diagnostic information for inspecting a laid-out UI tree. */
public record UiDebugSnapshot(
        String type,
        UiBounds layoutBounds,
        UiBounds contentBounds,
        UiBounds clipBounds,
        UiBounds eventBounds,
        boolean visible,
        boolean enabled,
        boolean interactive,
        boolean focused,
        int depth,
        List<UiDebugSnapshot> children
) {
    public UiDebugSnapshot {
        children = List.copyOf(children);
    }

    public static UiDebugSnapshot of(Ui.Node node) {
        return of(node, 0, node.bounds());
    }

    private static UiDebugSnapshot of(Ui.Node node, int depth, UiBounds inheritedClip) {
        UiBounds layout = node.bounds();
        UiBounds content = layout;
        UiBounds clip = node instanceof Ui.ClipProvider provider
            ? inheritedClip.intersection(provider.viewportBounds()) : inheritedClip;
        UiBounds event = layout.intersection(inheritedClip);
        List<UiDebugSnapshot> children = node instanceof Ui.ChildProvider provider
            ? provider.childNodes().stream().filter(child -> child != null).map(child -> of(child, depth + 1, clip)).toList()
            : List.of();
        return new UiDebugSnapshot(node.getClass().getSimpleName(), layout, content, clip, event,
            event.width() > 0 && event.height() > 0, node.enabled(), node.focusable() || node instanceof Ui.SelfDispatching,
            node.focused(), depth, children);
    }
}
