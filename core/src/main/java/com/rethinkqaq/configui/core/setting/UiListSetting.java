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

package com.rethinkqaq.configui.core.setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

/** Copy-on-write helpers for a configuration collection backed by a {@link UiSetting}. */
public final class UiListSetting<T> {
    private final UiSetting<List<T>> setting;

    public UiListSetting(UiSetting<List<T>> setting) { this.setting = Objects.requireNonNull(setting, "setting"); }
    public static <T> UiListSetting<T> of(UiSetting<List<T>> setting) { return new UiListSetting<>(setting); }

    public List<T> items() { return List.copyOf(setting.get()); }
    public UiSetting<List<T>> setting() { return setting; }
    public UiValidationResult replace(List<T> values) { return setting.set(List.copyOf(values)); }

    public UiValidationResult add(T value) {
        List<T> values = new ArrayList<>(items());
        values.add(value);
        return replace(values);
    }

    public UiValidationResult remove(int index) {
        List<T> values = new ArrayList<>(items());
        if (index < 0 || index >= values.size()) return UiValidationResult.error(com.rethinkqaq.configui.core.UiText.literal("Entry no longer exists"));
        values.remove(index);
        return replace(values);
    }

    public UiValidationResult update(int index, T value) {
        List<T> values = new ArrayList<>(items());
        if (index < 0 || index >= values.size()) return UiValidationResult.error(com.rethinkqaq.configui.core.UiText.literal("Entry no longer exists"));
        values.set(index, value);
        return replace(values);
    }

    public UiValidationResult transform(UnaryOperator<List<T>> operation) {
        return replace(operation.apply(new ArrayList<>(items())));
    }
}
