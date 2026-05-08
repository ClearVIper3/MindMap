package mindmap.ui;

import mindmap.engine.LayoutResult;
import mindmap.engine.NodeLayout;
import mindmap.model.MindNode;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

/**
 * 纯粹的绘制器：根据 LayoutResult 把思维导图画到任意 Graphics2D 上。
 * 既服务于 DrawingPanel 的屏幕绘制，也服务于 FileHandler.exportImage 的离屏导出，
 * 使得 util 层不再依赖 ui 层的具体组件。
 */
public final class MindMapRenderer {
    private static final int H_GAP = 80;

    private static final Color MAIN_BLUE = new Color(74, 144, 226);
    private static final Color SEL_BG_COLOR = new Color(225, 238, 252);
    private static final Color SEL_BORDER_COLOR = new Color(208, 2, 27);
    private static final Color TEXT_COLOR = new Color(50, 50, 50);

    private MindMapRenderer() {}

    public static void drawConnections(Graphics2D g2, MindNode node, LayoutResult layout) {
        g2.setStroke(new BasicStroke(1.8f));
        g2.setColor(MAIN_BLUE);
        NodeLayout pl = layout.get(node);
        if (pl == null) return;

        for (MindNode child : node.getChildren()) {
            NodeLayout cl = layout.get(child);
            if (cl == null) continue;

            boolean childIsRight = cl.getX() > pl.getX();
            int startX = childIsRight ? pl.getX() + pl.getWidth() : pl.getX();
            int startY = pl.getY() + pl.getHeight() / 2;
            int endX = childIsRight ? cl.getX() : cl.getX() + cl.getWidth();
            int endY = cl.getY() + cl.getHeight() / 2;

            int ctrlX1 = startX + (childIsRight ? H_GAP / 2 : -H_GAP / 2);
            int ctrlX2 = endX - (childIsRight ? H_GAP / 2 : -H_GAP / 2);

            Path2D path = new Path2D.Double();
            path.moveTo(startX, startY);
            path.curveTo(ctrlX1, startY, ctrlX2, endY, endX, endY);
            g2.draw(path);

            drawConnections(g2, child, layout);
        }
    }

    public static void drawNodes(Graphics2D g2, MindNode node, LayoutResult layout, MindNode selected) {
        for (MindNode child : node.getChildren()) drawNodes(g2, child, layout, selected);

        NodeLayout l = layout.get(node);
        if (l == null) return;

        boolean isSel = (node == selected);
        g2.setColor(isSel ? SEL_BG_COLOR : Color.WHITE);
        g2.fillRoundRect(l.getX(), l.getY(), l.getWidth(), l.getHeight(), 14, 14);

        g2.setStroke(new BasicStroke(isSel ? 2.5f : 1.5f));
        g2.setColor(isSel ? SEL_BORDER_COLOR : MAIN_BLUE);
        g2.drawRoundRect(l.getX(), l.getY(), l.getWidth(), l.getHeight(), 14, 14);

        g2.setColor(TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(isSel ? Font.BOLD : Font.PLAIN, 14f));
        FontMetrics fm = g2.getFontMetrics();
        int tx = l.getX() + 18;
        int ty = l.getY() + 12 + fm.getAscent();
        g2.drawString(node.getText(), tx, ty);
    }
}
