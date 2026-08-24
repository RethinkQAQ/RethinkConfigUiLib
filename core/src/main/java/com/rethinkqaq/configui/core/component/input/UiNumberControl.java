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
import com.rethinkqaq.configui.core.UiBinding;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiClipboard;
import com.rethinkqaq.configui.core.UiKeyEvent;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTextInput;
import com.rethinkqaq.configui.core.UiTheme;
import com.rethinkqaq.configui.core.setting.UiNumberSpec;
import com.rethinkqaq.configui.core.setting.UiSetting;
import java.util.List;

/** One bounded number control: a drag slider and manual value field backed by the same setting. */
public final class UiNumberControl<T extends Number> extends Ui.Node implements Ui.ChildProvider {
    private final UiNumericField<T> field;
    private final Ui.Slider slider;

    public UiNumberControl(UiSetting<T> setting, UiNumberSpec<T> spec) {
        if (!spec.hasFiniteRange() || spec.step() == null) {
            throw new IllegalArgumentException("number controls require a finite range and positive step");
        }
        field = new UiNumericField<>(setting, spec);
        UiBinding<Double> value = UiBinding.of(
            () -> spec.codec().asDouble(setting.get()),
            next -> setting.set(spec.snap(spec.codec().fromDouble(next)))
        );
        slider = Ui.slider(UiText.literal(""), value, spec.minimum(), spec.maximum(), spec.step());
    }

    public UiNumericField<T> field() { return field; }
    public Ui.Slider slider() { return slider; }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float fieldWidth = Math.min(Math.max(72, maxWidth * .24f), 112);
        fieldWidth = Math.min(fieldWidth, Math.max(48, maxWidth - 40 - theme.metrics().spacing()));
        field.measure(renderer, fieldWidth, maxHeight, theme);
        slider.measure(renderer, Math.max(0, maxWidth - fieldWidth - theme.metrics().spacing()), maxHeight, theme);
        measuredWidth = maxWidth;
        measuredHeight = Math.max(field.measuredHeight(), slider.measuredHeight());
    }

    @Override
    public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        float fieldWidth = field.measuredWidth();
        float sliderWidth = Math.max(0, value.width() - fieldWidth - theme.metrics().spacing());
        slider.layout(renderer, new UiBounds(value.x(), value.y() + (value.height() - slider.measuredHeight()) / 2f,
            sliderWidth, slider.measuredHeight()), theme);
        field.layout(renderer, new UiBounds(value.x() + sliderWidth + theme.metrics().spacing(),
            value.y() + (value.height() - field.measuredHeight()) / 2f, fieldWidth, field.measuredHeight()), theme);
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) {
        field.syncFromSetting();
        slider.render(renderer, theme);
        field.render(renderer, theme);
    }
    @Override public List<Ui.Node> childNodes() { return List.of(slider, field); }
    @Override public boolean click(float x, float y, int button) {
        return enabled() && (field.click(x, y, button) || slider.click(x, y, button));
    }
    @Override public boolean drag(float x, float y, int button) { return enabled() && slider.drag(x, y, button); }
    @Override public boolean release(float x, float y, int button) { return field.release(x, y, button) | slider.release(x, y, button); }
    @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) { return field.key(event, clipboard) || slider.key(event, clipboard); }
    @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) { return field.textInput(event, clipboard); }
    @Override public boolean focusable() { return false; }
}
