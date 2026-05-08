package mindmap.model;

import java.util.ArrayList;
import java.util.List;

public class MindMapModel {
    private MindNode root;
    private MindNode selectedNode;
    private String currentLayout = "Balanced";
    private String currentFileName = "Untitled.dt";

    private final List<ChangeListener> listeners = new ArrayList<>();

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
        fire(ChangeType.STRUCTURE_CHANGED);
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * 对外广播事件。一般由 Controller / Model 本身调用；
     * View 不应主动调用此方法。
     */
    public void fire(ChangeType type) {
        // 拷贝一份避免监听器内部修改 listeners 引发并发修改异常
        for (ChangeListener l : new ArrayList<>(listeners)) {
            l.onChange(type);
        }
    }

    public MindNode getRoot() { return root; }
    public void setRoot(MindNode root) {
        this.root = root;
        fire(ChangeType.STRUCTURE_CHANGED);
    }

    public MindNode getSelectedNode() { return selectedNode; }
    public void setSelectedNode(MindNode selectedNode) {
        if (this.selectedNode == selectedNode) return;
        this.selectedNode = selectedNode;
        fire(ChangeType.SELECTION_CHANGED);
    }

    public String getCurrentLayout() { return currentLayout; }
    public void setCurrentLayout(String currentLayout) {
        if (java.util.Objects.equals(this.currentLayout, currentLayout)) return;
        this.currentLayout = currentLayout;
        fire(ChangeType.LAYOUT_CHANGED);
    }

    public String getCurrentFileName() { return currentFileName; }
    public void setCurrentFileName(String currentFileName) {
        this.currentFileName = currentFileName;
        fire(ChangeType.FILE_CHANGED);
    }
}