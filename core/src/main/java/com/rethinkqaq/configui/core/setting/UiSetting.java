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

import com.rethinkqaq.configui.core.UiBinding;
import com.rethinkqaq.configui.core.UiText;
import java.util.Objects;

/** Optional configuration metadata around a live binding. Persistence stays with the host mod. */
public final class UiSetting<T> {
    private final UiBinding<T> binding;
    private final T defaultValue;
    private final UiText description;
    private final UiValidator<? super T> validator;

    public UiSetting(UiBinding<T> binding, T defaultValue, UiText description, UiValidator<? super T> validator) {
        this.binding = Objects.requireNonNull(binding, "binding");
        this.defaultValue = defaultValue;
        this.description = description == null ? UiText.literal("") : description;
        this.validator = validator == null ? UiValidator.acceptAll() : validator;
    }
    public static <T> UiSetting<T> of(UiBinding<T> binding, T defaultValue) {
        return new UiSetting<>(binding, defaultValue, UiText.literal(""), UiValidator.acceptAll());
    }
    public UiSetting<T> describedBy(UiText value) { return new UiSetting<>(binding, defaultValue, value, validator); }
    public UiSetting<T> validatedBy(UiValidator<? super T> value) {
        Objects.requireNonNull(value, "value");
        return new UiSetting<>(binding, defaultValue, description, candidate -> {
            UiValidationResult first = validator.validate(candidate);
            if (!first.accepted()) return first;
            UiValidationResult second = value.validate(candidate);
            return second.severity() == UiValidationResult.Severity.OK ? first : second;
        });
    }
    public T get() { return binding.get(); }
    public UiValidationResult validate(T value) { return validator.validate(value); }
    public UiValidationResult set(T value) {
        UiValidationResult result = validate(value);
        if (result.accepted()) binding.set(value);
        return result;
    }
    public void reset() { binding.set(defaultValue); }
    public boolean isDefault() { return Objects.equals(get(), defaultValue); }
    public T defaultValue() { return defaultValue; }
    public UiText description() { return description; }
    public UiBinding<T> binding() { return binding; }
}
