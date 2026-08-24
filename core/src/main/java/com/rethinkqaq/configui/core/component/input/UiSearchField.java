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

import com.rethinkqaq.configui.core.UiBinding;
import com.rethinkqaq.configui.core.UiClipboard;
import com.rethinkqaq.configui.core.UiKeyEvent;
import com.rethinkqaq.configui.core.UiTextInput;

/** A live-updating text input intended for transient filtering state. */
public class UiSearchField extends UiTextField {
    public UiSearchField(UiBinding<String> binding) { super(binding); }
    @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) {
        boolean handled = super.textInput(event, clipboard); if (handled) binding().set(draft()); return handled;
    }
    @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) {
        boolean handled = super.key(event, clipboard); if (handled && event.keyCode() != com.rethinkqaq.configui.core.UiKey.ESCAPE) binding().set(draft()); return handled;
    }
    @Override public boolean commit() { boolean result = super.commit(); if (result) binding().set(draft()); return result; }
}
