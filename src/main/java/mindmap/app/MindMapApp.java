package mindmap.app;

import mindmap.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MindMapApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new MainFrame().setVisible(true);
        });
    }
}
