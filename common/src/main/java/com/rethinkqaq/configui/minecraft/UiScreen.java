/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.minecraft;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiTheme;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
//? if >=1.21.11 {
/*import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
*///?}

/** A standalone Screen with the same core tree API as {@link UiHost}. */
public class UiScreen extends Screen {
    private final Screen parent;
    private final UiHost host;

    public UiScreen(Screen parent, Ui.Node root, UiTheme theme) {
        this(parent, root, theme, UiHost.LayoutMode.CONTENT);
    }

    public UiScreen(Screen parent, Ui.Node root, UiTheme theme, UiHost.LayoutMode layoutMode) {
        super(Component.literal("Rethink Config UI"));
        this.parent = parent;
        this.host = new UiHost(root, theme, layoutMode);
    }

    public UiHost host() { return host; }
    //? if >=26.1 {
    /*@Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    *///?} else {
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    //?}
        graphics.fill(0, 0, width, height, host.theme().palette().surface());
        host.render(new MinecraftUiRenderer(graphics), width, height, mouseX, mouseY);
    }
    //? if >=26.2 {
    /*@Override public void onClose() { minecraft.gui.setScreen(parent); }
    *///?} else {
    @Override public void onClose() { Minecraft.getInstance().setScreen(parent); }
    //?}

    //? if >=1.21.11 {
    /*@Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) { return host.mouseClicked(event.x(), event.y(), event.button()); }
    @Override public boolean mouseReleased(MouseButtonEvent event) { return host.mouseReleased(event.x(), event.y(), event.button()); }
    @Override public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) { return host.mouseDragged(mouseX, mouseY, event.button()); }
    @Override public boolean keyPressed(KeyEvent event) { return host.keyPressed(event.key(), event.modifiers()) || super.keyPressed(event); }
    *///?} else {
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { return host.mouseClicked(mouseX, mouseY, button); }
    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) { return host.mouseReleased(mouseX, mouseY, button); }
    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) { return host.mouseDragged(mouseX, mouseY, button); }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return host.keyPressed(keyCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers); }
    //?}

    //? if <1.20.2 {
    /*@Override public boolean mouseScrolled(double mouseX, double mouseY, double amount) { return host.mouseScrolled(mouseX, mouseY, amount); }
    *///?} else {
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) { return host.mouseScrolled(mouseX, mouseY, scrollY); }
    //?}
}
