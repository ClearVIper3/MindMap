# 📚 MindMap 项目学习规划（精简版）

> 面向**从零开始**的新读者。完整通读 ≈ **3～4 h**；如果只求"看懂主流程"≈ **1 h**。
>
> 配套阅读：[README.md](./README.md) 架构总览 · [pom.xml](./pom.xml) 构建配置。

---

## 0. 先决条件 Self-Check

| 知识点 | 熟练度要求 | 补课资源 |
|---|---|---|
| Java 基础语法、泛型、Lambda | ★★★ 必须 | 《Java 核心技术 卷 I》 |
| 集合框架（`List` / `Map` / `IdentityHashMap`）| ★★★ 必须 | 同上 |
| 接口 / 内部类 / 匿名类 | ★★★ 必须 | 同上 |
| Swing：`JFrame` / `JPanel` / `Graphics2D` / EDT | ★★ 推荐 | Oracle Swing Tutorial |
| 递归与树遍历（前序/后序）| ★★★ 必须 | 任一数据结构教材 |
| 仿射变换 `AffineTransform`（平移 + 缩放）| ★ 了解概念即可 | Java 2D 入门 |

> 💡 **不需要**提前掌握：JavaFX、Spring、并发、网络。本项目是单线程纯 Swing 桌面应用。

---

## 1. 项目整体视图

### 代码量一览（共 6 文件、约 530 行）

| 包 | 文件 | 行数 | 角色 |
|---|---|---|---|
| `app/`   | `MindMapApp.java`     | ~16  | 程序入口（EDT + LookAndFeel） |
| `model/` | `MindNode.java`       | ~36  | 树节点（纯数据，可序列化） |
| `model/` | `Layout.java`         | ~84  | 布局算法（三种类型，纯计算） |
| `model/` | `Renderer.java`       | ~58  | 纯绘制 + 配色常量 |
| `ui/`    | `MainFrame.java`      | ~290 | 主窗口（工具栏 + 画布 + 树，含 2 个内部类） |
| `util/`  | `FileHandler.java`    | ~54  | 序列化保存/加载、PNG 导出 |

> 📏 这是它前一版的约 **35%** 体量（19 文件 1500 行 → 6 文件 530 行）。所有功能保持不变。

### 模块依赖俯视图（无环）

```
        ┌─────────┐
        │  app/   │  入口
        └────┬────┘
             ▼
        ┌─────────┐
        │   ui/   │  Swing 组件 + 事件
        └─┬─────┬─┘
          │     │
          ▼     ▼
   ┌─────────┐ ┌──────────┐
   │ model/  │◄┤  util/   │  纯工具
   └─────────┘ └──────────┘
```

依赖永远向下：`util` 依赖 `model`，但 **不依赖 `ui`**——这意味着导出 PNG 与屏幕绘制共用同一份 `Renderer`，无需拉起任何窗口就可单测。

---

## 2. 分阶段学习路径（推荐顺序）

### 🎯 Phase 1 — 数据模型（15 min · 难度 ★☆☆☆☆）

**目标**：理解"思维导图"在内存中长什么样。

#### 📖 阅读
- [`MindNode.java`](./src/main/java/mindmap/model/MindNode.java) — 树节点

#### ✅ 通关自检
- [ ] `MindNode` 有哪些字段？为什么**没有** `x, y` 坐标？
- [ ] `parent` 字段为什么标 `transient`？`restoreParents()` 的作用？
- [ ] `addSiblingAfter` 为什么需要操作 `parent.children`，而不是当前节点自己？
- [ ] `remove()` 为什么对 root 是空操作？

#### 💻 动手
在 `MainFrame.newMap()` 中追加一个根节点子项 `"Networking"`，运行后观察是否自动出现在画布与左侧 JTree 中。

---

### 🎯 Phase 2 — 布局算法（45 min · 难度 ★★★☆☆）

**目标**：搞懂"一棵树 → 一组矩形"这个核心数学问题是怎么解的。

