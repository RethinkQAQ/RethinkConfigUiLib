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
import com.rethinkqaq.configui.core.UiBackground;
import com.rethinkqaq.configui.core.UiBinding;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiCrossAxisAlignment;
import com.rethinkqaq.configui.core.UiDensity;
import com.rethinkqaq.configui.core.UiDialogHost;
import com.rethinkqaq.configui.core.UiGrid;
import com.rethinkqaq.configui.core.UiMainAxisAlignment;
import com.rethinkqaq.configui.core.UiPageHost;
import com.rethinkqaq.configui.core.UiPreview;
import com.rethinkqaq.configui.core.UiPreviewCard;
import com.rethinkqaq.configui.core.UiScaffold;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;
import com.rethinkqaq.configui.core.component.UiComponent;
import com.rethinkqaq.configui.core.layout.UiHeader;
import com.rethinkqaq.configui.core.layout.UiHeaderStyle;
import com.rethinkqaq.configui.core.layout.UiTemplate;
import com.rethinkqaq.configui.core.component.data.UiListEntryAdapter;
import com.rethinkqaq.configui.core.component.feedback.UiFeedbackType;
import com.rethinkqaq.configui.core.component.feedback.UiToast;
import com.rethinkqaq.configui.core.setting.UiListSetting;
import com.rethinkqaq.configui.core.setting.UiNumberSpec;
import com.rethinkqaq.configui.core.setting.UiSetting;
import com.rethinkqaq.configui.core.setting.UiValidationResult;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.gui.screens.Screen;

/** Built-in visual smoke test, enabled only with the RCUI example JVM flag. */
public final class DemoScreen extends UiScreen {
    public DemoScreen() { this(null); }

    public DemoScreen(Screen parent) { this(parent, new AtomicReference<>()); }

    private DemoScreen(Screen parent, AtomicReference<UiHost> host) {
        super(parent, page(host), UiTheme.roseLight(), UiHost.LayoutMode.FULLSCREEN);
        host.set(host());
        host().scalePolicy(UiScalePolicy.adaptive());
    }

    private static Ui.Node page(AtomicReference<UiHost> host) {
        AtomicBoolean enabled = new AtomicBoolean(true);
        AtomicReference<Double> scale = new AtomicReference<>(1.0);
        AtomicReference<String> mode = new AtomicReference<>("Balanced");

        UiHeader header = UiHeader.builder(UiText.literal("RETHINK CONFIG UI LIB"))
            .subtitle(UiText.literal("Modern, dependency-free UI surfaces for configuration and custom pages"))
            .style(UiHeaderStyle.TEXT)
            .responsive(true)
            .build();

        UiDialogHost dialogs = Ui.dialogHost();
        UiPageHost pages = Ui.pageHost();
        pages.addPage(UiText.literal("General"), generalPage(enabled, scale, mode, host, dialogs))
            .addPage(UiText.literal("Content"), contentPage())
            .addPage(UiText.literal("Layout"), layoutPage())
            .addPage(UiText.literal("Navigation"), navigationPage())
            .addPage(UiText.literal("Input"), inputPage())
            .addPage(UiText.literal("Data"), dataPage(dialogs))
            .addPage(UiText.literal("Feedback"), feedbackPage(host, dialogs))
            .addPage(UiText.literal("Preview"), previewPage(dialogs))
            .addPage(UiText.literal("Themes"), themesPage(host))
            .addPage(UiText.literal("Templates"), templatesPage(dialogs))
            .addPage(UiText.literal("Custom"), advancedPage());

        Ui.Row footer = Ui.row().gap(8)
            .add(Ui.button(UiText.literal("Reset demo"), () -> { }).variant(Ui.ButtonVariant.SECONDARY))
            .add(Ui.button(UiText.literal("Done"), () -> { }));

        UiTemplate template = Ui.topNavigationTemplate()
            .header(header)
            .navigation(pages.navigation())
            .content(pages)
            .footer(footer)
            .maxContentWidth(1080)
            .regionGap(12)
            .build();
        return dialogs.root(template);
    }

