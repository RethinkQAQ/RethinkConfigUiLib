/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.minecraft;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBinding;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;
import com.rethinkqaq.configui.core.UiBadge;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.gui.screens.Screen;

/** Built-in visual smoke test, enabled only with the RCUI example JVM flag. */
public final class DemoScreen extends UiScreen {
    public DemoScreen() {
        this(null);
    }

    public DemoScreen(Screen parent) {
        super(parent, page(), UiTheme.roseLight(), UiHost.LayoutMode.FULLSCREEN);
        host().scalePolicy(UiScalePolicy.adaptive());
    }

    private static Ui.Node page() {
        AtomicBoolean enabled = new AtomicBoolean(true);
        AtomicReference<Double> scale = new AtomicReference<>(1.0);
        AtomicReference<String> mode = new AtomicReference<>("Balanced");
        Ui.Column navigation = Ui.column().gap(6);
        navigation.add(Ui.label(UiText.literal("SECTIONS")))
            .add(Ui.button(UiText.literal("General"), () -> { }))
            .add(Ui.button(UiText.literal("Preview"), () -> { }).variant(Ui.ButtonVariant.SECONDARY))
            .add(Ui.button(UiText.literal("Advanced"), () -> { }).variant(Ui.ButtonVariant.SECONDARY));

        Ui.Container header = Ui.panel()
            .add(Ui.label(UiText.literal("RETHINK CONFIG UI LIB")))
            .add(Ui.label(UiText.literal("Modern, dependency-free configuration surfaces")));

        Ui.Container general = Ui.section(UiText.literal("GENERAL"));
        general.add(Ui.settingRow(UiText.literal("Enable preview"),
                Ui.toggle(UiText.literal(""), binding(enabled::get, enabled::set)))
                .description(UiText.literal("Render a live preview in this screen.")))
            .add(Ui.settingRow(UiText.literal("Interface scale"),
                Ui.slider(UiText.literal(""), binding(scale::get, scale::set), 0.5, 2.0, 0.1))
                .description(UiText.literal("A host can bind this value directly to its own configuration.")))
            .add(Ui.settingRow(UiText.literal("Render mode"),
                Ui.select(UiText.literal("Mode"), binding(mode::get, mode::set), List.of("Fast", "Balanced", "Quality"), UiText::literal))
                .description(UiText.literal("Select values cycle with the mouse or keyboard.")));

        Ui.Node previewContent = new MinecraftPreview((graphics, bounds) -> {
            int left = Math.round(bounds.x());
            int top = Math.round(bounds.y());
            int right = Math.round(bounds.x() + bounds.width());
            int bottom = Math.round(bounds.y() + bounds.height());
            graphics.fill(left, top, right, bottom, 0xFFFCE8F0);
            int size = Math.min(right - left, bottom - top) / 2;
            int centerX = (left + right) / 2;
            int centerY = (top + bottom) / 2;
            graphics.fill(centerX - size / 2, centerY - size / 2, centerX + size / 2, centerY + size / 2, 0xFFF39ABA);
        });
        Ui.Container actions = Ui.section(UiText.literal("ACTIONS"));
        actions.add(Ui.row().gap(8)
                .add(Ui.button(UiText.literal("Reset"), () -> {
                    enabled.set(true);
                    scale.set(1.0);
                    mode.set("Balanced");
                }).variant(Ui.ButtonVariant.SECONDARY))
                .add(Ui.button(UiText.literal("Apply"), () -> { })))
            .add(Ui.tooltip(Ui.label(UiText.literal("Changes apply immediately")),
                UiText.literal("The host decides when and how values are persisted.")));

        Ui.Column settings = Ui.column().gap(18);
        settings.add(Ui.tooltip(
                Ui.label(UiText.literal("A responsive configuration page: compact windows stack controls; wide windows keep them aligned.")),
                UiText.literal("Hover this line to preview an RCUI tooltip. The host may replace the text with a localized description.")))
            .add(general)
            .add(Ui.grid().minimumColumnWidth(220).gap(14)
                .add(Ui.previewCard(UiText.literal("LIVE PREVIEW"), previewContent)
                    .description(UiText.literal("A platform renderer may draw an item, entity or model here."))
                    .action(Ui.button(UiText.literal("Open preview"), () -> { }).variant(Ui.ButtonVariant.SECONDARY)))
                .add(Ui.previewCard(UiText.literal("THEME"),
                    Ui.panel().add(Ui.label(UiText.literal("roseLight"))).add(Ui.badge(UiText.literal("DEFAULT")).tone(UiBadge.Tone.SUCCESS)))
                    .description(UiText.literal("Themes use semantic colours and immutable metrics."))))
            .add(actions);

        return Ui.scaffold(Ui.scrollView(settings))
            .header(header)
            .sidebar(Ui.section(UiText.literal("NAVIGATION")).add(navigation))
            .sidebarWidth(144)
            .maxContentWidth(1080);
    }

    private static <T> UiBinding<T> binding(java.util.function.Supplier<T> getter, java.util.function.Consumer<T> setter) {
        return UiBinding.of(getter, setter);
    }
}
