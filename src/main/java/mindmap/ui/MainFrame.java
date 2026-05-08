package mindmap.ui;

import mindmap.controller.MindMapController;
import mindmap.model.ChangeType;
import mindmap.model.MindMapModel;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

/**
 * 主窗口：只负责 UI 布局与"交互式 I/O"（文件对话框、消息框）。
 * 所有修改 Model 的业务逻辑都交给 {@link MindMapController}。
 */
public class MainFrame extends JFrame implements MindMapController.FileExportTarget {
    private final MindMapModel model;
    private final MindMapController controller;
    private DrawingPanel drawingPanel;
    private StructureTreeView treeView;
    private ToolbarView toolbarView;
    private JLabel statusLabel;

    public MainFrame(MindMapModel model) {
        this.model = model;
        this.controller = new MindMapController(model);

        setTitle("Java Core Technology - Mind Mapping Tool");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initUI();

        // 状态栏只关心文件名 / 布局变化
        model.addChangeListener(type -> {
            if (type == ChangeType.FILE_CHANGED || type == ChangeType.LAYOUT_CHANGED) {
                updateStatus();
            }
        });
        updateStatus();
        // 初始触发一次结构事件，让各 View 完成首次渲染
        model.fire(ChangeType.STRUCTURE_CHANGED);
    }

    private void initUI() {
        drawingPanel = new DrawingPanel(model);
        treeView = new StructureTreeView(model);
        toolbarView = new ToolbarView(controller, this::saveFile, this::openFile, this::exportImage);

        treeView.setPreferredSize(new Dimension(250, 0));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, drawingPanel, treeView);
        splitPane.setResizeWeight(1.0);
        splitPane.setDividerSize(4);
        splitPane.setBorder(null);

        statusLabel = new JLabel(" Ready | Drag canvas to Pan, Scroll to Zoom");
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusLabel.setForeground(Color.GRAY);

        add(toolbarView, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void updateStatus() {
        SwingUtilities.invokeLater(() -> statusLabel.setText(
                " File: " + model.getCurrentFileName()
                        + " | Layout: " + model.getCurrentLayout()
                        + " | Zoom, Pan, Explore"));
    }

    // ---------- UI 交互式 I/O ----------

    private void saveFile() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(model.getCurrentFileName()));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            controller.saveFile(fc.getSelectedFile());
            JOptionPane.showMessageDialog(this, "Masterpiece saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFile() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            controller.openFile(fc.getSelectedFile());
            drawingPanel.resetTransform();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportImage() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(model.getCurrentFileName().replace(".dt", ".png")));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            controller.exportImage(fc.getSelectedFile(), this);
            JOptionPane.showMessageDialog(this, "Image exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- FileExportTarget 实现 ----------

    @Override
    public DrawingPanel getDrawingPanel() {
        return drawingPanel;
    }

    @Override
    public Color getBackgroundColor() {
        return drawingPanel.getBgColor();
    }
}