package mindmap.model;

import java.util.EnumSet;

/**
 * Model 变更监听器。允许订阅者声明自己关心的事件类型集合。
 */
@FunctionalInterface
public interface ChangeListener {
    void onChange(ChangeType type);

    /** 便捷：仅关注某些事件类型的监听器包装。 */
    static ChangeListener filtered(EnumSet<ChangeType> interested, Runnable action) {
        return type -> {
            if (interested.contains(type)) action.run();
        };
    }
}
