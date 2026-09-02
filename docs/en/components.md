# Components

All stock components are nodes. Create them with the Ui.* factories, put them
inside a layout node, and let the parent measure and place them.

Every component section answers what it displays, how it is created, how it
participates in layout and input, and which visual choices belong to the theme.

## Content components

```java
Ui.Node body = Ui.column()
    .gap(8)
    .add(Ui.header(UiText.literal("Page title")))
    .add(Ui.label(UiText.literal("Body text can wrap when the available width is small."))
        .wrap(true))
    .add(Ui.section(UiText.literal("GENERAL"))
        .add(Ui.label(UiText.literal("Related content belongs in one section."))))
    .add(Ui.panel().padding(12)
        .add(Ui.label(UiText.literal("Panel surface"))));
```

- UiLabel displays ordinary text. Use wrap(true) and a line limit for long text.
- UiHeader displays a title and optional subtitle. Its styles are CARD,
  COMPACT, TEXT and NONE.
- UiSection groups content under a title.
- UiPanel and Ui.card() provide a surface and padding.
- UiDivider separates groups without changing business state.
- UiBadge displays a short status with neutral, accent, success, warning or
  danger tone.
- UiNavigationBar is normally supplied by UiPageHost.navigation().

### Preview cards

```java
UiPreviewCard card = Ui.previewCard(
        UiText.literal("A model"),
        Ui.preview((renderer, bounds, clip, theme) -> {
            // Draw only inside bounds and clip.
        }).preferredHeight(96))
    .description(UiText.literal("A bounded preview with an action area."))
    .action(Ui.button(UiText.literal("Select"), this::select))
    .onClick(this::select);
```

Preview cards combine a title, bounded preview area, description, selection
state and optional action node. Use them for models, textures and presets. The
preview does not own your model resource.

## Text styles

`UiText` stores text content. `UiTextStyle` stores reusable visual intent for
that text. This keeps content separate from typography.

```java
UiTextStyle title = UiTextStyle.title().scale(1.35f);
UiTextStyle muted = UiTextStyle.subtitle().scale(.85f).color(0xFF6B7280);
UiTextStyle success = UiTextStyle.success().color(0xFF3A8F5B);

Ui.Node text = Ui.column()
    .gap(6)
    .add(Ui.label(UiText.literal("A page title")).textStyle(title))
    .add(Ui.label(UiText.literal("A quieter explanation")).textStyle(muted))
    .add(Ui.label(UiText.literal("Saved successfully")).textStyle(success));
```

Built-in semantic factories are `title()`, `subtitle()`, `body()`,
`secondary()`, `button()`, `caption()`, `error()` and `success()`. Use
`UiTextStyle.of(UiTextRole.CAPTION)` for a role directly. The style is
immutable: `scale(...)`, `color(...)`, `role(...)` and `overflow(...)` return a
new style, so one style can safely be shared by several nodes.

```java
UiTextStyle actionText = UiTextStyle.button().scale(.9f);
Ui.Node actions = Ui.row()
    .gap(8)
    .add(Ui.button(UiText.literal("Apply"), this::apply).textStyle(actionText))
    .add(Ui.button(UiText.literal("Cancel"), this::cancel).textStyle(actionText));
```

`scale(...)` is a positive text-size multiplier; density and component metrics
are still applied. `color(...)` is an explicit ARGB override. Without it, the
component and theme provide the fallback. `overflow(...)` describes `WRAP`,
`ELLIPSIS`, `CLIP` or `NO_WRAP`; a `UiLabel` currently controls wrapping with
`wrap(true)` and `maxLines(...)`. Text styles do not change backgrounds,
padding, layout or business behaviour.

Headers and buttons also accept styles through `titleStyle(...)`,
`subtitleStyle(...)` and `textStyle(...)`. The `Text styles` Demo page shows
shared styles, scale differences, colour overrides, long text and fitting.

![Text style examples](../imags/text-styles.png)
<!-- TODO: Add a screenshot showing roles, scale, colour overrides and fitting. -->

## Layout components

```java
Ui.Node layout = Ui.column()
    .gap(12)
    .crossAxisAlignment(UiCrossAxisAlignment.STRETCH)
    .add(Ui.row()
        .gap(8)
        .mainAxisAlignment(UiMainAxisAlignment.SPACE_BETWEEN)
        .add(Ui.label(UiText.literal("Left")))
        .add(Ui.button(UiText.literal("Right"), this::action)))
    .add(Ui.grid()
        .minimumColumnWidth(140)
        .maximumColumnWidth(220)
        .gap(8)
        .rowAlignment(UiMainAxisAlignment.CENTER)
        .add(Ui.card())
        .add(Ui.card()))
    .add(Ui.split(leftNode(), rightNode())
        .primaryShare(.6f)
        .compactBelow(620));
```

| Component | Use it for | Important controls |
| --- | --- | --- |
| UiRow | Horizontal content | gap, main/cross alignment, equal child widths |
| UiColumn | Vertical content | gap, main/cross alignment |
| UiStack | Overlapping layers | child order |
| UiGrid | Responsive cards | minimum/maximum column width, gap, rowAlignment |
| UiSplitLayout | Two panes | primaryShare, gap, compactBelow |
| UiScrollView | A bounded scrolling region | child content and scroll input |
| UiPageHost | Pages and navigation | addPage, select, push, pop |
| UiScaffold | Page frame | navigation mode, sidebar width, footer |

