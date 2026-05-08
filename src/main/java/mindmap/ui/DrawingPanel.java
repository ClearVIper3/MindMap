package mindmap.ui;

import mindmap.engine.LayoutEngine;
import mindmap.engine.LayoutEngineFactory;
import mindmap.engine.LayoutResult;
import mindmap.engine.NodeLayout;
import mindmap.model.ChangeType;
import mindmap.model.MindMapModel;
import mindmap.model.MindNode;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;

public class DrawingPanel extends JPanel {
    private final MindMapModel model;

    private double scale = 1.0;
    private double translateX = 0, translateY = 0;
    private Point lastMousePt;

    private boolean layoutDirty = true;
    private LayoutResult layoutResult;

    private static final Color BG_COLOR = new Color(245, 247, 250);

    public DrawingPanel(MindMapModel model) {
        this.model = model;
        setBackground(BG_COLOR);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 只有结构或布局策略变化时才需要重算布局；选中变化只重绘。
        model.addChangeListener(type -> {
            if (type == ChangeType.STRUCTURE_CHANGED || type == ChangeType.LAYOUT_CHANGED) {
                layoutDirty = true;
            }
            if (type != ChangeType.FILE_CHANGED) {
                repaint();
            }
        });

        initInteractions();
    }

    public Color getBgColor() { return BG_COLOR; }

    /** 提供给外部（如 exportImage）的布局结果访问。若尚未计算，返回 null。 */
    public LayoutResult getLayoutResult() { return layoutResult; }

    /** 供导出图片时使用：在给定 FontMetrics 下同步计算一次布局并返回结果。 */
    public LayoutResult computeLayout(FontMetrics fm) {
        LayoutEngine engine = LayoutEngineFactory.create(model.getCurrentLayout());
        layoutResult = engine.calculateLayout(model.getRoot(), fm, model.getCurrentLayout());
        layoutDirty = false;
        return layoutResult;
    }

    /**
     * 重置视图变换：把原点移动到面板中心。
     * 若当前面板尚未显示（宽高为 0），则延迟到下一次组件尺寸变化时再归位，
     * 避免把画布挪到左上角不可见的位置。
     */
    public void resetTransform() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            // 等组件被布局后再归位
            translateX = 0;
            translateY = 0;
        } else {
            translateX = w / 2.0;
            translateY = h / 2.0;
        }
        scale = 1.0;
        repaint();
    }

    private void initInteractions() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastMousePt = e.getPoint();
                int worldX = (int) ((e.getX() - translateX) / scale);
                int worldY = (int) ((e.getY() - translateY) / scale);

                if (layoutResult == null) return;
                MindNode clicked = findNodeAt(model.getRoot(), worldX, worldY);
                if (clicked != null) {
                    model.setSelectedNode(clicked);
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (lastMousePt != null) {
                    translateX += e.getX() - lastMousePt.x;
                    translateY += e.getY() - lastMousePt.y;
                    lastMousePt = e.getPoint();
                    repaint();
                }
            }
        });

        addMouseWheelListener(e -> {
            double oldScale = scale;
            if (e.getWheelRotation() < 0) scale *= 1.1;
            else scale /= 1.1;
            scale = Math.max(0.2, Math.min(scale, 5.0));
            double scaleChange = scale / oldScale;
            translateX = e.getX() - scaleChange * (e.getX() - translateX);
            translateY = e.getY() - scaleChange * (e.getY() - translateY);
            repaint();
        });

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (translateX == 0 && translateY == 0 && getWidth() > 0) {
                    translateX = getWidth() / 2.0;
                    translateY = getHeight() / 2.0;
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if ((layoutDirty || layoutResult == null) && model.getRoot() != null) {
                FontMetrics fm = g2.getFontMetrics(g2.getFont().deriveFont(Font.BOLD, 14f));
                LayoutEngine engine = LayoutEngineFactory.create(model.getCurrentLayout());
                layoutResult = engine.calculateLayout(model.getRoot(), fm, model.getCurrentLayout());
                layoutDirty = false;
            }

            AffineTransform original = g2.getTransform();
            AffineTransform at = new AffineTransform(original);
            at.translate(translateX, translateY);
            at.scale(scale, scale);
            g2.setTransform(at);

            if (model.getRoot() != null && layoutResult != null) {
                MindMapRenderer.drawConnections(g2, model.getRoot(), layoutResult);
                MindMapRenderer.drawNodes(g2, model.getRoot(), layoutResult, model.getSelectedNode());
            }

            g2.setTransform(original);
        } finally {
            g2.dispose();
        }
    }

    private MindNode findNodeAt(MindNode node, int mx, int my) {
        NodeLayout l = layoutResult.get(node);
        if (l != null
                && mx >= l.getX() && mx <= l.getX() + l.getWidth()
                && my >= l.getY() && my <= l.getY() + l.getHeight()) {
            return node;
        }
        for (MindNode child : node.getChildren()) {
            MindNode found = findNodeAt(child, mx, my);
            if (found != null) return found;
        }
        return null;
    }
}