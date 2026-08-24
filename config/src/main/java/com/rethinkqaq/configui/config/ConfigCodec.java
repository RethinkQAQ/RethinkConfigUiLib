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

public interface ConfigCodec<T> {
    Object encode(T value);

    T decode(Object value) throws ConfigCodecException;

    @FunctionalInterface
    interface Decoder<T> {
        T decode(Object value) throws ConfigCodecException;
    }

    static <T> ConfigCodec<T> of(java.util.function.Function<T, Object> encoder, Decoder<T> decoder) {
        return new ConfigCodec<>() {
            @Override public Object encode(T value) { return encoder.apply(value); }
            @Override public T decode(Object value) throws ConfigCodecException { return decoder.decode(value); }
        };
    }
}
