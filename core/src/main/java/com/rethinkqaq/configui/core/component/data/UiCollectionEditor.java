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

package com.rethinkqaq.configui.core.component.data;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBinding;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiClipboard;
import com.rethinkqaq.configui.core.UiDialogHost;
import com.rethinkqaq.configui.core.UiKeyEvent;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTextInput;
import com.rethinkqaq.configui.core.UiTheme;
import com.rethinkqaq.configui.core.component.input.UiTextField;
import com.rethinkqaq.configui.core.layout.UiScrollView;
import com.rethinkqaq.configui.core.setting.UiListSetting;
import com.rethinkqaq.configui.core.setting.UiValidationResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Immediately committed collection editor with a modal add field and independently scrolling entries. */
public final class UiCollectionEditor<T> extends Ui.Node {
    private final UiDialogHost dialogs;
    private final UiText title;
    private final UiListSetting<T> setting;
    private final UiListEntryAdapter<T> adapter;

    public UiCollectionEditor(UiDialogHost dialogs, UiText title, UiListSetting<T> setting, UiListEntryAdapter<T> adapter) {
        this.dialogs = Objects.requireNonNull(dialogs, "dialogs");
        this.title = Objects.requireNonNull(title, "title");
        this.setting = Objects.requireNonNull(setting, "setting");
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    public UiText title() { return title; }
    public int size() { return setting.items().size(); }
    public void open() { dialogs.show(new CollectionDialog()); }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        measuredWidth = maxWidth;
        measuredHeight = theme.metrics().controlHeight();
    }
    @Override public void render(UiRenderer renderer, UiTheme theme) {
        int surface = enabled() ? blend(theme.palette().surfaceRaised(), theme.palette().surface(), hoverProgress()) : theme.palette().controlDisabled();
        int text = enabled() ? theme.palette().textPrimary() : theme.palette().textDisabled();
        renderer.fillRoundRect(bounds, theme.metrics().controlRadius(), surface);
        renderer.strokeRoundRect(bounds, theme.metrics().controlRadius(), theme.metrics().borderWidth(), theme.palette().border());
        Ui.drawFittedText(renderer, title, bounds.x() + theme.metrics().padding(), bounds.y() + (bounds.height() - renderer.lineHeight()) / 2f,
            Math.max(0, bounds.width() - theme.metrics().padding() * 4), text);
        UiText count = UiText.literal("[" + size() + "]  >");
        float countWidth = renderer.textWidth(count);
        renderer.drawText(count, bounds.x() + bounds.width() - theme.metrics().padding() - countWidth,
            bounds.y() + (bounds.height() - renderer.lineHeight()) / 2f, text);
    }
    @Override public boolean click(float x, float y, int button) {
        if (!enabled() || button != 0 || !bounds.contains(x, y)) return false;
        open();
        return true;
    }
    @Override public boolean key(int keyCode) {
        if (!enabled() || (keyCode != com.rethinkqaq.configui.core.UiKey.ENTER && keyCode != com.rethinkqaq.configui.core.UiKey.SPACE)) return false;
        open();
        return true;
    }
    @Override public boolean focusable() { return enabled(); }

    private UiValidationResult validate(int index, T value) {
        UiValidationResult result = adapter.validate(value);
        if (!result.accepted() || !adapter.uniqueValues()) return result;
        List<T> values = setting.items();
        for (int current = 0; current < values.size(); current++) {
            if (current != index && Objects.equals(values.get(current), value)) return UiValidationResult.error(UiText.literal("This entry already exists"));
        }
        return result;
    }

    private final class CollectionDialog extends Ui.Node implements Ui.ChildProvider {
        private final List<EntryRow> rows = new ArrayList<>();
        private final ListBody listBody = new ListBody(this);
        private final UiScrollView list = Ui.scrollView(listBody);
        private T draft = adapter.newValue();
        private final Ui.Node addEditor = adapter.editor(UiBinding.of(() -> draft, this::setDraft));
        private final Ui.IconButton add = Ui.iconButton(UiText.literal("+"), this::addDraft);
        private final Ui.Button done = Ui.button(UiText.literal("Done"), dialogs::close);
        private UiValidationResult validation = UiValidationResult.OK;
        private float titleHeight, addWidth, footerHeight, bodyHeight, listInset;

        private CollectionDialog() {
            if (addEditor instanceof UiTextField textField) textField.onSubmit(this::addDraft);
            refreshRows();
        }
        private void setDraft(T value) { draft = value; validation = validate(-1, value); invalidateLayout(); }
        private void refreshRows() {
            rows.clear();
            for (int index = 0; index < setting.items().size(); index++) rows.add(new EntryRow(this, index));
            list.reset();
            invalidateLayout();
        }
        private void addDraft() {
            if (addEditor instanceof UiTextField textField) textField.commit();
            validation = validate(-1, draft);
            if (!validation.accepted()) { invalidateLayout(); return; }
            validation = setting.add(adapter.copy(draft));
            if (!validation.accepted()) { invalidateLayout(); return; }
            draft = adapter.newValue();
            if (addEditor instanceof UiTextField textField) textField.cancel();
            validation = UiValidationResult.OK;
            refreshRows();
        }
        private void removeEntry(int index) {
            validation = setting.remove(index);
            if (validation.accepted()) refreshRows(); else invalidateLayout();
        }
        private void updateEntry(int index, T value) {
            validation = validate(index, value);
            if (validation.accepted()) validation = setting.update(index, value);
            invalidateLayout();
        }

