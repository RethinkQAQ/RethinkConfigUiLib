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

## 工具和编辑器模板

编辑器通常需要工具栏、主区域、状态提示和弹窗：

```text
Header
Content
├── Toolbar
├── Main editor area
└── Status/feedback area
Footer
Dialog
```

```java
UiDialogHost dialogs = Ui.dialogHost();

Ui.Node editor = Ui.column()
    .gap(8)
    .add(Ui.row()
        .add(Ui.button(UiText.literal("Import"), this::importData))
        .add(Ui.button(UiText.literal("Export"), this::exportData)))
    .add(Ui.split(
        Ui.panel().padding(12).add(Ui.label(UiText.literal("Editor"))),
        Ui.preview((renderer, bounds, clip, theme) -> {
            // Draw a model or image inside bounds and clip.
        }).preferredHeight(160)))
    .add(Ui.alert(UiFeedbackType.INFO, UiText.literal("Ready")));

UiScaffold page = Ui.scaffold(editor)
    .header(Ui.textHeader(UiText.literal("Editor")))
    .footer(Ui.row().add(Ui.button(UiText.literal("Open"),
        () -> dialogs.show(dialogContent()))));

Ui.Node root = dialogs.root(page);
```

需要叠加层时使用 `UiStack`；需要响应式左右面板时使用 `UiSplitLayout`；
需要有边界的绘制时使用 `UiPreview`；非阻塞反馈使用
`UiNotificationCenter`/`UiToast`。

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
UiTemplateLayout editorLayout = (slots, options) ->
    Ui.scaffold(Ui.split(
            slots.navigation() == null ? Ui.panel() : slots.navigation(),
            slots.content()))
        .header(slots.header())
        .footer(slots.footer())
        .regionGap(options.regionGap() >= 0 ? options.regionGap() : 12);

UiTemplate page = UiTemplate.template()
    .layout(editorLayout)
    .header(UiText.literal("Reusable editor"))
    .content(editorContent())
    .build();
```

模板必须保持通用。TotemDoll 专用的导入按钮应该留在 TotemDoll，而不是加入
RCUI 的内置模板。

![页面模板结构](../imags/template-structures.png)
<!-- TODO: Add screenshots for top navigation, sidebar and editor templates. -->

## Demo 对应页面

Navigation 页面展示小型标准外壳；Templates 页面同时展示侧边栏、编辑器、
固定 Footer、独立滚动和 Dialog 覆盖层。
