# 组件参考

所有内置组件都是节点。使用 Ui.* 工厂创建组件，把它们放入布局节点，
然后让父节点负责测量和摆放。

每个组件都说明它显示什么、如何创建、如何参与布局和输入，以及哪些视觉选择
应该由主题负责。

## 基础内容组件

```java
Ui.Node body = Ui.column()
    .gap(8)
    .add(Ui.header(UiText.literal("Page title")))
    .add(Ui.label(UiText.literal("Body text can wrap when the available width is small."))
        .wrap(true))
    .add(Ui.section(UiText.literal("GENERAL"))
        .add(Ui.label(UiText.literal("Related content belongs in one section."))))
    .add(Ui.panel().padding(12)
        .add(Ui.label(UiText.literal("Panel surface"))));
```

- UiLabel 显示普通文本，长文本可以使用 wrap(true) 和行数限制；
- UiHeader 显示标题和可选副标题，样式为 CARD、COMPACT、TEXT 或 NONE；
- UiSection 在标题下组织内容；
- UiPanel 和 Ui.card() 提供背景表面和 Padding；
- UiDivider 分隔内容，不改变业务状态；
- UiBadge 显示短状态，支持 neutral、accent、success、warning、danger；
- UiNavigationBar 通常由 UiPageHost.navigation() 提供。

### 预览卡片

```java
UiPreviewCard card = Ui.previewCard(
        UiText.literal("A model"),
        Ui.preview((renderer, bounds, clip, theme) -> {
            // Draw only inside bounds and clip.
        }).preferredHeight(96))
    .description(UiText.literal("A bounded preview with an action area."))
    .action(Ui.button(UiText.literal("Select"), this::select))
    .onClick(this::select);
```

PreviewCard 组合标题、有限大小的预览区、说明文字、选中状态和可选操作。
适合模型、纹理和预设。预览节点本身不负责管理宿主的模型资源。

## 布局组件

```java
Ui.Node layout = Ui.column()
    .gap(12)
    .crossAxisAlignment(UiCrossAxisAlignment.STRETCH)
    .add(Ui.row()
        .gap(8)
        .mainAxisAlignment(UiMainAxisAlignment.SPACE_BETWEEN)
        .add(Ui.label(UiText.literal("Left")))
        .add(Ui.button(UiText.literal("Right"), this::action)))
    .add(Ui.grid()
        .minimumColumnWidth(140)
        .maximumColumnWidth(220)
        .gap(8)
        .rowAlignment(UiMainAxisAlignment.CENTER)
        .add(Ui.card())
        .add(Ui.card()))
    .add(Ui.split(leftNode(), rightNode())
        .primaryShare(.6f)
        .compactBelow(620));
```

| 组件 | 用途 | 重要设置 |
| --- | --- | --- |
| UiRow | 水平内容 | gap、主轴/交叉轴对齐、等宽子节点 |
| UiColumn | 垂直内容 | gap、主轴/交叉轴对齐 |
| UiStack | 叠加图层 | 子节点顺序 |
| UiGrid | 响应式卡片 | 最小/最大列宽、gap、rowAlignment |
| UiSplitLayout | 双栏区域 | primaryShare、gap、compactBelow |
| UiScrollView | 独立滚动区域 | 内容节点和滚动输入 |
| UiPageHost | 页面和导航 | addPage、select、push、pop |
| UiScaffold | 页面框架 | 导航模式、侧边栏宽度、Footer |

主轴对齐为 START、CENTER、END、SPACE_BETWEEN；交叉轴对齐为 START、
CENTER、END、STRETCH。Grid 的 rowAlignment 只调整每一行内部的排列。

页面已经由 UiPageHost 管理滚动时，不要随意再包一层 ScrollView，避免嵌套滚动。

## 按钮

```java
Ui.Node buttons = Ui.row()
    .gap(8)
    .add(Ui.button(UiText.literal("Primary"), this::primary))
    .add(Ui.button(UiText.literal("Secondary"), this::secondary)
        .variant(Ui.ButtonVariant.SECONDARY))
    .add(Ui.button(UiText.literal("Outline"), this::outline)
        .variant(Ui.ButtonVariant.OUTLINE))
    .add(Ui.button(UiText.literal("Delete"), this::delete)
        .variant(Ui.ButtonVariant.DANGER))
    .add(Ui.iconButton(UiText.literal("+"), this::add));
```

