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

import com.rethinkqaq.configui.core.layout.UiHeader;
import com.rethinkqaq.configui.core.layout.UiHeaderStyle;

import java.util.Objects;

/**
 * A page frame with optional header, sidebar and footer. Content is the only required region;
 * absent regions reserve no space.
 */
public final class UiScaffold extends Ui.Node implements Ui.ChildProvider, Ui.ClipProvider, UiPageRoot {
    private static final float HEADER_HIDE_WIDTH = 360;
    private static final float COMPACT_WIDTH = 760;
    /** Selects which navigation region is active for this frame. */
    public enum NavigationMode { SIDEBAR, TOP }

    private final Ui.Node content;
    private Ui.Node header;
    private Ui.Node navigation;
    private Ui.Node sidebar;
    private Ui.Node footer;
    private UiBackground background = UiBackground.transparent();
    private NavigationMode navigationMode = NavigationMode.SIDEBAR;
    private float sidebarWidth = 132;
    private float regionGap = -1;
    private float maxContentWidth = 1200;
    private boolean compact;
    private boolean headerVisible;
    private float headerHeight, navigationHeight, sidebarHeight, footerHeight;

    UiScaffold(Ui.Node content) { this.content = Objects.requireNonNull(content, "content"); }

    @Override
    public UiBounds viewportBounds() { return bounds(); }
    public UiScaffold header(Ui.Node value) { header = Objects.requireNonNull(value, "header"); invalidateLayout(); return this; }
    /** Sets the category/navigation node used by {@link NavigationMode#TOP}. */
    public UiScaffold navigation(Ui.Node value) { navigation = Objects.requireNonNull(value, "navigation"); invalidateLayout(); return this; }
    public UiScaffold navigationMode(NavigationMode value) { navigationMode = Objects.requireNonNull(value, "navigationMode"); invalidateLayout(); return this; }
    public UiScaffold sidebar(Ui.Node value) { sidebar = Objects.requireNonNull(value, "sidebar"); invalidateLayout(); return this; }
    public UiScaffold footer(Ui.Node value) { footer = Objects.requireNonNull(value, "footer"); invalidateLayout(); return this; }
    /** Sets an optional surface painted across this complete page frame. Defaults to transparent. */
    public UiScaffold background(UiBackground value) { background = Objects.requireNonNull(value, "background"); return this; }
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
    public Ui.Node header() { return header; }
    public Ui.Node navigation() { return navigation; }
    public Ui.Node sidebar() { return sidebar; }
    public Ui.Node footer() { return footer; }
    public UiBackground background() { return background; }
    public NavigationMode navigationMode() { return navigationMode; }
    /** Returns whether the optional header is present at the current logical width. */
    public boolean headerVisible() { return headerVisible; }
    @Override public java.util.List<Ui.Node> childNodes() {
        java.util.ArrayList<Ui.Node> result = new java.util.ArrayList<>();
        if (headerVisible) result.add(header);
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
        // Compact text headers are intentionally retained at narrow logical widths.  They are
        // the primary title treatment for UiTemplate and have no card surface to consume space.
        boolean headerInRange = header != null && (shellWidth >= HEADER_HIDE_WIDTH
            || (header instanceof UiHeader uiHeader && uiHeader.style() == UiHeaderStyle.TEXT));
        headerHeight = headerInRange ? measureHeight(header, renderer, shellWidth, maxHeight, theme) : 0;
        headerVisible = headerInRange && headerHeight > 0;
        float effectiveHeaderHeight = headerVisible ? headerHeight : 0;
        footerHeight = measureHeight(footer, renderer, shellWidth, maxHeight, theme);
        navigationHeight = navigationMode == NavigationMode.TOP
            ? measureHeight(navigation, renderer, shellWidth, maxHeight, theme) : 0;
        float verticalGaps = (!headerVisible ? 0 : gap)
            + (navigationMode == NavigationMode.TOP && navigation != null ? navigationHeight + gap : 0)
            + (footer == null ? 0 : gap);
        float bodyHeight = Math.max(0, maxHeight - effectiveHeaderHeight - footerHeight - verticalGaps);

        if (navigationMode == NavigationMode.TOP) {
            sidebarHeight = 0;
            content.measure(renderer, shellWidth, bodyHeight, theme);
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
        if (headerVisible) {
            header.layout(renderer, new UiBounds(x, y, shellWidth, headerHeight), theme);
            y += headerHeight + gap;
        }
        if (navigationMode == NavigationMode.TOP && navigation != null) {
            navigation.layout(renderer, new UiBounds(x, y, shellWidth, navigationHeight), theme);
            y += navigationHeight + gap;
        }
        float effectiveHeaderHeight = headerVisible ? headerHeight : 0;
        float bodyHeight = Math.max(0, value.height() - effectiveHeaderHeight - footerHeight
            - (!headerVisible ? 0 : gap)
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
        if (background.paintsSurface()) renderer.fillRect(bounds(), background.color());
        if (headerVisible) header.render(renderer, theme);
        if (navigationMode == NavigationMode.TOP) {
            if (navigation != null) navigation.render(renderer, theme);
        } else if (sidebar != null) {
            sidebar.render(renderer, theme);
        }
        // The content region is a fixed viewport in addition to any scroll viewport owned by
        // the content itself. Platform preview nodes must not draw into header or footer bands.
        renderer.pushClip(content.bounds());
        try {
            content.render(renderer, theme);
        } finally {
            renderer.popClip();
        }
        // Footer is fixed chrome and remains in the normal synchronous render order.
        if (footer != null) footer.render(renderer, theme);
    }

    @Override public boolean click(float x, float y, int button) {
        if (footer != null && footer.bounds().contains(x, y) && footer.click(x, y, button)) return true;
        if (navigationMode == NavigationMode.TOP && navigation != null
            && navigation.bounds().contains(x, y) && navigation.click(x, y, button)) return true;
        if (navigationMode == NavigationMode.SIDEBAR && sidebar != null
            && sidebar.bounds().contains(x, y) && sidebar.click(x, y, button)) return true;
        if (headerVisible && header.bounds().contains(x, y) && header.click(x, y, button)) return true;
        return content.bounds().contains(x, y) && content.click(x, y, button);
    }
    @Override public boolean scroll(float x, float y, double amount) {
        // Route wheel input only to the region under the pointer.  Without this guard a
        // scrollable content node could consume wheel events while the pointer was over the
        // header/navigation, making fixed regions unexpectedly move.
        if (footer != null && footer.bounds().contains(x, y) && footer.scroll(x, y, amount)) return true;
        if (navigationMode == NavigationMode.TOP && navigation != null
            && navigation.bounds().contains(x, y) && navigation.scroll(x, y, amount)) return true;
        if (navigationMode == NavigationMode.SIDEBAR && sidebar != null
            && sidebar.bounds().contains(x, y) && sidebar.scroll(x, y, amount)) return true;
        return content.bounds().contains(x, y) && content.scroll(x, y, amount);
    }
    @Override public boolean drag(float x, float y, int button) {
        if (footer != null && footer.bounds().contains(x, y) && footer.drag(x, y, button)) return true;
        if (navigationMode == NavigationMode.TOP && navigation != null
            && navigation.bounds().contains(x, y) && navigation.drag(x, y, button)) return true;
        if (navigationMode == NavigationMode.SIDEBAR && sidebar != null
            && sidebar.bounds().contains(x, y) && sidebar.drag(x, y, button)) return true;
        if (headerVisible && header.bounds().contains(x, y) && header.drag(x, y, button)) return true;
        return content.bounds().contains(x, y) && content.drag(x, y, button);
    }
    @Override public boolean release(float x, float y, int button) {
        if (footer != null && footer.bounds().contains(x, y) && footer.release(x, y, button)) return true;
        if (navigationMode == NavigationMode.TOP && navigation != null
            && navigation.bounds().contains(x, y) && navigation.release(x, y, button)) return true;
        if (navigationMode == NavigationMode.SIDEBAR && sidebar != null
            && sidebar.bounds().contains(x, y) && sidebar.release(x, y, button)) return true;
        if (headerVisible && header.bounds().contains(x, y) && header.release(x, y, button)) return true;
        return content.bounds().contains(x, y) && content.release(x, y, button);
    }
    @Override public boolean key(int keyCode) {
        return (footer != null && footer.key(keyCode))
            || (navigationMode == NavigationMode.TOP && navigation != null && navigation.key(keyCode))
            || (navigationMode == NavigationMode.SIDEBAR && sidebar != null && sidebar.key(keyCode))
            || content.key(keyCode);
    }
    @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) {
        return (footer != null && footer.key(event, clipboard))
            || (navigationMode == NavigationMode.TOP && navigation != null && navigation.key(event, clipboard))
            || (navigationMode == NavigationMode.SIDEBAR && sidebar != null && sidebar.key(event, clipboard))
            || content.key(event, clipboard);
    }
    @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) {
        return (footer != null && footer.textInput(event, clipboard))
            || (navigationMode == NavigationMode.TOP && navigation != null && navigation.textInput(event, clipboard))
            || (navigationMode == NavigationMode.SIDEBAR && sidebar != null && sidebar.textInput(event, clipboard))
            || content.textInput(event, clipboard);
    }

    private static float measureHeight(Ui.Node node, UiRenderer renderer, float width, float height, UiTheme theme) {
        if (node == null) return 0;
        node.measure(renderer, width, height, theme);
        return node.measuredHeight();
    }
    private float shellWidth(float availableWidth) { return maxContentWidth == 0 ? availableWidth : Math.min(availableWidth, maxContentWidth); }
    private float gap(UiTheme theme) {
        float density = theme.metrics().controlHeight() <= 24.01f ? .5f : Math.min(1f,
            theme.metrics().controlHeight() / UiTheme.UiMetrics.comfortable().controlHeight());
        return (regionGap < 0 ? theme.metrics().spacing() : regionGap) * density;
    }
}
