# 📚 MindMap 项目学习规划

> 面向**从零开始**的新读者。完整通读 ≈ **一个工作日（6～8 h）**；如果只求"看懂主流程"≈ **2 h**。
>
> 本指南配套阅读：[README.md](./README.md) 架构总览 · [pom.xml](./pom.xml) 构建配置。

---

## 0. 先决条件 Self-Check

在开始前，请先确认自己掌握以下基础（如不熟悉，对照"补课资源"先补）：

| 知识点 | 熟练度要求 | 补课资源 |
|---|---|---|
| Java 基础语法、泛型、枚举 | ★★★ 必须 | 《Java 核心技术 卷 I》 |
| 集合框架（`List`、`Map`）| ★★★ 必须 | 同上 |
| 接口 / 抽象类 / 多态 | ★★★ 必须 | 同上 |
| Lambda 与 `Supplier` / `Consumer` | ★★ 推荐 | Java 8 Stream 教程 |
| Swing：`JFrame` / `JPanel` / `Graphics2D` | ★★ 推荐 | Oracle Swing Tutorial |
| 设计模式：MVC、观察者、策略 | ★ 了解概念即可 | 《Head First 设计模式》前 3 章 |

> 💡 **不需要**提前掌握：JavaFX、Spring、并发编程、网络编程。项目是纯单线程 Swing 桌面应用。

---

## 1. 项目整体视图

### 代码量一览

| 目录 | 文件数 | 行数（约） | 关键角色 |
|---|---|---|---|
| `app/`        | 1  | 23   | 程序入口 |
| `model/`      | 4  | 150  | 数据与事件源 |
| `controller/` | 1  | 110  | 业务协调 |
| `engine/`     | 7  | 220  | 布局算法 |
| `ui/`         | 5  | 510  | 界面与交互 |
| `util/`       | 1  | 90   | 文件 I/O |
| **合计**      | **19** | **~1500** | —— |

> 📏 参照感受：Spring Boot 一个最小 HelloWorld 依赖链 > 10 万行；本项目约为它的 **1.5%**。完全在一天之内通读并理解的量级。

### 模块依赖俯视图

```
           ┌──────────┐
           │   app/   │  (入口，仅 new 出其它对象)
           └────┬─────┘
                │
    ┌───────────▼────────────┐
    │          ui/           │  Swing 组件：窗口、工具栏、画布
    └──┬──────────┬──────────┘
       │          │
       ▼          ▼
┌──────────┐ ┌──────────┐
│controller│ │  engine/ │  纯算法，无 Swing 依赖
└────┬─────┘ └────┬─────┘
     │            │
     ▼            ▼
  ┌──────────────────┐
  │      model/      │  纯数据 + 事件源
  └──────────────────┘
           ▲
           │
       ┌───┴────┐
       │ util/  │  纯工具
       └────────┘
```

**依赖方向永远向下**，跨越箭头反向依赖是架构 bug，阅读时可以当作校验点。

---

## 2. 分阶段学习路径（推荐顺序）

### 🎯 Phase 1 — 数据层（30 min · 难度 ★☆☆☆☆）

**目标**：理解"思维导图"在内存中究竟长什么样、怎么通知外界"我变了"。

#### 📖 阅读顺序
1. [`MindNode.java`](./src/main/java/mindmap/model/MindNode.java) — 52 行
2. [`ChangeType.java`](./src/main/java/mindmap/model/ChangeType.java) — 16 行
3. [`ChangeListener.java`](./src/main/java/mindmap/model/ChangeListener.java) — 单方法接口
4. [`MindMapModel.java`](./src/main/java/mindmap/model/MindMapModel.java) — 72 行

#### ✅ 通关自检
- [ ] `MindNode` 有哪些字段？为什么**没有** `x, y` 坐标？
- [ ] `ChangeType` 有 4 种事件：`STRUCTURE_CHANGED` / `SELECTION_CHANGED` / `LAYOUT_CHANGED` / `FILE_CHANGED`，各对应什么场景？
- [ ] `MindMapModel.fire()` 为什么要 `new ArrayList<>(listeners)` 遍历？（答：防止监听器回调中再注册/移除导致并发修改）
- [ ] 为什么 `setSelectedNode` 里要判断"相同则 return"？