按钮支持点击、键盘激活、焦点、Tooltip、启用/禁用状态和自适应文本。
normal、hover、pressed、focused、disabled、selected 的颜色由主题提供；
业务操作仍然应该放在宿主中。

## 开关、选择和数字控件

```java
AtomicBoolean enabled = new AtomicBoolean(false);
AtomicReference<String> mode = new AtomicReference<>("Balanced");
AtomicReference<Double> value = new AtomicReference<>(.5);

Ui.Node controls = Ui.column()
    .gap(8)
    .add(Ui.toggle(UiText.literal("Enabled"),
        UiBinding.of(enabled::get, enabled::set)))
    .add(Ui.select(UiText.literal("Mode"),
        UiBinding.of(mode::get, mode::set),
        List.of("Fast", "Balanced", "Quality"), UiText::literal))
    .add(Ui.slider(UiText.literal("Value"),
        UiBinding.of(value::get, value::set), 0, 1, .1));
```

UiToggle 修改布尔值；UiSelect 显示选项列表并支持自定义选项文本；
UiSlider 支持范围、步进、拖动和键盘调整。多个数字控件需要共用约束时，
使用带 UiSetting 和 UiNumberSpec 的 UiNumberControl 或 UiNumericField。

## 文本和表单

```java
AtomicReference<String> query = new AtomicReference<>("");

Ui.Node form = Ui.column()
    .gap(8)
    .add(Ui.formField(UiText.literal("Name"),
        Ui.textField(UiBinding.of(query::get, query::set))
            .placeholder(UiText.literal("Type here"))))
    .add(Ui.searchField(UiBinding.of(query::get, query::set)));
```

文本框支持编辑、光标、焦点、Enter 提交、Esc 取消、文本输入和剪贴板。
SearchField 使用相同的绑定模式进行筛选；FormField 在控件旁添加标签和说明。

## 数据组件

UiSelectionList 适合选择列表；UiCollectionEditor 适合添加、编辑和删除条目的集合：

```java
Ui.Node choices = Ui.selectionList(
    () -> List.of("One", "Two", "Three"),
    UiBinding.of(selected::get, selected::set),
    UiText::literal);

UiListEntryAdapter<String> adapter = UiListEntryAdapter.builder(
    () -> "", UiText::literal,
    value -> Ui.textField(value).placeholder(UiText.literal("Value")))
    .build();
```

Adapter 决定单个条目如何显示和编辑。集合由宿主管理，保存前也应由宿主验证。

## 反馈、Tooltip 和 Dialog

```java
Ui.Node feedback = Ui.column()
    .gap(8)
    .add(Ui.alert(UiFeedbackType.INFO, UiText.literal("Information")))
    .add(Ui.alert(UiFeedbackType.SUCCESS, UiText.literal("Saved")))
    .add(Ui.alert(UiFeedbackType.WARNING, UiText.literal("Check this")))
    .add(Ui.alert(UiFeedbackType.ERROR, UiText.literal("Invalid value")))
    .add(Ui.tooltip(
        Ui.button(UiText.literal("Hover me"), this::action),
        UiText.literal("This tooltip is above normal content.")));
```

UiTooltip 可以接收普通文本或节点形式的 UiTooltipContent；UiAlert 支持 info、
success、warning、error；UiToast 通过宿主的 UiNotificationCenter 显示；
UiDialogHost 管理弹窗并把输入传给当前 Dialog。

## 样式由谁负责

组件从 UiTheme 读取语义 Token：

- palette：表面、控件、重点色、文字、边框和反馈颜色；
- metrics：间距、Padding、圆角、边框宽度和控件高度；
- state visuals：hover、pressed、focus、disabled 和遮罩强度；
- motion：hover 和 focus 的过渡。

很多组件都要改变时修改 Theme；只有一个实例不同时使用组件选项。
组件不要直接修改 Minecraft 的渲染状态。

![组件总览](../imags/component-catalogue.png)
<!-- TODO: Add a screenshot grid covering every component category. -->

## Demo 对应页面

Content 覆盖基础内容；Layout 覆盖流式布局、Grid、Split 和滚动；
Input、Data、Feedback 覆盖交互组件；Preview 覆盖预览卡片和裁剪。
