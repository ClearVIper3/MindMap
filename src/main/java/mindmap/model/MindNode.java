package mindmap.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MindNode implements Serializable {
    /** 序列化版本号，保证反序列化兼容性；若类结构变更需递增此值 */
    private static final long serialVersionUID = 1L;
    /** 节点显示文本 */
    private String text;
    /** 父节点引用（transient 语义：序列化时不保存，反序列化后由 restoreParents() 重建） */
    private MindNode parent;
    /** 子节点列表，维护树形结构的核心数据；final保证引用不变，内容可增删 */
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
