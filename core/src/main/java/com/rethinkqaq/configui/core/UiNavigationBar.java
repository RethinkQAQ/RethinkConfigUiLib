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

/** Responsive, keyboard-focusable category navigation for {@link UiPageHost}. */
public final class UiNavigationBar extends Ui.Node implements Ui.ChildProvider {
    private final UiPageHost host;
    private final List<Ui.Button> buttons = new ArrayList<>();
    private final List<Placement> placements = new ArrayList<>();
    private float gap;
    private int rows;
    private UiTheme buttonTheme;

    UiNavigationBar(UiPageHost owner) { host = owner; }

    @Override public List<Ui.Node> childNodes() { return List.copyOf(buttons); }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        syncButtons();
        buttonTheme = theme;
        gap = buttonTheme.metrics().spacing();
        placements.clear();
        float x = 0, rowHeight = 0, totalHeight = 0;
        rows = 0;
        for (int index = 0; index < buttons.size(); index++) {
            Ui.Button button = buttons.get(index);
            button.variant(index == host.selectedIndex() ? Ui.ButtonVariant.PRIMARY : Ui.ButtonVariant.SECONDARY);
            button.measure(renderer, Math.max(1, maxWidth), maxHeight, buttonTheme);
            float width = Math.min(maxWidth, button.measuredWidth());
            if (x > 0 && x + width > maxWidth) {
                totalHeight += rowHeight + gap;
                rows++;
                x = 0;
                rowHeight = 0;
            }
            placements.add(new Placement(index, x, width, rows));
            x += width + gap;
            rowHeight = Math.max(rowHeight, button.measuredHeight());
        }
        if (!buttons.isEmpty()) {
            totalHeight += rowHeight;
            rows++;
        }
        measuredWidth = maxWidth;
        measuredHeight = Math.min(maxHeight, totalHeight);
    }

    @Override public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        float[] rowHeights = new float[Math.max(0, rows)];
        for (Placement placement : placements) rowHeights[placement.row()] = Math.max(rowHeights[placement.row()], buttons.get(placement.index()).measuredHeight());
        float[] rowY = new float[rowHeights.length];
        float y = value.y();
        for (int row = 0; row < rowHeights.length; row++) {
            rowY[row] = y;
            y += rowHeights[row] + gap;
        }
        for (Placement placement : placements) {
            Ui.Button button = buttons.get(placement.index());
            button.layout(renderer, new UiBounds(value.x() + placement.x(), rowY[placement.row()], placement.width(), rowHeights[placement.row()]),
                buttonTheme == null ? theme : buttonTheme);
        }
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) {
        syncButtons();
        UiTheme activeTheme = buttonTheme == null ? theme : buttonTheme;
        for (Ui.Button button : buttons) button.render(renderer, activeTheme);
    }

    @Override public boolean click(float x, float y, int button) {
        for (int index = buttons.size() - 1; index >= 0; index--) if (buttons.get(index).click(x, y, button)) return true;
        return false;
    }
    @Override public boolean key(int keyCode) {
        for (int index = 0; index < buttons.size(); index++) {
            Ui.Button button = buttons.get(index);
            if (!button.focused()) continue;
            if (keyCode == UiKey.LEFT || keyCode == UiKey.UP) {
                host.select(Math.floorMod(index - 1, buttons.size()));
                return true;
            }
            if (keyCode == UiKey.RIGHT || keyCode == UiKey.DOWN) {
                host.select(Math.floorMod(index + 1, buttons.size()));
                return true;
            }
            if (button.key(keyCode)) return true;
        }
        return false;
    }
    @Override public boolean release(float x, float y, int button) {
        boolean handled = false;
        for (Ui.Button child : buttons) handled |= child.release(x, y, button);
        return handled;
    }
    @Override public boolean drag(float x, float y, int button) {
        boolean handled = false;
        for (Ui.Button child : buttons) handled |= child.drag(x, y, button);
        return handled;
    }

    private void syncButtons() {
        while (buttons.size() < host.pageCount()) {
            int index = buttons.size();
            buttons.add(Ui.button(host.pageTitle(index), () -> host.select(index)));
        }
        while (buttons.size() > host.pageCount()) buttons.remove(buttons.size() - 1);
    }

    private record Placement(int index, float x, float width, int row) { }
}
