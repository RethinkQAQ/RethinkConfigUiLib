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

import com.rethinkqaq.configui.RethinkConfigUiLib;
import com.rethinkqaq.configui.core.UiBounds;
//? if <1.21.3 {
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
//?}
//? if >=1.21.3 && <1.21.5 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.resources.ResourceLocation;
*///?}
//? if >=1.21.6 {
/*import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
*///?}
//? if >=1.21.5 && <1.21.6 {
/*import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
*///?}
//? if >=1.21.6 && <26.2 {
/*import com.mojang.blaze3d.shaders.UniformType;
*///?}
//? if >=1.21.6 && <1.21.11 {
/*import com.mojang.blaze3d.platform.DepthTestFunction;
import net.minecraft.resources.ResourceLocation;
*///?}
//? if >=1.21.11 && <26.1 {
/*import com.mojang.blaze3d.platform.DepthTestFunction;
import net.minecraft.resources.Identifier;
*///?}
//? if >=26.1 {
/*import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}
//? if >=26.2 {
/*import com.mojang.blaze3d.PrimitiveTopology;
import net.minecraft.client.renderer.BindGroupLayouts;
*///?}

/** Version-adapted native signed-distance rounded rectangle renderer. */
final class MinecraftSdfRenderer {
    private MinecraftSdfRenderer() { }

    /** Initializes the demo renderer early, so an opt-in demo reports backend errors at startup. */
    static void prewarm() {
        //? if <1.21.3 {
        shader();
        //?}
    }

    //? if <1.21.3 {
    private static final String SDF_JSON = """
        {"blend":{"func":"add","srcrgb":"srcalpha","dstrgb":"1-srcalpha"},"vertex":"rethink_config_ui_lib/rcui_sdf","fragment":"rethink_config_ui_lib/rcui_sdf","samplers":[],"uniforms":[
        {"name":"ModelViewMat","type":"matrix4x4","count":16,"values":[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1]},
        {"name":"ProjMat","type":"matrix4x4","count":16,"values":[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1]},
        {"name":"ColorModulator","type":"float","count":4,"values":[1,1,1,1]},
        {"name":"SdfBounds","type":"float","count":4,"values":[0,0,1,1]},
        {"name":"SdfStyle","type":"float","count":4,"values":[0,0,0,0]}]}
        """;
    private static final String SDF_VERTEX = """
        #version 150
        in vec3 Position;
        in vec4 Color;
        uniform mat4 ModelViewMat;
        uniform mat4 ProjMat;
        out vec4 vertexColor;
        void main() { gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0); vertexColor = Color; }
        """;
    private static final String SDF_FRAGMENT = """
        #version 150
        in vec4 vertexColor;
        uniform vec4 ColorModulator;
        uniform vec4 SdfBounds;
        uniform vec4 SdfStyle;
        out vec4 fragColor;
        float roundedBoxDistance(vec2 point, vec2 halfSize, float radius) {
            vec2 corner = abs(point) - halfSize + radius;
            return length(max(corner, 0.0)) + min(max(corner.x, corner.y), 0.0) - radius;
        }
        void main() {
            vec2 halfSize = max(SdfBounds.zw * 0.5, vec2(0.5));
            float radius = min(max(SdfStyle.x, 0.0), min(halfSize.x, halfSize.y));
            vec2 point = gl_FragCoord.xy - (SdfBounds.xy + halfSize);
            float feather = max(fwidth(roundedBoxDistance(point, halfSize, radius)), 0.75);
            float alpha = 1.0 - smoothstep(-feather, feather, roundedBoxDistance(point, halfSize, radius));
            if (SdfStyle.z > 0.5) {
                float stroke = min(max(SdfStyle.y, 0.0), min(halfSize.x, halfSize.y));
                vec2 innerSize = max(halfSize - vec2(stroke), vec2(0.0));
                alpha *= smoothstep(-feather, feather, roundedBoxDistance(point, innerSize, max(radius - stroke, 0.0)));
            }
            vec4 color = vertexColor * ColorModulator;
            if (alpha * color.a <= 0.0) discard;
            fragColor = vec4(color.rgb, color.a * alpha);
        }
        """;
    private static ShaderInstance shader;
    private static boolean unavailable;
    private static boolean warned;
    static boolean fill(GuiGraphics graphics, UiBounds box, float radius, int color, float coordinateScale) { return draw(graphics, box, radius, 0f, color, false, coordinateScale); }
    static boolean stroke(GuiGraphics graphics, UiBounds box, float radius, float width, int color, float coordinateScale) { return draw(graphics, box, radius, width, color, true, coordinateScale); }
    static void invalidate() { shader = null; unavailable = false; }
    private static ShaderInstance shader() {
        if (unavailable) return null;
        if (shader != null) return shader;
        try { shader = new ShaderInstance(resources(), "rethink_config_ui_lib/rcui_sdf", DefaultVertexFormat.POSITION_COLOR); return shader; }
        catch (Exception exception) { unavailable = true; warn(exception); return null; }
    }
    private static void warn(Exception exception) {
        if (!warned) { warned = true; RethinkConfigUiLib.LOGGER.warn("RCUI SDF renderer could not initialize; using the safe rounded fallback: {}", exception.toString()); }
    }
    //?}

