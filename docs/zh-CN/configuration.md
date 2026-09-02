# 定义配置

RCUI 有两个相关但分开的部分：

1. 可选的 `config` 模块负责描述、加载和保存值；
2. `core` 模块负责用 UI 组件显示这些值。

处理器不会自动决定页面布局。你仍然要选择每个值属于哪个分组，以及使用什么控件。

## 定义配置模型

模型必须是 public、非抽象，并且有 public 无参构造方法。用 `@RcuiConfig`
标记类，用 `@Setting` 标记需要保存的字段：

```java
@RcuiConfig(id = "example", file = "example.yaml",
    wrapperName = "ExampleConfig", schemaVersion = 1)
public final class ExampleConfigModel {
    @Setting(section = "general", key = "enabled",
        title = "Enable feature",
        description = "Turns the feature on or off.")
    public boolean enabled = true;

    @Setting(section = "display", key = "scale",
        title = "Interface scale", min = 0.5, max = 2.0, step = 0.1)
    public double scale = 1.0;
}
```

`id` 是配置标识；`file` 是相对 YAML 文件名；`wrapperName` 是生成的包装类；
`section` 和 `key` 描述保存路径；`title` 与 `description` 是 UI 元数据；
`min`、`max`、`step` 描述数字约束；`schemaVersion` 支持配置迁移。

只有标记了 `@Setting` 的字段会被保存。字段必须是 public、非 static、非 final。
不要把 Minecraft 对象放进配置模型。

## 加载生成的包装类

处理器会在模型所在的包中生成 `ExampleConfig`：

```java
try (ExampleConfig config = ExampleConfig.createAndLoad(configDirectory)) {
    boolean current = config.model().enabled;
    config.enabled().set(!current);
    config.flush();
}
```

包装类提供 `createAndLoad`、`save`、`flush`、`reload`、`close`，并为每个字段
生成绑定方法：

```java
config.enabledBinding();
config.scaleBinding();
```

getter 读取当前值，setter 修改模型。什么时候保存仍由宿主决定，可以每次提交时保存，
也可以提供 Save 按钮，在用户确认后调用 `flush()`。

## 使用绑定创建控件

布尔值使用 `UiToggle`：

```java
Ui.Node enabledControl = Ui.toggle(
    UiText.literal(""),
    config.enabledBinding()
);
```

数字控件可以共用一组 `UiSetting` 和 `UiNumberSpec`：

```java
UiSetting<Double> scale = UiSetting.of(config.scaleBinding(), 1.0)
    .describedBy(UiText.literal("Controls interface size."));

UiNumberSpec<Double> scaleSpec = UiNumberSpec.builder(UiNumberSpec.DOUBLE)
    .range(0.5, 2.0)
    .step(0.1)
    .formatter(value -> String.format(java.util.Locale.ROOT, "%.1f", value))
    .build();

Ui.Node scaleControl = Ui.numberControl(scale, scaleSpec);
Ui.Node scaleField = Ui.numericField(scale, scaleSpec);
```

Setting 保存绑定和默认值；NumberSpec 保存类型、范围、步长和显示格式。复用它们，
可以让所有数字控件保持一致。

## 验证文本

文本框可以在写回值之前拒绝非法值：

```java
AtomicReference<String> profile = new AtomicReference<>("Rethink");

Ui.Node field = Ui.textField(
    UiBinding.of(profile::get, profile::set)
).placeholder(UiText.literal("Profile name"))
 .validator(value -> value.trim().isEmpty()
     ? UiValidationResult.error(UiText.literal("A name is required"))
     : UiValidationResult.OK);

Ui.Node row = Ui.formField(UiText.literal("Profile name"), field);
```

验证属于 UI 交互，但宿主决定非法值是否可以保存，以及如何通知用户。

## 完整页面

```java
Ui.Node content = Ui.column()
    .gap(12)
    .add(Ui.section(UiText.literal("GENERAL"))
        .add(Ui.settingRow(UiText.literal("Enable feature"),
            Ui.toggle(UiText.literal(""), config.enabledBinding()))))
    .add(Ui.section(UiText.literal("DISPLAY"))
        .add(Ui.formField(UiText.literal("Interface scale"),
            Ui.numberControl(scale, scaleSpec))));

UiTemplate page = Ui.topNavigationTemplate()
    .header(UiText.literal("Example Mod"))
    .content(content)
    .footer(Ui.row()
        .mainAxisAlignment(UiMainAxisAlignment.END)
        .add(Ui.button(UiText.literal("Save"), () -> {
            try {
                config.flush();
            } catch (java.io.IOException exception) {
                // Notify the user through the host.
            }
        })))
    .build();
```

页面仍可编辑时，生成的包装类必须保持存活。不要创建节点树后立即关闭它，应在宿主
Screen dispose 时关闭，或交由宿主的配置服务管理。

![配置页面](../imags/configuration-page.png)
<!-- TODO: Add a screenshot showing bindings, numeric constraints and an error. -->

## Demo 对应页面

`General` 页面展示布尔、数字和选择绑定；`Input` 页面展示本地绑定；`Data`
页面展示集合编辑和验证。
