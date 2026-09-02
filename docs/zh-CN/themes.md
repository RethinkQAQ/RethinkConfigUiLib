# 主题

主题是整个 UI 树共享的视觉系统。它改变组件外观，但不改变业务行为或布局契约。

## 内置主题

```java
UiTheme light = UiTheme.roseLight();
UiTheme dark = UiTheme.roseDark();
```

亮色主题使用黑、白、灰和玫红重点色；暗色主题在更暗的表面上保持相同的语义含义。

## 从已有主题派生

推荐从已有主题派生：

```java
UiTheme darkBlue = UiTheme.roseDark()
    .withPalette(UiTheme.UiPalette.builder()
        .accent(0xFF60A5FA)
        .accentHover(0xFF93C5FD)
        .accentPressed(0xFF3B82F6)
        .focusRing(0xFF93C5FD)
        .build())
    .withAccent(0xFF60A5FA);
```

withPalette 修改语义颜色；withMetrics 修改尺寸；withMotion 修改过渡；
withStates 修改状态强度。只改变重点色时可以使用 withAccent。

## 排版与文本样式

主题提供默认文字颜色、密度度量和组件文字层级。当某个节点需要不同的语义、
大小或明确颜色时，使用 `UiTextStyle`：

```java
UiTextStyle note = UiTextStyle.secondary().scale(.9f).color(0xFF9CA3AF);
Ui.Node label = Ui.label(UiText.literal("可选设置")).textStyle(note);
```

不设置颜色时，文本样式会回退到组件或主题颜色。文本样式不会修改背景、内边距
或业务行为。

## 从头创建主题

UiTheme.custom 使用默认的 motion 和 state visuals：

```java
UiTheme blue = UiTheme.custom(
    UiTheme.UiPalette.builder()
        .background(0xFF111827)
        .surfaceRaised(0xFF1F2937)
        .control(0xFF374151)
        .controlHover(0xFF4B5563)
        .controlPressed(0xFF6B7280)
        .controlDisabled(0xFF374151)
        .accent(0xFF60A5FA)
        .accentHover(0xFF93C5FD)
        .accentPressed(0xFF3B82F6)
        .onAccent(0xFF0F172A)
        .textPrimary(0xFFF9FAFB)
        .textSecondary(0xFFD1D5DB)
        .textDisabled(0xFF9CA3AF)
        .border(0xFF4B5563)
        .focusRing(0xFF93C5FD)
        .build(),
    UiTheme.UiMetrics.comfortable()
);
```

只有需要同时修改动效和状态视觉时才使用完整 Builder：

```java
UiTheme complete = UiTheme.builder()
    .palette(palette)
    .metrics(metrics)
    .motion(UiTheme.UiMotion.defaults())
    .states(UiTheme.UiStateVisuals.defaults())
    .build();
```

Palette 是语义颜色：表面、控件、重点色、文字、边框、success、warning 和 danger。
主题不包含业务逻辑，也不能直接修改 Minecraft 的渲染状态。

## 背景和颜色工具

```java
UiBackground.opaque(0xFF202124);
UiBackground.translucent(0xFF202124, 0.78f);
UiBackground.transparent();

UiColor.withOpacity(0xFFFFFF, 0.5f);
UiColor.withAlpha(0x80FFFFFF, 0.5f);
UiColor.mix(base, accent, 0.5f);
```

透明度和混合强度使用 0 到 1。

## 密度

```java
UiScalePolicy.minecraft();
UiScalePolicy.adaptive();
UiScalePolicy.fixed(UiDensity.COMPACT);
```

GUI Scale 1–2 对应 COMFORTABLE，3–4 对应 NORMAL，5–8 对应 COMPACT。
密度会改变控件高度、间距、Padding、文字层级和卡片间距，但不会缩放 Canvas，
也不会改变鼠标和 Scissor 坐标。

## 验证清单

- 检查 normal、hover、pressed、focused、disabled、selected；
- 检查 error、warning、success 的对比度；
- 检查 Button、TextField、Tooltip、Dialog、Footer、PreviewCard；
- 检查窄和宽逻辑视口；
- 保持布局尺寸和业务行为独立于主题。

Header 样式属于页面结构而不是主题。当前 UiHeaderStyle 只有 CARD、COMPACT、
TEXT、NONE，没有 MODERN。

![主题状态](../imags/theme-states.png)
<!-- TODO: Add a screenshot showing every interactive state and overlay. -->

## Demo 对应页面

Themes 页面展示 roseLight、roseDark、Demo 蓝色主题和密度说明；Input、Feedback
页面用于检查交互状态和覆盖层。
