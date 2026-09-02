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

## Tool and editor template

Editors often need a toolbar, a work area, feedback and a modal:

```text
Header
Content
├── Toolbar
├── Main editor area
└── Status/feedback area
Footer
Dialog
```

```java
UiDialogHost dialogs = Ui.dialogHost();

Ui.Node editor = Ui.column()
    .gap(8)
    .add(Ui.row()
        .add(Ui.button(UiText.literal("Import"), this::importData))
        .add(Ui.button(UiText.literal("Export"), this::exportData)))
    .add(Ui.split(
        Ui.panel().padding(12).add(Ui.label(UiText.literal("Editor"))),
        Ui.preview((renderer, bounds, clip, theme) -> {
            // Draw a model or image inside bounds and clip.
        }).preferredHeight(160)))
    .add(Ui.alert(UiFeedbackType.INFO, UiText.literal("Ready")));

UiScaffold page = Ui.scaffold(editor)
    .header(Ui.textHeader(UiText.literal("Editor")))
    .footer(Ui.row().add(Ui.button(UiText.literal("Open"),
        () -> dialogs.show(dialogContent()))));

Ui.Node root = dialogs.root(page);
```

Use `UiStack` for overlapping layers, `UiSplitLayout` for responsive panes,
`UiPreview` for bounded rendering, and `UiNotificationCenter`/`UiToast`
for non-blocking feedback.

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
UiTemplateLayout editorLayout = (slots, options) ->
    Ui.scaffold(Ui.split(
            slots.navigation() == null ? Ui.panel() : slots.navigation(),
            slots.content()))
        .header(slots.header())
        .footer(slots.footer())
        .regionGap(options.regionGap() >= 0 ? options.regionGap() : 12);

UiTemplate page = UiTemplate.template()
    .layout(editorLayout)
    .header(UiText.literal("Reusable editor"))
    .content(editorContent())
    .build();
```

Keep the layout generic. A TotemDoll-specific import button belongs in TotemDoll,
not in RCUI's built-in template.

![Template structures](../imags/template-structures.png)
<!-- TODO: Add screenshots for top navigation, sidebar and editor templates. -->

## Demo

The Navigation page shows a small standard shell. The Templates page shows the
sidebar, editor, fixed footer, independent scroll and Dialog overlay together.
