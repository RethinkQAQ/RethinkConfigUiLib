/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.rethinkqaq.configui.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Stock UI nodes and concise factories for constructing a configuration page. */
public final class Ui {
    private Ui() { }
    public static Column column() { return new Column(); }
    public static Row row() { return new Row(); }
    public static Stack stack() { return new Stack(); }
    public static Panel panel() { return new Panel(); }
    public static Panel card() { return new Panel(); }
    public static Section section(UiText title) { return new Section(title); }
    public static Label label(UiText text) { return new Label(text); }
    public static Divider divider() { return new Divider(); }
    public static UiBadge badge(UiText text) { return new UiBadge(text); }
    public static UiGrid grid() { return new UiGrid(); }
    public static UiScaffold scaffold(Node content) { return new UiScaffold(content); }
    public static UiPageHost pageHost() { return new UiPageHost(); }
    public static UiSplitLayout split(Node primary, Node secondary) { return new UiSplitLayout(primary, secondary); }
    public static UiSettingRow settingRow(UiText label, Node control) { return new UiSettingRow(label, control); }
    public static UiPreviewCard previewCard(UiText title, Node preview) { return new UiPreviewCard(title, preview); }
    public static Button button(UiText text, Runnable action) { return new Button(text, action); }
    public static IconButton iconButton(UiText label, Runnable action) { return new IconButton(label, action); }
    public static Toggle toggle(UiText text, UiBinding<Boolean> binding) { return new Toggle(text, binding); }
    public static Slider slider(UiText text, UiBinding<Double> binding, double min, double max, double step) { return new Slider(text, binding, min, max, step); }
    public static <T> Select<T> select(UiText text, UiBinding<T> binding, List<T> values, Function<T, UiText> labels) { return new Select<>(text, binding, values, labels); }
    public static ScrollView scrollView(Node child) { return new ScrollView(child); }
    public static Tooltip tooltip(Node child, UiText text) { return new Tooltip(child, text); }

    /** Draws one line, fitting literal text with an ellipsis before rendering. */
    public static void drawFittedText(UiRenderer renderer, UiText text, float x, float y, float maxWidth, int color) {
        if (maxWidth <= 0) return;
        UiText displayed = fitted(renderer, text, maxWidth);
        renderer.drawText(displayed, x, y, color);
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

    public static void drawWrappedText(UiRenderer renderer, UiText text, float x, float y, float maxWidth,
                                       int maxLines, int color, float lineGap) {
        List<UiText> lines = wrapLines(renderer, text, maxWidth, maxLines);
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
        /** Marks this node's measured size or child structure as changed. */
        public void invalidateLayout() { layoutVersion++; }
        public long layoutVersion() { return layoutVersion; }
        public void setHovered(boolean value) { hovered = value; }
        public void setFocused(boolean value) { focused = value; }
        /** Current interpolated hover state in the inclusive range [0, 1]. */
        public float hoverProgress() { return hoverProgress; }
        /** Current interpolated keyboard-focus state in the inclusive range [0, 1]. */
        public float focusProgress() { return focusProgress; }
        /** Advanced by the host once per frame; no platform types are needed in core. */
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
        /** Called while the primary mouse button is held after a successful click. */
        public boolean drag(float mouseX, float mouseY, int button) { return false; }
        public boolean key(int keyCode) { return false; }
        /** Called when the host receives a mouse-release event. */
        public boolean release(float mouseX, float mouseY, int button) { return false; }
        public boolean focusable() { return false; }
        protected boolean hasVisibleFocus(UiTheme theme) {
            return focusProgress() > .01f && ((theme.palette().focusRing() >>> 24) & 0xFF) > 0;
        }
        public float measuredWidth() { return measuredWidth; }
        public float measuredHeight() { return measuredHeight; }
    }

    /** Implemented by nodes which own children without using the generic container layout. */
    public interface ChildProvider {
        List<Node> childNodes();
    }

    public abstract static class Container extends Node {
        protected final List<Node> children = new ArrayList<>();
        public Container add(Node child) { children.add(Objects.requireNonNull(child, "child")); return this; }
        public List<Node> children() { return List.copyOf(children); }
        @Override public boolean click(float x, float y, int button) {
            for (int index = children.size() - 1; index >= 0; index--) if (children.get(index).click(x, y, button)) return true;
            return false;
        }
        @Override public boolean scroll(float x, float y, double amount) {
            for (int index = children.size() - 1; index >= 0; index--) if (children.get(index).scroll(x, y, amount)) return true;
            return false;
        }
        public List<Node> focusableNodes() {
            List<Node> result = new ArrayList<>();
            for (Node child : children) { if (child.focusable()) result.add(child); if (child instanceof Container container) result.addAll(container.focusableNodes()); }
            return result;
        }
    }

    public static class Column extends Container {
        private float gap = -1;
        public Column gap(float value) { gap = value; return this; }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) {
            float width = 0, height = 0, actualGap = gap < 0 ? t.metrics().spacing() : gap;
            for (Node child : children) { child.measure(r, maxW, maxH, t); width = Math.max(width, child.measuredWidth()); height += child.measuredHeight(); }
            measuredWidth = Math.min(maxW, width); measuredHeight = Math.min(maxH, Math.max(0, height + actualGap * Math.max(0, children.size() - 1)));
        }
        @Override public void layout(UiRenderer r, UiBounds value, UiTheme t) {
            super.layout(r, value, t); float y = value.y(), actualGap = gap < 0 ? t.metrics().spacing() : gap;
            for (Node child : children) { child.layout(r, new UiBounds(value.x(), y, value.width(), child.measuredHeight()), t); y += child.measuredHeight() + actualGap; }
        }
        @Override public void render(UiRenderer r, UiTheme t) { for (Node child : children) child.render(r, t); }
    }

