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
