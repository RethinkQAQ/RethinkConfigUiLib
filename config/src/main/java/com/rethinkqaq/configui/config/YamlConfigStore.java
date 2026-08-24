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
import java.util.ArrayList;
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

public final class YamlConfigStore implements ConfigStore {
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
        this.spec = Objects.requireNonNull(spec);
        this.path = Objects.requireNonNull(path);
        this.diagnostic = diagnostic == null ? message -> { } : diagnostic;
        this.executor = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "rcui-config-save");
            thread.setDaemon(true);
            return thread;
        });
    }

    public static YamlConfigStore open(ConfigSpec spec, Path path) {
        return new YamlConfigStore(spec, path, null);
    }

    public static YamlConfigStore open(ConfigSpec spec, Path path, Consumer<String> diagnostic) {
        return new YamlConfigStore(spec, path, diagnostic);
    }

    @Override
    public ConfigSession load() throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (!Files.exists(path)) {
                raw = new LinkedHashMap<>();
                dirty = true;
                attachListeners();
                scheduleSave();
                return new ConfigSession(spec, path);
            }
            try {
                raw = parse(Files.readString(path, StandardCharsets.UTF_8));
                migrate();
                decodeKnownValues();
                attachListeners();
                if (dirty) scheduleSave();
                return new ConfigSession(spec, path);
            } catch (UnmigratedConfigException exception) {
                backup("unmigrated");
                diagnostic.accept("Could not migrate " + path + ": " + exception.getMessage());
                raw = new LinkedHashMap<>();
                attachListeners();
                dirty = false;
                return new ConfigSession(spec, path);
            } catch (RuntimeException exception) {
                backup("broken");
                diagnostic.accept("Could not read " + path + ": " + exception.getMessage());
                raw = new LinkedHashMap<>();
                attachListeners();
                dirty = true;
                scheduleSave();
                return new ConfigSession(spec, path);
            }
        }
    }

    @Override
    public void save() throws IOException {
        Future<?> write;
        synchronized (lock) {
            ensureOpen();
            write = executor.submit(() -> {
                synchronized (lock) { writeNow(); }
                return null;
            });
        }
        await(write);
    }

    @Override
    public void flush() throws IOException {
        Future<?> pending;
        synchronized (lock) {
            ensureOpen();
            pending = pendingSave;
        }
        if (pending != null) {
            try { pending.get(); }
            catch (Exception exception) {
                if (exception.getCause() instanceof IOException io) throw io;
                throw new IOException("Could not flush config", exception);
            }
        }
        synchronized (lock) {
            if (dirty) {
                pending = executor.submit(() -> {
                    synchronized (lock) {
                        if (dirty) writeNow();
                    }
                    return null;
                });
            }
        }
        if (pending != null && !pending.isDone()) await(pending);
    }

    @Override public boolean isDirty() { return dirty; }

    @Override
    public void close() throws IOException {
        if (closed) return;
        flush();
        closed = true;
        executor.shutdown();
    }

    private void attachListeners() {
        for (ConfigEntry<?> entry : spec.entries().values()) entry.attachDirtyListener(this::markDirty);
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
        for (ConfigEntry<?> entry : spec.entries().values()) putPath(output, entry.path(), encode(entry));
        StringBuilder text = new StringBuilder();
        text.append("# Generated by Rethink Config UI Lib\n");
        for (ConfigSpec.Section section : spec.sections().values()) {
            if (!section.description().isBlank()) text.append("# ").append(section.title()).append(": ").append(section.description()).append('\n');
        }
        for (ConfigEntry<?> entry : spec.entries().values()) {
            text.append("# ").append(entry.path()).append(" = ").append(String.valueOf(entry.defaultValue()));
            if (!entry.description().isBlank()) text.append(" — ").append(entry.description());
            if (!entry.constraints().isBlank()) text.append(" [").append(entry.constraints()).append(']');
            text.append('\n');
        }
        text.append(new Dump(DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK).build()).dumpToString(output));
        Files.createDirectories(path.toAbsolutePath().getParent());
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> parse(String text) {
        Object loaded = new Load(LoadSettings.builder().setLabel(path.toString()).build()).loadFromString(text);
        if (loaded == null) return new LinkedHashMap<>();
        if (!(loaded instanceof Map<?, ?> map)) throw new IllegalArgumentException("Root YAML value must be a map");
        return normalizeMap(map);
    }

    private void migrate() throws UnmigratedConfigException {
        Object schemaValue = raw.get("schemaVersion");
        if (schemaValue != null && !(schemaValue instanceof Number)) {
            throw new UnmigratedConfigException("schemaVersion must be a number");
        }
        boolean hasSchemaVersion = schemaValue instanceof Number;
        int originalVersion = hasSchemaVersion ? ((Number) schemaValue).intValue() : 0;
        if (originalVersion < 0 || originalVersion > spec.schemaVersion()) {
            throw new UnmigratedConfigException("Unsupported schema version " + originalVersion);
        }
        int version = originalVersion;
        while (version < spec.schemaVersion()) {
            ConfigMigration migration = spec.migrations().get(version);
            if (migration == null) {
                throw new UnmigratedConfigException("Missing migration from schema version " + version);
            }
            try {
                Map<String, Object> migrated = migration.migrate(deepCopyMap(raw));
                if (migrated == null) throw new ConfigMigrationException("Migration returned null");
                raw = normalizeMap(migrated);
            }
            catch (Exception exception) {
                throw new UnmigratedConfigException("Migration failed from schema version " + version, exception);
            }
            if (migration.toVersion() != version + 1) {
                throw new UnmigratedConfigException("Migration advanced to " + migration.toVersion() + " instead of " + (version + 1));
            }
            version = migration.toVersion();
        }
        if (!hasSchemaVersion || originalVersion != spec.schemaVersion()) dirty = true;
        raw.put("schemaVersion", spec.schemaVersion());
    }

    private void decodeKnownValues() {
        for (ConfigEntry<?> entry : spec.entries().values()) {
            Object node = getPath(raw, entry.path());
            if (node == null) {
                dirty = true;
                continue;
            }
            try {
                Object decoded = decode(entry, node);
                ConfigValidationResult validation = validate(entry, decoded);
                if (!validation.valid()) {
                    dirty = true;
                    diagnostic.accept("Invalid value for " + entry.path() + ": " + validation.message());
                } else {
                    load(entry, decoded);
                }
            }
            catch (ConfigCodecException exception) {
                dirty = true;
                diagnostic.accept("Invalid value for " + entry.path() + ": " + exception.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Object decode(ConfigEntry<?> entry, Object node) throws ConfigCodecException {
        ConfigEntry<T> typed = (ConfigEntry<T>) entry;
        return typed.codec().decode(node);
    }

    @SuppressWarnings("unchecked")
    private static <T> ConfigValidationResult validate(ConfigEntry<?> entry, Object value) {
        return ((ConfigEntry<T>) entry).validate((T) value);
    }

    @SuppressWarnings("unchecked")
    private static <T> void load(ConfigEntry<?> entry, Object value) {
        ((ConfigEntry<T>) entry).loadValue((T) value);
    }

    private void backup(String suffix) {
        try {
            if (Files.exists(path)) Files.copy(path, path.resolveSibling(path.getFileName() + "." + suffix + "-" + Instant.now().toEpochMilli()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) { diagnostic.accept("Could not back up " + path + ": " + exception.getMessage()); }
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("Config store is closed");
    }

    private static Object getPath(Map<String, Object> root, String path) {
        Object current = root;
        for (String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) return null;
            current = map.get(part);
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private static void putPath(Map<String, Object> root, String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            Object child = current.get(parts[i]);
            if (!(child instanceof Map<?, ?>)) {
                child = new LinkedHashMap<String, Object>();
                current.put(parts[i], child);
            }
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

    @SuppressWarnings("unchecked")
    private static Object encode(ConfigEntry<?> entry) {
        ConfigEntry<Object> typed = (ConfigEntry<Object>) entry;
        return typed.codec().encode(typed.get());
    }
}
