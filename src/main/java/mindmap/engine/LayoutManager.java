package mindmap.engine;

import mindmap.model.MindNode;
import java.awt.FontMetrics;

public interface LayoutManager {
    void calculateLayout(MindNode root, FontMetrics fm, String layoutType);
}