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

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;
import java.util.ArrayList;
import java.util.List;

/** Host-owned top-right toast stack. */
public final class UiNotificationCenter {
    private final List<Entry> entries = new ArrayList<>();
    public void show(UiToast toast) { entries.add(new Entry(toast, System.nanoTime())); }
    public void clear() { entries.clear(); }
    public int size() { return entries.size(); }
    public void render(UiRenderer renderer, float screenWidth, float screenHeight, UiTheme theme) {
        long now = System.nanoTime(); entries.removeIf(entry -> (now - entry.createdNanos) / 1_000_000L >= entry.toast.durationMillis());
        float y = theme.metrics().padding();
        for (int index = entries.size() - 1; index >= 0; index--) {
            UiToast toast = entries.get(index).toast; float maxWidth = Math.min(300, Math.max(120, screenWidth * .35f));
            List<UiText> lines = Ui.wrapLines(renderer, toast.text(), maxWidth - theme.metrics().padding(), 3);
            float height = lines.size() * renderer.lineHeight() + theme.metrics().padding(); float x = screenWidth - theme.metrics().padding() - maxWidth;
            int tone = toast.color();
            UiBounds bounds = new UiBounds(x, y, maxWidth, height);
            renderer.fillRoundRect(bounds, theme.metrics().controlRadius(), theme.palette().surfaceRaised());
            renderer.strokeRoundRect(bounds, theme.metrics().controlRadius(), theme.metrics().borderWidth(), tone);
            Ui.drawWrappedText(renderer, toast.text(), x + theme.metrics().padding() / 2, y + theme.metrics().padding() / 2, maxWidth - theme.metrics().padding(), lines.size(), theme.palette().textPrimary(), 0);
            y += height + theme.metrics().spacing() / 2;
        }
    }
    private record Entry(UiToast toast, long createdNanos) { }
}
