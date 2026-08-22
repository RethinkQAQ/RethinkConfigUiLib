/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.core;

import java.util.Objects;

/**
 * A page frame with optional header, sidebar and footer. Content is the only required region;
 * absent regions reserve no space.
 */
public final class UiScaffold extends Ui.Node implements Ui.ChildProvider {
    private static final float COMPACT_WIDTH = 440;
    /** Selects which navigation region is active for this frame. */
    public enum NavigationMode { SIDEBAR, TOP }

    private final Ui.Node content;
    private Ui.Node header;
    private Ui.Node navigation;
    private Ui.Node sidebar;
    private Ui.Node footer;
    private NavigationMode navigationMode = NavigationMode.SIDEBAR;
    private float sidebarWidth = 132;
    private float regionGap = -1;
    private float maxContentWidth = 1200;
    private boolean compact;
    private float headerHeight, navigationHeight, sidebarHeight, footerHeight;

    UiScaffold(Ui.Node content) { this.content = Objects.requireNonNull(content, "content"); }
    public UiScaffold header(Ui.Node value) { header = Objects.requireNonNull(value, "header"); invalidateLayout(); return this; }
    /** Sets the category/navigation node used by {@link NavigationMode#TOP}. */
    public UiScaffold navigation(Ui.Node value) { navigation = Objects.requireNonNull(value, "navigation"); invalidateLayout(); return this; }
    public UiScaffold navigationMode(NavigationMode value) { navigationMode = Objects.requireNonNull(value, "navigationMode"); invalidateLayout(); return this; }
    public UiScaffold sidebar(Ui.Node value) { sidebar = Objects.requireNonNull(value, "sidebar"); invalidateLayout(); return this; }
    public UiScaffold footer(Ui.Node value) { footer = Objects.requireNonNull(value, "footer"); invalidateLayout(); return this; }
    public UiScaffold sidebarWidth(float value) {
        if (value <= 0) throw new IllegalArgumentException("sidebar width must be positive");
        sidebarWidth = value;
        invalidateLayout();
        return this;
    }
    /** Gap between page regions; defaults to the active theme spacing token. */
    public UiScaffold regionGap(float value) {
        if (value < 0) throw new IllegalArgumentException("region gap must be non-negative");
        regionGap = value;
        invalidateLayout();
        return this;
    }
    /** Caps the responsive shell on very wide displays; zero opts out of the cap. */
    public UiScaffold maxContentWidth(float value) {
        if (value < 0) throw new IllegalArgumentException("maximum content width must be non-negative");
        maxContentWidth = value;
        invalidateLayout();
        return this;
    }
    public Ui.Node content() { return content; }
    public Ui.Node navigation() { return navigation; }
    public NavigationMode navigationMode() { return navigationMode; }
    @Override public java.util.List<Ui.Node> childNodes() {
        java.util.ArrayList<Ui.Node> result = new java.util.ArrayList<>();
        if (header != null) result.add(header);
        if (navigationMode == NavigationMode.TOP) {
            if (navigation != null) result.add(navigation);
        } else if (sidebar != null) {
            result.add(sidebar);
        }
        result.add(content);
        if (footer != null) result.add(footer);
        return result;
    }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float shellWidth = shellWidth(maxWidth);
        compact = shellWidth < COMPACT_WIDTH;
        float gap = gap(theme);
        headerHeight = measureHeight(header, renderer, shellWidth, maxHeight, theme);
        footerHeight = measureHeight(footer, renderer, shellWidth, maxHeight, theme);
        navigationHeight = navigationMode == NavigationMode.TOP
            ? measureHeight(navigation, renderer, shellWidth, maxHeight, theme) : 0;
        float verticalGaps = (header == null ? 0 : gap)
            + (navigationMode == NavigationMode.TOP && navigation != null ? gap : 0)
            + (footer == null ? 0 : gap);
        float bodyHeight = Math.max(0, maxHeight - headerHeight - footerHeight - verticalGaps);

