package mindmap.engine;

import mindmap.model.MindNode;

import java.awt.FontMetrics;

public class DirectionalLayoutEngine extends AutoLayoutEngine {
    @Override
    public LayoutResult calculateLayout(MindNode root, FontMetrics fm, String layoutType) {
        LayoutResult result = new LayoutResult();
        layoutNodeSizes(root, fm, result);

        NodeLayout rootLayout = result.getOrCreate(root);
        rootLayout.setX(-rootLayout.getWidth() / 2);
        rootLayout.setY(-rootLayout.getHeight() / 2);

        if ("Right-Flow".equals(layoutType)) {
            doLayout(root, rootLayout.getX() + rootLayout.getWidth() + H_GAP,
                    rootLayout.getY() + rootLayout.getHeight() / 2, true, result);
        } else if ("Left-Flow".equals(layoutType)) {
            doLayout(root, rootLayout.getX() - H_GAP,
                    rootLayout.getY() + rootLayout.getHeight() / 2, false, result);
        }
        return result;
    }
}