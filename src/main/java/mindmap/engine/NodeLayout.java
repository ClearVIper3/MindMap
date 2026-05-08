package mindmap.engine;

/**
 * 纯视图层几何信息：节点在画布上的位置和尺寸。
 * 与业务数据 (MindNode) 分离，避免 Model 污染 View 状态。
 */
public final class NodeLayout {
    private int x;
    private int y;
    private int width;
    private int height;

    public NodeLayout() {
    }

    public NodeLayout(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
}
