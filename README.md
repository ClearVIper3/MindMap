# 🧠 MindMap Desktop Application

一款基于 Java (Swing/JavaFX) 开发的轻量级思维导图桌面应用程序。本项目采用了经典的 MVC 架构，并实现了递归自动布局算法，旨在提供流畅、高效的思维导图绘制体验。

## 1. 架构概览 (Architecture Overview)

本项目采用了经典的 **MVC（Model-View-Controller）** 架构，实现了状态与渲染的彻底分离：

*   **Model (数据层)**：`MindNode` 和 `MindMapModel`。只负责维护树形数据结构、节点属性（文本、层级）以及父子关系的引用，与 UI 完全解耦。
*   **View (视图层)**：`DrawingPanel`, `StructureTreeView`, `ToolbarView` 等。专注于利用 `Graphics2D` 将节点绘制到屏幕上，或在侧边栏以树形列表展示。
*   **Controller (控制与逻辑层)**：`MainFrame` 充当事件协调者，`LayoutManager` 和具体的 `Engine`（如 `AutoLayoutEngine`）充当业务逻辑控制器。

### 核心数据流转（以“添加子节点”为例）
1.  **事件捕获**：用户在视图区点击“添加子节点”，触发事件监听器。
2.  **模型更新**：Controller 调用 `MindMapModel.addChildNode()` 修改底层数据结构。
3.  **重新布局**：Model 更新触发观察者模式通知，`LayoutManager` 调用相应的布局引擎（如 `AutoLayoutEngine`）重新计算各个节点的 $(x, y)$ 坐标。
4.  **视图重绘**：布局完毕后，触发 `DrawingPanel.repaint()`。
5.  **底层渲染**：Swing EDT (Event Dispatch Thread) 调用 `paintComponent(Graphics g)`，读取节点的新坐标并渲染屏幕。

## 2. 核心算法探秘 (The Core Algorithm)

### 递归布局逻辑 (Recursive Layout)
思维导图的自动排版采用 **后序遍历 (Bottom-Up) + 前序遍历 (Top-Down)** 结合的算法：

1.  **计算子树高度 (Bottom-Up)**：递归计算，一个节点的占用高度等于其所有子节点占用高度的总和（加上间距）。
2.  **分配坐标 (Top-Down)**：根节点位置固定，根据子树的高度，按比例将 Y 轴空间划分给各个子节点，确保节点父子居中对齐。

### 坐标系与防重叠原理
布局采用**包围盒（Bounding Box）**原理，将每个节点及其所有子代看作一个矩形块进行纵向堆叠排布，通过累加 `SubtreeHeight + Padding` 的方式分配 Y 坐标，从根本上杜绝节点物理碰撞。

## 3. 组件级剖析 (Component Breakdown)

### Graphics2D 渲染引擎
*   **双缓冲抗闪烁**：继承 `JPanel` 并重写 `paintComponent` 实现双缓冲绘制。
*   **抗锯齿渲染**：引入 `RenderingHints.KEY_ANTIALIASING`，确保连接曲线与圆角矩形的边缘平滑顺畅。

### 序列化与文件 I/O (Serialization)
*   通过 `ObjectOutputStream` 实现 `util.FileHandler` 的持久化操作。
*   **性能优化**：对所有的 UI 临时状态（如 `isSelected`, `isHovered`）以及计算得出的绝对坐标 $(x, y)$ 使用 `transient` 关键字修饰，极大减小了存档文件体积，避免跨设备分辨率导致的坐标错乱。

## 4. 技术亮点与设计模式

1.  **组合模式 (Composite)**：`MindNode` 即是节点也是容器，优雅处理树形数据结构。
2.  **策略模式 (Strategy)**：布局算法被抽象为 `LayoutEngine` 接口，支持在 `DirectionalLayoutEngine` 和 `AutoLayoutEngine` 之间无缝热切换。
3.  **观察者模式 (Observer)**：Model 的改变自动通知 UI 进行重绘，深度解耦数据和视图。
4.  **异步渲染优化**：处理超大节点树时，可将复杂的递归计算置于后台线程（如 `SwingWorker`），仅在计算完成后触发主线程的 `repaint()`，防止 UI 假死。

## 5. 未来演进路线 (Future Roadmap)

*   [ ] **撤销/重做 (Undo/Redo)**：引入命令模式 (Command Pattern)，封装操作栈以实现无损撤销。
*   [ ] **节点样式主题系统**：引入享元模式 (Flyweight) 全局管理 `Color` 和 `Font` 资源，实现一键切换主题外观。
*   [ ] **实时搜索定位**：利用深度优先搜索（DFS）遍历节点文本，匹配后动态高亮并自动滚动视图定位。
*   [ ] **连线类型扩展**：增加概要线 (Summary Link)，允许在非父子关系的节点之间创建视觉连接。