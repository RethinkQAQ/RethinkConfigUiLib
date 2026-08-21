/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.minecraft;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBinding;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.gui.screens.Screen;

/** Built-in visual smoke test, enabled only with the RCUI example JVM flag. */
public final class DemoScreen extends UiScreen {
    public DemoScreen() {
        this(null);
    }

    public DemoScreen(Screen parent) {
        super(parent, page(), UiTheme.roseLight());
    }

    private static Ui.Node page() {
        AtomicBoolean enabled = new AtomicBoolean(true);
        AtomicReference<Double> scale = new AtomicReference<>(1.0);
        AtomicReference<String> mode = new AtomicReference<>("Balanced");
        Ui.Column page = Ui.column().gap(18);
        Ui.Container general = Ui.section(UiText.literal("RETHINK CONFIG UI LIB"));
        general.add(Ui.toggle(UiText.literal("Enable feature"), binding(enabled::get, enabled::set)))
            .add(Ui.slider(UiText.literal("Interface scale"), binding(scale::get, scale::set), 0.5, 2.0, 0.1))
            .add(Ui.select(UiText.literal("Render mode"), binding(mode::get, mode::set), List.of("Fast", "Balanced", "Quality"), UiText::literal));
        page.add(Ui.label(UiText.literal("A small, flat configuration surface for your mod")))
            .add(general)
            .add(Ui.section(UiText.literal("ACTIONS"))
                .add(Ui.row().gap(8)
                    .add(Ui.button(UiText.literal("Reset"), () -> {
                        enabled.set(true);
                        scale.set(1.0);
                        mode.set("Balanced");
                    }))
                    .add(Ui.button(UiText.literal("Close"), () -> { })))
                .add(Ui.tooltip(Ui.label(UiText.literal("Changes apply immediately")), UiText.literal("The host decides when to persist values."))));
        return page;
    }

    private static <T> UiBinding<T> binding(java.util.function.Supplier<T> getter, java.util.function.Consumer<T> setter) {
        return UiBinding.of(getter, setter);
    }
}
