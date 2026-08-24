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

package com.rethinkqaq.configui.config;

import com.rethinkqaq.configui.core.UiBinding;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ConfigValue<T> {
    private final String path;
    private final String section;
    private final String key;
    private final String title;
    private final String description;
    private final String constraints;
    private final T defaultValue;
    private final ConfigCodec<T> codec;
    private final List<ConfigValidator<T>> validators;
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private final BiConsumer<ConfigValue<T>, ConfigValidationResult> changeListener;
    private volatile Runnable dirtyListener;
    private volatile ConfigValidationResult lastValidation = ConfigValidationResult.ok();

    public ConfigValue(String path, String section, String key, String title, String description,
                       String constraints, T defaultValue, ConfigCodec<T> codec,
                       List<ConfigValidator<T>> validators, Supplier<T> getter, Consumer<T> setter,
                       BiConsumer<ConfigValue<T>, ConfigValidationResult> changeListener) {
        this.path = Objects.requireNonNull(path, "path");
        this.section = Objects.requireNonNull(section, "section");
        this.key = Objects.requireNonNull(key, "key");
        this.title = Objects.requireNonNull(title, "title");
        this.description = description == null ? "" : description;
        this.constraints = constraints == null ? "" : constraints;
        this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.validators = List.copyOf(validators == null ? List.of() : validators);
        this.getter = Objects.requireNonNull(getter, "getter");
        this.setter = Objects.requireNonNull(setter, "setter");
        this.changeListener = changeListener;
        ConfigValidationResult initial = validate(defaultValue);
        if (!initial.valid()) throw new IllegalArgumentException("Invalid default for " + path + ": " + initial.message());
    }

    public String path() { return path; }
    public String section() { return section; }
    public String key() { return key; }
    public String title() { return title; }
    public String description() { return description; }
    public String constraints() { return constraints; }
    public T defaultValue() { return defaultValue; }
    public T get() { return getter.get(); }
    public ConfigCodec<T> codec() { return codec; }
    public ConfigValidationResult lastValidation() { return lastValidation; }
    public boolean isDefault() { return Objects.equals(get(), defaultValue); }

    public synchronized ConfigValidationResult validate(T candidate) {
        ConfigValidationResult result = ConfigValidationResult.ok();
        for (ConfigValidator<T> validator : validators) {
            result = Objects.requireNonNull(validator.validate(candidate), "validator result");
            if (!result.valid()) break;
        }
        return result;
    }

    public synchronized boolean set(T candidate) {
        Objects.requireNonNull(candidate, "candidate");
        ConfigValidationResult result = validate(candidate);
        lastValidation = result;
        if (!result.valid()) {
            if (changeListener != null) changeListener.accept(this, result);
            return false;
        }
        setter.accept(candidate);
        if (changeListener != null) changeListener.accept(this, result);
        if (dirtyListener != null) dirtyListener.run();
        return true;
    }

    public synchronized void reset() { set(defaultValue); }

    public UiBinding<T> binding() { return UiBinding.of(this::get, this::set); }

    public void attachDirtyListener(Runnable listener) { dirtyListener = listener; }

    void loadValue(T candidate) {
        setter.accept(Objects.requireNonNull(candidate, "candidate"));
        lastValidation = ConfigValidationResult.ok();
    }

    public static <T> ConfigValue<T> generated(String path, String section, String key, String title,
                                        String description, String constraints, T defaultValue,
                                        ConfigCodec<T> codec, List<ConfigValidator<T>> validators,
                                        Supplier<T> getter, Consumer<T> setter) {
        return new ConfigValue<>(path, section, key, title, description, constraints, defaultValue,
            codec, validators, getter, setter, null);
    }
}
