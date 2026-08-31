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

/** Shared measure/layout contract for the stock label-description-control compositions. */
public final class UiLabeledControlLayout {
    private static final int MAX_TEXT_LINES = 3;
    private boolean compact;
    private float controlWidth;
    private float textWidth;
    private UiTextMetrics.Block label = new UiTextMetrics.Block(java.util.List.of(), 1, 0, 0);
    private UiTextMetrics.Block description = new UiTextMetrics.Block(java.util.List.of(), 1, 0, 0);

    public void measure(UiRenderer renderer, UiText labelText, UiText descriptionText, Ui.Node control,
                        float maxWidth, float maxHeight, UiTheme theme, float compactBelow) {
        compact = maxWidth < compactBelow;
        if (compact) {
            control.measure(renderer, maxWidth, maxHeight, theme);
            controlWidth = maxWidth;
            textWidth = maxWidth;
        } else {
            controlWidth = Math.min(Math.max(theme.metrics().controlHeight() * 3.5f, maxWidth * .38f), maxWidth * .5f);
            control.measure(renderer, controlWidth, maxHeight, theme);
            controlWidth = control.measuredWidth();
            textWidth = Math.max(0, maxWidth - controlWidth - theme.metrics().spacing());
        }
        float scale = UiTextMetrics.bodyScale(theme.metrics());
        float gap = theme.metrics().spacing() / 2f;
        label = UiTextMetrics.block(renderer, labelText, textWidth, MAX_TEXT_LINES, scale, gap, true);
        description = descriptionText == null
            ? new UiTextMetrics.Block(java.util.List.of(), scale, UiTextMetrics.lineHeight(renderer, scale), gap)
            : UiTextMetrics.block(renderer, descriptionText, textWidth, MAX_TEXT_LINES, scale, gap, true);
    }

    public float textHeight(UiTheme theme) {
        return label.height() + (description.lines().isEmpty() ? 0 : theme.metrics().spacing() / 2f + description.height());
    }

    public float measuredHeight(Ui.Node control, UiTheme theme) {
        return compact ? textHeight(theme) + theme.metrics().spacing() + control.measuredHeight()
            : Math.max(textHeight(theme), control.measuredHeight());
    }

    public void layout(UiRenderer renderer, UiBounds bounds, Ui.Node control, UiTheme theme, float reservedBottom) {
        float availableHeight = Math.max(0, bounds.height() - reservedBottom);
        if (compact) {
            control.layout(renderer, new UiBounds(bounds.x(), bounds.y() + textHeight(theme) + theme.metrics().spacing(),
                bounds.width(), control.measuredHeight()), theme);
        } else {
            control.layout(renderer, new UiBounds(bounds.x() + bounds.width() - controlWidth,
                bounds.y() + (availableHeight - control.measuredHeight()) / 2f, controlWidth, control.measuredHeight()), theme);
        }
    }

    public void render(UiRenderer renderer, UiBounds bounds, UiTheme theme, int primary, int secondary) {
        UiTextMetrics.drawBlock(renderer, label, bounds.x(), bounds.y(), textWidth, primary, false);
        if (!description.lines().isEmpty()) {
            UiTextMetrics.drawBlock(renderer, description, bounds.x(),
                bounds.y() + label.height() + theme.metrics().spacing() / 2f, textWidth, secondary, false);
        }
    }

    public boolean compact() { return compact; }
    public float controlWidth() { return controlWidth; }
    public float textWidth() { return textWidth; }
}
