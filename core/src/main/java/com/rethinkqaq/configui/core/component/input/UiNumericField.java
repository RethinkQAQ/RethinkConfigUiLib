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

import com.rethinkqaq.configui.core.UiClipboard;
import com.rethinkqaq.configui.core.UiKey;
import com.rethinkqaq.configui.core.UiKeyEvent;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.setting.UiNumberSpec;
import com.rethinkqaq.configui.core.setting.UiSetting;
import com.rethinkqaq.configui.core.setting.UiValidationResult;
import java.util.Objects;

/** Text-backed number editor. Invalid text stays visible and never reaches the binding. */
public class UiNumericField<T extends Number> extends UiTextField {
    private final UiSetting<T> setting;
    private final UiNumberSpec<T> spec;
    public UiNumericField(UiSetting<T> setting, UiNumberSpec<T> spec) {
        super(com.rethinkqaq.configui.core.UiBinding.of(() -> "", value -> { })); this.setting = Objects.requireNonNull(setting, "setting"); this.spec = Objects.requireNonNull(spec, "spec");
        setDraft(spec.format(setting.get()));
        validator(value -> validate(value));
    }
    private UiValidationResult validate(String value) {
        try { T parsed = spec.codec().parse(value); UiValidationResult range = spec.validate(parsed); return range.severity() == UiValidationResult.Severity.ERROR ? range : setting.validate(parsed); }
        catch (NumberFormatException ignored) { return UiValidationResult.error(UiText.literal("Enter a valid number")); }
    }
    @Override public boolean commit() {
        try {
            T parsed = spec.codec().parse(draft());
            UiValidationResult result = spec.validate(parsed);
            if (!result.accepted()) return false;
            parsed = spec.snap(parsed);
            result = spec.validate(parsed);
            if (result.accepted()) result = setting.set(parsed);
            if (!result.accepted()) return false;
            setDraft(spec.format(parsed)); return true;
        } catch (NumberFormatException ignored) { return false; }
    }
    @Override public void cancel() { setDraft(spec.format(setting.get())); }
    /** Refreshes the visible value after a sibling control writes to the shared setting. */
    public void syncFromSetting() {
        if (focused()) return;
        String formatted = spec.format(setting.get());
        if (!draft().equals(formatted)) setDraft(formatted);
    }
    @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) {
        if ((event.keyCode() == UiKey.UP || event.keyCode() == UiKey.DOWN) && spec.step() != null) {
            double delta = event.keyCode() == UiKey.UP ? spec.step() : -spec.step();
            T value = spec.snap(spec.codec().fromDouble(spec.codec().asDouble(setting.get()) + delta));
            UiValidationResult result = spec.validate(value); if (result.accepted()) setting.set(value);
            setDraft(spec.format(setting.get())); return true;
        }
        return super.key(event, clipboard);
    }
    public UiSetting<T> setting() { return setting; }
    public UiNumberSpec<T> spec() { return spec; }
}
