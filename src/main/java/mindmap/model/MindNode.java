package mindmap.model;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MindNode implements Serializable {
    private static final long serialVersionUID = 1L;
    private String text;
    private MindNode parent;
    private List<MindNode> children = new ArrayList<>();
    
    private transient int x, y, width, height;
    private transient DefaultMutableTreeNode treeNodeObj;

    public MindNode(String text) {
        this.text = text;
    }

    public void addChild(MindNode child) {
        child.parent = this;
        children.add(child);
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
    
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public DefaultMutableTreeNode getTreeNodeObj() { return treeNodeObj; }
    public void setTreeNodeObj(DefaultMutableTreeNode treeNodeObj) { this.treeNodeObj = treeNodeObj; }
}