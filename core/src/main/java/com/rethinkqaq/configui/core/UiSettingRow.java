/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.core;

import java.util.Objects;

/**
 * Standard label/description/control composition for a configuration setting.
 * It becomes vertical in a narrow viewport and keeps the control aligned on the right otherwise.
 */
public final class UiSettingRow extends Ui.Node implements Ui.ChildProvider {
    private static final float COMPACT_WIDTH = 420;
    private final UiText label;
    private UiText description;
    private final Ui.Node control;
    private boolean compact;
    private float labelHeight;
    private float controlWidth;

    UiSettingRow(UiText label, Ui.Node control) {
        this.label = Objects.requireNonNull(label, "label");
        this.control = Objects.requireNonNull(control, "control");
    }

    public UiSettingRow description(UiText value) { description = Objects.requireNonNull(value, "description"); return this; }
    @Override public UiSettingRow enabled(boolean value) { super.enabled(value); control.enabled(value); return this; }
    public UiSettingRow compactBelow(float width) {
        if (width <= 0) throw new IllegalArgumentException("compact width must be positive");
        compact = false;
        compactWidth = width;
        return this;
    }

    private float compactWidth = COMPACT_WIDTH;
    public Ui.Node control() { return control; }
    @Override public java.util.List<Ui.Node> childNodes() { return java.util.List.of(control); }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        compact = maxWidth < compactWidth;
        labelHeight = renderer.lineHeight() + (description == null ? 0 : renderer.lineHeight() + theme.metrics().spacing() / 2f);
        if (compact) {
            control.measure(renderer, maxWidth, maxHeight, theme);
            controlWidth = maxWidth;
            measuredWidth = maxWidth;
            measuredHeight = labelHeight + theme.metrics().spacing() + control.measuredHeight();
        } else {
            controlWidth = Math.min(Math.max(theme.metrics().controlHeight() * 3.5f, maxWidth * .38f), maxWidth * .5f);
            control.measure(renderer, controlWidth, maxHeight, theme);
            controlWidth = control.measuredWidth();
            measuredWidth = maxWidth;
            measuredHeight = Math.max(labelHeight, control.measuredHeight());
        }
    }

    @Override
    public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        if (compact) {
            control.layout(renderer, new UiBounds(value.x(), value.y() + labelHeight + theme.metrics().spacing(),
                value.width(), control.measuredHeight()), theme);
        } else {
            control.layout(renderer, new UiBounds(value.x() + value.width() - controlWidth,
                value.y() + (value.height() - control.measuredHeight()) / 2f, controlWidth, control.measuredHeight()), theme);
        }
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        int primary = enabled() ? theme.palette().textPrimary() : theme.palette().textDisabled();
        int secondary = enabled() ? theme.palette().textSecondary() : theme.palette().textDisabled();
        float textWidth = compact ? bounds.width() : Math.max(0, control.bounds().x() - bounds.x() - theme.metrics().spacing());
        Ui.drawFittedText(renderer, label, bounds.x(), bounds.y(), textWidth, primary);
        if (description != null) Ui.drawFittedText(renderer, description, bounds.x(),
            bounds.y() + renderer.lineHeight() + theme.metrics().spacing() / 2f, textWidth, secondary);
        control.render(renderer, theme);
    }

    @Override public boolean click(float x, float y, int button) { return enabled() && control.click(x, y, button); }
    @Override public boolean scroll(float x, float y, double amount) { return enabled() && control.scroll(x, y, amount); }
    @Override public boolean key(int keyCode) { return enabled() && control.key(keyCode); }
    @Override public boolean focusable() { return false; }
    @Override public void setFocused(boolean value) { super.setFocused(value); control.setFocused(value); }
    @Override public void setHovered(boolean value) { super.setHovered(value); }
}
