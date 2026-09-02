# 快速开始

本页会制作一个最小但有用的 RCUI 页面。第一次使用时建议从上到下阅读；
以后可以把[组件参考](components.md)当作查询手册。

## 你将制作什么

最终页面包含标题、说明文字、开关、滑块和底部按钮：

```text
Header
Content
├── Section
│   ├── Label
│   ├── Toggle
│   └── Slider
└── Button
Footer
```

RCUI 不知道你的业务含义。值由宿主 Mod 保存，按钮点击后要做什么也由宿主
决定。RCUI 负责节点、布局、绘制和输入事件。

## 添加依赖

RCUI 暂不计划作为独立的终端用户运行时 Mod 发布。推荐将匹配平台的
artifact 以 Jar-in-Jar 形式内嵌到你自己的 Mod 中。

在原有仓库旁加入 JitPack，并将占位符替换为准确的 Minecraft 和 RCUI 版本。

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}
```

```kotlin
// Fabric
dependencies {
    include(modImplementation(
        "com.github.RethinkQAQ:rethink-config-ui-lib-<minecraft-version>-fabric:v<release-version>"
    ))
}

// Forge
dependencies {
    jarJar(implementation(
        "com.github.RethinkQAQ:rethink-config-ui-lib-<minecraft-version>-forge:v<release-version>"
    ))
}

// NeoForge
dependencies {
    jarJar(implementation(
        "com.github.RethinkQAQ:rethink-config-ui-lib-<minecraft-version>-neoforge:v<release-version>"
    ))
}
```

不要把三段代码全部放进同一个项目，只使用你的 Loader 对应的那一段。
artifact 必须与 Minecraft 版本完全匹配。RCUI 计划作为宿主 Mod 内部的库发布，
因此不应要求玩家另外安装 RCUI 运行时 Mod。

## 创建第一个页面

具体的打开页面方式取决于 Loader 集成。Common 层的 RCUI Screen 接收父页面、
根节点和主题：

```java
Ui.Node root = Ui.column()
    .gap(8)
    .add(Ui.label(UiText.literal("Welcome to RCUI")))
    .add(Ui.button(UiText.literal("Done"), this::closeScreen));

minecraft.setScreen(new UiScreen(parent, root, UiTheme.roseLight()));
```

逐行理解：

- `Ui.column()` 创建一个垂直容器；
- `.add(...)` 把子节点加入容器；
- `.gap(8)` 设置子节点之间的间距；
- `UiText.literal(...)` 创建普通显示文字；
- `Ui.button(...)` 的回调在按钮被激活时执行；
- `UiScreen` 将节点树连接到 Minecraft 的绘制和输入系统。

## 使用标准页面模板

大多数配置页面应该从 `UiTemplate` 开始，而不是手工摆放每个区域：

```java
UiPageHost pages = Ui.pageHost()
    .addPage(UiText.literal("General"), Ui.section(UiText.literal("GENERAL"))
        .add(Ui.label(UiText.literal("General settings go here."))))
    .addPage(UiText.literal("Advanced"), Ui.section(UiText.literal("ADVANCED"))
        .add(Ui.label(UiText.literal("Advanced settings go here."))));

UiTemplate template = Ui.topNavigationTemplate()
    .header(UiText.literal("My Mod"))
    .navigation(pages.navigation())
    .content(pages)
    .footer(Ui.row()
        .mainAxisAlignment(UiMainAxisAlignment.END)
        .add(Ui.button(UiText.literal("Done"), this::closeScreen)))
    .build();

minecraft.setScreen(new UiScreen(parent, template, UiTheme.roseLight()));
```

这里 `pages` 是 Content 节点。Footer 单独传给 `.footer(...)`，因此页面内容
滚动时 Footer 仍然固定在底部。

## 绑定一个值

RCUI 控件不会自动寻找配置对象中的字段，需要显式传入 `UiBinding`：

```java
AtomicBoolean enabled = new AtomicBoolean(true);

Ui.Node control = Ui.toggle(
    UiText.literal("Enable feature"),
    UiBinding.of(enabled::get, enabled::set)
);
```

getter 提供当前值，setter 接收新值。文本、数字和选择控件都使用同样的思路。

## 一个完整的小页面

```java
AtomicBoolean enabled = new AtomicBoolean(true);
AtomicReference<Double> volume = new AtomicReference<>(0.75);

Ui.Node content = Ui.column()
    .gap(12)
    .add(Ui.section(UiText.literal("GENERAL"))
        .add(Ui.settingRow(
            UiText.literal("Enable feature"),
            Ui.toggle(UiText.literal(""),
                UiBinding.of(enabled::get, enabled::set)))))
    .add(Ui.section(UiText.literal("AUDIO"))
        .add(Ui.slider(
            UiText.literal("Volume"),
            UiBinding.of(volume::get, volume::set),
            0.0, 1.0, 0.05)))
    .add(Ui.label(UiText.literal(
        "Changes are held by the host. Save them when your mod decides to."))
        .wrap(true));

UiTemplate template = Ui.topNavigationTemplate()
    .header(UiText.literal("Example Mod"))
    .content(content)
    .footer(Ui.row()
        .mainAxisAlignment(UiMainAxisAlignment.END)
        .add(Ui.button(UiText.literal("Save"), this::saveConfig))
        .add(Ui.button(UiText.literal("Cancel"), this::closeScreen)
            .variant(Ui.ButtonVariant.SECONDARY)))
    .build();
```

## 最常见的问题

- 页面空白：确认根节点传给了 `UiScreen`，或通过 `UiHost` 转发了绘制和输入；
- 控件不改变数据：检查 `UiBinding` 的 setter 是否真的写入配置值；
- Footer 跟着滚动：把它放进 `.footer(...)`，不要放进 Content；
- 运行时缺少类：确认平台 artifact 已以 Jar-in-Jar 内嵌；
- 页面太窄或溢出：先在不同 GUI Scale 下测试逻辑视口，不要马上使用绝对坐标。

![快速开始页面](../imags/quick-start.png)
<!-- TODO: Add a screenshot of the completed quick-start page. -->

## Demo 对应页面

RCUI Demo 的 `General` 页面将这些内容组合成了带设置、验证、列表、Tooltip
和反馈的完整页面；`Content` 与 `Navigation` 页面分别展示更小的组成部分。
