package mindmap.engine;

import mindmap.model.MindNode;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

public class AutoLayoutEngine implements LayoutManager {
    protected static final int H_GAP = 80;
    protected static final int V_GAP = 25;
    protected static final int PADDING_X = 18;
    protected static final int PADDING_Y = 12;

    @Override
    public void calculateLayout(MindNode root, FontMetrics fm, String layoutType) {
        layoutNodeSizes(root, fm);
        root.setX(-root.getWidth() / 2);
        root.setY(-root.getHeight() / 2);

        List<MindNode> rightSide = new ArrayList<>();
        List<MindNode> leftSide = new ArrayList<>();
        for (int i = 0; i < root.getChildren().size(); i++) {
            if (i % 2 == 0) rightSide.add(root.getChildren().get(i));
            else leftSide.add(root.getChildren().get(i));
        }
        
        layoutSubtrees(rightSide, root.getX() + root.getWidth() + H_GAP, root.getY() + root.getHeight() / 2, true);
        layoutSubtrees(leftSide, root.getX() - H_GAP, root.getY() + root.getHeight() / 2, false);
    }

    protected void layoutNodeSizes(MindNode node, FontMetrics fm) {
        node.setWidth(fm.stringWidth(node.getText()) + PADDING_X * 2);
        node.setHeight(fm.getHeight() + PADDING_Y * 2);
        for (MindNode c : node.getChildren()) layoutNodeSizes(c, fm);
    }

    protected void layoutSubtrees(List<MindNode> list, int startX, int centerY, boolean isRight) {
        if (list.isEmpty()) return;
        
        int totalH = 0;
        int[] heights = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            heights[i] = getSubtreeHeight(list.get(i));
            totalH += heights[i];
        }
        totalH += (list.size() - 1) * V_GAP;

        int currentY = centerY - totalH / 2; 
        
        for (int i = 0; i < list.size(); i++) {
            MindNode n = list.get(i);
            int nodeH = heights[i];
            
            n.setX(isRight ? startX : startX - n.getWidth());
            n.setY(currentY + nodeH / 2 - n.getHeight() / 2);

            int nextX = isRight ? n.getX() + n.getWidth() + H_GAP : n.getX() - H_GAP;
            doLayout(n, nextX, n.getY() + n.getHeight() / 2, isRight);
            
            currentY += nodeH + V_GAP;
        }
    }

    protected void doLayout(MindNode parent, int startX, int centerY, boolean isRight) {
        layoutSubtrees(parent.getChildren(), startX, centerY, isRight);
    }

    protected int getSubtreeHeight(MindNode node) {
        if (node.getChildren().isEmpty()) return node.getHeight();
        int h = 0;
        for (MindNode c : node.getChildren()) h += getSubtreeHeight(c);
        h += (node.getChildren().size() - 1) * V_GAP;
        return Math.max(h, node.getHeight());
    }
}