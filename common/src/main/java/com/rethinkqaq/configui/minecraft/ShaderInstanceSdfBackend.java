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

//? if >=1.21.3 {
/*final class ShaderInstanceSdfBackend { }
*///?} else {
import com.rethinkqaq.configui.RethinkConfigUiLib;
import com.rethinkqaq.configui.core.UiBounds;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;

// ShaderInstance implementation for Minecraft versions before 1.21.3.
final class ShaderInstanceSdfBackend implements SdfBackend<GuiGraphics> {
    private ShaderInstance shader;
    private boolean unavailable;
    private boolean warned;

    @Override public void prewarm() {
        shader();
    }

    @Override public boolean draw(GuiGraphics graphics, UiBounds box, float radius, float stroke, int color,
                                  boolean outline, float coordinateScale) {
        ShaderInstance active = shader();
        if (active == null || box.width() <= 0 || box.height() <= 0) return false;
        try {
            double scale = Minecraft.getInstance().getWindow().getGuiScale() * coordinateScale;
            active.getUniform("SdfBounds").set((float) (box.x() * scale),
                (float) (Minecraft.getInstance().getWindow().getHeight() - (box.y() + box.height()) * scale),
                (float) (box.width() * scale), (float) (box.height() * scale));
            active.getUniform("SdfStyle").set(radius * (float) scale, stroke * (float) scale, outline ? 1f : 0f, 0f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(() -> active);
            int alpha = color >>> 24 & 255;
            int red = color >>> 16 & 255;
            int green = color >>> 8 & 255;
            int blue = color & 255;
            BufferBuilder buffer;
            // 1.20.x exposes the older builder API; 1.21.1/1.21.2 use begin(...).
            //? if <1.21.1 {
            /*buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            *///?} else {
            buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            //?}
            // 1.20.x exposes the older builder API; 1.21.1/1.21.2 use addVertex/buildOrThrow.
            //? if <1.21.1 {
            /*buffer.vertex(graphics.pose().last().pose(), box.x(), box.y(), 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(graphics.pose().last().pose(), box.x(), box.y() + box.height(), 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(graphics.pose().last().pose(), box.x() + box.width(), box.y() + box.height(), 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(graphics.pose().last().pose(), box.x() + box.width(), box.y(), 0).color(red, green, blue, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
            *///?} else {
            buffer.addVertex(graphics.pose().last().pose(), box.x(), box.y(), 0).setColor(red, green, blue, alpha);
            buffer.addVertex(graphics.pose().last().pose(), box.x(), box.y() + box.height(), 0).setColor(red, green, blue, alpha);
            buffer.addVertex(graphics.pose().last().pose(), box.x() + box.width(), box.y() + box.height(), 0).setColor(red, green, blue, alpha);
            buffer.addVertex(graphics.pose().last().pose(), box.x() + box.width(), box.y(), 0).setColor(red, green, blue, alpha);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
            //?}
            RenderSystem.disableBlend();
            return true;
        } catch (RuntimeException exception) {
            unavailable = true;
            warn(exception);
            return false;
        }
    }

    private ShaderInstance shader() {
        if (unavailable) return null;
        if (shader != null) return shader;
        try {
            shader = new ShaderInstance(LegacySdfShaderResources.provider(), "rethink_config_ui_lib/rcui_sdf", DefaultVertexFormat.POSITION_COLOR);
            return shader;
        } catch (Exception exception) {
            unavailable = true;
            warn(exception);
            return null;
        }
    }

    private void warn(Exception exception) {
        if (warned) return;
        warned = true;
        RethinkConfigUiLib.LOGGER.warn("RCUI SDF renderer could not initialize; using the safe rounded fallback: {}", exception.toString());
    }
}
//?}
