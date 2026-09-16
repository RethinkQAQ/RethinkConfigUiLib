# Themes

A theme is the visual system shared by the UI tree. It changes how components
look without changing business behaviour or the layout contract.

## Built-in themes

```java
UiTheme light = UiTheme.roseLight();
UiTheme dark = UiTheme.roseDark();
UiTheme blueDark = UiTheme.midnightBlue();
```

The light theme uses black, white, grey and a rose accent. `roseDark` retains
the rose accent on dark surfaces; `midnightBlue` uses dark blue-grey surfaces
and a blue accent. `roseLight` remains the default, so existing screens do not
change.

## Derive a theme

Prefer deriving from an existing theme:

```java
UiTheme customBlue = UiTheme.midnightBlue().withAccent(0xFF4F9CFF);
```

withAccent replaces the accent, hover and pressed colours with one colour.
Use withPalette for the complete semantic palette, withMetrics for dimensions,
withMotion for transitions, and withStates for shared state strengths.

Interactive controls smoothly grow their corner radius on hover by default.
This affects rendering only, not hit bounds. The scale can be tuned through
state visuals:

```java
UiTheme theme = UiTheme.roseLight().withStates(
    UiTheme.UiStateVisuals.defaults()
        .withHoverRadiusScale(1.3f)
);
```

Hover text scaling is intentionally deferred until the text system can avoid
raster jitter from continuously transforming Minecraft's pixel font.

## Typography and text styles

The theme provides fallback text colours, density metrics and component text
levels. Use `UiTextStyle` when one node needs a different role, scale or
explicit colour:

```java
UiTextStyle note = UiTextStyle.secondary().scale(.9f).color(0xFF9CA3AF);
Ui.Node label = Ui.label(UiText.literal("Optional setting")).textStyle(note);
```

An unset style colour falls back to the component/theme. A text style does not
change backgrounds, padding or business behaviour.

## Create a theme from scratch

UiTheme.custom uses default motion and state visuals:

```java
UiTheme blue = UiTheme.custom(
    UiTheme.UiPalette.builder()
        .background(0xFF111827)
        .surfaceRaised(0xFF1F2937)
        .control(0xFF374151)
        .controlHover(0xFF4B5563)
        .controlPressed(0xFF6B7280)
        .controlDisabled(0xFF374151)
        .accent(0xFF60A5FA)
        .accentHover(0xFF93C5FD)
        .accentPressed(0xFF3B82F6)
        .onAccent(0xFF0F172A)
        .textPrimary(0xFFF9FAFB)
        .textSecondary(0xFFD1D5DB)
        .textDisabled(0xFF9CA3AF)
        .border(0xFF4B5563)
        .focusRing(0xFF93C5FD)
        .build(),
    UiTheme.UiMetrics.comfortable()
);
```

Use the full builder only when motion or state visuals must also change:

```java
UiTheme complete = UiTheme.builder()
    .palette(palette)
    .metrics(metrics)
    .motion(UiTheme.UiMotion.defaults())
    .states(UiTheme.UiStateVisuals.defaults())
    .build();
```

The palette is semantic: surfaces, controls, accent, text, borders, success,
warning and danger. A theme contains no business logic and must not modify
Minecraft render state.

## Background and colour helpers

```java
UiBackground.opaque(0xFF202124);
UiBackground.translucent(0xFF202124, 0.78f);
UiBackground.transparent();

UiColor.withOpacity(0xFFFFFF, 0.5f);
UiColor.withAlpha(0x80FFFFFF, 0.5f);
UiColor.mix(base, accent, 0.5f);
```

Opacity and blend strengths use the 0–1 range.

## Density

```java
UiScalePolicy.minecraft();
UiScalePolicy.adaptive();
UiScalePolicy.fixed(UiDensity.COMPACT);
```

GUI Scale 1–2 maps to COMFORTABLE, 3–4 to NORMAL and 5–8 to COMPACT.
Density changes component heights, spacing, padding, text levels and card gaps.
It does not scale the canvas or change mouse and Scissor coordinates.

## Validation checklist

- Check normal, hover, pressed, focused, disabled and selected states.
- Check error, warning and success contrast.
- Check buttons, TextField, Tooltip, Dialog, Footer and PreviewCard.
- Check narrow and wide logical viewports.
- Keep layout sizes and business behaviour independent from the theme.

Header style belongs to page structure, not the theme. Current UiHeaderStyle
values are CARD, COMPACT, TEXT and NONE; there is no MODERN value.

![Theme states](../imags/theme-states.png)
<!-- TODO: Add a screenshot showing every interactive state and overlay. -->

## Demo

The Themes page shows roseLight, roseDark, midnightBlue and density
guidance. Input and Feedback contain the interactive state and overlay checks.
