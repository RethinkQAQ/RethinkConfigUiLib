# Quick Start

This page builds the smallest useful RCUI screen. Read it from top to bottom
the first time. Later, use [Components](components.md) as a reference.

## What you will build

The result is a page with a title, a short message, one switch, one slider and
a footer button:

```text
Header
Content
├── Section
│   ├── Label
│   ├── Toggle
│   └── Slider
└── Button
Footer
```

RCUI does not know your business meaning. Your mod owns the values and decides
what a click should do. RCUI supplies the nodes, layout, drawing and input.

## Add the dependency

RCUI is not planned to be published as a separate end-user runtime mod. The
recommended distribution form is to embed the matching platform artifact in
your own mod as Jar-in-Jar.

Add JitPack beside your normal repositories and replace the placeholders with
your exact Minecraft and RCUI release versions.

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}
```

```kotlin
// Fabric
dependencies {
    include(modImplementation(
        "com.github.RethinkQAQ:rethink-config-ui-lib-<minecraft-version>-fabric:v<release-version>"
    ))
}

// Forge
dependencies {
    jarJar(implementation(
        "com.github.RethinkQAQ:rethink-config-ui-lib-<minecraft-version>-forge:v<release-version>"
    ))
}

// NeoForge
dependencies {
    jarJar(implementation(
        "com.github.RethinkQAQ:rethink-config-ui-lib-<minecraft-version>-neoforge:v<release-version>"
    ))
}
```

Do not copy all three blocks into one project. Use the block for your Loader.
The artifact must match the Minecraft version exactly. RCUI is intended to be
shipped inside the host mod, so players should not be asked to install a
separate RCUI runtime mod.

## Create a first screen

The exact screen entry point depends on your Loader integration. The common
RCUI screen constructor receives a parent screen, a root node and a theme:

```java
Ui.Node root = Ui.column()
    .gap(8)
    .add(Ui.label(UiText.literal("Welcome to RCUI")))
    .add(Ui.button(UiText.literal("Done"), this::closeScreen));

minecraft.setScreen(new UiScreen(parent, root, UiTheme.roseLight()));
```

Important details:

- `Ui.column()` creates a vertical container.
- `.add(...)` places children in that container.
- `.gap(8)` adds space between children.
- `UiText.literal(...)` creates literal display text.
- The callback passed to `Ui.button(...)` runs when the button is activated.
- `UiScreen` connects the node tree to Minecraft rendering and input.

## Use the standard page template

Most configuration pages should start with `UiTemplate` instead of manually
placing every region:

```java
UiPageHost pages = Ui.pageHost()
    .addPage(UiText.literal("General"), Ui.section(UiText.literal("GENERAL"))
        .add(Ui.label(UiText.literal("General settings go here."))))
    .addPage(UiText.literal("Advanced"), Ui.section(UiText.literal("ADVANCED"))
        .add(Ui.label(UiText.literal("Advanced settings go here."))));

UiTemplate template = Ui.topNavigationTemplate()
    .header(UiText.literal("My Mod"))
    .navigation(pages.navigation())
    .content(pages)
    .footer(Ui.row()
        .mainAxisAlignment(UiMainAxisAlignment.END)
        .add(Ui.button(UiText.literal("Done"), this::closeScreen)))
    .build();

minecraft.setScreen(new UiScreen(parent, template, UiTheme.roseLight()));
```

The important rule is that `pages` is the Content node. The Footer is passed
to `.footer(...)`, so it stays fixed while the page content scrolls.

## Bind a value

RCUI controls do not magically find fields in your config object. Give a
control a `UiBinding`:

```java
AtomicBoolean enabled = new AtomicBoolean(true);

Ui.Node control = Ui.toggle(
    UiText.literal("Enable feature"),
    UiBinding.of(enabled::get, enabled::set)
);
```

The getter supplies the current value. The setter receives the new value. The
same pattern works for text, numbers and selections.

## A complete small page

```java
AtomicBoolean enabled = new AtomicBoolean(true);
AtomicReference<Double> volume = new AtomicReference<>(0.75);

Ui.Node content = Ui.column()
    .gap(12)
    .add(Ui.section(UiText.literal("GENERAL"))
        .add(Ui.settingRow(
            UiText.literal("Enable feature"),
            Ui.toggle(UiText.literal(""),
                UiBinding.of(enabled::get, enabled::set)))))
    .add(Ui.section(UiText.literal("AUDIO"))
        .add(Ui.slider(
            UiText.literal("Volume"),
            UiBinding.of(volume::get, volume::set),
            0.0, 1.0, 0.05)))
    .add(Ui.label(UiText.literal(
        "Changes are held by the host. Save them when your mod decides to."))
        .wrap(true));

UiTemplate template = Ui.topNavigationTemplate()
    .header(UiText.literal("Example Mod"))
    .content(content)
    .footer(Ui.row()
        .mainAxisAlignment(UiMainAxisAlignment.END)
        .add(Ui.button(UiText.literal("Save"), this::saveConfig))
        .add(Ui.button(UiText.literal("Cancel"), this::closeScreen)
            .variant(Ui.ButtonVariant.SECONDARY)))
    .build();
```

## What commonly goes wrong

- The page is blank: make sure the root node is passed to `UiScreen` or
  forwarded through `UiHost`.
- A control does not change data: check that its `UiBinding` setter writes to
  the actual value.
- The footer scrolls away: put it in `.footer(...)`, not inside Content.
- A class is missing at runtime: embed the platform artifact as Jar-in-Jar.
- The layout is unexpectedly narrow: test the logical viewport at another GUI
  Scale before adding fixed positions.

![Quick start screen](../imags/quick-start.png)
<!-- TODO: Add a screenshot of the completed quick-start page. -->

## Demo

The `General` page of the RCUI Demo combines the same ideas with settings,
validation, lists, tooltips and feedback. The `Content` and `Navigation`
pages show the smaller pieces separately.
