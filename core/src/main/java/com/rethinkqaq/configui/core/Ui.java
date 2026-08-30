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
import java.util.function.Function;

import com.rethinkqaq.configui.core.component.UiButton;
import com.rethinkqaq.configui.core.component.UiCustom;
import com.rethinkqaq.configui.core.component.UiDivider;
import com.rethinkqaq.configui.core.component.UiIconButton;
import com.rethinkqaq.configui.core.component.UiLabel;
import com.rethinkqaq.configui.core.component.UiSelect;
import com.rethinkqaq.configui.core.component.UiSlider;
import com.rethinkqaq.configui.core.component.UiToggle;
import com.rethinkqaq.configui.core.component.UiTooltip;
import com.rethinkqaq.configui.core.component.UiTooltipContent;
import com.rethinkqaq.configui.core.component.data.UiListEntryAdapter;
import com.rethinkqaq.configui.core.component.data.UiCollectionEditor;
import com.rethinkqaq.configui.core.component.data.UiSelectionList;
import com.rethinkqaq.configui.core.component.feedback.UiAlert;
import com.rethinkqaq.configui.core.component.feedback.UiFeedbackType;
import com.rethinkqaq.configui.core.component.input.UiNumericField;
import com.rethinkqaq.configui.core.component.input.UiFormField;
import com.rethinkqaq.configui.core.component.input.UiSearchField;
import com.rethinkqaq.configui.core.component.input.UiNumberControl;
import com.rethinkqaq.configui.core.component.input.UiTextField;
import com.rethinkqaq.configui.core.layout.UiColumn;
import com.rethinkqaq.configui.core.layout.UiHeader;
import com.rethinkqaq.configui.core.layout.UiPanel;
import com.rethinkqaq.configui.core.layout.UiRow;
import com.rethinkqaq.configui.core.layout.UiScrollView;
import com.rethinkqaq.configui.core.layout.UiSection;
import com.rethinkqaq.configui.core.layout.UiStack;
import com.rethinkqaq.configui.core.layout.UiTemplate;
import com.rethinkqaq.configui.core.setting.UiNumberSpec;
import com.rethinkqaq.configui.core.setting.UiListSetting;
import com.rethinkqaq.configui.core.setting.UiSetting;
import java.util.function.Supplier;

/** Stock UI factories and compatibility aliases for the original {@code Ui.*} API. */
public final class Ui {
    private Ui() { }

