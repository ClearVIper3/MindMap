package mindmap.ui;

import mindmap.model.MindMapModel;
import mindmap.model.MindNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class ToolbarView extends JToolBar {
    private MindMapModel model;
    private Runnable onSave;
    private Runnable onOpen;
    private Runnable onExport;

    public ToolbarView(MindMapModel model, Runnable onSave, Runnable onOpen, Runnable onExport) {
        this.model = model;
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

        String[] layouts = {"Balanced", "Right-Flow", "Left-Flow"};
        JComboBox<String> layoutCombo = new JComboBox<>(layouts);
        layoutCombo.setMaximumSize(new Dimension(120, 30));

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

        btnNew.addActionListener(e -> {
            model.initDefaultData();
            model.setCurrentFileName("Untitled.dt");
        });
        
        btnOpen.addActionListener(e -> onOpen.run());
        btnSave.addActionListener(e -> onSave.run());
        btnExport.addActionListener(e -> onExport.run());
        
        btnAddChild.addActionListener(e -> {
            MindNode selected = model.getSelectedNode();
            if (selected != null) {
                String val = (String)JOptionPane.showInputDialog(this, "Node Text:", "Add Child", JOptionPane.PLAIN_MESSAGE, null, null, "New Node");
                if (val != null && !val.trim().isEmpty()) {
                    selected.addChild(new MindNode(val.trim()));
                    model.notifyListeners();
                }
            }
        });
        
        btnAddSibling.addActionListener(e -> {
            MindNode selected = model.getSelectedNode();
            if (selected != null && selected.getParent() != null) {
                String val = (String)JOptionPane.showInputDialog(this, "Node Text:", "Add Sibling", JOptionPane.PLAIN_MESSAGE, null, null, "New Node");
                if (val != null && !val.trim().isEmpty()) {
                    selected.getParent().addChild(new MindNode(val.trim()));
                    model.notifyListeners();
                }
            }
        });

        btnRename.addActionListener(e -> {
            MindNode selected = model.getSelectedNode();
            if (selected != null) {
                String val = (String)JOptionPane.showInputDialog(this, "Rename Node:", "Rename", JOptionPane.PLAIN_MESSAGE, null, null, selected.getText());
                if (val != null && !val.trim().isEmpty()) {
                    selected.setText(val.trim());
                    model.notifyListeners();
                }
            }
        });

        btnDelete.addActionListener(e -> {
            MindNode selected = model.getSelectedNode();
            if (selected != null && selected != model.getRoot()) {
                selected.remove();
                model.setSelectedNode(model.getRoot());
            }
        });

        layoutCombo.addActionListener(e -> {
            model.setCurrentLayout((String) layoutCombo.getSelectedItem());
        });
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