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
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
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
    private boolean compact;
    private int labelLines;
    private int descriptionLines;
    private float labelHeight;
    private float controlWidth;

    public UiFormField(UiText label, Ui.Node control) {
        this.label = Objects.requireNonNull(label, "label");
        this.control = Objects.requireNonNull(control, "control");
    }

    public UiFormField description(UiText value) { description = Objects.requireNonNull(value, "value"); return this; }
    public UiFormField validation(Supplier<UiValidationResult> value) { validation = Objects.requireNonNull(value, "value"); return this; }
    public UiFormField compactBelow(float value) { if (value <= 0) throw new IllegalArgumentException("compact width"); compactWidth = value; return this; }
    public Ui.Node control() { return control; }

    @Override public UiFormField enabled(boolean value) { super.enabled(value); control.enabled(value); return this; }
    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        compact = maxWidth < compactWidth;
        if (compact) {
            control.measure(renderer, maxWidth, maxHeight, theme);
            controlWidth = maxWidth;
            labelLines = Ui.wrapLines(renderer, label, maxWidth, 3).size();
            descriptionLines = description == null ? 0 : Ui.wrapLines(renderer, description, maxWidth, 3).size();
        } else {
            controlWidth = Math.min(Math.max(theme.metrics().controlHeight() * 3.5f, maxWidth * .38f), maxWidth * .5f);
            control.measure(renderer, controlWidth, maxHeight, theme);
            controlWidth = control.measuredWidth();
            float textWidth = Math.max(0, maxWidth - controlWidth - theme.metrics().spacing());
            labelLines = Ui.wrapLines(renderer, label, textWidth, 3).size();
            descriptionLines = description == null ? 0 : Ui.wrapLines(renderer, description, textWidth, 3).size();
        }
        labelHeight = renderer.lineHeight() * labelLines + (descriptionLines == 0 ? 0 : descriptionLines * renderer.lineHeight() + theme.metrics().spacing() / 2f);
        float messageHeight = messageHeight(renderer, theme);
        measuredWidth = maxWidth;
        measuredHeight = (compact ? labelHeight + theme.metrics().spacing() + control.measuredHeight() : Math.max(labelHeight, control.measuredHeight())) + messageHeight;
    }

    @Override public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        float messageHeight = messageHeight(renderer, theme);
        float controlAreaHeight = Math.max(0, value.height() - messageHeight);
        if (compact) {
            control.layout(renderer, new UiBounds(value.x(), value.y() + labelHeight + theme.metrics().spacing(), value.width(), control.measuredHeight()), theme);
        } else {
            control.layout(renderer, new UiBounds(value.x() + value.width() - controlWidth,
                value.y() + (controlAreaHeight - control.measuredHeight()) / 2f, controlWidth, control.measuredHeight()), theme);
        }
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) {
        UiValidationResult result = validation.get();
        int primary = enabled() ? theme.palette().textPrimary() : theme.palette().textDisabled();
        int secondary = enabled() ? theme.palette().textSecondary() : theme.palette().textDisabled();
        float textWidth = compact ? bounds.width() : Math.max(0, control.bounds().x() - bounds.x() - theme.metrics().spacing());
        Ui.drawWrappedText(renderer, label, bounds.x(), bounds.y(), textWidth, labelLines, primary, 0);
        if (description != null) Ui.drawWrappedText(renderer, description, bounds.x(), bounds.y() + renderer.lineHeight() * labelLines + theme.metrics().spacing() / 2f, textWidth, descriptionLines, secondary, 0);
        control.render(renderer, theme);
        if (result.severity() != UiValidationResult.Severity.OK) {
            int color = result.severity() == UiValidationResult.Severity.ERROR ? theme.palette().danger() : theme.palette().warning();
            Ui.drawFittedText(renderer, result.message(), bounds.x(), bounds.y() + bounds.height() - renderer.lineHeight(), bounds.width(), color);
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
        return validation.get().severity() == UiValidationResult.Severity.OK ? 0 : renderer.lineHeight() + theme.metrics().spacing() / 2f;
    }
}
