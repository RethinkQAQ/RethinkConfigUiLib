/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.core;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** A live value binding. Setters are intentionally invoked immediately. */
public interface UiBinding<T> {
    T get();
    void set(T value);

    static <T> UiBinding<T> of(Supplier<T> getter, Consumer<T> setter) {
        Objects.requireNonNull(getter, "getter");
        Objects.requireNonNull(setter, "setter");
        return new UiBinding<>() {
            @Override public T get() { return getter.get(); }
            @Override public void set(T value) { setter.accept(value); }
        };
    }
}
