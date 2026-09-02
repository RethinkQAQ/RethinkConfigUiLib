# Fully Custom UI

RCUI is not limited to configuration screens. Templates are optional. You can
build a dashboard, tool or preview surface directly from nodes.

## When to skip a template

Skip the standard template when the page needs a different composition, such
as a full-screen canvas, multi-panel tool or game-like interface. Keep using a
template when the page still has the normal Header/Navigation/Content/Footer
shape; customization inside Content is usually enough.

## Compose a custom root

```java
UiDialogHost dialogs = Ui.dialogHost();

Ui.Node root = Ui.stack()
    .add(Ui.panel().padding(12))
    .add(Ui.split(
        Ui.column()
            .add(Ui.label(UiText.literal("Tools")))
            .add(Ui.button(UiText.literal("Run"), this::runTool)),
        Ui.preview((renderer, bounds, clip, theme) -> {
            // Render a model, texture or chart here.
        }).preferredHeight(220))
        .gap(12));

Ui.Node withDialogLayer = dialogs.root(root);
```

UiStack gives layer order. UiSplitLayout gives responsive panes and stacks panes
vertically below its compact width. UiPreview provides final bounds and an
effective clip. UiDialogHost adds the modal layer above the root.

## Host-owned data

The host still owns configuration values, sorting, translations, models,
textures, persistence and asynchronous work. Pass data into bindings or custom
components; do not hide a global business service inside a generic node.

## Current boundaries

Stable building blocks include node composition, custom measurement and
rendering, events, focus, Tooltip, Dialog, Preview, scrolling, visibility and
lifecycle. Advanced rendering layers and larger animation systems should be
treated as Experimental until they are explicitly documented as stable.

## Checklist

- Decide which node owns each child.
- Measure from the available width and height.
- Render only inside bounds and effective clip.
- Forward keyboard, text, clipboard and mouse events through UiHost.
- Keep Dialog and Tooltip outside clipped Content.
- Dispose resources when the root is closed.

![Fully custom UI](../imags/custom-ui.png)
<!-- TODO: Add a screenshot of a non-configuration RCUI interface. -->

## Demo

The Custom page shows a small custom node tree. Templates and Preview show how
the same primitives can be used for larger custom pages.
