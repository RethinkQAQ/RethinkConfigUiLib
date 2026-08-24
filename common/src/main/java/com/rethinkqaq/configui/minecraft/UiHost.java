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
import com.rethinkqaq.configui.core.UiBackground;
import com.rethinkqaq.configui.core.UiClipboard;
import com.rethinkqaq.configui.core.UiDialogHost;
import com.rethinkqaq.configui.core.UiKey;
import com.rethinkqaq.configui.core.UiKeyEvent;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiTextInput;
import com.rethinkqaq.configui.core.UiTheme;
import com.rethinkqaq.configui.core.component.UiTooltip;
import com.rethinkqaq.configui.core.component.UiTooltipContent;
import com.rethinkqaq.configui.core.component.feedback.UiNotificationCenter;
import com.rethinkqaq.configui.core.component.feedback.UiToast;
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
    private long layoutRevision = Long.MIN_VALUE;
    private Ui.Node focused;
    private Ui.Node pointerCapture;
    private UiRenderer renderer;
    private UiClipboard clipboard = UiClipboard.memory();
    private final UiNotificationCenter notifications = new UiNotificationCenter();
    private final LayoutMode layoutMode;
    private UiScalePolicy scalePolicy = UiScalePolicy.minecraft();
    private UiBackground background;

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
    /** Overrides the default opaque theme background for standalone or embedded hosts. */
    public UiHost background(UiBackground value) { background = Objects.requireNonNull(value, "background"); return this; }
    public UiBackground background() {
        return background == null ? UiBackground.opaque(theme.palette().surface()) : background;
    }
    /** Provides the host clipboard to focused core input controls. */
    public UiHost clipboard(UiClipboard value) { clipboard = Objects.requireNonNull(value, "clipboard"); return this; }
    public UiClipboard clipboard() { return clipboard; }
    /** The host-owned notification stack rendered after the UI tree. */
    public UiNotificationCenter notifications() { return notifications; }
    public void showToast(UiToast toast) { notifications.show(toast); }
    /** Changes the policy used to map this surface onto Minecraft's selected GUI scale. */
    public UiHost scalePolicy(UiScalePolicy value) {
        scalePolicy = Objects.requireNonNull(value, "scalePolicy");
        width = -1;
        height = -1;
        return this;
    }
    public UiScalePolicy scalePolicy() { return scalePolicy; }
    /** Returns the current UI canvas transform for a Minecraft GUI Scale value. */
    public float contentScale(double minecraftGuiScale) { return scalePolicy.contentScale(minecraftGuiScale); }
    public void render(UiRenderer renderer, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        render(renderer, screenWidth, screenHeight, 1d, mouseX, mouseY);
    }
    /**
     * Renders against Minecraft's logical screen size. The GUI scale argument is accepted for
     * platform-call compatibility, but never creates a second coordinate system.
     */
    public void render(UiRenderer renderer, int screenWidth, int screenHeight, double minecraftGuiScale, int mouseX, int mouseY) {
        this.renderer = renderer;
        float contentScale = contentScale(minecraftGuiScale);
        int canvasWidth = Math.max(1, Math.round(screenWidth / contentScale));
        int canvasHeight = Math.max(1, Math.round(screenHeight / contentScale));
        if (root instanceof UiDialogHost dialogs) {
            dialogs.viewport(new com.rethinkqaq.configui.core.UiBounds(0, 0, canvasWidth, canvasHeight));
        }
        long currentRevision = layoutRevision(root);
        if (width != canvasWidth || height != canvasHeight || layoutRevision != currentRevision) {
            width = canvasWidth; height = canvasHeight;
            layoutRevision = currentRevision;
            float horizontalInset = layoutMode == LayoutMode.FULLSCREEN ? 12 : 20;
            float verticalInset = layoutMode == LayoutMode.FULLSCREEN ? 12 : 20;
            float availableWidth = Math.max(0, canvasWidth - horizontalInset * 2);
            if (layoutMode == LayoutMode.CONTENT) availableWidth = Math.min(availableWidth, 540);
            float availableHeight = Math.max(0, canvasHeight - verticalInset * 2);
            root.measure(renderer, availableWidth, availableHeight, theme);
            float contentWidth = Math.min(availableWidth, root.measuredWidth());
            float contentHeight = Math.min(availableHeight, root.measuredHeight());
            // FULLSCREEN is still a responsive shell, not a left-aligned canvas. When the
            // scaffold reaches its max width, center it like a web page while retaining the
            // inset when the logical viewport is narrower than that cap.
            float contentX = (canvasWidth - contentWidth) / 2f;
            float contentY = verticalInset;
            root.layout(renderer, new com.rethinkqaq.configui.core.UiBounds(
                contentX, contentY, contentWidth, contentHeight), theme);
        }
        float canvasMouseX = mouseX;
        float canvasMouseY = mouseY;
        updateHovered(root, canvasMouseX, canvasMouseY);
        advanceMotion(root, System.nanoTime());
        root.render(renderer, theme);
        hoveredTooltip(root).ifPresent(tooltip -> renderTooltip(renderer, tooltip, canvasWidth, canvasHeight,
            Math.round(canvasMouseX), Math.round(canvasMouseY)));
        notifications.render(renderer, canvasWidth, canvasHeight, theme);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        clearDetachedInputState();
        if (button == 0) pointerCapture = null;
        boolean handled = root.click((float) mouseX, (float) mouseY, button);
        if (handled) pointerCapture = findPointerCapture(root);
        if (handled) focusAt((float) mouseX, (float) mouseY);
        return handled;
    }
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        clearDetachedInputState();
        if (pointerCapture != null) {
            boolean handled = pointerCapture.release((float) mouseX, (float) mouseY, button);
            if (button == 0) pointerCapture = null;
            return handled;
        }
        return release(root, (float) mouseX, (float) mouseY, button);
    }
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        clearDetachedInputState();
        if (pointerCapture != null) return pointerCapture.drag((float) mouseX, (float) mouseY, button);
        return drag(root, (float) mouseX, (float) mouseY, button);
    }
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        clearDetachedInputState();
        boolean handled = root.scroll((float) mouseX, (float) mouseY, amount);
        if (handled && renderer != null) root.layout(renderer, root.bounds(), theme);
        return handled;
    }
    public boolean keyPressed(int keyCode, int modifiers) {
        clearDetachedInputState();
        if (keyCode == UiKey.TAB) { moveFocus((modifiers & 1) != 0); return true; }
        UiKeyEvent event = new UiKeyEvent(keyCode, 0, modifiers);
        // Escape is a dialog-level action even while a text field owns focus.
        if (keyCode == UiKey.ESCAPE && root instanceof UiDialogHost dialogs && dialogs.showingDialog()) {
            return root.key(event, clipboard);
        }
        return (focused != null && focused.key(event, clipboard)) || root.key(event, clipboard);
    }
    public boolean charTyped(int codePoint, int modifiers) {
        clearDetachedInputState();
        UiTextInput event = new UiTextInput(codePoint, modifiers);
        return (focused != null && focused.textInput(event, clipboard)) || root.textInput(event, clipboard);
    }

    private long layoutRevision(Ui.Node node) {
        // A maximum loses structural mutations whenever an existing descendant has a larger
        // local counter than a newly inserted node. Fold identity, local revision and children
        // so adding a dialog (or replacing a collection row) always produces a new signature.
        long revision = mix(System.identityHashCode(node), node.layoutVersion());
        if (node instanceof Ui.Tooltip tooltip) {
            revision = mix(revision, layoutRevision(tooltip.child()));
            if (tooltip.content() != null) revision = mix(revision, tooltip.content().layoutVersion());
        }
        if (node instanceof Ui.ChildProvider provider) for (Ui.Node child : provider.childNodes()) revision = mix(revision, layoutRevision(child));
        if (node instanceof Ui.Container container) for (Ui.Node child : container.children()) revision = mix(revision, layoutRevision(child));
        return revision;
    }

    private long mix(long current, long next) { return current * 31L + next; }

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
    private Ui.Node findPointerCapture(Ui.Node node) {
        if (node instanceof Ui.Tooltip tooltip) {
            Ui.Node child = findPointerCapture(tooltip.child());
            if (child != null) return child;
        }
        if (node instanceof Ui.ChildProvider provider) {
            for (Ui.Node child : provider.childNodes()) {
                Ui.Node capture = findPointerCapture(child);
                if (capture != null) return capture;
            }
        }
        if (node instanceof Ui.Container container) {
            for (Ui.Node child : container.children()) {
                Ui.Node capture = findPointerCapture(child);
                if (capture != null) return capture;
            }
        }
        return node.capturesPointer() ? node : null;
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
        if (node instanceof Ui.Tooltip tooltip && tooltip.visible(System.nanoTime())) return java.util.Optional.of(tooltip);
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
    private void renderTooltip(UiRenderer renderer, Ui.Tooltip tooltip, int screenWidth, int screenHeight,
                               int mouseX, int mouseY) {
        float padding = tooltip.padding(theme);
        float screenMargin = 6;
        float maximum = Math.min(tooltip.maxWidth(), Math.max(1, screenWidth - screenMargin * 2));
        float width;
        float height;
        List<com.rethinkqaq.configui.core.UiText> lines = List.of();
        UiTooltipContent content = tooltip.content();
        if (content != null) {
            content.measure(renderer, Math.max(1, maximum - padding * 2),
                Math.min(tooltip.maxHeight(), Math.max(1, screenHeight - screenMargin * 2 - padding * 2)), theme);
            width = Math.min(maximum, content.measuredWidth() + padding * 2);
            height = Math.min(tooltip.maxHeight(), content.measuredHeight() + padding * 2);
        } else {
            com.rethinkqaq.configui.core.UiText text = tooltip.text();
            float textMaximum = Math.max(1, maximum - padding * 2);
            if (tooltip.overflow() == UiTooltip.TextOverflow.NO_WRAP) {
                lines = List.of(Ui.fitText(renderer, text, textMaximum));
            } else {
                int lineLimit = tooltip.maxLines();
                if (tooltip.maxHeight() < Float.MAX_VALUE) {
                    float lineSpace = renderer.lineHeight() + tooltip.lineGap();
                    lineLimit = Math.min(lineLimit, Math.max(1,
                        (int) ((tooltip.maxHeight() - padding * 2 + tooltip.lineGap()) / lineSpace)));
                }
                boolean ellipsis = tooltip.overflow() == UiTooltip.TextOverflow.ELLIPSIS;
                lines = Ui.wrapLines(renderer, text, textMaximum, lineLimit, ellipsis);
            }
            float textWidth = 0;
            for (com.rethinkqaq.configui.core.UiText line : lines) textWidth = Math.max(textWidth, renderer.textWidth(line));
            width = Math.max(tooltip.minWidth(), textWidth + padding * 2);
            width = Math.min(maximum, width);
            height = renderer.lineHeight() * lines.size()
                + tooltip.lineGap() * Math.max(0, lines.size() - 1) + padding * 2;
            height = Math.min(tooltip.maxHeight(), height);
        }
        width = Math.max(1, Math.min(maximum, width));
        height = Math.max(1, Math.min(screenHeight - screenMargin * 2, height));
        float x = mouseX + 12;
        if (x + width > screenWidth - screenMargin) x = mouseX - width - 12;
        x = Math.max(screenMargin, Math.min(x, screenWidth - screenMargin - width));
        float y = mouseY + 12;
        if (y + height > screenHeight - screenMargin) y = mouseY - height - 12;
        y = Math.max(screenMargin, Math.min(y, screenHeight - screenMargin - height));
        com.rethinkqaq.configui.core.UiBounds bounds = new com.rethinkqaq.configui.core.UiBounds(x, y, width, height);
        renderer.fillRoundRect(bounds, theme.metrics().radius(), theme.palette().control());
        renderer.strokeRoundRect(bounds, theme.metrics().radius(), Math.min(1, theme.metrics().borderWidth()), theme.palette().border());
        if (content != null) {
            com.rethinkqaq.configui.core.UiBounds contentBounds = bounds.inset(padding);
            content.layout(renderer, contentBounds, theme);
            content.render(renderer, theme);
        } else if (!lines.isEmpty()) {
            boolean clipped = tooltip.overflow() == UiTooltip.TextOverflow.CLIP;
            int lineLimit = lines.size();
            Ui.drawWrappedText(renderer, tooltip.text(), x + padding, y + padding,
                width - padding * 2, lineLimit, theme.palette().onAccent(), tooltip.lineGap(), !clipped);
        }
    }
    private void focusAt(float x, float y) { for (Ui.Node node : focusable()) if (node.bounds().contains(x, y)) { setFocus(node); return; } setFocus(null); }
    private void moveFocus(boolean backwards) {
        List<Ui.Node> nodes = focusable(); if (nodes.isEmpty()) return;
        int index = focused == null ? (backwards ? 0 : -1) : nodes.indexOf(focused);
        setFocus(nodes.get(Math.floorMod(index + (backwards ? -1 : 1), nodes.size())));
    }
    private void setFocus(Ui.Node node) { if (focused == node) return; if (focused != null) focused.setFocused(false); focused = node; if (focused != null) focused.setFocused(true); }
    /** Dynamic collection rows are rebuilt after mutations; never dispatch another event to their old nodes. */
    private void clearDetachedInputState() {
        if (focused != null && !contains(root, focused)) setFocus(null);
        if (pointerCapture != null && !contains(root, pointerCapture)) pointerCapture = null;
    }
    private boolean contains(Ui.Node current, Ui.Node target) {
        if (current == target) return true;
        if (current instanceof Ui.Tooltip tooltip && contains(tooltip.child(), target)) return true;
        if (current instanceof Ui.ChildProvider provider) for (Ui.Node child : provider.childNodes()) if (contains(child, target)) return true;
        if (current instanceof Ui.Container container) for (Ui.Node child : container.children()) if (contains(child, target)) return true;
        return false;
    }
}
