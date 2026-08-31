/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 *
 * This file is part of Rethink Config UI Lib.
 * Rethink Config UI Lib is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation,
 * version 3 of the License.
 */
package com.rethinkqaq.configui.core.component;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiClipboard;
import com.rethinkqaq.configui.core.UiKeyEvent;
import com.rethinkqaq.configui.core.UiTextInput;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Base for reusable stateful components which own a small child tree. */
public abstract class UiComponent extends Ui.Node implements Ui.ChildProvider {
    protected final List<Ui.Node> children = new ArrayList<>();

    protected final <T extends Ui.Node> T child(T value) {
        children.add(Objects.requireNonNull(value, "child"));
        if (mounted()) value.mount();
        invalidateLayout();
        return value;
    }

    protected final boolean removeChild(Ui.Node value) {
        if (!children.remove(value)) return false;
        value.dispose();
        invalidateLayout();
        return true;
    }

    @Override public List<Ui.Node> childNodes() { return List.copyOf(children); }
    @Override public boolean click(float x, float y, int button) { return dispatchClick(x, y, button); }
    @Override public boolean scroll(float x, float y, double amount) {
        for (int i = children.size() - 1; i >= 0; i--) if (children.get(i).scroll(x, y, amount)) return true;
        return false;
    }
    @Override public boolean drag(float x, float y, int button) {
        for (int i = children.size() - 1; i >= 0; i--) if (children.get(i).drag(x, y, button)) return true;
        return false;
    }
    @Override public boolean release(float x, float y, int button) {
        for (int i = children.size() - 1; i >= 0; i--) if (children.get(i).release(x, y, button)) return true;
        return false;
    }
    @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) {
        for (int i = children.size() - 1; i >= 0; i--) if (children.get(i).key(event, clipboard)) return true;
        return false;
    }
    @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) {
        for (int i = children.size() - 1; i >= 0; i--) if (children.get(i).textInput(event, clipboard)) return true;
        return false;
    }
    private boolean dispatchClick(float x, float y, int button) {
        for (int i = children.size() - 1; i >= 0; i--) if (children.get(i).click(x, y, button)) return true;
        return false;
    }
}
