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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class ConfigEntry<T> {
    private final String path;
    private final String key;
    private final String title;
    private final String description;
    private final String constraints;
    private final T defaultValue;
    private final ConfigCodec<T> codec;
    private final List<ConfigValidator<T>> validators;
    private final BiConsumer<ConfigEntry<T>, ConfigValidationResult> changeListener;
    private volatile Runnable dirtyListener;
    private volatile T value;
    private volatile ConfigValidationResult lastValidation = ConfigValidationResult.ok();

    ConfigEntry(String path, String key, String title, String description, String constraints, T defaultValue,
                ConfigCodec<T> codec, List<ConfigValidator<T>> validators,
                BiConsumer<ConfigEntry<T>, ConfigValidationResult> changeListener) {
        this.path = path;
        this.key = key;
        this.title = title;
        this.description = description;
        this.constraints = constraints;
        this.defaultValue = defaultValue;
        this.codec = codec;
        this.validators = List.copyOf(validators);
        this.changeListener = changeListener;
        this.value = defaultValue;
    }

    public String path() { return path; }
    public String key() { return key; }
    public String title() { return title; }
    public String description() { return description; }
    public String constraints() { return constraints; }
    public T defaultValue() { return defaultValue; }
    public T get() { return value; }
    public ConfigCodec<T> codec() { return codec; }
    public ConfigValidationResult lastValidation() { return lastValidation; }
    public boolean isDefault() { return Objects.equals(value, defaultValue); }

    public synchronized ConfigValidationResult validate(T candidate) {
        ConfigValidationResult result = ConfigValidationResult.ok();
        for (ConfigValidator<T> validator : validators) {
            result = Objects.requireNonNull(validator.validate(candidate), "validator result");
            if (!result.valid()) break;
        }
        return result;
    }

    public synchronized boolean set(T candidate) {
        ConfigValidationResult result = validate(candidate);
        lastValidation = result;
        if (!result.valid()) {
            if (changeListener != null) changeListener.accept(this, result);
            return false;
        }
        value = candidate;
        if (changeListener != null) changeListener.accept(this, result);
        if (dirtyListener != null) dirtyListener.run();
        return true;
    }

    public synchronized void reset() { set(defaultValue); }

    void loadValue(T candidate) {
        value = candidate;
        lastValidation = ConfigValidationResult.ok();
    }

    void attachDirtyListener(Runnable listener) { dirtyListener = listener; }

    public static final class Builder<T> {
        private final String path;
        private final String key;
        private final String title;
        private final T defaultValue;
        private final ConfigCodec<T> codec;
        private String description = "";
        private String constraints = "";
        private final List<ConfigValidator<T>> validators = new ArrayList<>();
        private BiConsumer<ConfigEntry<T>, ConfigValidationResult> listener;

        Builder(String path, String key, String title, T defaultValue, ConfigCodec<T> codec) {
            this.path = path; this.key = key; this.title = title; this.defaultValue = defaultValue; this.codec = codec;
        }

        public Builder<T> description(String value) { description = value == null ? "" : value; return this; }
        public Builder<T> constraints(String value) { constraints = value == null ? "" : value; return this; }
        public Builder<T> validate(ConfigValidator<T> value) { validators.add(Objects.requireNonNull(value)); return this; }
        public Builder<T> onChange(BiConsumer<ConfigEntry<T>, ConfigValidationResult> value) { listener = value; return this; }
        public ConfigEntry<T> build() {
            Objects.requireNonNull(defaultValue, "defaultValue");
            Objects.requireNonNull(codec, "codec");
            for (ConfigValidator<T> validator : validators) {
                ConfigValidationResult result = Objects.requireNonNull(validator.validate(defaultValue), "validator result");
                if (!result.valid()) throw new IllegalArgumentException("Invalid default for " + path + ": " + result.message());
            }
            return new ConfigEntry<>(path, key, title, description, constraints, defaultValue, codec, validators, listener);
        }
    }
}
