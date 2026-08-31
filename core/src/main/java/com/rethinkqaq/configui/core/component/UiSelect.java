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

package com.rethinkqaq.configui.core.component;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBinding;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTextMetrics;
import com.rethinkqaq.configui.core.UiTheme;
import com.rethinkqaq.configui.core.UiKey;

/** A compact cycling selector. */
public class UiSelect<T> extends Ui.Node {
    private final UiText text;
    private final UiBinding<T> binding;
    private final List<T> values;
    private final Function<T, UiText> labels;

    public UiSelect(UiText text, UiBinding<T> binding, List<T> values, Function<T, UiText> labels) {
        this.text = Objects.requireNonNull(text, "text");
        this.binding = Objects.requireNonNull(binding, "binding");
        this.values = List.copyOf(values);
        this.labels = Objects.requireNonNull(labels, "labels");
        if (this.values.isEmpty()) throw new IllegalArgumentException("Select options must not be empty");
    }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        measuredWidth = maxWidth;
        measuredHeight = theme.metrics().controlHeight();
    }

    private void advance(int amount) {
        int at = values.indexOf(binding.get());
        binding.set(values.get(Math.floorMod(at + amount, values.size())));
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        int color = !enabled() ? theme.palette().controlDisabled()
            : blend(theme.palette().control(), theme.palette().controlHover(), hoverProgress());
        renderer.fillRoundRect(bounds, theme.metrics().controlRadius(), color);
        int textColor = enabled() ? theme.palette().onAccent() : theme.palette().textDisabled();
        float innerWidth = Math.max(0, bounds.width() - theme.metrics().padding() * 2);
        float half = innerWidth * .5f;
        float scale = UiTextMetrics.buttonScale(theme.metrics());
        float y = bounds.y() + (bounds.height() - UiTextMetrics.lineHeight(renderer, scale)) / 2;
        UiTextMetrics.draw(renderer, text, bounds.x() + theme.metrics().padding(), y, half, textColor, scale);
        UiText selected = UiTextMetrics.fit(renderer, labels.apply(binding.get()), half, scale);
        UiTextMetrics.draw(renderer, selected,
            bounds.x() + bounds.width() - theme.metrics().padding() - renderer.textWidth(selected, scale),
            y, half, textColor, scale);
        if (hasVisibleFocus(theme)) renderer.strokeRoundRect(bounds, theme.metrics().controlRadius(),
            theme.metrics().borderWidth(), blend(theme.palette().border(), theme.palette().focusRing(), focusProgress()));
    }

    @Override public boolean click(float x, float y, int button) {
        if (enabled() && button == 0 && bounds.contains(x, y)) { advance(1); return true; }
        return false;
    }

    @Override public boolean key(int keyCode) {
        if (!enabled()) return false;
        if (keyCode == UiKey.LEFT || keyCode == UiKey.UP) { advance(-1); return true; }
        if (keyCode == UiKey.RIGHT || keyCode == UiKey.DOWN
            || keyCode == UiKey.ENTER || keyCode == UiKey.SPACE) { advance(1); return true; }
        return false;
    }

    @Override public boolean focusable() { return enabled(); }
}
