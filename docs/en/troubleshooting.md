# Troubleshooting

## The screen is blank

Check that the root node or UiTemplate was passed to UiScreen, or that the
host forwards render and input callbacks to UiHost. A node created with Ui.*
does not appear until it is attached to an active tree.

## A control does not change the value

Check the UiBinding getter and setter. For generated configuration wrappers,
use the generated fieldBinding method. For local values, make sure the setter
updates the object that the rest of your mod reads.

## The footer moves or disappears

Pass a Ui.Node to UiTemplate.footer(...) or UiScaffold.footer(...). Do not put
it inside a scrolling Content node.

## Text input or focus does not work

Forward keyboard, text-input, clipboard, mouse and focus callbacks through
UiHost. Check that the field is visible, enabled and focusable. Enter submits
when the control accepts a submission; Escape can cancel editing.

## Content clips a Dialog or Tooltip

Put modal content below UiDialogHost and wrap the complete root with
dialogs.root(...). Use Ui.tooltip(...) for tooltips. Do not draw overlays
inside a clipped Content node.

## A preview draws outside its card

Use UiPreview or MinecraftPreview and respect the bounds and effective clip
passed to the renderer. Restore paired renderer clip state.

## High GUI Scale breaks the layout

Do not apply another canvas transform. Use UiScalePolicy.minecraft() or a
fixed UiDensity. Test narrow and wide logical viewports and let Row, Grid and
SplitLayout respond to available space.

## Resources remain after closing

Release textures, models, asynchronous work and temporary data in dispose().
Keep disposal idempotent and stop sending events to detached nodes.

## Runtime classes are missing

Confirm the artifact matches both Minecraft version and Loader, and that the
host JAR contains the platform artifact as Jar-in-Jar. RCUI is not expected to
be installed separately by the player.

## Debug checklist

1. Run the RCUI Demo.
2. Try GUI Scale values that produce comfortable, normal and compact density.
3. Test hover, pressed, focused, disabled and selected states.
4. Test Tooltip, Dialog, Footer, TextField, Preview and independent scrolling.
5. Compare the host code with the matching documentation example.
