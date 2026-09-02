# Custom Components

Choose the smallest extension that fits the problem:

| Need | Use |
| --- | --- |
| One drawing or a small click target | Ui.custom() |
| Reusable state or several children | UiComponent |
| Minecraft model or texture | UiPreview plus a common-layer renderer |

## One-off node with Ui.custom()

```java
Ui.Node marker = Ui.custom()
    .preferredWidth(160)
    .preferredHeight(36)
    .render((renderer, bounds, theme) -> {
        renderer.fillRoundRect(bounds,
            theme.metrics().controlRadius(),
            theme.palette().accent());
        renderer.drawCenteredText(UiText.literal("Custom"),
            bounds.x() + bounds.width() / 2,
            bounds.y() + 10,
            theme.palette().onAccent());
    })
    .click((x, y, button) -> button == 0)
    .build();
```

The custom builder supports preferred, minimum and maximum dimensions,
measurement and rendering callbacks, plus a click callback. The node also
inherits visibility, enabled, focusable state and layout invalidation from
Ui.Node. Wrap it with Ui.tooltip(...) or place it in Row, Grid or Stack.

The render callback receives the final bounds. Never use the Minecraft screen
size as a substitute for those bounds.

## A reusable UiComponent

Use UiComponent when the node owns children or internal state:

```java
public final class StatusCard extends UiComponent {
    private final Ui.Column content = Ui.column()
        .gap(4)
        .add(Ui.badge(UiText.literal("READY")))
        .add(Ui.label(UiText.literal("Reusable component"))
            .wrap(true));

    public StatusCard() {
        child(content);
    }

    @Override
    protected void measureSelf(UiRenderer renderer, float maxWidth,
                               float maxHeight, UiTheme theme) {
        content.measure(renderer, maxWidth, maxHeight, theme);
        measuredWidth = content.measuredWidth();
        measuredHeight = content.measuredHeight();
    }

    @Override
    public void layout(UiRenderer renderer, UiBounds bounds, UiTheme theme) {
        super.layout(renderer, bounds, theme);
        content.layout(renderer, bounds, theme);
    }

    @Override
    public void render(UiRenderer renderer, UiTheme theme) {
        content.render(renderer, theme);
    }
}
```

child(...) registers ownership. A component should measure its children, give
them final bounds during layout, and render them in the intended order. If a
child is added or removed later, call invalidateLayout().

## Events and focus

Ui.Node exposes click, scroll, drag, release, key and text-input entry points.
Use the event that the component really needs. A component with a text field
should forward keyboard, text and clipboard input through the host tree. Do
not make a parent and child both perform the same action.

Use visible(false) for temporary absence and enabled(false) for a visible but
unavailable control. Hidden nodes do not hover, click, provide Tooltip or take
focus.

## Lifecycle and resources

```text
mount → unmount → dispose
```

Dispose host-owned dynamic textures, previews and asynchronous work. dispose()
is idempotent, so cleanup code must tolerate repeated calls. Do not keep a
reference to a detached child and continue sending it events.

## Clipping and custom drawing

Custom drawing must stay inside the node bounds. Preview drawing additionally
receives an effective clip. Do not push a Minecraft Scissor and forget to pop
it; use the renderer's paired clip operations and let UiPreview manage its
contract.

## Demo

The Custom page contains a one-off drawing node, a reusable UiComponent,
visibility toggling, a dynamic child and disabled controls. The Preview page
shows the bounded preview version.

![Custom components](../imags/custom-component.png)
<!-- TODO: Add a screenshot of Ui.custom() beside a reusable UiComponent. -->
