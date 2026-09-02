# 问题排查

## 页面空白

确认根节点或 UiTemplate 已传给 UiScreen，或者宿主把绘制和输入回调转发给
UiHost。通过 Ui.* 创建的节点只有加入活动树后才会显示。

## 控件不改变值

检查 UiBinding 的 getter 和 setter。生成的配置包装类使用生成的字段 Binding；
本地值则确认 setter 修改的是 Mod 其他代码真正读取的对象。

## Footer 移动或消失

把 Ui.Node 传给 UiTemplate.footer(...) 或 UiScaffold.footer(...)。不要把 Footer
放进会滚动的 Content。

## 文本输入或焦点无效

通过 UiHost 转发键盘、文本输入、剪贴板、鼠标和焦点事件。确认文本框可见、enabled
且可聚焦。控件接受提交时 Enter 提交，Esc 可以取消编辑。

## Content 把 Dialog 或 Tooltip 裁剪了

把弹窗放在 UiDialogHost 下，并用 dialogs.root(...) 包住完整根节点。Tooltip 使用
Ui.tooltip(...)。不要把覆盖层直接绘制到会裁剪的 Content 中。

## 预览绘制超出卡片

使用 UiPreview 或 MinecraftPreview，并遵守 renderer 回调收到的 bounds 和有效
clip。Renderer 的 clip 操作必须成对恢复。

## 高 GUI Scale 破坏布局

不要再次缩放 Canvas。使用 UiScalePolicy.minecraft() 或固定 UiDensity。在窄和宽
逻辑视口下测试，让 Row、Grid、SplitLayout 根据可用空间布局。

## 关闭页面后资源仍存在

在 dispose() 中释放纹理、模型、异步任务和临时数据。保持释放幂等，节点脱离树后
不要继续给它发送事件。

## 运行时缺少类

确认 artifact 同时匹配 Minecraft 版本和 Loader，并确认宿主 JAR 已将平台 artifact
以 Jar-in-Jar 形式内嵌。玩家不应另外安装 RCUI。

## 调试清单

1. 运行 RCUI Demo；
2. 测试能产生 comfortable、normal、compact 密度的 GUI Scale；
3. 测试 hover、pressed、focused、disabled、selected；
4. 测试 Tooltip、Dialog、Footer、TextField、Preview 和独立滚动；
5. 将宿主代码与对应文档示例逐行对照。
