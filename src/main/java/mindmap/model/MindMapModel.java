package mindmap.model;

import java.util.ArrayList;
import java.util.List;

public class MindMapModel {
    private MindNode root;
    private MindNode selectedNode;
    private String currentLayout = "Balanced";
    private String currentFileName = "Untitled.dt";
    
    private List<Runnable> changeListeners = new ArrayList<>();

    public MindMapModel() {
        initDefaultData();
    }

    public void initDefaultData() {
        root = new MindNode("Java Core Technology");
        root.addChild(new MindNode("Collections"));
        root.addChild(new MindNode("Concurrency"));
        root.addChild(new MindNode("JVM Architecture"));
        root.addChild(new MindNode("I/O & NIO"));
        selectedNode = root;
        notifyListeners();
    }

    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    public void notifyListeners() {
        for (Runnable r : changeListeners) {
            r.run();
        }
    }

    public MindNode getRoot() { return root; }
    public void setRoot(MindNode root) { this.root = root; notifyListeners(); }
    
    public MindNode getSelectedNode() { return selectedNode; }
    public void setSelectedNode(MindNode selectedNode) { this.selectedNode = selectedNode; notifyListeners(); }
    
    public String getCurrentLayout() { return currentLayout; }
    public void setCurrentLayout(String currentLayout) { this.currentLayout = currentLayout; notifyListeners(); }
    
    public String getCurrentFileName() { return currentFileName; }
    public void setCurrentFileName(String currentFileName) { this.currentFileName = currentFileName; notifyListeners(); }
}