#### 📖 阅读
- [`Layout.java`](./src/main/java/mindmap/model/Layout.java) — 一站式布局

#### 🧠 核心算法

`Layout.compute(root, fm, type)` 分两遍递归：

```
① sizes(node)：根据 FontMetrics 算出每个节点矩形的 (w, h)
   w = textWidth + 2*PAD_X,   h = textHeight + 2*PAD_Y

② place(node, x, y, ...)：自底向上累计每棵子树总高度，
   再自顶向下按比例分配 Y 坐标，使父子居中对齐
```

三种布局共享同一份核心，仅"子节点放左还是放右"不同：

| 类型 | 子节点分布 |
|---|---|
| **Balanced**   | 偶序号放右、奇序号放左（默认） |
| **Right-Flow** | 全部向右延伸 |
| **Left-Flow**  | 全部向左延伸 |

> 📐 这是经典 **Reingold-Tilford 树布局**的简化版（非严格 tidy 版）。

#### ✅ 通关自检
- [ ] 为什么要先算所有子树高度，再分配 Y 坐标？能合并成一遍递归吗？
- [ ] `place` 里 `subtreeHeight` 数组有什么作用？为什么需要它？
- [ ] `Layout.bounds(...)` 用于什么场景？（提示：PNG 导出时算画布尺寸）
- [ ] 想新增一种"圆形放射状布局"，需要改动几个文件？（答：只改 `Layout.java`）

#### 💻 动手
1. 把 `V_GAP` 改成 50，观察画布间距变化。
2. 在 `Layout.TYPES` 数组里加一项 `"Compact"`，并在 `place` 里把它当作 `Balanced` 处理但 `H_GAP` 减半，验证下拉框出现新选项。

---

### 🎯 Phase 3 — 绘制（20 min · 难度 ★★☆☆☆）

**目标**：理解"矩形坐标 → 屏幕像素"这一步发生了什么。

#### 📖 阅读
- [`Renderer.java`](./src/main/java/mindmap/model/Renderer.java) — 纯绘制

#### 🔑 关键约定
- **无状态**：所有数据通过参数传入，类没有任何字段
- **后序绘制**：子节点先画、父节点后画 → 父节点会盖在子节点上方（更突出）
- **贝塞尔连线**：`Path2D.curveTo` 在父子节点之间画 S 形曲线，更优雅
- **选中态视觉**：浅蓝填充 + 红色加粗描边 + 文字加粗

#### ✅ 通关自检
- [ ] `drawConn` 里 `right` 布尔值如何判断？为什么决定连线方向？
- [ ] 选中节点的视觉差异由哪几行代码控制？
- [ ] 既然 `Renderer` 是无状态的，能不能复用它把当前导图导出成 SVG？需要改什么？

#### 💻 动手
给选中节点增加一圈**黄色阴影**：在画白底圆角矩形之前，先用半透明黄色画一个稍大的圆角矩形即可。

---

### 🎯 Phase 4 — UI 主框架（60 min · 难度 ★★★★☆）

**目标**：理解 Swing 如何把所有组件串起来，事件如何流动，缩放/平移如何实现。

#### 📖 阅读
- [`MainFrame.java`](./src/main/java/mindmap/ui/MainFrame.java)（含两个内部类 `DrawPanel` / `TreePanel`）

#### 🔑 关键概念

**(1) 极简事件总线**

没有 Controller、没有 ChangeType 枚举，只用一个 `List<Runnable>`：

```java
private final List<Runnable> listeners = new ArrayList<>();
private void fire() { for (Runnable r : listeners) r.run(); }
private void mutate(Runnable r) { r.run(); canvas.layoutDirty = true; fire(); }
```

所有"修改模型 → 标记重排 → 通知 UI"的样板都收拢到 `mutate(...)` 一行。

**(2) 缩放 / 平移**

`DrawPanel` 持有三个标量 `(scale, tx, ty)`，绘制时一次性应用：

```java
g.translate(tx, ty);
g.scale(scale, scale);
Renderer.render(g, root, ly, selected);
```