    private static Ui.Node contentPage() {
        return Ui.column().gap(14)
            .add(Ui.textHeader(UiText.literal("Content components"))
                .subtitle(UiText.literal("Text, status, grouping and preview surfaces"))
                .style(UiHeaderStyle.COMPACT))
            .add(Ui.section(UiText.literal("TEXT AND STATUS"))
                .add(Ui.label(UiText.literal("UiLabel renders ordinary body text and can wrap to a bounded number of lines."))
                    .wrap(true))
                .add(Ui.label(UiText.literal("Title and subtitle are composed with UiHeader.")))
                .add(Ui.row().gap(8)
                    .add(Ui.badge(UiText.literal("NEUTRAL")))
                    .add(Ui.badge(UiText.literal("ACCENT")).tone(UiBadge.Tone.ACCENT))
                    .add(Ui.badge(UiText.literal("SUCCESS")).tone(UiBadge.Tone.SUCCESS))
                    .add(Ui.badge(UiText.literal("WARNING")).tone(UiBadge.Tone.WARNING))
                    .add(Ui.badge(UiText.literal("DANGER")).tone(UiBadge.Tone.DANGER)))
                .add(Ui.divider())
                .add(Ui.label(UiText.literal("UiDivider separates related groups without introducing business logic."))))
            .add(Ui.panel().padding(12)
                .add(Ui.section(UiText.literal("PANEL AND SECTION"))
                    .add(Ui.label(UiText.literal("UiPanel supplies a surface; UiSection adds a titled content group.")))))
            .add(Ui.previewCard(UiText.literal("Preview card"), Ui.preview((renderer, bounds, clip, theme) -> {
                renderer.fillRoundRect(bounds, theme.metrics().controlRadius(), theme.palette().accent());
            }).preferredHeight(64))
                .description(UiText.literal("Preview area plus an optional action row."))
                .action(Ui.button(UiText.literal("Action"), () -> { })))
            .add(Ui.label(UiText.literal("The Footer is configured as a template slot using any Ui.Node, usually a UiRow."))
                .wrap(true));
    }

    private static Ui.Node layoutPage() {
        Ui.Node split = Ui.split(
            Ui.panel().padding(10).add(Ui.label(UiText.literal("Primary"))),
            Ui.panel().padding(10).add(Ui.label(UiText.literal("Secondary"))))
            .primaryShare(.62f).gap(10).compactBelow(620);
        Ui.Column scrollContent = Ui.column().gap(6);
        for (int i = 1; i <= 8; i++) scrollContent.add(Ui.label(UiText.literal("Scrollable row " + i)));
        return Ui.column().gap(14)
            .add(Ui.section(UiText.literal("FLOW AND ALIGNMENT"))
                .add(Ui.row().gap(8).mainAxisAlignment(UiMainAxisAlignment.SPACE_BETWEEN)
                    .add(Ui.badge(UiText.literal("START")))
                    .add(Ui.badge(UiText.literal("SPACE_BETWEEN")).tone(UiBadge.Tone.ACCENT))
                    .add(Ui.badge(UiText.literal("END"))))
                .add(Ui.row().gap(8).crossAxisAlignment(UiCrossAxisAlignment.CENTER)
                    .add(Ui.button(UiText.literal("Row"), () -> { }))
                    .add(Ui.column().gap(4).add(Ui.label(UiText.literal("Column"))).add(Ui.divider())))
                .add(Ui.stack()
                    .add(Ui.custom().preferredHeight(42)
                        .render((renderer, bounds, theme) -> renderer.fillRoundRect(bounds,
                            theme.metrics().controlRadius(), theme.palette().surfaceRaised()))
                        .build())
                    .add(Ui.badge(UiText.literal("Stacked")))))
            .add(Ui.section(UiText.literal("GRID AND SPLIT"))
                .add(Ui.grid().minimumColumnWidth(110).maximumColumnWidth(160).gap(8)
                    .rowAlignment(UiMainAxisAlignment.CENTER)
                    .add(layoutBox())
                    .add(layoutBox())
                    .add(layoutBox()))
                .add(split))
            .add(Ui.section(UiText.literal("SCROLL VIEW"))
                .add(new FixedViewport(Ui.scrollView(scrollContent), 90)));
    }

    private static Ui.Node layoutBox() {
        return Ui.custom().preferredHeight(36)
            .render((renderer, bounds, theme) -> renderer.fillRoundRect(bounds,
                theme.metrics().controlRadius(), theme.palette().surfaceRaised()))
            .build();
    }

    private static Ui.Node navigationPage() {
        UiPageHost nested = Ui.pageHost()
            .addPage(UiText.literal("First"), Ui.section(UiText.literal("FIRST PAGE"))
                .add(Ui.label(UiText.literal("UiPageHost owns pages and exposes a reusable navigation node."))))
            .addPage(UiText.literal("Second"), Ui.section(UiText.literal("SECOND PAGE"))
                .add(Ui.label(UiText.literal("Switch pages with the navigation bar."))));
        Ui.Row footer = Ui.row().gap(8)
            .add(Ui.button(UiText.literal("Secondary"), () -> { }).variant(Ui.ButtonVariant.SECONDARY))
            .add(Ui.button(UiText.literal("Primary"), () -> { }));
        UiTemplate template = Ui.template()
            .header(UiText.literal("Template shell"), UiHeaderStyle.COMPACT)
            .navigation(nested.navigation())
            .content(nested)
            .footer(footer)
            .footerAlignment(UiMainAxisAlignment.END)
            .footerDivider(true)
            .build();
        return Ui.column().gap(12)
            .add(Ui.label(UiText.literal("UiTemplate composes Header, Navigation, Content and Footer.")))
            .add(template);
    }