    public static Column column() { return new Column(); }
    public static Row row() { return new Row(); }
    public static Stack stack() { return new Stack(); }
    public static Panel panel() { return new Panel(); }
    public static Panel card() { return new Panel(); }
    public static Section section(UiText title) { return new Section(title); }
    public static Label label(UiText text) { return new Label(text); }
    /** Creates a platform-neutral one-off custom node. */
    public static UiCustom.Builder custom() { return UiCustom.builder(); }
    public static Divider divider() { return new Divider(); }
    public static UiBadge badge(UiText text) { return new UiBadge(text); }
    public static UiGrid grid() { return new UiGrid(); }
    public static UiScaffold scaffold(Node content) { return new UiScaffold(content); }
    public static UiTemplate.Builder template() { return UiTemplate.template(); }
    public static UiTemplate.Builder topNavigationTemplate() { return UiTemplate.topNavigation(); }
    public static UiHeader header(UiText title) { return UiHeader.card(title); }
    public static UiHeader textHeader(UiText title) { return UiHeader.text(title); }
    public static UiDialogHost dialogHost() { return new UiDialogHost(); }
    public static UiDialogHost dialogHost(Node root) { return new UiDialogHost(root); }
    public static UiPageHost pageHost() { return new UiPageHost(); }
    public static UiSplitLayout split(Node primary, Node secondary) { return new UiSplitLayout(primary, secondary); }
    public static UiSettingRow settingRow(UiText label, Node control) { return new UiSettingRow(label, control); }
    public static UiFormField formField(UiText label, Node control) { return new UiFormField(label, control); }
    public static UiPreviewCard previewCard(UiText title, Node preview) { return new UiPreviewCard(title, preview); }
    public static Button button(UiText text, Runnable action) { return new Button(text, action); }
    public static IconButton iconButton(UiText label, Runnable action) { return new IconButton(label, action); }
    public static Toggle toggle(UiText text, UiBinding<Boolean> binding) { return new Toggle(text, binding); }
    public static Slider slider(UiText text, UiBinding<Double> binding, double min, double max, double step) { return new Slider(text, binding, min, max, step); }
    public static <T> Select<T> select(UiText text, UiBinding<T> binding, List<T> values, Function<T, UiText> labels) { return new Select<>(text, binding, values, labels); }
    public static ScrollView scrollView(Node child) { return new ScrollView(child); }
    public static Tooltip tooltip(Node child, UiText text) { return new Tooltip(child, text); }
    public static Tooltip tooltip(Node child, UiTooltipContent content) { return new Tooltip(child, content); }
    public static Tooltip tooltip(Node child, Node content) { return new Tooltip(child, UiTooltipContent.node(content)); }
    public static UiTextField textField(UiBinding<String> binding) { return new UiTextField(binding); }
    public static UiSearchField searchField(UiBinding<String> binding) { return new UiSearchField(binding); }
    public static <T extends Number> UiNumericField<T> numericField(UiSetting<T> setting, UiNumberSpec<T> spec) { return new UiNumericField<>(setting, spec); }
    public static <T extends Number> UiNumberControl<T> numberControl(UiSetting<T> setting, UiNumberSpec<T> spec) { return new UiNumberControl<>(setting, spec); }
    public static UiAlert alert(UiFeedbackType type, UiText text) { return new UiAlert(type, text); }
    public static UiAlert customAlert(UiText text, int color) { return UiAlert.custom(text, color); }
    public static <T> UiSelectionList<T> selectionList(Supplier<List<T>> items, UiBinding<T> binding, Function<T, UiText> labels) { return new UiSelectionList<>(items, binding, labels); }
    public static <T> UiCollectionEditor<T> collectionEditor(UiDialogHost dialogs, UiText title, UiListSetting<T> setting, UiListEntryAdapter<T> adapter) { return new UiCollectionEditor<>(dialogs, title, setting, adapter); }

    /** Draws one line, fitting literal text with an ellipsis before rendering. */
    public static void drawFittedText(UiRenderer renderer, UiText text, float x, float y, float maxWidth, int color) {
        if (maxWidth <= 0) return;
        renderer.drawText(fitted(renderer, text, maxWidth), x, y, color);
    }

    /** Draws fitted text using an explicit renderer scale. */
    public static void drawFittedText(UiRenderer renderer, UiText text, float x, float y, float maxWidth, int color, float scale) {
        if (maxWidth <= 0 || scale <= 0) return;
        renderer.drawText(fitted(renderer, text, maxWidth / scale), x, y, color, scale);
    }

    /** Returns a renderer-measured text value that fits within the requested width. */
    public static UiText fitText(UiRenderer renderer, UiText text, float maxWidth) { return fitted(renderer, text, maxWidth); }

    /** Fits text using the same scale that will be used for drawing it. */
    public static UiText fitText(UiRenderer renderer, UiText text, float maxWidth, float scale) {
        return fitted(renderer, text, maxWidth / Math.max(.0001f, scale));
    }

    /** Splits literal text into renderer-measured lines without allowing a line to overflow. */
    public static List<UiText> wrapLines(UiRenderer renderer, UiText text, float maxWidth, int maxLines) {
        if (maxWidth <= 0 || maxLines <= 0) return List.of();
        if (text.translatable() || renderer.textWidth(text) <= maxWidth) return List.of(text);
        List<UiText> result = new ArrayList<>();
        String remaining = text.value().trim();
        while (!remaining.isEmpty() && result.size() < maxLines) {
            int length = fitPrefix(renderer, remaining, maxWidth);
            boolean hasMore = length < remaining.length();
            if (hasMore) {
                int split = remaining.lastIndexOf(' ', Math.max(0, length - 1));
                if (split > 0) length = split;
            }
            if (length <= 0) length = Math.min(1, remaining.length());
            String line = remaining.substring(0, length).trim();
            remaining = remaining.substring(length).trim();
            if (hasMore && result.size() + 1 == maxLines) {
                line = fitted(renderer, UiText.literal(line + "..."), maxWidth).value();
                remaining = "";
            }
            result.add(UiText.literal(line));
        }
        if (result.isEmpty()) result.add(UiText.literal(""));
        return result;
    }

