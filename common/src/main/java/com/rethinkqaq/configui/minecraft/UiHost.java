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
import com.rethinkqaq.configui.core.UiDensity;
import com.rethinkqaq.configui.core.UiDebugSnapshot;
import com.rethinkqaq.configui.core.UiKey;
import com.rethinkqaq.configui.core.UiKeyEvent;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiScaffold;
import com.rethinkqaq.configui.core.UiTextInput;
import com.rethinkqaq.configui.core.UiTheme;
import com.rethinkqaq.configui.core.layout.UiTemplate;
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
    private UiDensity density = UiDensity.COMFORTABLE;
    private UiTheme effectiveTheme;
    private UiBackground background;
    private boolean inspectorVisible;

    public UiHost(Ui.Node root, UiTheme theme) {
        this(root, theme, LayoutMode.CONTENT);
    }

    public UiHost(Ui.Node root, UiTheme theme, LayoutMode layoutMode) {
        this(root, theme, layoutMode, null);
    }

    public UiHost(UiTemplate template, UiTheme theme, LayoutMode layoutMode) {
        this(template, theme, layoutMode, template.background());
    }

    public UiHost(UiTemplate template, UiTheme theme) {
        this(template, theme, LayoutMode.CONTENT);
    }

    private UiHost(Ui.Node root, UiTheme theme, LayoutMode layoutMode, UiBackground initialBackground) {
        this.root = Objects.requireNonNull(root, "root");
        this.theme = Objects.requireNonNull(theme, "theme");
        this.layoutMode = Objects.requireNonNull(layoutMode, "layoutMode");
        this.background = initialBackground;
        this.root.mount();
    }

    public UiTheme theme() { return theme; }
    private UiTheme effectiveTheme() {
        if (effectiveTheme == null) effectiveTheme = theme.withMetrics(theme.metrics().forDensity(density));
        return effectiveTheme;
    }
    public Ui.Node root() { return root; }
    /** Returns a laid-out tree snapshot for development diagnostics and host tooling. */
    public UiDebugSnapshot debugSnapshot() { return UiDebugSnapshot.of(root); }
    /** Enables the development Inspector overlay. Hosts own any key binding for this switch. */
    public UiHost inspectorVisible(boolean value) { inspectorVisible = value; return this; }
    public boolean inspectorVisible() { return inspectorVisible; }
    /** Overrides the default opaque theme background for standalone or embedded hosts. */
    public UiHost background(UiBackground value) { background = Objects.requireNonNull(value, "background"); return this; }
    public UiBackground background() {
        return background == null ? UiBackground.opaque(theme.palette().surface()) : background;
    }
    /** Provides the host clipboard to focused core input controls. */
    public UiHost clipboard(UiClipboard value) { clipboard = Objects.requireNonNull(value, "clipboard"); return this; }
    public UiClipboard clipboard() { return clipboard; }
    /** Releases the active UI tree and any resources owned by custom nodes. */
    public void dispose() { root.dispose(); }
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
    public UiDensity density() { return density; }
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
        UiDensity nextDensity = scalePolicy.density(minecraftGuiScale);
        if (density != nextDensity) {
            density = nextDensity;
            effectiveTheme = null;
            layoutRevision = Long.MIN_VALUE;
        }
        UiTheme renderTheme = effectiveTheme();
        float contentScale = contentScale(minecraftGuiScale);
        int canvasWidth = Math.max(1, Math.round(screenWidth / contentScale));
        int canvasHeight = Math.max(1, Math.round(screenHeight / contentScale));
        UiBackground surface = background();
        if (surface.paintsSurface()) {
            renderer.fillRect(new com.rethinkqaq.configui.core.UiBounds(0, 0, canvasWidth, canvasHeight), surface.color());
        }
        if (root instanceof UiDialogHost dialogs) {
            dialogs.viewport(new com.rethinkqaq.configui.core.UiBounds(0, 0, canvasWidth, canvasHeight));
        }
        long currentRevision = layoutRevision(root);
        if (width != canvasWidth || height != canvasHeight || layoutRevision != currentRevision) {
            width = canvasWidth; height = canvasHeight;
            layoutRevision = currentRevision;
            float horizontalInset = layoutMode == LayoutMode.FULLSCREEN ? 12 : 20;
            float verticalInset = layoutMode == LayoutMode.FULLSCREEN ? 6 : 20;
            float availableWidth = Math.max(0, canvasWidth - horizontalInset * 2);
            if (layoutMode == LayoutMode.CONTENT) availableWidth = Math.min(availableWidth, 540);
            float availableHeight = Math.max(0, canvasHeight - verticalInset * 2);
            root.measure(renderer, availableWidth, availableHeight, renderTheme);
            float contentWidth = Math.min(availableWidth, root.measuredWidth());
            float contentHeight = Math.min(availableHeight, root.measuredHeight());
            // FULLSCREEN is still a responsive shell, not a left-aligned canvas. When the
            // scaffold reaches its max width, center it like a web page while retaining the
            // inset when the logical viewport is narrower than that cap.
            float contentX = (canvasWidth - contentWidth) / 2f;
            float contentY = verticalInset;
            root.layout(renderer, new com.rethinkqaq.configui.core.UiBounds(
                contentX, contentY, contentWidth, contentHeight), renderTheme);
        }
        float canvasMouseX = mouseX;
        float canvasMouseY = mouseY;
        updateHovered(root, canvasMouseX, canvasMouseY);
        advanceMotion(root, System.nanoTime());
        root.render(renderer, renderTheme);
        hoveredTooltip(root, canvasMouseX, canvasMouseY).ifPresent(tooltip -> renderTooltip(renderer, tooltip, canvasWidth, canvasHeight,
            Math.round(canvasMouseX), Math.round(canvasMouseY)));
        notifications.render(renderer, canvasWidth, canvasHeight, renderTheme);
        if (inspectorVisible) UiInspectorOverlay.render(renderer, debugSnapshot(), renderTheme);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        clearDetachedInputState();
        if (button == 0) pointerCapture = null;
        boolean handled = click(root, (float) mouseX, (float) mouseY, button, viewportBounds());
        if (handled) pointerCapture = findPointerCapture(root);
        // A click may open a modal dialog immediately. The release event will then be routed
        // to the dialog, so release the originating page control now to avoid leaving a button
        // permanently in its pressed visual state.
        if (handled && root instanceof UiDialogHost dialogs && dialogs.showingDialog() && pointerCapture != null) {
            pointerCapture.release((float) mouseX, (float) mouseY, button);
            pointerCapture = null;
        }
        if (handled && root instanceof UiDialogHost dialogs && dialogs.showingDialog()) {
            cancelPointerState(dialogs.root());
            pointerCapture = null;
        }
        if (handled) {
            focusAt((float) mouseX, (float) mouseY);
        } else {
            setFocus(null);
        }
        return handled;
    }
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        clearDetachedInputState();
        if (root instanceof UiDialogHost dialogs && dialogs.showingDialog()) {
            // A control inside a modal (for example a slider) keeps capture until the
            // release arrives, even when the pointer leaves the dialog bounds. The
            // modal still owns the event stream, so the page underneath cannot receive it.
            // A dialog may have been opened after a page control captured the pointer.
            // Never let that stale page capture receive input while the modal is active.
            if (pointerCapture != null && dialogs.dialog() != null && contains(dialogs.dialog(), pointerCapture)) {
                pointerCapture.release((float) mouseX, (float) mouseY, button);
                if (button == 0) pointerCapture = null;
                return true;
            }
            pointerCapture = null;
            release(root, (float) mouseX, (float) mouseY, button);
            return true;
        }
        if (pointerCapture != null) {
            boolean handled = pointerCapture.release((float) mouseX, (float) mouseY, button);
            if (button == 0) pointerCapture = null;
            return handled;
        }
        return release(root, (float) mouseX, (float) mouseY, button);
    }
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        clearDetachedInputState();
        if (root instanceof UiDialogHost dialogs && dialogs.showingDialog()) {
            if (pointerCapture != null && dialogs.dialog() != null && contains(dialogs.dialog(), pointerCapture)) {
                pointerCapture.drag((float) mouseX, (float) mouseY, button);
            } else {
                pointerCapture = null;
                drag(root, (float) mouseX, (float) mouseY, button);
            }
            return true;
        }
        if (pointerCapture != null) return pointerCapture.drag((float) mouseX, (float) mouseY, button);
        return drag(root, (float) mouseX, (float) mouseY, button);
    }
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        clearDetachedInputState();
        if (root instanceof UiDialogHost dialogs && dialogs.showingDialog()) {
            scroll(root, (float) mouseX, (float) mouseY, amount, viewportBounds());
            if (renderer != null) root.layout(renderer, root.bounds(), effectiveTheme());
            clearDetachedInputState();
            return true;
        }
        boolean handled = scroll(root, (float) mouseX, (float) mouseY, amount, viewportBounds());
        if (handled && renderer != null) root.layout(renderer, root.bounds(), effectiveTheme());
        if (handled) clearDetachedInputState();
        return handled;
    }
    public boolean keyPressed(int keyCode, int modifiers) {
        clearDetachedInputState();
        if (keyCode == UiKey.TAB) { moveFocus((modifiers & 1) != 0); return true; }
        UiKeyEvent event = new UiKeyEvent(keyCode, 0, modifiers);
        // A modal owns the complete keyboard stream, including text editing. The page focus
        // below it must never consume an event while the dialog is visible.
        if (root instanceof UiDialogHost dialogs && dialogs.showingDialog()) return root.key(event, clipboard);
        return (focused != null && focused.key(event, clipboard)) || root.key(event, clipboard);
    }
    public boolean charTyped(int codePoint, int modifiers) {
        clearDetachedInputState();
        UiTextInput event = new UiTextInput(codePoint, modifiers);
        if (root instanceof UiDialogHost dialogs && dialogs.showingDialog()) return root.textInput(event, clipboard);
        return (focused != null && focused.textInput(event, clipboard)) || root.textInput(event, clipboard);
    }

    private long layoutRevision(Ui.Node node) {
        if (node == null) return 0;
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
        updateHovered(node, x, y, viewportBounds());
    }
    private void updateHovered(Ui.Node node, float x, float y, com.rethinkqaq.configui.core.UiBounds visible) {
        if (node == null || !node.visible()) { if (node != null) clearHovered(node); return; }
        boolean modal = node instanceof UiDialogHost dialogs && dialogs.showingDialog();
        boolean visibleNode = visible.width() > 0 && visible.height() > 0
            && (modal || (visible.intersects(node.bounds()) && visible.contains(x, y)));
        node.setHovered(visibleNode && node.bounds().contains(x, y));
        if (!visibleNode) {
            clearHovered(node);
            return;
        }
        if (modal) {
            UiDialogHost dialogs = (UiDialogHost) node;
            // A modal dialog owns the pointer; clear stale hover state from the page below it.
            if (dialogs.root() != null) clearHovered(dialogs.root());
            if (dialogs.dialog() != null) updateHovered(dialogs.dialog(), x, y, visible);
            return;
        }
        if (node instanceof UiScaffold scaffold) {
            // Keep hover routing in the same fixed-region order as click/focus routing.  This
            // prevents a scrolled content child from retaining hover while the pointer is over
            // a navigation, header or footer surface.
            Ui.Node footer = scaffold.footer();
            Ui.Node navigation = scaffold.navigationMode() == UiScaffold.NavigationMode.TOP
                ? scaffold.navigation() : scaffold.sidebar();
            Ui.Node header = scaffold.headerVisible() ? scaffold.header() : null;
            Ui.Node active = firstRegionAt(footer, x, y, visible);
            if (active == null) active = firstRegionAt(navigation, x, y, visible);
            if (active == null) active = firstRegionAt(header, x, y, visible);
            if (active == null) active = firstRegionAt(scaffold.content(), x, y, visible);
            updateHoveredRegion(footer, x, y, visible, active == footer);
            updateHoveredRegion(navigation, x, y, visible, active == navigation);
            updateHoveredRegion(header, x, y, visible, active == header);
            updateHoveredRegion(scaffold.content(), x, y, visible, active == scaffold.content());
            return;
        }
        com.rethinkqaq.configui.core.UiBounds childVisible = childViewport(node, visible);
        if (node instanceof Ui.Tooltip tooltip) updateHovered(tooltip.child(), x, y, childVisible);
        if (node instanceof Ui.ChildProvider provider) for (Ui.Node child : provider.childNodes()) updateHovered(child, x, y, childVisible);
        if (node instanceof Ui.Container container) for (Ui.Node child : container.children()) updateHovered(child, x, y, childVisible);
    }
    private Ui.Node firstRegionAt(Ui.Node region, float x, float y,
                                  com.rethinkqaq.configui.core.UiBounds visible) {
        return region != null && region.bounds().contains(x, y) && visible.intersects(region.bounds())
            ? region : null;
    }
    private void updateHoveredRegion(Ui.Node region, float x, float y,
                                     com.rethinkqaq.configui.core.UiBounds visible, boolean active) {
        if (region == null) return;
        if (active) {
            updateHovered(region, x, y, visible);
        } else {
            clearHovered(region);
        }
    }
    private void clearHovered(Ui.Node node) {
        if (node == null) return;
        node.setHovered(false);
        if (node instanceof Ui.Tooltip tooltip) clearHovered(tooltip.child());
        if (node instanceof Ui.ChildProvider provider) for (Ui.Node child : provider.childNodes()) clearHovered(child);
        if (node instanceof Ui.Container container) for (Ui.Node child : container.children()) clearHovered(child);
    }
    private void advanceMotion(Ui.Node node, long nowNanos) {
        if (node == null) return;
        node.advanceMotion(nowNanos, effectiveTheme());
        if (node instanceof Ui.Tooltip tooltip) advanceMotion(tooltip.child(), nowNanos);
        if (node instanceof Ui.ChildProvider provider) for (Ui.Node child : provider.childNodes()) advanceMotion(child, nowNanos);
        if (node instanceof Ui.Container container) for (Ui.Node child : container.children()) advanceMotion(child, nowNanos);
    }
    private boolean click(Ui.Node node, float x, float y, int button,
                          com.rethinkqaq.configui.core.UiBounds visible) {
        if (node == null || !node.visible()) return false;
        boolean modal = node instanceof UiDialogHost dialogs && dialogs.showingDialog();
        if (visible.width() <= 0 || visible.height() <= 0 || !visible.contains(x, y)
            || (!modal && !visible.intersects(node.bounds()))) return false;
        if (modal) {
            UiDialogHost dialogs = (UiDialogHost) node;
            // Dispatch to the dialog exactly once.  Calling the host after a dialog child
            // returns false would invoke the same child a second time through UiDialogHost,
            // which can toggle controls twice or reopen/close a dialog unexpectedly.  The
            // dialog host owns the whole pointer surface, so clicks outside its content are
            // consumed as well and never reach the page underneath.
            if (dialogs.dialog() != null && dialogs.dialog().bounds().contains(x, y)) {
                click(dialogs.dialog(), x, y, button, visible);
            }
            return true;
        }
        // Scaffolds own a fixed z-order (footer, navigation, header, then content).
        // Dispatch through that contract instead of traversing the child list in reverse;
        // this prevents scrolled content from stealing clicks when it overlaps a fixed region.
        if (node instanceof UiScaffold scaffold) {
            return dispatchScaffoldClick(scaffold, x, y, button, visible);
        }
        com.rethinkqaq.configui.core.UiBounds childVisible = childViewport(node, visible);
        boolean delegated = false;
        if (node instanceof Ui.Tooltip tooltip) {
            delegated = true;
            if (click(tooltip.child(), x, y, button, childVisible)) return true;
        }
        if (node instanceof Ui.ChildProvider provider) {
            delegated = true;
            List<Ui.Node> children = provider.childNodes();
            for (int i = children.size() - 1; i >= 0; i--) {
                if (click(children.get(i), x, y, button, childVisible)) return true;
            }
        }
        if (node instanceof Ui.Container container) {
            delegated = true;
            List<Ui.Node> children = container.children();
            for (int i = children.size() - 1; i >= 0; i--) {
                if (click(children.get(i), x, y, button, childVisible)) return true;
            }
        }
        return delegated && !(node instanceof Ui.SelfDispatching) ? false : node.click(x, y, button);
    }
    private boolean scroll(Ui.Node node, float x, float y, double amount,
                           com.rethinkqaq.configui.core.UiBounds visible) {
        if (node == null) return false;
        boolean modal = node instanceof UiDialogHost dialogs && dialogs.showingDialog();
        if (visible.width() <= 0 || visible.height() <= 0 || !visible.contains(x, y)
            || (!modal && !visible.intersects(node.bounds()))) return false;
        if (modal) {
            UiDialogHost dialogs = (UiDialogHost) node;
            return dialogs.dialog() != null && scroll(dialogs.dialog(), x, y, amount, visible);
        }
        if (node instanceof UiScaffold scaffold) {
            return dispatchScaffoldScroll(scaffold, x, y, amount, visible);
        }
        com.rethinkqaq.configui.core.UiBounds childVisible = childViewport(node, visible);
        // Clip-owning nodes perform their own offset update and delegate to their content. Calling
        // them here avoids forwarding the same wheel event twice through generic child traversal.
        if (node instanceof Ui.ClipProvider) return node.scroll(x, y, amount);
        boolean delegated = false;
        if (node instanceof Ui.Tooltip tooltip) {
            delegated = true;
            if (scroll(tooltip.child(), x, y, amount, childVisible)) return true;
        }
        if (node instanceof Ui.ChildProvider provider) {
            delegated = true;
            List<Ui.Node> children = provider.childNodes();
            for (int i = children.size() - 1; i >= 0; i--) {
                if (scroll(children.get(i), x, y, amount, childVisible)) return true;
            }
        }
        if (node instanceof Ui.Container container) {
            delegated = true;
            List<Ui.Node> children = container.children();
            for (int i = children.size() - 1; i >= 0; i--) {
                if (scroll(children.get(i), x, y, amount, childVisible)) return true;
            }
        }
        return delegated ? false : node.scroll(x, y, amount);
    }
    private boolean release(Ui.Node node, float x, float y, int button) {
        return release(node, x, y, button, viewportBounds());
    }
    private boolean release(Ui.Node node, float x, float y, int button, com.rethinkqaq.configui.core.UiBounds visible) {
        if (node == null) return false;
        boolean modal = node instanceof UiDialogHost dialogs && dialogs.showingDialog();
        if (visible.width() <= 0 || visible.height() <= 0 || !visible.contains(x, y)
            || (!modal && !visible.intersects(node.bounds()))) return false;
        if (modal) {
            UiDialogHost dialogs = (UiDialogHost) node;
            return dialogs.dialog() != null && release(dialogs.dialog(), x, y, button, visible);
        }
        if (node instanceof UiScaffold scaffold) {
            return dispatchScaffoldRelease(scaffold, x, y, button, visible);
        }
        com.rethinkqaq.configui.core.UiBounds childVisible = childViewport(node, visible);
        boolean delegated = false;
        if (node instanceof Ui.Tooltip tooltip) {
            delegated = true;
            if (release(tooltip.child(), x, y, button, childVisible)) return true;
        }
        if (node instanceof Ui.ChildProvider provider) {
            delegated = true;
            List<Ui.Node> children = provider.childNodes();
            for (int i = children.size() - 1; i >= 0; i--) {
                if (release(children.get(i), x, y, button, childVisible)) return true;
            }
        }
        if (node instanceof Ui.Container container) {
            delegated = true;
            List<Ui.Node> children = container.children();
            for (int i = children.size() - 1; i >= 0; i--) {
                if (release(children.get(i), x, y, button, childVisible)) return true;
            }
        }
        return delegated ? false : node.release(x, y, button);
    }
    private boolean drag(Ui.Node node, float x, float y, int button) {
        return drag(node, x, y, button, viewportBounds());
    }
    private boolean drag(Ui.Node node, float x, float y, int button, com.rethinkqaq.configui.core.UiBounds visible) {
        if (node == null) return false;
        boolean modal = node instanceof UiDialogHost dialogs && dialogs.showingDialog();
        if (visible.width() <= 0 || visible.height() <= 0 || !visible.contains(x, y)
            || (!modal && !visible.intersects(node.bounds()))) return false;
        if (modal) {
            UiDialogHost dialogs = (UiDialogHost) node;
            return dialogs.dialog() != null && drag(dialogs.dialog(), x, y, button, visible);
        }
        if (node instanceof UiScaffold scaffold) {
            return dispatchScaffoldDrag(scaffold, x, y, button, visible);
        }
        com.rethinkqaq.configui.core.UiBounds childVisible = childViewport(node, visible);
        boolean delegated = false;
        if (node instanceof Ui.Tooltip tooltip) {
            delegated = true;
            if (drag(tooltip.child(), x, y, button, childVisible)) return true;
        }
        if (node instanceof Ui.ChildProvider provider) {
            delegated = true;
            List<Ui.Node> children = provider.childNodes();
            for (int i = children.size() - 1; i >= 0; i--) {
                if (drag(children.get(i), x, y, button, childVisible)) return true;
            }
        }
        if (node instanceof Ui.Container container) {
            delegated = true;
            List<Ui.Node> children = container.children();
            for (int i = children.size() - 1; i >= 0; i--) {
                if (drag(children.get(i), x, y, button, childVisible)) return true;
            }
        }
        return delegated ? false : node.drag(x, y, button);
    }

    private boolean dispatchScaffoldClick(UiScaffold scaffold, float x, float y, int button,
                                           com.rethinkqaq.configui.core.UiBounds visible) {
        if (click(scaffold.footer(), x, y, button, visible)) return true;
        Ui.Node navigation = scaffold.navigationMode() == UiScaffold.NavigationMode.TOP
            ? scaffold.navigation() : scaffold.sidebar();
        if (click(navigation, x, y, button, visible)) return true;
        if (scaffold.headerVisible() && click(scaffold.header(), x, y, button, visible)) return true;
        return click(scaffold.content(), x, y, button, visible);
    }

    private boolean dispatchScaffoldScroll(UiScaffold scaffold, float x, float y, double amount,
                                           com.rethinkqaq.configui.core.UiBounds visible) {
        if (scroll(scaffold.footer(), x, y, amount, visible)) return true;
        Ui.Node navigation = scaffold.navigationMode() == UiScaffold.NavigationMode.TOP
            ? scaffold.navigation() : scaffold.sidebar();
        if (scroll(navigation, x, y, amount, visible)) return true;
        if (scaffold.headerVisible() && scroll(scaffold.header(), x, y, amount, visible)) return true;
        return scroll(scaffold.content(), x, y, amount, visible);
    }

    private boolean dispatchScaffoldRelease(UiScaffold scaffold, float x, float y, int button,
                                            com.rethinkqaq.configui.core.UiBounds visible) {
        if (release(scaffold.footer(), x, y, button, visible)) return true;
        Ui.Node navigation = scaffold.navigationMode() == UiScaffold.NavigationMode.TOP
            ? scaffold.navigation() : scaffold.sidebar();
        if (release(navigation, x, y, button, visible)) return true;
        if (scaffold.headerVisible() && release(scaffold.header(), x, y, button, visible)) return true;
        return release(scaffold.content(), x, y, button, visible);
    }

    private boolean dispatchScaffoldDrag(UiScaffold scaffold, float x, float y, int button,
                                         com.rethinkqaq.configui.core.UiBounds visible) {
        if (drag(scaffold.footer(), x, y, button, visible)) return true;
        Ui.Node navigation = scaffold.navigationMode() == UiScaffold.NavigationMode.TOP
            ? scaffold.navigation() : scaffold.sidebar();
        if (drag(navigation, x, y, button, visible)) return true;
        if (scaffold.headerVisible() && drag(scaffold.header(), x, y, button, visible)) return true;
        return drag(scaffold.content(), x, y, button, visible);
    }

    private Ui.Node findPointerCapture(Ui.Node node) {
        if (node == null || !node.visible()) return null;
        if (node instanceof UiDialogHost dialogs && dialogs.showingDialog()) {
            return dialogs.dialog() == null ? null : findPointerCapture(dialogs.dialog());
        }
        if (node instanceof UiScaffold scaffold) {
            Ui.Node capture = findPointerCapture(scaffold.footer());
            if (capture != null) return capture;
            Ui.Node navigation = scaffold.navigationMode() == UiScaffold.NavigationMode.TOP
                ? scaffold.navigation() : scaffold.sidebar();
            capture = findPointerCapture(navigation);
            if (capture != null) return capture;
            capture = findPointerCapture(scaffold.headerVisible() ? scaffold.header() : null);
            if (capture != null) return capture;
            return findPointerCapture(scaffold.content());
        }
        if (node instanceof Ui.Tooltip tooltip) {
            Ui.Node child = findPointerCapture(tooltip.child());
            if (child != null) return child;
        }
        if (node instanceof Ui.ChildProvider provider) {
            List<Ui.Node> children = provider.childNodes();
            for (int i = children.size() - 1; i >= 0; i--) {
                Ui.Node capture = findPointerCapture(children.get(i));
                if (capture != null) return capture;
            }
        }
        if (node instanceof Ui.Container container) {
            List<Ui.Node> children = container.children();
            for (int i = children.size() - 1; i >= 0; i--) {
                Ui.Node capture = findPointerCapture(children.get(i));
                if (capture != null) return capture;
            }
        }
        return node.capturesPointer() ? node : null;
    }
    private List<Ui.Node> focusable() {
        List<Ui.Node> result = new ArrayList<>();
        collect(root, result, viewportBounds()); return result;
    }
    private void collect(Ui.Node node, List<Ui.Node> result, com.rethinkqaq.configui.core.UiBounds visible) {
        if (node == null) return;
        if (node instanceof UiDialogHost dialogs && dialogs.showingDialog()) {
            if (dialogs.dialog() != null) collect(dialogs.dialog(), result, visible);
            return;
        }
        if (visible.width() <= 0 || visible.height() <= 0
            || !visible.intersects(node.bounds())) return;
        if (node.focusable()) result.add(node);
        if (node instanceof UiScaffold scaffold) {
            collect(scaffold.footer(), result, visible);
            Ui.Node navigation = scaffold.navigationMode() == UiScaffold.NavigationMode.TOP
                ? scaffold.navigation() : scaffold.sidebar();
            collect(navigation, result, visible);
            collect(scaffold.headerVisible() ? scaffold.header() : null, result, visible);
            collect(scaffold.content(), result, visible);
            return;
        }
        com.rethinkqaq.configui.core.UiBounds childVisible = childViewport(node, visible);
        if (node instanceof Ui.Tooltip tooltip) collect(tooltip.child(), result, childVisible);
        if (node instanceof Ui.ChildProvider provider) for (Ui.Node child : provider.childNodes()) collect(child, result, childVisible);
        if (node instanceof Ui.Container container) for (Ui.Node child : container.children()) collect(child, result, childVisible);
    }
    private java.util.Optional<Ui.Tooltip> hoveredTooltip(Ui.Node node, float x, float y) {
        return hoveredTooltip(node, viewportBounds(), x, y);
    }
    private java.util.Optional<Ui.Tooltip> hoveredTooltip(Ui.Node node,
                                                           com.rethinkqaq.configui.core.UiBounds visible,
                                                           float x, float y) {
        if (node == null) return java.util.Optional.empty();
        boolean modal = node instanceof UiDialogHost dialogs && dialogs.showingDialog();
        if (visible.width() <= 0 || visible.height() <= 0
            || (!modal && (!visible.intersects(node.bounds()) || !visible.contains(x, y)))) return java.util.Optional.empty();
        if (modal) {
            Ui.Node dialog = ((UiDialogHost) node).dialog();
            return dialog == null ? java.util.Optional.empty() : hoveredTooltip(dialog, visible, x, y);
        }
        if (node instanceof UiScaffold scaffold) {
            // Tooltip lookup follows the same z-order as pointer dispatch.  A clipped content
            // tooltip must not win over a fixed header, navigation or footer surface.
            Ui.Node footer = scaffold.footer();
            Ui.Node navigation = scaffold.navigationMode() == UiScaffold.NavigationMode.TOP
                ? scaffold.navigation() : scaffold.sidebar();
            Ui.Node header = scaffold.headerVisible() ? scaffold.header() : null;
            java.util.Optional<Ui.Tooltip> result = hoveredTooltipRegion(footer, x, y, visible);
            if (result.isPresent()) return result;
            result = hoveredTooltipRegion(navigation, x, y, visible);
            if (result.isPresent()) return result;
            result = hoveredTooltipRegion(header, x, y, visible);
            if (result.isPresent()) return result;
            return hoveredTooltipRegion(scaffold.content(), x, y, visible);
        }
        if (node instanceof Ui.Tooltip tooltip && tooltip.visible(System.nanoTime())
            && (tooltip.bounds().contains(x, y) || tooltip.child().focused())) return java.util.Optional.of(tooltip);
        if (node instanceof Ui.Tooltip tooltip) return hoveredTooltip(tooltip.child(), childViewport(node, visible), x, y);
        com.rethinkqaq.configui.core.UiBounds childVisible = childViewport(node, visible);
        if (node instanceof Ui.ChildProvider provider) {
            List<Ui.Node> children = provider.childNodes();
            for (int i = children.size() - 1; i >= 0; i--) {
                java.util.Optional<Ui.Tooltip> result = hoveredTooltip(children.get(i), childVisible, x, y);
                if (result.isPresent()) return result;
            }
        }
        if (node instanceof Ui.Container container) {
            List<Ui.Node> children = container.children();
            for (int i = children.size() - 1; i >= 0; i--) {
                java.util.Optional<Ui.Tooltip> result = hoveredTooltip(children.get(i), childVisible, x, y);
                if (result.isPresent()) return result;
            }
        }
        return java.util.Optional.empty();
    }
    private java.util.Optional<Ui.Tooltip> hoveredTooltipRegion(Ui.Node region, float x, float y,
                                                                 com.rethinkqaq.configui.core.UiBounds visible) {
        return region == null || !region.bounds().contains(x, y) || !visible.intersects(region.bounds())
            ? java.util.Optional.empty() : hoveredTooltip(region, visible, x, y);
    }
    private com.rethinkqaq.configui.core.UiBounds viewportBounds() {
        return new com.rethinkqaq.configui.core.UiBounds(0, 0, Math.max(0, width), Math.max(0, height));
    }
    private com.rethinkqaq.configui.core.UiBounds childViewport(Ui.Node node, com.rethinkqaq.configui.core.UiBounds visible) {
        return node instanceof Ui.ClipProvider clip ? visible.intersection(clip.viewportBounds()) : visible;
    }
    private void renderTooltip(UiRenderer renderer, Ui.Tooltip tooltip, int screenWidth, int screenHeight,
                               int mouseX, int mouseY) {
        UiTheme effectiveTheme = effectiveTheme();
        float padding = tooltip.padding(effectiveTheme);
        float screenMargin = 6;
        float maximum = Math.min(tooltip.maxWidth(), Math.max(1, screenWidth - screenMargin * 2));
        float width;
        float height;
        List<com.rethinkqaq.configui.core.UiText> lines = List.of();
        UiTooltipContent content = tooltip.content();
        if (content != null) {
            content.measure(renderer, Math.max(1, maximum - padding * 2),
            Math.min(tooltip.maxHeight(), Math.max(1, screenHeight - screenMargin * 2 - padding * 2)), effectiveTheme);
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
        renderer.pushOverlay();
        try {
            renderer.fillRoundRect(bounds, effectiveTheme.metrics().radius(), effectiveTheme.palette().control());
            renderer.strokeRoundRect(bounds, effectiveTheme.metrics().radius(), Math.min(1, effectiveTheme.metrics().borderWidth()), effectiveTheme.palette().border());
            if (content != null) {
                com.rethinkqaq.configui.core.UiBounds contentBounds = bounds.inset(padding);
                content.layout(renderer, contentBounds, effectiveTheme);
                renderer.pushClip(contentBounds);
                content.render(renderer, effectiveTheme);
                renderer.popClip();
            } else if (!lines.isEmpty()) {
                float lineHeight = renderer.lineHeight() + tooltip.lineGap();
                for (int index = 0; index < lines.size(); index++) {
                    renderer.drawText(lines.get(index), x + padding,
                        y + padding + index * lineHeight, effectiveTheme.palette().onAccent());
                }
            }
        } finally {
            renderer.popOverlay();
        }
    }
    private void focusAt(float x, float y) { setFocus(findFocusableAt(root, x, y, viewportBounds())); }
    private Ui.Node findFocusableAt(Ui.Node node, float x, float y,
                                    com.rethinkqaq.configui.core.UiBounds visible) {
        if (node == null) return null;
        if (node instanceof UiDialogHost dialogs && dialogs.showingDialog()) {
            return dialogs.dialog() == null ? null : findFocusableAt(dialogs.dialog(), x, y, visible);
        }
        if (visible.width() <= 0 || visible.height() <= 0
            || !visible.intersects(node.bounds()) || !visible.contains(x, y)) return null;
        if (node instanceof UiScaffold scaffold) {
            Ui.Node found = findFocusableInRegion(scaffold.footer(), x, y, visible);
            Ui.Node navigation = scaffold.navigationMode() == UiScaffold.NavigationMode.TOP
                ? scaffold.navigation() : scaffold.sidebar();
            if (found != null) return found;
            found = findFocusableInRegion(navigation, x, y, visible);
            if (found != null) return found;
            found = findFocusableInRegion(scaffold.headerVisible() ? scaffold.header() : null, x, y, visible);
            if (found != null) return found;
            return findFocusableInRegion(scaffold.content(), x, y, visible);
        }
        com.rethinkqaq.configui.core.UiBounds childVisible = childViewport(node, visible);
        if (node instanceof Ui.Tooltip tooltip) {
            Ui.Node found = findFocusableAt(tooltip.child(), x, y, childVisible);
            if (found != null) return found;
        }
        if (node instanceof Ui.ChildProvider provider) {
            List<Ui.Node> children = provider.childNodes();
            for (int i = children.size() - 1; i >= 0; i--) {
                Ui.Node found = findFocusableAt(children.get(i), x, y, childVisible);
                if (found != null) return found;
            }
        }
        if (node instanceof Ui.Container container) {
            List<Ui.Node> children = container.children();
            for (int i = children.size() - 1; i >= 0; i--) {
                Ui.Node found = findFocusableAt(children.get(i), x, y, childVisible);
                if (found != null) return found;
            }
        }
        return node.focusable() && node.bounds().contains(x, y) ? node : null;
    }
    private Ui.Node findFocusableInRegion(Ui.Node region, float x, float y,
                                          com.rethinkqaq.configui.core.UiBounds visible) {
        if (region == null || !region.bounds().contains(x, y)) return null;
        return findFocusableAt(region, x, y, visible);
    }
    private void moveFocus(boolean backwards) {
        List<Ui.Node> nodes = focusable(); if (nodes.isEmpty()) return;
        int index = focused == null ? (backwards ? 0 : -1) : nodes.indexOf(focused);
        setFocus(nodes.get(Math.floorMod(index + (backwards ? -1 : 1), nodes.size())));
    }
    private void setFocus(Ui.Node node) { if (focused == node) return; if (focused != null) focused.setFocused(false); focused = node; if (focused != null) focused.setFocused(true); }
    /** Dynamic collection rows are rebuilt after mutations; never dispatch another event to old nodes. */
    private void clearDetachedInputState() {
        com.rethinkqaq.configui.core.UiBounds visible = viewportBounds();
        if (focused != null && (!contains(root, focused) || !visibleToInput(root, focused, visible))) setFocus(null);
        if (pointerCapture != null && !contains(root, pointerCapture)) pointerCapture = null;
    }

    private void cancelPointerState(Ui.Node node) {
        if (node == null) return;
        node.cancelPointerState();
        if (node instanceof Ui.Tooltip tooltip) cancelPointerState(tooltip.child());
        if (node instanceof Ui.ChildProvider provider) for (Ui.Node child : provider.childNodes()) cancelPointerState(child);
        if (node instanceof Ui.Container container) for (Ui.Node child : container.children()) cancelPointerState(child);
    }
    private boolean visibleToInput(Ui.Node current, Ui.Node target,
                                   com.rethinkqaq.configui.core.UiBounds visible) {
        if (current == null || target == null || !current.visible() || !target.visible()) return false;
        if (current == target) return visible.intersects(current.bounds());
        if (current instanceof UiDialogHost dialogs && dialogs.showingDialog()) {
            return dialogs.dialog() != null && visibleToInput(dialogs.dialog(), target, visible);
        }
        if (visible.width() <= 0 || visible.height() <= 0 || !visible.intersects(current.bounds())) return false;
        com.rethinkqaq.configui.core.UiBounds childVisible = childViewport(current, visible);
        if (current instanceof Ui.Tooltip tooltip && visibleToInput(tooltip.child(), target, childVisible)) return true;
        if (current instanceof Ui.ChildProvider provider) {
            for (Ui.Node child : provider.childNodes()) if (visibleToInput(child, target, childVisible)) return true;
        }
        if (current instanceof Ui.Container container) {
            for (Ui.Node child : container.children()) if (visibleToInput(child, target, childVisible)) return true;
        }
        return false;
    }
    private boolean contains(Ui.Node current, Ui.Node target) {
        if (current == null || target == null) return false;
        if (current == target) return true;
        if (current instanceof Ui.Tooltip tooltip && contains(tooltip.child(), target)) return true;
        if (current instanceof Ui.ChildProvider provider) for (Ui.Node child : provider.childNodes()) if (contains(child, target)) return true;
        if (current instanceof Ui.Container container) for (Ui.Node child : container.children()) if (contains(child, target)) return true;
        return false;
    }
}
