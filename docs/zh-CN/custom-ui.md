# 完全自定义 UI

RCUI 不只用于配置页面。模板不是强制的，你可以直接用节点制作编辑器、仪表盘、
工具或预览界面。

## 什么时候不使用模板

页面是全屏画布、三栏编辑器或游戏化工具时，可以跳过标准模板。如果页面仍然
符合 Header/Navigation/Content/Footer 结构，通常只需要在 Content 内自定义。

## 组合自定义根节点

```java
UiDialogHost dialogs = Ui.dialogHost();

Ui.Node root = Ui.stack()
    .add(Ui.panel().padding(12))
    .add(Ui.split(
        Ui.column()
            .add(Ui.label(UiText.literal("Tools")))
            .add(Ui.button(UiText.literal("Run"), this::runTool)),
        Ui.preview((renderer, bounds, clip, theme) -> {
            // Render a model, texture or chart here.
        }).preferredHeight(220))
        .gap(12));

Ui.Node withDialogLayer = dialogs.root(root);
```

UiStack 决定图层顺序；UiSplitLayout 提供响应式面板，并在宽度不足时改为垂直排列；
UiPreview 提供最终 bounds 和有效 clip；UiDialogHost 把弹窗层放到根节点上方。

## 宿主管理业务数据

配置值、排序、翻译、模型、纹理、持久化和异步任务仍由宿主负责。把数据传入
绑定或自定义组件，不要把全局业务服务隐藏在通用节点中。

## 当前边界

当前稳定能力包括节点组合、自定义测量和绘制、事件、焦点、Tooltip、Dialog、
Preview、滚动、可见性和生命周期。高级渲染层和大型动画系统在正式文档确认前，
应视为 Experimental。

## 检查清单

- 明确每个子节点由谁拥有；
- 根据可用宽高进行测量；
- 只在 bounds 和有效 clip 内绘制；
- 通过 UiHost 转发键盘、文本、剪贴板和鼠标事件；
- 让 Dialog 和 Tooltip 位于 Content 裁剪区域之外；
- 根节点关闭时释放资源。

![完全自定义 UI](../imags/custom-ui.png)
<!-- TODO: Add a screenshot of a non-configuration RCUI editor. -->

## Demo 对应页面

Custom 页面展示小型自定义节点树；Templates 和 Preview 页面展示如何用同样的
基础能力制作更大的编辑器式页面。
