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

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBinding;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiKey;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;

/** A bounded numeric slider with keyboard and drag interaction. */
public class UiSlider extends Ui.Node {
    private final UiText text;
    private final UiBinding<Double> binding;
    private final double min;
    private final double max;
    private final double step;
    private float displayedRatio = -1f;
    private long lastValueMotionNanos;
    private boolean dragging;

    public UiSlider(UiText text, UiBinding<Double> binding, double min, double max, double step) {
        if (max <= min || step <= 0) throw new IllegalArgumentException("invalid slider range");
        this.text = text;
        this.binding = binding;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public UiText text() { return text; }
    public double min() { return min; }
    public double max() { return max; }
    public double step() { return step; }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        measuredWidth = maxWidth;
        measuredHeight = theme.metrics().controlHeight() + renderer.lineHeight();
    }

    private void set(double value) {
        binding.set(Math.max(min, Math.min(max, Math.round((value - min) / step) * step + min)));
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        int textColor = enabled() ? theme.palette().textPrimary() : theme.palette().textDisabled();
        int accent = enabled() ? theme.palette().accent() : theme.palette().controlDisabled();
        Ui.drawFittedText(renderer, text, bounds.x(), bounds.y(), bounds.width(), textColor);
        UiBounds rail = new UiBounds(bounds.x(), bounds.y() + renderer.lineHeight() + theme.metrics().spacing(),
            bounds.width(), Math.max(1f, theme.metrics().borderWidth() * 2));
        renderer.fillRoundRect(rail, rail.height(), theme.palette().border());
        float ratio = displayedRatio < 0 ? targetRatio() : displayedRatio;
        renderer.fillRoundRect(new UiBounds(rail.x(), rail.y(), rail.width() * ratio, rail.height()), rail.height(), accent);
        float knob = theme.metrics().controlHeight() * .45f;
        renderer.fillRoundRect(new UiBounds(rail.x() + rail.width() * ratio - knob / 2,
            rail.y() - knob / 2 + rail.height() / 2, knob, knob), knob / 2, accent);
        if (hasVisibleFocus(theme)) renderer.strokeRoundRect(
            new UiBounds(rail.x() - knob / 2, rail.y() - knob / 2, rail.width() + knob, knob + rail.height()),
            knob / 2, theme.metrics().borderWidth(),
            blend(theme.palette().border(), theme.palette().focusRing(), focusProgress()));
    }

    public float displayedRatio() { return displayedRatio < 0 ? targetRatio() : displayedRatio; }

    @Override
    public void advanceMotion(long nowNanos, UiTheme theme) {
        super.advanceMotion(nowNanos, theme);
        float target = targetRatio();
        float elapsedMillis = lastValueMotionNanos == 0 ? 0f
            : Math.max(0f, (nowNanos - lastValueMotionNanos) / 1_000_000f);
        lastValueMotionNanos = nowNanos;
        if (displayedRatio < 0) displayedRatio = target;
        else displayedRatio = approach(displayedRatio, target, elapsedMillis, theme.motion().toggleMillis());
    }

    private float targetRatio() {
        return Math.max(0f, Math.min(1f, (float) ((binding.get() - min) / (max - min))));
    }

    private void setFromX(float x) {
        set(min + Math.max(0f, Math.min(1f, (x - bounds.x()) / Math.max(1f, bounds.width()))) * (max - min));
    }

    @Override
    public boolean click(float x, float y, int button) {
        if (enabled() && button == 0 && bounds.contains(x, y)) { dragging = true; setFromX(x); return true; }
        return false;
    }

    @Override
    public boolean drag(float x, float y, int button) {
        if (enabled() && dragging && button == 0) { setFromX(x); return true; }
        return false;
    }

    @Override
    public boolean release(float x, float y, int button) {
        boolean wasDragging = dragging;
        if (button == 0) dragging = false;
        return wasDragging;
    }

    @Override
    public boolean key(int keyCode) {
        if (!enabled()) return false;
        if (keyCode == UiKey.LEFT || keyCode == UiKey.DOWN) { set(binding.get() - step); return true; }
        if (keyCode == UiKey.RIGHT || keyCode == UiKey.UP) { set(binding.get() + step); return true; }
        return false;
    }

    @Override public boolean focusable() { return enabled(); }
}