滚轮缩放以**鼠标点为锚点**：
```
tx = e.x - (newScale/oldScale) * (e.x - tx);
```

**(3) 命中测试**

`DrawPanel.findAt` 是后序递归——子节点优先命中，避免父节点重叠时盖住子节点。

**(4) JTree 同步**

`TreePanel` 用 `IdentityHashMap<MindNode, DefaultMutableTreeNode>` 把模型节点映射到树节点；同步时用 `syncing` 标志位防止双向回调死循环。

#### ✅ 通关自检
- [ ] 鼠标点击屏幕坐标 `(e.x, e.y)`，怎样还原为模型坐标？为什么要除以 `scale` 再减 `tx`？
- [ ] `mutate(...)` 与 `fire()` 何时该用哪个？
- [ ] 切换布局类型属于"模型变更"吗？为什么也要走 `mutate`？
- [ ] 为什么 `TreePanel.sync()` 要用 `syncing` 标志？去掉会怎样？
- [ ] `paintComponent` 里 `g.create()` 与 `g.setTransform(o)` 的配对作用是什么？

#### 💻 动手
1. 加键盘快捷键：`Enter` = 添加子节点、`Delete` = 删除选中（`addKeyBinding` 或 `InputMap`）。
2. 给画布右下角画一个"当前缩放比"小标签（`100%`、`50%`...）。

---

### 🎯 Phase 5 — 文件 I/O（10 min · 难度 ★☆☆☆☆）

**目标**：理解保存/加载/导出图片的实现。

#### 📖 阅读
- [`FileHandler.java`](./src/main/java/mindmap/util/FileHandler.java)

#### ✅ 通关自检
- [ ] `.dt` 文件用的是什么序列化机制？优缺点？
- [ ] 加载后为什么必须调 `restoreParents()`？
- [ ] PNG 导出是"截屏画布"还是"离屏重画"？为什么这样设计？
- [ ] 导出图片时为什么要先 `Layout.compute` 一次再 `Layout.bounds`？

#### 💻 动手
让 `exportImage` 支持"裁剪 50px margin → 0px"——观察图片紧贴节点是否更好看。

---

### 🎯 Phase 6 — 启动入口（5 min · 难度 ★☆☆☆☆）

#### 📖 阅读
- [`MindMapApp.java`](./src/main/java/mindmap/app/MindMapApp.java)

#### ✅ 通关自检
- [ ] `main` 方法为什么把窗口创建放进 `SwingUtilities.invokeLater`？
- [ ] `setLookAndFeel(getSystemLookAndFeelClassName())` 失败时如何容错？

---

## 3. 全流程沙盘推演

完成上面 6 个 Phase 后，闭卷回答：**"用户点击工具栏 `+ Child` 后，屏幕上为什么会出现一个新节点？"**

标准答案应当覆盖以下 7 步：

```
1. JButton 的 ActionListener 被触发 → 进入 lambda → 弹出 JOptionPane 输入文本
2. mutate(() -> selected.addChild(new MindNode(t))) 被调用
3. mutate 体内：
     ① 修改模型（addChild）
     ② canvas.layoutDirty = true
     ③ fire() 通知所有 Runnable 监听器
4. 监听器触发：
     ① DrawPanel：repaint()
     ② TreePanel：sync() 重建 JTree
     ③ status 标签：刷新文字
5. Swing EDT 调度到 DrawPanel.paintComponent
6. 检测到 layoutDirty=true → Layout.compute(...) 重算所有矩形
7. Renderer.render(g, root, ly, selected) 把像素画出来
```

如果任一环节说不清，回到对应 Phase 重读。

---

## 4. 渐进式改造练习（Optional · 进阶）

