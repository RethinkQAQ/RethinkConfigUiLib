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
    public static Section section(UiText title) { return new Section(title); }
    public static Label label(UiText text) { return new Label(text); }
    public static Divider divider() { return new Divider(); }
    public static Button button(UiText text, Runnable action) { return new Button(text, action); }
    public static IconButton iconButton(UiText label, Runnable action) { return new IconButton(label, action); }
    public static Toggle toggle(UiText text, UiBinding<Boolean> binding) { return new Toggle(text, binding); }
    public static Slider slider(UiText text, UiBinding<Double> binding, double min, double max, double step) { return new Slider(text, binding, min, max, step); }
    public static <T> Select<T> select(UiText text, UiBinding<T> binding, List<T> values, Function<T, UiText> labels) { return new Select<>(text, binding, values, labels); }
    public static ScrollView scrollView(Node child) { return new ScrollView(child); }
    public static Tooltip tooltip(Node child, UiText text) { return new Tooltip(child, text); }

    public abstract static class Node {
        protected UiBounds bounds = UiBounds.EMPTY;
        protected float measuredWidth;
        protected float measuredHeight;
        private boolean enabled = true;
        private boolean hovered;
        private boolean focused;

        public Node enabled(boolean value) { enabled = value; return this; }
        public boolean enabled() { return enabled; }
        public boolean hovered() { return hovered; }
        public boolean focused() { return focused; }
        public UiBounds bounds() { return bounds; }
        public void setHovered(boolean value) { hovered = value; }
        public void setFocused(boolean value) { focused = value; }
        public final void measure(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) { measureSelf(renderer, maxWidth, maxHeight, theme); }
        protected abstract void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme);
        public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) { bounds = value; }
        public abstract void render(UiRenderer renderer, UiTheme theme);
        public boolean click(float mouseX, float mouseY, int button) { return false; }
        public boolean scroll(float mouseX, float mouseY, double amount) { return false; }
        public boolean key(int keyCode) { return false; }
        public boolean focusable() { return false; }
        public float measuredWidth() { return measuredWidth; }
        public float measuredHeight() { return measuredHeight; }
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
            // A very soft, flat-design shadow gives cards separation without introducing a
            // texture, shader or native rendering dependency.
            r.fillRoundRect(bounds.offset(0, 3), t.metrics().radius() + 2, 0x16000000);
            r.fillRoundRect(bounds, t.metrics().radius(), color == Integer.MIN_VALUE ? t.palette().surfaceRaised() : color);
            super.render(r, t);
        }
    }

    public static final class Section extends Panel {
        private final UiText title;
        private Section(UiText value) { title = Objects.requireNonNull(value, "title"); }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) { super.measureSelf(r, maxW, maxH - r.lineHeight() - t.metrics().spacing(), t); measuredHeight += r.lineHeight() + t.metrics().spacing(); }
        @Override public void layout(UiRenderer r, UiBounds value, UiTheme t) { super.layout(r, value, t); for (Node child : children) child.layout(r, new UiBounds(child.bounds.x(), child.bounds.y() + r.lineHeight() + t.metrics().spacing(), child.bounds.width(), child.bounds.height()), t); }
        @Override public void render(UiRenderer r, UiTheme t) { super.render(r, t); r.drawText(title, bounds.x() + t.metrics().padding(), bounds.y() + t.metrics().padding(), t.palette().textPrimary()); }
    }

    public static final class Label extends Node {
        private final UiText text;
        private Label(UiText value) { text = Objects.requireNonNull(value, "text"); }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) { measuredWidth = Math.min(maxW, r.textWidth(text)); measuredHeight = r.lineHeight(); }
        @Override public void render(UiRenderer r, UiTheme t) { r.drawText(text, bounds.x(), bounds.y(), t.palette().textSecondary()); }
    }

    public static final class Divider extends Node {
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) { measuredWidth = maxW; measuredHeight = t.metrics().borderWidth(); }
        @Override public void render(UiRenderer r, UiTheme t) { r.fillRoundRect(bounds, 0, t.palette().border()); }
    }

    public static class Button extends Node {
        protected final UiText text; private final Runnable action; private boolean pressed;
        private Button(UiText value, Runnable callback) { text = Objects.requireNonNull(value, "text"); action = Objects.requireNonNull(callback, "action"); }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) { measuredWidth = Math.min(maxW, Math.max(t.metrics().controlHeight() * 2, r.textWidth(text) + t.metrics().padding() * 3)); measuredHeight = t.metrics().controlHeight(); }
        @Override public void render(UiRenderer r, UiTheme t) {
            int color = !enabled() ? t.palette().border() : pressed ? t.palette().accentPressed() : hovered() ? t.palette().accentHover() : t.palette().control();
            r.fillRoundRect(bounds, t.metrics().radius(), color); float x = bounds.x() + (bounds.width() - r.textWidth(text)) / 2; r.drawText(text, x, bounds.y() + (bounds.height() - r.lineHeight()) / 2, t.palette().onAccent());
            if (focused()) r.strokeRoundRect(bounds, t.metrics().radius(), t.metrics().borderWidth(), t.palette().focusRing());
        }
        @Override public boolean click(float x, float y, int button) { if (enabled() && button == 0 && bounds.contains(x, y)) { pressed = true; action.run(); pressed = false; return true; } return false; }
        @Override public boolean key(int keyCode) { if (enabled() && (keyCode == UiKey.ENTER || keyCode == UiKey.SPACE)) { action.run(); return true; } return false; }
        @Override public boolean focusable() { return enabled(); }
    }

    public static final class IconButton extends Button { private IconButton(UiText text, Runnable action) { super(text, action); } }

    public static final class Toggle extends Node {
        private final UiText text; private final UiBinding<Boolean> binding;
        private Toggle(UiText value, UiBinding<Boolean> target) { text = value; binding = target; }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) { measuredWidth = maxW; measuredHeight = t.metrics().controlHeight(); }
        @Override public void render(UiRenderer r, UiTheme t) {
            boolean on = Boolean.TRUE.equals(binding.get()); r.drawText(text, bounds.x(), bounds.y() + (bounds.height() - r.lineHeight()) / 2, t.palette().textPrimary());
            float width = t.metrics().controlHeight() * 1.65f; UiBounds track = new UiBounds(bounds.x() + bounds.width() - width, bounds.y(), width, bounds.height()); r.fillRoundRect(track, track.height() / 2, on ? t.palette().accent() : t.palette().control());
            float knob = track.height() - t.metrics().padding(); r.fillRoundRect(new UiBounds(track.x() + (on ? track.width() - knob - t.metrics().padding() / 2 : t.metrics().padding() / 2), track.y() + t.metrics().padding() / 2, knob, knob), knob / 2, t.palette().surfaceRaised());
            if (focused()) r.strokeRoundRect(track, track.height() / 2, t.metrics().borderWidth(), t.palette().focusRing());
        }
        private void flip() { binding.set(!Boolean.TRUE.equals(binding.get())); }
        @Override public boolean click(float x, float y, int button) { if (enabled() && button == 0 && bounds.contains(x, y)) { flip(); return true; } return false; }
        @Override public boolean key(int keyCode) { if (enabled() && (keyCode == UiKey.ENTER || keyCode == UiKey.SPACE || keyCode == UiKey.LEFT || keyCode == UiKey.RIGHT)) { flip(); return true; } return false; }
        @Override public boolean focusable() { return enabled(); }
    }

    public static final class Slider extends Node {
        private final UiText text; private final UiBinding<Double> binding; private final double min, max, step;
        private Slider(UiText value, UiBinding<Double> target, double low, double high, double increment) { text = value; binding = target; min = low; max = high; step = increment; if (high <= low || increment <= 0) throw new IllegalArgumentException("invalid slider range"); }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) { measuredWidth = maxW; measuredHeight = t.metrics().controlHeight() + r.lineHeight(); }
        private void set(double value) { binding.set(Math.max(min, Math.min(max, Math.round((value - min) / step) * step + min))); }
        @Override public void render(UiRenderer r, UiTheme t) { double value = binding.get(); r.drawText(text, bounds.x(), bounds.y(), t.palette().textPrimary()); UiBounds rail = new UiBounds(bounds.x(), bounds.y() + r.lineHeight() + t.metrics().spacing(), bounds.width(), t.metrics().borderWidth() * 2); r.fillRoundRect(rail, rail.height(), t.palette().border()); float ratio = (float) ((value - min) / (max - min)); r.fillRoundRect(new UiBounds(rail.x(), rail.y(), rail.width() * ratio, rail.height()), rail.height(), t.palette().accent()); float knob = t.metrics().controlHeight() * .45f; r.fillRoundRect(new UiBounds(rail.x() + rail.width() * ratio - knob / 2, rail.y() - knob / 2 + rail.height() / 2, knob, knob), knob / 2, t.palette().accent()); }
        @Override public boolean click(float x, float y, int button) { if (enabled() && button == 0 && bounds.contains(x, y)) { set(min + (x - bounds.x()) / bounds.width() * (max - min)); return true; } return false; }
        @Override public boolean key(int keyCode) { if (!enabled()) return false; if (keyCode == UiKey.LEFT || keyCode == UiKey.DOWN) { set(binding.get() - step); return true; } if (keyCode == UiKey.RIGHT || keyCode == UiKey.UP) { set(binding.get() + step); return true; } return false; }
        @Override public boolean focusable() { return enabled(); }
    }

    public static final class Select<T> extends Node {
        private final UiText text; private final UiBinding<T> binding; private final List<T> values; private final Function<T, UiText> labels;
        private Select(UiText value, UiBinding<T> target, List<T> options, Function<T, UiText> labeler) { text = value; binding = target; values = List.copyOf(options); labels = labeler; if (values.isEmpty()) throw new IllegalArgumentException("Select options must not be empty"); }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) { measuredWidth = maxW; measuredHeight = t.metrics().controlHeight(); }
        private void advance(int amount) { int at = values.indexOf(binding.get()); binding.set(values.get(Math.floorMod(at + amount, values.size()))); }
        @Override public void render(UiRenderer r, UiTheme t) { r.fillRoundRect(bounds, t.metrics().radius(), hovered() ? t.palette().controlHover() : t.palette().control()); r.drawText(text, bounds.x() + t.metrics().padding(), bounds.y() + (bounds.height() - r.lineHeight()) / 2, t.palette().onAccent()); UiText selected = labels.apply(binding.get()); r.drawText(selected, bounds.x() + bounds.width() - t.metrics().padding() - r.textWidth(selected), bounds.y() + (bounds.height() - r.lineHeight()) / 2, t.palette().onAccent()); }
        @Override public boolean click(float x, float y, int button) { if (enabled() && button == 0 && bounds.contains(x, y)) { advance(1); return true; } return false; }
        @Override public boolean key(int keyCode) { if (!enabled()) return false; if (keyCode == UiKey.LEFT || keyCode == UiKey.UP) { advance(-1); return true; } if (keyCode == UiKey.RIGHT || keyCode == UiKey.DOWN || keyCode == UiKey.ENTER || keyCode == UiKey.SPACE) { advance(1); return true; } return false; }
        @Override public boolean focusable() { return enabled(); }
    }

    public static final class ScrollView extends Container {
        private float offset;
        private ScrollView(Node child) { add(child); }
        private Node child() { return children.get(0); }
        @Override protected void measureSelf(UiRenderer r, float maxW, float maxH, UiTheme t) { child().measure(r, maxW, Float.MAX_VALUE, t); measuredWidth = child().measuredWidth(); measuredHeight = Math.min(maxH, child().measuredHeight()); }
        @Override public void layout(UiRenderer r, UiBounds value, UiTheme t) { super.layout(r, value, t); child().layout(r, new UiBounds(value.x(), value.y() - offset, value.width(), child().measuredHeight()), t); }
        @Override public void render(UiRenderer r, UiTheme t) { r.pushClip(bounds); child().render(r, t); r.popClip(); }
        @Override public boolean scroll(float x, float y, double amount) { if (!bounds.contains(x, y)) return false; offset = Math.max(0, Math.min(Math.max(0, child().measuredHeight() - bounds.height()), (float) (offset - amount * 14))); return true; }
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
