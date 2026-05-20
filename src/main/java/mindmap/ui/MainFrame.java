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
    private MindNode root, selected;
    private String layoutType = Layout.DEFAULT, fileName = "Untitled.dt";
    private final List<Runnable> listeners = new ArrayList<>();
    private final DrawPanel canvas = new DrawPanel();
    private final TreePanel treePanel = new TreePanel();
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
        root = new MindNode("Java Core Technology");
        for (String s : new String[]{"Collections", "Concurrency", "JVM Architecture", "I/O & NIO"})
            root.addChild(new MindNode(s));
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
        String t = prompt("Node Text:", title, "New Node");
        if (t != null && !t.trim().isEmpty()) mutate(() -> action.accept(t.trim()));
    }

    private JToolBar buildToolbar() {
        JToolBar bar = new JToolBar();
        bar.setBackground(Color.WHITE); bar.setFloatable(false);
        bar.setBorder(new EmptyBorder(10, 10, 10, 10));

        addBtn(bar, "New", e -> newMap());
        addBtn(bar, "Open", e -> doOpen());
        addBtn(bar, "Save (.dt)", e -> doSave());
        bar.add(Box.createHorizontalStrut(10));
        addBtn(bar, "Export Image", e -> doExport());
        bar.add(Box.createHorizontalStrut(10));
        addBtn(bar, "+ Child", e -> editText("Add Child", t -> selected.addChild(new MindNode(t))));
        addBtn(bar, "+ Sibling", e -> {
            if (selected != null && selected.getParent() != null)
                editText("Add Sibling", t -> selected.addSiblingAfter(new MindNode(t)));
        });
        addBtn(bar, "Rename", e -> {
            if (selected == null) return;
            String t = prompt("Rename Node:", "Rename", selected.getText());
            if (t != null && !t.trim().isEmpty()) mutate(() -> selected.setText(t.trim()));
        });
        addBtn(bar, "Delete", e -> {
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
        double scale = 1.0, tx = 0, ty = 0;
        Point lastPt;
        boolean layoutDirty = true;
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
            addMouseWheelListener(e -> {
                double os = scale;
                scale = Math.max(0.2, Math.min(5.0, e.getWheelRotation() < 0 ? scale * 1.1 : scale / 1.1));
                double k = scale / os;
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
        Map<MindNode, DefaultMutableTreeNode> map = new IdentityHashMap<>();
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
