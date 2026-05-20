package mindmap.util;

import mindmap.model.Layout;
import mindmap.model.MindNode;
import mindmap.model.Renderer;

import javax.imageio.ImageIO;
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
import java.util.Map;

public class FileHandler {
    public static void save(MindNode root, File f) throws Exception {
        try (ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(f))) { o.writeObject(root); }
    }

    public static MindNode load(File f) throws Exception {
        try (ObjectInputStream i = new ObjectInputStream(new FileInputStream(f))) {
            MindNode root = (MindNode) i.readObject();
            root.restoreParents();
            return root;
        }
    }

    public static void exportImage(MindNode root, String layoutType, File f) throws Exception {
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D pg = probe.createGraphics();
        pg.setFont(pg.getFont().deriveFont(Font.BOLD, 14f));
        Map<MindNode, Rectangle> ly = Layout.compute(root, pg.getFontMetrics(), layoutType);
        pg.dispose();

        Rectangle b = Layout.bounds(root, ly);
        int m = 50;
        BufferedImage img = new BufferedImage(b.width + m * 2, b.height + m * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Renderer.BG); g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.translate(-b.x + m, -b.y + m);
        Renderer.render(g, root, ly, null);
        g.dispose();

        String n = f.getName().toLowerCase();
        ImageIO.write(img, n.endsWith(".jpg") || n.endsWith(".jpeg") ? "jpg" : "png", f);
    }
}
