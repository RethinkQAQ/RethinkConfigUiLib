/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.core;

/** Rendering capability implemented by the host platform. */
public interface UiRenderer {
    void fillRoundRect(UiBounds bounds, float radius, int color);
    void strokeRoundRect(UiBounds bounds, float radius, float width, int color);
    void drawText(UiText text, float x, float y, int color);
    float textWidth(UiText text);
    float lineHeight();
    void pushClip(UiBounds bounds);
    void popClip();
}