        @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
            float padding = theme.metrics().padding();
            float innerWidth = Math.max(0, maxWidth - padding * 2);
            titleHeight = renderer.lineHeight();
            add.measure(renderer, innerWidth, maxHeight, theme);
            addWidth = add.measuredWidth();
            addEditor.measure(renderer, Math.max(0, innerWidth - addWidth - theme.metrics().spacing()), maxHeight, theme);
            done.measure(renderer, innerWidth, maxHeight, theme);
            footerHeight = done.measuredHeight();
            float messageHeight = validation.severity() == UiValidationResult.Severity.OK ? 0 : renderer.lineHeight() + theme.metrics().spacing() / 2f;
            float footerGap = theme.metrics().spacing() * 3f;
            listInset = Math.max(2f, theme.metrics().spacing() / 2f);
            float fixed = padding * 2 + titleHeight + theme.metrics().spacing() + Math.max(add.measuredHeight(), addEditor.measuredHeight())
                + messageHeight + footerGap + footerHeight;
            list.measure(renderer, innerWidth, Math.max(theme.metrics().controlHeight() * 2, maxHeight - fixed), theme);
            bodyHeight = list.measuredHeight();
            measuredWidth = Math.min(maxWidth, Math.max(220, innerWidth + padding * 2));
            measuredHeight = Math.min(maxHeight, fixed + bodyHeight);
        }
        @Override public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
            super.layout(renderer, value, theme);
            float padding = theme.metrics().padding();
            float x = value.x() + padding;
            float width = Math.max(0, value.width() - padding * 2);
            float y = value.y() + padding + titleHeight + theme.metrics().spacing();
            float addHeight = Math.max(add.measuredHeight(), addEditor.measuredHeight());
            float editorWidth = Math.max(0, width - addWidth - theme.metrics().spacing());
            addEditor.layout(renderer, new UiBounds(x, y + (addHeight - addEditor.measuredHeight()) / 2f, editorWidth, addEditor.measuredHeight()), theme);
            add.layout(renderer, new UiBounds(x + editorWidth + theme.metrics().spacing(), y + (addHeight - add.measuredHeight()) / 2f, addWidth, add.measuredHeight()), theme);
            y += addHeight;
            if (validation.severity() != UiValidationResult.Severity.OK) y += renderer.lineHeight() + theme.metrics().spacing() / 2f;
            y += theme.metrics().spacing();
            list.layout(renderer, new UiBounds(x, y, width, Math.max(0, bodyHeight - listInset)), theme);
            float doneY = value.y() + value.height() - padding - footerHeight + theme.metrics().spacing() / 2f;
            done.layout(renderer, new UiBounds(x + width - done.measuredWidth(), doneY, done.measuredWidth(), footerHeight), theme);
        }
        @Override public void render(UiRenderer renderer, UiTheme theme) {
            renderer.fillRoundRect(bounds, theme.metrics().cardRadius(), theme.palette().surfaceRaised());
            renderer.strokeRoundRect(bounds, theme.metrics().cardRadius(), theme.metrics().borderWidth(), theme.palette().border());
            float padding = theme.metrics().padding();
            renderer.drawText(title, bounds.x() + padding, bounds.y() + padding, theme.palette().textPrimary());
            addEditor.render(renderer, theme);
            add.render(renderer, theme);
            if (validation.severity() != UiValidationResult.Severity.OK) {
                int color = validation.severity() == UiValidationResult.Severity.ERROR ? theme.palette().danger() : theme.palette().warning();
                Ui.drawFittedText(renderer, validation.message(), bounds.x() + padding, add.bounds().y() + add.bounds().height() + theme.metrics().spacing() / 2f,
                    Math.max(0, bounds.width() - padding * 2), color);
            }
            list.render(renderer, theme);
            float dividerY = list.bounds().y() + list.bounds().height() + theme.metrics().spacing() / 2f;
            renderer.fillRect(new UiBounds(list.bounds().x(), dividerY, list.bounds().width(), 1f),
                withAlpha(theme.palette().border(), 96));
            done.render(renderer, theme);
        }
        private int withAlpha(int color, int alpha) { return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24); }
        @Override public List<Ui.Node> childNodes() { return List.of(addEditor, add, list, done); }
        @Override public boolean click(float x, float y, int button) { return done.click(x, y, button) || add.click(x, y, button) || addEditor.click(x, y, button) || list.click(x, y, button); }
        @Override public boolean scroll(float x, float y, double amount) { return list.scroll(x, y, amount); }
        @Override public boolean drag(float x, float y, int button) { return addEditor.drag(x, y, button) || list.drag(x, y, button); }
        @Override public boolean release(float x, float y, int button) { return done.release(x, y, button) | add.release(x, y, button) | addEditor.release(x, y, button) | list.release(x, y, button); }
        @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) { return done.key(event, clipboard) || add.key(event, clipboard) || addEditor.key(event, clipboard) || list.key(event, clipboard); }
        @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) { return addEditor.textInput(event, clipboard) || list.textInput(event, clipboard); }
    }

    private final class ListBody extends Ui.Node implements Ui.ChildProvider {
        private final CollectionDialog dialog;
        private ListBody(CollectionDialog dialog) { this.dialog = dialog; }
        @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
            float height = 0;
            for (EntryRow row : dialog.rows) { row.measure(renderer, maxWidth, maxHeight, theme); if (height > 0) height += theme.metrics().spacing() / 2f; height += row.measuredHeight(); }
            measuredWidth = maxWidth;
            measuredHeight = Math.max(theme.metrics().controlHeight(), height);
        }
        @Override public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
            super.layout(renderer, value, theme);
            float y = value.y();
            for (EntryRow row : dialog.rows) { row.layout(renderer, new UiBounds(value.x(), y, value.width(), row.measuredHeight()), theme); y += row.measuredHeight() + theme.metrics().spacing() / 2f; }
        }
        @Override public void render(UiRenderer renderer, UiTheme theme) {
            if (dialog.rows.isEmpty()) renderer.drawText(UiText.literal("No entries yet"), bounds.x(), bounds.y(), theme.palette().textSecondary());
            for (EntryRow row : dialog.rows) row.render(renderer, theme);
        }
        @Override public List<Ui.Node> childNodes() { return List.copyOf(dialog.rows); }
        @Override public boolean click(float x, float y, int button) { for (int index = dialog.rows.size() - 1; index >= 0; index--) if (dialog.rows.get(index).click(x, y, button)) return true; return false; }
        @Override public boolean drag(float x, float y, int button) { for (EntryRow row : dialog.rows) if (row.drag(x, y, button)) return true; return false; }
        @Override public boolean release(float x, float y, int button) { boolean handled = false; for (EntryRow row : dialog.rows) handled |= row.release(x, y, button); return handled; }
        @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) { for (EntryRow row : dialog.rows) if (row.key(event, clipboard)) return true; return false; }
        @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) { for (EntryRow row : dialog.rows) if (row.textInput(event, clipboard)) return true; return false; }
    }

    private final class EntryRow extends Ui.Node implements Ui.ChildProvider {
        private final CollectionDialog page;
        private final int index;
        private final Ui.Node editor;
        private final Ui.IconButton remove;
        private EntryRow(CollectionDialog page, int index) {
            this.page = page;
            this.index = index;
            editor = adapter.editor(UiBinding.of(() -> valueAt(index), value -> page.updateEntry(index, value)));
            remove = Ui.iconButton(UiText.literal("-"), () -> page.removeEntry(index));
            remove.variant(Ui.ButtonVariant.DANGER);
        }
        private T valueAt(int itemIndex) { List<T> values = setting.items(); return itemIndex >= 0 && itemIndex < values.size() ? values.get(itemIndex) : null; }
        @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
            remove.measure(renderer, maxWidth, maxHeight, theme);
            editor.measure(renderer, Math.max(0, maxWidth - remove.measuredWidth() - theme.metrics().spacing()), maxHeight, theme);
            measuredWidth = maxWidth;
            measuredHeight = Math.max(editor.measuredHeight(), remove.measuredHeight());
        }
        @Override public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
            super.layout(renderer, value, theme);
            float editorWidth = Math.max(0, value.width() - remove.measuredWidth() - theme.metrics().spacing());
            editor.layout(renderer, new UiBounds(value.x(), value.y() + (value.height() - editor.measuredHeight()) / 2f, editorWidth, editor.measuredHeight()), theme);
            remove.layout(renderer, new UiBounds(value.x() + editorWidth + theme.metrics().spacing(), value.y() + (value.height() - remove.measuredHeight()) / 2f, remove.measuredWidth(), remove.measuredHeight()), theme);
        }
        @Override public void render(UiRenderer renderer, UiTheme theme) { editor.render(renderer, theme); remove.render(renderer, theme); }
        @Override public List<Ui.Node> childNodes() { return List.of(editor, remove); }
        @Override public boolean click(float x, float y, int button) { return remove.click(x, y, button) || editor.click(x, y, button); }
        @Override public boolean drag(float x, float y, int button) { return editor.drag(x, y, button); }
        @Override public boolean release(float x, float y, int button) { return remove.release(x, y, button) | editor.release(x, y, button); }
    }
}
