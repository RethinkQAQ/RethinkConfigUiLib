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
package com.rethinkqaq.configui.minecraft;

/** Pixel-quantized SDF parameters shared by RenderPipeline API generations. */
record SdfPipelineKey(int radius, int stroke, boolean outline) {
    static SdfPipelineKey from(float radius, float stroke, boolean outline, double guiScale, float coordinateScale) {
        float pixelScale = (float) (guiScale * coordinateScale);
        return new SdfPipelineKey(Math.round(radius * pixelScale), Math.round(stroke * pixelScale), outline);
    }
}