    private static Ui.Node inputPage() {
        AtomicReference<Boolean> toggle = new AtomicReference<>(false);
        AtomicReference<Double> value = new AtomicReference<>(.5);
        AtomicReference<String> text = new AtomicReference<>("");
        UiSetting<Double> setting = UiSetting.of(binding(value::get, value::set), .5);
        UiNumberSpec<Double> spec = UiNumberSpec.builder(UiNumberSpec.DOUBLE).range(0, 1).step(.1).build();
        return Ui.column().gap(14)
            .add(Ui.section(UiText.literal("BUTTONS"))
                .add(Ui.row().gap(8)
                    .add(Ui.button(UiText.literal("Primary"), () -> { }))
                    .add(Ui.button(UiText.literal("Secondary"), () -> { }).variant(Ui.ButtonVariant.SECONDARY))
                    .add(Ui.button(UiText.literal("Outline"), () -> { }).variant(Ui.ButtonVariant.OUTLINE))
                    .add(Ui.button(UiText.literal("Danger"), () -> { }).variant(Ui.ButtonVariant.DANGER))
                    .add(Ui.iconButton(UiText.literal("+"), () -> { }))
                    .add(Ui.button(UiText.literal("Disabled"), () -> { }).enabled(false))))
            .add(Ui.section(UiText.literal("TOGGLE AND SELECT"))
                .add(Ui.toggle(UiText.literal("Enable feature"), binding(toggle::get, toggle::set)))
                .add(Ui.select(UiText.literal("Mode"), binding(() -> "Balanced", ignored -> { }),
                    List.of("Fast", "Balanced", "Quality"), UiText::literal)))
            .add(Ui.section(UiText.literal("NUMERIC INPUT"))
                .add(Ui.slider(UiText.literal("Slider"), binding(value::get, value::set), 0, 1, .1))
                .add(Ui.numberControl(setting, spec))
                .add(Ui.numericField(setting, spec)))
            .add(Ui.section(UiText.literal("TEXT INPUT"))
                .add(Ui.textField(binding(text::get, text::set)).placeholder(UiText.literal("TextField")))
                .add(Ui.searchField(binding(text::get, text::set)).placeholder(UiText.literal("SearchField")))
                .add(Ui.formField(UiText.literal("FormField"), Ui.textField(binding(text::get, text::set)))));
    }

    private static Ui.Node dataPage(UiDialogHost dialogs) {
        AtomicReference<String> selected = new AtomicReference<>("Two");
        AtomicReference<List<String>> entries = new AtomicReference<>(List.of("alpha", "beta"));
        UiListEntryAdapter<String> adapter = UiListEntryAdapter.builder(
            () -> "", UiText::literal,
            value -> Ui.textField(value).placeholder(UiText.literal("value"))).build();
        return Ui.column().gap(14)
            .add(Ui.section(UiText.literal("SELECTION LIST"))
                .add(Ui.selectionList(() -> List.of("One", "Two", "Three"),
                    binding(selected::get, selected::set), UiText::literal)))
            .add(Ui.section(UiText.literal("COLLECTION EDITOR"))
                .add(Ui.collectionEditor(dialogs, UiText.literal("Entries"),
                    UiListSetting.of(UiSetting.of(binding(entries::get, entries::set), List.of())), adapter)))
            .add(Ui.label(UiText.literal("ListEntryAdapter supplies the editor and validation contract for collection values."))
                .wrap(true));
    }

    private static Ui.Node feedbackPage(AtomicReference<UiHost> host, UiDialogHost dialogs) {
        return Ui.column().gap(14)
            .add(Ui.section(UiText.literal("ALERTS"))
                .add(Ui.alert(UiFeedbackType.INFO, UiText.literal("Information")))
                .add(Ui.alert(UiFeedbackType.SUCCESS, UiText.literal("Success")))
                .add(Ui.alert(UiFeedbackType.WARNING, UiText.literal("Warning")))
                .add(Ui.alert(UiFeedbackType.ERROR, UiText.literal("Error"))))
            .add(Ui.section(UiText.literal("TOOLTIPS AND TOASTS"))
                .add(Ui.tooltip(Ui.button(UiText.literal("Hover me"), () -> { }),
                    UiText.literal("Tooltip content stays above normal content.")))
                .add(Ui.tooltip(Ui.badge(UiText.literal("Rich tooltip")),
                    Ui.panel().padding(8).add(Ui.label(UiText.literal("TooltipContent can contain nodes.")))))
                .add(Ui.row().gap(8)
                    .add(Ui.button(UiText.literal("Show toast"), () -> host.get().showToast(UiToast.success(UiText.literal("Toast notification")))))
                    .add(Ui.button(UiText.literal("Clear toasts"), () -> host.get().notifications().clear()).variant(Ui.ButtonVariant.SECONDARY))))
            .add(Ui.button(UiText.literal("Open dialog"), () -> dialogs.show(dialogContent(dialogs))));
    }

