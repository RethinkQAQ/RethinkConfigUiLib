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
import java.util.function.Supplier;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiKey;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;
import com.rethinkqaq.configui.core.UiTextMetrics;

/** A semantic action button. */
public class UiButton extends Ui.Node {
    protected final UiText text;
    private final Runnable action;
    private boolean pressed;
    private Ui.ButtonVariant variant = Ui.ButtonVariant.PRIMARY;
    private Supplier<Ui.ButtonVariant> variantSupplier;
    private float preferredWidth = -1;

    public UiButton(UiText text, Runnable action) {
        this.text = Objects.requireNonNull(text, "text");
        this.action = Objects.requireNonNull(action, "action");
    }

    public UiText text() { return text; }
    public UiButton variant(Ui.ButtonVariant value) { variant = Objects.requireNonNull(value, "variant"); variantSupplier = null; return this; }
    public UiButton variant(Supplier<Ui.ButtonVariant> value) { variantSupplier = Objects.requireNonNull(value, "variant"); return this; }
    public Ui.ButtonVariant variant() { return variant; }
    /** Sets an optional fixed logical width for compact action rows. */
    public UiButton preferredWidth(float value) {
        if (value <= 0) throw new IllegalArgumentException("preferred button width must be positive");
        preferredWidth = value;
        invalidateLayout();
        return this;
    }
    private Ui.ButtonVariant currentVariant() {
        return variantSupplier == null ? variant : Objects.requireNonNull(variantSupplier.get(), "variant supplier result");
    }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float textScale = UiTextMetrics.buttonScale(theme.metrics());
        float naturalWidth = Math.max(theme.metrics().controlHeight() * 2,
            renderer.textWidth(text, textScale) + theme.metrics().padding() * 3);
        measuredWidth = Math.min(maxWidth, preferredWidth > 0 ? preferredWidth : naturalWidth);
        measuredHeight = theme.metrics().controlHeight();
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        float textScale = UiTextMetrics.buttonScale(theme.metrics());
        Ui.ButtonVariant currentVariant = currentVariant();
        int color = color(theme);
        boolean unselected = currentVariant == Ui.ButtonVariant.SECONDARY || currentVariant == Ui.ButtonVariant.OUTLINE;
        boolean accentHover = unselected && hoverProgress() > .5f;
        int textColor = !accentHover && unselected && enabled()
            ? theme.palette().textPrimary() : theme.palette().onAccent();
        renderer.fillRoundRect(bounds, theme.metrics().controlRadius(), color);
        UiText displayed = Ui.fitText(renderer, text, Math.max(0, bounds.width() - theme.metrics().padding() * 2), textScale);
        float x = bounds.x() + (bounds.width() - renderer.textWidth(displayed, textScale)) / 2;
        Ui.drawFittedText(renderer, displayed, x,
            bounds.y() + (bounds.height() - renderer.lineHeight(textScale)) / 2,
            Math.max(0, bounds.x() + bounds.width() - theme.metrics().padding() - x), textColor, textScale);
        if (currentVariant == Ui.ButtonVariant.OUTLINE || currentVariant == Ui.ButtonVariant.SECONDARY) {
            renderer.strokeRoundRect(bounds, theme.metrics().controlRadius(),
                theme.metrics().borderWidth(), theme.palette().border());
        }
        if (hasVisibleFocus(theme)) {
            renderer.strokeRoundRect(bounds, theme.metrics().controlRadius(), theme.metrics().borderWidth(),
                blend(theme.palette().border(), theme.palette().accent(), focusProgress() * .55f));
        }
    }

    private int color(UiTheme theme) {
        if (!enabled()) return theme.palette().controlDisabled();
        return switch (currentVariant()) {
            case PRIMARY -> pressed ? blend(theme.palette().control(), 0xFF000000, .12f)
                : blend(theme.palette().control(), theme.palette().surfaceRaised(), hoverProgress() * .12f);
            case SECONDARY, OUTLINE -> pressed ? theme.palette().border()
                : blend(currentVariant() == Ui.ButtonVariant.OUTLINE ? theme.palette().surface() : theme.palette().surfaceRaised(),
                    theme.palette().accentHover(), hoverProgress());
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
