# 页面模板和结构

模板就是页面外壳。它回答 Header、Navigation、Content、Footer 放在哪里，
但不应该知道你的配置业务是什么意思。

## 标准配置模板

大多数 Mod 设置页面都可以使用：

```text
Header
Navigation
Content: Section + SettingRow
Footer
```

```java
UiPageHost pages = Ui.pageHost()
    .addPage(UiText.literal("General"),
        Ui.section(UiText.literal("GENERAL"))
            .add(Ui.settingRow(UiText.literal("Enabled"),
                Ui.toggle(UiText.literal(""),
                    UiBinding.of(() -> true, value -> { })))));

UiTemplate page = UiTemplate.topNavigation()
    .header(UiText.literal("My Mod"))
    .navigation(pages.navigation())
    .content(pages)
    .footer(Ui.row()
        .mainAxisAlignment(UiMainAxisAlignment.END)
        .add(Ui.button(UiText.literal("Done"), this::closeScreen)))
    .build();
```

`UiPageHost` 提供导航并切换当前页面；`UiTemplate` 把它放入顶部导航外壳。
默认 Content 可以独立滚动，Footer 位于滚动区域之外。

## 页面根节点与滚动

`UiScaffold` 和 `UiTemplate` 是完整的页面根节点，不是内容卡片。它们负责
固定的 Header、Sidebar 或 Navigation、Content 视口和 Footer。把它们直接传给
`UiPageHost` 时，RCUI 会直接布局，而不会再额外添加 ScrollView。

```text
UiHost background
└── UiScaffold
    ├── 固定 Header / Sidebar
    ├── 裁剪且可滚动的 Content
    └── 固定 Footer
```

不要把 Scaffold 或 Template 放进 `UiScrollView`、`UiSection`，或会整体滚动的
Panel 中。RCUI 会拒绝包含页面根节点的 ScrollView，包括间接嵌套，因为这种结构
会让本应固定的区域随滚动离开，并产生不一致的背景或点击区域。

Host 负责正常的页面背景。Scaffold 默认透明；只有完整页面骨架需要独立表面时，
才显式设置背景：

```java
UiScaffold page = Ui.scaffold(pages)
    .background(UiBackground.opaque(0xFF202124))
    .sidebar(pages.navigation())
    .footer(footer);
```

普通内容分组请在 Scaffold 的 Content 内使用 `UiSection` 和 `UiPanel`，不要反过来
用它们包裹 Scaffold。

## 双栏配置模板

设置分类很多时使用侧边栏：

```text
Header
Content
├── Sidebar navigation
└── Scrollable content
Footer
```

```java
UiPageHost pages = Ui.pageHost()
    .addPage(UiText.literal("General"), generalContent())
    .addPage(UiText.literal("Advanced"), advancedContent());

UiScaffold page = Ui.scaffold(pages)
    .header(Ui.header(UiText.literal("My Mod")))
    .navigationMode(UiScaffold.NavigationMode.SIDEBAR)
    .sidebar(pages.navigation())
    .sidebarWidth(150)
    .footer(Ui.row()
        .mainAxisAlignment(UiMainAxisAlignment.END)
        .add(Ui.button(UiText.literal("Save"), this::saveConfig)));
```

侧边栏是独立区域。内容可能超过视口时，应使用 `UiPageHost` 或
`UiScrollView`。`sidebarWidth(...)` 是布局值，不是 Minecraft 画布缩放。

## Footer 规则

没有独立的 `UiFooter` 组件。Footer 就是任意一个 `Ui.Node`：

```java
Ui.Node footer = Ui.row()
    .mainAxisAlignment(UiMainAxisAlignment.END)
    .add(Ui.button(UiText.literal("Cancel"), this::closeScreen))
    .add(Ui.button(UiText.literal("Save"), this::saveConfig));

UiTemplate page = UiTemplate.topNavigation()
    .content(content)
    .footer(footer)
    .footerDivider(true)
    .build();
```

不要把 Footer 放进会滚动的 Content。使用 `footerAlignment(...)` 设置 Footer
在主轴上的对齐方式。

## 制作可复用模板

先组合现有节点。只有当同一种新页面外壳被多个页面或 Mod 稳定复用时，才实现
`UiTemplateLayout`：

```java
UiTemplateLayout sidebarLayout = (slots, options) ->
    Ui.scaffold(Ui.split(
            slots.navigation() == null ? Ui.panel() : slots.navigation(),
            slots.content()))
        .header(slots.header())
        .footer(slots.footer())
        .regionGap(options.regionGap() >= 0 ? options.regionGap() : 12);

UiTemplate page = UiTemplate.template()
    .layout(sidebarLayout)
    .header(UiText.literal("Reusable sidebar"))
    .content(editorContent())
    .build();
```

模板必须保持通用。TotemDoll 专用的导入按钮应该留在 TotemDoll，而不是加入
RCUI 的内置模板。

![页面模板结构](../imags/template-structures.png)
<!-- TODO: Add screenshots for top navigation and sidebar templates. -->

## Demo 对应页面

Navigation 页面展示小型标准外壳；Templates 页面展示顶部导航和侧边栏页面根节点，
从而演示固定区域和独立 Content 滚动，不会把完整页面嵌套进外层 ScrollView。