    private static Ui.Node themesPage(AtomicReference<UiHost> host) {
        AtomicReference<UiDensity> density = new AtomicReference<>(UiDensity.NORMAL);
        UiTheme blue = UiTheme.custom(
            UiTheme.UiPalette.builder()
                .background(0xFF111827).surfaceRaised(0xFF1F2937).control(0xFF374151)
                .controlHover(0xFF4B5563).controlPressed(0xFF6B7280).controlDisabled(0xFF374151)
                .accent(0xFF60A5FA).accentHover(0xFF93C5FD).accentPressed(0xFF3B82F6)
                .onAccent(0xFF0F172A).textPrimary(0xFFF9FAFB).textSecondary(0xFFD1D5DB)
                .textDisabled(0xFF9CA3AF).border(0xFF4B5563).focusRing(0xFF93C5FD)
                .success(0xFF34D399).warning(0xFFFBBF24).danger(0xFFF87171).build(),
            new UiTheme.UiMetrics(9, 8, 11, 34, 1));
        return Ui.column().gap(14)
            .add(Ui.label(UiText.literal("Themes provide semantic visual tokens; they do not add business logic or change layout contracts."))
                .wrap(true))
            .add(themeSwatch("roseLight", UiTheme.roseLight()))
            .add(themeSwatch("roseDark", UiTheme.roseDark()))
            .add(themeSwatch("custom dark blue", blue))
            .add(Ui.section(UiText.literal("INTERACTIVE STATES"))
                .add(themeStateRow("roseLight states", UiTheme.roseLight()))
                .add(themeStateRow("dark blue states", blue))
                .add(Ui.row().gap(8)
                    .add(Ui.button(UiText.literal("Disabled action"), () -> { }).enabled(false))
                    .add(Ui.previewCard(UiText.literal("Selected preview"),
                            Ui.preview((renderer, bounds, clip, theme) -> renderer.fillRoundRect(bounds,
                                theme.metrics().controlRadius(), theme.palette().accent()))
                            .preferredHeight(42))
                        .selected(() -> true))))
            .add(Ui.section(UiText.literal("BACKGROUND AND DENSITY"))
                .add(Ui.row().gap(8)
                    .add(Ui.button(UiText.literal("Opaque"),
                        () -> host.get().background(UiBackground.opaque(0xFF202124)))
                        .variant(Ui.ButtonVariant.SECONDARY))
                    .add(Ui.button(UiText.literal("Translucent"),
                        () -> host.get().background(UiBackground.translucent(0xFF202124, .78f)))
                        .variant(Ui.ButtonVariant.SECONDARY))
                    .add(Ui.button(UiText.literal("Transparent"),
                        () -> host.get().background(UiBackground.transparent()))
                        .variant(Ui.ButtonVariant.SECONDARY)))
                .add(Ui.row().gap(8)
                    .add(Ui.button(UiText.literal("Comfortable"), () -> {
                        density.set(UiDensity.COMFORTABLE);
                        host.get().scalePolicy(UiScalePolicy.fixed(UiDensity.COMFORTABLE));
                    }))
                    .add(Ui.button(UiText.literal("Normal"), () -> {
                        density.set(UiDensity.NORMAL);
                        host.get().scalePolicy(UiScalePolicy.fixed(UiDensity.NORMAL));
                    }).variant(Ui.ButtonVariant.SECONDARY))
                    .add(Ui.button(UiText.literal("Compact"), () -> {
                        density.set(UiDensity.COMPACT);
                        host.get().scalePolicy(UiScalePolicy.fixed(UiDensity.COMPACT));
                    }).variant(Ui.ButtonVariant.SECONDARY)))
                .add(Ui.custom().preferredHeight(24)
                    .render((renderer, bounds, theme) -> renderer.drawText(
                        UiText.literal("Selected density: " + density.get()), bounds.x(), bounds.y(),
                        theme.palette().textSecondary()))
                    .build()))
            .add(Ui.section(UiText.literal("DENSITY"))
                .add(Ui.row().gap(8)
                    .add(Ui.badge(UiText.literal("COMFORTABLE")))
                    .add(Ui.badge(UiText.literal("NORMAL")).tone(UiBadge.Tone.ACCENT))
                    .add(Ui.badge(UiText.literal("COMPACT")).tone(UiBadge.Tone.SUCCESS))))
            .add(Ui.label(UiText.literal("Try the states on the Input and Feedback pages while changing Minecraft GUI Scale."))
                .wrap(true));
    }

