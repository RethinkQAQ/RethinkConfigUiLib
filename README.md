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

The platform JAR contains an opt-in visual smoke-test screen. Add this JVM
argument to the client run configuration:

```text
-Drethink_config_ui_lib_example=true
```

With the flag present, Fabric, Forge, and NeoForge add an `RCUI Demo` button to
the vanilla title screen. The demo opens only after that button is clicked.
Without the flag, the library remains inert and does not replace any Minecraft
screen. The demo is part of the platform JAR only as a development aid; it is
not shown by default and has no configuration persistence. The code also
accepts the dotted property name for hosts that pass JVM properties directly
rather than through Gradle.

## Verification

```powershell
.\gradlew.bat :core:test
.\gradlew.bat validateVersionProperties
.\gradlew.bat build
```
