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
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiBinding;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;
import com.rethinkqaq.configui.core.UiKey;

/** A boolean switch control. */
public class UiToggle extends Ui.Node {
    private final UiText text;
    private final UiBinding<Boolean> binding;
    private float onProgress = -1f;
    private long lastValueMotionNanos;

    public UiToggle(UiText text, UiBinding<Boolean> binding) {
        this.text = text;
        this.binding = binding;
    }

    public UiText text() { return text; }
    public UiBinding<Boolean> binding() { return binding; }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        measuredWidth = maxWidth;
        measuredHeight = theme.metrics().controlHeight();
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        boolean on = Boolean.TRUE.equals(binding.get());
        float width = theme.metrics().controlHeight() * 1.65f;
        Ui.drawFittedText(renderer, text, bounds.x(),
            bounds.y() + (bounds.height() - renderer.lineHeight()) / 2,
            Math.max(0, bounds.width() - width - theme.metrics().spacing()),
            enabled() ? theme.palette().textPrimary() : theme.palette().textDisabled());
        float progress = onProgress < 0 ? (on ? 1f : 0f) : onProgress;
        UiBounds track = new UiBounds(bounds.x() + bounds.width() - width, bounds.y(), width, bounds.height());
        int trackColor = !enabled()
            ? blend(theme.palette().controlDisabled(), theme.palette().surface(), .35f)
            : blend(theme.palette().control(), theme.palette().accent(), progress);
        renderer.fillRoundRect(track, track.height() / 2, trackColor);
        float knob = track.height() - theme.metrics().padding();
        renderer.fillRoundRect(new UiBounds(
            track.x() + theme.metrics().padding() / 2
                + (track.width() - knob - theme.metrics().padding()) * progress,
            track.y() + theme.metrics().padding() / 2, knob, knob), knob / 2,
            enabled() ? theme.palette().surfaceRaised() : theme.palette().textDisabled());
        if (hasVisibleFocus(theme)) renderer.strokeRoundRect(track, track.height() / 2,
            theme.metrics().borderWidth(), blend(theme.palette().border(), theme.palette().focusRing(), focusProgress()));
    }

    public float onProgress() { return onProgress < 0 ? (Boolean.TRUE.equals(binding.get()) ? 1f : 0f) : onProgress; }

    @Override
    public void advanceMotion(long nowNanos, UiTheme theme) {
        super.advanceMotion(nowNanos, theme);
        float target = Boolean.TRUE.equals(binding.get()) ? 1f : 0f;
        float elapsedMillis = lastValueMotionNanos == 0 ? 0f
            : Math.max(0f, (nowNanos - lastValueMotionNanos) / 1_000_000f);
        lastValueMotionNanos = nowNanos;
        if (onProgress < 0) onProgress = target;
        else onProgress = approach(onProgress, target, elapsedMillis, theme.motion().toggleMillis());
    }

    private void flip() { binding.set(!Boolean.TRUE.equals(binding.get())); }

    @Override
    public boolean click(float x, float y, int button) {
        if (enabled() && button == 0 && bounds.contains(x, y)) { flip(); return true; }
        return false;
    }

    @Override
    public boolean key(int keyCode) {
        if (enabled() && (keyCode == UiKey.ENTER || keyCode == UiKey.SPACE
            || keyCode == UiKey.LEFT || keyCode == UiKey.RIGHT)) { flip(); return true; }
        return false;
    }

    @Override public boolean focusable() { return enabled(); }
}
