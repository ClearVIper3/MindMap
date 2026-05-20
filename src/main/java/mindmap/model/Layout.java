package mindmap.model;

import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 布局算法：根据布局类型计算每个节点的矩形包围盒。 */
public final class Layout {
    public static final String[] TYPES = { "Balanced", "Right-Flow", "Left-Flow" };
    public static final String DEFAULT = TYPES[0];
    public static final int H_GAP = 80;
    static final int V_GAP = 25, PAD_X = 18, PAD_Y = 12;

    private Layout() {}

    public static Map<MindNode, Rectangle> compute(MindNode root, FontMetrics fm, String type) {
        Map<MindNode, Rectangle> r = new HashMap<>();
        sizes(root, fm, r);
        Rectangle rl = r.get(root);
        rl.x = -rl.width / 2;
        rl.y = -rl.height / 2;
        int sx = rl.x + rl.width + H_GAP, lx = rl.x - H_GAP, cy = rl.y + rl.height / 2;

        if ("Right-Flow".equals(type)) {
            place(root.getChildren(), sx, cy, true, r);
        } else if ("Left-Flow".equals(type)) {
            place(root.getChildren(), lx, cy, false, r);
        } else {
            // Balanced：交替分到左右两侧
            java.util.List<MindNode> right = new java.util.ArrayList<>(), left = new java.util.ArrayList<>();
            List<MindNode> ch = root.getChildren();
            for (int i = 0; i < ch.size(); i++) (i % 2 == 0 ? right : left).add(ch.get(i));
            place(right, sx, cy, true, r);
            place(left, lx, cy, false, r);
        }
        return r;
    }

    private static void sizes(MindNode n, FontMetrics fm, Map<MindNode, Rectangle> r) {
        r.put(n, new Rectangle(0, 0, fm.stringWidth(n.getText()) + PAD_X * 2, fm.getHeight() + PAD_Y * 2));
        for (MindNode c : n.getChildren()) sizes(c, fm, r);
    }

    private static void place(List<MindNode> list, int startX, int centerY, boolean right, Map<MindNode, Rectangle> r) {
        if (list.isEmpty()) return;
        int[] hs = new int[list.size()];
        int total = 0;
        for (int i = 0; i < list.size(); i++) { hs[i] = subtreeH(list.get(i), r); total += hs[i]; }
        total += (list.size() - 1) * V_GAP;
        int y = centerY - total / 2;
        for (int i = 0; i < list.size(); i++) {
            MindNode n = list.get(i);
            Rectangle nr = r.get(n);
            nr.x = right ? startX : startX - nr.width;
            nr.y = y + hs[i] / 2 - nr.height / 2;
            int nx = right ? nr.x + nr.width + H_GAP : nr.x - H_GAP;
            place(n.getChildren(), nx, nr.y + nr.height / 2, right, r);
            y += hs[i] + V_GAP;
        }
    }

    private static int subtreeH(MindNode n, Map<MindNode, Rectangle> r) {
        Rectangle nr = r.get(n);
        if (n.getChildren().isEmpty()) return nr.height;
        int h = 0;
        for (MindNode c : n.getChildren()) h += subtreeH(c, r);
        h += (n.getChildren().size() - 1) * V_GAP;
        return Math.max(h, nr.height);
    }

    /** 计算整棵子树的包围盒。 */
    public static Rectangle bounds(MindNode root, Map<MindNode, Rectangle> r) {
        Rectangle b = new Rectangle(r.get(root));
        addAll(root, r, b);
        return b;
    }

    private static void addAll(MindNode n, Map<MindNode, Rectangle> r, Rectangle b) {
        for (MindNode c : n.getChildren()) { b.add(r.get(c)); addAll(c, r, b); }
    }
}
