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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlConfigStoreTest {
    @TempDir Path temp;

    @Test
    void writesAndLoadsTypedValuesAndKeepsUnknownNodes() throws Exception {
        Path file = temp.resolve("demo.yaml");
        Files.writeString(file, "schemaVersion: 0\ngeneral:\n  enabled: false\n  unknown: keep\n", StandardCharsets.UTF_8);
        ConfigSpec spec = spec();
        try (YamlConfigStore store = YamlConfigStore.open(spec, file)) {
            store.load();
            assertFalse((Boolean) entry(spec, "general.enabled").get());
            assertTrue(entry(spec, "general.enabled").set(true));
            store.flush();
        }
        String text = Files.readString(file);
        assertTrue(text.contains("unknown: keep"));
        assertTrue(text.contains("enabled: true"));
        assertTrue(text.contains("range 0..10, step 5"));

        try (YamlConfigStore store = YamlConfigStore.open(spec, file)) {
            store.load();
            assertTrue((Boolean) entry(spec, "general.enabled").get());
        }
    }

    @Test
    void rejectsInvalidValuesAndSupportsLists() throws Exception {
        ConfigSpec spec = spec();
        ConfigEntry<Integer> scale = entry(spec, "general.scale");
        assertFalse(scale.set(11));
        assertEquals(5, scale.get());
        assertTrue(scale.set(10));
        assertEquals(List.of("stone", "dirt"), entry(spec, "filters.blocks").defaultValue());
    }

    @Test
    void migratesSchema() throws Exception {
        Path file = temp.resolve("migration.yaml");
        Files.writeString(file, "schemaVersion: 0\ngeneral:\n  enabled: true\n");
        ConfigSpec spec = ConfigSpec.builder("demo")
            .schemaVersion(1)
            .section("general", "General")
            .booleanValue("enabled", false)
            .endSection()
            .migration(new ConfigMigration() {
                @Override public int fromVersion() { return 0; }
                @Override public int toVersion() { return 1; }
                @Override public Map<String, Object> migrate(Map<String, Object> root) {
                    root.put("migrated", true);
                    return root;
                }
            })
            .build();
        try (YamlConfigStore store = YamlConfigStore.open(spec, file)) {
            store.load();
            store.flush();
        }
        assertTrue(Files.readString(file).contains("schemaVersion: 1"));
        assertTrue(Files.readString(file).contains("migrated: true"));
    }

    @Test
    void malformedFileIsBackedUpAndDefaultsAreUsed() throws Exception {
        Path file = temp.resolve("broken.yaml");
        Files.writeString(file, "general: [");
        ConfigSpec spec = spec();
        try (YamlConfigStore store = YamlConfigStore.open(spec, file)) {
            store.load();
            assertTrue((Boolean) entry(spec, "general.enabled").get());
        }
        assertTrue(Files.list(temp).anyMatch(path -> path.getFileName().toString().contains(".broken-")));
    }

    @Test
    void persistedValuesThatFailValidationUseDefaults() throws Exception {
        Path file = temp.resolve("invalid-value.yaml");
        Files.writeString(file, "schemaVersion: 0\ngeneral:\n  scale: 11\n");
        ConfigSpec spec = spec();
        try (YamlConfigStore store = YamlConfigStore.open(spec, file)) {
            store.load();
            assertEquals(5, entry(spec, "general.scale").get());
            store.flush();
        }
        assertTrue(Files.readString(file).contains("scale: 5"));
    }

    @Test
    void integerCodecRejectsFractionalAndOverflowValues() {
        assertThrows(ConfigCodecException.class, () -> ConfigCodecs.INTEGER.decode(1.5));
        assertThrows(ConfigCodecException.class, () -> ConfigCodecs.INTEGER.decode(Long.MAX_VALUE));
    }

    @Test
    void failedMigrationIsBackedUpSeparately() throws Exception {
        Path file = temp.resolve("unmigrated.yaml");
        Files.writeString(file, "schemaVersion: 0\n");
        ConfigSpec spec = ConfigSpec.builder("demo")
            .schemaVersion(1)
            .section("general", "General")
            .booleanValue("enabled", true)
            .endSection()
            .migration(new ConfigMigration() {
                @Override public int fromVersion() { return 0; }
                @Override public int toVersion() { return 1; }
                @Override public Map<String, Object> migrate(Map<String, Object> root) {
                    throw new IllegalStateException("migration failed");
                }
            })
            .build();
        try (YamlConfigStore store = YamlConfigStore.open(spec, file)) {
            store.load();
        }
        assertTrue(Files.list(temp).anyMatch(path -> path.getFileName().toString().contains(".unmigrated-")));
        assertEquals("schemaVersion: 0\n", Files.readString(file));
    }

    private ConfigSpec spec() {
        return ConfigSpec.builder("demo")
            .schemaVersion(0)
            .section("general", "General")
            .value("enabled", "Enabled", true, ConfigCodecs.BOOLEAN, builder -> builder.description("Enable the feature"))
            .integerValue("scale", 5, 0, 10, 5)
            .endSection()
            .section("filters", "Filters")
            .listValue("blocks", List.of("stone", "dirt"), ConfigCodecs.STRING)
            .endSection()
            .build();
    }

    @SuppressWarnings("unchecked")
    private static <T> ConfigEntry<T> entry(ConfigSpec spec, String path) {
        return (ConfigEntry<T>) spec.entry(path);
    }
}
