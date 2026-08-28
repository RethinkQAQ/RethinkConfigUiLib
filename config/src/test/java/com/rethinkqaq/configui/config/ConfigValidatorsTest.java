/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 *
 * This file is part of Rethink Config UI Lib.
 *
 * Rethink Config UI Lib is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, version 3 of the License.
 */

package com.rethinkqaq.configui.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ConfigValidatorsTest {
    @Test
    void acceptsFloatValuesOnDecimalStepBoundaries() {
        ConfigValidator<Float> validator = ConfigValidators.numeric(.05, 1, .05);
        assertTrue(validator.validate(.05F).valid());
        assertTrue(validator.validate(.1F).valid());
        assertTrue(validator.validate(.15F).valid());
        assertTrue(validator.validate(.5F).valid());
        assertTrue(validator.validate(1F).valid());
        assertFalse(validator.validate(.12F).valid());
    }
}
