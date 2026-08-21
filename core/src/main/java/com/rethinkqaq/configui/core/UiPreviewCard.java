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
 * A generic card with a bounded preview slot. Core owns the shell; a platform integration can
 * place an item, entity, model or texture node in the preview slot without leaking its types here.
 */
public final class UiPreviewCard extends Ui.Node implements Ui.ChildProvider {
    private final UiText title;
    private UiText description;
    private final Ui.Node preview;
    private Ui.Node action;
    private float previewHeight = 104;
    private float headerHeight;

    UiPreviewCard(UiText title, Ui.Node preview) {
        this.title = Objects.requireNonNull(title, "title");
        this.preview = Objects.requireNonNull(preview, "preview");
    }

    public UiPreviewCard description(UiText value) { description = Objects.requireNonNull(value, "description"); return this; }
    public UiPreviewCard action(Ui.Node value) { action = Objects.requireNonNull(value, "action"); return this; }
    public UiPreviewCard previewHeight(float value) {
        if (value <= 0) throw new IllegalArgumentException("preview height must be positive");
        previewHeight = value;
        return this;
    }
    @Override public List<Ui.Node> childNodes() {
        List<Ui.Node> result = new ArrayList<>();
        result.add(preview);
        if (action != null) result.add(action);
        return result;
    }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float innerWidth = Math.max(0, maxWidth - theme.metrics().padding() * 2);
        headerHeight = renderer.lineHeight() + (description == null ? 0 : renderer.lineHeight() + theme.metrics().spacing() / 2f);
        preview.measure(renderer, innerWidth, previewHeight, theme);
        float actionHeight = 0;
        if (action != null) {
            action.measure(renderer, innerWidth, maxHeight, theme);
            actionHeight = action.measuredHeight() + theme.metrics().spacing();
        }
        measuredWidth = maxWidth;
        measuredHeight = Math.min(maxHeight, theme.metrics().padding() * 2 + headerHeight + theme.metrics().spacing()
            + previewHeight + actionHeight);
    }

    @Override
    public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        float padding = theme.metrics().padding();
        float innerWidth = Math.max(0, value.width() - padding * 2);
        float y = value.y() + padding + headerHeight + theme.metrics().spacing();
        preview.layout(renderer, new UiBounds(value.x() + padding, y, innerWidth, previewHeight), theme);
        if (action != null) {
            y += previewHeight + theme.metrics().spacing();
            action.layout(renderer, new UiBounds(value.x() + padding, y, innerWidth, action.measuredHeight()), theme);
        }
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        renderer.fillRoundRect(bounds.offset(0, theme.metrics().shadowOffset()), theme.metrics().cardRadius() + 2, 0x16000000);
        renderer.fillRoundRect(bounds, theme.metrics().cardRadius(), theme.palette().card());
        renderer.strokeRoundRect(bounds, theme.metrics().cardRadius(), theme.metrics().borderWidth(), theme.palette().border());
        float padding = theme.metrics().padding();
        Ui.drawFittedText(renderer, title, bounds.x() + padding, bounds.y() + padding,
            Math.max(0, bounds.width() - padding * 2), theme.palette().textPrimary());
        if (description != null) Ui.drawFittedText(renderer, description, bounds.x() + padding,
            bounds.y() + padding + renderer.lineHeight() + theme.metrics().spacing() / 2f,
            Math.max(0, bounds.width() - padding * 2), theme.palette().textSecondary());
        renderer.pushClip(preview.bounds());
        preview.render(renderer, theme);
        renderer.popClip();
        if (action != null) action.render(renderer, theme);
    }

    @Override public boolean click(float x, float y, int button) {
        return (action != null && action.click(x, y, button)) || preview.click(x, y, button);
    }
    @Override public boolean scroll(float x, float y, double amount) { return preview.scroll(x, y, amount); }
}
