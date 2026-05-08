package mindmap.model;

/**
 * Model 变更事件类型。用于细粒度地通知观察者：
 * - STRUCTURE_CHANGED：节点增删、改名、换根 —— 需要重算布局 + 重建侧栏
 * - SELECTION_CHANGED：仅选中节点变化 —— 只需重绘
 * - LAYOUT_CHANGED：布局策略切换 —— 需要重算布局
 * - FILE_CHANGED：当前文件名变化 —— 仅刷新状态栏
 */
public enum ChangeType {
    STRUCTURE_CHANGED,
    SELECTION_CHANGED,
    LAYOUT_CHANGED,
    FILE_CHANGED
}
