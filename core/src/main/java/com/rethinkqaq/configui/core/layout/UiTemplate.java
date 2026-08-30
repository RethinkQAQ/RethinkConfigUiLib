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

package com.rethinkqaq.configui.core.layout;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBackground;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiClipboard;
import com.rethinkqaq.configui.core.UiKeyEvent;
import com.rethinkqaq.configui.core.UiMainAxisAlignment;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiPageHost;
import com.rethinkqaq.configui.core.UiScaffold;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTextInput;
import com.rethinkqaq.configui.core.UiTheme;

import java.util.List;
import java.util.Objects;

/**
 * A small, reusable page template for configuration screens. The default layout is
 * header, top navigation, independently scrolling content and an optional footer.
 */
public final class UiTemplate extends Ui.Node implements Ui.ChildProvider {
    private final Ui.Node composed;
    private final UiBackground background;
    private final Slots slots;
    private final Options options;

    private UiTemplate(Ui.Node composed, Slots slots, Options options) {
        this.composed = Objects.requireNonNull(composed, "composed layout");
        this.slots = slots;
        this.options = options;
        this.background = options.background();
    }

    public static Builder template() { return new Builder(TOP_NAVIGATION); }
    public static Builder topNavigation() { return new Builder(TOP_NAVIGATION); }

    public UiBackground background() { return background; }
    public Slots slots() { return slots; }
    public Options options() { return options; }