#### 💻 动手练习
1. 在 `MindMapModel` 里增加一个默认节点 `"Networking"`。运行后观察是否自动出现在画布上。
2. 写一个临时的匿名 `ChangeListener`，在控制台打印每次事件类型，感受事件流触发频率。

---

### 🎯 Phase 2 — 算法层（45 min · 难度 ★★★☆☆）

**目标**：搞懂"一棵树 → 一堆坐标"这个核心数学问题是怎么解的。

#### 📖 阅读顺序
1. [`NodeLayout.java`](./src/main/java/mindmap/engine/NodeLayout.java) — 单节点坐标/尺寸容器
2. [`LayoutResult.java`](./src/main/java/mindmap/engine/LayoutResult.java) — 整张图的布局结果
3. [`LayoutEngine.java`](./src/main/java/mindmap/engine/LayoutEngine.java) — 策略接口（**重点**）
4. [`AutoLayoutEngine.java`](./src/main/java/mindmap/engine/AutoLayoutEngine.java) — 核心算法实现
5. [`DirectionalLayoutEngine.java`](./src/main/java/mindmap/engine/DirectionalLayoutEngine.java) — 变体
6. [`LayoutEngineFactory.java`](./src/main/java/mindmap/engine/LayoutEngineFactory.java) — 注册表

> 🗒️ [`LayoutManager.java`](./src/main/java/mindmap/engine/LayoutManager.java) 是 `@Deprecated` 兼容接口，**跳过**。

#### 🧠 核心算法理解

`AutoLayoutEngine` 采用两遍递归：

```
① 后序遍历（Bottom-Up）：计算每棵子树的"包围盒高度"
   subtreeHeight(node) = Σ subtreeHeight(child) + padding
   叶子节点  subtreeHeight = nodeHeight

② 前序遍历（Top-Down）：按子树高度比例分配 Y 坐标
   父节点居中于所有子树占据的总高度之中
```

> 📐 这是经典的 **Reingold-Tilford 树布局算法**的简化版。推荐阅读 Wikipedia "Tidy Trees" 条目加深理解。

#### ✅ 通关自检
- [ ] 为什么要先算所有子树高度，再分配 Y 坐标？能一次完成吗？
- [ ] `LayoutEngineFactory` 用了 `Supplier<LayoutEngine>` 而非直接存实例，好处是什么？
- [ ] 如果我想加一种"圆形放射状布局"，需要改动几个文件？（答：新建 1 个 Engine 类 + Factory 注册 1 行）
- [ ] `LayoutResult` 和 `NodeLayout` 为什么拆成两个类？合成一个不行吗？

#### 💻 动手练习
1. 在 `AutoLayoutEngine` 里把节点间垂直间距 `+10px`，观察画布变化。
2. 给 `LayoutEngineFactory` 注册一个"Balanced-Wide" 布局（映射到 `AutoLayoutEngine`，但你可以新建一个类调整水平间距），然后在工具栏下拉框里验证它出现了。

---

### 🎯 Phase 3 — 控制层（20 min · 难度 ★★☆☆☆）

**目标**：理解"用户点了按钮"到"数据真的变了"之间发生了什么。

#### 📖 阅读顺序
1. [`MindMapController.java`](./src/main/java/mindmap/controller/MindMapController.java) — 111 行

#### 🔑 关键约定
- **View 永远不直接改 Model**，必须通过 `Controller` 的方法调用
- `Controller` 在改完 Model 后负责 `fire(ChangeType)`
- `Controller` **不依赖任何具体 Swing 组件类型**（例外：`DrawingPanel` 通过内部接口 `FileExportTarget` 解耦传入）

#### ✅ 通关自检
- [ ] `deleteSelected()` 为什么要判断 `selected == model.getRoot()`？
- [ ] `addSiblingToSelected` 为什么要求 `selected.getParent() != null`？
- [ ] 为什么抽象出 `FileExportTarget` 接口？（答：避免 Controller 直接 `import DrawingPanel`，保持 Controller → View 的禁止依赖方向）
- [ ] 如果要实现 **撤销/重做**，你会怎么改造 Controller？（提示：命令模式 + 操作栈）

#### 💻 动手练习
1. 新增一个 Controller 方法 `moveSelectedUp()`：把选中节点在其兄弟列表中上移一位。记得 `fire(STRUCTURE_CHANGED)`。
2. 在 `ToolbarView` 中加一个 "⬆ Move Up" 按钮调用它。

---

