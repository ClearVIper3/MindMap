package mindmap.engine;

import mindmap.model.MindNode;

import java.awt.FontMetrics;

/**
 * 布局策略接口。给定一棵思维导图的根节点和字体度量，
 * 计算并返回一个 {@link LayoutResult}（不产生副作用、不回写到 MindNode）。
 *
 * <p>之所以不再叫 LayoutManager：是为避免与 {@link java.awt.LayoutManager} 同名歧义。
 */
public interface LayoutEngine {
    LayoutResult calculateLayout(MindNode root, FontMetrics fm, String layoutType);
}
