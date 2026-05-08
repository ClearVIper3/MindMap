package mindmap.engine;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 布局引擎工厂。把"布局名称 → 引擎实例"的映射从调用方收敛到这里，
 * 新增一种布局类型只需要在 {@link #register} 里加一行，而不必修改 DrawingPanel
 * 或任何其他调用方（满足 OCP）。
 *
 * <p>使用 Supplier 而非直接持有实例，保证引擎无状态或每次使用独立状态。
 */
public final class LayoutEngineFactory {

    /** 默认布局名。由 UI 层（如工具栏下拉框默认值）使用。 */
    public static final String DEFAULT_LAYOUT = "Balanced";

    private static final Map<String, Supplier<LayoutEngine>> REGISTRY = new LinkedHashMap<>();

    static {
        register("Balanced", AutoLayoutEngine::new);
        register("Right-Flow", DirectionalLayoutEngine::new);
        register("Left-Flow", DirectionalLayoutEngine::new);
    }

    private LayoutEngineFactory() {}

    /** 注册一种新的布局类型。可在应用启动阶段由扩展点调用。 */
    public static void register(String layoutName, Supplier<LayoutEngine> supplier) {
        REGISTRY.put(layoutName, supplier);
    }

    /** 根据布局名称创建引擎。未知名称时回退到默认布局。 */
    public static LayoutEngine create(String layoutName) {
        Supplier<LayoutEngine> s = REGISTRY.get(layoutName);
        if (s == null) s = REGISTRY.get(DEFAULT_LAYOUT);
        return s.get();
    }

    /** 返回所有已注册的布局名（保持注册顺序），供工具栏下拉框使用。 */
    public static List<String> availableLayouts() {
        return List.copyOf(REGISTRY.keySet());
    }

    /** 仅测试/诊断用：是否已注册。 */
    public static boolean isRegistered(String layoutName) {
        return REGISTRY.containsKey(layoutName);
    }

    /** 仅测试用：导出只读快照。 */
    static Map<String, Supplier<LayoutEngine>> snapshot() {
        return Collections.unmodifiableMap(REGISTRY);
    }
}