    //? if <1.21.3 {
    private static ResourceProvider resources() {
        return MinecraftSdfRenderer::resourceFromEmbeddedSource;
    }
    private static Optional<Resource> resourceFromEmbeddedSource(ResourceLocation location) {
        if (!location.getNamespace().equals("minecraft") || !location.getPath().startsWith("shaders/core/rethink_config_ui_lib/")) {
            return Optional.empty();
        }
        String shaderSource = switch (location.getPath()) {
            case "shaders/core/rethink_config_ui_lib/rcui_sdf.json" -> SDF_JSON;
            case "shaders/core/rethink_config_ui_lib/rcui_sdf.vsh" -> SDF_VERTEX;
            case "shaders/core/rethink_config_ui_lib/rcui_sdf.fsh" -> SDF_FRAGMENT;
            default -> null;
        };
        if (shaderSource == null) return Optional.empty();
        // Resource requires a non-null source pack for diagnostics. Its bytes
        // deliberately come from these embedded shader sources, independent of
        // Fabric's optional resource-pack integration.
        return Minecraft.getInstance().getResourceManager().getResource(location.withPath("shaders/core/position_color.json")).map(vanilla ->
            new Resource(vanilla.source(), () -> new ByteArrayInputStream(shaderSource.getBytes(StandardCharsets.UTF_8)))
        );
    }
    //?}

    //? if >=1.21.3 && <1.21.5 {
    /*private static final ShaderProgram PROGRAM = new ShaderProgram(ResourceLocation.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "core/rcui_sdf"), DefaultVertexFormat.POSITION_COLOR, ShaderDefines.EMPTY);
    private static boolean warned;
    static boolean fill(GuiGraphics graphics, UiBounds box, float radius, int color, float coordinateScale) { return draw(graphics, box, radius, 0f, color, false, coordinateScale); }
    static boolean stroke(GuiGraphics graphics, UiBounds box, float radius, float width, int color, float coordinateScale) { return draw(graphics, box, radius, width, color, true, coordinateScale); }
    static void invalidate() { }
    private static void warn(Exception exception) {
        if (!warned) { warned = true; RethinkConfigUiLib.LOGGER.warn("RCUI SDF renderer could not initialize; using the safe rounded fallback: {}", exception.toString()); }
    }
    private static boolean draw(GuiGraphics graphics, UiBounds box, float radius, float stroke, int color, boolean outline, float coordinateScale) {
        if (box.width() <= 0 || box.height() <= 0) return true;
        try {
            CompiledShaderProgram active = Minecraft.getInstance().getShaderManager().getProgramForLoading(PROGRAM);
            if (active == null) {
                warn(new IllegalStateException("RCUI SDF shader program was not loaded: rethink_config_ui_lib:core/rcui_sdf"));
                return false;
            }
            double scale = Minecraft.getInstance().getWindow().getGuiScale() * coordinateScale;
            active.getUniform("SdfBounds").set((float) (box.x() * scale), (float) (Minecraft.getInstance().getWindow().getHeight() - (box.y() + box.height()) * scale), (float) (box.width() * scale), (float) (box.height() * scale));
            active.getUniform("SdfStyle").set(radius * (float) scale, stroke * (float) scale, outline ? 1f : 0f, 0f);
            RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc(); RenderSystem.setShader(active);
            int a = color >>> 24 & 255, r = color >>> 16 & 255, g = color >>> 8 & 255, b = color & 255;
            BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buffer.addVertex(graphics.pose().last().pose(), box.x(), box.y(), 0).setColor(r, g, b, a); buffer.addVertex(graphics.pose().last().pose(), box.x(), box.y() + box.height(), 0).setColor(r, g, b, a); buffer.addVertex(graphics.pose().last().pose(), box.x() + box.width(), box.y() + box.height(), 0).setColor(r, g, b, a); buffer.addVertex(graphics.pose().last().pose(), box.x() + box.width(), box.y(), 0).setColor(r, g, b, a);
            BufferUploader.drawWithShader(buffer.buildOrThrow()); RenderSystem.disableBlend(); return true;
        } catch (Exception exception) { warn(exception); return false; }
    }
    *///?}

