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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class UiBindingTest {
    @Test
    void bindingWritesImmediately() {
        AtomicBoolean value = new AtomicBoolean();
        UiBinding<Boolean> binding = UiBinding.of(value::get, value::set);
        binding.set(true);
        assertEquals(true, value.get());
    }

    @Test
    void roseThemeCanOverrideAccent() {
        assertEquals(0xFF112233, UiTheme.roseLight().withAccent(0xFF112233).palette().accent());
    }

    @Test
    void paletteBuilderOverridesOnlyRequestedTokens() {
        UiTheme.UiPalette palette = UiTheme.UiPalette.builder()
            .background(0xFF010203)
            .danger(0xFFAA0000)
            .build();
        assertEquals(0xFF010203, palette.background());
        assertEquals(0xFFAA0000, palette.danger());
        assertEquals(UiTheme.UiPalette.roseLight().accent(), palette.accent());
    }

    @Test
    void columnMeasuresAndLaysOutChildren() {
        Ui.Column column = Ui.column().gap(2);
        column.add(Ui.label(UiText.literal("A"))).add(Ui.label(UiText.literal("B")));
        column.measure(RENDERER, 100, 100, UiTheme.roseLight());
        column.layout(RENDERER, new UiBounds(0, 0, 100, column.measuredHeight()), UiTheme.roseLight());
        assertEquals(22, column.measuredHeight());
        assertEquals(0, column.children().get(0).bounds().y());
        assertEquals(12, column.children().get(1).bounds().y());
    }

    @Test
    void scrollViewKeepsFullContentHeightAndMovesItsChild() {
        Ui.Column content = Ui.column().gap(0);
        for (int index = 0; index < 5; index++) content.add(Ui.label(UiText.literal("Line" + index)));
        Ui.ScrollView view = Ui.scrollView(content);
        view.measure(RENDERER, 100, 20, UiTheme.roseLight());
        view.layout(RENDERER, new UiBounds(0, 0, 100, 20), UiTheme.roseLight());
        assertEquals(20, view.measuredHeight());
        assertTrue(view.scroll(10, 10, -1));
        view.layout(RENDERER, new UiBounds(0, 0, 100, 20), UiTheme.roseLight());
        assertEquals(-14, content.bounds().y());
    }

    @Test
    void disabledButtonCannotActivate() {
        AtomicInteger calls = new AtomicInteger();
        Ui.Button button = Ui.button(UiText.literal("Apply"), calls::incrementAndGet);
        button.enabled(false);
        button.measure(RENDERER, 100, 100, UiTheme.roseLight());
        button.layout(RENDERER, new UiBounds(0, 0, 100, 20), UiTheme.roseLight());
        assertFalse(button.click(10, 10, 0));
        assertEquals(0, calls.get());
    }

    @Test
    void toggleWritesThroughBindingImmediately() {
        AtomicBoolean value = new AtomicBoolean();
        Ui.Toggle toggle = Ui.toggle(UiText.literal("Enabled"), UiBinding.of(value::get, value::set));
        toggle.measure(RENDERER, 100, 100, UiTheme.roseLight());
        toggle.layout(RENDERER, new UiBounds(0, 0, 100, 20), UiTheme.roseLight());
        assertTrue(toggle.click(10, 10, 0));
        assertTrue(value.get());
    }

    @Test
    void sliderContinuesUpdatingWhileDraggedOutsideItsBounds() {
        AtomicInteger value = new AtomicInteger();
        Ui.Slider slider = Ui.slider(UiText.literal("Scale"), UiBinding.of(() -> (double) value.get(), next -> value.set(next.intValue())), 0, 100, 1);
        slider.measure(RENDERER, 100, 100, UiTheme.roseLight());
        slider.layout(RENDERER, new UiBounds(0, 0, 100, slider.measuredHeight()), UiTheme.roseLight());
        assertTrue(slider.click(20, 10, 0));
        assertTrue(slider.drag(120, 10, 0));
        assertEquals(100, value.get());
        assertTrue(slider.release(120, 10, 0));
        assertFalse(slider.drag(0, 10, 0));
    }

    @Test
    void roseThemeDoesNotDrawAFocusRingByDefault() {
        assertEquals(0, UiTheme.roseLight().palette().focusRing() >>> 24);
    }

    @Test
    void gridChoosesColumnsFromAvailableWidth() {
        UiGrid grid = Ui.grid().minimumColumnWidth(40).gap(4);
        grid.add(Ui.label(UiText.literal("One"))).add(Ui.label(UiText.literal("Two"))).add(Ui.label(UiText.literal("Three")));
        grid.measure(RENDERER, 130, 100, UiTheme.roseLight());
        grid.layout(RENDERER, new UiBounds(0, 0, 130, grid.measuredHeight()), UiTheme.roseLight());
        assertEquals(3, grid.columns());
        assertEquals(0, grid.children().get(0).bounds().x());
        assertTrue(grid.children().get(1).bounds().x() > 0);
    }

    @Test
    void settingRowStacksControlInCompactWidth() {
        UiSettingRow row = Ui.settingRow(UiText.literal("Feature"), Ui.toggle(UiText.literal(""), UiBinding.of(() -> false, value -> { })))
            .description(UiText.literal("Description"));
        row.measure(RENDERER, 300, 100, UiTheme.roseLight());
        row.layout(RENDERER, new UiBounds(0, 0, 300, row.measuredHeight()), UiTheme.roseLight());
        assertTrue(row.control().bounds().y() > 0);
    }

    @Test
    void scaffoldDoesNotReserveSpaceForAnAbsentFooter() {
        UiScaffold scaffold = Ui.scaffold(Ui.label(UiText.literal("Content")));
        scaffold.measure(RENDERER, 300, 120, UiTheme.roseLight());
        scaffold.layout(RENDERER, new UiBounds(0, 0, 300, 120), UiTheme.roseLight());
        assertEquals(120, scaffold.content().bounds().height());
    }

    @Test
    void scaffoldSeparatesHeaderAndCapsAndCentersWideShell() {
        Ui.Node header = Ui.label(UiText.literal("Header"));
        Ui.Node sidebar = Ui.label(UiText.literal("Navigation"));
        Ui.Node content = Ui.label(UiText.literal("Content"));
        UiScaffold scaffold = Ui.scaffold(content).header(header).sidebar(sidebar).maxContentWidth(800).regionGap(12);
        scaffold.measure(RENDERER, 1000, 200, UiTheme.roseLight());
        scaffold.layout(RENDERER, new UiBounds(0, 0, 1000, 200), UiTheme.roseLight());
        assertEquals(100, header.bounds().x());
        assertEquals(800, header.bounds().width());
        assertTrue(content.bounds().y() >= header.bounds().y() + header.bounds().height() + 12);
        assertTrue(content.bounds().x() >= sidebar.bounds().x() + sidebar.bounds().width() + 12);
    }

    @Test
    void toggleAndSliderInterpolateAndCanDisableMotion() {
        AtomicBoolean toggleValue = new AtomicBoolean(false);
        Ui.Toggle toggle = Ui.toggle(UiText.literal("Enabled"), UiBinding.of(toggleValue::get, toggleValue::set));
        toggle.advanceMotion(1, UiTheme.roseLight());
        toggleValue.set(true);
        toggle.advanceMotion(71_000_001, UiTheme.roseLight());
        assertTrue(toggle.onProgress() > 0 && toggle.onProgress() < 1);

        AtomicInteger sliderValue = new AtomicInteger(0);
        Ui.Slider slider = Ui.slider(UiText.literal("Scale"), UiBinding.of(() -> (double) sliderValue.get(), value -> sliderValue.set(value.intValue())), 0, 100, 1);
        slider.advanceMotion(1, UiTheme.roseLight());
        sliderValue.set(100);
        slider.advanceMotion(71_000_001, UiTheme.roseLight());
        assertTrue(slider.displayedRatio() > 0 && slider.displayedRatio() < 1);

        UiTheme instant = UiTheme.roseLight().withMotion(new UiTheme.UiMotion(0, 0, 0, 0));
        toggle.advanceMotion(72_000_001, instant);
        slider.advanceMotion(72_000_001, instant);
        assertEquals(1f, toggle.onProgress());
        assertEquals(1f, slider.displayedRatio());
    }

    @Test
    void splitLayoutStacksPanesInCompactWidth() {
        Ui.Node primary = Ui.label(UiText.literal("Primary"));
        Ui.Node secondary = Ui.label(UiText.literal("Secondary"));
        UiSplitLayout split = Ui.split(primary, secondary).compactBelow(400).gap(5);
        split.measure(RENDERER, 300, 100, UiTheme.roseLight());
        split.layout(RENDERER, new UiBounds(0, 0, 300, split.measuredHeight()), UiTheme.roseLight());
        assertEquals(0, primary.bounds().x());
        assertEquals(15, secondary.bounds().y());
    }

    @Test
    void topNavigationKeepsContentBelowOptionalRegions() {
        UiPageHost pages = Ui.pageHost()
            .addPage(UiText.literal("General"), Ui.column().add(Ui.label(UiText.literal("General content"))))
            .addPage(UiText.literal("Preview"), Ui.column().add(Ui.label(UiText.literal("Preview content"))));
        Ui.Node header = Ui.panel().add(Ui.label(UiText.literal("Header")));
        UiScaffold scaffold = Ui.scaffold(pages).header(header).navigation(pages.navigation())
            .navigationMode(UiScaffold.NavigationMode.TOP).regionGap(8);
        scaffold.measure(RENDERER, 300, 120, UiTheme.roseLight());
        scaffold.layout(RENDERER, new UiBounds(0, 0, 300, 120), UiTheme.roseLight());
        assertTrue(pages.navigation().bounds().y() >= header.bounds().y() + header.bounds().height() + 8);
        assertTrue(pages.bounds().y() >= pages.navigation().bounds().y() + pages.navigation().bounds().height() + 8);
        assertTrue(pages.bounds().height() >= 0);
    }

    @Test
    void scaffoldHidesOptionalHeaderOnlyBelowSmallViewportBreakpoint() {
        Ui.Node header = Ui.panel().add(Ui.label(UiText.literal("Header")));
        UiScaffold scaffold = Ui.scaffold(Ui.label(UiText.literal("Content")))
            .header(header).navigationMode(UiScaffold.NavigationMode.TOP);

        scaffold.measure(RENDERER, 320, 120, UiTheme.roseLight());
        scaffold.layout(RENDERER, new UiBounds(0, 0, 320, 120), UiTheme.roseLight());
        assertFalse(scaffold.headerVisible());
        assertEquals(0, scaffold.content().bounds().y());

        scaffold.measure(RENDERER, 360, 120, UiTheme.roseLight());
        scaffold.layout(RENDERER, new UiBounds(0, 0, 360, 120), UiTheme.roseLight());
        assertTrue(scaffold.headerVisible());
        assertTrue(scaffold.content().bounds().y() > header.bounds().y());
    }

    @Test
    void settingRowUsesSingleColumnAtResponsiveBreakpoint() {
        UiSettingRow row = Ui.settingRow(UiText.literal("Feature"),
            Ui.toggle(UiText.literal(""), UiBinding.of(() -> false, value -> { })));
        row.measure(RENDERER, 560, 200, UiTheme.roseLight());
        row.layout(RENDERER, new UiBounds(0, 0, 560, row.measuredHeight()), UiTheme.roseLight());
        assertTrue(row.control().bounds().y() > row.bounds().y());
    }

    @Test
    void scaffoldKeepsResponsiveRegionsValidAcrossLogicalWidths() {
        UiPageHost pages = Ui.pageHost()
            .addPage(UiText.literal("General"), Ui.column().add(Ui.label(UiText.literal("Content"))))
            .addPage(UiText.literal("Preview"), Ui.label(UiText.literal("Preview")));
        UiScaffold scaffold = Ui.scaffold(pages)
            .header(Ui.panel().add(Ui.label(UiText.literal("Optional header"))))
            .navigation(pages.navigation())
            .navigationMode(UiScaffold.NavigationMode.TOP);

        for (float width : new float[] {320, 360, 440, 560, 760, 1080}) {
            scaffold.measure(RENDERER, width, 180, UiTheme.roseLight());
            scaffold.layout(RENDERER, new UiBounds(0, 0, width, 180), UiTheme.roseLight());
            assertTrue(scaffold.content().bounds().width() >= 0);
            assertTrue(scaffold.content().bounds().height() >= 0);
            assertTrue(pages.navigation().bounds().height() >= 0);
        }
    }

    @Test
    void pageHostSwitchesContentAndResetsScroll() {
        Ui.Column first = Ui.column();
        for (int index = 0; index < 8; index++) first.add(Ui.label(UiText.literal("Line" + index)));
        UiPageHost pages = Ui.pageHost().addPage(UiText.literal("First"), first)
            .addPage(UiText.literal("Second"), Ui.label(UiText.literal("Second page")));
        pages.measure(RENDERER, 100, 20, UiTheme.roseLight());
        pages.layout(RENDERER, new UiBounds(0, 0, 100, 20), UiTheme.roseLight());
        assertTrue(pages.currentPage().scroll(5, 5, -1));
        pages.select(1);
        assertEquals(1, pages.selectedIndex());
        assertEquals(0, ((Ui.ScrollView) pages.currentPage()).offset());
        assertTrue(pages.layoutVersion() > 0);
    }

    @Test
    void navigationWrapsAtNarrowWidths() {
        UiPageHost pages = Ui.pageHost()
            .addPage(UiText.literal("General"), Ui.label(UiText.literal("A")))
            .addPage(UiText.literal("Preview"), Ui.label(UiText.literal("B")))
            .addPage(UiText.literal("Advanced"), Ui.label(UiText.literal("C")));
        UiNavigationBar navigation = pages.navigation();
        navigation.measure(RENDERER, 60, 100, UiTheme.roseLight());
        navigation.layout(RENDERER, new UiBounds(0, 0, 60, navigation.measuredHeight()), UiTheme.roseLight());
        assertTrue(navigation.measuredHeight() > RENDERER.lineHeight());
        assertTrue(navigation.childNodes().get(0).bounds().width() <= 60);
    }

    @Test
    void disabledSettingRowDisablesItsControl() {
        Ui.Toggle control = Ui.toggle(UiText.literal(""), UiBinding.of(() -> false, value -> { }));
        UiSettingRow row = Ui.settingRow(UiText.literal("Feature"), control).enabled(false);
        assertFalse(control.enabled());
        assertFalse(row.click(0, 0, 0));
    }

    private static final UiRenderer RENDERER = new UiRenderer() {
        @Override public void fillRoundRect(UiBounds bounds, float radius, int color) { }
        @Override public void strokeRoundRect(UiBounds bounds, float radius, float width, int color) { }
        @Override public void drawText(UiText text, float x, float y, int color) { }
        @Override public float textWidth(UiText text) { return text.value().length() * 6f; }
        @Override public float lineHeight() { return 10; }
        @Override public void pushClip(UiBounds bounds) { }
        @Override public void popClip() { }
    };
}
