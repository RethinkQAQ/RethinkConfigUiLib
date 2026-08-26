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
    private final LinkedHashMap<String, ConfigValue<?>> values;
    private final LinkedHashMap<Integer, ConfigMigration> migrations;

    private ConfigSpec(String id, int schemaVersion, List<Section> sections,
                       List<ConfigValue<?>> values, List<ConfigMigration> migrations) {
        this.id = requireId(id, "config id");
        if (schemaVersion < 0) throw new IllegalArgumentException("schemaVersion must be non-negative");
        this.schemaVersion = schemaVersion;
        this.sections = new LinkedHashMap<>();
        for (Section section : sections) {
            if (this.sections.putIfAbsent(section.id(), section) != null) throw new IllegalArgumentException("Duplicate section: " + section.id());
        }
        this.values = new LinkedHashMap<>();
        for (ConfigValue<?> value : values) {
            if (this.values.putIfAbsent(value.path(), value) != null) throw new IllegalArgumentException("Duplicate config value: " + value.path());
        }
        this.migrations = new LinkedHashMap<>();
        for (ConfigMigration migration : migrations) {
            Objects.requireNonNull(migration, "migration");
            if (migration.toVersion() != migration.fromVersion() + 1) throw new IllegalArgumentException("Migrations must advance exactly one schema version");
            if (this.migrations.putIfAbsent(migration.fromVersion(), migration) != null) throw new IllegalArgumentException("Duplicate migration from version " + migration.fromVersion());
        }
    }

    public static ConfigSpec generated(String id, int schemaVersion, List<Section> sections,
                                       List<ConfigValue<?>> values, List<ConfigMigration> migrations) {
        return new ConfigSpec(id, schemaVersion, sections, values, migrations);
    }

    public String id() { return id; }
    public int schemaVersion() { return schemaVersion; }
    public Map<String, Section> sections() { return Collections.unmodifiableMap(sections); }
    public Map<String, ConfigValue<?>> values() { return Collections.unmodifiableMap(values); }
    public ConfigValue<?> value(String path) { return values.get(path); }
    public Map<Integer, ConfigMigration> migrations() { return Collections.unmodifiableMap(migrations); }

    public record Section(String id, String title, String description) {
        public Section {
            id = requireId(id, "section id");
            title = Objects.requireNonNullElse(title, id);
            description = Objects.requireNonNullElse(description, "");
        }
    }

    private static String requireId(String value, String label) {
        if (value == null || !value.matches("[A-Za-z0-9_-]+")) throw new IllegalArgumentException("Invalid " + label + ": " + value);
        return value;
    }
}