### 🎯 Phase 4 — 视图层（60 min · 难度 ★★★★☆）

**目标**：理解 Swing 如何把计算结果真正画到屏幕上，以及鼠标事件如何流回 Controller。

#### 📖 阅读顺序（由简入繁）
1. [`MainFrame.java`](./src/main/java/mindmap/ui/MainFrame.java) — 装配窗口
2. [`ToolbarView.java`](./src/main/java/mindmap/ui/ToolbarView.java) — 140 行
3. [`StructureTreeView.java`](./src/main/java/mindmap/ui/StructureTreeView.java) — JTree 适配
4. [`MindMapRenderer.java`](./src/main/java/mindmap/ui/MindMapRenderer.java) — 79 行 **纯绘制**
5. [`DrawingPanel.java`](./src/main/java/mindmap/ui/DrawingPanel.java) — **重点**，含缩放/平移/拖动

#### 🔑 关键概念
- **EDT**：所有 Swing 操作必须在 Event Dispatch Thread 上进行
- **`paintComponent(Graphics g)`**：Swing 的绘制入口。不要自己调用，而是 `repaint()` 请求重绘
- **`AffineTransform`**：`DrawingPanel` 用它做缩放和平移，鼠标坐标需要反向变换回模型坐标
- **Renderer 无状态**：`MindMapRenderer` 只接受 `LayoutResult` + `Graphics2D` 参数，本身不持有任何字段；未来可直接复用它导出 SVG/PNG

#### ✅ 通关自检
- [ ] `DrawingPanel` 收到 `SELECTION_CHANGED` 时做什么？收到 `STRUCTURE_CHANGED` 时做什么？两者的性能差异？
- [ ] 鼠标点击画布 `(px, py)` 像素坐标，如何确定点到了哪个节点？
- [ ] 为什么 `Renderer` 不持有 `MindNode`？（答：保持无状态，可独立测试、可用于导出）
- [ ] `ToolbarView` 的下拉框选项是怎么来的？（答：`LayoutEngineFactory.availableLayouts()`，所以新增布局后下拉框会自动更新）

#### 💻 动手练习
1. 在 `MindMapRenderer` 中给选中节点增加一圈阴影效果。
2. 在 `DrawingPanel` 中加入 Ctrl + 滚轮缩放（可能已经实现，观察现状再决定是增强还是调参）。

---

### 🎯 Phase 5 — 工具层（10 min · 难度 ★☆☆☆☆）

**目标**：理解文件保存/读取/导出图片如何实现。

#### 📖 阅读顺序
1. [`FileHandler.java`](./src/main/java/mindmap/util/FileHandler.java) — 90 行

#### ✅ 通关自检
- [ ] 保存文件用的是什么序列化机制？有什么优缺点？
- [ ] 为什么 `MindNode` 要实现 `Serializable`？
- [ ] 导出图片是离屏渲染还是直接截屏 `DrawingPanel`？

---

### 🎯 Phase 6 — 启动入口（5 min · 难度 ★☆☆☆☆）

#### 📖 阅读顺序
1. [`MindMapApp.java`](./src/main/java/mindmap/app/MindMapApp.java) — 23 行

#### ✅ 通关自检
- [ ] `main` 方法为什么要把窗口创建放进 `SwingUtilities.invokeLater`？
- [ ] 对象装配顺序是 `Model → Controller → View`，反过来行吗？为什么？

---

## 3. 全流程沙盘推演

完成上面 6 个 Phase 后，闭卷回答：**"用户点击工具栏'添加子节点'按钮后，屏幕上为什么会出现一个新节点？"**

标准答案应当覆盖以下 8 个步骤：

```
1. ToolbarView 的 JButton ActionListener 被触发
2. 调用 MindMapController.addChildToSelected(text)
3. Controller 找到 model.getSelectedNode()，对其 addChild(new MindNode(text))
4. Controller 调用 model.fire(STRUCTURE_CHANGED)
5. MindMapModel 遍历 listeners，调用 onChange(STRUCTURE_CHANGED)
   - StructureTreeView：重建 JTree 模型
   - DrawingPanel：标记"布局需重算"并 repaint()
6. Swing EDT 调度到 DrawingPanel.paintComponent()
7. DrawingPanel 通过 LayoutEngineFactory.create(currentLayout) 拿到引擎
   调用 engine.calculateLayout(root, fm, layoutType) 得到 LayoutResult
8. MindMapRenderer.render(g, layoutResult, selectedNode) 把像素画出来
```

