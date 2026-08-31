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

package com.rethinkqaq.configui.core.component.input;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiClipboard;
import com.rethinkqaq.configui.core.UiKeyEvent;
import com.rethinkqaq.configui.core.UiLabeledControlLayout;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTextMetrics;
import com.rethinkqaq.configui.core.UiTextInput;
import com.rethinkqaq.configui.core.UiTheme;
import com.rethinkqaq.configui.core.setting.UiValidationResult;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Standard field shell for a label, optional description, control and validation message. */
public final class UiFormField extends Ui.Node implements Ui.ChildProvider {
    private static final float DEFAULT_COMPACT_WIDTH = 420;
    private final UiText label;
    private final Ui.Node control;
    private UiText description;
    private Supplier<UiValidationResult> validation = () -> UiValidationResult.OK;
    private float compactWidth = DEFAULT_COMPACT_WIDTH;
    private final UiLabeledControlLayout layout = new UiLabeledControlLayout();

    public UiFormField(UiText label, Ui.Node control) {
        this.label = Objects.requireNonNull(label, "label");
        this.control = Objects.requireNonNull(control, "control");
    }

    public UiFormField description(UiText value) {
        description = Objects.requireNonNull(value, "value");
        invalidateMeasure();
        return this;
    }
    public UiFormField validation(Supplier<UiValidationResult> value) {
        validation = Objects.requireNonNull(value, "value");
        invalidateMeasure();
        return this;
    }
    public UiFormField compactBelow(float value) {
        if (value <= 0) throw new IllegalArgumentException("compact width");
        compactWidth = value;
        invalidateMeasure();
        return this;
    }
    public Ui.Node control() { return control; }

    @Override public UiFormField enabled(boolean value) { super.enabled(value); control.enabled(value); return this; }
    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        layout.measure(renderer, label, description, control, maxWidth, maxHeight, theme, compactWidth);
        float messageHeight = messageHeight(renderer, theme);
        measuredWidth = maxWidth;
        measuredHeight = layout.measuredHeight(control, theme) + messageHeight;
    }

    @Override public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        float messageHeight = messageHeight(renderer, theme);
        layout.layout(renderer, value, control, theme, messageHeight);
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) {
        UiValidationResult result = validation.get();
        int primary = enabled() ? theme.palette().textPrimary() : theme.palette().textDisabled();
        int secondary = enabled() ? theme.palette().textSecondary() : theme.palette().textDisabled();
        layout.render(renderer, bounds, theme, primary, secondary);
        control.render(renderer, theme);
        if (result.severity() != UiValidationResult.Severity.OK) {
            int color = result.severity() == UiValidationResult.Severity.ERROR ? theme.palette().danger() : theme.palette().warning();
            float scale = UiTextMetrics.bodyScale(theme.metrics());
            UiTextMetrics.draw(renderer, result.message(), bounds.x(),
                bounds.y() + bounds.height() - UiTextMetrics.lineHeight(renderer, scale), bounds.width(), color, scale);
        }
    }

    @Override public List<Ui.Node> childNodes() { return List.of(control); }
    @Override public boolean click(float x, float y, int button) { return enabled() && control.click(x, y, button); }
    @Override public boolean drag(float x, float y, int button) { return enabled() && control.drag(x, y, button); }
    @Override public boolean release(float x, float y, int button) { return control.release(x, y, button); }
    @Override public boolean scroll(float x, float y, double amount) { return enabled() && control.scroll(x, y, amount); }
    @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) { return enabled() && control.key(event, clipboard); }
    @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) { return enabled() && control.textInput(event, clipboard); }
    @Override public boolean focusable() { return false; }

    private float messageHeight(UiRenderer renderer, UiTheme theme) {
        return validation.get().severity() == UiValidationResult.Severity.OK ? 0
            : UiTextMetrics.lineHeight(renderer, UiTextMetrics.bodyScale(theme.metrics())) + theme.metrics().spacing() / 2f;
    }
}
