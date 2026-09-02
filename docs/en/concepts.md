# Concepts

You do not need to understand every internal class to use RCUI. You only need
to understand how a page is assembled and who owns each responsibility.

## The four responsibilities

```text
Theme     = visual system
Template  = page structure
Component = one functional unit
Host      = business data and interaction
```

For example, a volume slider is a Component. Its colour is provided by the
Theme. Its position comes from a Template and layout parent. The actual volume
value belongs to the Host.

Do not put configuration saving inside a reusable button. Do not make a theme
read a Minecraft world. Keeping these responsibilities separate lets the same
component work in a settings page and an editor.

## Nodes form a tree

Every factory under `Ui.*` returns a `Ui.Node` or a builder that creates one.
Nodes are nested:

```text
UiHost
└── UiTemplate
    ├── Header
    ├── Navigation
    ├── Content
    │   └── UiColumn
    │       ├── UiSection
    │       │   └── UiToggle
    │       └── UiPreviewCard
    └── Footer
```

The parent decides where a child is laid out. A child should describe its
preferred size and draw inside its own bounds instead of guessing a screen
coordinate.

## The layout pass

RCUI lays out nodes in two main steps:

```text
measure → layout
```

`measure` answers “how much space would this node like?” `layout` assigns the
final `UiBounds` (x, y, width and height). Rendering and input then use those
final bounds:

```text
measure → layout → render
                   ↓
             event dispatch
```

This is why a custom node must not draw using a hard-coded screen position.
Use the bounds passed to its render callback.

## Main axis and cross axis

For `UiRow`, the main axis is horizontal. For `UiColumn`, it is vertical.
The cross axis is the other direction.

```java
Ui.row()
    .mainAxisAlignment(UiMainAxisAlignment.SPACE_BETWEEN)
    .crossAxisAlignment(UiCrossAxisAlignment.CENTER);
```

Main-axis values are `START`, `CENTER`, `END` and `SPACE_BETWEEN`.
Cross-axis values are `START`, `CENTER`, `END` and `STRETCH`.

`UiGrid.rowAlignment(...)` controls the children inside each grid row. It does
not replace the row/column alignment settings of a surrounding layout.

## Clipping and overlays

The normal rendering order is:

```text
background
→ Header
→ Navigation
→ Content
→ Footer
→ Tooltip
→ Dialog overlay
→ Dialog content
```

Content can scroll and clip its children. Tooltip and Dialog are overlay
surfaces; they must remain visible above the normal content. Use
`UiDialogHost` instead of drawing a modal yourself inside a clipped Content
node.

Preview renderers receive both their final bounds and an effective clip. Draw
only inside the intersection of those regions.

## Visibility and input

A node with `visible(false)` does not participate in normal interaction. It
does not receive hover, clicks, Tooltip lookup or focus. `enabled(false)` keeps
the node in the layout but prevents its action and displays the disabled visual.

Focusable controls can receive keyboard input through `UiHost`. Text controls
also receive text and clipboard events. A host screen must forward the input
callbacks to the `UiHost`; otherwise the page may render correctly while
buttons or text fields appear broken.

## Lifecycle

The node lifecycle is:

```text
mount → unmount → dispose
```

`mount` means the node entered an active tree. `unmount` means it temporarily
left that tree. `dispose` permanently releases resources and is idempotent.

Release host-owned dynamic textures, model previews, asynchronous tasks and
temporary data during disposal. Do not start a new asynchronous task from
`render`.

## GUI Scale and density

Minecraft already supplies logical screen coordinates. RCUI does not apply a
second canvas transform. `UiScalePolicy` chooses density tokens:

```java
UiScalePolicy.minecraft();
UiScalePolicy.adaptive();
UiScalePolicy.fixed(UiDensity.COMPACT);
```

The default mapping is GUI Scale 1–2 to `COMFORTABLE`, 3–4 to `NORMAL`, and
5–8 to `COMPACT`. Density changes control heights, spacing, padding, text
levels and available content space; it does not change mouse or Scissor
coordinates.

![Node tree and rendering layers](../imags/concepts-tree.png)
<!-- TODO: Add a diagram showing the node tree, clip region and dialog layer. -->

## Demo

The `Layout` page demonstrates measure/layout concepts. `Feedback` demonstrates
the overlay order, and `Custom` demonstrates visibility, lifecycle-oriented
composition and custom nodes.
