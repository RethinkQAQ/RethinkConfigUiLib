/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.minecraft;

import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
//? if >=1.21.11 {
/*import net.minecraft.client.input.MouseButtonEvent;
*///?}
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}

/** Flat, rounded RCUI-styled button used only by the opt-in demo entry. */
public final class FlatDemoButton extends AbstractWidget {
    private final Runnable action;
    private final UiTheme theme = UiTheme.roseLight();

    public FlatDemoButton(int x, int y, int width, int height, Runnable action) {
        super(x, y, width, height, Component.literal("RCUI Demo"));
        this.action = action;
    }

    //? if >=26.1 {
    /*@Override protected void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    *///?} else {
    @Override protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    //?}
        UiRenderer renderer = new MinecraftUiRenderer(graphics);
        UiBounds bounds = new UiBounds(getX(), getY(), width, height);
        int color = isHoveredOrFocused() ? theme.palette().accentHover() : theme.palette().control();
        renderer.fillRoundRect(bounds, theme.metrics().radius(), color);
        UiText text = UiText.literal(getMessage().getString());
        renderer.drawText(text, getX() + (width - renderer.textWidth(text)) / 2f,
            getY() + (height - renderer.lineHeight()) / 2f, theme.palette().onAccent());
        if (isFocused()) renderer.strokeRoundRect(bounds, theme.metrics().radius(), theme.metrics().borderWidth(), theme.palette().focusRing());
    }

    @Override protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }

    //? if >=1.21.11 {
    /*@Override public void onClick(MouseButtonEvent event, boolean doubleClick) { action.run(); }
    *///?} else {
    @Override public void onClick(double mouseX, double mouseY) { action.run(); }
    //?}
}
