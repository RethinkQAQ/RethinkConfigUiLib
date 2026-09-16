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

//? if >=1.21.5 {
/*final class ShaderProgramSdfBackend { }
*///?} else {
//? if >=1.21.3 {
/*import com.rethinkqaq.configui.RethinkConfigUiLib;
import com.rethinkqaq.configui.core.UiBounds;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.resources.ResourceLocation;

// ShaderProgram implementation for Minecraft 1.21.3 and 1.21.4.
final class ShaderProgramSdfBackend implements SdfBackend<GuiGraphics> {
    private static final ShaderProgram PROGRAM = new ShaderProgram(
        ResourceLocation.fromNamespaceAndPath(RethinkConfigUiLib.MOD_ID, "core/rcui_sdf"),
        DefaultVertexFormat.POSITION_COLOR,
        ShaderDefines.EMPTY
    );
    private boolean warned;

    @Override public boolean draw(GuiGraphics graphics, UiBounds box, float radius, float stroke, int color,
                                  boolean outline, float coordinateScale) {
        if (box.width() <= 0 || box.height() <= 0) return true;
        try {
            CompiledShaderProgram active = Minecraft.getInstance().getShaderManager().getProgramForLoading(PROGRAM);
            if (active == null) {
                warn(new IllegalStateException("RCUI SDF shader program was not loaded: rethink_config_ui_lib:core/rcui_sdf"));
                return false;
            }
            double scale = Minecraft.getInstance().getWindow().getGuiScale() * coordinateScale;
            active.getUniform("SdfBounds").set((float) (box.x() * scale),
                (float) (Minecraft.getInstance().getWindow().getHeight() - (box.y() + box.height()) * scale),
                (float) (box.width() * scale), (float) (box.height() * scale));
            active.getUniform("SdfStyle").set(radius * (float) scale, stroke * (float) scale, outline ? 1f : 0f, 0f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(active);
            int alpha = color >>> 24 & 255;
            int red = color >>> 16 & 255;
            int green = color >>> 8 & 255;
            int blue = color & 255;
            BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buffer.addVertex(graphics.pose().last().pose(), box.x(), box.y(), 0).setColor(red, green, blue, alpha);
            buffer.addVertex(graphics.pose().last().pose(), box.x(), box.y() + box.height(), 0).setColor(red, green, blue, alpha);
            buffer.addVertex(graphics.pose().last().pose(), box.x() + box.width(), box.y() + box.height(), 0).setColor(red, green, blue, alpha);
            buffer.addVertex(graphics.pose().last().pose(), box.x() + box.width(), box.y(), 0).setColor(red, green, blue, alpha);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
            RenderSystem.disableBlend();
            return true;
        } catch (Exception exception) {
            warn(exception);
            return false;
        }
    }

    private void warn(Exception exception) {
        if (warned) return;
        warned = true;
        RethinkConfigUiLib.LOGGER.warn("RCUI SDF renderer could not initialize; using the safe rounded fallback: {}", exception.toString());
    }
}
*///?} else {
final class ShaderProgramSdfBackend { }
//?}
//?}
