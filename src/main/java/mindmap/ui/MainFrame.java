package mindmap.ui;

import mindmap.model.MindMapModel;
import mindmap.model.MindNode;
import mindmap.util.FileHandler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class MainFrame extends JFrame {
    private MindMapModel model;
    private DrawingPanel drawingPanel;
    private StructureTreeView treeView;
    private ToolbarView toolbarView;
    private JLabel statusLabel;

    public MainFrame(MindMapModel model) {
        this.model = model;
        
        setTitle("Java Core Technology - Mind Mapping Tool");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        initUI();
        
        model.addChangeListener(this::updateStatus);
        model.notifyListeners(); // Initial render trigger
    }

    private void initUI() {
        drawingPanel = new DrawingPanel(model);
        treeView = new StructureTreeView(model);
        toolbarView = new ToolbarView(model, this::saveFile, this::openFile, this::exportImage);
        
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
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(" File: " + model.getCurrentFileName() + " | Layout: " + model.getCurrentLayout() + " | Zoom, Pan, Explore");
        });
    }

    private void saveFile() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(model.getCurrentFileName()));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getAbsolutePath().endsWith(".dt")) f = new File(f.getAbsolutePath() + ".dt");
            
            try {
                FileHandler.saveModel(model.getRoot(), f);
                model.setCurrentFileName(f.getName());
                JOptionPane.showMessageDialog(this, "Masterpiece saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to save: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openFile() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                MindNode loadedRoot = FileHandler.loadModel(fc.getSelectedFile());
                model.setRoot(loadedRoot);
                model.setSelectedNode(loadedRoot);
                model.setCurrentFileName(fc.getSelectedFile().getName());
                drawingPanel.resetTransform();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to open file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportImage() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(model.getCurrentFileName().replace(".dt", ".png")));
        
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                FileHandler.exportImage(model.getRoot(), drawingPanel, f, drawingPanel.getBgColor());
                JOptionPane.showMessageDialog(this, "Image exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}