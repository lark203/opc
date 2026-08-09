package com.atlantafx.core.config;

import com.atlantafx.core.constant.NotificationLevel;
import com.atlantafx.core.event.EventBus;
import com.atlantafx.core.event.NotificationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 纯 KV 配置持久化
 * <p>
 * 读取/写入扁平的 JSON 配置文件，不感知 AppState 或主题逻辑。
 */
public final class ConfigStore {
    private static final Logger log = LoggerFactory.getLogger(ConfigStore.class);
    private static final String CONFIG_FILE = Path.of("data", "config.json").toAbsolutePath().toString();
    private static final ObjectMapper mapper = new ObjectMapper();

    private static Map<String, String> data = new HashMap<>();

    private ConfigStore() {
    }

    // ==================== 加载 / 保存 ====================

    /**
     * 从磁盘加载配置到内存
     */
    public static void load() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try {
                Map<String, String> loaded = mapper.readValue(file, Map.class);
                data = loaded;
            } catch (Exception e) {
                log.error("加载配置失败", e);
            }
        }
    }

    /**
     * 将内存中的配置持久化到磁盘
     */
    public static void save() {
        try {
            mapper.writeValue(new File(CONFIG_FILE), data);
        } catch (Exception e) {
            EventBus.publish(new NotificationEvent("保存失败: " + e.getMessage(), NotificationLevel.ERROR));
        }
    }

    /**
     * 保存并持久化配置
     */
    public static void save(String key, String value) {
        set(key, value);
        save();
    }

    // ==================== String 访问 ====================

    public static String get(String key) {
        return data.get(key);
    }

    public static void set(String key, String value) {
        data.put(key, value);
    }

    public static boolean has(String key) {
        return data.containsKey(key);
    }

    // ==================== 类型化访问 ====================

    public static String getString(String key, String defaultValue) {
        String v = data.get(key);
        return v != null ? v : defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String v = data.get(key);
        if (v == null) return defaultValue;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String v = data.get(key);
        if (v == null) return defaultValue;
        return Boolean.parseBoolean(v);
    }

    // ==================== 批量访问（AppState 同步用） ====================

    public static Map<String, String> getAll() {
        return Collections.unmodifiableMap(data);
    }

    /**
     * 批量写入（不覆盖未涉及的 key）
     */
    public static void putAll(Map<String, String> entries) {
        data.putAll(entries);
    }
}