Main-axis alignment is START, CENTER, END or SPACE_BETWEEN. Cross-axis
alignment is START, CENTER, END or STRETCH. Grid rowAlignment only aligns
items within each row.

Do not add another scroll view around a page already managed by UiPageHost
unless nested scrolling is intentional.

## Buttons

```java
Ui.Node buttons = Ui.row()
    .gap(8)
    .add(Ui.button(UiText.literal("Primary"), this::primary))
    .add(Ui.button(UiText.literal("Secondary"), this::secondary)
        .variant(Ui.ButtonVariant.SECONDARY))
    .add(Ui.button(UiText.literal("Outline"), this::outline)
        .variant(Ui.ButtonVariant.OUTLINE))
    .add(Ui.button(UiText.literal("Delete"), this::delete)
        .variant(Ui.ButtonVariant.DANGER))
    .add(Ui.iconButton(UiText.literal("+"), this::add));
```

Buttons support clicks, keyboard activation, focus, tooltips, enabled/disabled
state and adaptive text. The theme supplies normal, hover, pressed, focused,
disabled and selected visuals. Keep the action in the host.

## Toggle, select and numeric controls

```java
AtomicBoolean enabled = new AtomicBoolean(false);
AtomicReference<String> mode = new AtomicReference<>("Balanced");
AtomicReference<Double> value = new AtomicReference<>(.5);

Ui.Node controls = Ui.column()
    .gap(8)
    .add(Ui.toggle(UiText.literal("Enabled"),
        UiBinding.of(enabled::get, enabled::set)))
    .add(Ui.select(UiText.literal("Mode"),
        UiBinding.of(mode::get, mode::set),
        List.of("Fast", "Balanced", "Quality"), UiText::literal))
    .add(Ui.slider(UiText.literal("Value"),
        UiBinding.of(value::get, value::set), 0, 1, .1));
```

UiToggle changes a boolean. UiSelect displays a list and accepts a custom label
function. UiSlider supports range, step, dragging and keyboard adjustment.
Use UiNumberControl or UiNumericField with UiSetting and UiNumberSpec when
several controls must share the same numeric contract.

## Text and form controls

```java
AtomicReference<String> query = new AtomicReference<>("");

Ui.Node form = Ui.column()
    .gap(8)
    .add(Ui.formField(UiText.literal("Name"),
        Ui.textField(UiBinding.of(query::get, query::set))
            .placeholder(UiText.literal("Type here"))))
    .add(Ui.searchField(UiBinding.of(query::get, query::set)));
```

Text fields support editing, cursor, focus, Enter submission, Escape
cancellation, text input and clipboard events. Search fields use the same
binding pattern for filtering. Form fields add a label and description.

## Data components

Use UiSelectionList for a selectable list and UiCollectionEditor for entries
that can be added, edited and removed:

```java
Ui.Node choices = Ui.selectionList(
    () -> List.of("One", "Two", "Three"),
    UiBinding.of(selected::get, selected::set),
    UiText::literal);

UiListEntryAdapter<String> adapter = UiListEntryAdapter.builder(
    () -> "", UiText::literal,
    value -> Ui.textField(value).placeholder(UiText.literal("Value")))
    .build();
```

The adapter decides how an entry is displayed and edited. The host owns the
collection and validates it before saving.

## Feedback, tooltips and dialogs

```java
Ui.Node feedback = Ui.column()
    .gap(8)
    .add(Ui.alert(UiFeedbackType.INFO, UiText.literal("Information")))
    .add(Ui.alert(UiFeedbackType.SUCCESS, UiText.literal("Saved")))
    .add(Ui.alert(UiFeedbackType.WARNING, UiText.literal("Check this")))
    .add(Ui.alert(UiFeedbackType.ERROR, UiText.literal("Invalid value")))
    .add(Ui.tooltip(
        Ui.button(UiText.literal("Hover me"), this::action),
        UiText.literal("This tooltip is above normal content.")));
```

UiTooltip accepts text or node-based UiTooltipContent. UiAlert uses info,
success, warning and error semantics. UiToast is shown through the host's
UiNotificationCenter. UiDialogHost owns modal content and routes input to it.

## Styling

Components consume semantic tokens from UiTheme:

- palette: surfaces, controls, accents, text, borders and feedback colours;
- metrics: spacing, padding, radius, border width and component heights;
- state visuals: hover, pressed, focus, disabled and overlay strength;
- motion: hover and focus transitions.

Change the theme when many components should change. Use a component option
when only one instance differs. Components must not modify Minecraft render
state directly.

![Component catalogue](../imags/component-catalogue.png)
<!-- TODO: Add a screenshot grid covering every component category. -->

## Demo

Content covers content components. Layout covers flow, grid, split and scroll.
Input, Data and Feedback cover interactive groups. Preview covers preview cards
and clipping.