    //? if <1.21.1 {
    /*private static boolean draw(GuiGraphics graphics, UiBounds box, float radius, float stroke, int color, boolean outline, float coordinateScale) {
        ShaderInstance active = shader(); if (active == null || box.width() <= 0 || box.height() <= 0) return false;
        try {
            double scale = Minecraft.getInstance().getWindow().getGuiScale() * coordinateScale;
            active.getUniform("SdfBounds").set((float) (box.x() * scale), (float) (Minecraft.getInstance().getWindow().getHeight() - (box.y() + box.height()) * scale), (float) (box.width() * scale), (float) (box.height() * scale));
            active.getUniform("SdfStyle").set(radius * (float) scale, stroke * (float) scale, outline ? 1f : 0f, 0f);
            RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc(); RenderSystem.setShader(() -> active);
            int a = color >>> 24 & 255, r = color >>> 16 & 255, g = color >>> 8 & 255, b = color & 255;
            BufferBuilder buffer = Tesselator.getInstance().getBuilder(); buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buffer.vertex(graphics.pose().last().pose(), box.x(), box.y(), 0).color(r, g, b, a).endVertex(); buffer.vertex(graphics.pose().last().pose(), box.x(), box.y() + box.height(), 0).color(r, g, b, a).endVertex(); buffer.vertex(graphics.pose().last().pose(), box.x() + box.width(), box.y() + box.height(), 0).color(r, g, b, a).endVertex(); buffer.vertex(graphics.pose().last().pose(), box.x() + box.width(), box.y(), 0).color(r, g, b, a).endVertex();
            BufferUploader.drawWithShader(buffer.end()); RenderSystem.disableBlend(); return true;
        } catch (RuntimeException exception) { unavailable = true; warn(exception); return false; }
    }
    *///?}

    //? if >=1.21.1 && <1.21.3 {
    private static boolean draw(GuiGraphics graphics, UiBounds box, float radius, float stroke, int color, boolean outline, float coordinateScale) {
        ShaderInstance active = shader(); if (active == null || box.width() <= 0 || box.height() <= 0) return false;
        try {
            double scale = Minecraft.getInstance().getWindow().getGuiScale() * coordinateScale;
            active.getUniform("SdfBounds").set((float) (box.x() * scale), (float) (Minecraft.getInstance().getWindow().getHeight() - (box.y() + box.height()) * scale), (float) (box.width() * scale), (float) (box.height() * scale));
            active.getUniform("SdfStyle").set(radius * (float) scale, stroke * (float) scale, outline ? 1f : 0f, 0f);
            RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc(); RenderSystem.setShader(() -> active);
            int a = color >>> 24 & 255, r = color >>> 16 & 255, g = color >>> 8 & 255, b = color & 255;
            BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buffer.addVertex(graphics.pose().last().pose(), box.x(), box.y(), 0).setColor(r, g, b, a); buffer.addVertex(graphics.pose().last().pose(), box.x(), box.y() + box.height(), 0).setColor(r, g, b, a); buffer.addVertex(graphics.pose().last().pose(), box.x() + box.width(), box.y() + box.height(), 0).setColor(r, g, b, a); buffer.addVertex(graphics.pose().last().pose(), box.x() + box.width(), box.y(), 0).setColor(r, g, b, a);
            BufferUploader.drawWithShader(buffer.buildOrThrow()); RenderSystem.disableBlend(); return true;
        } catch (RuntimeException exception) { unavailable = true; warn(exception); return false; }
    }
    //?}

