package mindmap.ui;

import mindmap.model.MindMapModel;
import mindmap.model.MindNode;
import mindmap.engine.LayoutManager;
import mindmap.engine.AutoLayoutEngine;
import mindmap.engine.DirectionalLayoutEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

public class DrawingPanel extends JPanel {
    private MindMapModel model;
    
    private double scale = 1.0;
    private double translateX = 0, translateY = 0;
    private Point lastMousePt;
    private boolean layoutDirty = true;
    
    private static final Color MAIN_BLUE = new Color(74, 144, 226);
    private static final Color BG_COLOR = new Color(245, 247, 250);
    private static final Color SEL_BG_COLOR = new Color(225, 238, 252);
    private static final Color SEL_BORDER_COLOR = new Color(208, 2, 27);
    private static final Color TEXT_COLOR = new Color(50, 50, 50);

    public DrawingPanel(MindMapModel model) {
        this.model = model;
        setBackground(BG_COLOR);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        model.addChangeListener(() -> {
            layoutDirty = true;
            repaint();
        });

        initInteractions();
    }
    
    public Color getBgColor() { return BG_COLOR; }
    
    public void resetTransform() {
        translateX = getWidth() / 2.0;
        translateY = getHeight() / 2.0;
        scale = 1.0;
        repaint();
    }

    private void initInteractions() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastMousePt = e.getPoint();
                int worldX = (int) ((e.getX() - translateX) / scale);
                int worldY = (int) ((e.getY() - translateY) / scale);
                
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
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (layoutDirty && model.getRoot() != null) {
            LayoutManager layoutEngine = "Balanced".equals(model.getCurrentLayout()) ? 
                new AutoLayoutEngine() : new DirectionalLayoutEngine();
            
            FontMetrics fm = g2.getFontMetrics(g2.getFont().deriveFont(Font.BOLD, 14f));
            layoutEngine.calculateLayout(model.getRoot(), fm, model.getCurrentLayout());
            layoutDirty = false;
        }

        AffineTransform at = new AffineTransform();
        at.translate(translateX, translateY);
        at.scale(scale, scale);
        g2.setTransform(at);

        if (model.getRoot() != null) {
            drawConnections(g2, model.getRoot());
            drawNodes(g2, model.getRoot());
        }
    }

    public void drawConnections(Graphics2D g2, MindNode node) {
        g2.setStroke(new BasicStroke(1.8f));
        g2.setColor(MAIN_BLUE);
        int H_GAP = 80;
        
        for (MindNode child : node.getChildren()) {
            boolean childIsRight = child.getX() > node.getX();
            int startX = childIsRight ? node.getX() + node.getWidth() : node.getX();
            int startY = node.getY() + node.getHeight() / 2;
            int endX = childIsRight ? child.getX() : child.getX() + child.getWidth();
            int endY = child.getY() + child.getHeight() / 2;

            int ctrlX1 = startX + (childIsRight ? H_GAP/2 : -H_GAP/2);
            int ctrlX2 = endX - (childIsRight ? H_GAP/2 : -H_GAP/2);

            Path2D path = new Path2D.Double();
            path.moveTo(startX, startY);
            path.curveTo(ctrlX1, startY, ctrlX2, endY, endX, endY);
            g2.draw(path);
            
            drawConnections(g2, child);
        }
    }

    public void drawNodes(Graphics2D g2, MindNode node) {
        for (MindNode child : node.getChildren()) drawNodes(g2, child);

        boolean isSel = (node == model.getSelectedNode());
        g2.setColor(isSel ? SEL_BG_COLOR : Color.WHITE);
        g2.fillRoundRect(node.getX(), node.getY(), node.getWidth(), node.getHeight(), 14, 14);
        
        g2.setStroke(new BasicStroke(isSel ? 2.5f : 1.5f));
        g2.setColor(isSel ? SEL_BORDER_COLOR : MAIN_BLUE);
        g2.drawRoundRect(node.getX(), node.getY(), node.getWidth(), node.getHeight(), 14, 14);
        
        g2.setColor(TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(isSel ? Font.BOLD : Font.PLAIN, 14f));
        FontMetrics fm = g2.getFontMetrics();
        int tx = node.getX() + 18;
        int ty = node.getY() + 12 + fm.getAscent();
        g2.drawString(node.getText(), tx, ty);
    }

    private MindNode findNodeAt(MindNode node, int mx, int my) {
        if (mx >= node.getX() && mx <= node.getX() + node.getWidth() && 
            my >= node.getY() && my <= node.getY() + node.getHeight()) {
            return node;
        }
        for (MindNode child : node.getChildren()) {
            MindNode found = findNodeAt(child, mx, my);
            if (found != null) return found;
        }
        return null;
    }
}