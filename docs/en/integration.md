# Integration

RCUI is distributed through JitPack as a platform artifact. RCUI is not planned
to be published as a separately installed runtime mod; embed the matching
artifact in the host mod as Jar-in-Jar.

Coordinates use this form:

```text
com.github.RethinkQAQ:
rethink-config-ui-lib-<minecraft-version>-<platform>:
v<release-version>
```

Use the Fabric, Forge or NeoForge dependency block from
[Quick Start](getting-started.md). Select the exact Minecraft version and the
Loader used by the host. Do not combine platform artifacts.

The module boundary is:

| Module | Responsibility |
| --- | --- |
| core | Nodes, layout, themes, events, configuration controls and templates |
| config | Configuration annotations, generated wrappers and YAML persistence |
| common | Minecraft Screen, renderer bridge, previews and version adapters |
| fabric / forge / neoforge | Loader metadata, packaging and platform integration |

Keep Minecraft, Fabric, Forge and NeoForge types out of core. Keep business
models, persistence policy, translations and domain actions in the host mod.

When reporting an integration issue, include the Minecraft version, Loader,
RCUI release, artifact coordinate and whether the host JAR contains the
embedded RCUI classes.
