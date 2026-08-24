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

public final class ConfigValidators {
    private ConfigValidators() { }

    public static <T extends Number> ConfigValidator<T> numeric(double minimum, double maximum, double step) {
        if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || minimum > maximum || step < 0.0 || !Double.isFinite(step)) {
            throw new IllegalArgumentException("Invalid numeric range or step");
        }
        return value -> {
            if (value == null || !Double.isFinite(value.doubleValue())) return ConfigValidationResult.error("Value must be finite");
            double number = value.doubleValue();
            if (number < minimum || number > maximum) return ConfigValidationResult.error("Value must be between " + minimum + " and " + maximum);
            if (step > 0.0) {
                double snapped = minimum + Math.rint((number - minimum) / step) * step;
                double tolerance = Math.max(1.0E-9, Math.abs(step) * 1.0E-9);
                if (Math.abs(number - snapped) > tolerance) return ConfigValidationResult.error("Value must use a step of " + step);
            }
            return ConfigValidationResult.ok();
        };
    }
}
