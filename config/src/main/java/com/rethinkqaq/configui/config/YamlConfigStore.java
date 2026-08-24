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

import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class YamlConfigStore implements AutoCloseable {
    private static final long SAVE_DELAY_MILLIS = 150;
    private final ConfigSpec spec;
    private final Path path;
    private final ScheduledExecutorService executor;
    private final Consumer<String> diagnostic;
    private final Object lock = new Object();
    private Map<String, Object> raw = new LinkedHashMap<>();
    private ScheduledFuture<?> pendingSave;
    private volatile boolean dirty;
    private volatile boolean closed;

    private YamlConfigStore(ConfigSpec spec, Path path, Consumer<String> diagnostic) {
        this.spec = Objects.requireNonNull(spec, "spec");
        this.path = Objects.requireNonNull(path, "path");
        this.diagnostic = diagnostic == null ? message -> { } : diagnostic;
        this.executor = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "rcui-config-save");
            thread.setDaemon(true);
            return thread;
        });
    }

    public static YamlConfigStore open(ConfigSpec spec, Path path) { return new YamlConfigStore(spec, path, null); }
    public static YamlConfigStore open(ConfigSpec spec, Path path, Consumer<String> diagnostic) { return new YamlConfigStore(spec, path, diagnostic); }

    public void load() throws IOException {
        synchronized (lock) {
            ensureOpen();
            attachListeners();
            resetValuesToDefaults();
            dirty = false;
            if (!Files.exists(path)) {
                raw = new LinkedHashMap<>();
                dirty = true;
                scheduleSave();
                return;
            }
            try {
                raw = parse(Files.readString(path, StandardCharsets.UTF_8));
                migrate();
                decodeKnownValues();
                if (dirty) scheduleSave();
            } catch (UnmigratedConfigException exception) {
                backup("unmigrated");
                diagnostic.accept("Could not migrate " + path + ": " + exception.getMessage());
                raw = new LinkedHashMap<>();
                dirty = false;
            } catch (RuntimeException exception) {
                backup("broken");
                diagnostic.accept("Could not read " + path + ": " + exception.getMessage());
                raw = new LinkedHashMap<>();
                dirty = true;
                scheduleSave();
            }
        }
    }

    public void save() throws IOException {
        Future<?> write;
        synchronized (lock) {
            ensureOpen();
            write = executor.submit(() -> {
                synchronized (lock) { if (dirty) writeNow(); }
                return null;
            });
        }
        await(write);
    }

    public void flush() throws IOException {
        Future<?> pending;
        synchronized (lock) {
            ensureOpen();
            pending = pendingSave;
        }
        if (pending != null) await(pending);
        synchronized (lock) {
            if (dirty) pending = executor.submit(() -> {
                synchronized (lock) { if (dirty) writeNow(); }
                return null;
            });
            else pending = null;
        }
        if (pending != null) await(pending);
    }

    public boolean isDirty() { return dirty; }
    public Path path() { return path; }

    @Override
    public void close() throws IOException {
        if (closed) return;
        flush();
        synchronized (lock) {
            closed = true;
            if (pendingSave != null) pendingSave.cancel(false);
        }
        executor.shutdown();
    }

    private void attachListeners() {
        for (ConfigValue<?> value : spec.values().values()) value.attachDirtyListener(this::markDirty);
    }

    private void resetValuesToDefaults() {
        for (ConfigValue<?> value : spec.values().values()) resetValue(value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void resetValue(ConfigValue value) {
        value.loadValue(value.defaultValue());
    }

    private void markDirty() {
        synchronized (lock) {
            if (!closed) {
                dirty = true;
                scheduleSave();
            }
        }
    }

    private void scheduleSave() {
        if (pendingSave != null) pendingSave.cancel(false);
        pendingSave = executor.schedule(() -> {
            synchronized (lock) {
                if (!closed && dirty) {
                    try { writeNow(); }
                    catch (IOException exception) { diagnostic.accept("Could not save " + path + ": " + exception.getMessage()); }
                }
            }
        }, SAVE_DELAY_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void writeNow() throws IOException {
        Map<String, Object> output = deepCopyMap(raw);
        output.put("schemaVersion", spec.schemaVersion());
        for (ConfigValue<?> value : spec.values().values()) putPath(output, value.path(), encode(value));
        StringBuilder text = new StringBuilder("# Generated by Rethink Config UI Lib\n");
        for (ConfigValue<?> value : spec.values().values()) {
            text.append("# ").append(value.path()).append(" = ").append(String.valueOf(value.defaultValue()));
            if (!value.description().isBlank()) text.append(" — ").append(value.description());
            if (!value.constraints().isBlank()) text.append(" [").append(value.constraints()).append(']');
            text.append('\n');
        }
        text.append(new Dump(DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK).build()).dumpToString(output));
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(temporary, text, StandardCharsets.UTF_8);
        try { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException exception) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
        raw = output;
        dirty = false;
        pendingSave = null;
    }

    private static void await(Future<?> future) throws IOException {
        try { future.get(); }
        catch (Exception exception) {
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            if (cause instanceof IOException io) throw io;
            throw new IOException("Could not save config", cause);
        }
    }

    private Map<String, Object> parse(String text) {
        Object loaded = new Load(LoadSettings.builder().setLabel(path.toString()).build()).loadFromString(text);
        if (loaded == null) return new LinkedHashMap<>();
        if (!(loaded instanceof Map<?, ?> map)) throw new IllegalArgumentException("Root YAML value must be a map");
        return normalizeMap(map);
    }

    private void migrate() throws UnmigratedConfigException {
        Object schemaValue = raw.get("schemaVersion");
        if (schemaValue != null && !(schemaValue instanceof Number)) throw new UnmigratedConfigException("schemaVersion must be a number");
        int originalVersion = schemaValue instanceof Number number ? number.intValue() : 0;
        if (originalVersion < 0 || originalVersion > spec.schemaVersion()) throw new UnmigratedConfigException("Unsupported schema version " + originalVersion);
        int version = originalVersion;
        while (version < spec.schemaVersion()) {
            ConfigMigration migration = spec.migrations().get(version);
            if (migration == null) throw new UnmigratedConfigException("Missing migration from schema version " + version);
            try {
                Map<String, Object> migrated = migration.migrate(deepCopyMap(raw));
                if (migrated == null) throw new ConfigMigrationException("Migration returned null");
                raw = normalizeMap(migrated);
            } catch (Exception exception) {
                throw new UnmigratedConfigException("Migration failed from schema version " + version, exception);
            }
            version = migration.toVersion();
        }
        if (schemaValue == null || originalVersion != spec.schemaVersion()) dirty = true;
        raw.put("schemaVersion", spec.schemaVersion());
    }

    private void decodeKnownValues() {
        for (ConfigValue<?> value : spec.values().values()) {
            Object node = getPath(raw, value.path());
            if (node == null) { dirty = true; continue; }
            try {
                Object decoded = decode(value, node);
                ConfigValidationResult validation = validate(value, decoded);
                if (!validation.valid()) {
                    dirty = true;
                    diagnostic.accept("Invalid value for " + value.path() + ": " + validation.message());
                } else load(value, decoded);
            } catch (ConfigCodecException exception) {
                dirty = true;
                diagnostic.accept("Invalid value for " + value.path() + ": " + exception.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked") private static <T> Object decode(ConfigValue<?> value, Object node) throws ConfigCodecException { return ((ConfigValue<T>) value).codec().decode(node); }
    @SuppressWarnings("unchecked") private static <T> ConfigValidationResult validate(ConfigValue<?> value, Object decoded) { return ((ConfigValue<T>) value).validate((T) decoded); }
    @SuppressWarnings("unchecked") private static <T> void load(ConfigValue<?> value, Object decoded) { ((ConfigValue<T>) value).loadValue((T) decoded); }
    @SuppressWarnings("unchecked") private static Object encode(ConfigValue<?> value) { return ((ConfigValue<Object>) value).codec().encode(((ConfigValue<Object>) value).get()); }

    private void backup(String suffix) {
        try {
            if (Files.exists(path)) Files.copy(path, path.resolveSibling(path.getFileName() + "." + suffix + "-" + Instant.now().toEpochMilli()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) { diagnostic.accept("Could not back up " + path + ": " + exception.getMessage()); }
    }

    private void ensureOpen() { if (closed) throw new IllegalStateException("Config store is closed"); }
    private static Object getPath(Map<String, Object> root, String path) {
        Object current = root;
        for (String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) return null;
            current = map.get(part);
        }
        return current;
    }
    @SuppressWarnings("unchecked") private static void putPath(Map<String, Object> root, String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            Object child = current.get(parts[i]);
            if (!(child instanceof Map<?, ?>)) { child = new LinkedHashMap<String, Object>(); current.put(parts[i], child); }
            current = (Map<String, Object>) child;
        }
        current.put(parts[parts.length - 1], value);
    }
    private static Map<String, Object> normalizeMap(Map<?, ?> source) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(String.valueOf(key), normalize(value)));
        return result;
    }
    private static Object normalize(Object value) {
        if (value instanceof Map<?, ?> map) return normalizeMap(map);
        if (value instanceof List<?> list) return list.stream().map(YamlConfigStore::normalize).toList();
        return value;
    }
    private static Map<String, Object> deepCopyMap(Map<String, Object> source) { return normalizeMap(source); }
    private static final class UnmigratedConfigException extends Exception {
        private UnmigratedConfigException(String message) { super(message); }
        private UnmigratedConfigException(String message, Throwable cause) { super(message, cause); }
    }
}
