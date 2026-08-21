/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.core;

public record UiBounds(float x, float y, float width, float height) {
    public static final UiBounds EMPTY = new UiBounds(0, 0, 0, 0);
    public boolean contains(float mouseX, float mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
    public UiBounds inset(float amount) {
        return new UiBounds(x + amount, y + amount, Math.max(0, width - amount * 2), Math.max(0, height - amount * 2));
    }
    public UiBounds offset(float dx, float dy) {
        return new UiBounds(x + dx, y + dy, width, height);
    }
}
