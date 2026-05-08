package mindmap.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 纯粹的业务数据：只维护文本与父子关系。
 * 不再持有任何 Swing/渲染相关状态 —— 坐标由 LayoutResult 管理，
 * Tree 映射由 StructureTreeView 自行维护。
 */
public class MindNode implements Serializable {
    private static final long serialVersionUID = 1L;
    private String text;
    private MindNode parent;
    private List<MindNode> children = new ArrayList<>();

    public MindNode(String text) {
        this.text = text;
    }

    public void addChild(MindNode child) {
        child.parent = this;
        children.add(child);
    }

    /** 在指定索引处插入子节点。 */
    public void insertChild(int index, MindNode child) {
        child.parent = this;
        children.add(index, child);
    }

    /** 在当前节点之后插入兄弟节点；若当前节点没有父节点则直接忽略。 */
    public void addSiblingAfter(MindNode sibling) {
        if (parent == null) return;
        int idx = parent.children.indexOf(this);
        if (idx < 0) parent.addChild(sibling);
        else parent.insertChild(idx + 1, sibling);
    }

    public void remove() {
        if (parent != null) {
            parent.children.remove(this);
        }
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public MindNode getParent() { return parent; }
    public void setParent(MindNode parent) { this.parent = parent; }
    public List<MindNode> getChildren() { return children; }
}