    /** Tooltip-specific wrapping with explicit newlines and selectable truncation. */
    public static List<UiText> wrapLines(UiRenderer renderer, UiText text, float maxWidth, int maxLines,
                                         boolean ellipsis) {
        if (maxWidth <= 0 || maxLines <= 0) return List.of();
        if (text.translatable()) return List.of(text);
        List<UiText> result = new ArrayList<>();
        for (String paragraph : text.value().split("\\R", -1)) {
            String remaining = paragraph.trim();
            if (remaining.isEmpty()) {
                if (result.size() < maxLines) result.add(UiText.literal(""));
                continue;
            }
            while (!remaining.isEmpty() && result.size() < maxLines) {
                int length = fitPrefix(renderer, remaining, maxWidth);
                boolean hasMore = length < remaining.length();
                if (hasMore) {
                    int split = remaining.lastIndexOf(' ', Math.max(0, length - 1));
                    if (split > 0) length = split;
                }
                if (length <= 0) length = Math.min(1, remaining.length());
                String line = remaining.substring(0, length).trim();
                remaining = remaining.substring(length).trim();
                if (hasMore && result.size() + 1 == maxLines) {
                    if (ellipsis) line = fitted(renderer, UiText.literal(line + "..."), maxWidth).value();
                    remaining = "";
                }
                result.add(UiText.literal(line));
            }
            if (result.size() == maxLines) break;
        }
        if (result.isEmpty()) result.add(UiText.literal(""));
        return result;
    }

    public static void drawWrappedText(UiRenderer renderer, UiText text, float x, float y, float maxWidth,
                                       int maxLines, int color, float lineGap) {
        drawWrappedText(renderer, text, x, y, maxWidth, maxLines, color, lineGap, true);
    }

    public static void drawWrappedText(UiRenderer renderer, UiText text, float x, float y, float maxWidth,
                                       int maxLines, int color, float lineGap, boolean ellipsis) {
        List<UiText> lines = wrapLines(renderer, text, maxWidth, maxLines, ellipsis);
        for (int index = 0; index < lines.size(); index++) renderer.drawText(lines.get(index), x,
            y + index * (renderer.lineHeight() + lineGap), color);
    }

    private static int fitPrefix(UiRenderer renderer, String value, float maxWidth) {
        int end = value.length();
        while (end > 0 && renderer.textWidth(UiText.literal(value.substring(0, end))) > maxWidth) end--;
        return end;
    }

    private static UiText fitted(UiRenderer renderer, UiText text, float maxWidth) {
        UiText displayed = text;
        if (!text.translatable() && renderer.textWidth(text) > maxWidth) {
            String suffix = "...";
            float suffixWidth = renderer.textWidth(UiText.literal(suffix));
            int end = text.value().length();
            while (end > 0 && renderer.textWidth(UiText.literal(text.value().substring(0, end))) + suffixWidth > maxWidth) end--;
            displayed = UiText.literal(end == 0 ? suffix : text.value().substring(0, end) + suffix);
        }
        return displayed;
    }

    public enum ButtonVariant { PRIMARY, SECONDARY, OUTLINE, DANGER }

    /** Base node remains here to keep the original source-compatible API stable. */
    public abstract static class Node {
        protected UiBounds bounds = UiBounds.EMPTY;
        protected float measuredWidth;
        protected float measuredHeight;
        private boolean enabled = true;
        private boolean hovered;
        private boolean focused;
        private long layoutVersion;
        private long lastMotionNanos = System.nanoTime();
        private float hoverProgress;
        private float focusProgress;

