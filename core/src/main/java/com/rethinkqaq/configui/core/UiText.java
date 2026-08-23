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
