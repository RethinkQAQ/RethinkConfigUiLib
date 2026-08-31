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

import java.util.Objects;

/**
 * Standard label/description/control composition for a configuration setting.
 * It becomes vertical in a narrow viewport and keeps the control aligned on the right otherwise.
 */
public final class UiSettingRow extends Ui.Node implements Ui.ChildProvider {
    /** Logical width below which the label and control use a vertical flow. */
    private static final float COMPACT_WIDTH = 760;
    private final UiText label;
    private UiText description;
    private final Ui.Node control;
    private final UiLabeledControlLayout layout = new UiLabeledControlLayout();

    UiSettingRow(UiText label, Ui.Node control) {
        this.label = Objects.requireNonNull(label, "label");
        this.control = Objects.requireNonNull(control, "control");
    }

    public UiSettingRow description(UiText value) {
        description = Objects.requireNonNull(value, "description");
        invalidateMeasure();
        return this;
    }
    @Override public UiSettingRow enabled(boolean value) { super.enabled(value); control.enabled(value); return this; }
    public UiSettingRow compactBelow(float width) {
        if (width <= 0) throw new IllegalArgumentException("compact width must be positive");
        compactWidth = width;
        invalidateMeasure();
        return this;
    }

    private float compactWidth = COMPACT_WIDTH;
    public Ui.Node control() { return control; }
    @Override public java.util.List<Ui.Node> childNodes() { return java.util.List.of(control); }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        layout.measure(renderer, label, description, control, maxWidth, maxHeight, theme, compactWidth);
        measuredWidth = maxWidth;
        measuredHeight = layout.measuredHeight(control, theme);
    }

    @Override
    public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        layout.layout(renderer, value, control, theme, 0);
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        int primary = enabled() ? theme.palette().textPrimary() : theme.palette().textDisabled();
        int secondary = enabled() ? theme.palette().textSecondary() : theme.palette().textDisabled();
        layout.render(renderer, bounds, theme, primary, secondary);
        control.render(renderer, theme);
    }

    @Override public boolean click(float x, float y, int button) { return enabled() && control.click(x, y, button); }
    @Override public boolean scroll(float x, float y, double amount) { return enabled() && control.scroll(x, y, amount); }
    @Override public boolean key(int keyCode) { return enabled() && control.key(keyCode); }
    @Override public boolean focusable() { return false; }
    @Override public void setFocused(boolean value) { super.setFocused(value); control.setFocused(value); }
    @Override public void setHovered(boolean value) { super.setHovered(value); }
}