        if (navigationMode == NavigationMode.TOP) {
            sidebarHeight = 0;
            content.measure(renderer, shellWidth, Math.max(0, bodyHeight - navigationHeight), theme);
        } else if (sidebar == null) {
            content.measure(renderer, shellWidth, bodyHeight, theme);
            sidebarHeight = 0;
        } else if (compact) {
            sidebar.measure(renderer, shellWidth, bodyHeight, theme);
            sidebarHeight = sidebar.measuredHeight();
            content.measure(renderer, shellWidth, Math.max(0, bodyHeight - sidebarHeight - gap), theme);
        } else {
            float actualSidebarWidth = Math.min(sidebarWidth, shellWidth * .4f);
            sidebar.measure(renderer, actualSidebarWidth, bodyHeight, theme);
            sidebarHeight = bodyHeight;
            content.measure(renderer, Math.max(0, shellWidth - actualSidebarWidth - gap), bodyHeight, theme);
        }
        measuredWidth = shellWidth;
        measuredHeight = maxHeight;
    }

    @Override
    public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        float shellWidth = shellWidth(value.width());
        float x = value.x() + (value.width() - shellWidth) / 2f;
        float gap = gap(theme);
        float y = value.y();
        if (header != null) {
            header.layout(renderer, new UiBounds(x, y, shellWidth, headerHeight), theme);
            y += headerHeight + gap;
        }
        if (navigationMode == NavigationMode.TOP && navigation != null) {
            navigation.layout(renderer, new UiBounds(x, y, shellWidth, navigationHeight), theme);
            y += navigationHeight + gap;
        }
        float bodyHeight = Math.max(0, value.height() - headerHeight - footerHeight
            - (header == null ? 0 : gap)
            - (navigationMode == NavigationMode.TOP && navigation != null ? navigationHeight + gap : 0)
            - (footer == null ? 0 : gap));
        if (navigationMode == NavigationMode.TOP || sidebar == null) {
            content.layout(renderer, new UiBounds(x, y, shellWidth, bodyHeight), theme);
        } else if (compact) {
            sidebar.layout(renderer, new UiBounds(x, y, shellWidth, sidebarHeight), theme);
            content.layout(renderer, new UiBounds(x, y + sidebarHeight + gap, shellWidth, Math.max(0, bodyHeight - sidebarHeight - gap)), theme);
        } else {
            float actualSidebarWidth = Math.min(sidebarWidth, shellWidth * .4f);
            sidebar.layout(renderer, new UiBounds(x, y, actualSidebarWidth, bodyHeight), theme);
            content.layout(renderer, new UiBounds(x + actualSidebarWidth + gap, y,
                Math.max(0, shellWidth - actualSidebarWidth - gap), bodyHeight), theme);
        }
        if (footer != null) footer.layout(renderer, new UiBounds(x, value.y() + value.height() - footerHeight,
            shellWidth, footerHeight), theme);
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        if (header != null) header.render(renderer, theme);
        if (navigationMode == NavigationMode.TOP) {
            if (navigation != null) navigation.render(renderer, theme);
        } else if (sidebar != null) {
            sidebar.render(renderer, theme);
        }
        content.render(renderer, theme);
        if (footer != null) footer.render(renderer, theme);
    }

    @Override public boolean click(float x, float y, int button) {
        return (footer != null && footer.click(x, y, button)) || content.click(x, y, button)
            || (navigationMode == NavigationMode.TOP && navigation != null && navigation.click(x, y, button))
            || (navigationMode == NavigationMode.SIDEBAR && sidebar != null && sidebar.click(x, y, button))
            || (header != null && header.click(x, y, button));
    }
    @Override public boolean scroll(float x, float y, double amount) {
        return content.scroll(x, y, amount)
            || (navigationMode == NavigationMode.SIDEBAR && sidebar != null && sidebar.scroll(x, y, amount));
    }
    @Override public boolean key(int keyCode) {
        return content.key(keyCode)
            || (navigationMode == NavigationMode.TOP && navigation != null && navigation.key(keyCode))
            || (navigationMode == NavigationMode.SIDEBAR && sidebar != null && sidebar.key(keyCode));
    }

    private static float measureHeight(Ui.Node node, UiRenderer renderer, float width, float height, UiTheme theme) {
        if (node == null) return 0;
        node.measure(renderer, width, height, theme);
        return node.measuredHeight();
    }
    private float shellWidth(float availableWidth) { return maxContentWidth == 0 ? availableWidth : Math.min(availableWidth, maxContentWidth); }
    private float gap(UiTheme theme) { return regionGap < 0 ? theme.metrics().spacing() : regionGap; }
}
