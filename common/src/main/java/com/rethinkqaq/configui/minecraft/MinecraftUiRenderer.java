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
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}
import net.minecraft.network.chat.Component;

/** GuiGraphics implementation of the renderer capability exposed by core. */
public final class MinecraftUiRenderer implements UiRenderer {
    //? if >=26.1 {
    /*private final GuiGraphicsExtractor graphics;
    *///?} else {
    private final GuiGraphics graphics;
    //?}
    private final Font font;
    private final float coordinateScale;

    //? if >=26.1 {
    /*public MinecraftUiRenderer(GuiGraphicsExtractor graphics) {
        this(graphics, 1f);
    }
    public MinecraftUiRenderer(GuiGraphicsExtractor graphics, float coordinateScale) {
    *///?} else {
    public MinecraftUiRenderer(GuiGraphics graphics) {
        this(graphics, 1f);
    }
    public MinecraftUiRenderer(GuiGraphics graphics, float coordinateScale) {
    //?}
        this.graphics = graphics;
        this.font = Minecraft.getInstance().font;
        this.coordinateScale = coordinateScale;
    }

    @Override public void fillRoundRect(UiBounds box, float radius, int color) {
        if (MinecraftSdfRenderer.fill(graphics, box, radius, color, coordinateScale)) return;
        int x = Math.round(box.x()), y = Math.round(box.y()), width = Math.round(box.width()), height = Math.round(box.height());
        int inset = Math.min(Math.round(radius), Math.min(width, height) / 2);
        graphics.fill(x + inset, y, x + width - inset, y + height, color);
        graphics.fill(x, y + inset, x + width, y + height - inset, color);
        for (int row = 0; row < inset; row++) {
            int cut = circleInset(inset, row);
            graphics.fill(x + cut, y + row, x + width - cut, y + row + 1, color);
            graphics.fill(x + cut, y + height - row - 1, x + width - cut, y + height - row, color);
        }
    }

    @Override public void strokeRoundRect(UiBounds box, float radius, float width, int color) {
        if (MinecraftSdfRenderer.stroke(graphics, box, radius, width, color, coordinateScale)) return;
        int x = Math.round(box.x()), y = Math.round(box.y());
        int right = Math.round(box.x() + box.width()), bottom = Math.round(box.y() + box.height());
        int boxWidth = right - x, boxHeight = bottom - y;
        int corner = Math.min(Math.round(radius), Math.min(boxWidth, boxHeight) / 2);
        int stroke = Math.min(Math.max(1, Math.round(width)), Math.max(1, corner));
        if (corner == 0) {
            graphics.fill(x, y, right, y + stroke, color);
            graphics.fill(x, bottom - stroke, right, bottom, color);
            graphics.fill(x, y, x + stroke, bottom, color);
            graphics.fill(right - stroke, y, right, bottom, color);
            return;
        }

        // Draw a real rounded ring instead of four square strips. The result remains pixel
        // aligned, but card borders no longer visually cut across rounded corners.
        int innerCorner = Math.max(0, corner - stroke);
        for (int row = 0; row < corner; row++) {
            int outerInset = circleInset(corner, row);
            int leftOuter = x + outerInset;
            int rightOuter = right - outerInset;
            int top = y + row;
            int bottomRow = bottom - row - 1;
            if (row < stroke || innerCorner == 0) {
                graphics.fill(leftOuter, top, rightOuter, top + 1, color);
                graphics.fill(leftOuter, bottomRow, rightOuter, bottomRow + 1, color);
            } else {
                int innerInset = stroke + circleInset(innerCorner, row - stroke);
                int leftInner = x + innerInset;
                int rightInner = right - innerInset;
                graphics.fill(leftOuter, top, leftInner, top + 1, color);
                graphics.fill(rightInner, top, rightOuter, top + 1, color);
                graphics.fill(leftOuter, bottomRow, leftInner, bottomRow + 1, color);
                graphics.fill(rightInner, bottomRow, rightOuter, bottomRow + 1, color);
            }
        }
        graphics.fill(x, y + corner, x + stroke, bottom - corner, color);
        graphics.fill(right - stroke, y + corner, right, bottom - corner, color);
    }

    private static int circleInset(int radius, int row) {
        double vertical = radius - row - .5d;
        return Math.max(0, (int) Math.ceil(radius - Math.sqrt(Math.max(0d, radius * radius - vertical * vertical))));
    }

    //? if >=26.1 {
    /*@Override public void drawText(UiText text, float x, float y, int color) { graphics.text(font, component(text), Math.round(x), Math.round(y), color, false); }
    *///?} else {
    @Override public void drawText(UiText text, float x, float y, int color) { graphics.drawString(font, component(text), Math.round(x), Math.round(y), color, false); }
    //?}
    @Override public float textWidth(UiText text) { return font.width(component(text)); }
    @Override public float lineHeight() { return font.lineHeight; }
    //? if >=1.21.8 {
    /*@Override public void pushClip(UiBounds box) {
        enableScissorWithAntialiasMargin(box);
    }
    *///?} else {
    @Override public void pushClip(UiBounds box) {
        enableScissorWithAntialiasMargin(box);
    }
    //?}
    @Override public void popClip() { graphics.disableScissor(); }

    /**
     * GuiGraphics transforms scissor coordinates by the current UI pose.  Keep one physical
     * pixel of margin so SDF/text antialias pixels at a rounded edge are not discarded by an
     * inclusive/exclusive integer conversion (especially when adaptive scaling is below 1).
     */
    private void enableScissorWithAntialiasMargin(UiBounds box) {
        float margin = 1f / Math.max(.25f, coordinateScale);
        //? if >=1.21.8 {
        /*// GuiGraphicsExtractor transforms the rectangle by its current pose.
        graphics.enableScissor(Math.round(box.x() - margin), Math.round(box.y() - margin),
            Math.round(box.x() + box.width() + margin),
            Math.round(box.y() + box.height() + margin));
        *///?} else {
        // DrawContext (1.20.x-1.21.4) keeps scissor coordinates in screen space and does not
        // transform them by the pose, so apply the UI scale explicitly here.
        graphics.enableScissor(Math.round((box.x() - margin) * coordinateScale),
            Math.round((box.y() - margin) * coordinateScale),
            Math.round((box.x() + box.width() + margin) * coordinateScale),
            Math.round((box.y() + box.height() + margin) * coordinateScale));
        //?}
    }

    //? if >=26.1 {
    /*void renderPreview(MinecraftPreview.Renderer callback, UiBounds bounds) { callback.render(graphics, bounds); }
    *///?} else {
    void renderPreview(MinecraftPreview.Renderer callback, UiBounds bounds) { callback.render(graphics, bounds); }
    //?}

    private static Component component(UiText text) {
        return text.translatable() ? Component.translatable(text.value(), text.arguments().toArray()) : Component.literal(text.value());
    }
}
