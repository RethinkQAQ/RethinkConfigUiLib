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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rethinkqaq.configui.core.component.data.UiSelectionList;
import com.rethinkqaq.configui.core.component.data.UiCollectionEditor;
import com.rethinkqaq.configui.core.component.data.UiListEntryAdapter;
import com.rethinkqaq.configui.core.component.feedback.UiAlert;
import com.rethinkqaq.configui.core.component.feedback.UiFeedbackType;
import com.rethinkqaq.configui.core.component.feedback.UiNotificationCenter;
import com.rethinkqaq.configui.core.component.feedback.UiToast;
import com.rethinkqaq.configui.core.component.input.UiNumericField;
import com.rethinkqaq.configui.core.component.input.UiNumberControl;
import com.rethinkqaq.configui.core.component.input.UiTextField;
import com.rethinkqaq.configui.core.component.input.UiFormField;
import com.rethinkqaq.configui.core.layout.UiHeader;
import com.rethinkqaq.configui.core.layout.UiHeaderStyle;
import com.rethinkqaq.configui.core.setting.UiListSetting;
import com.rethinkqaq.configui.core.setting.UiNumberSpec;
import com.rethinkqaq.configui.core.setting.UiSetting;
import com.rethinkqaq.configui.core.setting.UiValidationResult;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class UiConfigurationComponentsTest {
    @Test
    void settingResetsAndRejectsErrorsButAcceptsWarnings() {
        AtomicInteger value = new AtomicInteger(3);
        UiSetting<Integer> setting = UiSetting.of(UiBinding.of(value::get, value::set), 5)
            .validatedBy(candidate -> candidate < 0 ? UiValidationResult.error(UiText.literal("negative"))
                : candidate > 10 ? UiValidationResult.warning(UiText.literal("large")) : UiValidationResult.OK);
        assertFalse(setting.set(-1).accepted());
        assertEquals(3, value.get());
        assertTrue(setting.set(11).accepted());
        assertEquals(11, value.get());
        setting.reset();
        assertTrue(setting.isDefault());
    }

    @Test
    void textInputSupportsUnicodeClipboardAndEscape() {
        AtomicReference<String> value = new AtomicReference<>("one");
        UiTextField field = Ui.textField(UiBinding.of(value::get, value::set)).maxLength(8);
        UiClipboard clipboard = UiClipboard.memory();
        field.setFocused(true);
        assertTrue(field.textInput(new UiTextInput('猫', 0), clipboard));
        assertTrue(field.key(new UiKeyEvent(UiKey.A, 0, UiKey.MOD_CONTROL), clipboard));
        assertTrue(field.key(new UiKeyEvent(UiKey.X, 0, UiKey.MOD_CONTROL), clipboard));
        assertEquals("one猫", clipboard.get());
        assertTrue(field.key(new UiKeyEvent(UiKey.V, 0, UiKey.MOD_CONTROL), clipboard));
        assertTrue(field.key(new UiKeyEvent(UiKey.ENTER, 0, 0), clipboard));
        assertEquals("one猫", value.get());
        field.textInput(new UiTextInput('x', 0), clipboard);
        field.key(new UiKeyEvent(UiKey.ESCAPE, 0, 0), clipboard);
        assertEquals("one猫", field.draft());
    }

    @Test
    void textFieldCanOfferEscapeToItsEnclosingPage() {
        UiTextField field = Ui.textField(UiBinding.of(() -> "value", value -> { })).escapeCancels(false);
        assertFalse(field.key(new UiKeyEvent(UiKey.ESCAPE, 0, 0), UiClipboard.memory()));
    }

    @Test
    void textFieldSubmitOnlyRunsForEnterNotFocusLoss() {
        AtomicInteger submits = new AtomicInteger();
        AtomicReference<String> value = new AtomicReference<>("");
        UiTextField field = Ui.textField(UiBinding.of(value::get, value::set)).onSubmit(submits::incrementAndGet);
        field.setFocused(true);
        field.textInput(new UiTextInput('a', 0), UiClipboard.memory());
        field.setFocused(false);
        assertEquals("a", value.get());
        assertEquals(0, submits.get());
        field.setFocused(true);
        assertTrue(field.key(new UiKeyEvent(UiKey.ENTER, 0, 0), UiClipboard.memory()));
        assertEquals(1, submits.get());
    }

    @Test
    void numberFieldSnapsToStepAndKeepsInvalidDraft() {
        AtomicReference<Double> value = new AtomicReference<>(1.0);
        UiSetting<Double> setting = UiSetting.of(UiBinding.of(value::get, value::set), 1.0);
        UiNumberSpec<Double> spec = UiNumberSpec.builder(UiNumberSpec.DOUBLE).range(0, 2).step(.25).build();
        UiNumericField<Double> field = Ui.numericField(setting, spec);
        UiClipboard clipboard = UiClipboard.memory();
        field.setFocused(true);
        field.key(new UiKeyEvent(UiKey.A, 0, UiKey.MOD_CONTROL), clipboard);
        field.textInput(new UiTextInput('1', 0), clipboard);
        field.textInput(new UiTextInput('.', 0), clipboard);
        field.textInput(new UiTextInput('1', 0), clipboard);
        assertTrue(field.commit());
        assertEquals(1.0, value.get());
        field.key(new UiKeyEvent(UiKey.A, 0, UiKey.MOD_CONTROL), clipboard);
        field.textInput(new UiTextInput('9', 0), clipboard);
        assertFalse(field.commit());
        assertEquals(1.0, value.get());
        assertEquals("9", field.draft());
    }

    @Test
    void floatNumberFieldAcceptsDecimalStepsWithoutBinaryPrecisionLoss() {
        AtomicReference<Float> value = new AtomicReference<>(0.05F);
        UiSetting<Float> setting = UiSetting.of(UiBinding.of(value::get, value::set), 0.05F);
        UiNumberSpec<Float> spec = UiNumberSpec.builder(UiNumberSpec.FLOAT).range(.05, 1).step(.05).build();
        UiNumericField<Float> field = Ui.numericField(setting, spec);
        UiClipboard clipboard = UiClipboard.memory();
        field.setFocused(true);
        field.key(new UiKeyEvent(UiKey.A, 0, UiKey.MOD_CONTROL), clipboard);
        field.textInput(new UiTextInput('0', 0), clipboard);
        field.textInput(new UiTextInput('.', 0), clipboard);
        field.textInput(new UiTextInput('1', 0), clipboard);
        assertTrue(field.commit());
        assertEquals(.1F, value.get());
        assertEquals("0.1", field.draft());
    }

    @Test
    void numberControlKeepsSliderAndInputOnOneRow() {
        AtomicInteger value = new AtomicInteger(2);
        UiSetting<Integer> setting = UiSetting.of(UiBinding.of(value::get, value::set), 2);
        UiNumberControl<Integer> field = Ui.numberControl(setting, UiNumberSpec.builder(UiNumberSpec.INTEGER).range(0, 10).step(1).build());
        field.measure(RENDERER, 200, 200, UiTheme.roseLight());
        field.layout(RENDERER, new UiBounds(0, 0, 200, field.measuredHeight()), UiTheme.roseLight());
        assertEquals(field.childNodes().get(0).bounds().y(), field.childNodes().get(1).bounds().y());
    }

    @Test
    void numberControlForwardsPointerInputAndRefreshesItsField() {
        AtomicInteger value = new AtomicInteger(2);
        UiSetting<Integer> setting = UiSetting.of(UiBinding.of(value::get, value::set), 2);
        UiNumberControl<Integer> field = Ui.numberControl(setting, UiNumberSpec.builder(UiNumberSpec.INTEGER).range(0, 10).step(1).build());
        field.measure(RENDERER, 420, 200, UiTheme.roseLight());
        field.layout(RENDERER, new UiBounds(0, 0, 420, field.measuredHeight()), UiTheme.roseLight());
        assertTrue(field.click(200, 15, 0));
        assertTrue(field.drag(300, 15, 0));
        assertTrue(field.release(300, 15, 0));
        assertTrue(value.get() > 2);
        field.render(RENDERER, UiTheme.roseLight());
        assertEquals(Integer.toString(value.get()), field.field().draft());
    }

    @Test
    void listSettingWritesCopiedCollections() {
        AtomicReference<List<String>> value = new AtomicReference<>(List.of("minecraft:stone"));
        UiListSetting<String> setting = UiListSetting.of(UiSetting.of(UiBinding.of(value::get, value::set), List.of()));
        assertTrue(setting.add("minecraft:dirt").accepted());
        assertEquals(List.of("minecraft:stone", "minecraft:dirt"), value.get());
        assertTrue(setting.update(0, "minecraft:grass_block").accepted());
        assertEquals(List.of("minecraft:grass_block", "minecraft:dirt"), value.get());
        assertTrue(setting.remove(1).accepted());
        assertEquals(List.of("minecraft:grass_block"), setting.items());
    }

    @Test
    void collectionEditorUsesModalDialogAndCanRejectDuplicates() {
        AtomicReference<List<String>> value = new AtomicReference<>(List.of("minecraft:stone"));
        UiListSetting<String> setting = UiListSetting.of(UiSetting.of(UiBinding.of(value::get, value::set), List.of()));
        UiDialogHost dialogs = Ui.dialogHost(Ui.label(UiText.literal("General")));
        UiListEntryAdapter<String> adapter = UiListEntryAdapter.builder(() -> "minecraft:stone", UiText::literal,
            binding -> Ui.textField(binding)).uniqueValues().build();
        UiCollectionEditor<String> editor = Ui.collectionEditor(dialogs, UiText.literal("Block filters"), setting, adapter);
        editor.measure(RENDERER, 240, 100, UiTheme.roseLight());
        editor.layout(RENDERER, new UiBounds(0, 0, 240, editor.measuredHeight()), UiTheme.roseLight());
        assertTrue(editor.click(10, 10, 0));
        assertTrue(dialogs.showingDialog());
        assertTrue(dialogs.close());
        assertFalse(dialogs.showingDialog());
    }

    @Test
    void formFieldStacksItsControlBelowItsLabelsAtCompactWidths() {
        UiFormField field = Ui.formField(UiText.literal("Profile"), Ui.textField(UiBinding.of(() -> "", value -> { })))
            .description(UiText.literal("An optional profile name."));
        field.measure(RENDERER, 200, 200, UiTheme.roseLight());
        field.layout(RENDERER, new UiBounds(0, 0, 200, field.measuredHeight()), UiTheme.roseLight());
        assertTrue(field.control().bounds().y() > field.bounds().y());
    }

    @Test
    void listUsesKeyboardSelectionAndEmptyState() {
        AtomicReference<String> selected = new AtomicReference<>("one");
        UiSelectionList<String> list = Ui.selectionList(() -> List.of("one", "two"), UiBinding.of(selected::get, selected::set), UiText::literal);
        list.measure(RENDERER, 120, 200, UiTheme.roseLight());
        list.layout(RENDERER, new UiBounds(0, 0, 120, list.measuredHeight()), UiTheme.roseLight());
        assertTrue(list.key(UiKey.DOWN));
        assertEquals("two", selected.get());
    }

    @Test
    void feedbackSurfacesUseSemanticTokensAndToastsExpire() throws InterruptedException {
        UiAlert alert = Ui.alert(UiFeedbackType.WARNING, UiText.literal("Careful"));
        alert.measure(RENDERER, 100, 100, UiTheme.roseLight());
        alert.layout(RENDERER, new UiBounds(0, 0, 100, alert.measuredHeight()), UiTheme.roseLight());
        alert.render(RENDERER, UiTheme.roseLight());
        UiNotificationCenter center = new UiNotificationCenter();
        center.show(new UiToast(UiFeedbackType.INFO, UiText.literal("Short"), 1, 0));
        center.render(RENDERER, 200, 200, UiTheme.roseLight());
        Thread.sleep(2);
        center.render(RENDERER, 200, 200, UiTheme.roseLight());
        assertEquals(0, center.size());
    }

    @Test
    void toastSemanticColoursAreFixedAndCustomToastsKeepTheirOwnColour() {
        assertEquals(0xFF3B82F6, UiToast.info(UiText.literal("Info")).color());
        assertEquals(0xFFDC3C3C, UiToast.error(UiText.literal("Error")).color());
        assertEquals(0xFF7C3AED, UiToast.custom(UiText.literal("Custom"), 0xFF7C3AED).color());
    }

    @Test
    void tooltipUsesDelayAndCanBeShownImmediatelyForHostsThatNeedIt() {
        Ui.Tooltip tooltip = Ui.tooltip(Ui.label(UiText.literal("Target")), UiText.literal("Help"));
        tooltip.setHovered(true);
        assertFalse(tooltip.visible(System.nanoTime()));
        tooltip.delayMillis(0);
        assertTrue(tooltip.visible(System.nanoTime()));
    }

    @Test
    void tooltipTextSupportsExplicitLinesAndConfigurableOverflow() {
        List<UiText> lines = Ui.wrapLines(RENDERER,
            UiText.literal("First line\nSecond line with more words"), 60, Integer.MAX_VALUE, false);
        assertEquals(List.of("First line", "Second", "line with", "more words"),
            lines.stream().map(UiText::value).toList());

        Ui.Tooltip tooltip = Ui.tooltip(Ui.label(UiText.literal("Target")), UiText.literal("Help"))
            .maxWidth(360).maxLines(2).overflow(com.rethinkqaq.configui.core.component.UiTooltip.TextOverflow.ELLIPSIS);
        assertEquals(360, tooltip.maxWidth());
        assertEquals(2, tooltip.maxLines());
    }

    @Test
    void tooltipCanUseReadOnlyComponentContent() {
        Ui.Tooltip tooltip = Ui.tooltip(Ui.label(UiText.literal("Target")),
            com.rethinkqaq.configui.core.component.UiTooltipContent.node(Ui.badge(UiText.literal("INFO"))));
        assertTrue(tooltip.hasContent());
        assertFalse(tooltip.hasText());
    }

    @Test
    void backgroundModesPreserveOpaqueAndTransparentSemantics() {
        assertEquals(UiBackground.Mode.OPAQUE, UiBackground.opaque(0x00112233).mode());
        assertEquals(0xFF112233, UiBackground.opaque(0x00112233).color());
        assertTrue(UiBackground.translucent(0x88112233).paintsSurface());
        assertFalse(UiBackground.transparent().paintsSurface());
    }

    @Test
    void headerStylesMeasureCompactlyAndNoneConsumesNoSpace() {
        UiTheme theme = UiTheme.roseLight();
        UiHeader text = UiHeader.text(UiText.literal("Title"))
            .subtitle(UiText.literal("Description"));
        text.measure(RENDERER, 240, 200, theme);
        assertEquals(UiHeaderStyle.TEXT, text.resolvedStyle());
        assertEquals(33.5f, text.measuredHeight());

        UiHeader none = UiHeader.text(UiText.literal("Hidden")).style(UiHeaderStyle.NONE);
        none.measure(RENDERER, 240, 200, theme);
        assertEquals(0f, none.measuredHeight());
    }

    @Test
    void responsiveHeaderFallsBackToTextAtCompactWidth() {
        UiHeader header = UiHeader.card(UiText.literal("Title")).responsive(true);
        header.measure(RENDERER, 400, 200, UiTheme.roseLight());
        assertEquals(UiHeaderStyle.TEXT, header.resolvedStyle());
    }

    private static final UiRenderer RENDERER = new UiRenderer() {
        @Override public void fillRect(UiBounds bounds, int color) { }
        @Override public void fillRoundRect(UiBounds bounds, float radius, int color) { }
        @Override public void strokeRoundRect(UiBounds bounds, float radius, float width, int color) { }
        @Override public void drawText(UiText text, float x, float y, int color) { }
        @Override public float textWidth(UiText text) { return text.value().length() * 6f; }
        @Override public float lineHeight() { return 10; }
        @Override public void pushClip(UiBounds bounds) { }
        @Override public void popClip() { }
    };
}
