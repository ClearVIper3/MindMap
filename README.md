# 🧠 MindMap — Minimal Java Swing Mind-Mapping Demo

一款基于 **Java Swing** 的极简思维导图桌面应用。整个项目仅 **6 个 `.java` 文件、约 530 行**，零第三方依赖，开箱即用。在保留完整演示效果（增删/重命名/三种布局/缩放平移/导入导出/PNG 导出）的前提下，把代码体量压到了"够用就好"的程度，适合作为 Java 桌面应用的入门样本。

---

## ✨ Features

- 添加 / 删除 / 重命名 节点（含子节点与同级节点）
- 三种布局：**Balanced** / **Right-Flow** / **Left-Flow**，下拉即时切换
- **画布交互**：滚轮以鼠标点为锚点缩放、鼠标拖拽平移
- **左侧 JTree** 与画布双向同步选中
- 文件保存 / 打开（`.dt`，Java 原生序列化）
- **导出 PNG/JPG**：按当前布局离屏绘制整张图
- 系统外观（Look & Feel）自适配

---

## 🚀 Quick Start

### 环境要求
- **JDK 23**（见 [`pom.xml`](./pom.xml)）
- 可选：Maven 3.6+（仅用于构建/运行；纯 JDK 也能编译）

### 通过 Maven 运行
```bash
mvn compile
mvn exec:java
```

### 直接 javac 运行（无 Maven 也行）
```bash
javac -d out $(find src/main/java -name "*.java")
java  -cp out mindmap.app.MindMapApp
```

### 打包成可执行 jar
```bash
mvn package
java -jar target/MindMap-1.0-SNAPSHOT.jar
```

---

## 🗂 Project Layout

```
src/main/java/mindmap/
├── app/MindMapApp.java          # 入口：EDT + LookAndFeel
├── model/
│   ├── MindNode.java            # 树节点（text + children + parent，可序列化）
│   ├── Layout.java              # 布局算法（三种类型，纯计算）
│   └── Renderer.java            # 纯绘制 + 配色常量（无 Swing 状态）
├── ui/
│   └── MainFrame.java           # 主窗口：工具栏 + 画布 + JTree（含两个内部类）
└── util/
    └── FileHandler.java         # 序列化保存/加载、PNG 导出

合计 6 文件，~530 行。
```

依赖方向（单向，无循环）：

```
        app  ──►  ui  ──►  model  ◄──  util
                    └──────────────────┘
```

`util` 仅依赖 `model`；`ui` 不向下泄漏到 `util`。绘制逻辑统一在 `model.Renderer`，屏幕绘制和 PNG 导出共用同一份代码。

---

## 🧩 Architecture

经典"模型 + 视图 + 极简事件总线"：

```
┌────────────────── ui.MainFrame ──────────────────┐
│                                                  │
│   Toolbar  ──┐                                   │
│   DrawPanel ─┼─►  mutate(...) / fire()           │
│   TreePanel ─┘         │                         │
│                        ▼                         │
│              List<Runnable> listeners            │
│                        │                         │
│         ┌──────────────┼──────────────┐          │
│         ▼              ▼              ▼          │
│   DrawPanel      TreePanel        StatusBar      │
│   重排+重绘       重建JTree        刷新文字       │
└──────────────────────────────────────────────────┘

绘制时：
   DrawPanel ──► Layout.compute(...)  ──► Map<MindNode, Rectangle>
                 Renderer.render(g, root, ly, sel)
导出时：
   FileHandler ──► Layout.compute / Layout.bounds
                ──► Renderer.render（同一份）
```

### 关键约定

| 文件 | 职责 | 是否依赖 Swing |
|---|---|---|
| `MindNode`     | 纯数据：`text` / `children` / `parent`，可序列化 | ❌ |
| `Layout`       | 计算每个节点的矩形包围盒 + 整树包围盒 | ❌（仅 `FontMetrics`） |
| `Renderer`     | 把 `(root, ly, selected)` 画到 `Graphics2D` | ❌（仅 `java.awt`） |
| `FileHandler`  | `.dt` 序列化 + 离屏 PNG/JPG 导出 | ❌ |
| `MainFrame`    | 全部 UI、事件、缩放/平移/拾取、JTree 同步 | ✅ |

---

## ⚙️ How It Works

### 1. 布局：两遍递归
- **第一遍** `sizes`：根据 `FontMetrics` 算出每个节点矩形的宽高
- **第二遍** `place`：自底向上累计每棵子树的总高，再自顶向下按高度比例分 Y，使父子居中对齐
- **Balanced**：偶/奇序号子节点交替分到右/左两侧
- **Right-Flow / Left-Flow**：所有子节点统一向一侧延伸

### 2. 缩放/平移
画布持有 `(scale, tx, ty)` 三个标量。`paintComponent` 里只做一次：

```java
g.translate(tx, ty);
g.scale(scale, scale);
Renderer.render(g, root, ly, selected);
```

滚轮缩放以鼠标点为锚点：
```
tx = e.x - (newScale/oldScale) * (e.x - tx);
```

### 3. 极简事件总线
```java
private final List<Runnable> listeners = new ArrayList<>();
private void fire()     { for (Runnable r : listeners) r.run(); }
private void mutate(Runnable r) { r.run(); canvas.layoutDirty = true; fire(); }
```
所有"改模型 → 重排 → 通知"的样板都收拢到 `mutate(...)` 一行。

---

## 🎯 Why This Repo

> 之前的版本走了"工程化教科书"路线：MVC 三层、Controller、ChangeType 枚举、LayoutEngine 工厂注册、19 个文件、1500 行。
>
> 当前版本是它的**精简继任**：保留所有可见功能，剔除过度抽象，把行数缩到约 1/3，专注演示"用最少的代码把一个 Swing 应用跑起来"。
>
> 如果你想看带 Controller、ChangeType、LayoutEngineFactory 的"完整工程"版本，可在 git 历史中回溯。

---

## 📜 License

仅用于学习与交流。
