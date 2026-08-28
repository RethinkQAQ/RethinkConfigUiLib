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
    public boolean intersects(UiBounds other) {
        return width > 0 && height > 0 && other.width > 0 && other.height > 0
            && Math.max(x, other.x) < Math.min(x + width, other.x + other.width)
            && Math.max(y, other.y) < Math.min(y + height, other.y + other.height);
    }
    public UiBounds intersection(UiBounds other) {
        float left = Math.max(x, other.x);
        float top = Math.max(y, other.y);
        float right = Math.min(x + width, other.x + other.width);
        float bottom = Math.min(y + height, other.y + other.height);
        return right <= left || bottom <= top ? EMPTY : new UiBounds(left, top, right - left, bottom - top);
    }
}