如果任一环节说不清，回到对应 Phase 重读。

---

## 4. 渐进式改造练习（Optional · 进阶）

按难度从低到高，建议挑 2～3 个做：

| # | 练习 | 难度 | 涉及层 | 收获 |
|---|---|---|---|---|
| 1 | 添加键盘快捷键（Enter=添加子节点，Tab=添加兄弟）| ★★ | ui + controller | 熟悉 Swing KeyBinding |
| 2 | 新增"节点颜色"字段，按层级自动上色 | ★★ | model + renderer | 理解字段穿透 |
| 3 | 实现撤销 / 重做（命令模式） | ★★★★ | controller | 设计模式实战 |
| 4 | 把 `ObjectOutputStream` 序列化换成 JSON | ★★★ | util | 格式演进与兼容 |
| 5 | 新增圆形放射状布局 | ★★★ | engine | 策略模式扩展 |
| 6 | 超大树布局用 `SwingWorker` 异步计算 | ★★★★ | ui + engine | 并发 + UI 线程 |

---

## 5. 阅读节奏建议

### 📅 一日通关版（6～8 h）
- 上午：Phase 1 + 2（1.5 h）+ 动手练习（1 h）
- 下午：Phase 3 + 4（1.5 h）+ 动手练习（1.5 h）
- 傍晚：Phase 5 + 6 + 沙盘推演（1 h）

### 📅 一周精读版（每天 1 h）
- Day 1：Phase 1，做完所有练习
- Day 2：Phase 2，手画算法执行过程
- Day 3：Phase 3，完成"Move Up"练习
- Day 4：Phase 4 前半（MainFrame + ToolbarView + TreeView）
- Day 5：Phase 4 后半（Renderer + DrawingPanel）
- Day 6：Phase 5 + 6 + 沙盘推演
- Day 7：挑一个改造练习实战

---

## 6. 常见困惑 FAQ

**Q1：为什么 `LayoutManager.java` 是空的还留着？**
A：和 `java.awt.LayoutManager` 重名容易误导读者，P1 阶段改名为 `LayoutEngine`，但为了不破坏可能的外部引用，保留空的 `@Deprecated` 子接口做过渡。阅读时可以**完全忽略**它。

**Q2：为什么 Model 不持有坐标 (x, y)？**
A：坐标是"布局的产物"而非"数据的本质"。同一棵树可以用不同布局算法产生不同坐标。把坐标从 Model 中剥离，才能让 Model 纯净、可序列化、可单元测试。

**Q3：Controller 看起来很薄，为什么不直接让 View 改 Model？**
A：现在看是薄，但 Controller 是**抗变化的关键"腰部"**。一旦要加撤销/重做、权限、日志、脏检查、UI 无关的业务验证，全部都在这里加，View 不动。

**Q4：为什么 `MindMapRenderer` 要设计成无状态？**
A：① 可独立单测（给一个 `LayoutResult` 就能验证绘制）；② 可被复用（同一个 Renderer 即画屏幕又导出 PNG/SVG）；③ 线程安全隐患小。

**Q5：Swing 在 2026 年还值得学吗？**
A：作为学习目的**很值得**——它是少数还在活跃使用的"一切都可见"的 GUI 框架，事件循环、EDT、绘制管线都直白暴露。工程落地推荐 JavaFX / Compose Desktop / Electron，但这是另一个话题。

---

## 7. 延伸阅读

- **Reingold-Tilford Trees**（本项目布局算法的理论根源）
- **《Design Patterns》GoF** — 第 4 章观察者、第 5 章策略、第 2 章命令
- **Oracle Swing Tutorial** — `Graphics2D`、`AffineTransform`、`KeyBinding`
- **Effective Java（第 3 版）** — 条款 17 "使可变性最小化" 与本项目 Model 设计呼应

---

## 8. 你已经准备好了吗？

✅ 能在一张白纸上画出 5 个包的依赖关系
✅ 能背出 4 个 `ChangeType` 及它们各自的响应
✅ 能解释"为什么 `LayoutEngine` 需要 `FontMetrics` 参数"
✅ 能独立完成上面至少 2 个改造练习

做到以上 4 点，可以给自己贴一张 **"MindMap 项目毕业生 🎓"** 的标签了。
