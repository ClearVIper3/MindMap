package mindmap.ui;

import mindmap.model.Layout;
import mindmap.model.MindNode;
import mindmap.model.Renderer;
import mindmap.util.FileHandler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {
    /** root: 思维导图的根节点（整棵树的入口）; selected: 当前用户选中的节点 */
    private MindNode root, selected;
    /** layoutType: 当前布局模式名称; fileName: 当前文件名（用于标题栏和保存） */
    private String layoutType = Layout.DEFAULT, fileName = "Untitled.dt";
    /** listeners: 观察者模式——模型变更时需要通知的回调列表（画布重绘、树同步、状态栏更新等） */
    private final List<Runnable> listeners = new ArrayList<>();
    /** canvas: 思维导图的可视化绘制面板（支持缩放、平移、点击选中） */
    private final DrawPanel canvas = new DrawPanel();
    /** treePanel: 右侧JTree大纲面板，与画布双向同步选中状态 */
    private final TreePanel treePanel = new TreePanel();
    /** status: 底部状态栏标签，显示文件名、布局模式等信息 */
    private final JLabel status = new JLabel();

    public MainFrame() {
        setTitle("Java Core Technology - Mind Mapping Tool");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        newMap();

        treePanel.setPreferredSize(new Dimension(250, 0));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas, treePanel);
        split.setResizeWeight(1.0); split.setDividerSize(4); split.setBorder(null);
        status.setBorder(new EmptyBorder(5, 10, 5, 10));
        status.setForeground(Color.GRAY);

        add(buildToolbar(), BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        listeners.add(() -> SwingUtilities.invokeLater(() -> status.setText(
                " File: " + fileName + " | Layout: " + layoutType + " | Zoom, Pan, Explore")));
        fire();
    }

    private void fire() { for (Runnable r : listeners) r.run(); }

    /** 修改模型 → 标记重排 → 通知所有监听器。 */
    private void mutate(Runnable r) { r.run(); canvas.layoutDirty = true; fire(); }

    private void newMap() {
        root = new MindNode("孙子兵法");

        MindNode ch1 = new MindNode("第一章 始计");
        ch1.addChild(new MindNode("道"));
        ch1.addChild(new MindNode("天、地"));

        MindNode ch2 = new MindNode("第二章 作战");
        ch2.addChild(new MindNode("兵贵胜"));
        ch2.addChild(new MindNode("粮道与消耗"));

        MindNode ch3 = new MindNode("第三章 谋攻");
        ch3.addChild(new MindNode("上兵伐谋"));
        ch3.addChild(new MindNode("全胜之策"));

        MindNode ch4 = new MindNode("第四章 军形");
        ch4.addChild(new MindNode("不败之形"));
        ch4.addChild(new MindNode("胜可知"));

        MindNode ch5 = new MindNode("第五章 兵势");
        ch5.addChild(new MindNode("奇正之变"));
        ch5.addChild(new MindNode("势如破竹"));

        MindNode ch6 = new MindNode("第六章 虚实");
        ch6.addChild(new MindNode("以实击虚"));
        ch6.addChild(new MindNode("避实而击虚"));

        root.addChild(ch1);
        root.addChild(ch2);
        root.addChild(ch3);
        root.addChild(ch4);
        root.addChild(ch5);
        root.addChild(ch6);

        selected = root;
        fileName = "Untitled.dt";
        canvas.layoutDirty = true;
        fire();
    }

    private String prompt(String msg, String title, String init) {
        return (String) JOptionPane.showInputDialog(this, msg, title, JOptionPane.PLAIN_MESSAGE, null, null, init);
    }

    private void editText(String title, java.util.function.Consumer<String> action) {
        if (selected == null) return;
        String t = prompt("节点文本：", title, "新节点");
        if (t != null && !t.trim().isEmpty()) mutate(() -> action.accept(t.trim()));
    }

    private JToolBar buildToolbar() {
        JToolBar bar = new JToolBar();
        bar.setBackground(Color.WHITE); bar.setFloatable(false);
        bar.setBorder(new EmptyBorder(10, 10, 10, 10));

        addBtn(bar, "新建", e -> newMap());
        addBtn(bar, "打开", e -> doOpen());
        addBtn(bar, "保存 (.dt)", e -> doSave());
        bar.add(Box.createHorizontalStrut(10));
        addBtn(bar, "导出图片", e -> doExport());
        bar.add(Box.createHorizontalStrut(10));
        addBtn(bar, "+ 子节点", e -> editText("新增子节点", t -> selected.addChild(new MindNode(t))));
        addBtn(bar, "+ 兄弟节点", e -> {
            if (selected != null && selected.getParent() != null)
                editText("新增兄弟节点", t -> selected.addSiblingAfter(new MindNode(t)));
        });
        addBtn(bar, "重命名", e -> {
            if (selected == null) return;
            String t = prompt("重命名节点：", "重命名", selected.getText());
            if (t != null && !t.trim().isEmpty()) mutate(() -> selected.setText(t.trim()));
        });
        addBtn(bar, "删除", e -> {
            if (selected != null && selected != root)
                mutate(() -> { selected.remove(); selected = root; });
        });
        bar.add(Box.createHorizontalStrut(10));
        bar.add(new JLabel(" Layout: "));

        JComboBox<String> combo = new JComboBox<>(Layout.TYPES);
        combo.setMaximumSize(new Dimension(120, 30));
        combo.setSelectedItem(layoutType);
        combo.addActionListener(e -> {
            String s = (String) combo.getSelectedItem();
            if (s != null && !s.equals(layoutType)) mutate(() -> layoutType = s);
        });
        bar.add(combo);
        return bar;
    }

    private void addBtn(JToolBar bar, String text, ActionListener al) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(6, 12, 6, 12)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(al);
        bar.add(b);
        bar.add(Box.createHorizontalStrut(5));
    }

    private void doSave() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(fileName));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            File t = fc.getSelectedFile();
            if (!t.getAbsolutePath().endsWith(".dt")) t = new File(t.getAbsolutePath() + ".dt");
            FileHandler.save(root, t); fileName = t.getName(); fire();
            info("Masterpiece saved!");
        } catch (Exception ex) { error("Failed to save: " + ex.getMessage()); }
    }

    private void doOpen() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            root = FileHandler.load(fc.getSelectedFile());
            selected = root; fileName = fc.getSelectedFile().getName();
            canvas.layoutDirty = true; canvas.resetTransform(); fire();
        } catch (Exception ex) { error("Failed to open file: " + ex.getMessage()); }
    }

    private void doExport() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(fileName.replace(".dt", ".png")));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            FileHandler.exportImage(root, layoutType, fc.getSelectedFile());
            info("Image exported successfully!");
        } catch (Exception ex) { error("Export failed: " + ex.getMessage()); }
    }

    private void info(String m) { JOptionPane.showMessageDialog(this, m, "Success", JOptionPane.INFORMATION_MESSAGE); }
    private void error(String m) { JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); }

    // ============================================================
    private class DrawPanel extends JPanel {
        /** scale: 当前缩放倍率（鼠标滚轮控制，范围0.2~5.0） */
        /** tx, ty: 画布平移偏移量（像素），拖拽时累加，用于实现画布平移 */
        double scale = 1.0, tx = 0, ty = 0;
        /** lastPt: 上一次鼠标按下/拖拽的屏幕坐标，用于计算拖拽增量 */
        Point lastPt;
        /** layoutDirty: 脏标记——为true时下次paintComponent会重新计算布局，避免每帧都重算 */
        boolean layoutDirty = true;
        /** ly: 布局计算结果缓存，节点→屏幕矩形的映射（世界坐标系） */
        Map<MindNode, Rectangle> ly;

        DrawPanel() {
            setBackground(Renderer.BG);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            listeners.add(() -> { layoutDirty = true; repaint(); });

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    lastPt = e.getPoint();
                    if (ly == null) return;
                    int wx = (int) ((e.getX() - tx) / scale), wy = (int) ((e.getY() - ty) / scale);
                    MindNode c = findAt(root, wx, wy);
                    if (c != null && c != selected) { selected = c; fire(); }
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (lastPt == null) return;
                    tx += e.getX() - lastPt.x; ty += e.getY() - lastPt.y;
                    lastPt = e.getPoint(); repaint();
                }
            });
            // 鼠标滚轮缩放：以鼠标指针为中心进行缩放（保持指针下的内容不动）
            addMouseWheelListener(e -> {
                double os = scale; // os: 缩放前的旧比例
                scale = Math.max(0.2, Math.min(5.0, e.getWheelRotation() < 0 ? scale * 1.1 : scale / 1.1));
                double k = scale / os; // k: 新旧比例的比值
                // 关键公式：调整平移量使缩放中心固定在鼠标位置（仿射变换不动点公式）
                tx = e.getX() - k * (e.getX() - tx);
                ty = e.getY() - k * (e.getY() - ty);
                repaint();
            });
            addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    if (tx == 0 && ty == 0 && getWidth() > 0) { tx = getWidth() / 2.0; ty = getHeight() / 2.0; }
                }
            });
        }

        void resetTransform() {
            int w = getWidth(), h = getHeight();
            tx = w > 0 ? w / 2.0 : 0; ty = h > 0 ? h / 2.0 : 0;
            scale = 1.0;
            repaint();
        }

        MindNode findAt(MindNode n, int x, int y) {
            for (MindNode c : n.getChildren()) { MindNode f = findAt(c, x, y); if (f != null) return f; }
            Rectangle l = ly.get(n);
            return (l != null && l.contains(x, y)) ? n : null;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                if ((layoutDirty || ly == null) && root != null) {
                    ly = Layout.compute(root, g.getFontMetrics(g.getFont().deriveFont(Font.BOLD, 14f)), layoutType);
                    layoutDirty = false;
                }
                AffineTransform o = g.getTransform();
                AffineTransform at = new AffineTransform(o);
                at.translate(tx, ty); at.scale(scale, scale);
                g.setTransform(at);
                if (root != null && ly != null) Renderer.render(g, root, ly, selected);
                g.setTransform(o);
            } finally { g.dispose(); }
        }
    }

    // ============================================================
    private class TreePanel extends JPanel {
        final JTree tree = new JTree();
        /** map: MindNode→JTree节点的映射（IdentityHashMap按引用比较，避免equals/hashCode干扰） */
        Map<MindNode, DefaultMutableTreeNode> map = new IdentityHashMap<>();
        /** syncing: 同步锁标志——为true时忽略JTree的选中事件，防止程序化更新树时触发死循环回调 */
        boolean syncing = false;

        TreePanel() {
            setLayout(new BorderLayout());
            tree.setBorder(new EmptyBorder(10, 10, 10, 10));
            tree.setShowsRootHandles(true);
            tree.setBackground(Color.WHITE);
            tree.setCellRenderer(new DefaultTreeCellRenderer() {
                public Component getTreeCellRendererComponent(JTree t, Object v, boolean s, boolean ex, boolean lf, int row, boolean fc) {
                    super.getTreeCellRendererComponent(t, v, s, ex, lf, row, fc);
                    if (v instanceof DefaultMutableTreeNode) {
                        Object u = ((DefaultMutableTreeNode) v).getUserObject();
                        if (u instanceof MindNode) setText(((MindNode) u).getText());
                    }
                    return this;
                }
            });
            JScrollPane sp = new JScrollPane(tree);
            sp.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(200, 200, 200)));
            add(sp, BorderLayout.CENTER);

            tree.addTreeSelectionListener(e -> {
                if (syncing) return;
                DefaultMutableTreeNode n = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (n != null && n.getUserObject() instanceof MindNode) {
                    MindNode m = (MindNode) n.getUserObject();
                    if (m != selected) { selected = m; fire(); }
                }
            });

            listeners.add(this::sync);
        }

        void sync() {
            syncing = true;
            try {
                map = new IdentityHashMap<>();
                tree.setModel(new DefaultTreeModel(build(root)));
                for (int i = 0; i < tree.getRowCount(); i++) tree.expandRow(i);
                DefaultMutableTreeNode tn = map.get(selected);
                if (tn != null) {
                    TreePath p = new TreePath(tn.getPath());
                    tree.setSelectionPath(p); tree.scrollPathToVisible(p);
                }
            } finally { syncing = false; }
        }

        DefaultMutableTreeNode build(MindNode n) {
            DefaultMutableTreeNode t = new DefaultMutableTreeNode(n);
            map.put(n, t);
            for (MindNode c : n.getChildren()) t.add(build(c));
            return t;
        }
    }
}
