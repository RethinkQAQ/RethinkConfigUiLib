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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class YamlConfigStoreTest {
    @Test
    void createsAndSavesMissingFile() throws Exception {
        Path file = Files.createTempDirectory("rcui-config").resolve("demo.yaml");
        Holder holder = new Holder();
        ConfigSpec spec = spec(holder);
        try (YamlConfigStore store = YamlConfigStore.open(spec, file)) {
            store.load();
            assertTrue(store.isDirty());
            store.flush();
        }
        assertTrue(Files.exists(file));
        assertTrue(Files.readString(file).contains("general:"));
    }

    @Test
    void roundTripsValuesAndUnknownFields() throws Exception {
        Path file = Files.createTempDirectory("rcui-config").resolve("demo.yaml");
        Files.writeString(file, "schemaVersion: 0\ngeneral:\n  enabled: true\n  scale: 150\n  unknown: keep\n");
        Holder holder = new Holder();
        ConfigSpec spec = spec(holder);
        try (YamlConfigStore store = YamlConfigStore.open(spec, file)) {
            store.load();
            assertTrue(holder.enabled);
            assertEquals(150, holder.scale);
            holder.scaleValue.set(175);
            store.flush();
        }
        String output = Files.readString(file);
        assertTrue(output.contains("unknown: keep"));
        assertTrue(output.contains("scale: 175"));
    }

    @Test
    void rejectsInvalidValuesAndUsesDefaults() throws Exception {
        Path file = Files.createTempDirectory("rcui-config").resolve("demo.yaml");
        Files.writeString(file, "general:\n  scale: 151\n");
        Holder holder = new Holder();
        ConfigSpec spec = spec(holder);
        try (YamlConfigStore store = YamlConfigStore.open(spec, file)) {
            store.load();
            assertEquals(100, holder.scale);
            assertTrue(store.isDirty());
        }
    }

    @Test
    void backsUpBrokenYaml() throws Exception {
        Path directory = Files.createTempDirectory("rcui-config");
        Path file = directory.resolve("demo.yaml");
        Files.writeString(file, "[broken");
        try (YamlConfigStore store = YamlConfigStore.open(spec(new Holder()), file)) {
            store.load();
        }
        try (var files = Files.list(directory)) {
            assertTrue(files.anyMatch(path -> path.getFileName().toString().contains(".broken-")));
        }
    }

    private static ConfigSpec spec(Holder holder) {
        ConfigValue<Boolean> enabled = ConfigValue.generated("general.enabled", "general", "enabled", "Enabled", "", "", true,
            ConfigCodecs.BOOLEAN, List.of(), () -> holder.enabled, value -> holder.enabled = value);
        ConfigValue<Integer> scale = ConfigValue.generated("general.scale", "general", "scale", "Scale", "", "range 25..200, step 25", 100,
            ConfigCodecs.INTEGER, List.of(ConfigValidators.numeric(25, 200, 25)), () -> holder.scale, value -> holder.scale = value);
        holder.scaleValue = scale;
        return ConfigSpec.generated("demo", 0,
            List.of(new ConfigSpec.Section("general", "General", "")), List.of(enabled, scale), List.of());
    }

    private static final class Holder {
        private boolean enabled;
        private int scale = 100;
        private ConfigValue<Integer> scaleValue;
    }
}
