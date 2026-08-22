/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.core;

/** GLFW-compatible key values kept here so core has no GLFW dependency. */
public final class UiKey {
    public static final int TAB = 258, ENTER = 257, SPACE = 32, LEFT = 263, RIGHT = 262, UP = 265, DOWN = 264,
        PAGE_UP = 266, PAGE_DOWN = 267, HOME = 268, END = 269;
    private UiKey() { }
}
