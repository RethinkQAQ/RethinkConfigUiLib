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

package com.rethinkqaq.configui.core.component.feedback;

/** Fixed semantic feedback colours. Only {@link #CUSTOM} accepts a caller-provided colour. */
public enum UiFeedbackType {
    INFO(0xFF3B82F6),
    SUCCESS(0xFF22A060),
    WARNING(0xFFF59E0B),
    ERROR(0xFFDC3C3C),
    CUSTOM(0);

    private final int fixedColor;

    UiFeedbackType(int fixedColor) { this.fixedColor = fixedColor; }

    public int color(int customColor) { return this == CUSTOM ? customColor : fixedColor; }
}
