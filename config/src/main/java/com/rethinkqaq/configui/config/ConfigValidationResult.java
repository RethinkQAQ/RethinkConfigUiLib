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

package com.rethinkqaq.configui.config;

import java.util.Objects;

public record ConfigValidationResult(boolean valid, boolean warning, String message) {
    public ConfigValidationResult {
        message = message == null ? "" : message;
        if (!valid && warning) {
            throw new IllegalArgumentException("An invalid result cannot be a warning");
        }
    }

    public static ConfigValidationResult ok() {
        return new ConfigValidationResult(true, false, "");
    }

    public static ConfigValidationResult warning(String message) {
        return new ConfigValidationResult(true, true, Objects.requireNonNull(message));
    }

    public static ConfigValidationResult error(String message) {
        return new ConfigValidationResult(false, false, Objects.requireNonNull(message));
    }
}