| # | 练习 | 难度 | 涉及文件 | 收获 |
|---|---|---|---|---|
| 1 | 添加键盘快捷键（Enter / Tab / Delete）         | ★★    | `MainFrame`           | InputMap / ActionMap |
| 2 | 节点字段加 `color`，按层级自动上色             | ★★    | `MindNode` + `Renderer` | 字段穿透 |
| 3 | 实现撤销 / 重做（命令栈）                      | ★★★★  | `MainFrame`           | 命令模式 |
| 4 | 把 `ObjectOutputStream` 换成 JSON              | ★★★   | `FileHandler`         | 格式演进 |
| 5 | 新增"圆形放射状布局"                          | ★★★   | `Layout`              | 算法扩展 |
| 6 | 双击节点直接原地编辑文字（用 `JTextField` 浮层）| ★★★★  | `MainFrame.DrawPanel` | Swing 组件叠加 |

---

## 5. 阅读节奏建议

### 📅 半日通关版（3～4 h）
- 第 1 小时：Phase 1 + 2（数据 + 算法）
- 第 2 小时：Phase 3 + 4 前半（绘制 + 事件总线）
- 第 3 小时：Phase 4 后半（缩放/平移/JTree）
- 第 4 小时：Phase 5 + 6 + 沙盘推演

### 📅 三日精读版（每天 1 h）
- Day 1：Phase 1 + 2，手画算法执行过程
- Day 2：Phase 3 + 4，跑一次 `mutate` 全链路
- Day 3：Phase 5 + 6 + 沙盘 + 挑一个改造练习

---

## 6. 常见困惑 FAQ

**Q1：为什么没有 Controller / Service / Repository 这些"标准分层"？**
A：本项目主动选择了"够用就好"的极简风格——所有 UI 事件直接在 `MainFrame` 里 lambda + `mutate(...)` 处理，5 行做完一件事，没有跨文件跳转。如果未来真的需要撤销/重做、权限、日志、远程同步这类横切关注点，再单独抽 Controller 也来得及。**先别为了模式而模式。**

**Q2：为什么 `Renderer` 放在 `model` 包，而不是 `ui` 包？**
A：`Renderer` 不依赖任何 Swing 组件（只用 `java.awt.Graphics2D`），且**屏幕绘制和 PNG 导出共用同一份**。如果放 `ui`，会让 `util.FileHandler` 反向依赖 `ui`，制造依赖环。

**Q3：为什么 Model 不持有坐标 `(x, y)`？**
A：坐标是"布局的产物"而非"数据的本质"。同一棵树可用三种布局产生三套坐标。把坐标从 Model 中剥离，才能让 `MindNode` 纯净、可序列化、零 Swing 依赖。

**Q4：`mutate(Runnable)` 是什么模式？**
A：可以理解为**模板方法 + 钩子函数**。固定流程是"改模型 → 标记 dirty → 通知"，可变部分（具体怎么改）通过 `Runnable` 注入。它把 6 处样板压成一行，是本项目精简的关键。

**Q5：Swing 在 2026 年还值得学吗？**
A：作为学习目的**很值得**——它是少数还在活跃使用、"一切都可见"的 GUI 框架，事件循环、EDT、绘制管线都直白暴露。生产推荐 JavaFX / Compose Desktop。

---

## 7. 延伸阅读

- **Reingold-Tilford Trees**（本项目布局算法的理论根源，Wikipedia "Tidy Trees"）
- **Oracle Swing Tutorial** — `Graphics2D`、`AffineTransform`、`KeyBinding`
- **《Effective Java（第 3 版）》** — 条款 17 "使可变性最小化"，与本项目 Model 设计呼应
- **John Ousterhout《A Philosophy of Software Design》** — 第 4 章 "Modules Should Be Deep" 与本项目精简化思路一致

---

## 8. 你已经准备好了吗？

✅ 能在白纸上画出 4 个包的依赖关系
✅ 能解释 `mutate(...)` 一行代码做了哪三件事
✅ 能解释"为什么 `Layout.compute` 需要 `FontMetrics` 参数"
✅ 能独立完成上面至少 2 个改造练习

做到以上 4 点，可以给自己贴一张 **"MindMap Mini 项目毕业生 🎓"** 的标签。