    //? if >=1.21.5 && <1.21.6 {
    
    /*private static final Map<String, RenderType> TYPES = new HashMap<>();
    private static RenderType type(float radius, float stroke, boolean outline, float coordinateScale) {
        int r = Math.round(radius * (float) (Minecraft.getInstance().getWindow().getGuiScale() * coordinateScale));
        int s = Math.round(stroke * (float) (Minecraft.getInstance().getWindow().getGuiScale() * coordinateScale));
        String key = r + ":" + s + ":" + outline;
        return TYPES.computeIfAbsent(key, ignored -> {
            RenderPipeline pipeline = RenderPipeline.builder()
                .withLocation(ResourceLocation.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "pipeline/rcui_sdf_" + key.replace(':', '_')))
                .withVertexShader(ResourceLocation.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "core/rcui_sdf_legacy"))
                .withFragmentShader(ResourceLocation.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "core/rcui_sdf_legacy"))
                .withUniform("ModelViewMat", UniformType.MATRIX4X4)
                .withUniform("ProjMat", UniformType.MATRIX4X4)
                .withUniform("ColorModulator", UniformType.VEC4)
                .withShaderDefine("RCUI_RADIUS", r)
                .withShaderDefine("RCUI_STROKE", s)
                .withShaderDefine("RCUI_STROKE_MODE", outline ? 1 : 0)
                .withBlend(BlendFunction.TRANSLUCENT)
                .withDepthTestFunction(com.mojang.blaze3d.platform.DepthTestFunction.NO_DEPTH_TEST)
                .withDepthWrite(false).withCull(false)
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS).build();
            return RenderType.create("rcui_sdf_" + key, 256, pipeline,
                RenderType.CompositeState.builder().createCompositeState(false));
        });
    }
    static boolean fill(GuiGraphics graphics, UiBounds box, float radius, int color, float coordinateScale) { return draw(graphics, box, radius, 0, color, false, coordinateScale); }
    static boolean stroke(GuiGraphics graphics, UiBounds box, float radius, float width, int color, float coordinateScale) { return draw(graphics, box, radius, width, color, true, coordinateScale); }
    private static boolean draw(GuiGraphics graphics, UiBounds box, float radius, float width, int color, boolean outline, float coordinateScale) {
        if (box.width() <= 0 || box.height() <= 0) return true;
        graphics.fill(type(radius, width, outline, coordinateScale), Math.round(box.x()), Math.round(box.y()), Math.round(box.x() + box.width()), Math.round(box.y() + box.height()), color);
        return true;
    }
    static void invalidate() { TYPES.clear(); }
    *///?}

    //? if >=1.21.6 {
    /*private static final Map<PipelineKey, RenderPipeline> PIPELINES = new HashMap<>();
    private static boolean warned;
    private static boolean unavailable;
    static void invalidate() { PIPELINES.clear(); unavailable = false; }
    private record PipelineKey(int radius, int stroke, boolean outline) { }
    private static PipelineKey pipelineKey(float radius, float stroke, boolean outline, float coordinateScale) {
        float pixelScale = (float) (Minecraft.getInstance().getWindow().getGuiScale() * coordinateScale);
        return new PipelineKey(Math.round(radius * pixelScale), Math.round(stroke * pixelScale), outline);
    }
    private static void warn(Exception exception) {
        if (!warned) { warned = true; RethinkConfigUiLib.LOGGER.warn("RCUI SDF pipeline submission failed; using the safe rounded fallback: {}", exception.toString()); }
    }
    *///?}