    public static final class Row extends Container {
        private float gap = -1;
        public Row gap(float value) { gap = value; return this; }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) {
            float width = 0, height = 0, actualGap = gap < 0 ? t.metrics().spacing() : gap;
            for (Node child : children) { child.measure(r, maxW, maxH, t); width += child.measuredWidth(); height = Math.max(height, child.measuredHeight()); }
            measuredWidth = Math.min(maxW, width + actualGap * Math.max(0, children.size() - 1)); measuredHeight = height;
        }
        @Override public void layout(UiRenderer r, UiBounds value, UiTheme t) {
            super.layout(r, value, t); float x = value.x(), actualGap = gap < 0 ? t.metrics().spacing() : gap;
            for (Node child : children) { child.layout(r, new UiBounds(x, value.y(), child.measuredWidth(), value.height()), t); x += child.measuredWidth() + actualGap; }
        }
        @Override public void render(UiRenderer r, UiTheme t) { for (Node child : children) child.render(r, t); }
    }

    public static final class Stack extends Container {
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) {
            measuredWidth = measuredHeight = 0;
            for (Node child : children) { child.measure(r, maxW, maxH, t); measuredWidth = Math.max(measuredWidth, child.measuredWidth()); measuredHeight = Math.max(measuredHeight, child.measuredHeight()); }
        }
        @Override public void layout(UiRenderer r, UiBounds value, UiTheme t) { super.layout(r, value, t); for (Node child : children) child.layout(r, value, t); }
        @Override public void render(UiRenderer r, UiTheme t) { for (Node child : children) child.render(r, t); }
    }

    public static class Panel extends Column {
        private int color = Integer.MIN_VALUE;
        public Panel color(int value) { color = value; return this; }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) {
            super.measureSelf(r, Math.max(0, maxW - t.metrics().padding() * 2), maxH, t);
            measuredWidth += t.metrics().padding() * 2; measuredHeight += t.metrics().padding() * 2;
        }
        @Override public void layout(UiRenderer r, UiBounds value, UiTheme t) { super.layout(r, value, t); super.layout(r, value.inset(t.metrics().padding()), t); bounds = value; }
        @Override public void render(UiRenderer r, UiTheme t) {
            if (t.metrics().shadowOffset() > 0) {
                r.fillRoundRect(bounds.offset(0, t.metrics().shadowOffset()), t.metrics().cardRadius(), 0x10000000);
            }
            r.fillRoundRect(bounds, t.metrics().cardRadius(), color == Integer.MIN_VALUE ? t.palette().surfaceRaised() : color);
            r.strokeRoundRect(bounds, t.metrics().cardRadius(), t.metrics().borderWidth(), t.palette().border());
            super.render(r, t);
        }
    }

    public static final class Section extends Panel {
        private final UiText title;
        private Section(UiText value) { title = Objects.requireNonNull(value, "title"); }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) { super.measureSelf(r, maxW, maxH - r.lineHeight() - t.metrics().spacing(), t); measuredHeight += r.lineHeight() + t.metrics().spacing(); }
        @Override public void layout(UiRenderer r, UiBounds value, UiTheme t) { super.layout(r, value, t); for (Node child : children) child.layout(r, new UiBounds(child.bounds.x(), child.bounds.y() + r.lineHeight() + t.metrics().spacing(), child.bounds.width(), child.bounds.height()), t); }
        @Override public void render(UiRenderer r, UiTheme t) { super.render(r, t); drawFittedText(r, title, bounds.x() + t.metrics().padding(), bounds.y() + t.metrics().padding(), bounds.width() - t.metrics().padding() * 2, t.palette().textPrimary()); }
    }

    public static final class Label extends Node {
        private final UiText text;
        private boolean wrapped;
        private int maxLines = 3;
        private Label(UiText value) { text = Objects.requireNonNull(value, "text"); }
        public Label wrap(boolean value) { wrapped = value; invalidateLayout(); return this; }
        public Label maxLines(int value) { if (value <= 0) throw new IllegalArgumentException("maxLines must be positive"); maxLines = value; invalidateLayout(); return this; }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) {
            List<UiText> lines = wrapped ? wrapLines(r, text, maxW, maxLines) : List.of(text);
            measuredWidth = Math.min(maxW, (float) lines.stream().mapToDouble(r::textWidth).max().orElse(0));
            measuredHeight = Math.min(maxH, r.lineHeight() * lines.size());
        }
        @Override public void render(UiRenderer r, UiTheme t) {
            if (wrapped) drawWrappedText(r, text, bounds.x(), bounds.y(), bounds.width(), maxLines, t.palette().textSecondary(), 0);
            else drawFittedText(r, text, bounds.x(), bounds.y(), bounds.width(), t.palette().textSecondary());
        }
    }

    public static final class Divider extends Node {
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) { measuredWidth = maxW; measuredHeight = t.metrics().borderWidth(); }
        @Override public void render(UiRenderer r, UiTheme t) { r.fillRoundRect(bounds, 0, t.palette().border()); }
    }

    public static class Button extends Node {
        protected final UiText text; private final Runnable action; private boolean pressed;
        private ButtonVariant variant = ButtonVariant.PRIMARY;
        private Button(UiText value, Runnable callback) { text = Objects.requireNonNull(value, "text"); action = Objects.requireNonNull(callback, "action"); }
        public Button variant(ButtonVariant value) { variant = Objects.requireNonNull(value, "variant"); return this; }
        public ButtonVariant variant() { return variant; }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) { measuredWidth = Math.min(maxW, Math.max(t.metrics().controlHeight() * 2, r.textWidth(text) + t.metrics().padding() * 3)); measuredHeight = t.metrics().controlHeight(); }
        @Override public void render(UiRenderer r, UiTheme t) {
            int color = color(t);
            int textColor = (variant == ButtonVariant.SECONDARY || variant == ButtonVariant.OUTLINE) && enabled()
                ? t.palette().textPrimary() : t.palette().onAccent();
            r.fillRoundRect(bounds, t.metrics().controlRadius(), color); UiText displayed = fitted(r, text, Math.max(0, bounds.width() - t.metrics().padding() * 2)); float x = bounds.x() + (bounds.width() - r.textWidth(displayed)) / 2; drawFittedText(r, displayed, x, bounds.y() + (bounds.height() - r.lineHeight()) / 2, Math.max(0, bounds.x() + bounds.width() - t.metrics().padding() - x), textColor);
            if (variant == ButtonVariant.OUTLINE || variant == ButtonVariant.SECONDARY) r.strokeRoundRect(bounds, t.metrics().controlRadius(), t.metrics().borderWidth(), t.palette().border());
            if (hasVisibleFocus(t)) r.strokeRoundRect(bounds, t.metrics().controlRadius(), t.metrics().borderWidth(), blend(t.palette().border(), t.palette().focusRing(), focusProgress()));
        }
        private int color(UiTheme theme) {
            if (!enabled()) return theme.palette().controlDisabled();
            return switch (variant) {
                case PRIMARY -> pressed ? theme.palette().accentPressed() : blend(theme.palette().control(), theme.palette().accentHover(), hoverProgress());
                case SECONDARY -> pressed ? theme.palette().border() : blend(theme.palette().surfaceRaised(), theme.palette().surface(), hoverProgress());
                case OUTLINE -> pressed ? theme.palette().border() : blend(theme.palette().surface(), theme.palette().surfaceRaised(), hoverProgress());
                case DANGER -> pressed ? theme.palette().danger() : blend(theme.palette().danger(), theme.palette().accentPressed(), hoverProgress());
            };
        }
        @Override public boolean click(float x, float y, int button) { if (enabled() && button == 0 && bounds.contains(x, y)) { pressed = true; action.run(); return true; } return false; }
        @Override public boolean key(int keyCode) { if (enabled() && (keyCode == UiKey.ENTER || keyCode == UiKey.SPACE)) { action.run(); return true; } return false; }
        @Override public boolean release(float x, float y, int button) { boolean wasPressed = pressed; if (button == 0) pressed = false; return wasPressed; }
        @Override public boolean focusable() { return enabled(); }
    }

    public static final class IconButton extends Button { private IconButton(UiText text, Runnable action) { super(text, action); } }

    public static final class Toggle extends Node {
        private final UiText text; private final UiBinding<Boolean> binding;
        private float onProgress = -1f;
        private long lastValueMotionNanos;
        private Toggle(UiText value, UiBinding<Boolean> target) { text = value; binding = target; }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) { measuredWidth = maxW; measuredHeight = t.metrics().controlHeight(); }
        @Override public void render(UiRenderer r, UiTheme t) {
            boolean on = Boolean.TRUE.equals(binding.get());
            float width = t.metrics().controlHeight() * 1.65f;
            drawFittedText(r, text, bounds.x(), bounds.y() + (bounds.height() - r.lineHeight()) / 2,
                Math.max(0, bounds.width() - width - t.metrics().spacing()), enabled() ? t.palette().textPrimary() : t.palette().textDisabled());
            float progress = onProgress < 0 ? (on ? 1f : 0f) : onProgress;
            UiBounds track = new UiBounds(bounds.x() + bounds.width() - width, bounds.y(), width, bounds.height()); r.fillRoundRect(track, track.height() / 2, !enabled() ? t.palette().controlDisabled() : blend(t.palette().control(), t.palette().accent(), progress));
            float knob = track.height() - t.metrics().padding(); r.fillRoundRect(new UiBounds(track.x() + t.metrics().padding() / 2 + (track.width() - knob - t.metrics().padding()) * progress, track.y() + t.metrics().padding() / 2, knob, knob), knob / 2, t.palette().surfaceRaised());
            if (hasVisibleFocus(t)) r.strokeRoundRect(track, track.height() / 2, t.metrics().borderWidth(), blend(t.palette().border(), t.palette().focusRing(), focusProgress()));
        }
        /** Interpolated visual state, useful for custom renderers and tests. */
        public float onProgress() { return onProgress < 0 ? (Boolean.TRUE.equals(binding.get()) ? 1f : 0f) : onProgress; }
        @Override public void advanceMotion(long nowNanos, UiTheme theme) {
            super.advanceMotion(nowNanos, theme);
            float target = Boolean.TRUE.equals(binding.get()) ? 1f : 0f;
            float elapsedMillis = lastValueMotionNanos == 0 ? 0f : Math.max(0f, (nowNanos - lastValueMotionNanos) / 1_000_000f);
            lastValueMotionNanos = nowNanos;
            if (onProgress < 0) onProgress = target;
            else onProgress = approach(onProgress, target, elapsedMillis, theme.motion().toggleMillis());
        }
        private void flip() { binding.set(!Boolean.TRUE.equals(binding.get())); }
        @Override public boolean click(float x, float y, int button) { if (enabled() && button == 0 && bounds.contains(x, y)) { flip(); return true; } return false; }
        @Override public boolean key(int keyCode) { if (enabled() && (keyCode == UiKey.ENTER || keyCode == UiKey.SPACE || keyCode == UiKey.LEFT || keyCode == UiKey.RIGHT)) { flip(); return true; } return false; }
        @Override public boolean focusable() { return enabled(); }
    }

    public static final class Slider extends Node {
        private final UiText text; private final UiBinding<Double> binding; private final double min, max, step;
        private float displayedRatio = -1f;
        private long lastValueMotionNanos;
        private Slider(UiText value, UiBinding<Double> target, double low, double high, double increment) { text = value; binding = target; min = low; max = high; step = increment; if (high <= low || increment <= 0) throw new IllegalArgumentException("invalid slider range"); }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) { measuredWidth = maxW; measuredHeight = t.metrics().controlHeight() + r.lineHeight(); }
        private void set(double value) { binding.set(Math.max(min, Math.min(max, Math.round((value - min) / step) * step + min))); }
        @Override public void render(UiRenderer r, UiTheme t) { int textColor = enabled() ? t.palette().textPrimary() : t.palette().textDisabled(); int accent = enabled() ? t.palette().accent() : t.palette().controlDisabled(); drawFittedText(r, text, bounds.x(), bounds.y(), bounds.width(), textColor); UiBounds rail = new UiBounds(bounds.x(), bounds.y() + r.lineHeight() + t.metrics().spacing(), bounds.width(), Math.max(1f, t.metrics().borderWidth() * 2)); r.fillRoundRect(rail, rail.height(), t.palette().border()); float ratio = displayedRatio < 0 ? targetRatio() : displayedRatio; r.fillRoundRect(new UiBounds(rail.x(), rail.y(), rail.width() * ratio, rail.height()), rail.height(), accent); float knob = t.metrics().controlHeight() * .45f; r.fillRoundRect(new UiBounds(rail.x() + rail.width() * ratio - knob / 2, rail.y() - knob / 2 + rail.height() / 2, knob, knob), knob / 2, accent); if (hasVisibleFocus(t)) r.strokeRoundRect(new UiBounds(rail.x() - knob / 2, rail.y() - knob / 2, rail.width() + knob, knob + rail.height()), knob / 2, t.metrics().borderWidth(), blend(t.palette().border(), t.palette().focusRing(), focusProgress())); }
        public float displayedRatio() { return displayedRatio < 0 ? targetRatio() : displayedRatio; }
        @Override public void advanceMotion(long nowNanos, UiTheme theme) {
            super.advanceMotion(nowNanos, theme);
            float target = targetRatio();
            float elapsedMillis = lastValueMotionNanos == 0 ? 0f : Math.max(0f, (nowNanos - lastValueMotionNanos) / 1_000_000f);
            lastValueMotionNanos = nowNanos;
            if (displayedRatio < 0) displayedRatio = target;
            else displayedRatio = approach(displayedRatio, target, elapsedMillis, theme.motion().toggleMillis());
        }
        private float targetRatio() { return Math.max(0f, Math.min(1f, (float) ((binding.get() - min) / (max - min)))); }
        private boolean dragging;
        private void setFromX(float x) { set(min + Math.max(0f, Math.min(1f, (x - bounds.x()) / Math.max(1f, bounds.width()))) * (max - min)); }
        @Override public boolean click(float x, float y, int button) { if (enabled() && button == 0 && bounds.contains(x, y)) { dragging = true; setFromX(x); return true; } return false; }
        @Override public boolean drag(float x, float y, int button) { if (enabled() && dragging && button == 0) { setFromX(x); return true; } return false; }
        @Override public boolean release(float x, float y, int button) { boolean wasDragging = dragging; if (button == 0) dragging = false; return wasDragging; }
        @Override public boolean key(int keyCode) { if (!enabled()) return false; if (keyCode == UiKey.LEFT || keyCode == UiKey.DOWN) { set(binding.get() - step); return true; } if (keyCode == UiKey.RIGHT || keyCode == UiKey.UP) { set(binding.get() + step); return true; } return false; }
        @Override public boolean focusable() { return enabled(); }
    }

    public static final class Select<T> extends Node {
        private final UiText text; private final UiBinding<T> binding; private final List<T> values; private final Function<T, UiText> labels;
        private Select(UiText value, UiBinding<T> target, List<T> options, Function<T, UiText> labeler) { text = value; binding = target; values = List.copyOf(options); labels = labeler; if (values.isEmpty()) throw new IllegalArgumentException("Select options must not be empty"); }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) { measuredWidth = maxW; measuredHeight = t.metrics().controlHeight(); }
        private void advance(int amount) { int at = values.indexOf(binding.get()); binding.set(values.get(Math.floorMod(at + amount, values.size()))); }
        @Override public void render(UiRenderer r, UiTheme t) { int color = !enabled() ? t.palette().controlDisabled() : blend(t.palette().control(), t.palette().controlHover(), hoverProgress()); r.fillRoundRect(bounds, t.metrics().controlRadius(), color); int textColor = enabled() ? t.palette().onAccent() : t.palette().textDisabled(); float innerWidth = Math.max(0, bounds.width() - t.metrics().padding() * 2), half = innerWidth * .5f; drawFittedText(r, text, bounds.x() + t.metrics().padding(), bounds.y() + (bounds.height() - r.lineHeight()) / 2, half, textColor); UiText selected = fitted(r, labels.apply(binding.get()), half); drawFittedText(r, selected, bounds.x() + bounds.width() - t.metrics().padding() - r.textWidth(selected), bounds.y() + (bounds.height() - r.lineHeight()) / 2, half, textColor); if (hasVisibleFocus(t)) r.strokeRoundRect(bounds, t.metrics().controlRadius(), t.metrics().borderWidth(), blend(t.palette().border(), t.palette().focusRing(), focusProgress())); }
        @Override public boolean click(float x, float y, int button) { if (enabled() && button == 0 && bounds.contains(x, y)) { advance(1); return true; } return false; }
        @Override public boolean key(int keyCode) { if (!enabled()) return false; if (keyCode == UiKey.LEFT || keyCode == UiKey.UP) { advance(-1); return true; } if (keyCode == UiKey.RIGHT || keyCode == UiKey.DOWN || keyCode == UiKey.ENTER || keyCode == UiKey.SPACE) { advance(1); return true; } return false; }
        @Override public boolean focusable() { return enabled(); }
    }

    public static final class ScrollView extends Container {
        private float offset;
        private ScrollView(Node child) { add(child); }
        private Node child() { return children.get(0); }
        public float offset() { return offset; }
        public void reset() { offset = 0; }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) { child().measure(r, maxW, Float.MAX_VALUE, t); measuredWidth = child().measuredWidth(); measuredHeight = Math.min(maxH, child().measuredHeight()); }
        @Override public void layout(UiRenderer r, UiBounds value, UiTheme t) { super.layout(r, value, t); child().layout(r, new UiBounds(value.x(), value.y() - offset, value.width(), child().measuredHeight()), t); }
        @Override public void render(UiRenderer r, UiTheme t) { r.pushClip(bounds); child().render(r, t); r.popClip(); }
        @Override public boolean scroll(float x, float y, double amount) { if (!bounds.contains(x, y)) return false; offset = Math.max(0, Math.min(Math.max(0, child().measuredHeight() - bounds.height()), (float) (offset - amount * 14))); return true; }
        @Override public boolean key(int keyCode) {
            float max = Math.max(0, child().measuredHeight() - bounds.height());
            float page = Math.max(1, bounds.height() - 14);
            if (keyCode == UiKey.UP) { offset = Math.max(0, offset - 14); return true; }
            if (keyCode == UiKey.DOWN) { offset = Math.min(max, offset + 14); return true; }
            if (keyCode == UiKey.PAGE_UP) { offset = Math.max(0, offset - page); return true; }
            if (keyCode == UiKey.PAGE_DOWN) { offset = Math.min(max, offset + page); return true; }
            if (keyCode == UiKey.HOME) { offset = 0; return true; }
            if (keyCode == UiKey.END) { offset = max; return true; }
            return super.key(keyCode);
        }
    }

    public static final class Tooltip extends Node {
        private final Node child; private final UiText tooltip;
        private Tooltip(Node value, UiText description) { child = value; tooltip = description; }
        public Node child() { return child; }
        public UiText text() { return tooltip; }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) { child.measure(r, maxW, maxH, t); measuredWidth = child.measuredWidth(); measuredHeight = child.measuredHeight(); }
        @Override public void layout(UiRenderer r, UiBounds value, UiTheme t) { super.layout(r, value, t); child.layout(r, value, t); }
        @Override public void render(UiRenderer r, UiTheme t) { child.render(r, t); }
        @Override public boolean click(float x, float y, int button) { return child.click(x, y, button); }
        @Override public boolean scroll(float x, float y, double amount) { return child.scroll(x, y, amount); }
        @Override public boolean key(int keyCode) { return child.key(keyCode); }
        @Override public boolean focusable() { return false; }
    }
}
