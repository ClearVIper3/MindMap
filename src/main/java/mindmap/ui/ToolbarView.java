package mindmap.ui;

import mindmap.controller.MindMapController;
import mindmap.engine.LayoutEngineFactory;
import mindmap.model.ChangeType;
import mindmap.model.MindMapModel;
import mindmap.model.MindNode;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;

/**
 * 工具栏视图：只负责"按钮/下拉框 → Controller"的事件分发；
 * 不再直接操作 Model 的业务字段（除了读取状态用于 UI 同步）。
 */
public class ToolbarView extends JToolBar {
    private final MindMapController controller;
    private final MindMapModel model;
    private final Runnable onSave;
    private final Runnable onOpen;
    private final Runnable onExport;

    /** 标记当前下拉框变化是否由 Model 同步触发，避免回环。 */
    private boolean syncingFromModel = false;

    public ToolbarView(MindMapController controller,
                       Runnable onSave, Runnable onOpen, Runnable onExport) {
        this.controller = controller;
        this.model = controller.getModel();
        this.onSave = onSave;
        this.onOpen = onOpen;
        this.onExport = onExport;

        setBackground(Color.WHITE);
        setFloatable(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        initButtons();
    }

    private void initButtons() {
        JButton btnNew = createToolBtn("New");
        JButton btnOpen = createToolBtn("Open");
        JButton btnSave = createToolBtn("Save (.dt)");
        JButton btnExport = createToolBtn("Export Image");
        JButton btnAddChild = createToolBtn("+ Child");
        JButton btnAddSibling = createToolBtn("+ Sibling");
        JButton btnRename = createToolBtn("Rename");
        JButton btnDelete = createToolBtn("Delete");

        String[] layouts = LayoutEngineFactory.availableLayouts().toArray(new String[0]);
        JComboBox<String> layoutCombo = new JComboBox<>(layouts);
        layoutCombo.setMaximumSize(new Dimension(120, 30));
        layoutCombo.setSelectedItem(model.getCurrentLayout());

        add(btnNew); add(Box.createHorizontalStrut(5));
        add(btnOpen); add(Box.createHorizontalStrut(5));
        add(btnSave); add(Box.createHorizontalStrut(15));
        add(btnExport); add(Box.createHorizontalStrut(15));
        add(btnAddChild); add(Box.createHorizontalStrut(5));
        add(btnAddSibling); add(Box.createHorizontalStrut(5));
        add(btnRename); add(Box.createHorizontalStrut(5));
        add(btnDelete); add(Box.createHorizontalStrut(15));
        add(new JLabel(" Layout: "));
        add(layoutCombo);

        btnNew.addActionListener(e -> controller.newMindMap());
        btnOpen.addActionListener(e -> onOpen.run());
        btnSave.addActionListener(e -> onSave.run());
        btnExport.addActionListener(e -> onExport.run());

        btnAddChild.addActionListener(e -> {
            MindNode selected = model.getSelectedNode();
            if (selected == null) return;
            String val = prompt("Node Text:", "Add Child", "New Node");
            controller.addChildToSelected(val);
        });

        btnAddSibling.addActionListener(e -> {
            MindNode selected = model.getSelectedNode();
            if (selected == null || selected.getParent() == null) return;
            String val = prompt("Node Text:", "Add Sibling", "New Node");
            controller.addSiblingToSelected(val);
        });

        btnRename.addActionListener(e -> {
            MindNode selected = model.getSelectedNode();
            if (selected == null) return;
            String val = prompt("Rename Node:", "Rename", selected.getText());
            controller.renameSelected(val);
        });

        btnDelete.addActionListener(e -> controller.deleteSelected());

        // Combo → Model
        layoutCombo.addActionListener(e -> {
            if (syncingFromModel) return;
            Object sel = layoutCombo.getSelectedItem();
            if (sel instanceof String) controller.switchLayout((String) sel);
        });

        // Model → Combo（双向绑定，修复"外部改动 layout 时下拉框不跟随"）
        model.addChangeListener(type -> {
            if (type != ChangeType.LAYOUT_CHANGED && type != ChangeType.STRUCTURE_CHANGED) return;
            String cur = model.getCurrentLayout();
            if (cur == null || cur.equals(layoutCombo.getSelectedItem())) return;
            syncingFromModel = true;
            try {
                layoutCombo.setSelectedItem(cur);
            } finally {
                syncingFromModel = false;
            }
        });
    }

    private String prompt(String message, String title, String initial) {
        return (String) JOptionPane.showInputDialog(
                this, message, title, JOptionPane.PLAIN_MESSAGE, null, null, initial);
    }

    private JButton createToolBtn(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(6, 12, 6, 12)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}