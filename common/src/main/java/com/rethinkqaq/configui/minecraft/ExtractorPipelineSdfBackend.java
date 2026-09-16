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
/*final class ExtractorPipelineSdfBackend { }
*///?} else {
//? if >=26.1 {
/*import com.rethinkqaq.configui.RethinkConfigUiLib;
import com.rethinkqaq.configui.core.UiBounds;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
//? if >=26.2 {
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.shaders.ShaderType;
import java.io.IOException;
import java.io.InputStream;
//?}

// RenderPipeline implementation for GuiGraphicsExtractor on 26.1 and 26.2.
final class ExtractorPipelineSdfBackend implements SdfBackend<GuiGraphicsExtractor> {
    private static final Map<SdfPipelineKey, RenderPipeline> PIPELINES = new HashMap<>();
    private static boolean warned;
    private static boolean unavailable;

    @Override public boolean draw(GuiGraphicsExtractor graphics, UiBounds box, float radius, float stroke, int color,
                                  boolean outline, float coordinateScale) {
        if (unavailable || box.width() <= 0 || box.height() <= 0) return !unavailable;
        try {
            double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
            SdfPipelineKey key = SdfPipelineKey.from(radius, stroke, outline, guiScale, coordinateScale);
            RenderPipeline pipeline = PIPELINES.computeIfAbsent(key, ExtractorPipelineSdfBackend::pipeline);
            CompiledRenderPipeline compiled = precompile(pipeline);
            if (!compiled.isValid()) {
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

    private static CompiledRenderPipeline precompile(RenderPipeline pipeline) {
        // 26.2 introduced the source-provider overload; 26.1 uses the resource manager path.
        //? if >=26.2 {
        return RenderSystem.getDevice().precompilePipeline(pipeline, ExtractorPipelineSdfBackend::shaderSource);
        //?} else {
        return RenderSystem.getDevice().precompilePipeline(pipeline);
        //?}
    }

    private static RenderPipeline pipeline(SdfPipelineKey key) {
        int radius = Math.max(0, Math.min(2048, key.radius()));
        int stroke = Math.max(0, Math.min(512, key.stroke()));
        Identifier base = Identifier.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "core/rcui_sdf_pipeline");
        Identifier location = Identifier.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID,
            "pipeline/rcui_sdf_" + radius + "_" + stroke + "_" + (key.outline() ? "stroke" : "fill"));
        RenderPipeline.Builder builder = RenderPipeline.builder()
            .withLocation(location)
            .withVertexShader(base)
            .withFragmentShader(base)
            .withShaderDefine("RCUI_RADIUS", radius)
            .withShaderDefine("RCUI_STROKE", stroke)
            .withShaderDefine("RCUI_STROKE_MODE", key.outline() ? 1 : 0)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(Optional.empty())
            .withCull(false);
        // 26.1 declares matrix uniforms directly; 26.2 moved them into bind-group layouts.
        //? if >=26.2 {
        builder.withBindGroupLayout(net.minecraft.client.renderer.BindGroupLayouts.MATRICES_PROJECTION);
        //?} else {
        builder.withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER);
        //?}
        // Vertex input APIs changed in 26.2 along with the shader source provider.
        //? if >=26.2 {
        return builder.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .build();
        //?} else {
        return builder.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS).build();
        //?}
    }

    //? if >=26.2 {
    private static String shaderSource(Identifier ignored, ShaderType type) {
        String suffix = type == ShaderType.VERTEX ? ".vsh" : ".fsh";
        String resource = "/assets/rethink_config_ui_lib/shaders/core/rcui_sdf_pipeline" + suffix;
        try (InputStream stream = ExtractorPipelineSdfBackend.class.getResourceAsStream(resource)) {
            if (stream == null) return null;
            return new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException exception) {
            return null;
        }
    }
    //?}

    private static void warn(Exception exception) {
        if (warned) return;
        warned = true;
        RethinkConfigUiLib.LOGGER.warn("RCUI SDF pipeline submission failed; using the safe rounded fallback: {}", exception.toString());
    }
}
*///?} else {
final class ExtractorPipelineSdfBackend { }
//?}
//?}
