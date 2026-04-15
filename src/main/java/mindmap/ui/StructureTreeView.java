package mindmap.ui;

import mindmap.model.MindMapModel;
import mindmap.model.MindNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.*;
import java.awt.*;

public class StructureTreeView extends JPanel {
    private MindMapModel model;
    private JTree tree;
    private DefaultTreeModel treeModel;
    private boolean isUpdatingFromUser = false;

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
            if (isUpdatingFromUser) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof MindNode) {
                model.setSelectedNode((MindNode) node.getUserObject());
            }
        });

        model.addChangeListener(this::refreshTree);
    }
    
    private void refreshTree() {
        isUpdatingFromUser = true;
        
        DefaultMutableTreeNode treeRoot = buildTreeModel(model.getRoot());
        treeModel = new DefaultTreeModel(treeRoot);
        tree.setModel(treeModel);
        
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        
        MindNode selNode = model.getSelectedNode();
        if (selNode != null && selNode.getTreeNodeObj() != null) {
            TreePath path = new TreePath(selNode.getTreeNodeObj().getPath());
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
        }
        
        isUpdatingFromUser = false;
    }

    private DefaultMutableTreeNode buildTreeModel(MindNode node) {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);
        node.setTreeNodeObj(treeNode);
        for (MindNode child : node.getChildren()) {
            treeNode.add(buildTreeModel(child));
        }
        return treeNode;
    }

    private static class MindTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof DefaultMutableTreeNode) {
                Object uo = ((DefaultMutableTreeNode) value).getUserObject();
                if (uo instanceof MindNode) {
                    setText(((MindNode)uo).getText());
                }
            }
            return this;
        }
    }
}