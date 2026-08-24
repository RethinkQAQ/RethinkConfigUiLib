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

package com.rethinkqaq.configui.core.component.data;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBinding;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiKey;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/** Scrollable single-selection list for choosing one value from a data source. */
public final class UiSelectionList<T> extends Ui.Node {
    private final Supplier<List<T>> items;
    private final UiBinding<T> binding;
    private final Function<T, UiText> labels;
    private UiText emptyText = UiText.literal("No entries");
    private float scroll;
    private float rowHeight = 40;

    public UiSelectionList(Supplier<List<T>> items, UiBinding<T> binding, Function<T, UiText> labels) {
        this.items = Objects.requireNonNull(items, "items");
        this.binding = Objects.requireNonNull(binding, "binding");
        this.labels = Objects.requireNonNull(labels, "labels");
    }

    public UiSelectionList<T> emptyText(UiText value) { emptyText = Objects.requireNonNull(value, "value"); return this; }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        measuredWidth = maxWidth;
        measuredHeight = Math.min(maxHeight, Math.max(theme.metrics().controlHeight() * 3, theme.metrics().controlHeight()));
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) {
        renderer.fillRoundRect(bounds, theme.metrics().controlRadius(), theme.palette().surfaceRaised());
        renderer.strokeRoundRect(bounds, theme.metrics().controlRadius(), theme.metrics().borderWidth(), theme.palette().border());
        List<T> values = List.copyOf(items.get());
        rowHeight = theme.metrics().controlHeight();
        renderer.pushClip(bounds.inset(theme.metrics().borderWidth()));
        if (values.isEmpty()) Ui.drawFittedText(renderer, emptyText, bounds.x() + theme.metrics().padding() / 2,
            bounds.y() + (bounds.height() - renderer.lineHeight()) / 2, bounds.width() - theme.metrics().padding(), theme.palette().textSecondary());
        for (int i = 0; i < values.size(); i++) {
            float y = bounds.y() + i * rowHeight - scroll;
            if (y + rowHeight < bounds.y() || y > bounds.y() + bounds.height()) continue;
            T item = values.get(i);
            boolean selected = Objects.equals(item, binding.get());
            if (selected) renderer.fillRoundRect(new UiBounds(bounds.x() + 2, y + 2, bounds.width() - 4, rowHeight - 4),
                theme.metrics().controlRadius(), theme.palette().control());
            Ui.drawFittedText(renderer, labels.apply(item), bounds.x() + theme.metrics().padding() / 2,
                y + (rowHeight - renderer.lineHeight()) / 2, bounds.width() - theme.metrics().padding(),
                selected ? theme.palette().onAccent() : theme.palette().textPrimary());
        }
        renderer.popClip();
    }

    @Override public boolean click(float x, float y, int button) {
        if (!enabled() || button != 0 || !bounds.contains(x, y)) return false;
        List<T> values = items.get();
        int index = (int) ((y - bounds.y() + scroll) / rowHeight);
        if (index >= 0 && index < values.size()) binding.set(values.get(index));
        return true;
    }

    @Override public boolean scroll(float x, float y, double amount) {
        if (!bounds.contains(x, y)) return false;
        float maxScroll = Math.max(0, items.get().size() * rowHeight - bounds.height());
        scroll = Math.max(0, Math.min(maxScroll, scroll - (float) amount * rowHeight / 2));
        return true;
    }

    @Override public boolean key(int key) {
        List<T> values = items.get();
        if (values.isEmpty()) return false;
        int index = Math.max(0, values.indexOf(binding.get()));
        if (key == UiKey.UP) index--;
        else if (key == UiKey.DOWN || key == UiKey.ENTER || key == UiKey.SPACE) index++;
        else if (key == UiKey.HOME) index = 0;
        else if (key == UiKey.END) index = values.size() - 1;
        else return false;
        binding.set(values.get(Math.max(0, Math.min(values.size() - 1, index))));
        return true;
    }

    @Override public boolean focusable() { return enabled(); }
}
