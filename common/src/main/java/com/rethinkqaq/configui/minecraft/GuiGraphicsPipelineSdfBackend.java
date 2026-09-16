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

//? if >=26.1 {
/*final class GuiGraphicsPipelineSdfBackend { }
*///?} else {
//? if >=1.21.6 {
/*import com.rethinkqaq.configui.RethinkConfigUiLib;
import com.rethinkqaq.configui.core.UiBounds;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import java.util.HashMap;
import java.util.Map;
//? if <1.21.11 {
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.platform.DepthTestFunction;
//?} else {
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.DepthTestFunction;
//?}

// RenderPipeline implementation for the GuiGraphics API generation (1.21.6–1.21.11).
final class GuiGraphicsPipelineSdfBackend implements SdfBackend<GuiGraphics> {
    private static final Map<SdfPipelineKey, RenderPipeline> PIPELINES = new HashMap<>();
    private static boolean warned;

    @Override public boolean draw(GuiGraphics graphics, UiBounds box, float radius, float stroke, int color,
                                  boolean outline, float coordinateScale) {
        if (box.width() <= 0 || box.height() <= 0) return true;
        try {
            double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
            SdfPipelineKey key = SdfPipelineKey.from(radius, stroke, outline, guiScale, coordinateScale);
            graphics.fill(PIPELINES.computeIfAbsent(key, GuiGraphicsPipelineSdfBackend::pipeline),
                Math.round(box.x()), Math.round(box.y()), Math.round(box.x() + box.width()), Math.round(box.y() + box.height()), color);
            return true;
        } catch (RuntimeException exception) {
            warn(exception);
            return false;
        }
    }

    private static RenderPipeline pipeline(SdfPipelineKey key) {
        int radius = Math.max(0, Math.min(2048, key.radius()));
        int stroke = Math.max(0, Math.min(512, key.stroke()));
        // Identifier replaced ResourceLocation in 1.21.11; this is a compile-time-only API split.
        //? if <1.21.11 {
        var base = ResourceLocation.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "core/rcui_sdf_pipeline");
        var location = ResourceLocation.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID,
            "pipeline/rcui_sdf_" + radius + "_" + stroke + "_" + (key.outline() ? "stroke" : "fill"));
        //?} else {
        var base = Identifier.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "core/rcui_sdf_pipeline");
        var location = Identifier.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID,
            "pipeline/rcui_sdf_" + radius + "_" + stroke + "_" + (key.outline() ? "stroke" : "fill"));
        //?}
        return RenderPipeline.builder()
            .withLocation(location)
            .withVertexShader(base)
            .withFragmentShader(base)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withShaderDefine("RCUI_RADIUS", radius)
            .withShaderDefine("RCUI_STROKE", stroke)
            .withShaderDefine("RCUI_STROKE_MODE", key.outline() ? 1 : 0)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .build();
    }

    private static void warn(Exception exception) {
        if (warned) return;
        warned = true;
        RethinkConfigUiLib.LOGGER.warn("RCUI SDF pipeline submission failed; using the safe rounded fallback: {}", exception.toString());
    }
}
*///?} else {
final class GuiGraphicsPipelineSdfBackend { }
//?}
//?}
