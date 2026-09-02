# RCUI Developer Documentation

Rethink Config UI Lib (RCUI) is a Minecraft UI library for configuration
screens, previews, dashboards, tools, and other custom interfaces.

RCUI separates four responsibilities:

```text
Theme     = colours, spacing, radii and state visuals
Template  = the page shell and region structure
Component = one UI function
Host      = your data, persistence and Minecraft integration
```

## Learning path

1. [Quick Start](getting-started.md) — install RCUI and open a first screen.
2. [Concepts](concepts.md) — understand nodes, layout, clipping and lifecycle.
3. [Configuration](configuration.md) — bind controls to configuration values.
4. [Templates](templates.md) — build top-navigation and sidebar pages.
5. [Components](components.md) — find the right component and its styling API.
6. [Themes](themes.md) — use or create a visual theme.
7. [Custom Components](custom-components.md) — extend RCUI safely.
8. [Fully Custom UI](custom-ui.md) — use RCUI without a standard template.
9. [Integration](integration.md) and [Troubleshooting](troubleshooting.md).

Each tutorial contains a small example, a complete example, explanations, and
the matching page in the RCUI Demo. Code in this documentation is Java unless
the code block is explicitly labelled as Gradle Kotlin DSL.

Screenshots and diagrams are reserved in [`docs/imags`](../imags/).

See also: [Chinese documentation](../zh-CN/README.md).
