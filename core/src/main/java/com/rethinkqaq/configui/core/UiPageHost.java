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
 * Hosts a set of arbitrary UI pages behind one reusable category navigation bar.
 * Pages are wrapped in a scroll viewport so switching pages never requires a
 * platform-specific screen implementation.
 */
public final class UiPageHost extends Ui.Node implements Ui.ChildProvider, Ui.ClipProvider {
    private final List<Page> pages = new ArrayList<>();
    private final List<Page> subpages = new ArrayList<>();
    private UiNavigationBar navigation;
    private int selectedIndex = -1;

    public UiPageHost addPage(UiText title, Ui.Node content) {
        pages.add(new Page(Objects.requireNonNull(title, "title"), scrollContent(content)));
        if (selectedIndex < 0) selectedIndex = 0;
        invalidateLayout();
        return this;
    }

    public int pageCount() { return pages.size(); }
    public int selectedIndex() { return selectedIndex; }
    public UiText pageTitle(int index) { return pages.get(index).title(); }
    public Ui.Node currentPage() { return currentContent(); }
    public boolean showingSubpage() { return !subpages.isEmpty(); }

    /** Opens a temporary second-level page. {@link #pop()} returns to the selected category page. */
    public UiPageHost push(UiText title, Ui.Node content) {
        subpages.add(new Page(Objects.requireNonNull(title, "title"), scrollContent(content)));
        invalidateLayout();
        return this;
    }

    /** Closes the current second-level page, if one is open. */
    public boolean pop() {
        if (subpages.isEmpty()) return false;
        subpages.remove(subpages.size() - 1);
        invalidateLayout();
        return true;
    }

    /** Returns a stable navigation node which can be attached to {@link UiScaffold}. */
    public UiNavigationBar navigation() {
        if (navigation == null) navigation = new UiNavigationBar(this);
        return navigation;
    }

    public UiPageHost select(int index) {
        if (pages.isEmpty()) {
            selectedIndex = -1;
            return this;
        }
        int next = Math.max(0, Math.min(index, pages.size() - 1));
        if (next != selectedIndex) {
            selectedIndex = next;
            subpages.clear();
            pages.get(selectedIndex).content().reset();
            invalidateLayout();
        }
        return this;
    }

    @Override public List<Ui.Node> childNodes() {
        Ui.ScrollView content = currentContent();
        return content == null ? List.of() : List.of(content);
    }

    @Override public UiBounds viewportBounds() {
        Ui.ScrollView content = currentContent();
        return content == null ? UiBounds.EMPTY : content.bounds();
    }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        if (selectedIndex < 0) {
            measuredWidth = maxWidth;
            measuredHeight = maxHeight;
            return;
        }
        Ui.ScrollView page = currentContent();
        page.measure(renderer, maxWidth, maxHeight, theme);
        measuredWidth = Math.min(maxWidth, page.measuredWidth());
        measuredHeight = Math.min(maxHeight, page.measuredHeight());
    }

    @Override public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        Ui.ScrollView content = currentContent();
        if (content != null) content.layout(renderer, value, theme);
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) {
        Ui.ScrollView content = currentContent();
        if (content != null) content.render(renderer, theme);
    }

    @Override public boolean click(float x, float y, int button) {
        Ui.ScrollView content = currentContent();
        return content != null && content.click(x, y, button);
    }
    @Override public boolean scroll(float x, float y, double amount) {
        Ui.ScrollView content = currentContent();
        return content != null && content.scroll(x, y, amount);
    }
    @Override public boolean drag(float x, float y, int button) {
        Ui.ScrollView content = currentContent();
        return content != null && content.drag(x, y, button);
    }
    @Override public boolean release(float x, float y, int button) {
        Ui.ScrollView content = currentContent();
        return content != null && content.release(x, y, button);
    }
    @Override public boolean key(int keyCode) {
        if (keyCode == UiKey.ESCAPE && pop()) return true;
        Ui.ScrollView content = currentContent();
        return content != null && content.key(keyCode);
    }
    @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) {
        if (event.keyCode() == UiKey.ESCAPE && pop()) return true;
        Ui.ScrollView content = currentContent();
        return content != null && content.key(event, clipboard);
    }
    @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) {
        Ui.ScrollView content = currentContent();
        return content != null && content.textInput(event, clipboard);
    }

    private Ui.ScrollView currentContent() {
        if (!subpages.isEmpty()) return subpages.get(subpages.size() - 1).content();
        return selectedIndex < 0 ? null : pages.get(selectedIndex).content();
    }

    private static Ui.ScrollView scrollContent(Ui.Node content) {
        Ui.Node node = Objects.requireNonNull(content, "content");
        return node instanceof Ui.ScrollView scroll ? scroll : Ui.scrollView(node);
    }

    private record Page(UiText title, Ui.ScrollView content) { }
}
