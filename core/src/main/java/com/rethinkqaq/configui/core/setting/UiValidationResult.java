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

import com.rethinkqaq.configui.core.UiText;

/** Outcome of a setting validation. Errors reject a write; warnings allow it. */
public record UiValidationResult(Severity severity, UiText message) {
    public enum Severity { OK, WARNING, ERROR }
    public static final UiValidationResult OK = new UiValidationResult(Severity.OK, UiText.literal(""));
    public UiValidationResult {
        if (severity == null || message == null) throw new NullPointerException("validation values");
    }
    public static UiValidationResult warning(UiText message) { return new UiValidationResult(Severity.WARNING, message); }
    public static UiValidationResult error(UiText message) { return new UiValidationResult(Severity.ERROR, message); }
    public boolean accepted() { return severity != Severity.ERROR; }
}