    private static Ui.Node themeStateRow(String title, UiTheme theme) {
        int[] colors = {
            theme.palette().control(), theme.palette().controlHover(),
            theme.palette().controlPressed(), theme.palette().focusRing(),
            theme.palette().controlDisabled(), theme.palette().accent()
        };
        String[] labels = {"normal", "hover", "pressed", "focused", "disabled", "selected"};
        Ui.Row row = Ui.row().gap(6);
        for (int index = 0; index < colors.length; index++) {
            final int color = colors[index];
            final String label = labels[index];
            row.add(Ui.custom().preferredWidth(86).preferredHeight(30)
                .render((renderer, bounds, ignored) -> {
                    renderer.fillRoundRect(bounds, 6, color);
                    renderer.drawCenteredText(UiText.literal(label),
                        bounds.x() + bounds.width() / 2, bounds.y() + 10,
                        color == theme.palette().controlDisabled()
                            ? theme.palette().textPrimary() : theme.palette().onAccent());
                }).build());
        }
        return Ui.column().gap(4)
            .add(Ui.label(UiText.literal(title)))
            .add(row);
    }

    private static Ui.Node themeSwatch(String name, UiTheme theme) {
        return Ui.panel().padding(10)
            .add(Ui.row().gap(8)
                .add(Ui.badge(UiText.literal(name)).tone(UiBadge.Tone.ACCENT))
                .add(Ui.custom().preferredWidth(42).preferredHeight(24)
                    .render((renderer, bounds, ignored) -> renderer.fillRoundRect(bounds, 6, theme.palette().accent()))
                    .build())
                .add(Ui.label(UiText.literal("surface / control / accent"))));
    }

    private static Ui.Node templatesPage(UiDialogHost dialogs) {
        UiPageHost sidebarPages = Ui.pageHost()
            .addPage(UiText.literal("Settings"), Ui.section(UiText.literal("SETTINGS"))
                .add(Ui.settingRow(UiText.literal("Example"), Ui.toggle(UiText.literal("Enabled"),
                    UiBinding.of(() -> true, ignored -> { })))))
            .addPage(UiText.literal("Advanced"), Ui.section(UiText.literal("ADVANCED"))
                .add(Ui.label(UiText.literal("The sidebar stays fixed while the selected page scrolls."))));
        UiScaffold sidebar = Ui.scaffold(sidebarPages)
            .header(UiHeader.compact(UiText.literal("Sidebar template")))
            .sidebar(sidebarPages.navigation())
            .footer(Ui.row().add(Ui.button(UiText.literal("Done"), () -> { })))
            .sidebarWidth(138);
        UiScaffold editor = Ui.scaffold(
            Ui.column().gap(8)
                .add(Ui.row().gap(8).add(Ui.button(UiText.literal("Tool"), () -> { })).add(Ui.badge(UiText.literal("READY"))))
                .add(Ui.split(Ui.panel().padding(10).add(Ui.label(UiText.literal("Editor"))),
                    Ui.preview((renderer, bounds, clip, theme) -> renderer.fillRoundRect(bounds, 8, theme.palette().accent()))
                        .preferredWidth(120).preferredHeight(70)).gap(8))
                .add(Ui.alert(UiFeedbackType.INFO, UiText.literal("Status area"))))
            .header(UiHeader.text(UiText.literal("Editor template")))
            .footer(Ui.row().add(Ui.button(UiText.literal("Open dialog"), () -> dialogs.show(dialogContent(dialogs)))));
        return Ui.column().gap(14)
            .add(Ui.section(UiText.literal("SIDEBAR CONFIGURATION" )).add(sidebar))
            .add(Ui.section(UiText.literal("TOOL / EDITOR / STATUS")).add(editor));
    }

