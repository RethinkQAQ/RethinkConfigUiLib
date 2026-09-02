<div align="center">
  <img src="common/src/main/resources/assets/rethink_config_ui_lib/icon.png" width="160" alt="Rethink Config UI Lib icon">
  <h1>Rethink Config UI Lib</h1>
  <p>A lightweight, themed configuration UI library for Minecraft mods.</p>
</div>

Rethink Config UI Lib (RCUI) provides reusable UI components, layouts, themes,
configuration controls, scrolling, dialogs, tooltips, and screen integration
for Fabric, Forge, and NeoForge mods.

[![](https://jitpack.io/v/RethinkQAQ/RethinkConfigUiLib.svg)](https://jitpack.io/#RethinkQAQ/RethinkConfigUiLib)

## Dependency

RCUI is published through [JitPack](https://jitpack.io/#RethinkQAQ/rethink-config-ui-lib).
Replace `<minecraft-version>` and `<release-version>` with the versions you
need. Add JitPack after your normal repositories.

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}
```

Use the artifact matching your loader:

```kotlin
// Fabric
dependencies {
    include(modImplementation("com.github.RethinkQAQ:rethink-config-ui-lib-<minecraft-version>-fabric:v<release-version>"))
}

// Forge
dependencies {
    jarJar(implementation("com.github.RethinkQAQ:rethink-config-ui-lib-<minecraft-version>-forge:v<release-version>"))
}

// NeoForge
dependencies {
    jarJar(implementation("com.github.RethinkQAQ:rethink-config-ui-lib-<minecraft-version>-neoforge:v<release-version>"))
}
```

The dependency must be packaged as **Jar-in-Jar**. RCUI is a library and does
not publish a separate end-user mod, so the host mod is responsible for
embedding the platform artifact. Use one RCUI release consistently across all
embedded dependencies.

## Documentation

- [Project documentation](docs/README.md)
- [Online documentation](https://github.com/RethinkQAQ/rethink-config-ui-lib/wiki)
- [Releases](https://github.com/RethinkQAQ/rethink-config-ui-lib/releases)
- [Source code](https://github.com/RethinkQAQ/rethink-config-ui-lib)
