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

package com.rethinkqaq.configui.core.setting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.Function;

/** Number parsing, bounds, step and formatting rules shared by numeric controls. */
public final class UiNumberSpec<T extends Number> {
    public interface Codec<T extends Number> {
        T parse(String value) throws NumberFormatException;
        double asDouble(T value);
        T fromDouble(double value);
    }
    public static final Codec<Integer> INTEGER = codec(Integer::valueOf, Number::doubleValue, value -> (int) Math.round(value));
    public static final Codec<Long> LONG = codec(Long::valueOf, Number::doubleValue, value -> Math.round(value));
    public static final Codec<Float> FLOAT = codec(Float::valueOf, Number::doubleValue, value -> value.floatValue());
    public static final Codec<Double> DOUBLE = codec(Double::valueOf, Number::doubleValue, value -> value);
    private final Codec<T> codec;
    private final Double minimum, maximum, step;
    private final Function<T, String> formatter;
    private final UiValidator<? super T> validator;
    private UiNumberSpec(Codec<T> codec, Double minimum, Double maximum, Double step,
                         Function<T, String> formatter, UiValidator<? super T> validator) {
        this.codec = Objects.requireNonNull(codec, "codec"); this.minimum = minimum; this.maximum = maximum; this.step = step;
        this.formatter = formatter == null ? value -> String.valueOf(value) : formatter;
        this.validator = validator == null ? UiValidator.acceptAll() : validator;
        if (minimum != null && maximum != null && maximum < minimum) throw new IllegalArgumentException("maximum is below minimum");
        if (step != null && (!Double.isFinite(step) || step <= 0)) throw new IllegalArgumentException("step must be positive and finite");
    }
    public static <T extends Number> Builder<T> builder(Codec<T> codec) { return new Builder<>(codec); }
    public Codec<T> codec() { return codec; }
    public Double minimum() { return minimum; }
    public Double maximum() { return maximum; }
    public Double step() { return step; }
    public boolean hasFiniteRange() { return minimum != null && maximum != null && Double.isFinite(minimum) && Double.isFinite(maximum); }
    public String format(T value) { return formatter.apply(value); }
    public UiValidationResult validate(T value) {
        double number = codec.asDouble(value);
        if (!Double.isFinite(number)) return UiValidationResult.error(com.rethinkqaq.configui.core.UiText.literal("Value must be finite"));
        if (minimum != null && number < minimum) return UiValidationResult.error(com.rethinkqaq.configui.core.UiText.literal("Value is below the minimum"));
        if (maximum != null && number > maximum) return UiValidationResult.error(com.rethinkqaq.configui.core.UiText.literal("Value is above the maximum"));
        return validator.validate(value);
    }
    public T snap(T value) {
        double number = codec.asDouble(value);
        if (step != null) number = (minimum == null ? 0d : minimum) + Math.round((number - (minimum == null ? 0d : minimum)) / step) * step;
        if (minimum != null) number = Math.max(minimum, number);
        if (maximum != null) number = Math.min(maximum, number);
        return codec.fromDouble(number);
    }
    public static final class Builder<T extends Number> {
        private final Codec<T> codec; private Double min, max, step; private Function<T, String> formatter; private UiValidator<? super T> validator;
        private Builder(Codec<T> codec) { this.codec = codec; }
        public Builder<T> range(double value, double upper) { min = value; max = upper; return this; }
        public Builder<T> minimum(double value) { min = value; return this; }
        public Builder<T> maximum(double value) { max = value; return this; }
        public Builder<T> step(double value) { step = value; return this; }
        public Builder<T> formatter(Function<T, String> value) { formatter = value; return this; }
        public Builder<T> validator(UiValidator<? super T> value) { validator = value; return this; }
        public UiNumberSpec<T> build() { return new UiNumberSpec<>(codec, min, max, step, formatter, validator); }
    }
    private static <T extends Number> Codec<T> codec(Function<String, T> parser, Function<T, Double> toDouble, Function<Double, T> fromDouble) {
        return new Codec<>() { public T parse(String value) { return parser.apply(value); } public double asDouble(T value) { return toDouble.apply(value); } public T fromDouble(double value) { return fromDouble.apply(value); } };
    }
}
