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

import java.math.BigDecimal;
import java.math.RoundingMode;

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
                // Numeric settings are commonly floats. Comparing their binary double
                // expansion to a decimal step rejects valid values such as 0.10 for a
                // 0.05 step. Use the value's printable decimal representation instead.
                BigDecimal candidate = new BigDecimal(value.toString());
                BigDecimal base = BigDecimal.valueOf(minimum);
                BigDecimal increment = BigDecimal.valueOf(step);
                BigDecimal steps = candidate.subtract(base).divide(increment, 0, RoundingMode.HALF_UP);
                BigDecimal snapped = base.add(increment.multiply(steps));
                if (candidate.compareTo(snapped) != 0) return ConfigValidationResult.error("Value must use a step of " + step);
            }
            return ConfigValidationResult.ok();
        };
    }
}
