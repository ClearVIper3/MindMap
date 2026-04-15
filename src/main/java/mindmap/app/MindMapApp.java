package mindmap.app;

import mindmap.model.MindMapModel;
import mindmap.ui.MainFrame;

import javax.swing.*;

public class MindMapApp {
    public static void main(String[] args) {
        // Run application on the EDT
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                System.out.println("Could not use system LookAndFeel");
            }
            
            MindMapModel model = new MindMapModel();
            MainFrame frame = new MainFrame(model);
            frame.setVisible(true);
        });
    }
}