    private static Ui.Node generalPage(AtomicBoolean enabled, AtomicReference<Double> scale,
                                       AtomicReference<String> mode, AtomicReference<UiHost> host, UiDialogHost dialogs) {
        UiSetting<Double> scaleSetting = UiSetting.of(binding(scale::get, scale::set), 1.0)
            .describedBy(UiText.literal("Controls the visual scale used by this demo."));
        UiNumberSpec<Double> scaleSpec = UiNumberSpec.builder(UiNumberSpec.DOUBLE)
            .range(0.5, 2.0).step(0.1).formatter(value -> String.format(java.util.Locale.ROOT, "%.1f", value)).build();
        AtomicReference<String> profile = new AtomicReference<>("Rethink");
        AtomicReference<String> filter = new AtomicReference<>("");
        AtomicReference<String> selectedPreset = new AtomicReference<>("Balanced");
        AtomicReference<List<String>> blockFilters = new AtomicReference<>(List.of("minecraft:stone", "minecraft:dirt"));
        AtomicReference<Integer> precision = new AtomicReference<>(25);
        UiSetting<Integer> precisionSetting = UiSetting.of(binding(precision::get, precision::set), 25);
        UiNumberSpec<Integer> precisionSpec = UiNumberSpec.builder(UiNumberSpec.INTEGER).range(0, 100).step(5).build();
        Ui.Container settings = Ui.section(UiText.literal("GENERAL"));
        settings.add(Ui.settingRow(UiText.literal("Enable preview"),
                Ui.toggle(UiText.literal(""), binding(enabled::get, enabled::set)))
                .description(UiText.literal("Render a live preview in this screen.")))
            .add(Ui.settingRow(UiText.literal("Interface scale"),
                Ui.numberControl(scaleSetting, scaleSpec))
                .description(UiText.literal("A single control keeps the slider and 0.1-step numeric value synchronized.")))
            .add(Ui.settingRow(UiText.literal("Render mode"),
                Ui.select(UiText.literal("Mode"), binding(mode::get, mode::set),
                    List.of("Fast", "Balanced", "Quality"), UiText::literal))
                .description(UiText.literal("Select values with the mouse, keyboard or controller.")));

        Ui.Container inputs = Ui.section(UiText.literal("INPUTS"));
        inputs.add(Ui.formField(UiText.literal("Profile name"),
                Ui.textField(binding(profile::get, profile::set)).placeholder(UiText.literal("Type a name"))
                    .validator(value -> value.trim().isEmpty() ? UiValidationResult.error(UiText.literal("A name is required")) : UiValidationResult.OK))
                .description(UiText.literal("Text commits on Enter or when the field loses focus.")))
            .add(Ui.formField(UiText.literal("Precision"), Ui.numericField(precisionSetting, precisionSpec))
                .description(UiText.literal("A separate integer setting that snaps to increments of 5.")));

        Ui.Container feedback = Ui.section(UiText.literal("FEEDBACK"));
        Ui.Row toastButtons = Ui.row().gap(8);
        toastButtons.add(Ui.button(UiText.literal("Show info"),
                () -> host.get().showToast(UiToast.info(UiText.literal("This is an information toast."))))
                .variant(Ui.ButtonVariant.SECONDARY))
            .add(Ui.button(UiText.literal("Show warning"),
                () -> host.get().showToast(UiToast.warning(UiText.literal("This is a warning toast."))))
                .variant(Ui.ButtonVariant.SECONDARY))
            .add(Ui.button(UiText.literal("Show error"),
                () -> host.get().showToast(UiToast.error(UiText.literal("This is an error toast."))))
                .variant(Ui.ButtonVariant.DANGER));
        feedback.add(Ui.alert(UiFeedbackType.INFO, UiText.literal("Info: this page uses optional UiSetting metadata.")))
            .add(Ui.alert(UiFeedbackType.SUCCESS, UiText.literal("Success: valid values write back immediately when committed.")))
            .add(Ui.alert(UiFeedbackType.WARNING, UiText.literal("Warning: persistence remains the responsibility of the host mod.")))
            .add(Ui.alert(UiFeedbackType.ERROR, UiText.literal("Error: invalid input remains visible instead of silently changing the value.")))
            .add(toastButtons);

        Ui.Container list = Ui.section(UiText.literal("PRESETS"));
        list.add(Ui.searchField(binding(filter::get, filter::set)).placeholder(UiText.literal("Filter presets")))
            .add(Ui.selectionList(() -> List.of("Fast", "Balanced", "Quality", "Cinematic").stream()
                    .filter(value -> value.toLowerCase(java.util.Locale.ROOT).contains(filter.get().toLowerCase(java.util.Locale.ROOT))).toList(),
                binding(selectedPreset::get, selectedPreset::set), UiText::literal).emptyText(UiText.literal("No matching preset")));

        UiListEntryAdapter<String> blockFilterAdapter = UiListEntryAdapter.builder(
                () -> "", UiText::literal,
                entry -> Ui.textField(entry).placeholder(UiText.literal("namespace:path"))
                    .escapeCancels(false).validator(DemoScreen::validateResourceId))
            .validator(DemoScreen::validateResourceId)
            .uniqueValues()
            .build();
        Ui.Container editableList = Ui.section(UiText.literal("BLOCK FILTERS"));
        editableList.add(Ui.collectionEditor(dialogs, UiText.literal("Block filters"),
            UiListSetting.of(UiSetting.of(binding(blockFilters::get, blockFilters::set), List.of())), blockFilterAdapter));

        Ui.Container actions = Ui.section(UiText.literal("ACTIONS"));
        actions.add(Ui.row().gap(8)
                .add(Ui.button(UiText.literal("Reset"), () -> {
                    enabled.set(true);
                    scale.set(1.0);
                    mode.set("Balanced");
                }).variant(Ui.ButtonVariant.SECONDARY))
                .add(Ui.button(UiText.literal("Apply"), () -> { })))
            .add(Ui.tooltip(Ui.label(UiText.literal("Changes apply immediately")),
                UiText.literal("The host decides when and how values are persisted.\n"
                    + "This tooltip wraps complete paragraphs and stays inside the screen."))
                .maxWidth(300)
                .overflow(com.rethinkqaq.configui.core.component.UiTooltip.TextOverflow.WRAP))
            .add(Ui.tooltip(Ui.label(UiText.literal("Rich tooltip preview")),
                Ui.panel().padding(8)
                    .add(Ui.label(UiText.literal("Preview content")).wrap(false))
                    .add(Ui.badge(UiText.literal("READ ONLY"))))
                .maxWidth(220));

        return Ui.column().gap(14)
            .add(Ui.tooltip(
                Ui.label(UiText.literal("A responsive page: navigation stays visible while the selected content scrolls."))
                    .wrap(true),
                UiText.literal("Only the main content area scrolls; the header and category navigation remain available.")))
            .add(settings)
            .add(inputs)
            .add(feedback)
            .add(list)
            .add(editableList)
            .add(actions);
    }

