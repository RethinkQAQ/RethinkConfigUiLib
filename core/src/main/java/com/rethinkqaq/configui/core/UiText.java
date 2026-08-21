/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.core;

import java.util.List;
import java.util.Objects;

/** Text independent from the Minecraft text implementation. */
public record UiText(String value, boolean translatable, List<Object> arguments) {
    public UiText {
        value = Objects.requireNonNull(value, "value");
        arguments = List.copyOf(arguments);
    }

    public static UiText literal(String value) { return new UiText(value, false, List.of()); }
    public static UiText translatable(String key, Object... arguments) { return new UiText(key, true, List.of(arguments)); }
}
