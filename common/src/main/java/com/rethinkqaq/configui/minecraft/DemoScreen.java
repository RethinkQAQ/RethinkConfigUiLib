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
package com.rethinkqaq.configui.minecraft;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBadge;
import com.rethinkqaq.configui.core.UiBinding;
import com.rethinkqaq.configui.core.UiPageHost;
import com.rethinkqaq.configui.core.UiScaffold;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.gui.screens.Screen;

/** Built-in visual smoke test, enabled only with the RCUI example JVM flag. */
public final class DemoScreen extends UiScreen {
    public DemoScreen() { this(null); }

    public DemoScreen(Screen parent) {
        super(parent, page(), UiTheme.roseLight(), UiHost.LayoutMode.FULLSCREEN);
    }

    private static Ui.Node page() {
        AtomicBoolean enabled = new AtomicBoolean(true);
        AtomicReference<Double> scale = new AtomicReference<>(1.0);
        AtomicReference<String> mode = new AtomicReference<>("Balanced");

        Ui.Container header = Ui.panel()
            .add(Ui.label(UiText.literal("RETHINK CONFIG UI LIB")))
            .add(Ui.label(UiText.literal("Modern, dependency-free UI surfaces for configuration and custom pages")).wrap(true));

        UiPageHost pages = Ui.pageHost()
            .addPage(UiText.literal("General"), generalPage(enabled, scale, mode))
            .addPage(UiText.literal("Preview"), previewPage())
            .addPage(UiText.literal("Advanced"), advancedPage());

        return Ui.scaffold(pages)
            .header(header)
            .navigation(pages.navigation())
            .navigationMode(UiScaffold.NavigationMode.TOP)
            .maxContentWidth(1080);
    }

    private static Ui.Node generalPage(AtomicBoolean enabled, AtomicReference<Double> scale,
                                       AtomicReference<String> mode) {
        Ui.Container settings = Ui.section(UiText.literal("GENERAL"));
        settings.add(Ui.settingRow(UiText.literal("Enable preview"),
                Ui.toggle(UiText.literal(""), binding(enabled::get, enabled::set)))
                .description(UiText.literal("Render a live preview in this screen.")))
            .add(Ui.settingRow(UiText.literal("Interface scale"),
                Ui.slider(UiText.literal(""), binding(scale::get, scale::set), 0.5, 2.0, 0.1))
                .description(UiText.literal("A host can bind this value directly to its own configuration.")))
            .add(Ui.settingRow(UiText.literal("Render mode"),
                Ui.select(UiText.literal("Mode"), binding(mode::get, mode::set),
                    List.of("Fast", "Balanced", "Quality"), UiText::literal))
                .description(UiText.literal("Select values with the mouse, keyboard or controller.")));

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

        return Ui.column().gap(14)
            .add(Ui.tooltip(
                Ui.label(UiText.literal("A responsive page: navigation stays visible while the selected content scrolls."))
                    .wrap(true),
                UiText.literal("Only the main content area scrolls; the header and category navigation remain available.")))
            .add(settings)
            .add(actions);
    }

    private static Ui.Node previewPage() {
        Ui.Node previewContent = new MinecraftPreview((graphics, bounds) -> {
            int left = Math.round(bounds.x());
            int top = Math.round(bounds.y());
            int right = Math.round(bounds.x() + bounds.width());
            int bottom = Math.round(bounds.y() + bounds.height());
            graphics.fill(left, top, right, bottom, 0xFFFCE8F0);
            int size = Math.min(right - left, bottom - top) / 2;
            int centerX = (left + right) / 2;
            int centerY = (top + bottom) / 2;
            graphics.fill(centerX - size / 2, centerY - size / 2,
                centerX + size / 2, centerY + size / 2, 0xFFF39ABA);
        });
        return Ui.column().gap(14)
            .add(Ui.previewCard(UiText.literal("LIVE PREVIEW"), previewContent)
                .description(UiText.literal("A platform renderer may draw an item, entity or model here."))
                .action(Ui.button(UiText.literal("Open preview"), () -> { }).variant(Ui.ButtonVariant.SECONDARY)))
            .add(Ui.previewCard(UiText.literal("THEME"),
                Ui.panel().add(Ui.label(UiText.literal("roseLight")))
                    .add(Ui.badge(UiText.literal("DEFAULT")).tone(UiBadge.Tone.SUCCESS)))
                .description(UiText.literal("Themes use semantic colours and immutable metrics.")));
    }

    private static Ui.Node advancedPage() {
        Ui.Container disabled = Ui.section(UiText.literal("STATES"));
        disabled.add(Ui.settingRow(UiText.literal("Disabled toggle"),
                Ui.toggle(UiText.literal("Unavailable"), UiBinding.of(() -> false, value -> { })))
                .description(UiText.literal("Disabled controls retain their layout and remain readable.")))
            .add(Ui.button(UiText.literal("Disabled action"), () -> { })
                .enabled(false));
        return Ui.column().gap(14)
            .add(Ui.section(UiText.literal("ADVANCED"))
                .add(Ui.label(UiText.literal("Any core node can be composed into a secondary page."))
                    .wrap(true)))
            .add(disabled)
            .add(Ui.badge(UiText.literal("Theme override ready")).tone(UiBadge.Tone.ACCENT));
    }

    private static <T> UiBinding<T> binding(java.util.function.Supplier<T> getter,
                                            java.util.function.Consumer<T> setter) {
        return UiBinding.of(getter, setter);
    }
}
