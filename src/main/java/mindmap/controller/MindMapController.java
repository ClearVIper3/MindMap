package mindmap.controller;

import mindmap.model.ChangeType;
import mindmap.model.MindMapModel;
import mindmap.model.MindNode;
import mindmap.ui.DrawingPanel;
import mindmap.util.FileHandler;

import java.awt.Color;
import java.io.File;

/**
 * 业务逻辑控制器。承担以下职责：
 * <ul>
 *   <li>协调 View 层请求（保存 / 打开 / 导出 / 新建 / 增删改）与 Model 的更新</li>
 *   <li>负责触发合适的 {@link ChangeType} 事件</li>
 * </ul>
 *
 * <p>与 View 的交互约定：View 不直接操作 Model 的业务字段，而是调用 Controller 暴露的方法。
 * 真正的文件对话框、用户确认等 UI 交互仍留在 View（MainFrame）中 —— Controller 只关心"文件选好了，接下来做什么"。
 *
 * <p>Controller 与具体 UI 解耦，仅通过 {@link FileExportTarget} 接口访问导出时需要的
 * 布局/背景色，这样 Controller 无需 import 任何具体 UI 组件类型（如 DrawingPanel）。
 */
public class MindMapController {

    private final MindMapModel model;

    public MindMapController(MindMapModel model) {
        this.model = model;
    }

    public MindMapModel getModel() {
        return model;
    }

    // ---------------- 文件操作 ----------------

    public void saveFile(File file) throws Exception {
        File target = file;
        if (!target.getAbsolutePath().endsWith(".dt")) {
            target = new File(target.getAbsolutePath() + ".dt");
        }
        FileHandler.saveModel(model.getRoot(), target);
        model.setCurrentFileName(target.getName());
    }

    public void openFile(File file) throws Exception {
        MindNode loadedRoot = FileHandler.loadModel(file);
        model.setRoot(loadedRoot);
        model.setSelectedNode(loadedRoot);
        model.setCurrentFileName(file.getName());
    }

    public void exportImage(File file, FileExportTarget target) throws Exception {
        FileHandler.exportImage(model.getRoot(), target.getDrawingPanel(), file, target.getBackgroundColor());
    }

    public void newMindMap() {
        model.initDefaultData();
        model.setCurrentFileName("Untitled.dt");
    }

    // ---------------- 节点操作 ----------------

    public void addChildToSelected(String text) {
        MindNode selected = model.getSelectedNode();
        if (selected == null || text == null || text.trim().isEmpty()) return;
        selected.addChild(new MindNode(text.trim()));
        model.fire(ChangeType.STRUCTURE_CHANGED);
    }

    public void addSiblingToSelected(String text) {
        MindNode selected = model.getSelectedNode();
        if (selected == null || selected.getParent() == null
                || text == null || text.trim().isEmpty()) return;
        selected.addSiblingAfter(new MindNode(text.trim()));
        model.fire(ChangeType.STRUCTURE_CHANGED);
    }

    public void renameSelected(String newText) {
        MindNode selected = model.getSelectedNode();
        if (selected == null || newText == null || newText.trim().isEmpty()) return;
        selected.setText(newText.trim());
        model.fire(ChangeType.STRUCTURE_CHANGED);
    }

    public void deleteSelected() {
        MindNode selected = model.getSelectedNode();
        if (selected == null || selected == model.getRoot()) return;
        selected.remove();
        model.setSelectedNode(model.getRoot());
        model.fire(ChangeType.STRUCTURE_CHANGED);
    }

    // ---------------- 布局切换 ----------------

    public void switchLayout(String layoutName) {
        model.setCurrentLayout(layoutName);
    }

    /**
     * 导出图片时需要的外部依赖。抽成接口是为了让 Controller 不直接依赖 DrawingPanel，
     * 仍可在 P2 阶段进一步把离屏渲染逻辑下沉时替换实现。
     */
    public interface FileExportTarget {
        DrawingPanel getDrawingPanel();
        Color getBackgroundColor();
    }
}
