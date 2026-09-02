# 自定义组件

先选择能解决问题的最小扩展方式：

| 需求 | 使用 |
| --- | --- |
| 一次性绘图或简单点击区域 | Ui.custom() |
| 可复用状态或多个子节点 | UiComponent |
| Minecraft 模型或纹理 | UiPreview 加 Common 层渲染器 |

## 使用 Ui.custom() 制作一次性节点

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

Custom Builder 支持 preferred、minimum、maximum 尺寸，测量和绘制回调，以及
点击回调。节点还从 Ui.Node 继承可见性、enabled、focusable 和布局失效能力。
可以用 Ui.tooltip(...) 包装，也可以放入 Row、Grid 或 Stack。

render 回调收到的是最终 bounds。不要用 Minecraft 屏幕尺寸替代它。

## 使用 UiComponent 制作可复用组件

组件包含子节点或内部状态时使用 UiComponent：

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

child(...) 注册子节点所有权。组件应该测量子节点，在 layout 时给出最终 bounds，
并按需要的顺序绘制。如果之后增删子节点，调用 invalidateLayout()。

## 事件和焦点

Ui.Node 提供 click、scroll、drag、release、key 和 text-input 入口。只实现组件
真正需要的事件。包含文本框的组件必须通过宿主树转发键盘、文本和剪贴板输入。
不要让父节点和子节点重复处理同一个动作。

临时隐藏使用 visible(false)；显示但不可用使用 enabled(false)。隐藏节点不会
hover、点击、提供 Tooltip 或获取焦点。

## 生命周期和资源

```text
mount → unmount → dispose
```

动态纹理、预览和异步任务等宿主资源应在 dispose 中释放。dispose() 幂等，因此
清理代码必须能够安全重复调用。节点脱离树后不要继续向它发送事件。

## 裁剪和自定义绘制

自定义绘制必须留在节点 bounds 内。Preview 绘制还会收到有效 clip。不要手工
push Minecraft Scissor 后忘记 pop；使用成对的 Renderer clip 操作，并遵守
UiPreview 的契约。

## Demo 对应页面

Custom 页面包含一次性绘图节点、可复用 UiComponent、可见性切换、动态子节点和
禁用控件；Preview 页面展示带边界的预览版本。

![自定义组件](../imags/custom-component.png)
<!-- TODO: Add a screenshot of Ui.custom() beside a reusable UiComponent. -->
