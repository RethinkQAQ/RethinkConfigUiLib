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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ConfigSpec {
    private final String id;
    private final int schemaVersion;
    private final LinkedHashMap<String, Section> sections;
    private final LinkedHashMap<String, ConfigEntry<?>> entries;
    private final LinkedHashMap<Integer, ConfigMigration> migrations;

    private ConfigSpec(Builder builder) {
        id = builder.id;
        schemaVersion = builder.schemaVersion;
        sections = new LinkedHashMap<>(builder.sections);
        entries = new LinkedHashMap<>(builder.entries);
        migrations = new LinkedHashMap<>(builder.migrations);
    }

    public String id() { return id; }
    public int schemaVersion() { return schemaVersion; }
    public Map<String, Section> sections() { return Collections.unmodifiableMap(sections); }
    public Map<String, ConfigEntry<?>> entries() { return Collections.unmodifiableMap(entries); }
    public ConfigEntry<?> entry(String path) { return entries.get(path); }
    public Map<Integer, ConfigMigration> migrations() { return Collections.unmodifiableMap(migrations); }

    public static Builder builder(String id) { return new Builder(id); }

    public record Section(String id, String title, String description) { }

    public static final class Builder {
        private final String id;
        private int schemaVersion;
        private final LinkedHashMap<String, Section> sections = new LinkedHashMap<>();
        private final LinkedHashMap<String, ConfigEntry<?>> entries = new LinkedHashMap<>();
        private final LinkedHashMap<Integer, ConfigMigration> migrations = new LinkedHashMap<>();

        private Builder(String id) {
            requireId(id, "config id");
            this.id = id;
        }

        public Builder schemaVersion(int value) {
            if (value < 0) throw new IllegalArgumentException("schemaVersion must be non-negative");
            schemaVersion = value;
            return this;
        }

        public SectionBuilder section(String sectionId, String title) {
            requireId(sectionId, "section id");
            if (sections.containsKey(sectionId)) throw new IllegalArgumentException("Duplicate section: " + sectionId);
            sections.put(sectionId, new Section(sectionId, Objects.requireNonNullElse(title, sectionId), ""));
            return new SectionBuilder(this, sectionId);
        }

        public Builder section(String sectionId, String title, java.util.function.Consumer<SectionBuilder> consumer) {
            SectionBuilder section = section(sectionId, title);
            consumer.accept(section);
            return this;
        }

        public <T> Builder value(String path, String title, T defaultValue, ConfigCodec<T> codec) {
            addEntry(path, title, defaultValue, codec, entry -> entry);
            return this;
        }

        public Builder booleanValue(String path, boolean defaultValue) { return value(path, path, defaultValue, ConfigCodecs.BOOLEAN); }
        public Builder integerValue(String path, int defaultValue) { return value(path, path, defaultValue, ConfigCodecs.INTEGER); }
        public Builder longValue(String path, long defaultValue) { return value(path, path, defaultValue, ConfigCodecs.LONG); }
        public Builder floatValue(String path, float defaultValue) { return value(path, path, defaultValue, ConfigCodecs.FLOAT); }
        public Builder doubleValue(String path, double defaultValue) { return value(path, path, defaultValue, ConfigCodecs.DOUBLE); }
        public Builder stringValue(String path, String defaultValue) { return value(path, path, defaultValue, ConfigCodecs.STRING); }
        public <E extends Enum<E>> Builder enumValue(String path, E defaultValue, Class<E> type) {
            return value(path, path, defaultValue, ConfigCodecs.enumCodec(type));
        }
        public <T> Builder listValue(String path, List<T> defaultValue, ConfigCodec<T> elementCodec) {
            return value(path, path, List.copyOf(defaultValue), ConfigCodecs.listOf(elementCodec));
        }

        public Builder migration(ConfigMigration migration) {
            Objects.requireNonNull(migration, "migration");
            if (migration.toVersion() != migration.fromVersion() + 1) {
                throw new IllegalArgumentException("Migrations must advance exactly one schema version");
            }
            if (migrations.putIfAbsent(migration.fromVersion(), migration) != null) {
                throw new IllegalArgumentException("Duplicate migration from version " + migration.fromVersion());
            }
            return this;
        }

        <T> ConfigEntry<T> addEntry(String path, String title, T defaultValue, ConfigCodec<T> codec,
                                    java.util.function.Function<ConfigEntry.Builder<T>, ConfigEntry.Builder<T>> customizer) {
            requirePath(path);
            if (entries.containsKey(path)) throw new IllegalArgumentException("Duplicate config entry: " + path);
            String key = path.substring(path.lastIndexOf('.') + 1);
            ConfigEntry.Builder<T> builder = new ConfigEntry.Builder<>(path, key, Objects.requireNonNullElse(title, key), defaultValue, codec);
            ConfigEntry<T> entry = customizer.apply(builder).build();
            entries.put(path, entry);
            return entry;
        }

        public ConfigSpec build() {
            if (schemaVersion > 0) {
                for (int version = 0; version < schemaVersion; version++) {
                    if (!migrations.containsKey(version)) throw new IllegalArgumentException("Missing migration from version " + version);
                }
            }
            return new ConfigSpec(this);
        }
    }

    public static final class SectionBuilder {
        private final Builder parent;
        private final String sectionId;

        private SectionBuilder(Builder parent, String sectionId) { this.parent = parent; this.sectionId = sectionId; }
        private String path(String key) { return sectionId + "." + key; }

        public SectionBuilder description(String description) {
            Section old = parent.sections.get(sectionId);
            parent.sections.put(sectionId, new Section(sectionId, old.title(), Objects.requireNonNullElse(description, "")));
            return this;
        }

        public <T> SectionBuilder value(String key, String title, T defaultValue, ConfigCodec<T> codec) {
            parent.addEntry(path(key), title, defaultValue, codec, entry -> entry);
            return this;
        }
        public <T> SectionBuilder value(String key, String title, T defaultValue, ConfigCodec<T> codec,
                                        java.util.function.Consumer<ConfigEntry.Builder<T>> configure) {
            parent.addEntry(path(key), title, defaultValue, codec, entry -> {
                configure.accept(entry);
                return entry;
            });
            return this;
        }
        public SectionBuilder booleanValue(String key, boolean defaultValue) { return value(key, key, defaultValue, ConfigCodecs.BOOLEAN); }
        public SectionBuilder integerValue(String key, int defaultValue) { return value(key, key, defaultValue, ConfigCodecs.INTEGER); }
        public SectionBuilder integerValue(String key, int defaultValue, int minimum, int maximum, int step) {
            validateRange(key, minimum, maximum, step);
            return value(key, key, defaultValue, ConfigCodecs.INTEGER, entry -> entry.validate(value -> {
                if (value < minimum || value > maximum) return ConfigValidationResult.error("Value must be between " + minimum + " and " + maximum);
                if ((value - minimum) % step != 0) return ConfigValidationResult.error("Value must use a step of " + step);
                return ConfigValidationResult.ok();
            }).constraints("range " + minimum + ".." + maximum + ", step " + step));
        }
        public SectionBuilder longValue(String key, long defaultValue) { return value(key, key, defaultValue, ConfigCodecs.LONG); }
        public SectionBuilder longValue(String key, long defaultValue, long minimum, long maximum, long step) {
            if (step <= 0 || minimum > maximum) throw new IllegalArgumentException("Invalid long range or step");
            return value(key, key, defaultValue, ConfigCodecs.LONG, entry -> entry.validate(value -> {
                if (value < minimum || value > maximum) return ConfigValidationResult.error("Value must be between " + minimum + " and " + maximum);
                if ((value - minimum) % step != 0) return ConfigValidationResult.error("Value must use a step of " + step);
                return ConfigValidationResult.ok();
            }).constraints("range " + minimum + ".." + maximum + ", step " + step));
        }
        public SectionBuilder floatValue(String key, float defaultValue) { return value(key, key, defaultValue, ConfigCodecs.FLOAT); }
        public SectionBuilder floatValue(String key, float defaultValue, float minimum, float maximum, float step) {
            validateRange(key, minimum, maximum, step);
            return value(key, key, defaultValue, ConfigCodecs.FLOAT, entry -> entry.validate(value -> {
                if (!Float.isFinite(value) || value < minimum || value > maximum) return ConfigValidationResult.error("Value must be between " + minimum + " and " + maximum);
                return ConfigValidationResult.ok();
            }).constraints("range " + minimum + ".." + maximum + ", step " + step));
        }
        public SectionBuilder doubleValue(String key, double defaultValue) { return value(key, key, defaultValue, ConfigCodecs.DOUBLE); }
        public SectionBuilder doubleValue(String key, double defaultValue, double minimum, double maximum, double step) {
            validateRange(key, minimum, maximum, step);
            return value(key, key, defaultValue, ConfigCodecs.DOUBLE, entry -> entry.validate(value -> {
                if (!Double.isFinite(value) || value < minimum || value > maximum) return ConfigValidationResult.error("Value must be between " + minimum + " and " + maximum);
                return ConfigValidationResult.ok();
            }).constraints("range " + minimum + ".." + maximum + ", step " + step));
        }
        public SectionBuilder stringValue(String key, String defaultValue) { return value(key, key, defaultValue, ConfigCodecs.STRING); }
        public <E extends Enum<E>> SectionBuilder enumValue(String key, E defaultValue, Class<E> type) { return value(key, key, defaultValue, ConfigCodecs.enumCodec(type)); }
        public <T> SectionBuilder listValue(String key, List<T> defaultValue, ConfigCodec<T> elementCodec) { return value(key, key, List.copyOf(defaultValue), ConfigCodecs.listOf(elementCodec)); }

        public Builder endSection() { return parent; }
    }

    private static void requirePath(String path) {
        if (path == null || !path.matches("[A-Za-z0-9_-]+(?:\\.[A-Za-z0-9_-]+)*")) {
            throw new IllegalArgumentException("Invalid config path: " + path);
        }
    }

    private static void validateRange(String key, double minimum, double maximum, double step) {
        if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || !Double.isFinite(step) || minimum > maximum || step <= 0) {
            throw new IllegalArgumentException("Invalid range or step for " + key);
        }
    }

    private static void requireId(String id, String label) {
        if (id == null || !id.matches("[A-Za-z0-9_-]+")) throw new IllegalArgumentException("Invalid " + label + ": " + id);
    }
}
