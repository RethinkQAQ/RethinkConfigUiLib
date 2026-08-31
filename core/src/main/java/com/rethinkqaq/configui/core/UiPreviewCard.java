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
import java.util.function.BooleanSupplier;

/**
 * A generic card with a bounded preview slot. Core owns the shell; a platform integration can
 * place an item, entity, model or texture node in the preview slot without leaking its types here.
 */
public final class UiPreviewCard extends Ui.Node implements Ui.ChildProvider, Ui.SelfDispatching, Ui.ClipProvider {
    private final UiText title;
    private UiText description;
    private UiTextMetrics.Block titleBlock = new UiTextMetrics.Block(List.of(), 1, 0, 0);
    private UiTextMetrics.Block descriptionBlock = new UiTextMetrics.Block(List.of(), 1, 0, 0);
    private int maximumDescriptionLines = 3;
    private final Ui.Node preview;
    private Ui.Node action;
    private float previewHeight = 72;
    private float headerHeight;
    private float actionHeight;
    private Runnable cardAction;
    private BooleanSupplier selected = () -> false;

    UiPreviewCard(UiText title, Ui.Node preview) {
        this.title = Objects.requireNonNull(title, "title");
        this.preview = Objects.requireNonNull(preview, "preview");
    }

    public UiPreviewCard description(UiText value) { description = Objects.requireNonNull(value, "description"); invalidateMeasure(); return this; }
    public UiPreviewCard action(Ui.Node value) { action = Objects.requireNonNull(value, "action"); invalidateMeasure(); return this; }
    public UiPreviewCard onClick(Runnable value) { cardAction = Objects.requireNonNull(value, "onClick"); return this; }
    public UiPreviewCard selected(BooleanSupplier value) { selected = Objects.requireNonNull(value, "selected"); return this; }
    public UiPreviewCard previewHeight(float value) {
        if (value <= 0) throw new IllegalArgumentException("preview height must be positive");
        previewHeight = value;
        invalidateMeasure();
        return this;
    }
    /** Limits description text while retaining a fixed slot for every card in the grid. */
    public UiPreviewCard maximumDescriptionLines(int value) {
        if (value < 0) throw new IllegalArgumentException("maximumDescriptionLines must be non-negative");
        maximumDescriptionLines = value;
        invalidateMeasure();
        return this;
    }
    public int maximumDescriptionLines() { return maximumDescriptionLines; }
    @Override public List<Ui.Node> childNodes() {
        List<Ui.Node> result = new ArrayList<>();
        result.add(preview);
        if (action != null) result.add(action);
        return result;
    }

    @Override public UiBounds viewportBounds() {
        return bounds;
    }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float innerWidth = Math.max(0, maxWidth - theme.metrics().padding() * 2);
        float textScale = UiTextMetrics.bodyScale(theme.metrics());
        float textGap = theme.metrics().spacing() / 2f;
        titleBlock = UiTextMetrics.block(renderer, title, innerWidth, 1, textScale, 0, true);
        descriptionBlock = description == null || maximumDescriptionLines == 0
            ? new UiTextMetrics.Block(List.of(), textScale, UiTextMetrics.lineHeight(renderer, textScale), textGap)
            : UiTextMetrics.block(renderer, description, innerWidth, maximumDescriptionLines, textScale, textGap, false);
        // Reserve a fixed description slot: card previews share one vertical baseline even when
        // localized descriptions use fewer lines.
        float descriptionSlotHeight = maximumDescriptionLines == 0 ? 0
            : maximumDescriptionLines * descriptionBlock.lineHeight()
                + Math.max(0, maximumDescriptionLines - 1) * descriptionBlock.lineGap();
        headerHeight = titleBlock.height() + (maximumDescriptionLines == 0 ? 0 : textGap + descriptionSlotHeight);
        preview.measure(renderer, innerWidth, previewHeight, theme);
        actionHeight = 0;
        if (action != null) {
            action.measure(renderer, innerWidth, maxHeight, theme);
            actionHeight = action.measuredHeight();
        }
        measuredWidth = maxWidth;
        measuredHeight = Math.min(maxHeight, theme.metrics().padding() * 2 + headerHeight + theme.metrics().spacing()
            + previewHeight + (action == null ? 0 : theme.metrics().spacing() + actionHeight));
    }

    @Override
    public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        float padding = theme.metrics().padding();
        float innerWidth = Math.max(0, value.width() - padding * 2);
        float y = value.y() + padding + headerHeight + theme.metrics().spacing();
        float actionY = action == null ? value.y() + value.height() - padding
            : value.y() + value.height() - padding - actionHeight;
        float previewSlotHeight = Math.min(previewHeight, Math.max(0, actionY - theme.metrics().spacing() - y));
        preview.layout(renderer, new UiBounds(value.x() + padding, y, innerWidth, previewSlotHeight), theme);
        if (action != null) {
            action.layout(renderer, new UiBounds(value.x() + padding, actionY, innerWidth, actionHeight), theme);
        }
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        // Keep every slot inside the card. This is important for platform previews that use
        // their own transform and for cards displayed in a scrolling grid.
        renderer.pushClip(bounds);
        renderer.fillRoundRect(bounds, theme.metrics().cardRadius(), theme.palette().card());
        renderer.strokeRoundRect(bounds, theme.metrics().cardRadius(), theme.metrics().borderWidth(), theme.palette().border());
        renderer.pushClip(preview.bounds());
        preview.render(renderer, theme);
        renderer.popClip();
        if (selected.getAsBoolean()) {
            renderer.strokeRoundRect(bounds, theme.metrics().cardRadius(),
                Math.max(1, theme.metrics().borderWidth()), theme.palette().accent());
        }
        float padding = theme.metrics().padding();
        float textWidth = Math.max(0, bounds.width() - padding * 2);
        UiTextMetrics.drawBlock(renderer, titleBlock, bounds.x() + padding, bounds.y() + padding,
            textWidth, theme.palette().textPrimary(), true);
        UiTextMetrics.drawBlock(renderer, descriptionBlock, bounds.x() + padding,
            bounds.y() + padding + titleBlock.height() + theme.metrics().spacing() / 2f,
            textWidth, theme.palette().textSecondary(), true);
        if (action != null) action.render(renderer, theme);
        renderer.popClip();
    }

    @Override public boolean click(float x, float y, int button) {
        if (!bounds.contains(x, y)) return false;
        if (action != null && action.bounds().contains(x, y) && action.click(x, y, button)) return true;
        // The card is itself the primary selection target.  A preview may expose secondary
        // interaction, but it must not swallow a click that the host registered for the card.
        if (button == 0 && cardAction != null) { cardAction.run(); return true; }
        if (preview.bounds().contains(x, y) && preview.click(x, y, button)) return true;
        return false;
    }
    @Override public boolean scroll(float x, float y, double amount) {
        return preview.bounds().contains(x, y) && preview.scroll(x, y, amount);
    }
}
