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
        int x = Math.round(box.x()), y = Math.round(box.y()), width = Math.round(box.width()), height = Math.round(box.height());
        int inset = Math.min(Math.round(radius), Math.min(width, height) / 2);
        graphics.fill(x + inset, y, x + width - inset, y + height, color);
        graphics.fill(x, y + inset, x + width, y + height - inset, color);
        for (int row = 0; row < inset; row++) {
            int cut = Math.max(0, inset - row - 1);
            graphics.fill(x + cut, y + row, x + width - cut, y + row + 1, color);
            graphics.fill(x + cut, y + height - row - 1, x + width - cut, y + height - row, color);
        }
    }

    @Override public void strokeRoundRect(UiBounds box, float radius, float width, int color) {
        int stroke = Math.max(1, Math.round(width));
        int x = Math.round(box.x()), y = Math.round(box.y()), right = Math.round(box.x() + box.width()), bottom = Math.round(box.y() + box.height());
        graphics.fill(x, y, right, y + stroke, color);
        graphics.fill(x, bottom - stroke, right, bottom, color);
        graphics.fill(x, y, x + stroke, bottom, color);
        graphics.fill(right - stroke, y, right, bottom, color);
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

    private static Component component(UiText text) {
        return text.translatable() ? Component.translatable(text.value(), text.arguments().toArray()) : Component.literal(text.value());
    }
}
