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

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiClipboard;
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
        this.host.clipboard(new UiClipboard() {
            @Override public String get() { return Minecraft.getInstance().keyboardHandler.getClipboard(); }
            @Override public void set(String value) { Minecraft.getInstance().keyboardHandler.setClipboard(value); }
        });
    }

    public UiHost host() { return host; }
    //? if >=26.1 {
    /*@Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    *///?} else {
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    //?}
        graphics.fill(0, 0, width, height, host.theme().palette().surface());
        float contentScale = host.contentScale(Minecraft.getInstance().getWindow().getGuiScale());
        //? if >=1.21.8 {
        /*graphics.pose().pushMatrix();
        graphics.pose().scale(contentScale, contentScale);
        host.render(new MinecraftUiRenderer(graphics, contentScale), width, height,
            Minecraft.getInstance().getWindow().getGuiScale(), mouseX, mouseY);
        graphics.pose().popMatrix();
        *///?} else {
        graphics.pose().pushPose();
        graphics.pose().scale(contentScale, contentScale, 1f);
        host.render(new MinecraftUiRenderer(graphics, contentScale), width, height,
            Minecraft.getInstance().getWindow().getGuiScale(), mouseX, mouseY);
        graphics.pose().popPose();
        //?}
    }
    //? if >=26.2 {
    /*@Override public void onClose() { minecraft.gui.setScreen(parent); }
    *///?} else {
    @Override public void onClose() { Minecraft.getInstance().setScreen(parent); }
    //?}

    //? if >=1.21.11 {
    /*@Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) { return host.mouseClicked(event.x() / contentScale(), event.y() / contentScale(), event.button()); }
    @Override public boolean mouseReleased(MouseButtonEvent event) { return host.mouseReleased(event.x() / contentScale(), event.y() / contentScale(), event.button()); }
    @Override public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) { return host.mouseDragged(event.x() / contentScale(), event.y() / contentScale(), event.button()); }
    @Override public boolean keyPressed(KeyEvent event) { return host.keyPressed(event.key(), event.modifiers()) || super.keyPressed(event); }
    *///?} else {
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { return host.mouseClicked(mouseX / contentScale(), mouseY / contentScale(), button); }
    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) { return host.mouseReleased(mouseX / contentScale(), mouseY / contentScale(), button); }
    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) { return host.mouseDragged(mouseX / contentScale(), mouseY / contentScale(), button); }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return host.keyPressed(keyCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers); }
    //?}

    //? if >=26.1 {
    /*@Override public boolean charTyped(net.minecraft.client.input.CharacterEvent event) { return host.charTyped(event.codepoint(), 0) || super.charTyped(event); }
    *///?} else if >=1.21.11 {
    /*@Override public boolean charTyped(net.minecraft.client.input.CharacterEvent event) { return host.charTyped(event.codepoint(), event.modifiers()) || super.charTyped(event); }
    *///?} else {
    @Override public boolean charTyped(char codePoint, int modifiers) { return host.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers); }
    //?}

    //? if <1.20.2 {
    /*@Override public boolean mouseScrolled(double mouseX, double mouseY, double amount) { return host.mouseScrolled(mouseX / contentScale(), mouseY / contentScale(), amount); }
    *///?} else {
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) { return host.mouseScrolled(mouseX / contentScale(), mouseY / contentScale(), scrollY); }
    //?}

    private float contentScale() { return host.contentScale(Minecraft.getInstance().getWindow().getGuiScale()); }
}
