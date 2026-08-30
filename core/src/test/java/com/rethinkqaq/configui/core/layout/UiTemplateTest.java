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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBackground;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiMainAxisAlignment;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiScaffold;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;
import org.junit.jupiter.api.Test;

class UiTemplateTest {
    private static final UiTheme THEME = UiTheme.roseLight();

    @Test
    void contentIsRequired() {
        assertThrows(NullPointerException.class, () -> Ui.template().build());
    }

    @Test
    void topNavigationKeepsRegionsOrderedAndFooterSeparated() {
        Ui.Node header = UiHeader.text(UiText.literal("Header"));
        Ui.Node navigation = Ui.label(UiText.literal("Navigation"));
        Ui.Node content = Ui.label(UiText.literal("Content"));
        Ui.Node footer = Ui.button(UiText.literal("Done"), () -> { });
        UiTemplate template = Ui.template()
            .header(header)
            .navigation(navigation)
            .content(content)
            .footer(footer)
            .regionGap(8)
            .background(UiBackground.transparent())
            .build();

        layout(template, 800, 200);
        UiScaffold scaffold = (UiScaffold) template.childNodes().get(0);
        assertEquals(UiScaffold.NavigationMode.TOP, scaffold.navigationMode());
        assertEquals(4, scaffold.childNodes().size());
        assertSame(header, scaffold.childNodes().get(0));
        assertSame(navigation, scaffold.childNodes().get(1));
        assertTrue(scaffold.content().bounds().y() >= navigation.bounds().y() + navigation.bounds().height() + 8);
        assertTrue(footer.bounds().y() >= scaffold.content().bounds().y() + scaffold.content().bounds().height());
        assertEquals(UiBackground.Mode.TRANSPARENT, template.background().mode());
    }

    @Test
    void suppliedScrollViewIsNotWrappedAgain() {
        Ui.ScrollView content = Ui.scrollView(Ui.label(UiText.literal("Content")));
        UiTemplate template = Ui.template().content(content).build();
        layout(template, 800, 200);
        UiScaffold scaffold = (UiScaffold) template.childNodes().get(0);
        assertSame(content, scaffold.content());
    }

    @Test
    void spaceBetweenFooterUsesFullRowWidth() {
        Ui.Button left = Ui.button(UiText.literal("Reset"), () -> { });
        Ui.Button right = Ui.button(UiText.literal("Done"), () -> { });
        Ui.Row footer = Ui.row().add(left).add(right);
        UiTemplate template = Ui.template()
            .content(Ui.label(UiText.literal("Content")))
            .footer(footer)
            .footerAlignment(UiMainAxisAlignment.SPACE_BETWEEN)
            .build();

        layout(template, 800, 200);
        assertTrue(right.bounds().x() > left.bounds().x() + left.bounds().width());
        assertEquals(footer.bounds().width(), 800, 0.001f);
    }

    private static void layout(UiTemplate template, float width, float height) {
        template.measure(RENDERER, width, height, THEME);
        template.layout(RENDERER, new UiBounds(0, 0, width, height), THEME);
    }

    private static final UiRenderer RENDERER = new UiRenderer() {
        @Override public void fillRect(UiBounds bounds, int color) { }
        @Override public void fillRoundRect(UiBounds bounds, float radius, int color) { }
        @Override public void strokeRoundRect(UiBounds bounds, float radius, float width, int color) { }
        @Override public void drawText(UiText text, float x, float y, int color) { }
        @Override public float textWidth(UiText text) { return text.value().length() * 6f; }
        @Override public float lineHeight() { return 10; }
        @Override public void pushClip(UiBounds bounds) { }
        @Override public void popClip() { }
    };
}
