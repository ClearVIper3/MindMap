package mindmap.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.util.Map;

/** 纯绘制：屏幕和图片导出共用。 */
public final class Renderer {
    public static final Color BG = new Color(245, 247, 250);
    public static final Color BLUE = new Color(74, 144, 226);
    public static final Color SEL_BG = new Color(225, 238, 252);
    public static final Color SEL_BD = new Color(208, 2, 27);
    public static final Color TEXT = new Color(50, 50, 50);

    private Renderer() {}

    public static void render(Graphics2D g, MindNode root, Map<MindNode, Rectangle> ly, MindNode sel) {
        drawConn(g, root, ly);
        drawNodes(g, root, ly, sel);
    }

    private static void drawConn(Graphics2D g, MindNode n, Map<MindNode, Rectangle> ly) {
        g.setStroke(new BasicStroke(1.8f));
        g.setColor(BLUE);
        Rectangle p = ly.get(n);
        if (p == null) return;
        for (MindNode c : n.getChildren()) {
            Rectangle cr = ly.get(c);
            if (cr == null) continue;
            boolean right = cr.x > p.x;
            int sx = right ? p.x + p.width : p.x, sy = p.y + p.height / 2;
            int ex = right ? cr.x : cr.x + cr.width, ey = cr.y + cr.height / 2;
            int half = (right ? 1 : -1) * Layout.H_GAP / 2;
            Path2D path = new Path2D.Double();
            path.moveTo(sx, sy);
            path.curveTo(sx + half, sy, ex - half, ey, ex, ey);
            g.draw(path);
            drawConn(g, c, ly);
        }
    }

    private static void drawNodes(Graphics2D g, MindNode n, Map<MindNode, Rectangle> ly, MindNode sel) {
        for (MindNode c : n.getChildren()) drawNodes(g, c, ly, sel);
        Rectangle l = ly.get(n);
        if (l == null) return;
        boolean s = (n == sel);
        g.setColor(s ? SEL_BG : Color.WHITE);
        g.fillRoundRect(l.x, l.y, l.width, l.height, 14, 14);
        g.setStroke(new BasicStroke(s ? 2.5f : 1.5f));
        g.setColor(s ? SEL_BD : BLUE);
        g.drawRoundRect(l.x, l.y, l.width, l.height, 14, 14);
        g.setColor(TEXT);
        g.setFont(g.getFont().deriveFont(s ? Font.BOLD : Font.PLAIN, 14f));
        g.drawString(n.getText(), l.x + 18, l.y + 12 + g.getFontMetrics().getAscent());
    }
}
