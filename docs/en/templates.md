# Templates and Page Structure

A template is the page shell. It answers where the header, navigation, content
and footer go. It does not know what your settings mean.

## Standard configuration template

Use this for most Mod settings:

```text
Header
Navigation
Content: Section + SettingRow
Footer
```

```java
UiPageHost pages = Ui.pageHost()
    .addPage(UiText.literal("General"),
        Ui.section(UiText.literal("GENERAL"))
            .add(Ui.settingRow(UiText.literal("Enabled"),
                Ui.toggle(UiText.literal(""),
                    UiBinding.of(() -> true, value -> { })))));

UiTemplate page = UiTemplate.topNavigation()
    .header(UiText.literal("My Mod"))
    .navigation(pages.navigation())
    .content(pages)
    .footer(Ui.row()
        .mainAxisAlignment(UiMainAxisAlignment.END)
        .add(Ui.button(UiText.literal("Done"), this::closeScreen)))
    .build();
```

`UiPageHost` supplies navigation and changes the current page.
`UiTemplate` places it in the top-navigation shell. The default Content is
independently scrollable. Footer is outside that scrolling region.

## Page roots and scrolling

`UiScaffold` and `UiTemplate` are complete page roots, not content cards. They
own the fixed Header, Sidebar or Navigation, Content viewport and Footer. When
one is passed directly to `UiPageHost`, RCUI lays it out directly instead of
adding another scroll view.

```text
UiHost background
└── UiScaffold
    ├── fixed Header / Sidebar
    ├── clipped, scrollable Content
    └── fixed Footer
```

Do not put a Scaffold or Template inside `UiScrollView`, `UiSection`, or a
Panel that will itself scroll. RCUI rejects a scroll view that contains a page
root, including indirect nesting, because it would make fixed regions scroll
away and produce inconsistent backgrounds or hit areas.

The host owns the normal page background. A Scaffold is transparent by default;
only use its explicit background when the complete page frame needs a distinct
surface:

```java
UiScaffold page = Ui.scaffold(pages)
    .background(UiBackground.opaque(0xFF202124))
    .sidebar(pages.navigation())
    .footer(footer);
```

Use `UiSection` and `UiPanel` inside a Scaffold's Content for ordinary content
grouping, not around the Scaffold itself.

## Two-column configuration template

Use a sidebar when there are many categories:

```text
Header
Content
├── Sidebar navigation
└── Scrollable content
Footer
```

```java
UiPageHost pages = Ui.pageHost()
    .addPage(UiText.literal("General"), generalContent())
    .addPage(UiText.literal("Advanced"), advancedContent());

UiScaffold page = Ui.scaffold(pages)
    .header(Ui.header(UiText.literal("My Mod")))
    .navigationMode(UiScaffold.NavigationMode.SIDEBAR)
    .sidebar(pages.navigation())
    .sidebarWidth(150)
    .footer(Ui.row()
        .mainAxisAlignment(UiMainAxisAlignment.END)
        .add(Ui.button(UiText.literal("Save"), this::saveConfig)));
```

The sidebar is a separate region. Use `UiPageHost` or `UiScrollView` when
content can be taller than the viewport. `sidebarWidth(...)` is a layout value,
not a Minecraft pixel transform.

## Footer rules

There is no standalone `UiFooter` component. A footer is any `Ui.Node`:

```java
Ui.Node footer = Ui.row()
    .mainAxisAlignment(UiMainAxisAlignment.END)
    .add(Ui.button(UiText.literal("Cancel"), this::closeScreen))
    .add(Ui.button(UiText.literal("Save"), this::saveConfig));

UiTemplate page = UiTemplate.topNavigation()
    .content(content)
    .footer(footer)
    .footerDivider(true)
    .build();
```

Do not put the footer inside scrolling Content. Use
`footerAlignment(...)` to align it on the main axis.

## Making a reusable template

Compose existing nodes first. Implement `UiTemplateLayout` only when the same
new shell is reused by several pages or mods:

```java
UiTemplateLayout sidebarLayout = (slots, options) ->
    Ui.scaffold(Ui.split(
            slots.navigation() == null ? Ui.panel() : slots.navigation(),
            slots.content()))
        .header(slots.header())
        .footer(slots.footer())
        .regionGap(options.regionGap() >= 0 ? options.regionGap() : 12);

UiTemplate page = UiTemplate.template()
    .layout(sidebarLayout)
    .header(UiText.literal("Reusable sidebar"))
    .content(editorContent())
    .build();
```

Keep the layout generic. A TotemDoll-specific import button belongs in TotemDoll,
not in RCUI's built-in template.

![Template structures](../imags/template-structures.png)
<!-- TODO: Add screenshots for top navigation and sidebar templates. -->

## Demo

The Navigation page shows a small standard shell. The Templates page demonstrates
top navigation and sidebar roots, including fixed regions and independent Content
scrolling without nesting a complete page inside an outer scroll view.