        public Node enabled(boolean value) { enabled = value; invalidateLayout(); return this; }
        public boolean enabled() { return enabled; }
        public boolean hovered() { return hovered; }
        public boolean focused() { return focused; }
        public UiBounds bounds() { return bounds; }
        public void invalidateLayout() { layoutVersion++; }
        public long layoutVersion() { return layoutVersion; }
        public void setHovered(boolean value) { hovered = value; }
        public void setFocused(boolean value) { focused = value; }
        public float hoverProgress() { return hoverProgress; }
        public float focusProgress() { return focusProgress; }
        public void advanceMotion(long nowNanos, UiTheme theme) {
            float elapsedMillis = Math.max(0f, (nowNanos - lastMotionNanos) / 1_000_000f);
            lastMotionNanos = nowNanos;
            hoverProgress = approach(hoverProgress, hovered ? 1f : 0f, elapsedMillis, theme.motion().hoverMillis());
            focusProgress = approach(focusProgress, focused ? 1f : 0f, elapsedMillis, theme.motion().focusMillis());
        }
        protected static float approach(float current, float target, float elapsedMillis, int durationMillis) {
            if (durationMillis <= 0) return target;
            float step = Math.min(1f, elapsedMillis / durationMillis);
            return current + (target - current) * step;
        }
        protected static int blend(int from, int to, float amount) {
            float t = Math.max(0f, Math.min(1f, amount));
            int a = Math.round(((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * t);
            int r = Math.round(((from >>> 16) & 0xFF) + (((to >>> 16) & 0xFF) - ((from >>> 16) & 0xFF)) * t);
            int g = Math.round(((from >>> 8) & 0xFF) + (((to >>> 8) & 0xFF) - ((from >>> 8) & 0xFF)) * t);
            int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
            return a << 24 | r << 16 | g << 8 | b;
        }
        public final void measure(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) { measureSelf(renderer, maxWidth, maxHeight, theme); }
        protected abstract void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme);
        public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) { bounds = value; }
        public abstract void render(UiRenderer renderer, UiTheme theme);
        public boolean click(float mouseX, float mouseY, int button) { return false; }
        public boolean scroll(float mouseX, float mouseY, double amount) { return false; }
        public boolean drag(float mouseX, float mouseY, int button) { return false; }
        /** True while this node owns an active pointer gesture. */
        public boolean capturesPointer() { return false; }
        public boolean key(int keyCode) { return false; }
        /** Receives a key with modifiers and a host-provided clipboard. */
        public boolean key(UiKeyEvent event, UiClipboard clipboard) { return key(event.keyCode()); }
        /** Receives one Unicode code point; hosts normalize version-specific events before this call. */
        public boolean textInput(UiTextInput event, UiClipboard clipboard) { return false; }
        public boolean release(float mouseX, float mouseY, int button) { return false; }
        public boolean focusable() { return false; }
        protected boolean hasVisibleFocus(UiTheme theme) {
            return focusProgress() > .01f && ((theme.palette().focusRing() >>> 24) & 0xFF) > 0;
        }
        public float measuredWidth() { return measuredWidth; }
        public float measuredHeight() { return measuredHeight; }
    }

    /** Implemented by nodes which own children without using the generic container layout. */
    public interface ChildProvider { List<Node> childNodes(); }

    /** Composite nodes that also handle an event after their children have been checked. */
    public interface SelfDispatching { }

    /** Implemented by nodes whose children are only interactive inside a clipped viewport. */
    public interface ClipProvider { UiBounds viewportBounds(); }

