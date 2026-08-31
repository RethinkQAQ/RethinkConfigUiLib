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

package com.rethinkqaq.configui.core.component.feedback;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;
import com.rethinkqaq.configui.core.UiColor;
import com.rethinkqaq.configui.core.UiTextMetrics;
import java.util.Objects;

/** Inline semantic feedback surface for configuration guidance. */
public final class UiAlert extends Ui.Node {
    private final UiFeedbackType type; private final UiText text; private final int customColor; private int lines;
    public UiAlert(UiFeedbackType type, UiText text) { this(type, text, 0); }
    public UiAlert(UiFeedbackType type, UiText text, int customColor) { this.type = Objects.requireNonNull(type, "type"); this.text = Objects.requireNonNull(text, "text"); this.customColor = customColor; }
    public static UiAlert custom(UiText text, int color) { return new UiAlert(UiFeedbackType.CUSTOM, text, color); }
    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) { lines = Ui.wrapLines(renderer, text, Math.max(0, maxWidth - theme.metrics().padding() * 2), 4).size(); measuredWidth = maxWidth; measuredHeight = Math.min(maxHeight, lines * UiTextMetrics.lineHeight(renderer, UiTextMetrics.bodyScale(theme.metrics())) + theme.metrics().padding()); }
    @Override public void render(UiRenderer renderer, UiTheme theme) {
        int tone = type.color(customColor);
        renderer.fillRoundRect(bounds, theme.metrics().controlRadius(), UiColor.withAlpha(tone, theme.states().alertFillOpacity()));
        renderer.strokeRoundRect(bounds, theme.metrics().controlRadius(), theme.metrics().borderWidth(), UiColor.withAlpha(tone, theme.states().alertBorderOpacity()));
        UiTextMetrics.Block block = UiTextMetrics.block(renderer, text, bounds.width() - theme.metrics().padding(), lines,
            UiTextMetrics.bodyScale(theme.metrics()), 0, true);
        UiTextMetrics.drawBlock(renderer, block, bounds.x() + theme.metrics().padding() / 2, bounds.y() + theme.metrics().padding() / 2,
            bounds.width() - theme.metrics().padding(), theme.palette().textPrimary(), false);
    }
    public UiFeedbackType type() { return type; }
    public UiText text() { return text; }
}
