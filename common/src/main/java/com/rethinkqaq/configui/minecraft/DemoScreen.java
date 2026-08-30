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
import com.rethinkqaq.configui.core.UiDialogHost;
import com.rethinkqaq.configui.core.UiGrid;
import com.rethinkqaq.configui.core.UiPageHost;
import com.rethinkqaq.configui.core.UiPreviewCard;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;
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
            .addPage(UiText.literal("Preview"), previewPage(dialogs))
            .addPage(UiText.literal("Advanced"), advancedPage());

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
            .alignment(UiGrid.Alignment.START);
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
                    .wrap(true)))
            .add(disabled)
            .add(Ui.badge(UiText.literal("Theme override ready")).tone(UiBadge.Tone.ACCENT));
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
