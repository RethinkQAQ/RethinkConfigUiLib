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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

public final class ConfigCodecs {
    private ConfigCodecs() { }

    public static final ConfigCodec<Boolean> BOOLEAN = ConfigCodec.of(
        value -> value,
        value -> value instanceof Boolean b ? b : throwType("boolean", value)
    );
    public static final ConfigCodec<Integer> INTEGER = ConfigCodec.of(
        value -> value,
        value -> exactInteger(value, Integer.class).intValue()
    );
    public static final ConfigCodec<Long> LONG = ConfigCodec.of(
        value -> value,
        value -> exactInteger(value, Long.class).longValue()
    );
    public static final ConfigCodec<Float> FLOAT = ConfigCodec.of(
        value -> value,
        value -> floating(value, Float.class).floatValue()
    );
    public static final ConfigCodec<Double> DOUBLE = ConfigCodec.of(
        value -> value,
        value -> floating(value, Double.class).doubleValue()
    );
    public static final ConfigCodec<String> STRING = ConfigCodec.of(
        value -> value,
        value -> value instanceof String s ? s : throwType("string", value)
    );

    public static <E extends Enum<E>> ConfigCodec<E> enumCodec(Class<E> type) {
        return ConfigCodec.of(
            Enum::name,
            value -> {
                if (!(value instanceof String text)) throw new ConfigCodecException("Expected enum string");
                try { return Enum.valueOf(type, text); }
                catch (IllegalArgumentException exception) {
                    throw new ConfigCodecException("Unknown " + type.getSimpleName() + " value: " + text, exception);
                }
            }
        );
    }

    public static <T> ConfigCodec<List<T>> listOf(ConfigCodec<T> elementCodec) {
        return ConfigCodec.of(
            value -> {
                List<Object> encoded = new ArrayList<>(value.size());
                value.forEach(item -> encoded.add(elementCodec.encode(item)));
                return encoded;
            },
            value -> {
                if (!(value instanceof List<?> list)) throw new ConfigCodecException("Expected YAML list");
                List<T> decoded = new ArrayList<>(list.size());
                for (Object item : list) decoded.add(elementCodec.decode(item));
                return List.copyOf(decoded);
            }
        );
    }

    private static Number number(Object value, Class<?> target) throws ConfigCodecException {
        if (!(value instanceof Number number)) throw new ConfigCodecException("Expected " + target.getSimpleName());
        return number;
    }

    private static Number floating(Object value, Class<?> target) throws ConfigCodecException {
        Number number = number(value, target);
        double source = number.doubleValue();
        double converted = target == Float.class ? number.floatValue() : source;
        if (!Double.isFinite(source) || !Double.isFinite(converted)) {
            throw new ConfigCodecException("Non-finite or out-of-range number is not supported");
        }
        return number;
    }

    private static BigInteger exactInteger(Object value, Class<?> target) throws ConfigCodecException {
        if (!(value instanceof Number number)) throw new ConfigCodecException("Expected " + target.getSimpleName());
        try {
            BigInteger integer = new BigDecimal(number.toString()).toBigIntegerExact();
            if (target == Integer.class) return BigInteger.valueOf(integer.intValueExact());
            return BigInteger.valueOf(integer.longValueExact());
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new ConfigCodecException("Expected an exact " + target.getSimpleName().toLowerCase(Locale.ROOT), exception);
        }
    }

    private static <T> T throwType(String expected, Object value) throws ConfigCodecException {
        throw new ConfigCodecException("Expected " + expected + ", got " + (value == null ? "null" : value.getClass().getSimpleName()).toLowerCase(Locale.ROOT));
    }
}
