package mindmap.engine;

import mindmap.model.MindNode;

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

public class AutoLayoutEngine implements LayoutEngine {
    protected static final int H_GAP = 80;
    protected static final int V_GAP = 25;
    protected static final int PADDING_X = 18;
    protected static final int PADDING_Y = 12;

    @Override
    public LayoutResult calculateLayout(MindNode root, FontMetrics fm, String layoutType) {
        LayoutResult result = new LayoutResult();
        layoutNodeSizes(root, fm, result);

        NodeLayout rootLayout = result.getOrCreate(root);
        rootLayout.setX(-rootLayout.getWidth() / 2);
        rootLayout.setY(-rootLayout.getHeight() / 2);

        List<MindNode> rightSide = new ArrayList<>();
        List<MindNode> leftSide = new ArrayList<>();
        for (int i = 0; i < root.getChildren().size(); i++) {
            if (i % 2 == 0) rightSide.add(root.getChildren().get(i));
            else leftSide.add(root.getChildren().get(i));
        }

        layoutSubtrees(rightSide, rootLayout.getX() + rootLayout.getWidth() + H_GAP,
                rootLayout.getY() + rootLayout.getHeight() / 2, true, result);
        layoutSubtrees(leftSide, rootLayout.getX() - H_GAP,
                rootLayout.getY() + rootLayout.getHeight() / 2, false, result);

        return result;
    }

    protected void layoutNodeSizes(MindNode node, FontMetrics fm, LayoutResult result) {
        NodeLayout layout = result.getOrCreate(node);
        layout.setWidth(fm.stringWidth(node.getText()) + PADDING_X * 2);
        layout.setHeight(fm.getHeight() + PADDING_Y * 2);
        for (MindNode c : node.getChildren()) layoutNodeSizes(c, fm, result);
    }

    protected void layoutSubtrees(List<MindNode> list, int startX, int centerY,
                                  boolean isRight, LayoutResult result) {
        if (list.isEmpty()) return;

        int totalH = 0;
        int[] heights = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            heights[i] = getSubtreeHeight(list.get(i), result);
            totalH += heights[i];
        }
        totalH += (list.size() - 1) * V_GAP;

        int currentY = centerY - totalH / 2;

        for (int i = 0; i < list.size(); i++) {
            MindNode n = list.get(i);
            NodeLayout nLayout = result.getOrCreate(n);
            int nodeH = heights[i];

            nLayout.setX(isRight ? startX : startX - nLayout.getWidth());
            nLayout.setY(currentY + nodeH / 2 - nLayout.getHeight() / 2);

            int nextX = isRight
                    ? nLayout.getX() + nLayout.getWidth() + H_GAP
                    : nLayout.getX() - H_GAP;
            doLayout(n, nextX, nLayout.getY() + nLayout.getHeight() / 2, isRight, result);

            currentY += nodeH + V_GAP;
        }
    }

    protected void doLayout(MindNode parent, int startX, int centerY,
                            boolean isRight, LayoutResult result) {
        layoutSubtrees(parent.getChildren(), startX, centerY, isRight, result);
    }

    protected int getSubtreeHeight(MindNode node, LayoutResult result) {
        NodeLayout layout = result.getOrCreate(node);
        if (node.getChildren().isEmpty()) return layout.getHeight();
        int h = 0;
        for (MindNode c : node.getChildren()) h += getSubtreeHeight(c, result);
        h += (node.getChildren().size() - 1) * V_GAP;
        return Math.max(h, layout.getHeight());
    }
}