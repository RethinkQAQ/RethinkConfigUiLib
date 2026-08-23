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

import java.util.Objects;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;

/** A panel with a heading rendered above its children. */
public class UiSection extends UiPanel {
    private final UiText title;

    public UiSection(UiText title) {
        this.title = Objects.requireNonNull(title, "title");
    }

    public UiText title() { return title; }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        super.measureSelf(renderer, maxWidth,
            maxHeight - renderer.lineHeight() - theme.metrics().spacing(), theme);
        measuredHeight += renderer.lineHeight() + theme.metrics().spacing();
    }

    @Override
    public void layout(UiRenderer renderer, com.rethinkqaq.configui.core.UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        for (Ui.Node child : children) {
            child.layout(renderer, new com.rethinkqaq.configui.core.UiBounds(
                child.bounds().x(), child.bounds().y() + renderer.lineHeight() + theme.metrics().spacing(),
                child.bounds().width(), child.bounds().height()), theme);
        }
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        super.render(renderer, theme);
        Ui.drawFittedText(renderer, title,
            bounds.x() + theme.metrics().padding(), bounds.y() + theme.metrics().padding(),
            bounds.width() - theme.metrics().padding() * 2, theme.palette().textPrimary());
    }
}
