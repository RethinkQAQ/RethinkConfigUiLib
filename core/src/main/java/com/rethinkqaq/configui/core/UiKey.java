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

/** GLFW-compatible key values kept here so core has no GLFW dependency. */
public final class UiKey {
    public static final int TAB = 258, ENTER = 257, SPACE = 32, LEFT = 263, RIGHT = 262, UP = 265, DOWN = 264,
        PAGE_UP = 266, PAGE_DOWN = 267, HOME = 268, END = 269;
    private UiKey() { }
}
