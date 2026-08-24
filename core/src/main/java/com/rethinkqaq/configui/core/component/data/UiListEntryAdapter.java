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

package com.rethinkqaq.configui.core.component.data;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBinding;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.setting.UiValidationResult;
import com.rethinkqaq.configui.core.setting.UiValidator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/** Describes how a {@link UiCollectionEditor} creates, labels, edits and validates one entry type. */
public final class UiListEntryAdapter<T> {
    private final Function<T, UiText> label;
    private final Supplier<T> newValue;
    private final Function<UiBinding<T>, Ui.Node> editor;
    private final Function<T, T> copier;
    private final UiValidator<? super T> validator;
    private final boolean uniqueValues;

    private UiListEntryAdapter(Builder<T> builder) {
        label = builder.label;
        newValue = builder.newValue;
        editor = builder.editor;
        copier = builder.copier;
        validator = builder.validator;
        uniqueValues = builder.uniqueValues;
    }

    public UiText label(T value) { return label.apply(value); }
    public T newValue() { return newValue.get(); }
    public Ui.Node editor(UiBinding<T> binding) { return editor.apply(binding); }
    public T copy(T value) { return copier.apply(value); }
    public UiValidationResult validate(T value) { return validator.validate(value); }
    public boolean uniqueValues() { return uniqueValues; }

    public static <T> Builder<T> builder(Supplier<T> newValue, Function<T, UiText> label,
                                         Function<UiBinding<T>, Ui.Node> editor) {
        return new Builder<>(newValue, label, editor);
    }

    public static final class Builder<T> {
        private final Supplier<T> newValue;
        private final Function<T, UiText> label;
        private final Function<UiBinding<T>, Ui.Node> editor;
        private Function<T, T> copier = Function.identity();
        private UiValidator<? super T> validator = UiValidator.acceptAll();
        private boolean uniqueValues;

        private Builder(Supplier<T> newValue, Function<T, UiText> label, Function<UiBinding<T>, Ui.Node> editor) {
            this.newValue = Objects.requireNonNull(newValue, "newValue");
            this.label = Objects.requireNonNull(label, "label");
            this.editor = Objects.requireNonNull(editor, "editor");
        }

        public Builder<T> copier(Function<T, T> value) { copier = Objects.requireNonNull(value, "copier"); return this; }
        public Builder<T> validator(UiValidator<? super T> value) { validator = Objects.requireNonNull(value, "validator"); return this; }
        /** Rejects an entry equal to another value already present in its collection. */
        public Builder<T> uniqueValues() { uniqueValues = true; return this; }
        public UiListEntryAdapter<T> build() { return new UiListEntryAdapter<>(this); }
    }
}
