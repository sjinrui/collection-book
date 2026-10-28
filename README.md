# 收集书 Collection Book

一个**通用、事件驱动**的 Minecraft 收集手册模组。它不依赖任何特定 mod，由模组自己监听玩家行为来记录收集进度，因此**可以放进任意整合包**使用。

## 特性

- **四类收集**：
  - 物品（拾取/获得）
  - 生物（击杀）
  - 食物（进食）
  - 生物群系（到访）
- **新收集提醒**：新获得的条目金色高亮 + 角标，顶部「新 N」按钮一键确认。
- **按获取时间排序**：默认 / 最新优先 / 最早优先，循环切换。
- **按状态筛选**：全部 / 已收集 / 未收集。
- 数据按玩家在服务端持久化，并通过网络同步到客户端界面；支持单人游戏与专用服务器。
- 基于 Architectury，同一份核心代码同时提供 **Forge 与 Fabric** 两个版本。

## 安装

- Minecraft **1.20.1**
- 需要 **Architectury API**（9.2.14 或更高）
- Fabric 版还需 **Fabric Loader**（0.15.11+）与 **Fabric API**
- 下载对应平台的 jar 放入 `mods` 文件夹即可。

## 合成

在游戏中用 **1 本书 + 4 个金粒**（无序合成）获得「收集书」，右键打开；也可按快捷键 **B** 打开。

## 从源码构建

需要 JDK 17。

```bash
# 同时构建两个平台
./gradlew build

# 或分别构建
./gradlew :forge:build
./gradlew :fabric:build
```

构建产物位于 `forge/build/libs/` 与 `fabric/build/libs/`（无 classifier 的 jar 即为可安装的主文件）。

## 许可证

MIT
