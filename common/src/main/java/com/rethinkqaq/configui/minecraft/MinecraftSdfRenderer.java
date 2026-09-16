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

import com.rethinkqaq.configui.core.UiBounds;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}

/** Stable, version-independent entry point for native SDF drawing. */
final class MinecraftSdfRenderer {
    // The renderer uses GuiGraphicsExtractor starting with 26.1; older nodes use GuiGraphics.
    //? if >=26.1 {
    /*private static final SdfBackend<GuiGraphicsExtractor> BACKEND = backend();
    *///?} else {
    private static final SdfBackend<GuiGraphics> BACKEND = backend();
    //?}

    private MinecraftSdfRenderer() { }

    /** Initializes the selected backend when the opt-in demo is enabled. */
    static void prewarm() {
        BACKEND.prewarm();
    }

    //? if >=26.1 {
    /*private static SdfBackend<GuiGraphicsExtractor> backend() {
        //? if >=26.3 {
        return new RenderPearlSdfBackend();
        //?} else {
        return new ExtractorPipelineSdfBackend();
        //?}
    }
    static boolean fill(GuiGraphicsExtractor graphics, UiBounds box, float radius, int color, float coordinateScale) {
        return BACKEND.draw(graphics, box, radius, 0f, color, false, coordinateScale);
    }
    static boolean stroke(GuiGraphicsExtractor graphics, UiBounds box, float radius, float width, int color, float coordinateScale) {
        return BACKEND.draw(graphics, box, radius, width, color, true, coordinateScale);
    }
    *///?} else {
    private static SdfBackend<GuiGraphics> backend() {
        //? if >=1.21.6 {
        /*return new GuiGraphicsPipelineSdfBackend();
        *///?} else if >=1.21.5 {
        /*return new RenderTypeSdfBackend();
        *///?} else if >=1.21.3 {
        /*return new ShaderProgramSdfBackend();
        *///?} else {
        return new ShaderInstanceSdfBackend();
        //?}
    }
    static boolean fill(GuiGraphics graphics, UiBounds box, float radius, int color, float coordinateScale) {
        return BACKEND.draw(graphics, box, radius, 0f, color, false, coordinateScale);
    }
    static boolean stroke(GuiGraphics graphics, UiBounds box, float radius, float width, int color, float coordinateScale) {
        return BACKEND.draw(graphics, box, radius, width, color, true, coordinateScale);
    }
    //?}
}
