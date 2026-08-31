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

package com.rethinkqaq.configui.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Root-level overlay owner for dialogs. The active dialog receives all input before the page
 * below it, so dialogs do not need to depend on a particular Screen implementation.
 */
public final class UiDialogHost extends Ui.Node implements Ui.ChildProvider, Ui.ClipProvider {
    private Ui.Node root;
    private Ui.Node dialog;
    private UiBounds viewport = UiBounds.EMPTY;

    public UiDialogHost() { }
    public UiDialogHost(Ui.Node root) { root(root); }

    /** Installs the normal page tree before the host is handed to a platform adapter. */
    public UiDialogHost root(Ui.Node value) {
        root = Objects.requireNonNull(value, "root");
        invalidateLayout();
        return this;
    }

    public Ui.Node root() { return root; }
    public Ui.Node dialog() { return dialog; }
    public boolean showingDialog() { return dialog != null; }

    /**
     * Supplies the host's complete logical viewport. Platform adapters call this each frame so a
     * modal backdrop covers the whole GUI, not merely the normal page's safe-area bounds.
     */
    public void viewport(UiBounds value) {
        Objects.requireNonNull(value, "viewport");
        if (!viewport.equals(value)) {
            viewport = value;
            invalidateLayout();
        }
    }

    @Override
    public UiBounds viewportBounds() {
        return viewport.width() > 0 && viewport.height() > 0 ? viewport : bounds;
    }

    public void show(Ui.Node value) {
        if (dialog != null) dialog.cancelPointerState();
        dialog = Objects.requireNonNull(value, "dialog");
        invalidateLayout();
    }

    public boolean close() {
        if (dialog == null) return false;
        dialog = null;
        invalidateLayout();
        return true;
    }

    @Override public List<Ui.Node> childNodes() {
        List<Ui.Node> children = new ArrayList<>(2);
        if (root != null) children.add(root);
        if (dialog != null) children.add(dialog);
        return List.copyOf(children);
    }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        if (root == null) {
            measuredWidth = maxWidth;
            measuredHeight = maxHeight;
            return;
        }
        root.measure(renderer, maxWidth, maxHeight, theme);
        measuredWidth = Math.min(maxWidth, root.measuredWidth());
        measuredHeight = Math.min(maxHeight, root.measuredHeight());
        if (dialog != null) measureDialog(renderer, maxWidth, maxHeight, theme);
    }

    @Override public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        if (root != null) root.layout(renderer, value, theme);
        if (dialog == null) return;
        UiBounds area = viewport.width() > 0 && viewport.height() > 0 ? viewport : value;
        measureDialog(renderer, area.width(), area.height(), theme);
        float width = Math.min(area.width(), dialog.measuredWidth());
        float height = Math.min(area.height(), dialog.measuredHeight());
        dialog.layout(renderer, new UiBounds(area.x() + (area.width() - width) / 2f,
            area.y() + (area.height() - height) / 2f, width, height), theme);
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) {
        if (root != null) root.render(renderer, theme);
        if (dialog == null) return;
        UiBounds area = viewport.width() > 0 && viewport.height() > 0 ? viewport : bounds;
        renderer.fillRoundRect(area, 0, 0x66000000);
        dialog.render(renderer, theme);
    }

    @Override public boolean click(float x, float y, int button) {
        if (dialog != null) {
            UiBounds area = viewport.width() > 0 && viewport.height() > 0 ? viewport : bounds;
            // The backdrop consumes outside clicks, but dialog controls must only receive
            // events while the pointer is inside the dialog surface itself.
            return (dialog.bounds().contains(x, y) && dialog.click(x, y, button)) || area.contains(x, y);
        }
        return root != null && root.click(x, y, button);
    }
    @Override public boolean scroll(float x, float y, double amount) {
        return dialog != null
            ? dialog.bounds().contains(x, y) && dialog.scroll(x, y, amount)
            : root != null && root.scroll(x, y, amount);
    }
    @Override public boolean drag(float x, float y, int button) {
        return dialog != null
            ? dialog.bounds().contains(x, y) && dialog.drag(x, y, button)
            : root != null && root.drag(x, y, button);
    }
    @Override public boolean release(float x, float y, int button) {
        return dialog != null
            ? dialog.bounds().contains(x, y) && dialog.release(x, y, button)
            : root != null && root.release(x, y, button);
    }
    @Override public boolean key(int keyCode) {
        if (dialog != null) return (keyCode == UiKey.ESCAPE && close()) || dialog.key(keyCode);
        return root != null && root.key(keyCode);
    }
    @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) {
        if (dialog != null) return (event.keyCode() == UiKey.ESCAPE && close()) || dialog.key(event, clipboard);
        return root != null && root.key(event, clipboard);
    }
    @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) {
        return dialog != null ? dialog.textInput(event, clipboard) : root != null && root.textInput(event, clipboard);
    }

    private void measureDialog(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float width = Math.min(560, Math.max(160, maxWidth * .82f));
        float height = Math.max(theme.metrics().controlHeight() * 3f, maxHeight * .72f);
        dialog.measure(renderer, width, height, theme);
    }
}
