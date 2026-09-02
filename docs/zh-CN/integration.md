# 集成

RCUI 通过 JitPack 提供平台 artifact。RCUI 暂不计划作为单独安装的运行时 Mod
发布；请将匹配的 artifact 以 Jar-in-Jar 形式内嵌到宿主 Mod。

坐标格式如下：

```text
com.github.RethinkQAQ:
rethink-config-ui-lib-<minecraft-version>-<platform>:
v<release-version>
```

请使用[快速开始](getting-started.md)中的 Fabric、Forge 或 NeoForge 依赖脚本。
选择宿主实际使用的 Minecraft 版本和 Loader，不要混用不同平台 artifact。

模块职责如下：

| 模块 | 职责 |
| --- | --- |
| core | 节点、布局、主题、事件、配置控件和模板 |
| config | 配置注解、生成包装类和 YAML 持久化 |
| common | Minecraft Screen、渲染桥接、预览和版本适配 |
| fabric / forge / neoforge | Loader 元数据、打包和平台集成 |

不要让 Minecraft、Fabric、Forge、NeoForge 类型进入 core。业务模型、持久化策略、
翻译和领域操作应放在宿主 Mod。

报告集成问题时，请附上 Minecraft 版本、Loader、RCUI release、artifact 坐标，
以及宿主 JAR 是否包含内嵌的 RCUI 类。
