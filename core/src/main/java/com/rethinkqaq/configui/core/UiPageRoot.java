/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 *
 * This file is part of Rethink Config UI Lib.
 */

package com.rethinkqaq.configui.core;

/**
 * Marks a complete page frame. Page roots own their fixed regions and content viewport, so they
 * must not be placed inside a {@link com.rethinkqaq.configui.core.layout.UiScrollView}.
 */
public interface UiPageRoot { }