    @Override public List<Ui.Node> childNodes() { return List.of(composed); }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        composed.measure(renderer, maxWidth, maxHeight, theme);
        measuredWidth = composed.measuredWidth();
        measuredHeight = composed.measuredHeight();
    }

    @Override public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        composed.layout(renderer, value, theme);
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) { composed.render(renderer, theme); }
    @Override public boolean click(float x, float y, int button) { return composed.click(x, y, button); }
    @Override public boolean scroll(float x, float y, double amount) { return composed.scroll(x, y, amount); }
    @Override public boolean drag(float x, float y, int button) { return composed.drag(x, y, button); }
    @Override public boolean release(float x, float y, int button) { return composed.release(x, y, button); }
    @Override public boolean key(int keyCode) { return composed.key(keyCode); }
    @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) { return composed.key(event, clipboard); }
    @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) {
        return composed.textInput(event, clipboard);
    }

    /** Immutable region references passed to a template layout. */
    public record Slots(Ui.Node header, Ui.Node navigation, Ui.Node content, Ui.Node footer) {
        public Slots {
            Objects.requireNonNull(content, "content");
        }
    }

    /** Immutable layout options passed to a template layout. */
    public record Options(
        UiBackground background,
        float maxContentWidth,
        float regionGap,
        boolean scrollContent,
        UiMainAxisAlignment footerAlignment,
        boolean footerDivider
    ) {
        public Options {
            if (maxContentWidth < 0) throw new IllegalArgumentException("maximum content width must be non-negative");
            if (regionGap < -1) throw new IllegalArgumentException("region gap must be non-negative");
            Objects.requireNonNull(footerAlignment, "footerAlignment");
        }
    }

    public static final class Builder {
        private final UiTemplateLayout layout;
        private Ui.Node header;
        private Ui.Node navigation;
        private Ui.Node content;
        private Ui.Node footer;
        private UiBackground background;
        private float maxContentWidth = 1200;
        private float regionGap = -1;
        private boolean scrollContent = true;
        private UiMainAxisAlignment footerAlignment = UiMainAxisAlignment.END;
        // The footer is separated by layout and surface contrast by default. Consumers can
        // opt into an explicit divider with footerDivider(true).
        private boolean footerDivider = false;

        private Builder(UiTemplateLayout layout) { this.layout = Objects.requireNonNull(layout, "layout"); }

        public Builder header(UiText title, UiHeaderStyle style) {
            header = UiHeader.builder(Objects.requireNonNull(title, "title"))
                .style(Objects.requireNonNull(style, "style")).build();
            return this;
        }
        public Builder header(UiText title) { return header(title, UiHeaderStyle.TEXT); }
        public Builder header(Ui.Node value) { header = Objects.requireNonNull(value, "header"); return this; }
        public Builder navigation(Ui.Node value) { navigation = Objects.requireNonNull(value, "navigation"); return this; }
        public Builder content(Ui.Node value) { content = Objects.requireNonNull(value, "content"); return this; }
        public Builder footer(Ui.Node value) { footer = Objects.requireNonNull(value, "footer"); return this; }
        public Builder background(UiBackground value) { background = Objects.requireNonNull(value, "background"); return this; }
        public Builder maxContentWidth(float value) {
            if (value < 0) throw new IllegalArgumentException("maximum content width must be non-negative");
            maxContentWidth = value;
            return this;
        }
        public Builder regionGap(float value) {
            if (value < 0) throw new IllegalArgumentException("region gap must be non-negative");
            regionGap = value;
            return this;
        }
        public Builder scrollContent(boolean value) { scrollContent = value; return this; }
        public Builder footerAlignment(UiMainAxisAlignment value) {
            footerAlignment = Objects.requireNonNull(value, "footerAlignment");
            return this;
        }
        public Builder footerDivider(boolean value) { footerDivider = value; return this; }
        public Builder layout(UiTemplateLayout value) {
            return new Builder(Objects.requireNonNull(value, "layout"))
                .copyFrom(this);
        }

        private Builder copyFrom(Builder source) {
            header = source.header;
            navigation = source.navigation;
            content = source.content;
            footer = source.footer;
            background = source.background;
            maxContentWidth = source.maxContentWidth;
            regionGap = source.regionGap;
            scrollContent = source.scrollContent;
            footerAlignment = source.footerAlignment;
            footerDivider = source.footerDivider;
            return this;
        }

        public UiTemplate build() {
            Slots slots = new Slots(header, navigation, Objects.requireNonNull(content, "content"), footer);
            Options options = new Options(background, maxContentWidth, regionGap, scrollContent,
                footerAlignment, footerDivider);
            return new UiTemplate(layout.compose(slots, options), slots, options);
        }
    }

    private static final UiTemplateLayout TOP_NAVIGATION = (slots, options) -> {
        Ui.Node content = slots.content();
        if (options.scrollContent() && !(content instanceof UiScrollView) && !(content instanceof UiPageHost)) {
            content = Ui.scrollView(content);
        }
        UiScaffold scaffold = Ui.scaffold(content)
            .navigationMode(UiScaffold.NavigationMode.TOP)
            .maxContentWidth(options.maxContentWidth());
        if (options.regionGap() >= 0) scaffold.regionGap(options.regionGap());
        if (slots.header() != null) scaffold.header(slots.header());
        if (slots.navigation() != null) scaffold.navigation(slots.navigation());
        if (slots.footer() != null) {
            scaffold.footer(new FooterNode(slots.footer(), options.footerAlignment(), options.footerDivider()));
        }
        return scaffold;
    };

    private static final class FooterNode extends Ui.Node implements Ui.ChildProvider {
        private final Ui.Node child;
        private final UiMainAxisAlignment alignment;
        private final boolean divider;
        private static final float DIVIDER_HEIGHT = 1;

        private FooterNode(Ui.Node child, UiMainAxisAlignment alignment, boolean divider) {
            this.child = child;
            this.alignment = alignment;
            this.divider = divider;
        }

        @Override public List<Ui.Node> childNodes() { return List.of(child); }

        @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
            float dividerGap = dividerGap(theme);
            child.measure(renderer, maxWidth, Math.max(0, maxHeight - (divider ? DIVIDER_HEIGHT + dividerGap : 0)), theme);
            measuredWidth = maxWidth;
            measuredHeight = Math.min(maxHeight, child.measuredHeight() + (divider ? DIVIDER_HEIGHT + dividerGap : 0));
        }

        @Override public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
            super.layout(renderer, value, theme);
            float top = value.y() + (divider ? DIVIDER_HEIGHT + dividerGap(theme) : 0);
            float width = alignment == UiMainAxisAlignment.SPACE_BETWEEN && child instanceof UiRow
                ? value.width() : child.measuredWidth();
            if (child instanceof UiRow row && alignment == UiMainAxisAlignment.SPACE_BETWEEN
                && row.mainAxisAlignment() != UiMainAxisAlignment.SPACE_BETWEEN) {
                row.mainAxisAlignment(UiMainAxisAlignment.SPACE_BETWEEN);
            }
            float x = switch (alignment) {
                case START, SPACE_BETWEEN -> value.x();
                case CENTER -> value.x() + Math.max(0, value.width() - width) / 2f;
                case END -> value.x() + Math.max(0, value.width() - width);
            };
            child.layout(renderer, new UiBounds(x, top, width, Math.min(child.measuredHeight(), Math.max(0, value.height() - (top - value.y())))), theme);
        }

        @Override public void render(UiRenderer renderer, UiTheme theme) {
            if (divider && bounds.width() > 0) {
                renderer.fillRect(new UiBounds(bounds.x(), bounds.y(), bounds.width(), DIVIDER_HEIGHT),
                    withAlpha(theme.palette().border(), 90));
            }
            child.render(renderer, theme);
        }
        @Override public boolean click(float x, float y, int button) { return child.click(x, y, button); }
        @Override public boolean scroll(float x, float y, double amount) { return child.scroll(x, y, amount); }
        @Override public boolean drag(float x, float y, int button) { return child.drag(x, y, button); }
        @Override public boolean release(float x, float y, int button) { return child.release(x, y, button); }
        @Override public boolean key(int keyCode) { return child.key(keyCode); }
        @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) { return child.key(event, clipboard); }
        @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) { return child.textInput(event, clipboard); }

        private static float dividerGap(UiTheme theme) {
            return Math.max(1, theme.metrics().spacing() * .65f);
        }

        private static int withAlpha(int color, int alpha) { return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF); }
    }
}