    private static Ui.Node previewPage(UiDialogHost dialogs) {
        AtomicReference<Integer> selected = new AtomicReference<>(0);
        UiGrid cards = Ui.grid().minimumColumnWidth(168).maximumColumnWidth(188).gap(12)
            .rowAlignment(com.rethinkqaq.configui.core.UiMainAxisAlignment.START);
        cards.add(previewCard("No action", "Card click only", selected, 0, null, null));
        cards.add(previewCard("One action", "One full-width action", selected, 1,
            Ui.button(UiText.literal("Select"), () -> selected.set(1)), null));
        cards.add(previewCard("Two actions", "Actions share the row", selected, 2,
            Ui.row().gap(6).equalChildWidths(true)
                .add(Ui.button(UiText.literal("Select"), () -> selected.set(2)))
                .add(Ui.button(UiText.literal("Create"), () -> { })), null));
        cards.add(previewCard("Tooltip", "Hover the card for details", selected, 3,
            Ui.button(UiText.literal("Manage"), () -> { }),
            UiText.literal("Every card has a bounded preview slot and an independent action area.")));
        cards.add(previewCard("Dialog", "Open a modal above content", selected, 4,
            Ui.button(UiText.literal("Open dialog"), () -> dialogs.show(dialogContent(dialogs))), null));
        cards.add(previewCard("Clip probe", "The preview intentionally overdraws", selected, 5,
            Ui.button(UiText.literal("Select"), () -> selected.set(5)), null));

        return Ui.column().gap(14)
            .add(Ui.label(UiText.literal("Preview card regression: click any card, hover for a tooltip, and scroll this page.")))
            .add(cards)
            .add(Ui.section(UiText.literal("CLIP PROBE"))
                .add(Ui.label(UiText.literal("The pink preview rectangles deliberately extend beyond their slots. No color should escape a card or the content viewport."))
                    .wrap(true)));
    }

    private static Ui.Node previewCard(String title, String description, AtomicReference<Integer> selected,
                                       int id, Ui.Node action, UiText tooltip) {
        UiPreviewCard card = Ui.previewCard(UiText.literal(title), oversizedPreview(id))
            .description(UiText.literal(description))
            .onClick(() -> selected.set(id))
            .selected(() -> selected.get() == id);
        if (action != null) card.action(action);
        Ui.Node result = card;
        if (tooltip != null) result = Ui.tooltip(result, tooltip);
        return result;
    }

    private static Ui.Node oversizedPreview(int id) {
        int color = switch (id % 3) {
            case 0 -> 0xFFF39ABA;
            case 1 -> 0xFF9ABAF3;
            default -> 0xFF9AF3BA;
        };
        return new MinecraftPreview((graphics, bounds, clip) -> {
            int left = Math.round(bounds.x());
            int top = Math.round(bounds.y());
            int right = Math.round(bounds.x() + bounds.width());
            int bottom = Math.round(bounds.y() + bounds.height());
            int centerX = (left + right) / 2;
            int centerY = (top + bottom) / 2;
            int size = Math.max(right - left, bottom - top);
            graphics.fill(centerX - size, centerY - size, centerX + size, centerY + size, 0xFFFCE8F0);
            graphics.fill(centerX - size / 3, centerY - size / 3,
                centerX + size / 3, centerY + size / 3, color);
        }).preferredHeight(92);
    }

