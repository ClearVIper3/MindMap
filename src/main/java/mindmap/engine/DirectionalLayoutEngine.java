package mindmap.engine;

import mindmap.model.MindNode;
import java.awt.FontMetrics;

public class DirectionalLayoutEngine extends AutoLayoutEngine {
    @Override
    public void calculateLayout(MindNode root, FontMetrics fm, String layoutType) {
        layoutNodeSizes(root, fm);
        root.setX(-root.getWidth() / 2);
        root.setY(-root.getHeight() / 2);

        if ("Right-Flow".equals(layoutType)) {
            doLayout(root, root.getX() + root.getWidth() + H_GAP, root.getY() + root.getHeight() / 2, true);
        } else if ("Left-Flow".equals(layoutType)) {
            doLayout(root, root.getX() - H_GAP, root.getY() + root.getHeight() / 2, false);
        }
    }
}