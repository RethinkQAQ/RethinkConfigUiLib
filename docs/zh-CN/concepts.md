# 基本概念

使用 RCUI 不需要先理解所有内部类，只需要先理解页面如何组成，以及每种
职责应该由谁负责。

## 四种职责

```text
Theme     = 视觉系统
Template  = 页面结构
Component = 一个功能单元
Host      = 业务数据和交互
```

例如，音量滑块是 Component；它的颜色来自 Theme；它的位置由 Template 和
父布局决定；真正的音量数值属于 Host。

对于文字，`UiText` 是内容，`UiTextStyle` 是视觉意图。样式可以在多个节点之间
复用，同时不会把业务数据塞进 UI 层。

不要把保存配置写进可复用的按钮，也不要让主题读取 Minecraft 世界状态。
职责分开后，同一个组件才能同时用于设置页面和自定义工具页面。

## 节点组成树

`Ui.*` 下的工厂会返回 `Ui.Node`，或者返回用于创建节点的 Builder。节点会
互相嵌套：

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

父节点决定子节点的位置。子节点应该报告自己的期望尺寸，并在自己的 bounds
内绘制，而不是猜测整个屏幕的绝对坐标。

## 布局流程

RCUI 主要通过两个步骤布局：

```text
measure → layout
```

`measure` 回答“这个节点希望占多大空间”；`layout` 分配最终的 `UiBounds`
（x、y、width、height）。绘制和事件都使用最终 bounds：

```text
measure → layout → render
                   ↓
             event dispatch
```

所以自定义节点不能使用固定的屏幕坐标绘制，应该使用 render 回调收到的 bounds。

## 主轴和交叉轴

对 `UiRow` 来说，主轴是水平方向；对 `UiColumn` 来说，主轴是垂直方向。
交叉轴就是另一条方向。

```java
Ui.row()
    .mainAxisAlignment(UiMainAxisAlignment.SPACE_BETWEEN)
    .crossAxisAlignment(UiCrossAxisAlignment.CENTER);
```

主轴对齐有 `START`、`CENTER`、`END`、`SPACE_BETWEEN`。
交叉轴对齐有 `START`、`CENTER`、`END`、`STRETCH`。

`UiGrid.rowAlignment(...)` 只控制网格每一行内部的排列，不会替代外层
Row 或 Column 的对齐设置。

## 裁剪和覆盖层

普通绘制顺序是：

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

Content 可以滚动并裁剪子节点。Tooltip 和 Dialog 是覆盖层，必须显示在普通
Content 上方。不要把弹窗直接画进会裁剪的 Content，应该使用 `UiDialogHost`。

Preview 渲染器会收到最终 bounds 和有效 clip，只能在这两个区域的交集内绘制。

## 可见性和输入

`visible(false)` 的节点不会参与普通交互，不会响应 hover、点击、Tooltip 或
焦点。`enabled(false)` 会保留布局位置，但禁止操作并显示禁用样式。

可聚焦控件可以通过 `UiHost` 接收键盘事件。文本控件还需要文本输入和剪贴板
事件。宿主页面必须把这些回调转发给 `UiHost`，否则页面可能能显示，但按钮和
文本框无法正常工作。

## 生命周期

节点生命周期为：

```text
mount → unmount → dispose
```

`mount` 表示节点进入活动树；`unmount` 表示暂时离开；`dispose` 永久释放资源，
并且可以安全地重复调用。

动态纹理、模型预览、异步任务和临时数据应在释放阶段处理。不要在 `render`
中启动新的异步任务。

## GUI Scale 和密度

Minecraft 已经提供逻辑屏幕坐标，RCUI 不会再次对 Canvas 做整体变换。
`UiScalePolicy` 负责选择密度：

```java
UiScalePolicy.minecraft();
UiScalePolicy.adaptive();
UiScalePolicy.fixed(UiDensity.COMPACT);
```

默认映射为 GUI Scale 1–2 使用 `COMFORTABLE`，3–4 使用 `NORMAL`，5–8 使用
`COMPACT`。密度会改变控件高度、间距、Padding、文字层级和可用内容空间，
但不会改变鼠标坐标或 Scissor 坐标系。

![节点树和绘制层级](../imags/concepts-tree.png)
<!-- TODO: Add a diagram showing the node tree, clip region and dialog layer. -->

## Demo 对应页面

`Layout` 页面展示 measure/layout；`Feedback` 展示覆盖层顺序；`Custom` 展示
可见性、生命周期相关组合和自定义节点。