    public abstract static class Container extends Node implements ChildProvider {
        protected final List<Node> children = new ArrayList<>();
        public Container add(Node child) { children.add(Objects.requireNonNull(child, "child")); invalidateLayout(); return this; }
        public List<Node> children() { return List.copyOf(children); }
        @Override public List<Node> childNodes() { return List.copyOf(children); }
        @Override public boolean click(float x, float y, int button) {
            for (int index = children.size() - 1; index >= 0; index--) if (children.get(index).click(x, y, button)) return true;
            return false;
        }
        @Override public boolean scroll(float x, float y, double amount) {
            for (int index = children.size() - 1; index >= 0; index--) if (children.get(index).scroll(x, y, amount)) return true;
            return false;
        }
        @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) {
            for (int index = children.size() - 1; index >= 0; index--) if (children.get(index).key(event, clipboard)) return true;
            return false;
        }
        @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) {
            for (int index = children.size() - 1; index >= 0; index--) if (children.get(index).textInput(event, clipboard)) return true;
            return false;
        }
        public List<Node> focusableNodes() {
            List<Node> result = new ArrayList<>();
            for (Node child : children) { if (child.focusable()) result.add(child); if (child instanceof Container container) result.addAll(container.focusableNodes()); }
            return result;
        }
    }

    // Compatibility aliases. New code should import the concrete class from layout/ or component/.
    public static class Column extends UiColumn {
        @Override public Column gap(float value) { super.gap(value); return this; }
        @Override public Column mainAxisAlignment(UiMainAxisAlignment value) { super.mainAxisAlignment(value); return this; }
        @Override public Column crossAxisAlignment(UiCrossAxisAlignment value) { super.crossAxisAlignment(value); return this; }
        @Override public Column add(Node child) { super.add(child); return this; }
    }
    public static class Row extends UiRow {
        @Override public Row gap(float value) { super.gap(value); return this; }
        @Override public Row mainAxisAlignment(UiMainAxisAlignment value) { super.mainAxisAlignment(value); return this; }
        @Override public Row crossAxisAlignment(UiCrossAxisAlignment value) { super.crossAxisAlignment(value); return this; }
        @Override public Row equalChildWidths(boolean value) { super.equalChildWidths(value); return this; }
        @Override public Row add(Node child) { super.add(child); return this; }
    }
    public static class Stack extends UiStack {
        @Override public Stack add(Node child) { super.add(child); return this; }
    }
    public static class Panel extends UiPanel {
        @Override public Panel color(int value) { super.color(value); return this; }
        @Override public Panel padding(float value) { super.padding(value); return this; }
        @Override public Panel gap(float value) { super.gap(value); return this; }
        @Override public Panel add(Node child) { super.add(child); return this; }
    }
    public static final class Section extends UiSection {
        private Section(UiText title) { super(title); }
        @Override public Section add(Node child) { super.add(child); return this; }
    }
    public static final class Label extends UiLabel {
        private Label(UiText text) { super(text); }
        @Override public Label wrap(boolean value) { super.wrap(value); return this; }
        @Override public Label maxLines(int value) { super.maxLines(value); return this; }
    }
    public static final class Divider extends UiDivider { }
    public static class Button extends UiButton {
        private Button(UiText text, Runnable action) { super(text, action); }
        @Override public Button variant(ButtonVariant value) { super.variant(value); return this; }
        @Override public Button variant(Supplier<ButtonVariant> value) { super.variant(value); return this; }
    }
    public static final class IconButton extends UiIconButton {
        private IconButton(UiText text, Runnable action) { super(text, action); }
    }
    public static final class Toggle extends UiToggle {
        private Toggle(UiText text, UiBinding<Boolean> binding) { super(text, binding); }
    }
    public static final class Slider extends UiSlider {
        private Slider(UiText text, UiBinding<Double> binding, double min, double max, double step) { super(text, binding, min, max, step); }
    }
    public static final class Select<T> extends UiSelect<T> {
        private Select(UiText text, UiBinding<T> binding, List<T> values, Function<T, UiText> labels) { super(text, binding, values, labels); }
    }
    public static final class ScrollView extends UiScrollView {
        private ScrollView(Node child) { super(child); }
        Ui.ScrollView fillViewportChild() { setFillViewportChild(true); return this; }
    }
    public static final class Tooltip extends UiTooltip {
        private Tooltip(Node child, UiText text) { super(child, text); }
        private Tooltip(Node child, UiTooltipContent content) { super(child, content); }
        @Override public Tooltip delayMillis(long value) { super.delayMillis(value); return this; }
        @Override public Tooltip minWidth(float value) { super.minWidth(value); return this; }
        @Override public Tooltip maxWidth(float value) { super.maxWidth(value); return this; }
        @Override public Tooltip maxHeight(float value) { super.maxHeight(value); return this; }
        @Override public Tooltip maxLines(int value) { super.maxLines(value); return this; }
        @Override public Tooltip padding(float value) { super.padding(value); return this; }
        @Override public Tooltip lineGap(float value) { super.lineGap(value); return this; }
        @Override public Tooltip overflow(UiTooltip.TextOverflow value) { super.overflow(value); return this; }
    }
}
