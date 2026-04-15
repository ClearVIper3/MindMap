package mindmap.util;

import mindmap.model.MindNode;
import mindmap.ui.DrawingPanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

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

    public static void exportImage(MindNode root, DrawingPanel drawingPanel, File file, Color bgColor) throws Exception {
        Rectangle bounds = getTreeBounds(root, new Rectangle(root.getX(), root.getY(), root.getWidth(), root.getHeight()));
        int margin = 50;
        
        BufferedImage img = new BufferedImage(bounds.width + margin * 2, bounds.height + margin * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        g2.setColor(bgColor);
        g2.fillRect(0, 0, img.getWidth(), img.getHeight());
        g2.translate(-bounds.x + margin, -bounds.y + margin);
        
        drawingPanel.drawConnections(g2, root);
        drawingPanel.drawNodes(g2, root);
        g2.dispose();

        String ext = "png";
        if (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".jpeg")) {
            ext = "jpg";
        }
        ImageIO.write(img, ext, file);
    }
    
    private static Rectangle getTreeBounds(MindNode node, Rectangle rect) {
        rect.add(new Rectangle(node.getX(), node.getY(), node.getWidth(), node.getHeight()));
        for(MindNode c : node.getChildren()) getTreeBounds(c, rect);
        return rect;
    }
}