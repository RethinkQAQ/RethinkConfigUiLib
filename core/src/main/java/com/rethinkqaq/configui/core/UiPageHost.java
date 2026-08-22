/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
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
public final class UiPageHost extends Ui.Node implements Ui.ChildProvider {
    private final List<Page> pages = new ArrayList<>();
    private UiNavigationBar navigation;
    private int selectedIndex = -1;

    public UiPageHost addPage(UiText title, Ui.Node content) {
        pages.add(new Page(Objects.requireNonNull(title, "title"), Ui.scrollView(Objects.requireNonNull(content, "content"))));
        if (selectedIndex < 0) selectedIndex = 0;
        invalidateLayout();
        return this;
    }

    public int pageCount() { return pages.size(); }
    public int selectedIndex() { return selectedIndex; }
    public UiText pageTitle(int index) { return pages.get(index).title(); }
    public Ui.Node currentPage() { return selectedIndex < 0 ? null : pages.get(selectedIndex).content(); }

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
            pages.get(selectedIndex).content().reset();
            invalidateLayout();
        }
        return this;
    }

    @Override public List<Ui.Node> childNodes() {
        return selectedIndex < 0 ? List.of() : List.of(pages.get(selectedIndex).content());
    }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        if (selectedIndex < 0) {
            measuredWidth = maxWidth;
            measuredHeight = maxHeight;
            return;
        }
        Ui.Node page = pages.get(selectedIndex).content();
        page.measure(renderer, maxWidth, maxHeight, theme);
        measuredWidth = Math.min(maxWidth, page.measuredWidth());
        measuredHeight = Math.min(maxHeight, page.measuredHeight());
    }

    @Override public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        if (selectedIndex >= 0) pages.get(selectedIndex).content().layout(renderer, value, theme);
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) {
        if (selectedIndex >= 0) pages.get(selectedIndex).content().render(renderer, theme);
    }

    @Override public boolean click(float x, float y, int button) {
        return selectedIndex >= 0 && pages.get(selectedIndex).content().click(x, y, button);
    }
    @Override public boolean scroll(float x, float y, double amount) {
        return selectedIndex >= 0 && pages.get(selectedIndex).content().scroll(x, y, amount);
    }
    @Override public boolean drag(float x, float y, int button) {
        return selectedIndex >= 0 && pages.get(selectedIndex).content().drag(x, y, button);
    }
    @Override public boolean release(float x, float y, int button) {
        return selectedIndex >= 0 && pages.get(selectedIndex).content().release(x, y, button);
    }
    @Override public boolean key(int keyCode) {
        return selectedIndex >= 0 && pages.get(selectedIndex).content().key(keyCode);
    }

    private record Page(UiText title, Ui.ScrollView content) { }
}
