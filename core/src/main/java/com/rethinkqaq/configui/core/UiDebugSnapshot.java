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
        UiBounds bounds,
        boolean visible,
        boolean enabled,
        boolean interactive,
        int depth,
        List<UiDebugSnapshot> children
) {
    public UiDebugSnapshot {
        children = List.copyOf(children);
    }

    public static UiDebugSnapshot of(Ui.Node node) {
        return of(node, 0);
    }

    private static UiDebugSnapshot of(Ui.Node node, int depth) {
        List<UiDebugSnapshot> children = node instanceof Ui.ChildProvider provider
            ? provider.childNodes().stream().filter(child -> child != null).map(child -> of(child, depth + 1)).toList()
            : List.of();
        UiBounds bounds = node.bounds();
        return new UiDebugSnapshot(node.getClass().getSimpleName(), bounds,
            bounds.width() > 0 && bounds.height() > 0, node.enabled(), node.focusable() || node instanceof Ui.SelfDispatching,
            depth, children);
    }
}
