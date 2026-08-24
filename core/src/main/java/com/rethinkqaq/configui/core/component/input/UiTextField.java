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

package com.rethinkqaq.configui.core.component.input;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBinding;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiClipboard;
import com.rethinkqaq.configui.core.UiKey;
import com.rethinkqaq.configui.core.UiKeyEvent;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTextInput;
import com.rethinkqaq.configui.core.UiTheme;
import com.rethinkqaq.configui.core.setting.UiValidationResult;
import java.util.Objects;
import java.util.function.Predicate;

/** Single-line, Unicode-aware input whose draft is committed only when valid. */
public class UiTextField extends Ui.Node {
    private final UiBinding<String> binding;
    private String draft;
    private String placeholder = "";
    private int maxLength = 256;
    private Predicate<String> characterFilter = value -> true;
    private java.util.function.Function<String, UiValidationResult> validator = value -> UiValidationResult.OK;
    private int cursor;
    private int selection;
    private boolean escapeCancels = true;
    private Runnable onSubmit = () -> { };
    private UiValidationResult validation = UiValidationResult.OK;
    private UiRenderer lastRenderer;

    public UiTextField(UiBinding<String> binding) {
        this.binding = Objects.requireNonNull(binding, "binding");
        draft = safe(binding.get()); cursor = draft.length(); selection = cursor;
    }
    public UiTextField placeholder(UiText value) { placeholder = Objects.requireNonNull(value, "value").value(); return this; }
    public UiTextField maxLength(int value) { if (value < 0) throw new IllegalArgumentException("maxLength"); maxLength = value; trimToLength(); return this; }
    public UiTextField filter(Predicate<String> value) { characterFilter = Objects.requireNonNull(value, "value"); return this; }
    public UiTextField validator(java.util.function.Function<String, UiValidationResult> value) { validator = Objects.requireNonNull(value, "value"); validateDraft(); return this; }
    /** Controls whether Escape restores the draft instead of being offered to an enclosing page. */
    public UiTextField escapeCancels(boolean value) { escapeCancels = value; return this; }
    /** Runs after a successful Enter submit, but not after focus-loss commits. */
    public UiTextField onSubmit(Runnable value) { onSubmit = Objects.requireNonNull(value, "value"); return this; }
    public String draft() { return draft; }
    public UiValidationResult validation() { return validation; }
    public boolean commit() {
        validateDraft();
        if (!validation.accepted()) return false;
        binding.set(draft);
        return true;
    }
    public void cancel() { draft = safe(binding.get()); cursor = selection = draft.length(); validation = UiValidationResult.OK; }
    protected UiBinding<String> binding() { return binding; }
    protected void setDraft(String value) { draft = safe(value); trimToLength(); cursor = selection = draft.length(); validateDraft(); }
    protected String safe(String value) { return value == null ? "" : value; }
    private void trimToLength() {
        if (draft.codePointCount(0, draft.length()) <= maxLength) return;
        draft = draft.substring(0, draft.offsetByCodePoints(0, maxLength)); cursor = selection = draft.length();
    }
    protected void validateDraft() { validation = validator.apply(draft); }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        measuredWidth = maxWidth; measuredHeight = theme.metrics().controlHeight();
    }
    @Override public void render(UiRenderer renderer, UiTheme theme) {
        lastRenderer = renderer;
        int background = !enabled() ? theme.palette().controlDisabled() : theme.palette().surfaceRaised();
        renderer.fillRoundRect(bounds, theme.metrics().controlRadius(), background);
        int border = validation.severity() == UiValidationResult.Severity.ERROR ? theme.palette().danger()
            : validation.severity() == UiValidationResult.Severity.WARNING ? theme.palette().warning() : theme.palette().border();
        renderer.strokeRoundRect(bounds, theme.metrics().controlRadius(), theme.metrics().borderWidth(), border);
        String shown = draft.isEmpty() && !focused() ? placeholder : draft;
        int color = draft.isEmpty() && !focused() ? theme.palette().textSecondary()
            : enabled() ? theme.palette().textPrimary() : theme.palette().textDisabled();
        float x = bounds.x() + theme.metrics().padding() / 2f;
        float y = bounds.y() + (bounds.height() - renderer.lineHeight()) / 2f;
        Ui.drawFittedText(renderer, UiText.literal(shown), x, y, Math.max(0, bounds.width() - theme.metrics().padding()), color);
        if (focused() && enabled()) {
            int safeCursor = Math.min(cursor, draft.length());
            float cursorX = x + renderer.textWidth(UiText.literal(draft.substring(0, safeCursor)));
            renderer.fillRoundRect(new UiBounds(Math.min(cursorX, bounds.x() + bounds.width() - 1), y, 1, renderer.lineHeight()), 0, theme.palette().accent());
        }
    }
    @Override public boolean click(float x, float y, int button) {
        if (!enabled() || button != 0 || !bounds.contains(x, y)) return false;
        if (lastRenderer == null) { cursor = selection = draft.length(); return true; }
        float target = Math.max(0, x - (bounds.x() + 8));
        int candidate = 0;
        while (candidate < draft.length()) {
            int next = draft.offsetByCodePoints(candidate, 1);
            if (lastRenderer.textWidth(UiText.literal(draft.substring(0, next))) > target) break;
            candidate = next;
        }
        cursor = selection = candidate; return true;
    }
    @Override public void setFocused(boolean value) {
        boolean wasFocused = focused(); super.setFocused(value);
        if (wasFocused && !value) commit();
    }
    @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) {
        if (!enabled()) return false;
        int key = event.keyCode();
        if (event.controlDown()) {
            if (key == UiKey.A) { selection = 0; cursor = draft.length(); return true; }
            if (key == UiKey.C) { clipboard.set(selected()); return true; }
            if (key == UiKey.X) { clipboard.set(selected()); replaceSelection(""); return true; }
            if (key == UiKey.V) { insert(clipboard.get()); return true; }
        }
        if (key == UiKey.ENTER) {
            boolean committed = commit();
            if (committed) onSubmit.run();
            return committed;
        }
        if (key == UiKey.ESCAPE && escapeCancels) { cancel(); return true; }
        if (key == UiKey.HOME) { move(0, event.shiftDown()); return true; }
        if (key == UiKey.END) { move(draft.length(), event.shiftDown()); return true; }
        if (key == UiKey.LEFT) { move(previous(cursor), event.shiftDown()); return true; }
        if (key == UiKey.RIGHT) { move(next(cursor), event.shiftDown()); return true; }
        if (key == UiKey.BACKSPACE) { if (hasSelection()) replaceSelection(""); else if (cursor > 0) { int at = previous(cursor); draft = draft.substring(0, at) + draft.substring(cursor); cursor = selection = at; validateDraft(); } return true; }
        if (key == UiKey.DELETE) { if (hasSelection()) replaceSelection(""); else if (cursor < draft.length()) { int at = next(cursor); draft = draft.substring(0, cursor) + draft.substring(at); validateDraft(); } return true; }
        return false;
    }
    @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) { if (!enabled() || Character.isISOControl(event.codePoint())) return false; insert(event.text()); return true; }
    private void insert(String value) {
        String accepted = value == null ? "" : value.codePoints().filter(point -> characterFilter.test(new String(Character.toChars(point))))
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
        replaceSelection(accepted);
    }
    private void replaceSelection(String value) {
        int start = Math.min(cursor, selection), end = Math.max(cursor, selection);
        String candidate = draft.substring(0, start) + value + draft.substring(end);
        if (candidate.codePointCount(0, candidate.length()) > maxLength) return;
        draft = candidate; cursor = selection = start + value.length(); validateDraft();
    }
    private String selected() { return draft.substring(Math.min(cursor, selection), Math.max(cursor, selection)); }
    private boolean hasSelection() { return cursor != selection; }
    private void move(int to, boolean extend) { cursor = Math.max(0, Math.min(draft.length(), to)); if (!extend) selection = cursor; }
    private int previous(int index) { return index <= 0 ? 0 : draft.offsetByCodePoints(index, -1); }
    private int next(int index) { return index >= draft.length() ? draft.length() : draft.offsetByCodePoints(index, 1); }
    @Override public boolean focusable() { return enabled(); }
}
