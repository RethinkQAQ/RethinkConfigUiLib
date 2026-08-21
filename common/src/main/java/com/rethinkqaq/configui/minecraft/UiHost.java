/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.minecraft;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiKey;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiTheme;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Embeds one core UI tree inside a Minecraft Screen or an existing render callback. */
public final class UiHost {
    /** Selects whether a tree is a centred form or an application-style responsive page. */
    public enum LayoutMode { CONTENT, FULLSCREEN }

    private final Ui.Node root;
    private final UiTheme theme;
    private int width = -1;
    private int height = -1;
    private Ui.Node focused;
    private UiRenderer renderer;
    private final LayoutMode layoutMode;

    public UiHost(Ui.Node root, UiTheme theme) {
        this(root, theme, LayoutMode.CONTENT);
    }

    public UiHost(Ui.Node root, UiTheme theme, LayoutMode layoutMode) {
        this.root = Objects.requireNonNull(root, "root");
        this.theme = Objects.requireNonNull(theme, "theme");
        this.layoutMode = Objects.requireNonNull(layoutMode, "layoutMode");
    }

    public UiTheme theme() { return theme; }
    public Ui.Node root() { return root; }
    public void render(UiRenderer renderer, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        this.renderer = renderer;
        if (width != screenWidth || height != screenHeight) {
            width = screenWidth; height = screenHeight;
            float horizontalInset = layoutMode == LayoutMode.FULLSCREEN ? 16 : 24;
            float verticalInset = layoutMode == LayoutMode.FULLSCREEN ? 16 : 24;
            float availableWidth = Math.max(0, screenWidth - horizontalInset * 2);
            if (layoutMode == LayoutMode.CONTENT) availableWidth = Math.min(availableWidth, 540);
            float availableHeight = Math.max(0, screenHeight - verticalInset * 2);
            root.measure(renderer, availableWidth, availableHeight, theme);
            float contentWidth = Math.min(availableWidth, root.measuredWidth());
            float contentHeight = Math.min(availableHeight, root.measuredHeight());
            float contentX = layoutMode == LayoutMode.CONTENT ? (screenWidth - contentWidth) / 2f : horizontalInset;
            float contentY = verticalInset;
            root.layout(renderer, new com.rethinkqaq.configui.core.UiBounds(
                contentX, contentY, contentWidth, contentHeight), theme);
        }
        updateHovered(root, mouseX, mouseY);
        advanceMotion(root, System.nanoTime());
        root.render(renderer, theme);
        hoveredTooltip(root).ifPresent(tooltip -> renderTooltip(renderer, tooltip.text(), screenWidth, screenHeight, mouseX, mouseY));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = root.click((float) mouseX, (float) mouseY, button);
        if (handled) focusAt((float) mouseX, (float) mouseY);
        return handled;
    }
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return release(root, (float) mouseX, (float) mouseY, button);
    }
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        return drag(root, (float) mouseX, (float) mouseY, button);
    }
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        boolean handled = root.scroll((float) mouseX, (float) mouseY, amount);
        if (handled && renderer != null) root.layout(renderer, root.bounds(), theme);
        return handled;
    }
    public boolean keyPressed(int keyCode, int modifiers) {
        if (keyCode == UiKey.TAB) { moveFocus((modifiers & 1) != 0); return true; }
        return focused != null && focused.key(keyCode);
    }

    private void updateHovered(Ui.Node node, float x, float y) {
        node.setHovered(node.bounds().contains(x, y));
        if (node instanceof Ui.Tooltip tooltip) updateHovered(tooltip.child(), x, y);
        if (node instanceof Ui.ChildProvider provider) for (Ui.Node child : provider.childNodes()) updateHovered(child, x, y);
        if (node instanceof Ui.Container container) for (Ui.Node child : container.children()) updateHovered(child, x, y);
    }
    private void advanceMotion(Ui.Node node, long nowNanos) {
        node.advanceMotion(nowNanos, theme);
        if (node instanceof Ui.Tooltip tooltip) advanceMotion(tooltip.child(), nowNanos);
        if (node instanceof Ui.ChildProvider provider) for (Ui.Node child : provider.childNodes()) advanceMotion(child, nowNanos);
        if (node instanceof Ui.Container container) for (Ui.Node child : container.children()) advanceMotion(child, nowNanos);
    }
    private boolean release(Ui.Node node, float x, float y, int button) {
        boolean handled = node.release(x, y, button);
        if (node instanceof Ui.Tooltip tooltip) handled |= release(tooltip.child(), x, y, button);
        if (node instanceof Ui.ChildProvider provider) for (Ui.Node child : provider.childNodes()) handled |= release(child, x, y, button);
        if (node instanceof Ui.Container container) for (Ui.Node child : container.children()) handled |= release(child, x, y, button);
        return handled;
    }
    private boolean drag(Ui.Node node, float x, float y, int button) {
        boolean handled = node.drag(x, y, button);
        if (node instanceof Ui.Tooltip tooltip) handled |= drag(tooltip.child(), x, y, button);
        if (node instanceof Ui.ChildProvider provider) for (Ui.Node child : provider.childNodes()) handled |= drag(child, x, y, button);
        if (node instanceof Ui.Container container) for (Ui.Node child : container.children()) handled |= drag(child, x, y, button);
        return handled;
    }
    private List<Ui.Node> focusable() {
        List<Ui.Node> result = new ArrayList<>();
        collect(root, result); return result;
    }
    private void collect(Ui.Node node, List<Ui.Node> result) {
        if (node.focusable()) result.add(node);
        if (node instanceof Ui.Tooltip tooltip) collect(tooltip.child(), result);
        if (node instanceof Ui.ChildProvider provider) for (Ui.Node child : provider.childNodes()) collect(child, result);
        if (node instanceof Ui.Container container) for (Ui.Node child : container.children()) collect(child, result);
    }
    private java.util.Optional<Ui.Tooltip> hoveredTooltip(Ui.Node node) {
        if (node instanceof Ui.Tooltip tooltip && tooltip.hovered()) return java.util.Optional.of(tooltip);
        if (node instanceof Ui.Tooltip tooltip) return hoveredTooltip(tooltip.child());
        if (node instanceof Ui.ChildProvider provider) {
            for (Ui.Node child : provider.childNodes()) {
                java.util.Optional<Ui.Tooltip> result = hoveredTooltip(child);
                if (result.isPresent()) return result;
            }
        }
        if (node instanceof Ui.Container container) {
            for (Ui.Node child : container.children()) {
                java.util.Optional<Ui.Tooltip> result = hoveredTooltip(child);
                if (result.isPresent()) return result;
            }
        }
        return java.util.Optional.empty();
    }
    private void renderTooltip(UiRenderer renderer, com.rethinkqaq.configui.core.UiText text, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        float padding = theme.metrics().padding() / 2f;
        float width = renderer.textWidth(text) + padding * 2;
        float height = renderer.lineHeight() + padding * 2;
        float x = Math.min(Math.max(0, mouseX + 10), screenWidth - width);
        float y = Math.min(Math.max(0, mouseY + 10), screenHeight - height);
        com.rethinkqaq.configui.core.UiBounds bounds = new com.rethinkqaq.configui.core.UiBounds(x, y, width, height);
        renderer.fillRoundRect(bounds, theme.metrics().radius(), theme.palette().control());
        renderer.drawText(text, x + padding, y + padding, theme.palette().onAccent());
    }
    private void focusAt(float x, float y) { for (Ui.Node node : focusable()) if (node.bounds().contains(x, y)) { setFocus(node); return; } }
    private void moveFocus(boolean backwards) {
        List<Ui.Node> nodes = focusable(); if (nodes.isEmpty()) return;
        int index = focused == null ? (backwards ? 0 : -1) : nodes.indexOf(focused);
        setFocus(nodes.get(Math.floorMod(index + (backwards ? -1 : 1), nodes.size())));
    }
    private void setFocus(Ui.Node node) { if (focused != null) focused.setFocused(false); focused = node; focused.setFocused(true); }
}
