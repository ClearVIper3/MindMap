package mindmap.util;

import mindmap.engine.LayoutResult;
import mindmap.model.MindNode;
import mindmap.ui.DrawingPanel;
import mindmap.ui.MindMapRenderer;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileHandler {

    public static void saveModel(MindNode root, File file) throws Exception {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(root);
        }
    }

    public static MindNode loadModel(File file) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            MindNode root = (MindNode) ois.readObject();
            restoreParentRefs(root);
            return root;
        }
    }

    private static void restoreParentRefs(MindNode node) {
        for (MindNode c : node.getChildren()) {
            c.setParent(node);
            restoreParentRefs(c);
        }
    }

    /**
     * 导出为图片。先计算一次离屏布局（避免依赖屏幕上的 DrawingPanel 内部状态），
     * 再通过通用 Renderer 画到 BufferedImage。
     */
    public static void exportImage(MindNode root, DrawingPanel drawingPanel, File file, Color bgColor) throws Exception {
        // 为了测量字体需要一个 Graphics2D；先建一张 1x1 临时图像拿 FontMetrics。
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D probeG = probe.createGraphics();
        try {
            probeG.setFont(probeG.getFont().deriveFont(Font.BOLD, 14f));
            LayoutResult layout = drawingPanel.computeLayout(probeG.getFontMetrics());
            Rectangle bounds = layout.computeBounds(root);
            if (bounds == null) throw new IllegalStateException("Empty mind map, nothing to export");

            int margin = 50;
            BufferedImage img = new BufferedImage(
                    bounds.width + margin * 2,
                    bounds.height + margin * 2,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img.createGraphics();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2.setColor(bgColor);
                g2.fillRect(0, 0, img.getWidth(), img.getHeight());
                g2.translate(-bounds.x + margin, -bounds.y + margin);

                MindMapRenderer.drawConnections(g2, root, layout);
                MindMapRenderer.drawNodes(g2, root, layout, null);
            } finally {
                g2.dispose();
            }

            String ext = "png";
            String lower = file.getName().toLowerCase();
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) ext = "jpg";
            ImageIO.write(img, ext, file);
        } finally {
            probeG.dispose();
        }
    }
}