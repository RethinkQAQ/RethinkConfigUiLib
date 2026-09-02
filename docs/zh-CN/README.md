# RCUI 开发者文档

Rethink Config UI Lib（RCUI）是一个面向 Minecraft Mod 的 UI 库，可以制作
配置页面、预览界面、工具界面和普通的自定义 UI。

RCUI 将页面拆成四种职责：

```text
Theme     = 颜色、间距、圆角和状态视觉
Template  = 页面外壳和区域结构
Component = 一个具体的 UI 功能
Host      = 业务数据、持久化和 Minecraft 集成
```

## 推荐阅读顺序

1. [快速开始](getting-started.md) —— 添加依赖并打开第一个页面。
2. [基本概念](concepts.md) —— 理解节点、布局、裁剪和生命周期。
3. [定义配置](configuration.md) —— 将控件绑定到配置值。
4. [页面模板](templates.md) —— 制作顶部导航和侧边栏页面。
5. [组件参考](components.md) —— 查找组件和可调整的样式。
6. [主题](themes.md) —— 使用或制作自己的视觉主题。
7. [自定义组件](custom-components.md) —— 安全地扩展 RCUI。
8. [完全自定义 UI](custom-ui.md) —— 不使用标准模板制作界面。
9. [集成](integration.md) 与 [问题排查](troubleshooting.md)。

每篇教程都包含最小示例、完整示例、代码解释、常见错误以及对应的
RCUI Demo 页面。除非代码块特别标注 Gradle Kotlin DSL，否则示例均为 Java。

图片和示意图统一预留在 [`docs/imags`](../imags/) 中。

另请参阅：[English documentation](../en/README.md)。
