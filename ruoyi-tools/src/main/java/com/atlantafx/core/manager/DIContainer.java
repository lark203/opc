package com.atlantafx.core.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 轻量级依赖注入容器
 * <p>
 * 支持：
 * <ul>
 *   <li>单例注册：手动注册实例或自动单例化</li>
 *   <li>接口绑定：将接口/抽象类绑定到具体实现</li>
 *   <li>构造器注入：自动解析构造函数参数</li>
 * </ul>
 */
public class DIContainer {
    private static final Logger log = LoggerFactory.getLogger(DIContainer.class);

    /** 已注册的单例实例 */
    private static final Map<Class<?>, Object> SINGLETONS = new ConcurrentHashMap<>();

    /** 接口 -> 实现类的绑定 */
    private static final Map<Class<?>, Class<?>> BINDINGS = new ConcurrentHashMap<>();

    // ==================== 注册 API ====================

    /**
     * 注册单例实例
     */
    public static <T> void registerSingleton(Class<T> type, T instance) {
        SINGLETONS.put(type, instance);
        log.debug("注册单例实例: {}", type.getName());
    }

    /**
     * 注册单例类型（首次 get 时实例化）
     */
    public static <T> void registerSingleton(Class<T> type) {
        BINDINGS.put(type, type);
        log.debug("注册单例类型: {}", type.getName());
    }

    /**
     * 绑定接口到实现类
     */
    public static <T> void bind(Class<T> interfaceType, Class<? extends T> implementationType) {
        BINDINGS.put(interfaceType, implementationType);
        log.debug("绑定接口 {} -> {}", interfaceType.getName(), implementationType.getName());
    }

    // ==================== 获取 API ====================

    /**
     * 获取或创建一个类的实例
     * <p>
     * 查找顺序：已注册单例实例 -> 绑定的实现类 -> 直接实例化
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        // 1. 检查单例缓存
        Object existing = SINGLETONS.get(clazz);
        if (existing != null) {
            return (T) existing;
        }

        // 2. 解析绑定的实现类
        Class<?> implementation = BINDINGS.getOrDefault(clazz, clazz);

        try {
            // 3. 实例化
            T instance = createInstance((Class<T>) implementation);

            // 4. 如果注册为单例类型，缓存实例
            if (BINDINGS.containsKey(clazz) && !SINGLETONS.containsKey(clazz)) {
                SINGLETONS.put(clazz, instance);
            }

            return instance;
        } catch (Exception e) {
            log.error("DIContainer 实例化失败: {}", clazz.getName(), e);
            return null;
        }
    }

    // ==================== 内部方法 ====================

    @SuppressWarnings("unchecked")
    private static <T> T createInstance(Class<T> clazz) throws Exception {
        Constructor<?>[] constructors = clazz.getConstructors();
        if (constructors.length == 0) {
            return clazz.getDeclaredConstructor().newInstance();
        }

        // 选择无参构造函数优先，否则选第一个构造函数进行注入
        Constructor<?> constructor = constructors[0];
        for (Constructor<?> c : constructors) {
            if (c.getParameterCount() == 0) {
                constructor = c;
                break;
            }
        }

        Object[] parameters = new Object[constructor.getParameterCount()];
        Class<?>[] parameterTypes = constructor.getParameterTypes();

        for (int i = 0; i < parameterTypes.length; i++) {
            parameters[i] = get(parameterTypes[i]);
        }

        return (T) constructor.newInstance(parameters);
    }
}
