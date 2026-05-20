package mindmap.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MindNode implements Serializable {
    private static final long serialVersionUID = 1L;
    private String text;
    private MindNode parent;
    private final List<MindNode> children = new ArrayList<>();

    public MindNode(String text) { this.text = text; }

    public void addChild(MindNode c) { c.parent = this; children.add(c); }

    public void addSiblingAfter(MindNode s) {
        if (parent == null) return;
        int i = parent.children.indexOf(this);
        s.parent = parent;
        if (i < 0) parent.children.add(s); else parent.children.add(i + 1, s);
    }

    public void remove() { if (parent != null) parent.children.remove(this); }

    public String getText() { return text; }
    public void setText(String t) { text = t; }
    public MindNode getParent() { return parent; }
    public List<MindNode> getChildren() { return children; }

    /** 反序列化后恢复 parent 引用 */
    public void restoreParents() {
        for (MindNode c : children) { c.parent = this; c.restoreParents(); }
    }
}
