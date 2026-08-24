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

import java.util.Objects;

/** Pure setting validation contract. */
@FunctionalInterface
public interface UiValidator<T> {
    UiValidationResult validate(T value);
    static <T> UiValidator<T> acceptAll() { return value -> UiValidationResult.OK; }
    default UiValidator<T> and(UiValidator<? super T> other) {
        Objects.requireNonNull(other, "other");
        return value -> {
            UiValidationResult first = validate(value);
            if (!first.accepted()) return first;
            UiValidationResult second = other.validate(value);
            return second.severity() == UiValidationResult.Severity.OK ? first : second;
        };
    }
}
