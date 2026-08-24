# Rethink Config UI Lib

Rethink Config UI Lib (RCUI) is a small LGPL-3.0-only client UI library for
Minecraft mods. It is designed for local Maven use and native Jar-in-Jar
packaging, not as a separately distributed end-user mod.

## Targets

The Stonecutter matrix builds Fabric, Forge and NeoForge for every configured
Minecraft version from `1.20.1` onward. NeoForge is intentionally absent from
the `1.20.1` node. `core` is Java 17 and deliberately has no Minecraft or
loader dependencies; all game API differences live in `common`.

## API

```java
var enabled = UiBinding.of(config::enabled, config::setEnabled);
var scale = UiBinding.of(config::scale, config::setScale);

Ui.Node page = Ui.scrollView(
    Ui.column().gap(10)
        .add(Ui.section(UiText.literal("General"))
            .add(Ui.toggle(UiText.literal("Enable feature"), enabled))
            .add(Ui.slider(UiText.literal("Scale"), scale, 0.5, 2.0, 0.1)))
        .add(Ui.button(UiText.literal("Done"), this::onClose))
);

minecraft.setScreen(new UiScreen(previousScreen, page, UiTheme.roseLight()));
```

`UiHost` exposes the same tree for embedding in an existing Screen: forward its
render, mouse, scroll and keyboard callbacks to the host. Bindings write their
new value immediately; persistence remains the responsibility of the host mod.

### YAML configuration

The optional `config` module uses `@RcuiConfig` and `@Setting` to keep the
configuration definition visible beside the model fields. The same module
contains the compile-time processor and the threaded YAML store; no runtime
reflection is used. A host normally adds the platform library as
`implementation` and the config artifact as `annotationProcessor`:

```kotlin
implementation("com.rethinkqaq.configui:rethink-config-ui-lib-fabric:<mc>-<version>")
annotationProcessor("com.rethinkqaq.configui:rethink-config-ui-lib-config:<version>")
```

```java
@RcuiConfig(id = "totemdoll", wrapperName = "TotemDollConfig")
public final class TotemDollConfigModel {
    @Setting(section = "general", title = "Enable Totem Doll")
    public boolean enabled = true;

    @Setting(section = "general", min = 25, max = 200, step = 25)
    public int scale = 100;

    @Setting(section = "appearance", title = "Style", codec = StyleIdCodec.class)
    public StyleId style = new StyleId("classic");
}

try (var config = TotemDollConfig.createAndLoad(configDirectory)) {
    var enabled = config.enabledBinding();
    var scale = config.scaleBinding();
    // Build the UI from these bindings. Done may call config.flush().
}
```

The generated wrapper loads `config/totemdoll.yaml` relative to the directory
passed by the host. Values are validated before write-back. Changes are
coalesced off the render thread, `flush()` and `close()` wait for pending
writes, and saves use a temporary file followed by an atomic replacement when
the filesystem supports it. Broken YAML and failed migrations are backed up
before defaults are used; unknown sections and fields remain in the file.

### Core source layout

The implementation is split by responsibility so layout work and control work
can evolve independently:

| Package | Owns | Depends on |
| --- | --- | --- |
| `core.layout` | flow, stacking, panels, sections and scrolling | `Ui.Node` / `Ui.Container` tree primitives |
| `core.component` | labels, buttons, toggles, sliders, selects and tooltips | tree primitives and renderer/theme contracts |
| `core` | tree primitives, renderer/theme contracts, bindings and compatibility facade | Java 17 only |

`Ui.*` remains a source-compatible facade for existing hosts. New components
should be added as a top-level class in the appropriate package, then exposed
through `Ui` only when a concise factory or compatibility alias is useful. Do
not put Minecraft or loader types in these packages; those belong in the
version-specific `common` adapter.

### GUI Scale policy

`UiScreen` uses Minecraft's logical GUI coordinate system directly. The renderer
does not apply a second inverse transform, so higher GUI scales naturally expose
a smaller logical viewport and responsive layouts can reflow around it:

```java
var screen = new UiScreen(previousScreen, page, UiTheme.roseLight());
minecraft.setScreen(screen);
```

`UiScalePolicy.minecraft()` is the only supported policy API in v1. `UiHost`
already uses this native coordinate contract directly; components should adapt
through measurement and layout (wrapping, stacking and scrolling), not by
applying a global UI scale.

## Build and local Maven

Build one target, then publish it locally:

```powershell
.\gradlew.bat :fabric:1.21.1:publishToMavenLocal
.\gradlew.bat :forge:1.21.1:publishToMavenLocal
.\gradlew.bat :neoforge:1.21.1:publishToMavenLocal
```

The artifacts use `com.rethinkqaq.configui` and one target-specific artifact
name: `rethink-config-ui-lib-fabric`, `-forge`, or `-neoforge`. Their version is
`<minecraft-version>-<library-version>`, for example `1.21.1-1.0.0`.

## Jar-in-Jar use

All of your host mods should depend on the same RCUI artifact version.

Fabric:

```kotlin
repositories { mavenLocal() }
dependencies {
    modImplementation("com.rethinkqaq.configui:rethink-config-ui-lib-fabric:1.21.1-1.0.0")
    include("com.rethinkqaq.configui:rethink-config-ui-lib-fabric:1.21.1-1.0.0")
}
```

Forge (with the template's JarJar plugin):

```kotlin
repositories { mavenLocal() }

dependencies {
    jarJar(implementation("com.rethinkqaq.configui:rethink-config-ui-lib-forge:1.21.1-1.0.0"))
}
```

NeoForge (ModDevGradle's native JarJar support):

```kotlin
repositories { mavenLocal() }

dependencies {
    jarJar(implementation("com.rethinkqaq.configui:rethink-config-ui-lib-neoforge:1.21.1-1.0.0"))
}
```

Use the artifact for the host's exact Minecraft version and pin every host to
the same RCUI version. The library is client-only and has empty loader
bootstraps: it registers no content, events, Mixins, or Screen replacements.

## Built-in demo

The platform JAR contains a visual smoke-test screen for development. Gradle
automatically passes this property to every `runClient` task:

```text
-Drethink_config_ui_lib_example=true
```

Fabric, Forge, and NeoForge therefore add an `RCUI Demo` button to the vanilla
title screen when launched from the development `runClient` task. The demo
opens only after that button is clicked. To disable it for a development run,
put `-Drethink_config_ui_lib_example=false` before the Gradle task name. The
platform JAR remains inert for normal mod use and does not replace any
Minecraft screen. The code also accepts the dotted property name for hosts
that pass JVM properties directly rather than through Gradle.

## Verification

```powershell
.\gradlew.bat :core:test
.\gradlew.bat validateVersionProperties
.\gradlew.bat build
```