    private static Ui.Node dialogContent(UiDialogHost dialogs) {
        return Ui.panel().padding(18)
            .add(Ui.section(UiText.literal("DIALOG REGRESSION"))
                .add(Ui.label(UiText.literal("This dialog is rendered above the page and is not clipped by the scrolling content."))
                    .wrap(true))
                .add(Ui.button(UiText.literal("Close"), dialogs::close)));
    }
    private static Ui.Node advancedPage() {
        AtomicBoolean customEnabled = new AtomicBoolean(true);
        Ui.Column dynamicNodes = Ui.column().gap(6);
        AtomicReference<Ui.Node> dynamicNode = new AtomicReference<>();
        Ui.Node customSurface = Ui.custom()
            .preferredHeight(48)
            .render((renderer, bounds, theme) -> {
                renderer.fillRoundRect(bounds, theme.metrics().controlRadius(), theme.palette().surfaceRaised());
                renderer.drawCenteredText(UiText.literal("Ui.custom() extension point"),
                    bounds.x() + bounds.width() / 2f, bounds.y() + 14, theme.palette().textPrimary());
            })
            .click((x, y, button) -> { customEnabled.set(!customEnabled.get()); return true; })
            .build()
            .visible(true);
        Ui.Container disabled = Ui.section(UiText.literal("STATES"));
        disabled.add(Ui.settingRow(UiText.literal("Disabled toggle"),
                Ui.toggle(UiText.literal("Unavailable"), UiBinding.of(() -> false, value -> { }))
                    .enabled(false))
                .description(UiText.literal("Disabled controls retain their layout and remain readable.")))
            .add(Ui.button(UiText.literal("Disabled action"), () -> { })
                .enabled(false));
        return Ui.column().gap(14)
            .add(Ui.section(UiText.literal("ADVANCED"))
                .add(Ui.label(UiText.literal("Any core node can be composed into a secondary page."))
                    .wrap(true))
                .add(Ui.tooltip(customSurface, UiText.literal("A platform-neutral custom node with its own measure, render and click contract.")))
                .add(new DemoComponent())
                .add(Ui.button(UiText.literal("Toggle custom visibility"),
                    () -> customSurface.visible(!customSurface.visible())))
                .add(Ui.label(UiText.literal("Toggle visibility, then hover and click the surface again to verify that hidden nodes do not receive input."))
                    .wrap(true))
                .add(Ui.button(UiText.literal("Add / remove dynamic node"), () -> {
                    Ui.Node current = dynamicNode.get();
                    if (current == null) {
                        current = Ui.badge(UiText.literal("Dynamically mounted node"));
                        dynamicNode.set(current);
                        dynamicNodes.add(current);
                    } else {
                        dynamicNodes.remove(current);
                        dynamicNode.set(null);
                    }
                }))
                .add(dynamicNodes))
            .add(disabled)
            .add(Ui.badge(UiText.literal("Theme override ready")).tone(UiBadge.Tone.ACCENT));
    }

    /** Small reusable state-free component used to document the UiComponent extension point. */
    private static final class DemoComponent extends UiComponent {
        private final Ui.Column content = Ui.column().gap(4)
            .add(Ui.badge(UiText.literal("UiComponent" )).tone(UiBadge.Tone.ACCENT))
            .add(Ui.label(UiText.literal("Owns a child tree and forwards measure, layout and render."))
                .wrap(true));

        private DemoComponent() { child(content); }

        @Override
        protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
            content.measure(renderer, maxWidth, maxHeight, theme);
            measuredWidth = content.measuredWidth();
            measuredHeight = content.measuredHeight();
        }

        @Override
        public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
            super.layout(renderer, value, theme);
            content.layout(renderer, value, theme);
        }

        @Override
        public void render(UiRenderer renderer, UiTheme theme) { content.render(renderer, theme); }
    }

    /** Gives an independently scrolling child a bounded viewport for the layout showcase. */
    private static final class FixedViewport extends UiComponent {
        private final Ui.Node content;
        private final float height;

        private FixedViewport(Ui.Node content, float height) {
            this.content = child(content);
            this.height = height;
        }

        @Override
        protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
            content.measure(renderer, maxWidth, height, theme);
            measuredWidth = content.measuredWidth();
            measuredHeight = Math.min(maxHeight, height);
        }

        @Override
        public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
            super.layout(renderer, value, theme);
            content.layout(renderer, new UiBounds(value.x(), value.y(), value.width(),
                Math.min(value.height(), height)), theme);
        }

        @Override
        public void render(UiRenderer renderer, UiTheme theme) { content.render(renderer, theme); }
    }

    private static <T> UiBinding<T> binding(java.util.function.Supplier<T> getter,
                                            java.util.function.Consumer<T> setter) {
        return UiBinding.of(getter, setter);
    }

    private static UiValidationResult validateResourceId(String value) {
        return value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") ? UiValidationResult.OK
            : UiValidationResult.error(UiText.literal("Use a lowercase namespace:path identifier"));
    }

}
