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

//? if >=26.3 {
/*import com.rethinkqaq.configui.RethinkConfigUiLib;
import com.rethinkqaq.configui.core.UiBounds;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.resources.Identifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// RenderPearl pipeline implementation for Minecraft 26.3 and later.
final class RenderPearlSdfBackend implements SdfBackend<GuiGraphicsExtractor> {
    private static final Map<SdfPipelineKey, RenderPipeline> PIPELINES = new HashMap<>();
    private static boolean warned;
    private static boolean unavailable;

    @Override public boolean draw(GuiGraphicsExtractor graphics, UiBounds box, float radius, float stroke, int color,
                                  boolean outline, float coordinateScale) {
        if (unavailable || box.width() <= 0 || box.height() <= 0) return !unavailable;
        try {
            double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
            SdfPipelineKey key = SdfPipelineKey.from(radius, stroke, outline, guiScale, coordinateScale);
            RenderPipeline pipeline = PIPELINES.computeIfAbsent(key, RenderPearlSdfBackend::pipeline);
            CompiledRenderPipeline compiled = RenderSystem.getCompiledPipelineNullable(pipeline);
            if (compiled == null) {
                PIPELINES.remove(key, pipeline);
                unavailable = true;
                warn(new IllegalStateException("RCUI SDF pipeline compilation failed"));
                return false;
            }
            graphics.fill(pipeline, Math.round(box.x()), Math.round(box.y()), Math.round(box.x() + box.width()),
                Math.round(box.y() + box.height()), color);
            return true;
        } catch (RuntimeException exception) {
            unavailable = true;
            warn(exception);
            return false;
        }
    }

    private static RenderPipeline pipeline(SdfPipelineKey key) {
        int radius = Math.max(0, Math.min(2048, key.radius()));
        int stroke = Math.max(0, Math.min(512, key.stroke()));
        Identifier base = Identifier.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "core/rcui_sdf_pipeline_26_3");
        Identifier location = Identifier.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID,
            "pipeline/rcui_sdf_" + radius + "_" + stroke + "_" + (key.outline() ? "stroke" : "fill"));
        return RenderPipeline.builder()
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withLocation(location)
            .withVertexShader(base)
            .withFragmentShader(base)
            .withShaderDefine("RCUI_RADIUS", radius)
            .withShaderDefine("RCUI_STROKE", stroke)
            .withShaderDefine("RCUI_STROKE_MODE", key.outline() ? 1 : 0)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(Optional.empty())
            .withCull(false)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .build();
    }

    private static void warn(Exception exception) {
        if (warned) return;
        warned = true;
        RethinkConfigUiLib.LOGGER.warn("RCUI SDF pipeline submission failed; using the safe rounded fallback: {}", exception.toString());
    }
}
*///?} else {
final class RenderPearlSdfBackend { }
//?}
