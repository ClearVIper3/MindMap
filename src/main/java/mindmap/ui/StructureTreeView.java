package mindmap.ui;

import mindmap.model.ChangeType;
import mindmap.model.MindMapModel;
import mindmap.model.MindNode;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.IdentityHashMap;
import java.util.Map;

public class StructureTreeView extends JPanel {
    private final MindMapModel model;
    private final JTree tree;

    /** MindNode -> 对应 JTree 节点的映射。由本视图自行维护，不再污染 Model。 */
    private Map<MindNode, DefaultMutableTreeNode> nodeMapping = new IdentityHashMap<>();

    /** 区分"程序同步 UI"与"用户点击 UI"，防止事件回环。 */
    private boolean syncingFromModel = false;

    public StructureTreeView(MindMapModel model) {
        this.model = model;
        setLayout(new BorderLayout());

        tree = new JTree();
        tree.setBorder(new EmptyBorder(10, 10, 10, 10));
        tree.setShowsRootHandles(true);
        tree.setBackground(Color.WHITE);
        tree.setCellRenderer(new MindTreeCellRenderer());

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(200, 200, 200)));
        add(scrollPane, BorderLayout.CENTER);

        tree.addTreeSelectionListener(e -> {
            if (syncingFromModel) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof MindNode) {
                model.setSelectedNode((MindNode) node.getUserObject());
            }
        });

        model.addChangeListener(this::onModelChanged);
    }

    private void onModelChanged(ChangeType type) {
        switch (type) {
            case STRUCTURE_CHANGED:
                rebuildTree();
                syncSelection();
                break;
            case SELECTION_CHANGED:
                syncSelection();
                break;
            case LAYOUT_CHANGED:
            case FILE_CHANGED:
            default:
                // 不影响树结构
                break;
        }
    }

    private void rebuildTree() {
        syncingFromModel = true;
        try {
            nodeMapping = new IdentityHashMap<>();
            DefaultMutableTreeNode treeRoot = buildTreeModel(model.getRoot());
            tree.setModel(new DefaultTreeModel(treeRoot));
            for (int i = 0; i < tree.getRowCount(); i++) tree.expandRow(i);
        } finally {
            syncingFromModel = false;
        }
    }

    private void syncSelection() {
        MindNode selNode = model.getSelectedNode();
        if (selNode == null) return;
        DefaultMutableTreeNode tn = nodeMapping.get(selNode);
        if (tn == null) return;
        syncingFromModel = true;
        try {
            TreePath path = new TreePath(tn.getPath());
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
        } finally {
            syncingFromModel = false;
        }
    }

    private DefaultMutableTreeNode buildTreeModel(MindNode node) {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);
        nodeMapping.put(node, treeNode);
        for (MindNode child : node.getChildren()) {
            treeNode.add(buildTreeModel(child));
        }
        return treeNode;
    }

    private static class MindTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(javax.swing.JTree tree, Object value,
                                                      boolean sel, boolean expanded, boolean leaf,
                                                      int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof DefaultMutableTreeNode) {
                Object uo = ((DefaultMutableTreeNode) value).getUserObject();
                if (uo instanceof MindNode) {
                    setText(((MindNode) uo).getText());
                }
            }
            return this;
        }
    }
}