    //? if >=1.21.6 && <1.21.11 {
    /*static boolean fill(GuiGraphics graphics, UiBounds box, float radius, int color, float coordinateScale) { return draw(graphics, box, radius, 0f, color, false, coordinateScale); }
    static boolean stroke(GuiGraphics graphics, UiBounds box, float radius, float width, int color, float coordinateScale) { return draw(graphics, box, radius, width, color, true, coordinateScale); }
    private static boolean draw(GuiGraphics graphics, UiBounds box, float radius, float stroke, int color, boolean outline, float coordinateScale) {
        if (box.width() <= 0 || box.height() <= 0) return true;
        try {
            graphics.fill(PIPELINES.computeIfAbsent(pipelineKey(radius, stroke, outline, coordinateScale), MinecraftSdfRenderer::pipeline), Math.round(box.x()), Math.round(box.y()), Math.round(box.x() + box.width()), Math.round(box.y() + box.height()), color);
            return true;
        } catch (RuntimeException exception) { warn(exception); return false; }
    }
    private static RenderPipeline pipeline(PipelineKey key) {
        int radius = Math.max(0, Math.min(2048, key.radius())), stroke = Math.max(0, Math.min(512, key.stroke()));
        ResourceLocation base = ResourceLocation.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "core/rcui_sdf_pipeline");
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "pipeline/rcui_sdf_" + radius + "_" + stroke + "_" + (key.outline() ? "stroke" : "fill"));
        return RenderPipeline.builder().withLocation(location).withVertexShader(base).withFragmentShader(base).withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER).withUniform("Projection", UniformType.UNIFORM_BUFFER).withShaderDefine("RCUI_RADIUS", radius).withShaderDefine("RCUI_STROKE", stroke).withShaderDefine("RCUI_STROKE_MODE", key.outline() ? 1 : 0).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS).build();
    }
    *///?}

    //? if >=1.21.11 && <26.1 {
    /*static boolean fill(GuiGraphics graphics, UiBounds box, float radius, int color, float coordinateScale) { return draw(graphics, box, radius, 0f, color, false, coordinateScale); }
    static boolean stroke(GuiGraphics graphics, UiBounds box, float radius, float width, int color, float coordinateScale) { return draw(graphics, box, radius, width, color, true, coordinateScale); }
    private static boolean draw(GuiGraphics graphics, UiBounds box, float radius, float stroke, int color, boolean outline, float coordinateScale) {
        if (box.width() <= 0 || box.height() <= 0) return true;
        try {
            graphics.fill(PIPELINES.computeIfAbsent(pipelineKey(radius, stroke, outline, coordinateScale), MinecraftSdfRenderer::pipeline), Math.round(box.x()), Math.round(box.y()), Math.round(box.x() + box.width()), Math.round(box.y() + box.height()), color);
            return true;
        } catch (RuntimeException exception) { warn(exception); return false; }
    }
    private static RenderPipeline pipeline(PipelineKey key) {
        int radius = Math.max(0, Math.min(2048, key.radius())), stroke = Math.max(0, Math.min(512, key.stroke()));
        Identifier base = Identifier.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "core/rcui_sdf_pipeline");
        Identifier location = Identifier.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "pipeline/rcui_sdf_" + radius + "_" + stroke + "_" + (key.outline() ? "stroke" : "fill"));
        return RenderPipeline.builder().withLocation(location).withVertexShader(base).withFragmentShader(base).withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER).withUniform("Projection", UniformType.UNIFORM_BUFFER).withShaderDefine("RCUI_RADIUS", radius).withShaderDefine("RCUI_STROKE", stroke).withShaderDefine("RCUI_STROKE_MODE", key.outline() ? 1 : 0).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS).build();
    }
    *///?}

