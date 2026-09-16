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
//? if >=26.3 {
import com.rethinkqaq.configui.core.component.input.UiTextField;
//?}
import net.minecraft.client.Minecraft;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
//? if >=1.21.10 {
/*import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
*///?}

/** A standalone Screen with the same core tree API as {@link UiHost}. */
public class UiScreen extends Screen {
    private final Screen parent;
    private final UiHost host;
    //? if >=26.3 {
    /*private boolean textInputFocused;
    *///?}

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
        host.render(new MinecraftUiRenderer(graphics, 1f), width, height,
            Minecraft.getInstance().getWindow().getGuiScale(), mouseX, mouseY);
        //? if >=26.3 {
        /*syncTextInputFocus();
        *///?}
    }
    //? if >=26.2 {
    /*@Override public void onClose() { stopTextInput(); minecraft.gui.setScreen(parent); }
    *///?} else {
    @Override public void onClose() { stopTextInput(); Minecraft.getInstance().setScreen(parent); }
    //?}

    @Override public void removed() { stopTextInput(); }

    //? if >=26.3 {
    /*@Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) { return host.mouseClicked(event.x(), event.y(), uiMouseButton(event)); }
    @Override public boolean mouseReleased(MouseButtonEvent event) { return host.mouseReleased(event.x(), event.y(), uiMouseButton(event)); }
    @Override public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) { return host.mouseDragged(event.x(), event.y(), uiMouseButton(event)); }
    @Override public boolean keyPressed(KeyEvent event) {
        int key = uiKey(event);
        if (key == com.rethinkqaq.configui.core.UiKey.ESCAPE && closeOnEscape()) return true;
        return host.keyPressed(key, uiModifiers(event)) || super.keyPressed(event);
    }
    *///?} else if >=1.21.10 {
    /*@Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) { return host.mouseClicked(event.x(), event.y(), event.button()); }
    @Override public boolean mouseReleased(MouseButtonEvent event) { return host.mouseReleased(event.x(), event.y(), event.button()); }
    @Override public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) { return host.mouseDragged(event.x(), event.y(), event.button()); }
    @Override public boolean keyPressed(KeyEvent event) {
        if (event.key() == com.rethinkqaq.configui.core.UiKey.ESCAPE && closeOnEscape()) return true;
        return host.keyPressed(event.key(), event.modifiers()) || super.keyPressed(event);
    }
    *///?} else {
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { return host.mouseClicked(mouseX, mouseY, button); }
    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) { return host.mouseReleased(mouseX, mouseY, button); }
    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) { return host.mouseDragged(mouseX, mouseY, button); }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == com.rethinkqaq.configui.core.UiKey.ESCAPE && closeOnEscape()) return true;
        return host.keyPressed(keyCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }
    //?}

    //? if >=26.3 {
    /*private static int uiMouseButton(MouseButtonEvent event) {
        return switch (event.button()) {
            case 1 -> 0;
            case 2 -> 1;
            case 3 -> 2;
            default -> event.button();
        };
    }

    private static int uiKey(KeyEvent event) {
        return switch (event.key()) {
            case 4 -> com.rethinkqaq.configui.core.UiKey.A;
            case 6 -> com.rethinkqaq.configui.core.UiKey.C;
            case 25 -> com.rethinkqaq.configui.core.UiKey.V;
            case 27 -> com.rethinkqaq.configui.core.UiKey.X;
            case 40, 88 -> com.rethinkqaq.configui.core.UiKey.ENTER;
            case 41 -> com.rethinkqaq.configui.core.UiKey.ESCAPE;
            case 42 -> com.rethinkqaq.configui.core.UiKey.BACKSPACE;
            case 43 -> com.rethinkqaq.configui.core.UiKey.TAB;
            case 44 -> com.rethinkqaq.configui.core.UiKey.SPACE;
            case 74 -> com.rethinkqaq.configui.core.UiKey.HOME;
            case 75 -> com.rethinkqaq.configui.core.UiKey.PAGE_UP;
            case 76 -> com.rethinkqaq.configui.core.UiKey.DELETE;
            case 77 -> com.rethinkqaq.configui.core.UiKey.END;
            case 78 -> com.rethinkqaq.configui.core.UiKey.PAGE_DOWN;
            case 79 -> com.rethinkqaq.configui.core.UiKey.RIGHT;
            case 80 -> com.rethinkqaq.configui.core.UiKey.LEFT;
            case 81 -> com.rethinkqaq.configui.core.UiKey.DOWN;
            case 82 -> com.rethinkqaq.configui.core.UiKey.UP;
            default -> event.key();
        };
    }

    private static int uiModifiers(KeyEvent event) {
        int modifiers = 0;
        if (event.hasShiftDown()) modifiers |= com.rethinkqaq.configui.core.UiKey.MOD_SHIFT;
        if (event.hasControlDown()) modifiers |= com.rethinkqaq.configui.core.UiKey.MOD_CONTROL;
        return modifiers;
    }
    *///?}

    //? if >=26.3 {
    /*private void syncTextInputFocus() {
        boolean wantsTextInput = host.focusedNode() instanceof UiTextField;
        if (wantsTextInput == textInputFocused) return;
        Minecraft.getInstance().onTextInputFocusChange(this, wantsTextInput);
        textInputFocused = wantsTextInput;
    }

    private void stopTextInput() {
        if (!textInputFocused) return;
        Minecraft.getInstance().onTextInputFocusChange(this, false);
        textInputFocused = false;
    }
    *///?} else {
    private void stopTextInput() { }
    //?}

    private boolean closeOnEscape() {
        if (host.root() instanceof com.rethinkqaq.configui.core.UiDialogHost dialogs && dialogs.showingDialog()) {
            host.keyPressed(com.rethinkqaq.configui.core.UiKey.ESCAPE, 0);
            return true;
        }
        host.keyPressed(com.rethinkqaq.configui.core.UiKey.ESCAPE, 0);
        onClose();
        return true;
    }

    //? if >=26.1 {
    /*@Override public boolean charTyped(net.minecraft.client.input.CharacterEvent event) { return host.charTyped(event.codepoint(), 0) || super.charTyped(event); }
    *///?} else if >=1.21.10 {
    /*@Override public boolean charTyped(net.minecraft.client.input.CharacterEvent event) { return host.charTyped(event.codepoint(), event.modifiers()) || super.charTyped(event); }
    *///?} else {
    @Override public boolean charTyped(char codePoint, int modifiers) { return host.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers); }
    //?}

    //? if <1.20.2 {
    /*@Override public boolean mouseScrolled(double mouseX, double mouseY, double amount) { return host.mouseScrolled(mouseX, mouseY, amount); }
    *///?} else {
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) { return host.mouseScrolled(mouseX, mouseY, scrollY); }
    //?}
}
