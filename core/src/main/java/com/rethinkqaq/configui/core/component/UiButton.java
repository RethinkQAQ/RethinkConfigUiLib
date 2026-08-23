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

import java.util.Objects;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiKey;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;

/** A semantic action button. */
public class UiButton extends Ui.Node {
    protected final UiText text;
    private final Runnable action;
    private boolean pressed;
    private Ui.ButtonVariant variant = Ui.ButtonVariant.PRIMARY;

    public UiButton(UiText text, Runnable action) {
        this.text = Objects.requireNonNull(text, "text");
        this.action = Objects.requireNonNull(action, "action");
    }

    public UiText text() { return text; }
    public UiButton variant(Ui.ButtonVariant value) { variant = Objects.requireNonNull(value, "variant"); return this; }
    public Ui.ButtonVariant variant() { return variant; }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        measuredWidth = Math.min(maxWidth, Math.max(theme.metrics().controlHeight() * 2,
            renderer.textWidth(text) + theme.metrics().padding() * 3));
        measuredHeight = theme.metrics().controlHeight();
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        int color = color(theme);
        int textColor = (variant == Ui.ButtonVariant.SECONDARY || variant == Ui.ButtonVariant.OUTLINE) && enabled()
            ? theme.palette().textPrimary() : theme.palette().onAccent();
        renderer.fillRoundRect(bounds, theme.metrics().controlRadius(), color);
        UiText displayed = Ui.fitText(renderer, text, Math.max(0, bounds.width() - theme.metrics().padding() * 2));
        float x = bounds.x() + (bounds.width() - renderer.textWidth(displayed)) / 2;
        Ui.drawFittedText(renderer, displayed, x,
            bounds.y() + (bounds.height() - renderer.lineHeight()) / 2,
            Math.max(0, bounds.x() + bounds.width() - theme.metrics().padding() - x), textColor);
        if (variant == Ui.ButtonVariant.OUTLINE || variant == Ui.ButtonVariant.SECONDARY) {
            renderer.strokeRoundRect(bounds, theme.metrics().controlRadius(),
                theme.metrics().borderWidth(), theme.palette().border());
        }
        if (hasVisibleFocus(theme)) {
            renderer.strokeRoundRect(bounds, theme.metrics().controlRadius(), theme.metrics().borderWidth(),
                blend(theme.palette().border(), theme.palette().focusRing(), focusProgress()));
        }
    }

    private int color(UiTheme theme) {
        if (!enabled()) return theme.palette().controlDisabled();
        return switch (variant) {
            case PRIMARY -> pressed ? theme.palette().accentPressed()
                : blend(theme.palette().control(), theme.palette().accentHover(), hoverProgress());
            case SECONDARY -> pressed ? theme.palette().border()
                : blend(theme.palette().surfaceRaised(), theme.palette().surface(), hoverProgress());
            case OUTLINE -> pressed ? theme.palette().border()
                : blend(theme.palette().surface(), theme.palette().surfaceRaised(), hoverProgress());
            case DANGER -> pressed ? theme.palette().danger()
                : blend(theme.palette().danger(), theme.palette().accentPressed(), hoverProgress());
        };
    }

    @Override
    public boolean click(float x, float y, int button) {
        if (enabled() && button == 0 && bounds.contains(x, y)) {
            pressed = true;
            action.run();
            return true;
        }
        return false;
    }

    @Override
    public boolean key(int keyCode) {
        if (enabled() && (keyCode == UiKey.ENTER || keyCode == UiKey.SPACE)) {
            action.run();
            return true;
        }
        return false;
    }

    @Override
    public boolean release(float x, float y, int button) {
        boolean wasPressed = pressed;
        if (button == 0) pressed = false;
        return wasPressed;
    }

    @Override
    public boolean focusable() { return enabled(); }
}
