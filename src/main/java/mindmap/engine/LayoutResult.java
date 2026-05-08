package mindmap.engine;

import mindmap.model.MindNode;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

/**
 * 一次布局的产物：MindNode -> NodeLayout 的映射。
 * 由 LayoutEngine 计算并返回，供视图层消费，取代把坐标塞回 MindNode 的做法。
 */
public final class LayoutResult {
    private final Map<MindNode, NodeLayout> layouts = new HashMap<>();

    public NodeLayout get(MindNode node) {
        return layouts.get(node);
    }

    public NodeLayout getOrCreate(MindNode node) {
        return layouts.computeIfAbsent(node, k -> new NodeLayout());
    }

    public void put(MindNode node, NodeLayout layout) {
        layouts.put(node, layout);
    }

    public boolean contains(MindNode node) {
        return layouts.containsKey(node);
    }

    public int size() {
        return layouts.size();
    }

    /**
     * 计算整棵子树的包围盒（含根节点）。不存在时返回 null。
     */
    public Rectangle computeBounds(MindNode root) {
        NodeLayout rootLayout = layouts.get(root);
        if (rootLayout == null) return null;
        Rectangle rect = new Rectangle(rootLayout.getX(), rootLayout.getY(),
                rootLayout.getWidth(), rootLayout.getHeight());
        addBounds(root, rect);
        return rect;
    }

    private void addBounds(MindNode node, Rectangle rect) {
        for (MindNode c : node.getChildren()) {
            NodeLayout l = layouts.get(c);
            if (l != null) {
                rect.add(new Rectangle(l.getX(), l.getY(), l.getWidth(), l.getHeight()));
            }
            addBounds(c, rect);
        }
    }
}
