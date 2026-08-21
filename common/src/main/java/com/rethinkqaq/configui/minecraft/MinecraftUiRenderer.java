/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
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

    //? if >=26.1 {
    /*public MinecraftUiRenderer(GuiGraphicsExtractor graphics) {
    *///?} else {
    public MinecraftUiRenderer(GuiGraphics graphics) {
    //?}
        this.graphics = graphics;
        this.font = Minecraft.getInstance().font;
    }

    @Override public void fillRoundRect(UiBounds box, float radius, int color) {
        if (MinecraftSdfRenderer.fill(graphics, box, radius, color)) return;
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
        if (MinecraftSdfRenderer.stroke(graphics, box, radius, width, color)) return;
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
    @Override public void pushClip(UiBounds box) { graphics.enableScissor(Math.round(box.x()), Math.round(box.y()), Math.round(box.x() + box.width()), Math.round(box.y() + box.height())); }
    @Override public void popClip() { graphics.disableScissor(); }

    //? if >=26.1 {
    /*void renderPreview(MinecraftPreview.Renderer callback, UiBounds bounds) { callback.render(graphics, bounds); }
    *///?} else {
    void renderPreview(MinecraftPreview.Renderer callback, UiBounds bounds) { callback.render(graphics, bounds); }
    //?}

    private static Component component(UiText text) {
        return text.translatable() ? Component.translatable(text.value(), text.arguments().toArray()) : Component.literal(text.value());
    }
}
