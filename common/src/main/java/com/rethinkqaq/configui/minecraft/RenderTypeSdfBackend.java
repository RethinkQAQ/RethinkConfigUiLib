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

//? if >=1.21.6 {
/*final class RenderTypeSdfBackend { }
*///?} else {
//? if >=1.21.5 {
/*import com.rethinkqaq.configui.RethinkConfigUiLib;
import com.rethinkqaq.configui.core.UiBounds;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;

// RenderType-backed pipeline implementation for Minecraft 1.21.5.
final class RenderTypeSdfBackend implements SdfBackend<GuiGraphics> {
    private static final Map<SdfPipelineKey, RenderType> TYPES = new HashMap<>();

    @Override public boolean draw(GuiGraphics graphics, UiBounds box, float radius, float stroke, int color,
                                  boolean outline, float coordinateScale) {
        if (box.width() <= 0 || box.height() <= 0) return true;
        graphics.fill(type(radius, stroke, outline, coordinateScale), Math.round(box.x()), Math.round(box.y()),
            Math.round(box.x() + box.width()), Math.round(box.y() + box.height()), color);
        return true;
    }

    private static RenderType type(float radius, float stroke, boolean outline, float coordinateScale) {
        SdfPipelineKey key = SdfPipelineKey.from(radius, stroke, outline,
            Minecraft.getInstance().getWindow().getGuiScale(), coordinateScale);
        String cacheKey = key.radius() + ":" + key.stroke() + ":" + key.outline();
        return TYPES.computeIfAbsent(key, ignored -> {
            RenderPipeline pipeline = RenderPipeline.builder()
                .withLocation(ResourceLocation.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "pipeline/rcui_sdf_" + cacheKey.replace(':', '_')))
                .withVertexShader(ResourceLocation.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "core/rcui_sdf_legacy"))
                .withFragmentShader(ResourceLocation.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "core/rcui_sdf_legacy"))
                .withUniform("ModelViewMat", UniformType.MATRIX4X4)
                .withUniform("ProjMat", UniformType.MATRIX4X4)
                .withUniform("ColorModulator", UniformType.VEC4)
                .withShaderDefine("RCUI_RADIUS", key.radius())
                .withShaderDefine("RCUI_STROKE", key.stroke())
                .withShaderDefine("RCUI_STROKE_MODE", key.outline() ? 1 : 0)
                .withBlend(BlendFunction.TRANSLUCENT)
                .withDepthTestFunction(com.mojang.blaze3d.platform.DepthTestFunction.NO_DEPTH_TEST)
                .withDepthWrite(false)
                .withCull(false)
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                .build();
            return RenderType.create("rcui_sdf_" + cacheKey, 256, pipeline,
                RenderType.CompositeState.builder().createCompositeState(false));
        });
    }
}
*///?} else {
final class RenderTypeSdfBackend { }
//?}
//?}