    //? if >=26.1 && <26.2 {
    /*static boolean fill(GuiGraphicsExtractor graphics, UiBounds box, float radius, int color, float coordinateScale) { return draw(graphics, box, radius, 0f, color, false, coordinateScale); }
    static boolean stroke(GuiGraphicsExtractor graphics, UiBounds box, float radius, float width, int color, float coordinateScale) { return draw(graphics, box, radius, width, color, true, coordinateScale); }
    private static boolean draw(GuiGraphicsExtractor graphics, UiBounds box, float radius, float stroke, int color, boolean outline, float coordinateScale) {
        if (unavailable || box.width() <= 0 || box.height() <= 0) return !unavailable;
        try {
            PipelineKey key = pipelineKey(radius, stroke, outline, coordinateScale);
            RenderPipeline pipeline = PIPELINES.computeIfAbsent(key, MinecraftSdfRenderer::pipeline);
            CompiledRenderPipeline compiled = RenderSystem.getDevice().precompilePipeline(pipeline);
            if (!compiled.isValid()) {
                PIPELINES.remove(key, pipeline);
                unavailable = true;
                warn(new IllegalStateException("RCUI SDF pipeline compilation failed"));
                return false;
            }
            graphics.fill(pipeline, Math.round(box.x()), Math.round(box.y()), Math.round(box.x() + box.width()), Math.round(box.y() + box.height()), color);
            return true;
        } catch (RuntimeException exception) { unavailable = true; warn(exception); return false; }
    }
    private static RenderPipeline pipeline(PipelineKey key) {
        int radius = Math.max(0, Math.min(2048, key.radius())), stroke = Math.max(0, Math.min(512, key.stroke()));
        Identifier base = Identifier.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "core/rcui_sdf_pipeline");
        Identifier location = Identifier.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "pipeline/rcui_sdf_" + radius + "_" + stroke + "_" + (key.outline() ? "stroke" : "fill"));
        return RenderPipeline.builder().withLocation(location).withVertexShader(base).withFragmentShader(base).withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER).withUniform("Projection", UniformType.UNIFORM_BUFFER).withShaderDefine("RCUI_RADIUS", radius).withShaderDefine("RCUI_STROKE", stroke).withShaderDefine("RCUI_STROKE_MODE", key.outline() ? 1 : 0).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withDepthStencilState(Optional.empty()).withCull(false).withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS).build();
    }
    *///?}

    //? if >=26.2 {
    /*static boolean fill(GuiGraphicsExtractor graphics, UiBounds box, float radius, int color, float coordinateScale) { return draw(graphics, box, radius, 0f, color, false, coordinateScale); }
    static boolean stroke(GuiGraphicsExtractor graphics, UiBounds box, float radius, float width, int color, float coordinateScale) { return draw(graphics, box, radius, width, color, true, coordinateScale); }
    private static boolean draw(GuiGraphicsExtractor graphics, UiBounds box, float radius, float stroke, int color, boolean outline, float coordinateScale) {
        if (unavailable || box.width() <= 0 || box.height() <= 0) return !unavailable;
        try {
            PipelineKey key = pipelineKey(radius, stroke, outline, coordinateScale);
            RenderPipeline pipeline = PIPELINES.computeIfAbsent(key, MinecraftSdfRenderer::pipeline);
            CompiledRenderPipeline compiled = RenderSystem.getDevice().precompilePipeline(pipeline, MinecraftSdfRenderer::shaderSource);
            if (!compiled.isValid()) {
                PIPELINES.remove(key, pipeline);
                unavailable = true;
                warn(new IllegalStateException("RCUI SDF pipeline compilation failed"));
                return false;
            }
            graphics.fill(pipeline, Math.round(box.x()), Math.round(box.y()), Math.round(box.x() + box.width()), Math.round(box.y() + box.height()), color);
            return true;
        } catch (RuntimeException exception) { unavailable = true; warn(exception); return false; }
    }
    private static RenderPipeline pipeline(PipelineKey key) {
        int radius = Math.max(0, Math.min(2048, key.radius())), stroke = Math.max(0, Math.min(512, key.stroke()));
        Identifier base = Identifier.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "core/rcui_sdf_pipeline");
        Identifier location = Identifier.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "pipeline/rcui_sdf_" + radius + "_" + stroke + "_" + (key.outline() ? "stroke" : "fill"));
        return RenderPipeline.builder().withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION).withLocation(location).withVertexShader(base).withFragmentShader(base).withShaderDefine("RCUI_RADIUS", radius).withShaderDefine("RCUI_STROKE", stroke).withShaderDefine("RCUI_STROKE_MODE", key.outline() ? 1 : 0).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withDepthStencilState(Optional.empty()).withCull(false).withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR).withPrimitiveTopology(PrimitiveTopology.QUADS).build();
    }
    private static String shaderSource(Identifier ignored, ShaderType type) {
        String suffix = type == ShaderType.VERTEX ? ".vsh" : ".fsh";
        String resource = "/assets/rethink_config_ui_lib/shaders/core/rcui_sdf_pipeline" + suffix;
        try (InputStream stream = MinecraftSdfRenderer.class.getResourceAsStream(resource)) {
            if (stream == null) return null;
            return new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException exception) {
            return null;
        }
    }
    *///?}
}
